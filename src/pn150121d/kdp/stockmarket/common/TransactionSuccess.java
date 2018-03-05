package pn150121d.kdp.stockmarket.common;

import java.io.Serializable;

public class TransactionSuccess implements NetworkMessage
{
    static final long serialVersionUID = 20492304924234L;
    public final String target;
    public final TransactionType type;
    public final int item;
    public int count;
    public TransactionSuccess(String target, TransactionType type, int item, int count)
    {
        this.target=target;
        this.type=type;
        this.item=item;
        this.count=count;
    }

    @Override
    public String getType()
    {
        return MessageTypes.TRANSACTION_SUCCESS;
    }
}
