/*
 * Copyright 2012 Jelastic.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lindar.teamcity.jelastic.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lindar.teamcity.jelastic.api.model.AuthenticationResponse;
import com.lindar.teamcity.jelastic.api.model.CreateObjectResponse;
import com.lindar.teamcity.jelastic.api.model.DeployResponse;
import com.lindar.teamcity.jelastic.api.model.UploadResponse;
import jetbrains.buildServer.agent.BuildProgressLogger;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JelasticService {

    public static final String              UTF_8 = "UTF-8";
    private final       BuildProgressLogger buildLogger;
    private final       String              filepath;
    private final       String              apiHoster;
    private final       String              environment;
    private final       String              context;
    private final       String              nodeGroup;

    private String      scheme      = "https";
    private int         port        = -1;
    private Double      version     = 1.0;
    private long        totalSize;
    private int         numSt;
    private CookieStore cookieStore = new BasicCookieStore();

    private String authPath         = "/" + version + "/users/authentication/rest/signin";
    private String uploadPath       = "/" + version + "/storage/uploader/rest/upload";
    private String createObjectPath = "/deploy/createobject";
    private String deployPath       = "/deploy/DeployArchive";


    public AuthenticationResponse authentication(String email, String password) {
        AuthenticationResponse authenticationResponse = null;
        try {
            HttpClient httpclient = buildHttpClientWithSSLAcceptAll().setDefaultCookieStore(this.cookieStore).build();

            URI uri = new URIBuilder().setScheme(this.scheme).setHost(this.apiHoster).setPort(port).setPath(this.authPath).build();
            buildLogger.debug("Authentication url : " + uri);

            HttpPost httpPost = new HttpPost(uri);

            List<NameValuePair> queryParams = new ArrayList<>();
            queryParams.add(new BasicNameValuePair("login", email));
            queryParams.add(new BasicNameValuePair("password", password));
            UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(queryParams);

            httpPost.setEntity(encodedFormEntity);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            String responseBody = EntityUtils.toString(httpResponse.getEntity(), UTF_8);

            buildLogger.message("Authentication response : " + responseBody);

            Gson gson = new GsonBuilder().setVersion(version).create();
            authenticationResponse = gson.fromJson(responseBody, AuthenticationResponse.class);
        } catch (URISyntaxException | IOException e) {
            buildLogger.error(e.getMessage());
        }
        return authenticationResponse;
    }

    public UploadResponse upload(AuthenticationResponse authenticationResponse) {
        UploadResponse uploadResponse = null;
        try {
            HttpClient httpclient = buildHttpClientWithSSLAcceptAll().setDefaultCookieStore(this.cookieStore).build();

            for (Cookie cookie : this.cookieStore.getCookies()) {
                buildLogger.debug(cookie.getName() + " = " + cookie.getValue());
            }

            final File file = findFileInPath(this.filepath).toFile();
            if (!file.exists()) {
                throw new IllegalArgumentException("First build artifact and try again. Artifact not found: " + this.filepath);
            }

            MultipartEntity multipartEntity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, num -> {
                if (((int) ((num / (float) totalSize) * 100)) != numSt) {
                    buildLogger.message("File Uploading : [" + (int) ((num / (float) totalSize) * 100) + "%]");
                    numSt = ((int) ((num / (float) totalSize) * 100));
                }
            });

            multipartEntity.addPart("fid", new StringBody("123456", ContentType.TEXT_PLAIN));
            multipartEntity.addPart("session", new StringBody(authenticationResponse.getSession(), ContentType.TEXT_PLAIN));
            multipartEntity.addPart("file", new FileBody(file));
            totalSize = multipartEntity.getContentLength();

            URI uri = new URIBuilder().setScheme(this.scheme).setHost(this.apiHoster).setPort(port).setPath(this.uploadPath).build();
            buildLogger.message("Upload url : " + uri);

            HttpPost httpPost = new HttpPost(uri);
            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(multipartEntity);

            HttpResponse httpResponse = httpclient.execute(httpPost);
            String responseBody = EntityUtils.toString(httpResponse.getEntity(), UTF_8);

            buildLogger.message("Upload response : " + responseBody);
            Gson gson = new GsonBuilder().setVersion(version).create();
            uploadResponse = gson.fromJson(responseBody, UploadResponse.class);
        } catch (URISyntaxException | IOException e) {
            buildLogger.error(e.getMessage());
        }
        return uploadResponse;
    }

    private Path findFileInPath(String filePathWithRegex) {
        if (!filePathWithRegex.contains("/")) {
            throw new IllegalArgumentException("Invalid artifact file path: " + filePathWithRegex);
        }
        try {
            String directoryToCheck = filePathWithRegex.substring(0, filePathWithRegex.lastIndexOf("/"));
            String fileNameRegex = filePathWithRegex.substring(filePathWithRegex.lastIndexOf("/") + 1);
            List<Path> fileNames = Files.list(Paths.get(directoryToCheck)).filter(file -> fileNameMatches(fileNameRegex, file.getFileName().toString())).collect(Collectors.toList());
            if (fileNames.isEmpty()) {
                throw new IllegalArgumentException("No files found with the given file path: " + filePathWithRegex);
            }
            if (fileNames.size() > 1) {
                throw new IllegalArgumentException(
                        "Multiple files found with the given file path: " + filePathWithRegex + " | " + fileNames.stream().map(Path::toString).collect(Collectors.joining()));
            }
            Path filePath = fileNames.get(0);
            buildLogger.message("One file matches the given file path (or regex): " + filePath.getFileName());
            return filePath;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid artifact file path: " + filePathWithRegex, ex);
        }
    }

    private boolean fileNameMatches(String fileNameRegex, String currentFileName) {
        return Pattern.matches(fileNameRegex, currentFileName);
    }

    public CreateObjectResponse createObject(UploadResponse upLoader, AuthenticationResponse authentication) {
        CreateObjectResponse createObjectResponse = null;
        try {
            HttpClient httpclient = buildHttpClientWithSSLAcceptAll().setDefaultCookieStore(this.cookieStore).build();

            List<NameValuePair> nameValuePairList = new ArrayList<>();
            nameValuePairList.add(new BasicNameValuePair("charset", UTF_8));
            nameValuePairList.add(new BasicNameValuePair("session", authentication.getSession()));
            nameValuePairList.add(new BasicNameValuePair("type", "JDeploy"));
            nameValuePairList.add(new BasicNameValuePair("data",
                                                         "{'name':'" + upLoader.getName() + "', 'archive':'" + upLoader.getFile() + "', 'link':0, 'size':" + upLoader.getSize() + ", 'comment':'" + upLoader.getName() + "'}"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");


            for (NameValuePair nameValuePair : nameValuePairList) {
                buildLogger.message(nameValuePair.getName() + " : " + nameValuePair.getValue());
            }

            URI uri = new URIBuilder().setScheme(this.scheme).setHost(this.apiHoster).setPort(port).setPath(this.createObjectPath).build();
            buildLogger.message("CreateObject url : " + uri.toString());

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            buildLogger.message("CreateObject response : " + responseBody);

            Gson gson = new GsonBuilder().setVersion(version).create();
            createObjectResponse = gson.fromJson(responseBody, CreateObjectResponse.class);
        } catch (URISyntaxException | IOException e) {
            buildLogger.error(e.getMessage());
        }
        return createObjectResponse;
    }

    public DeployResponse deploy(AuthenticationResponse authentication, UploadResponse upLoader) {
        DeployResponse deployResponse = null;
        try {
            HttpClient httpclient = buildHttpClientWithSSLAcceptAll().setDefaultCookieStore(this.cookieStore).build();

            List<NameValuePair> queryParams = new ArrayList<>();
            queryParams.add(new BasicNameValuePair("charset", UTF_8));
            queryParams.add(new BasicNameValuePair("session", authentication.getSession()));
            queryParams.add(new BasicNameValuePair("archiveUri", upLoader.getFile()));
            queryParams.add(new BasicNameValuePair("archiveName", upLoader.getName()));
            queryParams.add(new BasicNameValuePair("newContext", this.context));
            queryParams.add(new BasicNameValuePair("domain", this.environment));

            if (this.nodeGroup != null) {
                queryParams.add(new BasicNameValuePair("nodeGroup", this.nodeGroup));
            }

            for (NameValuePair nameValuePair : queryParams) {
                buildLogger.message(nameValuePair.getName() + " : " + nameValuePair.getValue());
            }

            URI uri = new URIBuilder().setScheme(this.scheme).setHost(this.apiHoster).setPort(port).setPath(this.createObjectPath).setParameters(queryParams).build();
            buildLogger.message("Deploy url : " + uri);

            HttpGet httpPost = new HttpGet(uri);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            buildLogger.message("Deploy response : " + responseBody);
            Gson gson = new GsonBuilder().setVersion(version).create();
            deployResponse = gson.fromJson(responseBody, DeployResponse.class);
        } catch (URISyntaxException | IOException e) {
            buildLogger.error(e.getMessage());
        }
        return deployResponse;
    }


    public static HttpClientBuilder buildHttpClientWithSSLAcceptAll() {
        try {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                                                                              NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                                   .register("https", sslsf)
                                   .register("http", new PlainConnectionSocketFactory())
                                   .build();

            BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
            return HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(connectionManager);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
