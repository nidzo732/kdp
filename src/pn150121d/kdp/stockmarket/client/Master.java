package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.Base64;
import pn150121d.kdp.stockmarket.common.NetworkMessage;
import pn150121d.kdp.stockmarket.common.RegistrationRequest;
import pn150121d.kdp.stockmarket.common.SocketWrapper;

import java.io.IOException;

/**
 * Klasa koja predstavlja vezu sa glavnim serverom
 */
class Master
{
    final String username;
    public final int port;
    private final String ip;

    /**
     *
     * @param ip adresa glavnog servera
     * @param port port na kojem slusa glavni server
     * @param myPort port na kojem klijent sluša
     * @param username korisnično ime
     * @param password lozinka
     * @throws IOException u slučaju mrežne greške
     */
    Master(String ip, int port, int myPort, String username, String password) throws IOException
    {
        this.ip = ip;
        this.port = port;
        this.username = username;
        SocketWrapper sock = null;
        try
        {
            sock = new SocketWrapper(ip, port);
            sock.write(Base64.objectTo64(new RegistrationRequest(myPort, username, password)));
            String response = sock.read();
            if (!response.equals("OK")) throw new IOException("Master responded with "+response);
        }
        finally
        {
            if (sock != null) sock.close();
        }
    }

    /**
     * Šalje poruku glavnom serveru
     * @param message poruka
     * @return odgvor glavnog servera
     * @throws IOException u slučaju problema na mreži
     */
    String sendMessage(NetworkMessage message) throws IOException
    {
        SocketWrapper sock = null;
        try
        {
            sock = new SocketWrapper(ip, port);
            sock.write(Base64.objectTo64(message));
            return sock.read();
        }
        finally
        {
            if (sock != null) sock.close();
        }

    }
}
