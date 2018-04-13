package pn150121d.kdp.stockmarket.common;

/**
 * Ako neki objekat želi da prati stanje lokalnih podataka o ponudama,
 * tabeli rutiranja itd. može da se prijavi serveru kao UpdateListener
 */
public interface UpdateListener
{
    void dataUpdated();
}
