package com.fitwise.rest.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/")
@Slf4j
public class GoogleGDriveAccessController {
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,DriveScopes.DRIVE_FILE,
            "https://www.googleapis.com/auth/drive.install");

    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";

    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

    @Value("${google.secret.key.path}")
    private Resource gdSecretKeys;

    @Value("${google.credentials.folder.path}")
    private String credentialsFolderPath;

    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init() throws IOException {
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(gdSecretKeys.getInputStream()));
        java.io.File file = new java.io.File(credentialsFolderPath);
        if(!file.exists()){
            file.mkdirs();
        }
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(file)).build();
    }

    @GetMapping(value = { "/googlesignin" })
    public void doGoogleSignIn(HttpServletResponse response) throws IOException {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").setApprovalPrompt("force").build();
        response.sendRedirect(redirectURL);
    }

    @GetMapping(value = { "/gauth" })
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            saveToken(code);
            return "Connected";
        }
        return "Not connected";
    }

    private void saveToken(String code) throws IOException {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
    }

    public void downloadFiles(String folderId, String pathToDownload) throws IOException {
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("FWDRIVE").build();
        log.info("Download Started -  ");
        String pageToken = null;
        do{
            FileList fileList = drive.files().list().setQ(" '" +folderId+ "' in parents ").setPageToken(pageToken).execute();
            for(File file : fileList.getFiles()){
                log.info("File Name : " + file.getName());
                FileOutputStream fileOutputStream = null;
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    drive.files().get(file.getId())
                            .executeMediaAndDownloadTo(outputStream);
                    java.io.File downloadTo = new java.io.File(pathToDownload);
                    if(!downloadTo.exists()){
                        downloadTo.mkdirs();
                    }
                    fileOutputStream = new FileOutputStream(pathToDownload + file.getName());
                    fileOutputStream.write(outputStream.toByteArray());
                    outputStream.flush();
                    outputStream.close();
                    fileOutputStream.flush();
                } catch (Exception exception) {
                    log.error("GDrive Download failed : " + exception.getMessage());
                }finally {
                    if(fileOutputStream != null){
                        fileOutputStream.close();
                    }
                }
            }
            log.info("Size : " + fileList.getFiles().size());
            pageToken = fileList.getNextPageToken();
            log.info("Page token " + pageToken);
        }while(pageToken != null);
        log.info("Download Completed - ");
    }

}
