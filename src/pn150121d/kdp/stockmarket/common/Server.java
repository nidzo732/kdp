package pn150121d.kdp.stockmarket.common;

import java.io.IOException;
import java.net.ServerSocket;

public class Server implements Runnable
{
    private ServerSocket listener;
    private int listenPort;
    private Logger logger=null;
    private UpdateListener updateListener=null;
    private RequestHandler handler;
    private boolean terminated=false;

    public Server(int listenPort, RequestHandler handler) throws IOException
    {
        this.listenPort=listenPort;
        this.handler=handler;
        listener = new ServerSocket(listenPort);
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                SocketWrapper request = new SocketWrapper(listener.accept());
                handler.handleRequest(request, this);
            }
        }
        catch (IOException err)
        {
            if(!terminated)
            {
                System.out.println("IOException in slave");
                System.out.println(err.getMessage());
                err.printStackTrace();
            }
        }
        finally
        {
            if(listener!=null && !listener.isClosed())
            {
                try
                {
                    listener.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
    }

    public void notifyUpdate()
    {
        if(updateListener!=null) updateListener.dataUpdated();
    }
    public void log(String message)
    {
        if(logger!=null) logger.logMessage(message);
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    public void setUpdateListener(UpdateListener updateListener)
    {
        this.updateListener = updateListener;
    }
    public void kill()
    {
        terminated=true;
        try
        {
            listener.close();
        }
        catch (IOException ignored)
        {
        }
    }
}
