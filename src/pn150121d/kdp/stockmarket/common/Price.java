package pn150121d.kdp.stockmarket.common;

import java.io.Serializable;

public class Price implements Serializable
{
    static final long serialVersionUID = 34589340958340958L;
    public final int item;
    public final int price;
    public Price(int item, int price)
    {
        this.item=item;
        this.price=price;
    }
}
