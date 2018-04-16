package pn150121d.kdp.stockmarket.slave;

import pn150121d.kdp.stockmarket.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * GUI za podserver. Prikazuje status i logove
 */
class UI extends JPanel implements Logger, UpdateListener
{
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 400;
    private final Server server;
    private JFrame frame;
    private ScrollableTextList sales;
    private ScrollableTextList purchases;
    private ScrollableTextList prices;
    private ScrollableTextList log;

    UI(Server server)
    {
        this.server = server;
    }

    void setupUI()
    {
        frame = new JFrame("Podserver");
        frame.getContentPane().add(this);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                server.kill();
                frame.dispose();
            }
        });
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setVisible(true);

        setLayout(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(2, 4, 10, 0));
        top.add(new JLabel("Prodaje"));
        top.add(new JLabel("Kupovine"));
        top.add(new JLabel("Cene"));
        top.add(new JLabel("Log"));
        top.add(new JLabel("Posijlalac:Hartija:Cena:Broj"));
        top.add(new JLabel("Posijlalac:Hartija:Cena:Broj"));
        top.add(new JLabel("Hartija:cena"));
        top.add(new JLabel(""));
        this.add(top, BorderLayout.NORTH);

        log = new ScrollableTextList();
        sales = new ScrollableTextList();
        purchases = new ScrollableTextList();
        prices = new ScrollableTextList();
        JPanel mid = new JPanel(new GridLayout(1, 4, 10, 0));
        mid.add(sales);
        mid.add(purchases);
        mid.add(prices);
        mid.add(log);
        this.add(mid, BorderLayout.CENTER);

        server.setLogger(this);
        server.setUpdateListener(this);
        frame.setResizable(false);
        dataUpdated();
    }

    @Override
    public synchronized void logMessage(String message)
    {
        log.append(message);
    }

    @Override
    public synchronized void dataUpdated()
    {
        TransactionStorage.getReadLock();
        sales.clear();
        for (Transaction t : TransactionStorage.getAllTransactions(TransactionType.SALE))
        {
            sales.append(t.toShortString());
        }
        purchases.clear();
        for (Transaction t : TransactionStorage.getAllTransactions(TransactionType.PURCHASE))
        {
            purchases.append(t.toShortString());
        }
        prices.clear();
        for (Price price : TransactionStorage.getPrices())
        {
            prices.append(price.item + ":" + price.price);
        }
        TransactionStorage.releaseReadLock();
    }
}
