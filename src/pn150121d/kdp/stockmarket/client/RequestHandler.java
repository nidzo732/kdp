package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.*;

import java.io.IOException;

public class RequestHandler implements pn150121d.kdp.stockmarket.common.RequestHandler
{
    @Override
    public void handleRequest(SocketWrapper request, Server server)
    {
        new Thread(new Handler(request, server)).start();
    }

    private class Handler implements Runnable
    {
        private final SocketWrapper request;
        private final Server server;

        Handler(SocketWrapper request, Server server)
        {
            this.request = request;
            this.server = server;
        }

        @Override
        public void run()
        {
            try
            {
                NetworkMessage message = Base64.objectFrom64(request.read());
                switch (message.getType())
                {
                    case MessageTypes.ECHO:
                        request.write("ECHO");
                        break;
                    case MessageTypes.TRANSACTION_SUCCESS:
                        TransactionsAndPrices.handleSuccess((TransactionSuccess) message);
                        request.write("ACK");
                        server.log("Transaction success: " + ((TransactionSuccess) message).transId);
                        server.notifyUpdate();
                        break;
                    case MessageTypes.REVOKE_TRANSACTION_RESPONSE:
                        server.log(TransactionsAndPrices.handleRevoke((RevokeTransactionResponse) message));
                        server.notifyUpdate();
                        break;
                    case MessageTypes.ANNOUNCE_PRICES:
                        TransactionsAndPrices.prices=((PriceAnnounce)message).priceList;
                        server.notifyUpdate();
                        break;
                    default:
                        server.log("Got unknown request type: " + message);
                        break;

                }
            }
            catch (IOException | ClassNotFoundException | ClassCastException | IllegalArgumentException e)
            {
                server.log("Exception while handling request");
                server.log(e.toString());
                server.log(e.getMessage());
            }
            finally
            {
                request.close();
            }
        }
    }
}
