package org.dominwos.exchangerates.model;

import javax.persistence.*;
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;

@Table(name = "exchange_rates")
@Entity
public class ExchangeRate implements Serializable {
    private String name;
    private String symbol;
    private LocalDate date;
    private double rate;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public ExchangeRate() {

    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", date=" + date +
                ", rate=" + rate +
                '}';
    }

    public LocalDate getDate() {
        return date;
    }

    public double getRate() {
        return rate;
    }

    public ExchangeRate(String name, String symbol, LocalDate date, double rate) throws ParseException {
        this.name = name;
        this.symbol = symbol;
        this.date = date;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }
}
