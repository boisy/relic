package org.relic;

import org.relic.util.*;
import org.antlr.runtime.*;
import java.io.*;
import java.util.*;

public class RaginBasicFrontEnd extends RelicObject
{
	static ArrayList errors = new ArrayList();
	static ArrayList warnings = new ArrayList();
	
    public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			showHelp();
			return;
		}

		String in, out = null;
		
		in = args[0];
		if (args.length == 2)
		{
			out = args[1];
		}
		
		process(in, out);
	}	

	public static int process(String infile, String outfile) throws Exception
	{
		ANTLRNoCaseInputStream input;
		RaginBasicLexer lexer;
			
		try
		{
			input = new ANTLRNoCaseInputStream(new FileInputStream(infile));
		}
		catch (IOException e)
		{
			return 1;
		}
		
		try
		{
			lexer = new RaginBasicLexer(input);
		}
		catch (Exception e)
		{
			return 1;
		}
		
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RaginBasicParser parser = new RaginBasicParser(tokens);
        parser.program();
		if (errors.size() > 0)
		{
			System.err.print(errors);
			return 1;
		}

		if (outfile == null)
		{
			parser.symbolTable.show();
			parser.quadTable.show();
		}
		else
		{
			save(parser, outfile);
		}
		
		return 0;
    }

	public static void showHelp()
	{
		System.err.println("Ragin' Basic Front-End v" + version);
		System.err.println("usage: java org.relic.rbfe infile.rb outfile.rx");
	}
	
	public static void reportError(String errorString)
	{
		errors.add(errorString + "\n");
	}
	
	public static void reportWarning(String warningString)
	{
		warnings.add(warningString + "\n");
	}

	public static void save(RaginBasicParser parser, String file)
	{
		ObjectOutputStream s;
		
		try
		{
			FileOutputStream out = new FileOutputStream(file);
			s = new ObjectOutputStream(out);
		}
		catch (IOException e)
		{
			System.err.println("Failed to create file '" + file + "'");
			return;
		}

		try
		{
			s.writeObject(parser.symbolTable);
			s.writeObject(parser.quadTable);
		}
		catch (IOException e)
		{
			System.err.println("Failed to write symbolTable or quadTable");
			return;
		}
	}
}
