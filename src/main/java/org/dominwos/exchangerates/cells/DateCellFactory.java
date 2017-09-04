package org.dominwos.exchangerates.cells;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;

import java.time.LocalDate;

public class DateCellFactory implements Callback<DatePicker, DateCell> {

        private final LocalDate minimalDate;
        private final LocalDate maximalDate;

    public DateCellFactory(LocalDate minimalDate, LocalDate maximalDate) {
        this.minimalDate = minimalDate;
        this.maximalDate = maximalDate;
    }

    @Override
        public DateCell call(DatePicker param) {
            return new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if(item.isBefore(minimalDate.minusDays(1)) || item.isAfter(maximalDate.plusDays(1))) {
                    setDisable(true);
                    setStyle("-fx-background-color:#FFC0CB;");
                }
            }
        };

    }
}
