package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionsAndPrices
{
    static HashMap<String, Transaction> transactions = new HashMap<>();
    static List<Price> prices = new LinkedList<>();
    private static ReadWriteLock lock = new ReentrantReadWriteLock(true);

    static void getReadLock()
    {
        lock.readLock().lock();
    }

    static void releaseReadLock()
    {
        lock.readLock().unlock();
    }

    static void addTransaction(Transaction transaction)
    {
        lock.writeLock().lock();
        transactions.put(transaction.id, transaction);
        lock.writeLock().unlock();
    }

    static void handleSuccess(TransactionSuccess success)
    {
        lock.writeLock().lock();
        if (!transactions.containsKey(success.transId)) return;
        Transaction trans = transactions.get(success.transId);
        trans.count -= success.count;
        if (trans.count == 0) transactions.remove(success.transId);
        lock.writeLock().unlock();
    }

    static String handleRevoke(RevokeTransactionResponse response)
    {

        switch (response.status)
        {
            case "OK":
                lock.writeLock().lock();
                if (transactions.containsKey(response.transId))
                {
                    transactions.remove(response.transId);
                }
                lock.writeLock().unlock();
                return "Transakcija opozvana";
            case "NOT_FOUND":
                return "Transakcija nije nadjena";
            case "TOO_SOON":
                return "Transakcija mora biti aktivna najmanje " + TimeConstants.MIN_AGE_BEFORE_REVOKE + " sekundi pre opoziva";
        }
        return null;
    }
}
