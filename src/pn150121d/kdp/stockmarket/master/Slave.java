package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.NetworkMessage;

class Slave extends Correspondent
{
    final int id;
    private int failureCount=0;

    Slave(String ip, int port, int id)
    {
        super(ip, port);
        this.id = id;
    }

    synchronized String send(NetworkMessage message, boolean supervised)
    {
        String result=super.send(message);
        if(supervised)
        {
            if (result == null)
            {
                backlog.remove(message);
                failureCount++;
            }
            else failureCount = 0;
        }
        return result;
    }

    int getFailureCount()
    {
        return failureCount;
    }
}
