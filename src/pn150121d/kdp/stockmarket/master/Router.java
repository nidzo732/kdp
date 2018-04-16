package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.NetworkMessage;
import pn150121d.kdp.stockmarket.common.RegistrationRequest;
import pn150121d.kdp.stockmarket.common.SlaveRegistrationRequest;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe klasa za vođenje evidencije o povezanim klijentima i podserverima,
 * prosleđuje poruke na osnovu tabele rutiranja, obrađuje mrtve podservere
 */
class Router
{
    private static ReadWriteLock lock = new ReentrantReadWriteLock(true);
    static Map<String, Client> clients = Collections.synchronizedMap(new HashMap<>());
    static Map<String, Slave> slaves = Collections.synchronizedMap(new HashMap<>());
    private static Iterator<Slave> slavesIterator = null;
    static Map<String, String> slaveTransMap = Collections.synchronizedMap(new HashMap<>());
    static Set<Slave> potentiallyDeadSlaves=Collections.synchronizedSet(new HashSet<>());

    static boolean registerClient(RegistrationRequest request, String fromIp)
    {
        lock.writeLock().lock();
        Client oldClient=null;
        try
        {
            if (request.name == null || request.port <= 1024 || request.port >= 65535 || request.password == null)
                return false;
            if(clients.containsKey(request.name))
            {
                oldClient=clients.get(request.name);
                if(!oldClient.password.equals(request.password))
                {
                    return false;
                }
                else
                {
                    clients.remove(request.name);
                }
            }
            Client newClient=new Client(fromIp, request.port, request.name, request.password);
            if(oldClient!=null) newClient.loadBacklog(oldClient);
            clients.put(request.name, newClient);
            return true;

        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    static boolean registerSlave(SlaveRegistrationRequest request, String fromIp)
    {
        lock.writeLock().lock();
        try
        {
            if (request.port <= 1024 || request.port >= 65535) return false;
            slaves.put(fromIp+":"+request.port, new Slave(fromIp, request.port, fromIp+":"+request.port));
            slavesIterator = slaves.values().iterator();
            return true;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private static String getTargetSlave(String itemId)
    {
        if (slaves.size() == 0) return null;
        if (slaveTransMap.containsKey(itemId))
        {
            String selectedSlave=slaveTransMap.get(itemId);
            if(slaves.containsKey(selectedSlave))
            {
                return selectedSlave;
            }
            else
            {
                slaveTransMap.remove(itemId);
                return getTargetSlave(itemId);
            }
        }
        else
        {
            if (!slavesIterator.hasNext()) slavesIterator = slaves.values().iterator();
            Slave slave = slavesIterator.next();
            slaveTransMap.put(itemId, slave.id);
            return slave.id;
        }
    }

    static Slave getSlaveAndTakeReadLock(String itemId)
    {
        lock.writeLock().lock();
        String targetSlave = getTargetSlave(itemId);
        lock.readLock().lock();
        lock.writeLock().unlock();
        if (targetSlave == null) return null;
        return slaves.get(targetSlave);
    }
    static List<Client> getAllClients()
    {
        lock.readLock().lock();
        try
        {
            return new LinkedList<>(clients.values());
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    static List<Slave> getAllSlaves()
    {
        lock.readLock().lock();
        try
        {
            return new LinkedList<>(slaves.values());
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    static List<NetworkMessage> handleDeadSlavesAndGetAllBacklogs()
    {
        lock.writeLock().lock();
        try
        {
            List<Slave> slavesToRemove = new LinkedList<>();
            List<NetworkMessage> backlogs = new LinkedList<>();
            for (Slave slave : slaves.values())
            {
                if (slave.getFailureCount() >= 3)
                {
                    slavesToRemove.add(slave);
                }
                backlogs.addAll(slave.swapBacklogs());
            }
            for (Slave slave : slavesToRemove)
            {
                slaves.remove(slave.id);
            }
            if (slavesToRemove.size() > 0)
            {
                slavesIterator = slaves.values().iterator();
            }
            return backlogs;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    static void getReadLock()
    {
        lock.readLock().lock();
    }

    static void releaseReadLock()
    {
        lock.readLock().unlock();
    }
}
