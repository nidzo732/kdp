package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.SocketWrapper;

import java.io.IOException;
import java.net.ServerSocket;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket srv=new ServerSocket(6666);
        while(true)
        {
            SocketWrapper wrp = new SocketWrapper(srv.accept());
            System.out.println("OOO: "+wrp.read());
            wrp.write("QQQ");
            wrp.close();
        }

    }
}
