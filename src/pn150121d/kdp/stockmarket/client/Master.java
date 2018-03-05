package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.*;

import java.io.IOException;

public class Master
{
    private final String ip;
    public final String username;
    public final int port;
    public final String password;
    public Master(String ip, int port, int myPort, String username, String password) throws IOException
    {
        this.ip=ip;
        this.port=port;
        this.username=username;
        this.password=password;
        SocketWrapper sock=null;
        try
        {
            sock = new SocketWrapper(ip, port);
            sock.write(Base64.objectTo64(new RegistrationRequest(myPort, username, password)));
            String response = sock.read();
            if(!response.equals("OK")) throw new IOException();
        }
        finally
        {
            if(sock!=null) sock.close();
        }
    }
    public String sendMessage(NetworkMessage message) throws IOException
    {
        SocketWrapper sock = null;
        try
        {
            sock = new SocketWrapper(ip, port);
            sock.write(Base64.objectTo64(message));
            return sock.read();
        }
        finally
        {
            if(sock!=null) sock.close();
        }

    }
}
