package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.NetworkMessage;

class Client extends Correspondent
{
    final String name;

    Client(String ip, int port, String name)
    {
        super(ip, port);
        this.name = name;
    }

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
