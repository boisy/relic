package org.relic;

import org.relic.util.*;
import org.relic.ecoas.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Compiler extends RelicObject
{
	static Boolean exitAfterFrontEnd = false;
	static Boolean exitAfterRELAXOptimizer = false;
	static Boolean exitAfterBackEnd = false;
	static Boolean exitAfterAssembler = false;
	static Boolean dryRun = false;
	static Boolean quietMode = false;
	static Boolean runRELAXOptimizer = true;
	static ArrayList<String> libraryPath = new ArrayList<String>();
	static String outputPath = ".";
	static String target = "6309";
	static Boolean newAssembler = false;
	static String assemblerOptions = "-";
	
	static int processOptions(String[] args)
	{
		for (int a = 0; a < args.length; a++)
		{
			if (args[a].charAt(0) == '-')
			{
				switch (args[a].charAt(1))
				{
					case 'a':
						switch(args[a].charAt(2))
						{
							case 'a':
								assemblerOptions += args[a].substring(3);
								break;
						}
						break;

					case 'd':
						dryRun = true;
						break;

					case 'n':
						newAssembler = true;
						break;

					case 'q':
						quietMode = true;
						break;

					case 's':
						switch(args[a].charAt(2))
						{
							case 'r':
								runRELAXOptimizer = false;
								break;
						}
						break;

					case 't':
   					target = args[a].substring(3, args[a].length());
						break;
                  
					case 'x':
						switch(args[a].charAt(2))
						{
							case 'f':
								exitAfterFrontEnd = true;
								break;

							case 'r':
								exitAfterRELAXOptimizer = true;
								break;

							case 'b':
								exitAfterBackEnd = true;
								break;

							case 'a':
								exitAfterAssembler = true;
								break;
						}
						break;

					case 'L':
						libraryPath.add(args[a].substring(3, args[a].length()));
						break;

					case 'O':
						outputPath = args[a].substring(3, args[a].length());
						break;

					default:
						System.err.println("unknown option: " + args[a]);
						return 1;
				}
			}
		}
	
		return 0;
	}
	
    public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			showHelp();
			return;
		}

		if (processOptions(args) == 0)
		{
			process(args);
		}
	}	

	static int execFrontEnd(String infile, String outfile) throws Exception
	{
		if (!quietMode) { System.out.println("RaginBasicFrontEnd " + infile + " " + outfile); }
		if (!dryRun)
		{
			RaginBasicFrontEnd fe = new RaginBasicFrontEnd();
			if (fe.process(infile, outfile) != 0)
			{
				System.err.println("error: front end failed to process " + infile);
				return 1;
			}
		}

		return 0;
	}
	
	static int execOptimizer(String infile, String outfile) throws Exception
	{
		if (!quietMode) { System.out.println("RELAXOpt " + infile + " " + outfile); }
		if (!dryRun)
		{
			RELAXOpt ro = new RELAXOpt();
			if (ro.process(infile, outfile) != 0)
			{
				System.err.println("error: optimizer failed to process " + infile);
				return 1;
			}
		}

		return 0;
	}
	
	static int execBackEnd6309(String infile, String outfile) throws Exception
	{
		if (!quietMode) { System.out.println("BackEnd6309 " + infile + " " + outfile); }
		if (!dryRun)
		{
			BackEnd6309 be = new BackEnd6309();
			if (be.process(infile, outfile) != 0)
			{
				System.err.println("error: optimizer failed to process " + infile);
				return 1;
			}
		}

		return 0;
	}
	
	static int execBackEndPPC(String infile, String outfile, String target) throws Exception
	{
		if (!quietMode) { System.out.println("BackEndPPC -t=" + target + " " + infile + " " + outfile); }
		if (!dryRun)
		{
			BackEndPPC be = new BackEndPPC();
			if (be.process(target, infile, outfile) != 0)
			{
				System.err.println("error: backend failed to process " + infile);
				return 1;
			}
		}
      
		return 0;
	}

	static int execECOASAssembler(String ... options) throws Exception
	{
		if (!quietMode)
		{
			StringBuilder sb = new StringBuilder();

			sb.append("Assembler ");
			
			for (int i = 0; i < options.length; i++)
			{
				sb.append(options[i] + " ");
			}
			
			System.out.println(sb.toString());
		}
		
		if (!dryRun)
		{
			
//			try
			{
				ECOAS.main(options);
			}
//			catch (AssemblerException e)
			{
//				System.err.println("error: assembler failed to process : " + e.getMessage());
//				return 1;
			}
		}
      
		return 0;
	}
	
	static int execAssembler6309(String infile, String outfile) throws Exception
	{
		// Invoke the assembler
		String command = "lwasm " + infile + " --6309 --format=obj --pragma=pcaspcr,condundefzero,undefextern,dollarnotlocal,noforwardrefmax,export --output=" + outfile;
		if (!quietMode)
		{
			System.out.println(command);
		}
		if (!dryRun)
		{
			return Execute(command);
		}

		return 0;
	}

	static int execAssemblerPPC(String infile, String outfile, String target) throws Exception
	{
		String command;
		
		// Invoke the assembler
		if (target.equals("ppc"))
		{
			command = "as -g -arch ppc " + infile + " -o " + outfile;
		}
		else
		{
			command = "as -g -mppc -mregnames " + infile + " -o " + outfile;
		}
		if (!quietMode)
		{
			System.out.println(command);
		}
		if (!dryRun)
		{
			return Execute(command);
		}
      
		return 0;
	}
   
	static int execLinker6309(String infile, String outfile, String preOpts, String postOpts) throws Exception
	{
		// Invoke the assembler
		if (preOpts == null)
		{
			preOpts = "";
		}
		if (postOpts == null)
		{
			postOpts = "";
		}
		
		String command = "lwlink " + preOpts + " " + infile + " --output=" + outfile + " " + postOpts;
		if (!quietMode)
		{
			System.out.println(command);
		}
		if (!dryRun)
		{
			return Execute(command);
		}

		return 0;
	}

	static int execLinkerPPC(String infile, String outfile, String preOpts, String postOpts, String target) throws Exception
	{
		// Invoke the assembler
		if (preOpts == null)
		{
			preOpts = "";
		}
		if (postOpts == null)
		{
			postOpts = "";
		}
		
		String command = "ld -arch ppc " + preOpts + " " + infile + " -o " + outfile + " " + postOpts;
		if (!quietMode)
		{
			System.out.println(command);
		}
		if (!dryRun)
		{
			return Execute(command);
		}
      
		return 0;
	}
   
	public static int process(String[] args) throws Exception
	{
		System.out.println("RELIC compiler v" + version + " - (C) 2009 Boisy G. Pitre");

		for (int a = 0; a < args.length; a++)
		{
			if (args[a].charAt(0) != '-')
			{
				String infile = args[a];
				
				// Invoke the front-end
				StringToolbox tb = new StringToolbox();
				
				String sourceFile = tb.fileNameWithPath(infile);
				String destFile = tb.fileName(infile);
				String beInFile = null;
				
				if (execFrontEnd(sourceFile + ".rb", destFile + ".rx") == 0)
				{
					if (exitAfterFrontEnd == false)
					{
						if (runRELAXOptimizer == true)
						{
							if (execOptimizer(destFile + ".rx", destFile + ".ro") != 0)
							{
								return 0;
							}
							beInFile = destFile + ".ro";
						}
						else
						{
							beInFile = destFile + ".rx";
						}

						if (exitAfterRELAXOptimizer == false)
						{
                     if (target.equals("6309"))
                     {
                        if (execBackEnd6309(beInFile, destFile + ".as") == 0)
                        {
                           if (exitAfterBackEnd == false)
                           {
                           		if (newAssembler == true)
                           		{
                           			execECOASAssembler(
                           					"-aLevel=1",
                           					"-aDRAGON=0",
                           					"-aH6309=0",
                           					pathToLibrary(libraryPath, "rbstart.a"),
                           					destFile + ".as",
                           					pathToLibrary(libraryPath, "rblib.zip"),
                           					pathToLibrary(libraryPath, "alib.zip"),
                           					pathToLibrary(libraryPath, "sys.zip"),
                           					"-o=" + destFile,
                           					assemblerOptions
                           			);
	                           	}
	                           	else
	                           	{
	                           		
		                            if (execAssembler6309(destFile + ".as", destFile + ".a") == 0)
		                            {
                                 		if (exitAfterAssembler == false)
		                                {
		                                    if (execLinker6309(destFile + ".a", destFile, pathToLibrary(libraryPath, "rbstart.a"), "--library-path=" + pathToLibrary(libraryPath, "") + " --library=rb --library=alib --library=nos96309l2") == 0)
        		                            {
                		                    }
                		                }
                		             }    
                                 }
                              
                           }
                        }
                     }
                     else if (target.equals("ppc") || target.equals("cell"))
                     {
						 if (execBackEndPPC(beInFile, destFile + ".s", target) == 0)
						 {
							 if (exitAfterBackEnd == false)
							 {
								 if (execAssemblerPPC(destFile + ".s", destFile + ".o", target) == 0)
								 {
									 if (exitAfterAssembler == false)
									 {
										 if (execLinkerPPC(
											destFile + ".o",
											destFile,
											pathToLibrary(libraryPath, "rbstart.o"),
											" -L" + libraryPath.get(0) + " -lrelic",
											target) == 0)
										 {
										 }
									 }
								 }
							 }
						 }
					 }
                     else
                     {
                        System.err.println("Unknown target: " + target);
							}
						}
					}
				}
			}
		}

		return 0;
    }

	public static String pathToLibrary(ArrayList<String> paths, String library)
	{
		Iterator<String> j = paths.iterator();
		
		while (j.hasNext())
		{
			String full = j.next() + "/" + library;
			if (new File(full).exists())
			{
				return full;
			}
		}
		
		return "./" + library;
	}

	public static int Execute(String command) throws IOException
	{
		Runtime runtime;
		Process proc;
		try
		{
		runtime = Runtime.getRuntime();
		proc = runtime.exec(command);
		} catch (IOException e)
		{
			System.err.println(e);
			return 1;	
		}

		// put a BufferedReader on the output
		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(proc.getErrorStream());
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

		// read the output
		String line;
		while ((line = bufferedreader.readLine()) != null)
		{
			System.out.println(line);
		}
	
		// check for failure

		try
		{
			if (proc.waitFor() != 0)
			{
				return proc.exitValue();
			}
		}
		catch (InterruptedException e)
		{
			System.err.println(e);
			return 1;
		}

		return 0;
	}


	public static void showHelp()
	{
		System.err.println("RELIC - Retargetable Embedded Language Independent Compiler v" + version);
		System.err.println("usage: java org.relic.Compiler {<opts>} <sourcefile>");
		System.err.println("options:");
		System.err.println("     -d          dry run (don't actually execute phases)");
		System.err.println("     -n          run the new Java assembler");
		System.err.println("     -q          quiet mode (don't show phases as they execute)");
		System.err.println("     -sr         skip RELAX optimizer");
		System.err.println("     -t=<target> set target (ppc, cell or 6309)");
		System.err.println("     -xf         exit after front-end");
		System.err.println("     -xr         exit after RELAX optimizer");
		System.err.println("     -xb         exit after back-end");
		System.err.println("     -xa         exit after assembler");
		System.err.println("     -L=<path>   path to libraries");
		System.err.println("     -O=<path>   output directory");
	}	
}
