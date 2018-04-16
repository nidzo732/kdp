package pn150121d.kdp.stockmarket.slave;

import pn150121d.kdp.stockmarket.common.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe skladište za informacije o ponudama i za uparivanje ponuda
 */
class TransactionStorage
{
    private static class PriceDescriptor
    {
        private PriceDescriptor()
        {
            reset();
            previousIteration=-1;
        }
        private void reset()
        {
            previousIteration=getValue();
            lastTransaction=-1;
            lastSaleOffer=Integer.MAX_VALUE;
            lastPurchaseOffer=-1;
        }
        private int getValue()
        {
            if(lastTransaction!=-1) return lastTransaction;
            else if(lastSaleOffer!=Integer.MAX_VALUE) return lastSaleOffer;
            else if(lastPurchaseOffer!=-1) return lastPurchaseOffer;
            else return previousIteration;
        }
        private float getGrowth()
        {
            if(previousIteration==-1) return 0;
            int current=getValue();
            return ((float)current)/((float)previousIteration)-1;

        }
        int lastSaleOffer;
        int lastPurchaseOffer;
        int lastTransaction;
        int previousIteration;
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


    static List<Price> getPrices(boolean doReset)
    {
        lock.readLock().lock();
        try
        {
            List<Price> result = new LinkedList<>();
            for (String item : prices.keySet())
            {
                PriceDescriptor descriptor=prices.get(item);
                result.add(new Price(item, descriptor.getValue(), descriptor.getGrowth()));
                if(doReset) descriptor.reset();
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
        if(trans.price<=0) return new LinkedList<>();
        synchronized (handledTransactionsLocks)
        {
            if(handledTransactionsLocks.containsKey(trans.id))
            {
                try
                {
                    handledTransactionsLocks.get(trans.id).readLock().lock();
                    return handledTransactions.get(trans.id);
                }
                finally
                {
                    handledTransactionsLocks.get(trans.id).readLock().unlock();
                }
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
                if(successes.size()==0) updatePriceFromOffer(trans.item, trans.price, trans.type);
                return successes;
            }
            else
            {
                if(recurse) updatePriceFromOffer(trans.item, trans.price, trans.type);
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
                if(successes.size()==0) updatePriceFromOffer(trans.item, trans.price, trans.type);
                return successes;
            }
            else
            {
                if(recurse) updatePriceFromOffer(trans.item, trans.price, trans.type);
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
    private static void updatePriceFromOffer(String item, int price, TransactionType type)
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
        if(type==TransactionType.SALE)
        {
            if(price<descriptor.lastSaleOffer) descriptor.lastSaleOffer=price;
        }
        else
        {
            if(price>descriptor.lastPurchaseOffer) descriptor.lastPurchaseOffer=price;
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
    }
}
