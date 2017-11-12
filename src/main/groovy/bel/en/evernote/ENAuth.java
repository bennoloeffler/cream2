package bel.en.evernote;

import bel.en.MainGUI;
import bel.en.gui.SimpleSwingBrowser;
import bel.util.RegexUtils;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


/**
 * Handles authentication.
 * Saves the needed stuff in System properties (encryption?)
 * Those need to be saved outside, if needed.
 * They should be available at the next call
 */
@Log4j2
public class ENAuth {

    public static final String EVERNOTE_TOKEN="EVERNOTE_TOKEN";
    //public static final String EVERNOTE_TOKEN_TIME="EVERNOTE_TOKEN_TIME";

    private Properties p;
    private EvernoteOauth10aAPI evernoteApi;
    //private EvernoteService serviceType;


    @Getter
    private UserStoreClient userStore;
    @Getter
    private NoteStoreClient noteStore;
    //@Getter
    private String authToken;

    private static ENAuth enauth = null;

    public static ENAuth get() {
        if(enauth==null) {
            throw new RuntimeException("not initialized");
        }
        return enauth;
    }

    public static ENAuth get(Properties p, EvernoteService type) {
        if(enauth==null) {
            enauth = new ENAuth(p,type);
        } else {
            log.info("init ENAuth again.");
            enauth = new ENAuth(p,type);
        }
        return enauth;
    }

    private ENAuth(Properties p, EvernoteService type) {
        this.p = p;
        authToken = p.getProperty(EVERNOTE_TOKEN); // may not be there...
        evernoteApi = new EvernoteOauth10aAPI(type);
    }



    public SwingWorker<String, String> authOverEvernoteWebsite(JPanel panel) throws InterruptedException, ExecutionException, IOException {
        log.traceEntry();
        final OAuth10aService service = new ServiceBuilder()
                .apiKey("bennoloeffler-2708")
                .apiSecret("7dfe6594731f5751")
                .callback("http://v-und-s.de")
                .build(evernoteApi);

        final OAuth1RequestToken requestToken = service.getRequestToken();
        log.trace("getRequestToken: {}", requestToken.getToken());

        String authUrl = service.getAuthorizationUrl(requestToken);
        log.trace("getAuthorizationUrl: {}", authUrl);


        //
        // either popup (panel == null) or show in panel
        //
        SimpleSwingBrowser browser = new SimpleSwingBrowser();
        if(panel != null) {
            panel.add(browser.getPanel());
            browser.loadURL(authUrl);
        } else {
            browser.openAndShow(authUrl);
        }

        log.trace("started browser with the gotten url: {}", authUrl);



        SwingWorker<String, String> authAnswer = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                log.trace("start polling for 'oauth_verifier=' in redirected url");
                String url;
                int loops = 0;
                boolean closedBrowser = false;
                List<String> verifierStr;
                do {
                    loops++;
                    synchronized (this) {
                        //System.out.println("waiting");
                        wait(500);
                    }
                    url = browser.getShownURL();
                    log.trace("redirected url is: {}", url);
                    verifierStr = RegexUtils.findWithRegex(url, "(oauth_verifier=)([0-9A-Z]*)", 2);
                    if(verifierStr.size() == 1) {
                        log.trace("found ONE verifier. Cool! {}", verifierStr);
                        break;
                    }
                    /*
                    if(!browser.isVisible()) {
                        log.trace("closed browser... did not get credentials", verifierStr);
                        closedBrowser = true;
                        break;
                    }*/
                } while (loops < 120 * 5); // 5 min

                browser.setVisible(false);

                if(closedBrowser) {
                    log.warn("user closed browser");
                    return log.traceExit("token: {}", null);
                } else {
                    if (verifierStr.size() == 0) {
                        log.warn("did not find 'oauth_verifier=' in evernote url {}", url);
                    }

                    if (verifierStr.size() > 1) {
                        log.fatal("did find more than one 'oauth_verifier=' in evernote url {}", url);
                        JOptionPane.showMessageDialog(browser, "authError", "Login Error", JOptionPane.ERROR_MESSAGE);
                        return log.traceExit("token: {}", null);
                    }
                }

                final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, verifierStr.get(0));
                authToken = accessToken.getToken();

                if(p.get(EVERNOTE_TOKEN) != null) {
                    log.info("removing old auth token from properties: " + p.get(EVERNOTE_TOKEN) );
                }
                p.put(EVERNOTE_TOKEN, authToken);

                return authToken;
            }

            @Override
            protected void done() {
                super.done();
            }
        };

        authAnswer.execute();


        return log.traceExit("token from and for evernote is: {}", authAnswer);
    }

    /**
     *
     */
    public ENConnection connectToEvernote() {
        if(MainGUI.debugOffline) {
            log.warn("preventing ENAuth.connectToEvernote() by cmd args -offline");
            return null;
        }
        try {
            if (authToken != null) {
                ENConnection enConnection = ENConnection.from(authToken);
                if(enConnection.connect()) {
                    log.info("token available and connected to evernote: " + authToken);
                    //log.debug("connected!");
                    return enConnection;
                } else {
                    log.info("connection failed!");
                    return null;
                }
            } else {
                log.info("token not available try to get one by evernote website...");
                SwingWorker<String, String> tokenGetter = authOverEvernoteWebsite(null);
                while (!tokenGetter.isDone()) {
                    Thread.sleep(300);
                }
                authToken = tokenGetter.get();
                if(authToken != null) {
                    log.debug("got a new token: {}", authToken);
                    ENConnection enConnection = ENConnection.from(authToken);
                    if(enConnection.connect()) {
                        log.info("CONNECTED with new token. Works! :-)");
                        return enConnection;
                    } else {
                        log.info("connection FAILED with new token");
                        return null;
                    }
                } else {
                    log.info("Website did not deliver token...");
                    return null;
                }
            }
        } catch (InterruptedException e) {
            log.catching(e);
        } catch (ExecutionException e) {
            log.catching(e);
        } catch (IOException e) {
            log.warn("Probably no network to connect to evernote...");
        }
        return null;
    }




    public class EvernoteOauth10aAPI extends DefaultApi10a {

        @Getter private EvernoteService type;

        public EvernoteOauth10aAPI(EvernoteService type) {
            this.type = type;
        }

        @Override
        public String getRequestTokenEndpoint() {
            return type.getRequestTokenEndpoint();
        }

        @Override
        public String getAccessTokenEndpoint() {
            return type.getAccessTokenEndpoint();
        }

        @Override
        public String getAuthorizationUrl(OAuth1RequestToken oAuth1RequestToken) {
            return type.getAuthorizationUrl(oAuth1RequestToken.getToken());
        }
    }

    public static void main(String[] args) {
        log.info("starting test of evernote auth");

        try {
            Properties p = new Properties();
            ENAuth a = new ENAuth(p, EvernoteService.PRODUCTION);
            a.authOverEvernoteWebsite(null);
            a.connectToEvernote();
        } catch (Exception e) {
            log.catching(e);
        }
    }



}
