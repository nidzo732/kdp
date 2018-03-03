package pn150121d.kdp.stockmarket.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketWrapper
{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

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
        writer.close();
        reader.close();
        socket.close();
    }
}
