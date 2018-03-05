package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class RequestHandler implements pn150121d.kdp.stockmarket.common.RequestHandler
{
    @Override
    public void handleRequest(SocketWrapper request, Server server)
    {
        new Thread(new Handler(request, server)).start();
    }

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
                        processTransaction((Transaction) message);
                        break;
                    case MessageTypes.REGISTER_CLIENT:
                        registerClientRequest((RegistrationRequest) message);
                        break;
                    case MessageTypes.REGISTER_SLAVE:
                        registerSlaveRequest((SlaveRegistrationRequest) message);
                        break;
                    case MessageTypes.REVOKE_TRANSACTION:
                        revokeTransaction((RevokeTransactionRequest) message);
                    case MessageTypes.ECHO:
                        request.write("ECHO");
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
                try
                {
                    request.close();
                }
                catch (IOException ignored)
                {

                }
            }
        }

        private void processTransaction(Transaction trans) throws IOException, ClassNotFoundException
        {
            if(!Router.clients.containsKey(trans.sender))
            {
                server.log("Got transaction from unknown client");
                return;
            }
            trans.id = UUID.randomUUID().toString();
            String response = Router.routeMessageToSlave(trans, trans.item);
            if (response != null)
            {
                if(response.equals("NO_SLAVES"))
                {
                    request.write(response);
                    return;
                }
                List<TransactionSuccess> statuses = Base64.objectFrom64(response);
                if(statuses.size()>0)
                {
                    for (TransactionSuccess status : statuses)
                    {
                        if (status.target.equals(trans.sender))
                        {
                            request.write(Base64.objectTo64(status));
                        }
                        else
                        {
                            Client client = Router.clients.get(status.target);
                            client.send(status);
                        }
                    }
                }
                else
                {
                    request.write("OK");
                }
                server.notifyUpdate();
            }
            else
            {
                server.notifyUpdate();
                request.write("OK");
            }
        }

        private void registerClientRequest(RegistrationRequest req)
        {
            if (Router.registerClient(req, request.getIp()))
            {
                server.log("Client registration accepted");
                request.write("OK");
                server.notifyUpdate();
            }
            else
            {
                server.log("Client registration rejected");
                request.write("FAIL");
                server.notifyUpdate();
            }
        }

        private void registerSlaveRequest(SlaveRegistrationRequest reg)
        {
            if (Router.registerSlave(reg, request.getIp()))
            {
                server.log("Slave registration accepted");
                request.write("OK");
                server.notifyUpdate();
            }
            else
            {
                server.log("Slave registration rejected");
                request.write("FAIL");
                server.notifyUpdate();
            }
        }

        private void revokeTransaction(RevokeTransactionRequest req)
        {
            if(!Router.clients.containsKey(req.trans.sender))
            {
                server.log("Got transaction from unknown client");
                return;
            }
            String response = Router.routeMessageToSlave(req, req.trans.item);
            if (response != null)
            {
                request.write(response);
                server.notifyUpdate();
            }
        }
    }
}
