package org.relic.util;

public class StringToolbox
{
	public static String fileName(String filename)
	{
		String fn = fileNameWithPath(filename);
		
		int i = fn.lastIndexOf('/');

		if (i == -1)
		{
			return fn;
		}
		

		return fn.substring(i + 1, fn.length());
	}
	
	public static String fileNameWithPath(String filename)
	{
		int i = filename.lastIndexOf('.');

		if (i == -1)
		{
			return filename;
		}

		return filename.substring(0, i);
	}
	
	public static String extension(String filename)
	{
		int i = filename.lastIndexOf('.');

		if (i == -1)
		{
			return null;
		}

		return filename.substring(i + 1);
	}
}
