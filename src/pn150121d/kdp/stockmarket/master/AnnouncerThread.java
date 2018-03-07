package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.*;
import java.util.*;

public class AnnouncerThread extends Thread
{
    private boolean running=true;
    private UpdateListener updateListener;
    private Logger logger;
    private final CollectorThread collector;

    public AnnouncerThread(CollectorThread collector)
    {
        this.collector = collector;
    }


    public void halt()
    {
        running=false;
        interrupt();
    }

    @Override
    public void run()
    {
        while (running)
        {
            try
            {

                List<Client> clientList=Router.getAllClients();
                List<Thread> announcers=new LinkedList<>();
                Map<String, Integer> prices = collector.getPrices();
                List<Price> priceList=new ArrayList<>(prices.size());
                for(String item:prices.keySet())
                {
                    priceList.add(new Price(item, prices.get(item)));
                }
                for (Client client : clientList)
                {
                    Thread announcer = new Thread(() -> announce(client, priceList));
                    announcer.start();
                    announcers.add(announcer);
                }
                for (Thread announcer:announcers)
                {
                    announcer.join();
                }
                log("Prices announced");
                Thread.sleep(TimeConstants.PRICE_ANNOUNCE_PERIOD*1000);
            }
            catch (InterruptedException ignored)
            {

            }
        }
    }

    private void announce(Client client, List<Price> prices)
    {
        String response = client.sendWithoutBacklog(new PriceAnnounce(prices));
        if(response!=null)
        {
            List<NetworkMessage> backlog = client.swapBacklogs();
            for(NetworkMessage message:backlog)
            {
                client.send(message);
            }
        }
    }

    public void setUpdateListener(UpdateListener updateListener)
    {
        this.updateListener = updateListener;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }
    private void log(String message)
    {
        if(logger!=null) logger.logMessage(message);
    }
}
