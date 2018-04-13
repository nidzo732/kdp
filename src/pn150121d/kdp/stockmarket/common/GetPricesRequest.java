package pn150121d.kdp.stockmarket.common;

/**
 * Poruka koju glavni server šalje podserverima
 * kad kupi informacije o cenama
 */
public class GetPricesRequest extends NetworkMessage
{
    static final long serialVersionUID = 3450308509834095L;

    @Override
    public String getType()
    {
        return MessageTypes.GET_PRICES;
    }
}
