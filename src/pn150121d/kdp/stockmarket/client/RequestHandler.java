package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Klasa koja obrađuje poruke koje klijent primi preko mreže
 */
public class RequestHandler implements pn150121d.kdp.stockmarket.common.RequestHandler
{
    private final Set<Integer> handledMessages=new HashSet<>();
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
                synchronized (handledMessages)
                {
                    if(handledMessages.contains(message.msgId))
                    {
                        request.write("ACK");
                        return;
                    }
                    else
                    {
                        handledMessages.add(message.msgId);
                    }
                }
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
                        request.write("ACK");
                        server.notifyUpdate();
                        break;
                    case MessageTypes.GET_TRANSACTION_LIST_RESPONSE:
                        TransactionsAndPrices.globalTransactions=((GetTransactionListResponse)message).transactionList;
                        server.notifyUpdate();
                        server.log("Got new list of transactions "+((GetTransactionListResponse)message).transactionList.size());
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
