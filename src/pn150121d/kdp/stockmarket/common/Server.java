package pn150121d.kdp.stockmarket.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Klasa koja oslškuje na zadatom portu, prima poruke
 * i prosleđuje ih zadatom RequestHandler-u na obradu
 */
public class Server implements Runnable
{
    private static final int MAX_EXECUTOR_THREADS = 200;
    private ServerSocket listener;
    private Logger logger = null;
    private UpdateListener updateListener = null;
    private RequestHandler handler;
    private boolean terminated = false;
    private final ExecutorService executor;

    /**
     *
     * @param listenPort port na kojem se očekuju konekciju
     * @param handler klasa koja obrađuje poruke
     * @throws IOException u slučaju neuspešnog otvaranja serverskog socketa
     */
    public Server(int listenPort, RequestHandler handler) throws IOException
    {
        this.handler = handler;
        listener = new ServerSocket(listenPort);
        this.executor= Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);
    }

    /**
     * Ponovo obrađuje zahteve iz backlogova - koristi se na glavnom serveru
     * za backlogovane zahteve sa mrtvih podservera
     * @param requests zahtevi
     */
    public void handleDelayedRequests(List<NetworkMessage> requests)
    {
        handler.handleDelayedRequests(requests, this);
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
            if (!terminated)
            {
                System.out.println("IOException in slave");
                System.out.println(err.getMessage());
                err.printStackTrace();
            }
        }
        finally
        {
            if (listener != null && !listener.isClosed())
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
        if (updateListener != null) updateListener.dataUpdated();
    }

    public void log(String message)
    {
        if (logger != null) logger.logMessage(message);
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
        terminated = true;
        try
        {
            listener.close();
            executor.shutdown();
        }
        catch (IOException ignored)
        {
        }
    }
    public void execute(Runnable target)
    {
        executor.execute(target);
    }
}
