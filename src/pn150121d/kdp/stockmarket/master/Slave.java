package pn150121d.kdp.stockmarket.master;

public class Slave extends Correspondent
{
    public final int id;
    public Slave(String ip, int port, int id)
    {
        super(ip, port);
        this.id=id;
    }
}
