package pn150121d.kdp.stockmarket.master;

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
    private JTextArea clients;
    private JTextArea slaves;
    private JTextArea items;
    private JTextArea log;
    UI(Server server)
    {
        this.server=server;
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
                frame.dispose();
            }
        });
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setVisible(true);

        setLayout(new BorderLayout());
        JPanel top=new JPanel(new GridLayout(2,4, 10, 0));
        top.add(new JLabel("Klijenti"));
        top.add(new JLabel("Podserveri"));
        top.add(new JLabel("Hartije"));
        top.add(new JLabel("Log"));
        top.add(new JLabel("naziv:ip:port"));
        top.add(new JLabel("ip:port"));
        top.add(new JLabel("hartija:nadlezni podserver"));
        top.add(new JLabel(""));
        this.add(top, BorderLayout.NORTH);

        log=new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        clients=new JTextArea();
        clients.setEditable(false);
        slaves=new JTextArea();
        slaves.setEditable(false);
        items=new JTextArea();
        items.setEditable(false);
        JPanel mid=new JPanel(new GridLayout(1,4, 10,0));
        mid.add(clients);
        mid.add(slaves);
        mid.add(items);
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
        Router.getReadLock();
        slaves.setText("");
        for(Slave slave:Router.slaves.values())
        {
            slaves.append(slave.ip+":"+slave.port+SEPARATOR);
        }
        clients.setText("");
        for(Client client:Router.clients.values())
        {
            clients.append(client.name+":"+client.ip+":"+client.port+SEPARATOR);
        }
        items.setText("");
        for(Integer item:Router.slaveTransMap.keySet())
        {
            items.append(item+":"+Router.slaveTransMap.get(item)+SEPARATOR);
        }
        Router.releaseReadLock();
    }
}
