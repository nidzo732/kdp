package pn150121d.kdp.stockmarket.client;
import pn150121d.kdp.stockmarket.common.*;

import javax.swing.*;
import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        SwingUtilities.invokeLater(() -> {
            UI ui=new UI();
            ui.setupUI();
        });

    }

    private static void testTransaction(String user, TransactionType type, int item, int count, int price, int listenPort) throws IOException
    {

    }
}
