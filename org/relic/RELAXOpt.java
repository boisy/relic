package org.relic;

import org.antlr.runtime.*;
import org.relic.util.*;
import java.io.*;
import java.util.*;

public class RELAXOpt extends RelicObject
{
	static int errorCount = 0, warningCount = 0;
	static SymbolTable symbolTable;
	static QuadTable quadTable;
	static 	String file = null;

	// Capture table sizes for reporting later
	static int numSymbols = 0;
	static int numInstructions = 0;
	static int reductions = 0;

	static int optNumInstructions = 0;
	static int optNumSymbols = 0;

	static Boolean report = true;

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

	public static void showHelp()
	{
		System.err.println("rxopt v" + version);
		System.err.println("usage: java org.relic.rxopt infile outfile");
	}
	
	public static int process(String infile, String outfile) throws Exception
	{
		ObjectInputStream s;

		FileInputStream in = new FileInputStream(infile);
		s = new ObjectInputStream(in);

		symbolTable = (SymbolTable)s.readObject();
		quadTable = (QuadTable)s.readObject();

		// Capture table sizes for reporting later
		numSymbols = symbolTable.size();
		numInstructions = quadTable.size();
		
		// Now that we have the symbol and quad tables, perform the optimization processes
		reductions = consecutive_ret_collapsing();
		if (report == true)
		{
			System.out.printf("A total of %d ret instructions have been reduced from consecutive_ret_collapsing()\n", reductions);
		}

		reductions = strip_constant_negation();
		if (report == true)
		{
			System.out.printf("A total of %d negates have been reduced from strip_constant_negation()\n", reductions);
		}

		reductions = single_reference_copy_symbol_stripping();
		if (report == true)
		{
			System.out.printf("A total of %d unused symbols have been reduced from single_reference_copy_symbol_stripping()\n", reductions);
		}
	
		reductions = unused_symbol_stripping();
		if (report == true)
		{
			System.out.printf("A total of %d unused symbols have been reduced from unused_symbol_stripping()\n", reductions);
		}
	
		reductions = constant_collapsing();
		if (report == true)
		{
			System.out.printf("A total of %d duplicate symbols have been collapsed from constant_collapsing()\n", reductions);
		}

		reductions = peep_math_copy_symbol_stripping();
		if (report == true)
		{
			System.out.printf("A total of %d symbols/cp instructions have been collapsed from peep_math_copy_symbol_stripping()\n", reductions);
		}

		int optNumSymbols = symbolTable.size();
		int optNumInstructions = quadTable.size();
	
		if (outfile == null)
		{
			symbolTable.show();
			quadTable.show();
		}
		else
		{
			save(outfile);
		}
		
		if (report == true)
		{
			System.out.printf("Pre-opt symbols       : %5d   Post-opt symbols      : %5d\n", numSymbols, optNumSymbols);
			float f = (1 - ((float)optNumSymbols / (float)numSymbols)) * 100;
			System.out.printf("Symbol reduction      : %5.1f%%\n", f);
			System.out.printf("Pre-opt instructions  : %5d   Post-opt instructions : %5d\n", numInstructions, optNumInstructions);
			f = (1 - ((float)optNumInstructions / (float)numInstructions)) * 100;
			System.out.printf("Instruction reduction : %5.1f%%\n", f);
		}
		
		return 0;
	}
	
	/* this optimization routine strips redundant ret opcodes
	 *
	 * i.e.
	 *
	 * ret
	 * ret
	 *
	 * becomes:
	 *
	 * ret
	 */
	static int consecutive_ret_collapsing()
	{
		int reductions = 0;
		
		for (int i = 0; i < quadTable.size() - 1; i++)
		{
			Quad q1 = (Quad)quadTable.get(i);
			Quad q2 = (Quad)quadTable.get(i + 1);
		
			if (q1.opcode.equals("ret") && q2.opcode.equals("ret"))
			{
				quadTable.remove(i--);
				reductions++;
			}
		}

		return reductions;
	}

	/* this optimization routine removes programmatic negation of constants
	 *
	 * i.e.
	 *
	 * Symbol:  _10 = 3
	 *
	 * Code:    neg _10
	 *
	 * If no other access to _10 occurs before the negation, we can remove the
	 * neg opcode and negate the constant in the symbol table.
	 */
	static int strip_constant_negation()
	{
		int reductions = 0;
		
		return reductions;
	}

