package pn150121d.kdp.stockmarket.common;

public class TransactionSuccess implements NetworkMessage
{
    static final long serialVersionUID = 20492304924234L;
    public final String target;
    public final TransactionType type;
    public final int item;
    public final String transId;
    public int count;
    public TransactionSuccess(String target, TransactionType type, int item, int count, String transId)
    {
        this.target=target;
        this.type=type;
        this.item=item;
        this.count=count;
        this.transId=transId;
    }

    @Override
    public String getType()
    {
        return MessageTypes.TRANSACTION_SUCCESS;
    }
}
