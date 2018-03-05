package pn150121d.kdp.stockmarket.common;

public class RegistrationRequest implements NetworkMessage
{
    static final long serialVersionUID = 1239812093812545L;
    public final int port;
    public final String name;
    public final String password;

    public RegistrationRequest(int port, String name, String password)
    {
        this.port=port;
        this.name=name;
        this.password=password;
    }
    @Override
    public String getType()
    {
        return MessageTypes.REGISTER_CLIENT;
    }
}