	/* this optimization routine removes one-time use symbols
	 */
	static int single_reference_copy_symbol_stripping()
	{
		int reductions = 0;
		
		int ip;
		
		/* 1. For every "cp" in the code...
		 */
		for (int i = 0; i < quadTable.size(); i++)
		{
			Quad q = (Quad)quadTable.get(i);
			
			if (q.opcode.equals("cp"))
			{
				Symbol s1 = (Symbol)symbolTable.get(q.src1);
				
				/* We cannot optimize this variable if the source is a global or a
				 * parameter, since we do not know if it will be changed externally
				 * from this program.
				 */
				
				if (s1.global != false || s1.param != 0)
				{
					break;
				}
				
				Symbol s = (Symbol)symbolTable.get(q.dst);

				/* ...that the destination as SCOPE >=0 AND GLOBAL = 0 ... */
				if (s.global == false && s.param == 0)
//				if (g->s.getScope(s) >= 0 && g->s.getGlobal(s) == 0)
				{
					int canOptimize = 1;
					
					/* ... if that symbol is the destination in any other instruction ... */
					if (quadTable.symbolIsDestinationOfAnyOtherInstruction(s, i))
					{
						canOptimize = 0;
						break;
					}
					
					if (canOptimize == 1) // && ip == g->getNextQuad())
					{
						/* If here, we can optimize the symbol 's' and the instruction 'i'
						 * out of the code.
						 */
//						System.out.printf("optimization candidate: symbol %s, instruction %d\n", s.name, i);

//						Symbol newsrc = (Symbol)symbolTable.get(q.src1);
						
						/* 1. Remove the instruction from the code.
						 */
						quadTable.remove(i);
						  
						/* 2. Replace all references in the code of src1/src2 with src1 of the cp
						 */
						for (ip = 0; ip < quadTable.size(); ip++)
						{
							Quad qq = (Quad)quadTable.get(ip);
							Boolean dirty = false;
							
							if (s.name.equals(qq.src1))
							{
								qq.src1 = s1.name;
								dirty = true;
							}
							if (s.name.equals(qq.src2))
							{
								qq.src2 = s1.name;
								dirty = true;
							}

							if (dirty == true)
							{
								quadTable.set(ip, qq);
							}
						}
						  
						/* 3. Remove the temporary from the symbol table.
						 */
						symbolTable.remove(s.name);
						reductions++;
						
						i -= 1;
					}
				}
			}
		}

		return reductions;
	}

	/* This optimization routine removes unused symbols from the symbol table
	 *
	 * Cases which fall under this optimization catagory are declared variables
	 * that are not used in a program.
	 */
	static int unused_symbol_stripping()
	{
		int reductions = 0;
		
		/* 1. For every symbol in the symbol table...
		 */
		Iterator it = symbolTable.entrySet().iterator();

		while (it.hasNext())
		{
			int used = 0;

			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			
			/* If the type is a label or procedure, just skip it */
			if (s.type.equals("PROCEDURE") || s.type.equals("LABEL"))
			{
				continue;
			}
			
			/* ... then for every instruction in code ... */
			for (int ip = 0; ip < quadTable.size(); ip++)
			{
				Quad q = (Quad)quadTable.get(ip);
				
				/* ... if the current symbol is used in src1, src2 or dst ... */
				if (s.name.equals(q.src1) || s.name.equals(q.src2) || s.name.equals(q.dst))
				{
						/* ... then break out of the inner loop and go to the next symbol */
						used = 1;
				}
			}

			if (used == 0)
			{
				/* ... else remove the symbol from the symbol table ... */
				symbolTable.remove(s);

				reductions++;
			}
		}

		return reductions;
	}


