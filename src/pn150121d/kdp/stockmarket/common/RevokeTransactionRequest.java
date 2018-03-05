package pn150121d.kdp.stockmarket.common;

public class RevokeTransactionRequest implements NetworkMessage
{
    static final long serialVersionUID = 142304920394823094L;
    public final Transaction trans;
    public RevokeTransactionRequest(Transaction trans)
    {
        this.trans=trans;
    }
    @Override
    public String getType()
    {
        return MessageTypes.REVOKE_TRANSACTION;
    }
}
