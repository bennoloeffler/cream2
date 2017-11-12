package bel.en.gui;


import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;

/**
 * @link http://stackoverflow.com/questions/2492540/webkit-browser-in-a-java-app/26028556#26028556
 */
@Log4j2
public class JavaFxWebBrowser extends JFXPanel {
    private WebView webView;
    private  Scene webViewScene;
    private WebEngine webEngine;

    private HTMLEditor htmlEditor;
    private  Scene editorScene;
    //private String htmlContent;

    public JavaFxWebBrowser() {
        Platform.runLater(() -> {
            initialiseJavaFXScene();
        });
    }

    public void loadContent(String html)
    {
        //log.trace(CRASH_NIT, "starting web browser in fx thread... ");

        webEngine.loadContent(html);
        //log.trace(CRASH_NIT, "started... going to load html");
        htmlEditor.setHtmlText(html);
        //log.trace(CRASH_NIT, "loaded");
        //htmlContent = html;
    }

    private void initialiseJavaFXScene() {
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.load("http://v-und-s.de");
        //webEngine.loadContent();
        //System.out.println(webEngine.getLocation());
        webViewScene = new Scene(webView);
        setScene(webViewScene);

        htmlEditor = new HTMLEditor();
        editorScene = new Scene(htmlEditor);
    }

    public void setEditorEnabled(boolean enable){
        if(enable) {
            Platform.runLater(() -> {
                setScene(editorScene);
            });
        } else {
            webEngine.loadContent(htmlEditor.getHtmlText());
            setScene(webViewScene);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JavaFxWebBrowser javaFxWebBrowser = new JavaFxWebBrowser();
            JFrame frame = new JFrame("web browser");
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            Container contentPane = frame.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(javaFxWebBrowser, BorderLayout.CENTER);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            Platform.runLater(() -> javaFxWebBrowser.initialiseJavaFXScene());
        });
    }
}
