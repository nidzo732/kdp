package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.Logger;
import pn150121d.kdp.stockmarket.common.ScrollableTextList;
import pn150121d.kdp.stockmarket.common.Server;
import pn150121d.kdp.stockmarket.common.UpdateListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class UI extends JPanel implements Logger, UpdateListener
{
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 400;
    private final Server server;
    private JFrame frame;
    private ScrollableTextList clients;
    private ScrollableTextList slaves;
    private ScrollableTextList items;
    private ScrollableTextList log;
    private CollectorThread collectorThread;
    private AnnouncerThread announcerThread;

    UI(Server server, CollectorThread collectorThread, AnnouncerThread announcerThread)
    {
        this.server = server;
        this.collectorThread=collectorThread;
        this.announcerThread=announcerThread;
    }

    void setupUI()
    {
        frame = new JFrame("Server");
        frame.getContentPane().add(this);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                server.kill();
                collectorThread.halt();
                frame.dispose();
            }
        });
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setVisible(true);

        setLayout(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(2, 4, 10, 0));
        top.add(new JLabel("Klijenti"));
        top.add(new JLabel("Podserveri"));
        top.add(new JLabel("Hartije"));
        top.add(new JLabel("Log"));
        top.add(new JLabel("naziv:ip:port"));
        top.add(new JLabel("ip:port"));
        top.add(new JLabel("hartija:nadlezni podserver:cena"));
        top.add(new JLabel(""));
        this.add(top, BorderLayout.NORTH);

        log = new ScrollableTextList();
        clients = new ScrollableTextList();
        slaves = new ScrollableTextList();
        items = new ScrollableTextList();
        JPanel mid = new JPanel(new GridLayout(1, 4, 10, 0));
        mid.add(clients);
        mid.add(slaves);
        mid.add(items);
        mid.add(log);
        this.add(mid, BorderLayout.CENTER);

        server.setLogger(this);
        server.setUpdateListener(this);
        collectorThread.setLogger(this);
        collectorThread.setUpdateListener(this);
        announcerThread.setLogger(this);
        announcerThread.setUpdateListener(this);
        frame.setResizable(false);
        dataUpdated();
    }

    @Override
    public void logMessage(String message)
    {
        log.append(message);
    }

    @Override
    public synchronized void dataUpdated()
    {
        Router.getReadLock();
        slaves.clear();
        for (Slave slave : Router.slaves.values())
        {
            slaves.append(slave.ip + ":" + slave.port);
        }
        clients.clear();
        for (Client client : Router.clients.values())
        {
            clients.append(client.name + ":" + client.ip + ":" + client.port);
        }
        items.clear();
        for (String item : Router.slaveTransMap.keySet())
        {
            items.append(item + ":" + Router.slaveTransMap.get(item)+":"+collectorThread.getPrice(item));
        }
        Router.releaseReadLock();
    }
}
