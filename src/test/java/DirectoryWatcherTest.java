import org.dominwos.exchangerates.model.ExchangeRate;
import org.dominwos.exchangerates.observing.DirectoryObserver;
import org.dominwos.exchangerates.service.DatabaseService;
import org.dominwos.exchangerates.view.ViewController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DirectoryWatcherTest {
    private DatabaseService mockedDatabaseService;
    private ViewController mockedViewController;
    private Path observedTestPath;
    private Path testCurrenciesFilePath;
    private Path pathToCopy;
    List<ExchangeRate> testRates = new ArrayList<>();
    ArgumentCaptor<List> exchangeRatesCaptor;

    @Before
    public void before() throws URISyntaxException, ParseException {
        observedTestPath = Paths.get(this.getClass().getClassLoader().getResource("").toURI());
        testCurrenciesFilePath = Paths.get(this.getClass().getResource("/test_currency.txt").toURI());
        mockedDatabaseService = Mockito.mock(DatabaseService.class);
        mockedViewController = Mockito.mock(ViewController.class);
        exchangeRatesCaptor = ArgumentCaptor.forClass(List.class);
        ExchangeRate testRate1 = new ExchangeRate("test currency","1TST", LocalDate.of(2017,1,1),1.0);
        ExchangeRate testRate2 = new ExchangeRate("test currency","1TST", LocalDate.of(2017,1,2),2.0);
        ExchangeRate testRate3 = new ExchangeRate("test currency","1TST", LocalDate.of(2017,1,3),3.0);
        testRates.add(testRate1);
        testRates.add(testRate2);
        testRates.add(testRate3);

    }

    @Test
    public void testReadsProper() throws IOException {
        DirectoryObserver directoryObserver = new DirectoryObserver(observedTestPath, mockedDatabaseService, mockedViewController);
        pathToCopy = Paths.get(observedTestPath.toString(),"/test_currency_copy.txt");
        Files.copy(testCurrenciesFilePath, pathToCopy);
        directoryObserver.run();
        verify(mockedDatabaseService).storeAllRates(exchangeRatesCaptor.capture());
        List<ExchangeRate> capturedValues = exchangeRatesCaptor.getValue();
        Assert.assertFalse(capturedValues.isEmpty());
        Assert.assertEquals(capturedValues.size(), 3);
        for(int i = 0; i < capturedValues.size(); i++) {
            ExchangeRate capturedValue = capturedValues.get(i);
            ExchangeRate excpectedValue = testRates.get(i);
            Assert.assertEquals(capturedValue.getDate(), excpectedValue.getDate());
            Assert.assertEquals(capturedValue.getRate(), excpectedValue.getRate(),0);
            Assert.assertEquals(capturedValue.getName(), excpectedValue.getName());
            Assert.assertEquals(capturedValue.getSymbol(), excpectedValue.getSymbol());
        }
    }

    @After
    public void after() {
        pathToCopy.toFile().delete();

    }
}
