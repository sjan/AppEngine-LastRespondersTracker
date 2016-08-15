package org.lastresponders.tracker.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SheetsTestService {
	private static final Logger log = Logger.getLogger(SheetsTestService.class
			.getName());
    /** Application name. */
    private static final String APPLICATION_NAME =
        "lastresponderstracker";
    
    private static final String SPREADSHEET_ID = "1tiVCjheex7q5c-N5ZHWQ9nP9ZbGRCoNRTWAWds09GzA";
    private static final String SPREADSHEET_SHEETID = "Route without Kyrgyzstan";
    private static final String SPREADSHEET_RANGE = "A2:T76";
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
	
    
    public static Sheets getSheetsService() throws IOException {
        Credential credential = new Credential(BearerToken
                .authorizationHeaderAccessMethod())
                .setFromTokenResponse(new TokenResponse().setAccessToken(ACCESS_TOKEN));
                return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void test() throws IOException {
    	 Sheets service = getSheetsService();
         
         String range = SPREADSHEET_SHEETID +"!" +SPREADSHEET_RANGE;
         ValueRange response = service.spreadsheets().values()
             .get(SPREADSHEET_ID, range)
             .execute();
         
         List<List<Object>> values = response.getValues();
         
         if (values == null || values.size() == 0) {
           System.out.println("No data found.");
         } else {
           System.out.println("Date, City, Lat, Long");           
           Iterator <List<Object>>iterator = values.iterator();
           iterator.next(); //skip first two lines
           iterator.next();
           while (iterator.hasNext()) {
        	   List <Object> row = iterator.next();
        	   if(row.size() >= 18) {
	        	   System.out.printf("%s, %s, [%s, %s]\n", row.get(1), row.get(5),row.get(18),row.get(19));
        	   }
           }
         }
    }
}