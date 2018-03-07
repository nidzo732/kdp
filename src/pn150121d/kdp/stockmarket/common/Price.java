package pn150121d.kdp.stockmarket.common;

import java.io.Serializable;

public class Price implements Serializable
{
    static final long serialVersionUID = 34589340958340958L;
    public final String item;
    public final Integer price;

    public Price(String item, Integer price)
    {
        this.item = item;
        this.price = price;
    }
}
