package pn150121d.kdp.stockmarket.master;

import pn150121d.kdp.stockmarket.common.NetworkMessage;
import pn150121d.kdp.stockmarket.common.RegistrationRequest;
import pn150121d.kdp.stockmarket.common.SlaveRegistrationRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Router
{
    static ReadWriteLock lock=new ReentrantReadWriteLock();
    static HashMap<String, Client> clients=new HashMap<>();
    static HashMap<Integer, Slave> slaves=new HashMap<>();
    static Iterator<Slave> slavesIterator=null;
    static HashMap<Integer, Integer> slaveTransMap=new HashMap<>();
    static int slaveCount=0;

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
            int newSlaveId=slaveCount++;
            slaves.put(newSlaveId, new Slave(fromIp, request.port, newSlaveId));
            slavesIterator=slaves.values().iterator();
            return true;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    private static int getTargetSlave(int itemId)
    {
        if(slaves.size()==0) return -1;
        if(slaveTransMap.containsKey(itemId)) return slaveTransMap.get(itemId);
        else
        {
            if(!slavesIterator.hasNext()) slavesIterator=slaves.values().iterator();
            Slave slave=slavesIterator.next();
            slaveTransMap.put(itemId, slave.id);
            return slave.id;
        }
    }
    static String routeMessageToSlave(NetworkMessage message, int itemId)
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
    static void getReadLock()
    {
        lock.readLock().lock();
    }

    static void releaseReadLock()
    {
        lock.readLock().unlock();
    }
}
