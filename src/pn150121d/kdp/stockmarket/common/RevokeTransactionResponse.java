package pn150121d.kdp.stockmarket.common;

/**
 * Status opoziva ponude
 */
public class RevokeTransactionResponse extends NetworkMessage
{
    public final String target;
    public final String status;
    public final String transId;

    public RevokeTransactionResponse(String target, String status, String transId)
    {
        this.target = target;
        this.status = status;
        this.transId = transId;
    }

    @Override
    public String getType()
    {
        return MessageTypes.REVOKE_TRANSACTION_RESPONSE;
    }
}
