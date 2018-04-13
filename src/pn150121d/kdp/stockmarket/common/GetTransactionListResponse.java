package pn150121d.kdp.stockmarket.common;

import java.util.List;

public class GetTransactionListResponse extends NetworkMessage
{
    public final List<Transaction> transactionList;

    public GetTransactionListResponse(List<Transaction> transactionList)
    {
        this.transactionList = transactionList;
    }

    @Override
    public String getType()
    {
        return MessageTypes.GET_TRANSACTION_LIST_RESPONSE;
    }
}
