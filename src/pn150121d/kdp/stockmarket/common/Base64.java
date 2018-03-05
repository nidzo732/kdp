package pn150121d.kdp.stockmarket.common;

import java.io.*;

public class Base64
{
    public static String objectTo64(Serializable object) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream stream=new ObjectOutputStream(byteStream);
        stream.writeObject(object);
        stream.flush();
        return java.util.Base64.getEncoder().encodeToString(byteStream.toByteArray());
    }
    public static <T extends Serializable> T objectFrom64(String base64) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(java.util.Base64.getDecoder().decode(base64));
        ObjectInputStream stream=new ObjectInputStream(byteStream);
        return (T)stream.readObject();
    }
}
