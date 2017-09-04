package org.dominwos.exchangerates.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.dominwos.exchangerates.cells.DateCellFactory;
import org.dominwos.exchangerates.model.ExchangeRate;
import org.dominwos.exchangerates.model.ViewRateEntry;
import org.dominwos.exchangerates.parsing.ParsingUtils;
import org.dominwos.exchangerates.service.DatabaseService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class ViewController {
    private static final int LISTVIEW_PREFERED_WIDTH = 400;
    private static final int LISTVIEW_MAX_WIDTH = 600;
    private final Stage primaryStage;
    private final DatabaseService databaseService;

    public ViewController(Stage primaryStage, DatabaseService databaseService) {
        this.primaryStage = primaryStage;
        this.databaseService = databaseService;
        updateStage();
    }


    public void updateStage(){

        List<ViewRateEntry> symbols = databaseService.getCodesList();
        if(symbols.isEmpty()) {
          showNoDataLabel();
        } else {
            LocalDate dateEarliest = databaseService.getEarliestDate();
            LocalDate dateLatest = databaseService.getLatestDate();
            Platform.runLater(() -> {
                    DateCellFactory factory = new DateCellFactory(dateEarliest, dateLatest);
                    DatePicker earliestRating = new DatePicker();
                    earliestRating.setValue(dateEarliest);
                    earliestRating.setDayCellFactory(factory);
                    DatePicker latestRating = new DatePicker();
                    latestRating.setValue(dateLatest);
                    latestRating.setDayCellFactory(factory);
                    ListView<ViewRateEntry> listView = new ListView<>();
                    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                    listView.setPrefWidth(LISTVIEW_PREFERED_WIDTH);
                    listView.setMaxWidth(LISTVIEW_MAX_WIDTH);
                    listView.getItems().addAll(symbols);
                    Button button = new Button("Show Charts");
                    button.setOnAction(event -> drawCharts(listView, earliestRating ,latestRating));
                    HBox box = new HBox(listView, earliestRating, latestRating, button);
                    Scene scene = new Scene(box, primaryStage.getMinWidth(), primaryStage.getMinHeight());
                    primaryStage.setScene(scene);
                    primaryStage.show();

            });
        }
    }

    private void showNoDataLabel(){
        Platform.runLater( () -> {
        Label label = new Label("There is no data in database!");
        Scene noDataScene = new Scene(label);
        primaryStage.setScene(noDataScene);
        primaryStage.show();
        });
    }

    private void drawCharts(ListView listView, DatePicker firstRatingToShow, DatePicker lastRatingToShow) {
        TimeSeriesCollection collection = new TimeSeriesCollection();
        List<ViewRateEntry> selectedSymbols = listView.getSelectionModel().getSelectedItems();

        selectedSymbols.forEach(viewRateEntry -> {
            List<ExchangeRate> exchangeRates = databaseService.getExchangeRatesWithSymbol(
                    viewRateEntry.getCurrencySymbol(), firstRatingToShow.getValue(), lastRatingToShow.getValue()
            );
            collection.addSeries(createTimeSeries(exchangeRates, viewRateEntry.getCurrencySymbol()));
        });

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Exchange Rates according to NBP",
                "Date",
                "Exchange Rate",
                collection,
                true,
                true,
                false
        );
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.getRenderer().setBaseSeriesVisible(true);
        JFrame frame = new JFrame("Exchange Rates");
        frame.setSize(800, 600);
        frame.setContentPane(new ChartPanel(chart));
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private TimeSeries createTimeSeries(List<ExchangeRate> exchangeRates, String symbol){
        TimeSeries chartTimeSeries = new TimeSeries(symbol);
            exchangeRates.forEach(rate -> {
            Date date = ParsingUtils.getDateFromLocalDate(rate.getDate());
            chartTimeSeries.add(new Day(date), rate.getRate());
        });
        return chartTimeSeries;
    }

}
