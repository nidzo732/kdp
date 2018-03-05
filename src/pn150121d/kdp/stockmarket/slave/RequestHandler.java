package pn150121d.kdp.stockmarket.slave;

import pn150121d.kdp.stockmarket.common.*;

import java.io.IOException;
import java.io.Serializable;

public class RequestHandler implements pn150121d.kdp.stockmarket.common.RequestHandler
{
    private static class Handler implements Runnable
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
                    case MessageTypes.PROCESS_TRANSACTION:
                        handleTransaction((Transaction) message);
                        break;
                    case MessageTypes.GET_PRICES:
                        handlePriceQuery((GetPricesRequest) message);
                        break;
                    case MessageTypes.REVOKE_TRANSACTION:
                        handleRevoke((RevokeTransactionRequest) message);
                        break;
                    case MessageTypes.ECHO:
                        request.write("ECHO");
                        break;
                    default:
                        server.log("Got unknown request type: " + message);
                        break;
                }
            }
            catch (IOException | ClassNotFoundException | ClassCastException e)
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

        private void handleTransaction(Transaction trans) throws IOException, ClassNotFoundException
        {
            server.log("Handling transaction: " + trans.toString());
            request.write(Base64.objectTo64((Serializable) TransactionStorage.process(trans)));
            server.notifyUpdate();
        }

        private void handlePriceQuery(GetPricesRequest prices) throws IOException
        {
            server.log("Handling price query");
            request.write(Base64.objectTo64((Serializable) TransactionStorage.getPrices()));
            server.notifyUpdate();
        }

        private void handleRevoke(RevokeTransactionRequest req) throws IOException, ClassNotFoundException
        {
            Transaction trans = req.trans;
            server.log("Handling revoke: " + trans.toString());
            request.write(Base64.objectTo64(TransactionStorage.revoke(trans)));
            server.notifyUpdate();
        }
    }
    @Override
    public void handleRequest(SocketWrapper request, Server server)
    {
        new Thread(new Handler(request, server)).start();
    }
}
