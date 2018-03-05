package pn150121d.kdp.stockmarket.common;

public interface RequestHandler
{
    void handleRequest(SocketWrapper request, Server server);
}
