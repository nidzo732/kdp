package pn150121d.kdp.stockmarket.slave;

import pn150121d.kdp.stockmarket.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class UI extends JPanel implements Logger, UpdateListener
{
    private final Server server;
    private final String SEPARATOR="\n________________________________________________\n";
    private JFrame frame;
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 400;
    private JTextArea sales;
    private JTextArea purchases;
    private JTextArea prices;
    private JTextArea log;
    UI(Server server)
    {
        this.server=server;
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
        JPanel top=new JPanel(new GridLayout(2,4, 10, 0));
        top.add(new JLabel("Prodaje"));
        top.add(new JLabel("Kupovine"));
        top.add(new JLabel("Cene"));
        top.add(new JLabel("Log"));
        top.add(new JLabel("Posijlalac:Hartija:Cena:Broj"));
        top.add(new JLabel("Posijlalac:Hartija:Cena:Broj"));
        top.add(new JLabel("Hartija:cena"));
        top.add(new JLabel(""));
        this.add(top, BorderLayout.NORTH);

        log=new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        sales=new JTextArea();
        sales.setEditable(false);
        purchases=new JTextArea();
        purchases.setEditable(false);
        prices=new JTextArea();
        prices.setEditable(false);
        JPanel mid=new JPanel(new GridLayout(1,4, 10,0));
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
    public void logMessage(String message)
    {
        log.append(message+SEPARATOR);
    }

    @Override
    public void dataUpdated()
    {
        TransactionStorage.getReadLock();
        sales.setText("");
        for(Transaction t:TransactionStorage.getAllTransactions(TransactionType.SALE))
        {
            sales.append(t.toShortString()+SEPARATOR);
        }
        purchases.setText("");
        for(Transaction t:TransactionStorage.getAllTransactions(TransactionType.PURCHASE))
        {
            purchases.append(t.toShortString()+SEPARATOR);
        }
        prices.setText("");
        for(Price price:TransactionStorage.getPrices())
        {
            prices.append(price.item+":"+price.price+SEPARATOR);
        }
        TransactionStorage.releaseReadLock();
    }
}
