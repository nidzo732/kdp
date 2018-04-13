package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.*;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * RequestHandler za glavni server
 */
public class RequestHandler implements pn150121d.kdp.stockmarket.common.RequestHandler
{
    private static int nextTransId = 0;

    private static synchronized int getNextTransactionId()
    {
        return nextTransId++;
    }

    @Override
    public void handleRequest(SocketWrapper request, Server server)
    {
        new Thread(new Handler(request, server)).start();
    }

    @Override
    public void handleDelayedRequests(List<NetworkMessage> requests, Server server)
    {
        for(NetworkMessage request:requests)
        {
            new Thread(new Handler(request, server)).start();
        }
    }

    private static class Handler implements Runnable
    {
        private final SocketWrapper request;
        private final Server server;
        private final NetworkMessage predefinedMessage;

        Handler(SocketWrapper request, Server server)
        {
            this.request = request;
            this.server = server;
            this.predefinedMessage = null;
        }

        Handler(NetworkMessage predefinedMessage, Server server)
        {
            this.request = null;
            this.server = server;
            this.predefinedMessage = predefinedMessage;
        }


        @Override
        public void run()
        {
            try
            {
                NetworkMessage message;
                if (predefinedMessage != null) message = predefinedMessage;
                else message = Base64.objectFrom64(request.read());
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
                        break;
                    case MessageTypes.GET_TRANSACTION_LIST:
                        getTransactionList((GetTransactionListRequest)message);
                        break;
                    case MessageTypes.ECHO:
                        respond("ECHO");
                        break;
                    default:
                        server.log("Got unknown request type: " + message);
                        break;

                }
            }
            catch (IOException | ClassNotFoundException | ClassCastException | IllegalArgumentException | InterruptedException e)
            {
                server.log("Exception while handling request");
                server.log(e.toString());
                server.log(e.getMessage());
            }
            finally
            {
                if (request != null) request.close();
            }
        }

        private void getTransactionList(GetTransactionListRequest message) throws InterruptedException
        {
            if (!Router.clients.containsKey(message.sender))
            {
                server.log("Got transaction from unknown client");
                respond("REJECT");
                return;
            }
            Client client = Router.clients.get(message.sender);
            if (request != null)
            {
                if (!client.ip.equals(request.getIp()))
                {
                    server.log("Got transaction from fake client");
                    request.write("REJECT");
                    return;
                }
            }
            request.write("OK");
            Router.getReadLock();
            try
            {
                List<Transaction> transactionList= Collections.synchronizedList(new LinkedList<>());
                List<Slave> slaves=Router.getAllSlaves();
                List<Thread> threads=new LinkedList<>();
                for(Slave slave: slaves)
                {
                    Thread thread=new Thread(() -> {
                        String response = slave.sendWithoutBacklog(message);
                        if(response!=null)
                        {
                            try
                            {
                                GetTransactionListResponse rsp=Base64.objectFrom64(response);
                                transactionList.addAll(rsp.transactionList);
                            }
                            catch (IOException | ClassNotFoundException e)
                            {
                                return;
                            }
                        }
                    });
                    thread.start();
                    threads.add(thread);
                }
                for (Thread thread:threads)
                {
                    thread.join();
                }
                client.sendWithoutBacklog(new GetTransactionListResponse(new LinkedList<>(transactionList)));
            }
            finally
            {
                Router.releaseReadLock();
            }
        }

        private void processTransaction(Transaction trans) throws IOException, ClassNotFoundException
        {
            if (!Router.clients.containsKey(trans.sender))
            {
                server.log("Got transaction from unknown client");
                respond("REJECT");
                return;
            }
            Client client = Router.clients.get(trans.sender);
            if (request != null)
            {
                if (!client.ip.equals(request.getIp()))
                {
                    server.log("Got transaction from fake client");
                    request.write("REJECT");
                    return;
                }
            }
            if(request!=null) trans.id = Integer.toString(getNextTransactionId());
            String response = Router.routeMessageToSlave(trans, trans.item);
            if (response != null)
            {
                if (response.equals("NO_SLAVES"))
                {
                    respond(response);
                    return;
                }
                else
                {
                    respond(trans.id);
                }
                List<TransactionSuccess> statuses = Base64.objectFrom64(response);
                if (statuses.size() > 0)
                {
                    for (TransactionSuccess status : statuses)
                    {
                        Client cl = Router.clients.get(status.target);
                        cl.send(status);
                    }
                }
                server.notifyUpdate();
            }
            else
            {
                server.notifyUpdate();
                respond(trans.id);
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

        private void revokeTransaction(RevokeTransactionRequest req) throws IOException, ClassNotFoundException
        {
            if (!Router.clients.containsKey(req.trans.sender))
            {
                server.log("Got transaction from unknown client");
                return;
            }
            Client client = Router.clients.get(req.trans.sender);
            if (request != null)
            {
                if (!client.ip.equals(request.getIp()))
                {
                    server.log("Got transaction from fake client");
                    request.write("REJECT");
                    return;
                }
            }
            respond("OK");
            String response = Router.routeMessageToSlave(req, req.trans.item);
            NetworkMessage responseObject = Base64.objectFrom64(response);
            if (response != null)
            {
                client.send(responseObject);
                server.notifyUpdate();
            }
        }

        private void respond(String message)
        {
            if (request != null)
            {
                request.write(message);
            }
        }
    }
}
