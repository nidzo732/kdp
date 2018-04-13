package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.NetworkMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Klasa koja predstavlja jednog povezanog klijenta
 */
class Client extends Correspondent
{
    final String name;
    private int messagesSent=0;

    Client(String ip, int port, String name)
    {
        super(ip, port);
        this.name = name;
    }

    /**
     * Šalje poruku, u slučaju mrežne greške stavlja poruku u backlog i vraća null
     * @param message poruka
     * @return odgovor
     */
    @Override
    synchronized String send(NetworkMessage message)
    {
        message.msgId=messagesSent++;
        String response=super.send(message);
        if(response!=null && backlog.size()>0)
        {
            List<NetworkMessage> oldBacklog=backlog;
            backlog=new LinkedList<>();
            int idx=0;
            for(NetworkMessage msg:oldBacklog)
            {
                if(super.send(msg)==null) break;
                idx++;
            }
            if(idx+1<oldBacklog.size())
            {
                backlog.addAll(oldBacklog.subList(idx+1, oldBacklog.size()));
            }
        }
        else if(backlog.size()>0)
        {
            List<NetworkMessage> oldBacklog=backlog;
            backlog=new LinkedList<>();
            int idx=0;
            for(NetworkMessage msg:oldBacklog)
            {
                if(super.send(msg)==null) break;
                idx++;
            }
            if(idx+1<oldBacklog.size())
            {
                backlog.addAll(oldBacklog.subList(idx+1, oldBacklog.size()));
            }
        }
        return response;
    }

    /**
     * Šalje poruku, ali bez ubacivanja u backlog
     * @param message poruka
     * @return ogovor, null u slučaju greške
     */
    synchronized String sendWithoutBacklog(NetworkMessage message)
    {
        String response=send(message);
        if(response==null)
        {
            backlog.remove(message);
        }
        return response;
    }
}
