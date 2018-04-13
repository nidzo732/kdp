package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.SocketWrapper;

import javax.swing.*;
import java.io.IOException;

/**
 *  Main klasa za klijentski program, startuje grafički interfejs
 */
public class Main
{
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        SocketWrapper.TIMEOUT=4000;
        SwingUtilities.invokeLater(() -> {
            UI ui = new UI();
            ui.setupUI();
        });

    }
}
