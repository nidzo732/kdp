package pn150121d.kdp.stockmarket.common;

import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class SocketWrapper
{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean open=true;

    public SocketWrapper(String ip, int port) throws IOException
    {
        this(new Socket(ip, port));
    }

    public SocketWrapper(Socket socket) throws IOException
    {
        this.socket=socket;
        reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer=new PrintWriter(socket.getOutputStream(), true);
    }
    public void write(String data)
    {
        writer.println(data);
    }

    public String read() throws IOException
    {
        String data= reader.readLine();
        if(data==null) throw new IOException();
        return data;
    }
    public void close() throws IOException
    {
        if(open)
        {
            open=false;
            writer.close();
            reader.close();
            socket.close();
        }
    }
    public String getIp()
    {
        String ip = socket.getRemoteSocketAddress().toString();
        ip=ip.substring(1);
        ip=ip.split(":")[0];
        return ip;
    }
}
