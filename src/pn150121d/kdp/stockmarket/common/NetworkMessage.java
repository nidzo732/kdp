package pn150121d.kdp.stockmarket.common;

import java.io.Serializable;

/**
 * Klasa iz koje se izvode sve poruke poslate preko mreže
 */
public abstract class NetworkMessage implements Serializable
{
    /**
     * @return tip poruke - konstanta iz MessageTypes
     */
    public abstract String getType();
    public Integer msgId;
}
