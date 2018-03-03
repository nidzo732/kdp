package pn150121d.kdp.stockmarket.client;
import pn150121d.kdp.stockmarket.common.SocketWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader i = new BufferedReader(new InputStreamReader(System.in));
        String msg=i.readLine();
        SocketWrapper sock=new SocketWrapper("localhost", 6666);
        sock.write(msg);
        System.out.println(sock.read());
        sock.close();
    }
}
