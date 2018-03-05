package pn150121d.kdp.stockmarket.master;

public class Client extends Correspondent
{
    final String name;
    public Client(String ip, int port, String name)
    {
        super(ip, port);
        this.name=name;
    }
}
