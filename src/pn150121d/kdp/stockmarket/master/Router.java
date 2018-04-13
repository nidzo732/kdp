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
    static Map<Integer, Slave> slaves = Collections.synchronizedMap(new HashMap<>());
    private static Iterator<Slave> slavesIterator = null;
    static Map<String, Integer> slaveTransMap = Collections.synchronizedMap(new HashMap<>());
    static Set<Slave> potentiallyDeadSlaves=Collections.synchronizedSet(new HashSet<>());
    private static int slaveCount = 0;

    static boolean registerClient(RegistrationRequest request, String fromIp)
    {
        lock.readLock().lock();
        try
        {
            if (request.name == null || request.port <= 1024 || request.port >= 65535 || request.password == null)
                return false;
            clients.put(request.name, new Client(fromIp, request.port, request.name));
            return true;

        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    static boolean registerSlave(SlaveRegistrationRequest request, String fromIp)
    {
        lock.writeLock().lock();
        try
        {
            if (request.port <= 1024 || request.port >= 65535) return false;
            int newSlaveId = slaveCount++;
            slaves.put(newSlaveId, new Slave(fromIp, request.port, newSlaveId));
            slavesIterator = slaves.values().iterator();
            return true;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private static int getTargetSlave(String itemId)
    {
        if (slaves.size() == 0) return -1;
        if (slaveTransMap.containsKey(itemId))
        {
            int selectedSlave=slaveTransMap.get(itemId);
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

    static String routeMessageToSlave(NetworkMessage message, String itemId)
    {
        lock.writeLock().lock();
        int targetSlave = getTargetSlave(itemId);
        lock.readLock().lock();
        lock.writeLock().unlock();
        try
        {
            if (targetSlave < 0) return "NO_SLAVES";
            Slave slave = slaves.get(targetSlave);
            return slave.send(message);
        }
        finally
        {
            lock.readLock().unlock();
        }
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
