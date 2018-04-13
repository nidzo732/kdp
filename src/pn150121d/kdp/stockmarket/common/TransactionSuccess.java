package pn150121d.kdp.stockmarket.common;

/**
 * Poruka koju vraća podserver kad uspe da upari neke dve ponude
 */
public class TransactionSuccess extends NetworkMessage
{
    static final long serialVersionUID = 20492304924234L;
    public final String target;
    public final TransactionType type;
    public final String item;
    public final String transId;
    public int count;

    public TransactionSuccess(String target, TransactionType type, String item, int count, String transId)
    {
        this.target = target;
        this.type = type;
        this.item = item;
        this.count = count;
        this.transId = transId;
    }

    @Override
    public String getType()
    {
        return MessageTypes.TRANSACTION_SUCCESS;
    }
}
