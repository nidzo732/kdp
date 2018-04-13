package pn150121d.kdp.stockmarket.common;

import java.io.Serializable;

public abstract class NetworkMessage implements Serializable
{
    public abstract String getType();
    public Integer msgId;
}
