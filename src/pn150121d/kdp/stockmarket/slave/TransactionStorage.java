package pn150121d.kdp.stockmarket.slave;

import pn150121d.kdp.stockmarket.common.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class TransactionStorage
{
    private static class PriceDescriptor
    {
        private PriceDescriptor()
        {
            lastTransaction=-1;
            lastPrice=-1;
        }
        int lastTransaction;
        int lastPrice;

        @Override
        public String toString()
        {
            return "{lT:"+lastTransaction+", lP:"+lastPrice+"}";
        }
    }
    private static ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private static HashMap<String, List<Transaction>> sales = new HashMap<>();
    private static HashMap<String, List<Transaction>> purchases = new HashMap<>();
    private static HashMap<String, PriceDescriptor> prices = new HashMap<>();
    private static final HashMap<String, ReadWriteLock> handledTransactionsLocks=new HashMap<>();
    private static final HashMap<String, List<TransactionSuccess>> handledTransactions=new HashMap<>();

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
            for (String item : prices.keySet())
            {
                result.add(new Price(item, getPrice(item)));
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
        lock.readLock().lock();
        try
        {
            List<Transaction> result = new LinkedList<>();
            HashMap<String, List<Transaction>> target;
            if (type == TransactionType.SALE) target = sales;
            else target = purchases;
            for (List<Transaction> l : target.values())
            {
                result.addAll(l);
            }
            return result;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    static List<TransactionSuccess> process(Transaction trans)
    {
        synchronized (handledTransactionsLocks)
        {
            if(handledTransactionsLocks.containsKey(trans.id))
            {
                handledTransactionsLocks.get(trans.id).readLock().lock();
                return handledTransactions.get(trans.id);
            }
            else
            {
                handledTransactionsLocks.put(trans.id, new ReentrantReadWriteLock(true));
                handledTransactionsLocks.get(trans.id).writeLock().lock();
            }
        }
        List<TransactionSuccess> result=null;
        switch (trans.type)
        {
            case SALE:
                result = sell(trans, true);
                break;
            case PURCHASE:
                result = purchase(trans, true);
                break;
        }
        handledTransactions.put(trans.id, result);
        handledTransactionsLocks.get(trans.id).writeLock().unlock();
        return result;
    }

    static RevokeTransactionResponse revoke(Transaction trans)
    {
        lock.writeLock().lock();
        String status = "NOT_FOUND";
        String transId = null;
        try
        {
            HashMap<String, List<Transaction>> targetMap;
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
                        if (t.sender.equals(trans.sender))
                        {
                            if (t.timeStamp + TimeConstants.MIN_AGE_BEFORE_REVOKE * 1000 >= (new Date().getTime()))
                            {
                                status = "TOO_SOON";
                            }
                            else
                            {
                                status = "OK";
                                target = t;
                                transId = target.id;
                            }
                        }
                        break;
                    }
                }
                if (target != null)
                {
                    transactions.remove(target);
                }
            }
            return new RevokeTransactionResponse(trans.sender, status, transId);
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
            if(!prices.containsKey(trans.item))
            {
                PriceDescriptor descriptor=new PriceDescriptor();
                descriptor.lastPrice=trans.price;
                prices.put(trans.item, descriptor);
            }
            if (recurse && purchases.containsKey(trans.item))
            {
                List<Transaction> purchasesList = purchases.get(trans.item);
                TransactionSuccess response = new TransactionSuccess(trans.sender, trans.type, trans.item, 0, trans.id);
                List<TransactionSuccess> successes = new LinkedList<>();
                while (purchasesList.size() > 0 && purchasesList.get(0).price >= trans.price && trans.count > 0)
                {
                    Transaction offer = purchasesList.get(0);
                    if (offer.count <= trans.count)
                    {
                        purchasesList.remove(0);
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, offer.count, offer.id));
                        trans.count -= offer.count;
                        response.count += offer.count;
                    }
                    else
                    {
                        offer.count -= trans.count;
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, trans.count, offer.id));
                        response.count += trans.count;
                        trans.count = 0;
                    }
                    updatePriceFromTransaction(trans.item, trans.price);
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
            if(!prices.containsKey(trans.item))
            {
                PriceDescriptor descriptor=new PriceDescriptor();
                descriptor.lastPrice=trans.price;
                prices.put(trans.item, descriptor);

            }
            if (recurse && sales.containsKey(trans.item))
            {
                List<Transaction> salesList = sales.get(trans.item);
                TransactionSuccess response = new TransactionSuccess(trans.sender, trans.type, trans.item, 0, trans.id);
                List<TransactionSuccess> successes = new LinkedList<>();
                while (salesList.size() > 0 && salesList.get(0).price <= trans.price && trans.count > 0)
                {
                    Transaction offer = salesList.get(0);
                    if (offer.count <= trans.count)
                    {
                        salesList.remove(0);
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, offer.count, offer.id));
                        trans.count -= offer.count;
                        response.count += offer.count;
                    }
                    else
                    {
                        offer.count -= trans.count;
                        successes.add(new TransactionSuccess(offer.sender, offer.type, offer.item, trans.count, offer.id));
                        response.count += trans.count;
                        trans.count = 0;
                    }
                    updatePriceFromTransaction(trans.item, trans.price);
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
    private static void updatePriceFromTransaction(String item, int price)
    {
        PriceDescriptor descriptor;
        if(prices.containsKey(item))
        {
            descriptor=prices.get(item);
        }
        else
        {
            descriptor=new PriceDescriptor();
            prices.put(item, descriptor);
        }
        descriptor.lastTransaction=price;
        descriptor.lastPrice=price;
    }
    private static Integer getPrice(String item)
    {
        PriceDescriptor descriptor=prices.getOrDefault(item, null);
        if(descriptor!=null && descriptor.lastTransaction>=0)
        {
            int price=descriptor.lastTransaction;
            descriptor.lastTransaction=-1;
            return price;
        }
        else if(sales.containsKey(item) && sales.get(item).size()>0)
        {
            return sales.get(item).get(0).price;
        }
        else if(purchases.containsKey(item) && purchases.get(item).size()>0)
        {
            return purchases.get(item).get(0).price;
        }
        else if(descriptor!=null && descriptor.lastPrice>=0)
        {
            return descriptor.lastPrice;
        }
        return null;
    }
}
