package pn150121d.kdp.stockmarket.common;

/**
 * Klase Server i RequestHandler objavljuju poruke o zanimljivim događajima
 * Klase koje žele da prime ove poruke (GUI ili konzolni logger, na primer)
 * implementiraju ovaj interfejs
 */
public interface Logger
{
    void logMessage(String message);
}
