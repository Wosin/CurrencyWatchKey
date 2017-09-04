package org.dominwos.exchangerates;

import javafx.application.Application;
import javafx.stage.Stage;
import org.dominwos.exchangerates.view.ViewController;
import org.dominwos.exchangerates.service.DatabaseService;
import org.dominwos.exchangerates.observing.DirectoryObserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.*;

public class App extends Application{
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 600;
    private final Path path = Paths.get("C:","currencies");
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Currency Exchange Rates via NBP");
        primaryStage.setMinHeight(HEIGHT);
        primaryStage.setMinWidth(WIDTH);
        primaryStage.setOnCloseRequest((event) -> System.exit(1));

        DatabaseService databaseService = new DatabaseService();
        ViewController viewController = new ViewController(primaryStage, databaseService);
        DirectoryObserver directoryObserver = new DirectoryObserver(path, databaseService, viewController);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(directoryObserver, 2000, 3000, TimeUnit.MILLISECONDS);

    }
}

