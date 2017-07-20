package ru.isalnikov.appfxclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.log4j.Log4j2;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

@Log4j2
public class MainApp extends Application {

    private WebEngine engine;

    private String URL_BASE = "https://www.google.ru/";
    private String URL_LOGOUT = "https://www.google.ru/logout";

    @Override
    public void start(Stage stage) throws Exception {
        stage.setWidth(1920);
        stage.setHeight(1080);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.centerOnScreen();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.jpg")));

        try {

            HostnameVerifier hostnameVerifier = (String hostname, SSLSession session) -> {
                log.info(hostname);
                log.info(session);
                return true;
            };

            FileInputStream fis = new FileInputStream("/src/main/resources/cert/client_cert.p12");

            HttpsURLConnection.setDefaultSSLSocketFactory(getFactory(fis, "pwZb9e3sGaz0rCibYkwxGVWeDAR7WSj", "client_cert"));
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

        } catch (GeneralSecurityException | FileNotFoundException e) {
            log.error(e);
        }

        WebView view = new WebView();
        Scene scene = new Scene(view);

        KeyCombination ctrlQ = KeyCodeCombination.keyCombination("Ctrl+Q");
        KeyCombination ctrlF5 = KeyCodeCombination.keyCombination("Ctrl+F5");

        scene.setOnKeyPressed((KeyEvent event) -> {
            if (ctrlQ.match(event)) {
                engine.load(URL_LOGOUT);
                Platform.exit();
            }
            if (ctrlF5.match(event)) {
                engine.reload();
            }
        });

        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> {

            engine = view.getEngine();

            engine.setJavaScriptEnabled(true);

            engine.setOnAlert((WebEvent<String> wEvent) -> {
                log.info("Alert Event  -  Message:  " + wEvent.getData());
            });

            engine.setOnError((WebErrorEvent event) -> {
                log.error("EventHandler WebErrorEvent:" + event.getMessage());
            });

            Worker<Void> loadWorker = engine.getLoadWorker();
            loadWorker.stateProperty().addListener((property, oldState,
                    newState) -> {
                log.info(oldState + " -> " + newState);
                //log.info(engine.getLoadWorker().exceptionProperty());

                if (Worker.State.SUCCEEDED == newState) {

                    log.info(property.getValue());

                    EventListener listener = (Event evt) -> {
                        log.info(evt.getType());
                    };

                }

            });

            engine.load(URL_BASE);

        });

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static SSLSocketFactory getFactory(InputStream is, String pKeyPassword, String certAlias) throws Exception {

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        keyStore.load(is, pKeyPassword.toCharArray());
        is.close();
        keyManagerFactory.init(keyStore, pKeyPassword.toCharArray());

        //Replace the original KeyManagers with the AliasForcingKeyManager
        KeyManager[] kms = keyManagerFactory.getKeyManagers();
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kms, trustAllCerts, null);
        return context.getSocketFactory();
    }

}
