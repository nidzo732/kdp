package pn150121d.kdp.stockmarket.client;

import pn150121d.kdp.stockmarket.common.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Statička klasa koja vodi lokalnu evidenciju o ponudama
 * koje je poslao ovaj klijent i o cenama
 */
class TransactionsAndPrices
{
    static HashMap<String, Transaction> transactions = new HashMap<>();
    static List<Transaction> globalTransactions=null;
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


    /**
     * Dodaje novu transakciju u listu
     * @param transaction transakcija
     */
    static void addTransaction(Transaction transaction)
    {
        lock.writeLock().lock();
        transactions.put(transaction.id, transaction);
        lock.writeLock().unlock();
    }

    /**
     * Ažurira postojeću transakciju na osnovu podataka o promeni
     * @param success podaci o promeni
     */
    static void handleSuccess(TransactionSuccess success)
    {
        lock.writeLock().lock();
        if (!transactions.containsKey(success.transId)) return;
        Transaction trans = transactions.get(success.transId);
        trans.count -= success.count;
        if (trans.count == 0) transactions.remove(success.transId);
        lock.writeLock().unlock();
    }

    /**
     * Briše opozvanu transakciju iz liste, ako je opoziv uspešan
     * @param response rezultat opoziva
     * @return tekstualni opis rezultata - za prikaz na GUI-ju
     */
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