	/* This optimization routine collapses immutable initialized symbols of the same value
	 */
	static int constant_collapsing()
	{
		int reductions = 0;

		/* 1. For every symbol in the symbol table... */
		Iterator it = symbolTable.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();

			/* If the symbol is the destination of any instruction, skip it */
			if (quadTable.symbolIsDestinationOfAnyInstruction(s))
			{
				continue;
			}
					
			/* If the type is a label or procedure, skip it */
			if (s.type.equals("PROCEDURE") || s.type.equals("LABEL"))
			{
				continue;
			}
			
			/* If the symbol is uninitialized, a global or a parameter, skip it */
			if (s.value == null || s.param > 0 || s.global == true)
			{
				continue;
			}
			
			/* Look for a second symbol of the same value */
			// Get past same symbol in symbolTable2
			Symbol s2;
			
			Iterator itplaceholder = it;

			while (it.hasNext())
			{
				Map.Entry e3 = (Map.Entry)it.next();
				s2 = (Symbol)e3.getValue();

				/* If the type doesn't match the type of the candidate symbol, skip it */
				if (!s2.type.equals(s.type))
				{
					continue;
				}

				/* If the symbol is the destination of any instruction, skip it */
				if (quadTable.symbolIsDestinationOfAnyInstruction(s2))
				{
					continue;
				}
					
				/* If the symbol is uninitialized, a global or a parameter, skip it */
				if (s2.value == null || s2.param > 0 || s2.global == true)
				{
					continue;
				}

				/* Continue only if the values are the same */
				if (s2.value.equals(s.value))
				{
//					System.out.printf("Candidate symbols %s and %s are the same\n", s.name, s2.name);

					// Change all references in instructions from s2 to s
					for (int ip = 0; ip < quadTable.size(); ip++)
					{
						Quad q = (Quad)quadTable.get(ip);
						Boolean dirty = false;
						
						if (q.src1 != null && q.src1.equals(s2.name))							
						{
							q.src1 = s.name;
							dirty = true;
						}
						if (q.src2 != null && q.src2.equals(s2.name))							
						{
							q.src2 = s.name;
							dirty = true;
						}
						if (q.dst != null && q.dst.equals(s2.name))							
						{
							q.dst = s.name;
							dirty = true;
						}

						if (dirty == true)
						{
							quadTable.set(ip, q);
						}
					}
					

					// Remove the symbol s2
					it.remove();

					reductions++;
				}
			}

			// Start over from the top
			it = symbolTable.entrySet().iterator();
			
			// And iterate to symbol AFTER the one we just evaluated
			while (it.hasNext())
			{
				Map.Entry e3 = (Map.Entry)it.next();
				s2 = (Symbol)e3.getValue();
				if (s2.name.equals(s.name))
				{
					break;
				}
			}
		}

		return reductions;
	}

	public static void save(String file)
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
			s.writeObject(symbolTable);
			s.writeObject(quadTable);
		}
		catch (IOException e)
		{
			System.err.println("Failed to write symbolTable or quadTable");
			return;
		}
	}

	/* this peephole optimization routine removes a subsequent cp from a math operation
	 * and replaces the destination of the math instruction with the destination of the
	 * cp instruction IF:
	 *    the destination of the math instruction is a temporary
	 */
	static int peep_math_copy_symbol_stripping()
	{
		int reductions = 0;
		
		int ip;
		
		/* 1. For every "+, -, *, or /" in the code...
		 */
//		Quad q = (Quad)quadTable.get(i);
			
		for (int i = 0; i < quadTable.size() - 1; i++)
		{
			Quad q = (Quad)quadTable.get(i);

			if (q.opcode.equals("+") || q.opcode.equals("-") || q.opcode.equals("*") || q.opcode.equals("//"))
			{
				/* if next quad isn't cp, just continue */
				Quad qn = (Quad)quadTable.get(++i);
				if (!qn.opcode.equals("cp"))
				{
					i--;
					continue;
				}
				
				Symbol s = (Symbol)symbolTable.get(q.dst);

				/* ...that the destination as SCOPE >=0 AND GLOBAL = 0 ... and the source1 of the cp is the destination of
				 * the math operation
				 */
				if (s.global == false && s.param == 0 && qn.src1.equals(q.dst))
				{
					int canOptimize = 1;
					
					/* ... if that symbol is the destination in any other instruction ... */
					if (quadTable.symbolIsDestinationOfAnyOtherInstruction(s, i - 1) && qn.label == null)
					{
						canOptimize = 0;
						break;
					}
					
					if (canOptimize == 1) // && ip == g->getNextQuad())
					{
						/* If here, we can optimize the symbol 's' and the instruction 'i'
						 * out of the code.
						 */

						/* 1. Remove the math operation's destination from the symbol table.
						 */
						symbolTable.remove(q.dst);
						reductions++;
						
						/* 2. Replace the reference to the dst of the math operation with the dest of the cp
						 */
						q.dst = qn.dst;

						/* 3. Remove the cp instruction from the code.
						 */
						quadTable.remove(i);
					}
				}
			}
		}

		return reductions;
	}

}
