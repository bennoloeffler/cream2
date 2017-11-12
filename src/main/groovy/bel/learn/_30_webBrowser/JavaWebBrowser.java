package bel.learn._30_webBrowser;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;



public class JavaWebBrowser extends JFXPanel {
    private WebView webView;
    private WebEngine webEngine;

    public JavaWebBrowser() {
        Platform.runLater(() -> {
            initialiseJavaFXScene();
        });
    }

    public void loadContent(String html) {
        Platform.runLater(() -> webEngine.loadContent(html));
    }

    private void initialiseJavaFXScene() {
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.load("http://v-und-s.de");
        //webEngine.loadContent();
        //System.out.println(webEngine.getLocation());
        Scene scene = new Scene(webView);
        setScene(scene);
    }

    public void startInJFrame() {
        SwingUtilities.invokeLater(() -> {

            //JavaWebBrowser javaFxWebBrowser = new JavaWebBrowser();
            JFrame frame = new JFrame("web browser");
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            Container contentPane = frame.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(this, BorderLayout.CENTER);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            //Platform.runLater(() -> javaFxWebBrowser.initialiseJavaFXScene());
        });
    }

    public static void main(String[] args) {
        new JavaWebBrowser().startInJFrame();
    }
}