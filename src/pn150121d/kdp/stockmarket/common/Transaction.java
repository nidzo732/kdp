package pn150121d.kdp.stockmarket.common;

/**
 * Poruka koja opisuje ponudu
 */
public class Transaction extends NetworkMessage
{
    static final long serialVersionUID = 9809284092841L;
    public final String sender;
    public final TransactionType type;
    public final String item;
    public final int price;
    public long timeStamp;
    public int count;
    public String id;

    /**
     *
     * @param sender klijent koji šalje ponudu
     * @param type kupovina ili prodaja
     * @param item hartija
     * @param price cena
     * @param count količina
     */
    public Transaction(String sender, TransactionType type, String item, int price, int count)
    {
        this.sender = sender;
        this.type = type;
        this.item = item;
        this.price = price;
        this.count = count;
    }

    @Override
    public String toString()
    {
        return "{sender='" + sender + "'" + ", type='" + type + "', item=" + item + ", price=" + price + ", count=" + count + "}";
    }

    public String toShortString()
    {
        return sender + ":" + item + ":" + price + ":" + count;
    }

    @Override
    public String getType()
    {
        return MessageTypes.PROCESS_TRANSACTION;
    }

    public boolean valid()
    {
        return price>0 && count>0;
    }
}
