package pn150121d.kdp.stockmarket.master;

class Slave extends Correspondent
{
    final int id;
    Slave(String ip, int port, int id)
    {
        super(ip, port);
        this.id=id;
    }
}
