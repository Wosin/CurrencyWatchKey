package org.dominwos.exchangerates.model;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;


@Table(name="exchange_rates")
@Entity
public class  ViewRateEntry {
    String currencyName;
    String currencySymbol;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public ViewRateEntry(String currencySymbol, String currencyName) {

        this.currencySymbol = currencySymbol;
        this.currencyName = currencyName;

    }

    @Override
    public String toString() {
        String toString =  currencyName + "(" + currencySymbol + ")" ;
        return new String(toString.getBytes(), StandardCharsets.UTF_8);
    }
}
