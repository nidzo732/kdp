package pn150121d.kdp.stockmarket.common;

public class GetTransactionListRequest extends NetworkMessage
{
    public final String sender;

    public GetTransactionListRequest(String sender)
    {
        this.sender = sender;
    }

    @Override
    public String getType()
    {
        return MessageTypes.GET_TRANSACTION_LIST;
    }
}
