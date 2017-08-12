package org.relic.util;

import java.util.*;
import java.io.*;
import org.relic.util.AsmLine;
import org.relic.util.Symbol;

public class AsmTable extends ArrayList
{
	SymbolTable symbols;
	
	public AsmTable()
	{
		super();
		symbols = new SymbolTable();
	}
	
	public Boolean read(String infile)
	{
		BufferedReader in;
		
		try
		{
			in = new BufferedReader(new FileReader(infile));
		}
		catch (IOException e)
		{
			System.err.println("failed to create file for reading");
			return false;
		}

		Boolean eof = false;
		
		do
		{
			String line;
			
			try
			{
				line = in.readLine();
			}
			catch (IOException e)
			{
				System.err.println("failed on reading");
				return false;
			}

			if (line == null)
			{
				eof = true;
			}
			else
			{
				AsmLine l = new AsmLine(line);
				add(l);
			}
		}
		while (eof == false);
		
		try
		{
			in.close();
		}
		catch (IOException e)
		{
			return false;
		}
		
		return true;
	}
	
	public void show(String outFile)
	{
		FileWriter out = null;
		
		if (outFile != null)
		{		
			try
			{
				out = new FileWriter(outFile);
			}
			catch (IOException e)
			{
				System.err.println("failed to create file for writing");
				return;
			}
		}
		
		for (Iterator it = this.iterator(); it.hasNext(); )
		{
			AsmLine l = (AsmLine)it.next();
			l.show(out);
		}

		if (outFile != null)
		{		
			try
			{
				out.close();
			}
			catch (IOException e)
			{
			}
		}
	}
}
