package bel.en.test;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;

import java.awt.*;
import java.net.URI;


/**
 * get the principle up and running
 *
 */
public class OAuthTest {

    String CONSUMER_KEY = "bennoloeffler-0962";
    String CONSUMER_SECRET ="4a827fecd658af78";
    String CALLBACK_URL ="http://benno.loeffler.com";
    //Application Name: CREAM

    com.evernote.auth.EvernoteService EVERNOTE_SERVICE = EvernoteService.SANDBOX;
    //String authTokenString;

    public static void main(String[] args) throws Exception{
        OAuthTest oauth = new OAuthTest();
        oauth.testAuth();
    }

    private void testAuth() throws Exception {
        Class providerClass = org.scribe.builder.api.EvernoteApi.class;

        org.scribe.oauth.OAuthService service = new org.scribe.builder.ServiceBuilder()
                .provider(providerClass)
                .apiKey(CONSUMER_KEY)
                .apiSecret(CONSUMER_SECRET)
                .callback(CALLBACK_URL)
                .build();

        org.scribe.model.Token requestTokenObject = service.getRequestToken();
        String authUrl = EVERNOTE_SERVICE.getAuthorizationUrl(requestTokenObject.getToken());
        Desktop.getDesktop().browse(new URI(authUrl));

        String verificationCode = ""; // get that from URL... (or from callback...)
        org.scribe.model.Verifier verifier = new org.scribe.model.Verifier(verificationCode);
        org.scribe.model.Token accessTokenObject = service.getAccessToken(requestTokenObject, verifier);

        com.evernote.auth.EvernoteAuth enAuth = com.evernote.auth.EvernoteAuth.parseOAuthResponse(EVERNOTE_SERVICE, accessTokenObject.getRawResponse());
        String authTokenString = enAuth.getToken();

        // next time it can be used... like that
        EvernoteAuth authObj = new EvernoteAuth(EVERNOTE_SERVICE, authTokenString);

        //String noteStoreUrl = authObj.getNoteStoreUrl();
        com.evernote.clients.NoteStoreClient noteStoreClient = new com.evernote.clients.ClientFactory(authObj).createNoteStoreClient();
        java.util.List<com.evernote.edam.type.Notebook> notebooks = noteStoreClient.listNotebooks();
        for (com.evernote.edam.type.Notebook notebook : notebooks) {
            System.out.println("Notebook: " + notebook.getName());
        }
    }
}
