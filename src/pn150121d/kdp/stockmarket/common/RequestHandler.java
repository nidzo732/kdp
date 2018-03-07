package pn150121d.kdp.stockmarket.common;

import java.util.List;

public interface RequestHandler
{
    void handleRequest(SocketWrapper request, Server server);
    default void handleDelayedRequests(List<NetworkMessage> requests, Server server)
    {

    }
}
