package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Grafički interfejs klijenta
 */
public class UI extends JPanel implements Logger, UpdateListener, ActionListener
{
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 400;
    private Server server;
    private Master master;
    private JFrame frame;
    private ScrollableTextList transactions;
    private ScrollableTextList globalTransactions;
    private ScrollableTextList prices;
    private ScrollableTextList log;
    private JButton loginButton;
    private JTextField username;
    private JTextField password;
    private JTextField port;
    private JTextField masterIp;
    private JTextField item;
    private JTextField price;
    private JTextField count;
    private JButton buy;
    private JButton sell;
    private JButton revoke;

    private JPanel top;
    private JPanel topRow1;
    private JPanel topRow2;
    private JPanel topRow3;

    private GridLayout topGrid;

    UI()
    {

    }

    void setupUI()
    {
        frame = new JFrame("Klijent");
        frame.getContentPane().add(this);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (server != null) server.kill();
                frame.dispose();
            }
        });
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setVisible(true);

        setLayout(new BorderLayout());
        topGrid = new GridLayout(3, 1);
        top = new JPanel(topGrid);
        topRow1 = new JPanel(new GridLayout(2, 5, 10, 0));
        topRow1.add(new JLabel("Ime"));
        topRow1.add(new JLabel("Lozinka"));
        topRow1.add(new JLabel("Lokalni port"));
        topRow1.add(new JLabel("server"));
        topRow1.add(new JLabel(""));
        username = new JTextField();
        password = new JTextField();
        port = new JTextField(Integer.toString(Ports.CLIENT_LISTEN_PORT));
        masterIp = new JTextField("localhost");
        loginButton = new JButton("Prijava");
        loginButton.addActionListener(this);
        topRow1.add(username);
        topRow1.add(password);
        topRow1.add(port);
        topRow1.add(masterIp);
        topRow1.add(loginButton);

        item = new JTextField();
        price = new JTextField();
        count = new JTextField();
        buy = new JButton("Kupi");
        sell = new JButton("Prodaj");
        revoke = new JButton("Opozovi");
        buy.addActionListener(this);
        sell.addActionListener(this);
        revoke.addActionListener(this);
        topRow2 = new JPanel(new GridLayout(2, 4, 1, 0));
        JPanel transactionButtons = new JPanel(new GridLayout(1, 3,1,0));
        transactionButtons.add(buy);
        transactionButtons.add(sell);
        transactionButtons.add(revoke);
        topRow2.add(new JLabel("Hartija/ID transakcije"));
        topRow2.add(new JLabel("Cena"));
        topRow2.add(new JLabel("Količina"));
        topRow2.add(new JLabel(""));
        topRow2.add(item);
        topRow2.add(price);
        topRow2.add(count);
        topRow2.add(transactionButtons);

        topRow3 = new JPanel(new GridLayout(2, 3, 10, 0));
        topRow3.add(new JLabel("Moje Transakcije"));
        topRow3.add(new JLabel("Transakcije"));
        topRow3.add(new JLabel("Cene"));
        topRow3.add(new JLabel("Log"));
        topRow3.add(new JLabel("ID:hartija:Tip:Cena:Broj"));
        JButton refreshGlobalTransactions=new JButton("Osvezi");
        refreshGlobalTransactions.addActionListener(this);
        topRow3.add(refreshGlobalTransactions);
        topRow3.add(new JLabel("Hartija:cena:rast"));
        topRow3.add(new JLabel(""));
        top.add(topRow1);
        top.add(topRow2);
        top.add(topRow3);
        this.add(top, BorderLayout.NORTH);

        log = new ScrollableTextList();
        transactions = new ScrollableTextList();
        globalTransactions=new ScrollableTextList();
        prices = new ScrollableTextList();
        JPanel mid = new JPanel(new GridLayout(1, 3, 10, 0));
        mid.add(transactions);
        mid.add(globalTransactions);
        mid.add(prices);
        mid.add(log);
        this.add(mid, BorderLayout.CENTER);

        dataUpdated();
        frame.setResizable(false);
        buy.setEnabled(false);
        sell.setEnabled(false);
        revoke.setEnabled(false);
    }

    @Override
    public synchronized void logMessage(String message)
    {
        SwingUtilities.invokeLater(()-> log.append(message));
    }

    @Override
    public synchronized void dataUpdated()
    {
        SwingUtilities.invokeLater(()->{
            TransactionsAndPrices.getReadLock();
            transactions.clear();
            for (Transaction t : TransactionsAndPrices.transactions.values())
            {
                transactions.append(t.id + ":"+t.item+":"+t.type + ":"+t.price+":"+ t.count);
            }
            prices.clear();
            for(Price price:TransactionsAndPrices.prices)
            {
                prices.append(price.item+":"+price.price+":"+price.growth*100+"%");
            }
            globalTransactions.clear();
            if(TransactionsAndPrices.globalTransactions!=null)
            {
                for (Transaction t : TransactionsAndPrices.globalTransactions)
                {
                    globalTransactions.append(t.id + ":"+t.item+":"+t.type + ":"+t.price+":"+ t.count);
                }
            }
            TransactionsAndPrices.releaseReadLock();
        });
    }

    private void doLogin()
    {
        try
        {
            String username;
            String password;
            String portText;
            int port;
            try
            {
                username = this.username.getText();
                password = this.password.getText();
                portText = this.port.getText();
                if (username.equals("") || password.equals("") || portText.equals(""))
                {
                    JOptionPane.showMessageDialog(null, "Unesite sve potrebne podatke");
                    return;
                }
                port = Integer.parseInt(portText);
                if (port <= 1024 || port >= 65535)
                {
                    JOptionPane.showMessageDialog(null, "Port mora biti broj u opsegu [1025, 65535)");
                    return;
                }
            }
            catch (Exception err)
            {
                JOptionPane.showMessageDialog(null, "Port mora biti broj u opsegu [1025, 65535)");
                return;
            }
            server = new Server(port, new RequestHandler());
            new Thread(server).start();
            int masterPort=Ports.MASTER_LISTEN_PORT;
            String masterIP = masterIp.getText();
            if(masterIP.contains(":"))
            {
                String ipText=masterIP.split(":")[0];
                String masterPortText=masterIP.split(":")[1];
                try
                {
                    masterPort=Integer.parseInt(masterPortText);
                    if(masterPort<1025 || masterPort>=65535) throw new Exception();
                    masterIP=ipText;
                }
                catch (Exception err)
                {
                    JOptionPane.showMessageDialog(null, "Port mora biti broj u opsegu [1025, 65535)");
                    return;
                }
            }
            master = new Master(masterIP, masterPort, port, username, password);
            logMessage("Prijava uspesna");
            loginButton.setVisible(false);
            topRow1.setVisible(false);
            topGrid.setRows(2);
            top.remove(topRow1);
            buy.setEnabled(true);
            sell.setEnabled(true);
            revoke.setEnabled(true);
            server.setLogger(this);
            server.setUpdateListener(this);
        }
        catch (IOException err)
        {
            logMessage("Greska pri prijavi");
            logMessage(err.getMessage());
            if (server != null) server.kill();
        }
    }

    private void doTrans(TransactionType type)
    {
        try
        {
            String item;
            int price;
            int count;
            try
            {
                item = this.item.getText();
                if(item.length()==0)
                {
                    JOptionPane.showMessageDialog(null, "Unesite naziv hartije");
                    return;
                }
                price = Integer.parseInt(this.price.getText());
                count = Integer.parseInt(this.count.getText());
                if (price <= 0 || count <= 0)
                {
                    JOptionPane.showMessageDialog(null, "Cena i broj moraju biti pozitivni brojevi");
                    return;
                }
            }
            catch (Exception err)
            {
                JOptionPane.showMessageDialog(null, "Unesite brojčane podatke");
                return;
            }
            try
            {
                TransactionsAndPrices.getReadLock();
                Transaction trans = new Transaction(master.username, type, item, price, count);
                String response = master.sendMessage(trans);
                if (response.equals("NO_SLAVES"))
                {
                    logMessage("Nema dostupnih podservera");
                    return;
                }
                else if(response.equals("REJECT"))
                {
                    logMessage("Zahtev odbijen");
                    return;
                }
                trans.id = response;
                TransactionsAndPrices.transactions.put(trans.id, trans);
                logMessage("Transakcija uspešno poslata, ID: " + trans.id);
                dataUpdated();
            }
            finally
            {
                TransactionsAndPrices.releaseReadLock();
            }
        }
        catch (IOException err)
        {
            logMessage(err.getMessage());
        }
    }

    private void revokeTransaction()
    {
        String transId = item.getText();
        try
        {
            TransactionsAndPrices.getReadLock();
            Transaction trans = null;
            if (TransactionsAndPrices.transactions.containsKey(transId))
            {
                trans = TransactionsAndPrices.transactions.get(transId);
            }
            TransactionsAndPrices.releaseReadLock();
            if (trans == null)
            {
                logMessage("Ne postoji izabrana transakcija");
                return;
            }
            master.sendMessage(new RevokeTransactionRequest(trans));
            logMessage("Zahtev za opoziv poslat");
        }
        catch (IOException err)
        {
            logMessage(err.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("Prijava"))
        {
            doLogin();
        }
        else if (e.getActionCommand().equals("Kupi"))
        {
            doTrans(TransactionType.PURCHASE);
        }
        else if (e.getActionCommand().equals("Prodaj"))
        {
            doTrans(TransactionType.SALE);
        }
        else if (e.getActionCommand().equals("Opozovi"))
        {
            revokeTransaction();
        }
        else if(e.getActionCommand().equals("Osvezi"))
        {
            try
            {
                master.sendMessage(new GetTransactionListRequest(master.username));
            }
            catch (IOException err)
            {
                logMessage(err.getMessage());
            }
        }
    }
}
