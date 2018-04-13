package pn150121d.kdp.stockmarket.common;

/**
 * Poruka koja se šalje pri "pingovanju"
 */
public class Echo extends NetworkMessage
{
    static final long serialVersionUID = 423948230984234L;

    @Override
    public String getType()
    {
        return null;
    }
}
