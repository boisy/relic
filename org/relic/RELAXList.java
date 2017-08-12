package org.relic;

import org.antlr.runtime.*;
import org.relic.util.*;
import java.io.*;
import java.util.*;

public class RELAXList extends RelicObject
{
	static int errorCount = 0, warningCount = 0;
	static SymbolTable symbolTable;
	static QuadTable quadTable;
	
    public static void main(String[] args) throws Exception
	{
		ObjectInputStream s;
		String file = null;

		try
		{
			file = args[0];
		}
		catch(Exception e)
		{
			System.err.println("filename argument required");
			System.exit(1);
		}
		
		try
		{
			FileInputStream in = new FileInputStream(file);
			s = new ObjectInputStream(in);
		}
		catch (IOException e)
		{
			System.err.println("failed to open file '" + file + "'");
			return;
		}

		try
		{
			symbolTable = (SymbolTable)s.readObject();
			quadTable = (QuadTable)s.readObject();
		}
		catch (IOException e)
		{
			System.err.println("failed to read symbolTable or quadTable");
			return;
		}

		symbolTable.show();
		quadTable.show();
	}
}
