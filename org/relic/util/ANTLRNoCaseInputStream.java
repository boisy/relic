package org.relic.util;

import org.antlr.runtime.*;
import java.io.*;

/**
 *
 * @author jimi
 */
public class ANTLRNoCaseInputStream  extends ANTLRInputStream
{
	public ANTLRNoCaseInputStream(FileInputStream stream) throws IOException
	{
		super(stream, null);
	}

//	public ANTLRNoCaseInputStream(String fileName, String encoding) throws IOException
//	{
//		super(fileName, encoding);
//	}
	    
	public int LA(int i)
	{
		if (i == 0)
		{
			return 0; // undefined
		}
		if (i < 0)
		{
			i++; // e.g., translate LA(-1) to use offset 0 
		}

		if ((p + i - 1) >= n)
		{
            //System.out.println("char LA("+i+")=EOF; p="+p);
            return CharStream.EOF;
        }
        //System.out.println("char LA("+i+")="+data.charAt(p+i-1)+";
//p="+p);
        return Character.toUpperCase(data[p + i - 1]);
    }
}
