package org.dominwos.exchangerates.parsing;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ParsingUtils {

    public static double parseDoubleWithComma(String commaSeparatedFloat) throws ParseException {
        return Double.valueOf(commaSeparatedFloat.replace(",","."));
    }

    public static Date getDateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate getDateFromString(String dateString) {
        int year = Integer.valueOf(dateString.substring(0,4));
        int month = Integer.valueOf(dateString.substring(4,6));
        int day = Integer.valueOf(dateString.substring(6));

        return LocalDate.of(year, month, day);
    }
}
