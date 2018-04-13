package pn150121d.kdp.stockmarket.common;

import java.io.*;

/**
 * Helper klasa za serijalizovanje objekata u base64 stringove
 */
public class Base64
{
    /**
     * Serijalizuje objekat u base64 string
     * @param object objekat za serijalizaciju
     * @return base64 string
     * @throws IOException u slučaju neuspele serijalizacije
     */
    public static String objectTo64(Serializable object) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(byteStream);
        stream.writeObject(object);
        stream.flush();
        return java.util.Base64.getEncoder().encodeToString(byteStream.toByteArray());
    }

    /**
     * Deserijalizuje objekat iz base64 stringa
     * @param base64 serijalizovani objekat u base64 formatu
     * @param <T> tip objekta koji se očekuje
     * @return deserijalizovani objekat
     * @throws IOException neuspela deserijalizacija
     * @throws ClassNotFoundException klasa serijalizovanog objekta nije poznata
     */
    public static <T extends Serializable> T objectFrom64(String base64) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(java.util.Base64.getDecoder().decode(base64));
        ObjectInputStream stream = new ObjectInputStream(byteStream);
        return (T) stream.readObject();
    }
}
