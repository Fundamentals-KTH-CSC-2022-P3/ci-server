package fundamentals.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContinuousIntegrationServerTest {

    @Test
    void failingTest() {
        assertTrue(false);
    }

    @Test
    void getPortNumberFromInputOrElseDefaultTestWithValidInput() {
        var portNumber = 123456;
        var arrayWithPort = new String[]{String.valueOf(portNumber)};
        var parsedPortNumber = ContinuousIntegrationServer.getPortNumberFromInputOrElseDefault(arrayWithPort);
        assertEquals(portNumber, parsedPortNumber);
    }

    @Test
    void getPortNumberFromInputOrElseDefaultTestWithInvalidInput() {
        var portNumber = ContinuousIntegrationServer.DEFAULT_PORT_NUMBER;
        var arrayWithPort = new String[]{"Hello", "World"};
        var returnedDefault = ContinuousIntegrationServer.getPortNumberFromInputOrElseDefault(arrayWithPort);
        assertEquals(portNumber, returnedDefault);
    }

    @Test
    void getPortNumberFromInputOrElseDefaultTestWithNoInput() {
        var portNumber = ContinuousIntegrationServer.DEFAULT_PORT_NUMBER;
        var arrayWithPort = new String[0];
        var returnedDefault = ContinuousIntegrationServer.getPortNumberFromInputOrElseDefault(arrayWithPort);
        assertEquals(portNumber, returnedDefault);
    }
}