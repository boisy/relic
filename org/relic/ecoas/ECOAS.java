/*
 * ECOAS - Energy Conserving Optimizing Assembler
 *
 * 2010 Boisy G. Pitre
 */

package org.relic.ecoas;

import java.util.*;
import java.io.IOException;

import org.relic.ecoas.h6309.Assembler;
import org.relic.ecoas.util.AssemblerException;
import org.relic.ecoas.util.SymbolTable;
import org.relic.ecoas.util.Symbol;

public class ECOAS
{
	static Boolean showListing = false;
	static Boolean showSymbolMap = false;
	static Boolean showSummary = true;
	static Boolean verbose = false;
	static ArrayList<String> sourceFiles = new ArrayList<String>();
	static String destFile = null;
	static String moduleName = null;
	static String version = "0.1";
	String file;
	static SymbolTable symbols = new SymbolTable();
	static int reductionLevel = 0;

	static int processOptions(String[] args)
	{
		for (int a = 0; a < args.length; a++)
		{
			if (args[a].charAt(0) == '-')
			{
				int b = 1;
				
				while (args[a].length() > b)
				{
					switch (args[a].charAt(b++))
					{
						case 'a':
							String name = args[a].substring(b);
							int value = 1;
							int equals = name.indexOf('=');
							if (equals != -1)
							{
								value = Integer.parseInt(name.substring(equals + 1));
								name = name.substring(0, equals);
							}
							symbols.add(new Symbol(name, value, "cnst", true));
							b = args[a].length();
							break;
		
						case 'l':
							showListing = true;
							break;
		
						case 'n':
							moduleName = args[a].substring(3);
							break;
		
						case 'o':
							destFile = args[a].substring(3);
							b = args[a].length();
							break;
		
						case 'r':
							reductionLevel = 1;
							if (args[a].length() >= b)
							{
								reductionLevel = Integer.parseInt(args[a].substring(b));
								b = args[a].length();
							}
							break;
		
						case 's':
							showSymbolMap = true;
							break;
		
						case 'v':
							verbose = true;
							break;
		
						default:
							System.err.println("unknown option: " + args[a]);
							return 1;
					}
				}
			} else {
				sourceFiles.add(args[a]);
			}
		}

		return 0;
	}

	public static void main(String[] args) throws AssemblerException
	{
		if (args.length < 1)
		{
			showHelp();
			return;
		}

		if (processOptions(args) == 0)
		{
			try
			{
				Assembler m = new Assembler(true, sourceFiles);
				m.symbols.putAll(symbols);
				m.verbose = verbose;
				
				try
				{
					if (moduleName == null) moduleName = destFile;
					m.assemble(moduleName, destFile, reductionLevel);
					if (showListing)
					{
						m.showSource();
					}
					
					if (verbose == true)
					{
						m.showErrors();
						m.showWarnings();
					}
					
					if (showSummary)
					{
						m.showSummary();
					
					}
				}
				catch (AssemblerException e)
				{
					System.out.println(e.getMessage());
				}
			}
			catch (IOException e)
			{
				
			}
		}
	}
	
	public static void showHelp()
	{
		System.err.println("ECOAS v" + version);
		System.err.println("usage: java org.toolshed.ECOAS infile <opts>");
		System.err.println("options:");
		System.err.println("     -a<sym>[=<val>] assign val to sym");
		System.err.println("     -l              show listing");
		System.err.println("     -n=name         module name");
		System.err.println("     -o=file         output file");
		System.err.println("     -r              do not apply assembler reductions");
		System.err.println("     -s              show symbol map");
		System.err.println("     -v              verbose output");
	}


	public String toString()
	{
		return file;
	}
}
