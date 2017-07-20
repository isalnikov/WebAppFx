package ru.isalnikov.appfxclient;

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

    private String URL_BASE   = "https://www.google.ru/";
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

}
