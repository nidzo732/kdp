package pn150121d.kdp.stockmarket.common;

import java.util.List;

/**
 * RequestHandler klase obrađuju poruke primljene preko mreže.
 * Svaki program (klijent, server i podserver) ima svoju implementaciju
 * RequestHandler-a
 */
public interface RequestHandler
{
    void handleRequest(SocketWrapper request, Server server);
    default void handleDelayedRequests(List<NetworkMessage> requests, Server server)
    {

    }
}
