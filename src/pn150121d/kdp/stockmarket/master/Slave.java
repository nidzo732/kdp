package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.NetworkMessage;

/**
 * Klasa koja predstavlja jedan podserver.
 * Prosleđuje mrežne poruke i vodi baclkog
 */
class Slave extends Correspondent
{
    final String id;
    private int failureCount=0;

    Slave(String ip, int port, String id)
    {
        super(ip, port);
        this.id = id;
    }

    /**
     * Šalje poruku
     * @param message poruka
     * @param supervised da li da se ubaci u backlog i inkrementira brojač
     *                   neuspelih u slučaju mrežne greške
     * @return odgovor ili null ako dođe do greške u mreži
     */
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

    /**
     * Šalje poruku bez backlogovanja
     * @param message poruka
     * @return odgovor ili null ako dođe do mrežne greške
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

    int getFailureCount()
    {
        return failureCount;
    }
}
