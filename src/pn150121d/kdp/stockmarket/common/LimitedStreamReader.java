package pn150121d.kdp.stockmarket.common;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class LimitedStreamReader extends InputStreamReader
{
    private static final int MAX_READ_LIMIT = 10485760;
    private int bytesRead=0;
    public LimitedStreamReader(InputStream in)
    {
        super(in);
    }

    @Override
    public int read(char[] cbuf, int offset, int length) throws IOException
    {
        if(bytesRead>=MAX_READ_LIMIT)
        {
            throw new IOException("Message too long");
        }
        int result=super.read(cbuf, offset, length);
        if(result!=-1) bytesRead+=result;
        return result;
    }

    @Override
    public int read(CharBuffer target) throws IOException
    {
        if(bytesRead>=MAX_READ_LIMIT)
        {
            throw new IOException("Message too long");
        }
        int result=super.read(target);
        if(result!=-1) bytesRead+=result;
        return result;
    }

    @Override
    public int read(char[] cbuf) throws IOException
    {
        if(bytesRead>=MAX_READ_LIMIT)
        {
            throw new IOException("Message too long");
        }
        int result = super.read(cbuf);
        if(result!=-1) bytesRead+=result;
        return result;
    }

    @Override
    public int read() throws IOException
    {
        if(bytesRead>=MAX_READ_LIMIT)
        {
            throw new IOException("Message too long");
        }
        bytesRead++;
        return super.read();
    }
}
