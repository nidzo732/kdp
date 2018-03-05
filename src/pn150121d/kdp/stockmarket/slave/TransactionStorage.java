package pn150121d.kdp.stockmarket.slave;

import pn150121d.kdp.stockmarket.common.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class TransactionStorage
{
    private static ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private static HashMap<Integer, List<Transaction>> sales = new HashMap<>();
    private static HashMap<Integer, List<Transaction>> purchases = new HashMap<>();
    private static HashMap<Integer, Integer> prices = new HashMap<>();

    static void getReadLock()
    {
        lock.readLock().lock();
    }

    static void releaseReadLock()
    {
        lock.readLock().unlock();
    }


    static List<Price> getPrices()
    {
        lock.readLock().lock();
        try
        {
            List<Price> result = new LinkedList<>();
            for (Integer item : prices.keySet())
            {
                result.add(new Price(item, prices.get(item)));
            }
            return result;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    static List<Transaction> getAllTransactions(TransactionType type)
    {
        List<Transaction> result = new LinkedList<>();
        HashMap<Integer, List<Transaction>> target = null;
        if (type == TransactionType.SALE) target = sales;
        else target = purchases;
        for (List<Transaction> l : target.values())
        {
            result.addAll(l);
        }
        return result;
    }

    static List<TransactionSuccess> process(Transaction trans)
    {
        switch (trans.type)
        {
            case SALE:
                return sell(trans, true);
            case PURCHASE:
                return purchase(trans, true);
        }
        return null;
    }

    static boolean revoke(Transaction trans)
    {
        lock.writeLock().lock();
        try
        {
            HashMap<Integer, List<Transaction>> targetMap = null;
            if (trans.type == TransactionType.SALE) targetMap = sales;
            else targetMap = purchases;
            if (targetMap.containsKey(trans.item))
            {
                List<Transaction> transactions = targetMap.get(trans.item);
                Transaction target = null;
                for (Transaction t : transactions)
                {
                    if (t.id.equals(trans.id))
                    {
                        if (t.sender.equals(trans.sender) && t.timeStamp + TimeConstants.MIN_AGE_BEFORE_REVOKE < (new Date().getTime()))
                        {
                            target = t;
                        }
                        break;
                    }
                }
                if (target != null)
                {
                    transactions.remove(target);
                    return true;
                }
            }
            return false;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private static List<TransactionSuccess> sell(Transaction trans, boolean recurse)
    {
        lock.writeLock().lock();
        try
        {
            if (recurse && purchases.containsKey(trans.item))
            {
                List<Transaction> purchasesList = purchases.get(trans.item);
                TransactionSuccess response = new TransactionSuccess(trans.sender, trans.type, trans.item, 0);
                List<TransactionSuccess> successes = new LinkedList<>();
                while (purchasesList.size() > 0 && purchasesList.get(0).price >= trans.price && trans.count > 0)
                {
                    Transaction offer = purchasesList.get(0);
                    if (offer.count <= trans.count)
                    {
                        purchasesList.remove(0);
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, offer.count));
                        trans.count -= offer.count;
                        response.count += offer.count;
                    }
                    else
                    {
                        offer.count -= trans.count;
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, trans.count));
                        response.count += trans.count;
                        trans.count = 0;
                    }
                    prices.put(trans.item, trans.price);
                }
                if (response.count > 0) successes.add(response);
                if (trans.count > 0) sell(trans, false);
                return successes;
            }
            else
            {
                trans.timeStamp = (new Date()).getTime();
                if (!sales.containsKey(trans.item))
                {
                    sales.put(trans.item, new ArrayList<>());
                }
                sales.get(trans.item).add(trans);
                sales.get(trans.item).sort((o1, o2) -> Integer.compare(o1.price, o2.price));
                return new LinkedList<>();
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private static List<TransactionSuccess> purchase(Transaction trans, boolean recurse)
    {
        lock.writeLock().lock();
        try
        {
            if (recurse && sales.containsKey(trans.item))
            {
                List<Transaction> salesList = sales.get(trans.item);
                TransactionSuccess response = new TransactionSuccess(trans.sender, trans.type, trans.item, 0);
                List<TransactionSuccess> successes = new LinkedList<>();
                while (salesList.size() > 0 && salesList.get(0).price <= trans.price && trans.count > 0)
                {
                    Transaction offer = salesList.get(0);
                    if (offer.count <= trans.count)
                    {
                        salesList.remove(0);
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, offer.count));
                        trans.count -= offer.count;
                        response.count += offer.count;
                    }
                    else
                    {
                        offer.count -= trans.count;
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, trans.count));
                        response.count += trans.count;
                        trans.count = 0;
                    }
                    prices.put(trans.item, offer.price);
                }
                if (response.count > 0) successes.add(response);
                if (trans.count > 0) purchase(trans, false);
                return successes;
            }
            else
            {
                trans.timeStamp = (new Date()).getTime();
                if (!purchases.containsKey(trans.item))
                {
                    purchases.put(trans.item, new ArrayList<>());
                }
                purchases.get(trans.item).add(trans);
                purchases.get(trans.item).sort((o1, o2) -> Integer.compare(o2.price, o1.price));
                return new LinkedList<>();
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
}
