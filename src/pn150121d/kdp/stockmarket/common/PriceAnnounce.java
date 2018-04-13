package pn150121d.kdp.stockmarket.common;

import java.util.List;

/**
 * Poruka koju server periodično šalje klijentima sa informacijama
 * o novim cenama
 */
public class PriceAnnounce extends NetworkMessage
{
    public final List<Price> priceList;
    public PriceAnnounce(List<Price> priceList)
    {
        this.priceList = priceList;
    }
    @Override
    public String getType()
    {
        return MessageTypes.ANNOUNCE_PRICES;
    }
}
