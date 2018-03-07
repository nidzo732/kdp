package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.Ports;
import pn150121d.kdp.stockmarket.common.Server;

import javax.swing.*;
import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        boolean useGui = false;
        int port = Ports.MASTER_LISTEN_PORT;
        for (String arg : args)
        {
            if (arg.startsWith("port="))
            {
                port = Integer.parseInt(arg.substring(5));
            }
            else if (arg.equals("ui"))
            {
                useGui = true;
            }
        }
        try
        {
            Server server = new Server(port, new RequestHandler());
            Thread serverThread = new Thread(server);
            CollectorThread collectorThread=new CollectorThread(server);
            AnnouncerThread announcerThread=new AnnouncerThread(collectorThread);
            if (useGui)
            {
                SwingUtilities.invokeLater(() -> {
                    UI ui = new UI(server, collectorThread, announcerThread);
                    ui.setupUI();
                });
            }
            else
            {
                server.setLogger(message -> System.out.println("MASTER LOG: " + message));
            }
            serverThread.start();
            collectorThread.start();
            announcerThread.start();
            serverThread.join();
        }
        catch (IOException err)
        {
            System.out.println("Failed to start server");
            System.out.println(err.getMessage());
        }

    }
}
