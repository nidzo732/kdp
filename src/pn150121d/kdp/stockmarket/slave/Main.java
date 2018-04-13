package pn150121d.kdp.stockmarket.slave;

import pn150121d.kdp.stockmarket.common.*;

import javax.swing.*;
import java.io.IOException;

/**
 * Ulazna klasa za podserver. Otvara serverski socket i pokušava da se
 * poveže na glavni server. Eventualno startuje GUI
 */
public class Main
{
    public static void main(String[] args) throws InterruptedException, IOException
    {
        boolean useGui = false;
        int port = Ports.SLAVE_LISTEN_PORT;
        String masterIp = "localhost";
        int masterPort = Ports.MASTER_LISTEN_PORT;
        for (String arg : args)
        {
            if (arg.startsWith("port="))
            {
                port = Integer.parseInt(arg.substring(5));
            }
            else if (arg.startsWith("masterip="))
            {
                masterIp = arg.substring(9);
            }
            else if (arg.startsWith("masterport="))
            {
                masterPort = Integer.parseInt(arg.substring(11));
            }
            else if (arg.equals("ui"))
            {
                useGui = true;
            }
        }
        SocketWrapper serverConnection = null;
        try
        {
            serverConnection = new SocketWrapper(masterIp, masterPort);
            serverConnection.write(Base64.objectTo64(new SlaveRegistrationRequest(port)));
            String response = serverConnection.read();
            if (!response.equals("OK"))
            {
                System.out.println("Master refused connection");
                System.out.println(response);
                return;
            }
        }
        catch (IOException err)
        {
            System.out.println("Failed to start");
            System.out.println(err.getMessage());
            return;
        }
        finally
        {
            if (serverConnection != null) serverConnection.close();
        }
        Server server = new Server(port, new RequestHandler());
        Thread serverThread = new Thread(server);
        if (useGui)
        {
            SwingUtilities.invokeLater(() -> {
                UI ui = new UI(server);
                ui.setupUI();
            });
        }
        else
        {
            server.setLogger(message -> System.out.println("SLAVE LOG: " + message));
        }
        serverThread.start();
        serverThread.join();
    }
}
