package org.dominwos.exchangerates.service;

import org.dominwos.exchangerates.model.ExchangeRate;
import org.dominwos.exchangerates.model.ViewRateEntry;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public class DatabaseService {
    private final SessionFactory sessionFactory;

    public DatabaseService() {
        final StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().configure().build();
        this.sessionFactory = new MetadataSources(serviceRegistry).buildMetadata().buildSessionFactory();
    }


    public List<ViewRateEntry> getCodesList() {
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("SELECT DISTINCT new ViewRateEntry(symbol,name) FROM ExchangeRate");
        return query.list();

    }

    public List<ExchangeRate> getExchangeRatesWithSymbol(String symbol, LocalDate ratingStart, LocalDate ratingEnd) {
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(
                "FROM ExchangeRate AS rate WHERE rate.symbol = :symbol AND rate.date BETWEEN :startDate AND :endDate");
            query.setParameter("symbol", symbol);
        query.setParameter("startDate", ratingStart.isBefore(ratingEnd) ? ratingStart : ratingEnd);
        query.setParameter("endDate", ratingEnd.isAfter(ratingStart) ? ratingEnd : ratingStart);

        return (List<ExchangeRate>) query.list();
    }

    public LocalDate getEarliestDate() {
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("Select min(date) FROM ExchangeRate");
        LocalDate earliest = (LocalDate) query.list().get(0);
        session.close();
        return earliest;
    }

    public LocalDate getLatestDate() {
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("Select max(date) FROM ExchangeRate");
        LocalDate latest = (LocalDate) query.list().get(0);
        session.close();
        return latest;
    }

    public void storeAllRates(Collection<ExchangeRate> rateCollection) {
        Session session = sessionFactory.withOptions().openSession();
        Transaction transaction = session.beginTransaction();
        rateCollection.forEach(session::save);
        transaction.commit();
        session.close();
        }
    }
