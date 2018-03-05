package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.*;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class UI extends JPanel implements Logger, UpdateListener, ActionListener
{
    private Server server;
    private final String SEPARATOR="\n________________________________________________\n";
    private JFrame frame;
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 400;
    private JTextArea transactions;
    private JTextArea prices;
    private JTextArea log;
    private JButton loginButton;
    private JTextField username;
    private JTextField password;
    private JTextField port;
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
                if(server!=null) server.kill();
                frame.dispose();
            }
        });
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setVisible(true);

        setLayout(new BorderLayout());
        topGrid=new GridLayout(3,1);
        top=new JPanel(topGrid);
        topRow1=new JPanel(new GridLayout(2,4, 10, 0));
        topRow1.add(new JLabel("Ime"));
        topRow1.add(new JLabel("Lozinka"));
        topRow1.add(new JLabel("Port"));
        topRow1.add(new JLabel(""));
        username=new JTextField();
        password=new JTextField();
        port=new JTextField(Integer.toString(Ports.CLIENT_LISTEN_PORT));
        loginButton=new JButton("Prijava");
        loginButton.addActionListener(this);
        topRow1.add(username);
        topRow1.add(password);
        topRow1.add(port);
        topRow1.add(loginButton);

        item=new JTextField();
        price=new JTextField();
        count=new JTextField();
        buy=new JButton("Kupi");
        sell=new JButton("Prodaj");
        revoke=new JButton("Opozovi");
        buy.addActionListener(this);
        sell.addActionListener(this);
        revoke.addActionListener(this);
        topRow2=new JPanel(new GridLayout(2, 4, 10, 0));
        JPanel transactionButtons=new JPanel(new FlowLayout());
        transactionButtons.add(buy);
        transactionButtons.add(sell);
        transactionButtons.add(revoke);
        topRow2.add(new JLabel("Hartija"));
        topRow2.add(new JLabel("Cena"));
        topRow2.add(new JLabel("Količina"));
        topRow2.add(new JLabel(""));
        topRow2.add(item);
        topRow2.add(price);
        topRow2.add(count);
        topRow2.add(transactionButtons);

        topRow3=new JPanel(new GridLayout(2, 3,10,0));
        topRow3.add(new JLabel("Transakcije"));
        topRow3.add(new JLabel("Cene"));
        topRow3.add(new JLabel("Log"));
        topRow3.add(new JLabel("Posijlalac:Hartija:Cena:Broj"));
        topRow3.add(new JLabel("Hartija:cena"));
        topRow3.add(new JLabel(""));
        top.add(topRow1);
        top.add(topRow2);
        top.add(topRow3);
        this.add(top, BorderLayout.NORTH);

        log=new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        transactions=new JTextArea();
        transactions.setEditable(false);
        prices=new JTextArea();
        prices.setEditable(false);
        JPanel mid=new JPanel(new GridLayout(1,3,10,0));
        mid.add(transactions);
        mid.add(prices);
        mid.add(log);
        this.add(mid, BorderLayout.CENTER);

        /*server.setLogger(this);
        server.setUpdateListener(this);
        dataUpdated();*/
        frame.setResizable(false);
        buy.setEnabled(false);
        sell.setEnabled(false);
        revoke.setEnabled(false);
    }

    @Override
    public void logMessage(String message)
    {
        log.append(message+SEPARATOR);
    }

    @Override
    public void dataUpdated()
    {
        /*TransactionStorage.getReadLock();
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
        TransactionStorage.releaseReadLock();*/
    }
    private void doLogin()
    {
        try
        {
            String username=null;
            String password=null;
            String portText=null;
            int port=0;
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
                if(port<=1024 || port>=65535)
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
            SocketWrapper sock = new SocketWrapper("localhost", Ports.MASTER_LISTEN_PORT);
            sock.write(Base64.objectTo64(new RegistrationRequest(port, username, password)));
            String response = sock.read();
            if(response.equals("OK"))
            {
                logMessage("Prijava uspesna");
                loginButton.setVisible(false);
                topRow1.setVisible(false);
                topGrid.setRows(2);
                top.remove(topRow1);
                buy.setEnabled(true);
                sell.setEnabled(true);
                revoke.setEnabled(true);
            }
            else
            {
                logMessage("Greska pri prijavi");
                logMessage(response);
            }
            sock.close();
        }
        catch (IOException err)
        {
            logMessage("Greska pri prijavi");
            logMessage(err.getMessage());
        }
    }
    private void doTrans(TransactionType type)
    {
        try
        {
            int item=0;
            int price=0;
            int count=0;
            try
            {
                item = Integer.parseInt(this.item.getText());
                price = Integer.parseInt(this.item.getText());
                count = Integer.parseInt(this.item.getText());
                if(price<=0 || count<=0)
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
            SocketWrapper sock = new SocketWrapper("localhost", Ports.MASTER_LISTEN_PORT);
            sock.write(Base64.objectTo64(new Transaction(username.getText(), type, item, price, count)));
            String response = sock.read();
            if(response.equals("OK") || response.equals("NO_SLAVES"))
            {
                logMessage(response);
            }
            else
            {
                TransactionSuccess success=Base64.objectFrom64(response);
                logMessage("OK: "+success.count);
            }
        }
        catch (IOException | ClassNotFoundException err)
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
        else if(e.getActionCommand().equals("Kupi"))
        {
            doTrans(TransactionType.PURCHASE);
        }
        else if(e.getActionCommand().equals("Prodaj"))
        {
            doTrans(TransactionType.SALE);
        }
    }
}
