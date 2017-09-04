package org.dominwos.exchangerates.observing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dominwos.exchangerates.service.DatabaseService;
import org.dominwos.exchangerates.view.ViewController;
import org.dominwos.exchangerates.model.ExchangeRate;
import org.dominwos.exchangerates.parsing.ParsingUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.dominwos.exchangerates.parsing.ParsingUtils.getDateFromString;

public class DirectoryObserver implements Runnable {
    public static final Logger log = LogManager.getLogger(DirectoryObserver.class);

    private final Path observedPath;
    private final DatabaseService databaseService;
    private final ViewController viewController;
    private final WatchKey observingKey;
    public DirectoryObserver(Path observedPath, DatabaseService service, ViewController viewController) {
        this.observedPath = observedPath;
        this.databaseService = service;
        this.viewController = viewController;
        this.observingKey = createWatchKey();
    }

    @Override
    public void run() {
        List<WatchEvent<?>> eventList = observingKey.pollEvents();
        if(!eventList.isEmpty()) {
            eventList.forEach(
                    event -> {
                        Path filePath = (Path)event.context();
                        File createdFile = observedPath.resolve(filePath).toFile();
                        List<ExchangeRate> exchangeRates = new ArrayList<>();
                        try {
                            exchangeRates = readDataFromFile(createdFile);
                        } catch (IOException |  ParseException ex) {
                            log.warn("Failed to read data from file {}", createdFile, ex);
                        }
                        databaseService.storeAllRates(exchangeRates);
                        viewController.updateStage();
                    }
            );
        }

    }

    private List<ExchangeRate> readDataFromFile(File newlyCreatedFile) throws IOException, ParseException {
        List<ExchangeRate> exchangeRatesList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newlyCreatedFile), Charset.forName("ISO-8859-2")));
            String codesLine = reader.readLine();
            List<String> currencyCodes = Arrays.stream(codesLine.split(";"))
                    .filter(code -> code.matches("[0-9]{1,3}[A-Za-z]{3}"))
                    .collect(Collectors.toList());
            String[] currencyNames = reader.readLine().split(";");
            List<String> currencyNamesList = Arrays.stream(currencyNames).filter(name -> !name.isEmpty())
                    .collect(Collectors.toList());
            currencyNamesList.forEach(currencyName -> log.error(currencyName));
            String line = reader.readLine();
            while (line != null && line.contains("NBP")) {
                String[] exchangeRatesFromFile = line.split(";");
                String dateString = exchangeRatesFromFile[0];
                LocalDate ratingDate = getDateFromString(dateString);
                for (int i = 0; i < currencyCodes.size(); i++) {
                    ExchangeRate rate = new ExchangeRate(
                            currencyNamesList.get(i),
                            currencyCodes.get(i), ratingDate,
                            ParsingUtils.parseDoubleWithComma(exchangeRatesFromFile[i + 1]));

                    exchangeRatesList.add(rate);
                }
                line = reader.readLine();
            }
            reader.close();

        return exchangeRatesList;
    }
    private WatchKey createWatchKey() {
        WatchKey directoryObservingKey;

        try {
            WatchService directoryWatchService = FileSystems.getDefault().newWatchService();
            directoryObservingKey = observedPath.register(directoryWatchService, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("Created Watch Key for path: {}", () -> observedPath);
        } catch (IOException exception) {
            log.error("Error while creating Watch Key", exception);
            throw new RuntimeException("Directory observing failed");
        }
        return directoryObservingKey;
    }
}
