package pn150121d.kdp.stockmarket.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketWrapper
{
    public static int TIMEOUT = 2000;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean open = true;

    public SocketWrapper(String ip, int port) throws IOException
    {
        socket = new Socket();
        socket.setSoTimeout(TIMEOUT);
        socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public SocketWrapper(Socket socket) throws IOException
    {
        this.socket = socket;
        this.socket.setSoTimeout(TIMEOUT);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void write(String data)
    {
        writer.println(data);
    }

    public String read() throws IOException
    {
        String data = reader.readLine();
        if (data == null) throw new IOException();
        return data;
    }

    public void close()
    {
        if (open)
        {
            try
            {
                open = false;
                writer.close();
                reader.close();
                socket.close();
            }
            catch (IOException ignored)
            {

            }
        }
    }

    public String getIp()
    {
        String ip = socket.getRemoteSocketAddress().toString();
        ip = ip.substring(1);
        ip = ip.split(":")[0];
        return ip;
    }
}
