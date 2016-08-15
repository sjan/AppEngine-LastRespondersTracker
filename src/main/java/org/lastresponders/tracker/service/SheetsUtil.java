package org.lastresponders.tracker.service;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;

public class SheetsUtil {
	private static final Logger log = Logger.getLogger(SheetsUtil.class
			.getName());
	 private static final String APPLICATION_NAME =
		        "lastresponderstracker";
		    private static final String ACCESS_TOKEN = "ya29.Ci9AAw8eUnCzrN1Pz-EC3frtMNLYfvkOs1H5wr-XzKkhmygDCiMOjcBItYBVnJaSlg";
		    
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
	
    
    public static Sheets getSheet() throws IOException {
        Credential credential = new Credential(BearerToken
                .authorizationHeaderAccessMethod())
                .setFromTokenResponse(new TokenResponse().setAccessToken(ACCESS_TOKEN));
                return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
