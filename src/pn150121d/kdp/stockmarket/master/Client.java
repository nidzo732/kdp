package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.NetworkMessage;

public class Client extends Correspondent
{
    final String name;

    public Client(String ip, int port, String name)
    {
        super(ip, port);
        this.name = name;
    }

    public synchronized String sendWithoutBacklog(NetworkMessage message)
    {
        String response=send(message);
        if(response==null)
        {
            backlog.remove(message);
        }
        return response;
    }
}
