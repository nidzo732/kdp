package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.Server;
import pn150121d.kdp.stockmarket.common.SocketWrapper;

import java.io.IOException;

public class RequestHandler implements pn150121d.kdp.stockmarket.common.RequestHandler
{
    @Override
    public void handleRequest(SocketWrapper request, Server server)
    {
        try
        {
            System.out.println(request.read());
            request.write("OK");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
