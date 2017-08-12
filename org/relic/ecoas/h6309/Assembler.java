package org.relic.ecoas.h6309;

import org.relic.ecoas.util.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;

public class Assembler
{
	private LinkedList<Assembly> ll = new LinkedList<Assembly>();
	public SymbolTable symbols;
	private int[] b = null;
	public Boolean verbose = false;
	int errorCount = 0, warningCount = 0, blankLineCount = 0, commentLineCount = 0, lineCount = 0, instructionLineCount = 0;
	int byteCount = 0;
	String outFile = null;
	int moduleHeaderSize;
	int cycles, preReductionCycles, postReductionCycles;
	double power, preReductionPower, postReductionPower;
	double clockPeriod = 1.0 / (28636363.0 / 16.0);
	
	// reduction counters
	int totalReductions;
	int storeLoadInstructionCollapseCount;
	int loadAccumulatorImmediateZeroRegisterInstructionCollapseCount;
	int loadAccumulatorImmediateRegisterInstructionCollapseCount;
	int addSubtractAccumulatorImmediateRegisterInstructionCollapseCount;
	int longBranchReductionCount;


	public Assembler(Boolean nativeMode, ArrayList<String> files) throws IOException
	{
		symbols = new SymbolTable();
		
		for (Iterator<String> i = files.iterator(); i.hasNext(); )
		{
			String file = i.next();
			
			LinkedList<String> l = Unzip.doUnzip(file);
			
			if (l.size() == 0)
			{
				Assembly a = new Assembly(new CPU(nativeMode, clockPeriod), true, file);
				a.read(file);
				add(a);
			}
			else
			{
				for (Iterator<String> ii1 = l.iterator(); ii1.hasNext(); )
				{
					String file1 = ii1.next();
					Assembly a = new Assembly(new CPU(nativeMode, clockPeriod), true, file1);
					a.read(file1);
					add(a);
				}
			}
		}
	}

	public Assembler(Assembly... assemblers)
	{
		symbols = new SymbolTable();
		
		for (Assembly a: assemblers)
		{
			a.symbols.putAll(symbols);
			ll.add(a);
		}
	}

	public void add(Assembly assembler)
	{
		assembler.symbols.putAll(symbols);
		ll.add(assembler);
	}

	public void assemble(String moduleName, int reductionLevel) throws AssemblerException
	{
		assemble(moduleName, null, reductionLevel);
	}
	
	public void assemble(String moduleName, String fileName, int reductionLevel) throws AssemblerException
	{
		// Step 1: assemble all assemblies
		try
		{
			assemble(moduleName, fileName != null);
			preReductionCycles = cycles;
			preReductionPower = power;
		}
		catch (AssemblerException e)
		{
			throw e;
		}

		// Step 2: perform reductions if directed
		if (reductionLevel > 0)
		{
			postReductionCycles = 0;
			postReductionPower = 0;
			
			totalReductions = 0;
			int tempCounter = 0;

			storeLoadInstructionCollapseCount = 0;
			tempCounter = 0;
			do
			{
				tempCounter = storeLoadInstructionCollapse(ll, reductionLevel);
				storeLoadInstructionCollapseCount += tempCounter;
			}
			while (tempCounter > 0);
	
			loadAccumulatorImmediateZeroRegisterInstructionCollapseCount = 0;
			tempCounter = 0;
			do
			{
				tempCounter = loadAccumulatorImmediateZeroRegisterInstructionCollapse(ll, reductionLevel);
				loadAccumulatorImmediateZeroRegisterInstructionCollapseCount += tempCounter;
			}
			while (tempCounter > 0);
	
			loadAccumulatorImmediateRegisterInstructionCollapseCount = 0;
			tempCounter = 0;
			do
			{
				tempCounter = loadAccumulatorImmediateRegisterInstructionCollapse(ll, reductionLevel);
				loadAccumulatorImmediateRegisterInstructionCollapseCount += tempCounter;
			}
			while (tempCounter > 0);
	
			addSubtractAccumulatorImmediateRegisterInstructionCollapseCount = 0;
			tempCounter = 0;
			do
			{
				tempCounter = addSubtractAccumulatorImmediateRegisterInstructionCollapse(ll, reductionLevel);
				addSubtractAccumulatorImmediateRegisterInstructionCollapseCount += tempCounter;
			}
			while (tempCounter > 0);
			
			assemble(moduleName, fileName != null);
			
			longBranchReductionCount = 0;
			tempCounter = 0;
			do
			{
				tempCounter = longBranchReduction(ll, reductionLevel);
				longBranchReductionCount += tempCounter;
				
				if (tempCounter > 0)
				{
					assemble(moduleName, fileName != null);
				}
			}
			while (tempCounter > 0);

			totalReductions =   storeLoadInstructionCollapseCount +
								loadAccumulatorImmediateZeroRegisterInstructionCollapseCount +
								loadAccumulatorImmediateRegisterInstructionCollapseCount +
								addSubtractAccumulatorImmediateRegisterInstructionCollapseCount +
								longBranchReductionCount;
	
			if (totalReductions > 0)
			{
				postReductionCycles = cycles;
				postReductionPower = power;
			}
		}
		else
		{
			postReductionCycles = cycles;
			postReductionPower = power;
			// since we are not reducing long branches, we must go through and mark each long branch
			// that could have been a short branch with a warning
			setLongBranchWarnings(ll);
		}
		
		// Step 3. Show the one-line summary of the assemblies
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();

			System.out.println(as.toString());
		}

		// Step 4: write the output file if desired
		if (fileName != null) // && mainline != null)
		{
			OS9Emitter emitter = new OS9Emitter();
			b = emitter.generateBinary(moduleName, ll);

			try
			{
				FileOutputStream f = new FileOutputStream(fileName);
				
				for (int i = 0; i < b.length; i++)
				{
					f.write(b[i]);
				}
				
				f.close();
			}
			catch (IOException e)
			{
				throw new AssemblerException("error writing to file");
			}
		}
		
		return;
	}

	public void assemble(String moduleName, Boolean expectMainline) throws AssemblerException
	{
		errorCount = 0;
		warningCount = 0;
		instructionLineCount = 0;
		blankLineCount = 0;
		commentLineCount = 0;
		lineCount = 0; 
		byteCount = 0;

		if (moduleName == null)
		{
			moduleHeaderSize = 0;
		}
		else
		{
			moduleHeaderSize = 14 + moduleName.length();
		}
		SymbolTable allGlobals = new SymbolTable();
		
		// Step 1: assemble all assemblies
		Assembly mainline = null;
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();
			as.symbols.clear();					// remove symbols
			as.symbols.putAll(symbols);			// put our symbols in

			// perform the assembly
			as.assemble();
			
			// check for the existence of a mainline ONLY if we are emitting a file
			if (expectMainline == true)
			{
				if (as.sectionTypeLang != 0)
				{
					if (mainline != null)
					{
						throw new AssemblerException("too many mainline psects");
					}

					if (ll.get(0) != as)
					{
						throw new AssemblerException("no mainline psect found -- required for emitting a file");
					}

					mainline = as;
				}
			}
		}
					
		// Step 2: starting with the mainline, follow its external references to determine
		// any assemblies that aren't dependencies of the mainline (or its dependencies).
		// remove unused sections when done.
		int beforeCount = ll.size();
		if (mainline != null)
		{
			mainline.used = true;

			if (verbose == true) System.out.println("Keeping " + mainline);
			findAndKeep(mainline, ll);

			for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
			{
				Assembly as = it.next();
				
				if (as.used == false)
				{
					it.remove();
				}
			}
			int afterCount = ll.size();
			if (verbose == true)
			{
				System.out.printf("Purged %d sections from %d (%d kept)\n", beforeCount - afterCount, beforeCount, afterCount);
			}
		}

		// Step 3: tally up cycles and power
		cycles = 0;
		power = 0.0;
		
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();

			cycles += as.cycles;
			power += as.power;
		}
		
		// Step 5: Count up counts and adjust all code and data labels for all sections, and capture globals into one convenient table
		// Step 5a: Do code and IDpD, and count up various line type counts
		int Code = moduleHeaderSize;
		int IDpD = 0;
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = (Assembly)it.next();
			
			for (Iterator i = as.symbols.entrySet().iterator(); i.hasNext(); )
			{
				Map.Entry e = (Map.Entry)i.next();
				Symbol s = (Symbol)e.getValue();
			
				if (s.type == "code")
				{
					s.value += Code;
				}
				else
				if (s.type == "idpd")
				{
					s.value += IDpD;
				}

				// if this is a global symbol, add it to the globals table
				if (s.global == true) allGlobals.add(s);
			}
			
			IDpD += as.initializedDPData.size();
			Code += as.objectCode.size();
			
			errorCount += as.errorCount;
			warningCount += as.warningCount;
			instructionLineCount += as.instructionLineCount;
			blankLineCount += as.blankLineCount;
			commentLineCount += as.commentLineCount;
			lineCount += as.lineCount; 
			byteCount += as.objectCode.size();
		}

		// Step 5b: Do UDpD
		int UDpD = 0;
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = (Assembly)it.next();
			
			for (Iterator i = as.symbols.entrySet().iterator(); i.hasNext(); )
			{
				Map.Entry e = (Map.Entry)i.next();
				Symbol s = (Symbol)e.getValue();
			
				if (s.type == "udpd")
				{
					s.value += IDpD + UDpD;
				}

				// if this is a global symbol, add it to the globals table
				if (s.global == true) allGlobals.add(s);
			}
			
			UDpD += as.uninitializedDPDataCount;
		}

		// Step 5c: IDat
		int IDat = 0;
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = (Assembly)it.next();
			
			for (Iterator i = as.symbols.entrySet().iterator(); i.hasNext(); )
			{
				Map.Entry e = (Map.Entry)i.next();
				Symbol s = (Symbol)e.getValue();
			
				if (s.type == "idat")
				{
					s.value += IDpD + UDpD + IDat;
				}

				// if this is a global symbol, add it to the globals table
				if (s.global == true) allGlobals.add(s);
			}
			
			IDat += as.initializedData.size();
		}

		// Step 5d: UDat
		int UDat = 0;
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = (Assembly)it.next();
			
			for (Iterator i = as.symbols.entrySet().iterator(); i.hasNext(); )
			{
				Map.Entry e = (Map.Entry)i.next();
				Symbol s = (Symbol)e.getValue();
			
				if (s.type == "udat")
				{
					s.value += IDpD + UDpD + IDat + UDat;
				}

				// if this is a global symbol, add it to the globals table
				if (s.global == true) allGlobals.add(s);
			}
			
			UDat += as.uninitializedDataCount;
		}

		// Step 6: now that we have enough information, create the following global symbols:
		// - btext  (code)  (always zero?)
		// - end    (udat)  end of bss address
		// - etext  (code)  offset  in module to end of code area and start of DP-data count word
		// - edata  (idat)  initialized to one byte higher than the uninitialized data.
		// - dpsiz  (udpd)  direct page data size
		int dpsiz = IDpD + UDpD;
		int end = dpsiz + UDat + IDat;
		int edata = dpsiz + IDat;
		int btext = 0;
		int etext = Code; // + moduleHeaderSize;
		
		allGlobals.add(new Symbol("dpsiz", dpsiz, "cnst",     true));
		allGlobals.add(new Symbol("end",   end,   "cnst",     true));
		allGlobals.add(new Symbol("edata", edata, "cnst",     true));
		allGlobals.add(new Symbol("btext", btext, "code",     true));
		allGlobals.add(new Symbol("etext", etext, "code",     true));
		
		// Step 7: walk through and patch up references for each assembly
		int codeOffset = moduleHeaderSize;
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();

			as.symbols.putAll(allGlobals);
			
			patchUpReferences(as, allGlobals, Code, IDat, UDat, IDpD, UDpD, codeOffset);
			codeOffset += as.objectCode.size();
		}

		return;
	}

	// this method goes through the section and patches up global references in both the
	// line's bytes and the aggregate code object in the section object.
	void patchUpReferences(Assembly as, SymbolTable symbols, int Code, int IDat, int UDat, int IDpD, int UDpD, int codeOffset)
	{
		ExpressionParser p = new ExpressionParser(symbols);
		
		// for each reference, set its branch type to what was passed
		Iterator<AsmLine> it1 = as.iterator();
		
		while (it1.hasNext())
		{
			// for each line...
			AsmLine line = it1.next();

			for (Iterator i = line.references.iterator(); i.hasNext(); )
			{
				Reference r = (Reference)i.next(); 
				
				if (r.external == true)
				{
					// look up external reference in symbol table
					Symbol s = symbols.symbolForName(r.name);
					if (s == null)
					{
						line.error = "unresolved reference to " + r.name;
						errorCount++;
						if (line.warning != null)
						{
							line.warning = null;
							as.warningCount--;
							warningCount--;
						}
					}
					else
					{
						int result = 0;
						try
						{
							result = p.evaluate(line.expression, null);
						}
						catch (Exception e)
						{
						}
						int locationToPatch = r.offset - line.offset;
						int locationInObjectToPatch = r.offset - as.currentSection.counter;
						
						if (s.type.equals("code"))
						{
							// if the symbol is in code, adjust its value according to our reference type
							if (r.branchType == Reference.BranchType.absolute)
							{
							}
							else
							{
								// pcr relative
								result -= (line.offset + line.bytes.size() + codeOffset);
							}
						}
						
						// patch the reference with the value of the symbol
						switch (r.size)
						{
							case 1:
								as.objectCode.set(locationInObjectToPatch, (result >> 0) & 0xFF);
								line.bytes.set(locationToPatch, (result >> 0) & 0xFF);
								break;
							case 2:
								as.objectCode.set(locationInObjectToPatch++, (result >> 8) & 0xFF);
								line.bytes.set(locationToPatch++, (result >> 8) & 0xFF);
								as.objectCode.set(locationInObjectToPatch++, (result >> 0) & 0xFF);
								line.bytes.set(locationToPatch, (result >> 0) & 0xFF);
								break;
							case 4:
								as.objectCode.set(locationInObjectToPatch++, (result >> 24) & 0xFF);
								line.bytes.set(locationToPatch++, (result >> 24) & 0xFF);
								as.objectCode.set(locationInObjectToPatch++, (result >> 16) & 0xFF);
								line.bytes.set(locationToPatch++, (result >> 16) & 0xFF);
								as.objectCode.set(locationInObjectToPatch++, (result >> 8) & 0xFF);
								line.bytes.set(locationToPatch++, (result >>  8) & 0xFF);
								as.objectCode.set(locationInObjectToPatch++, (result >> 0) & 0xFF);
								line.bytes.set(locationToPatch, (result >>  0) & 0xFF);
								break;
						}
					}
				}
				else
				{
					p.setSymbolTable(as.symbols);
					int result = 0;
					try
					{
						result = p.evaluate(line.expression, null);
					}
					catch (Exception e)
					{
					}
					int locationToPatch = r.offset - line.offset;
					int locationInObjectToPatch = r.offset - as.currentSection.counter;
					
					// local reference.
					switch (r.size)
					{
						case 1:
							as.objectCode.set(locationInObjectToPatch, (result >> 0) & 0xFF);
							line.bytes.set(locationToPatch, (result >> 0) & 0xFF);
							break;
						case 2:
							as.objectCode.set(locationInObjectToPatch++, (result >> 8) & 0xFF);
							line.bytes.set(locationToPatch++, (result >> 8) & 0xFF);
							as.objectCode.set(locationInObjectToPatch++, (result >> 0) & 0xFF);
							line.bytes.set(locationToPatch, (result >> 0) & 0xFF);
							break;
						case 4:
							as.objectCode.set(locationInObjectToPatch++, (result >> 24) & 0xFF);
							line.bytes.set(locationToPatch++, (result >> 24) & 0xFF);
							as.objectCode.set(locationInObjectToPatch++, (result >> 16) & 0xFF);
							line.bytes.set(locationToPatch++, (result >> 16) & 0xFF);
							as.objectCode.set(locationInObjectToPatch++, (result >> 8) & 0xFF);
							line.bytes.set(locationToPatch++, (result >>  8) & 0xFF);
							as.objectCode.set(locationInObjectToPatch++, (result >> 0) & 0xFF);
							line.bytes.set(locationToPatch, (result >>  0) & 0xFF);
							break;
					}
				}
			}
		}
	}

	// starting with the mainline, go through line's each external reference
	// and mark the section that holds it as used
	void findAndKeep(Assembly as, LinkedList<Assembly> ll)
	{
		for (Iterator<AsmLine> h = as.iterator(); h.hasNext(); )
		{
			AsmLine l = h.next();

			for (Iterator<Reference> i = l.references.iterator(); i.hasNext(); )
			{
				Reference r = (Reference)i.next();
	
				for (Iterator<Assembly> j = ll.iterator(); j.hasNext(); )
				{
					Assembly a = j.next();
					Symbol s = a.symbols.symbolForName(r.name);
					if (a != as && a.used == false && r.external == true && s != null && s.global == true)
					{
						a.used = true;
						if (verbose == true)
							System.out.println("Keeping " + a);
						findAndKeep(a, ll);
					}
				}
			}
		}
	}

	public void showSource()
	{
		int codeCounter = moduleHeaderSize;
		
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();
			
			as.show(codeCounter);
			codeCounter += as.objectCode.size();
		}
	}

	public void showErrors()
	{
		int codeCounter = moduleHeaderSize;
		
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();
			
			as.showErrors(codeCounter);
			codeCounter += as.objectCode.size();
		}
	}

	public void showWarnings()
	{
		int codeCounter = moduleHeaderSize;
		
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();
			
			as.showWarnings(codeCounter);
			codeCounter += as.objectCode.size();
		}
	}

	public void showSymbols()
	{
		for (Iterator<Assembly> it = ll.iterator(); it.hasNext(); )
		{
			Assembly as = it.next();
			
			as.symbols.show();
		}
	}

	public void showSummary()
	{
		double preReductionNanojoules = preReductionPower * 1000000000;
		double postReductionNanojoules = postReductionPower * 1000000000;

		System.out.println();
		System.out.println("Assembler Summary:");
		System.out.println(" - " + errorCount + " errors, " + warningCount + " warnings");
		System.out.println(" - " + lineCount + " lines (" + instructionLineCount + " instruction, " + (lineCount - (instructionLineCount + blankLineCount + commentLineCount)) + " pseudo, " + blankLineCount + " blank, " + commentLineCount + " comment)");
		if (totalReductions > 0)
		{
			System.out.printf(" - %d pre-reduction cycles, %d post-reduction cycles, %.02f%% cycle reduction\n", preReductionCycles, postReductionCycles, (1.0 - ((float)postReductionCycles / (float)preReductionCycles)) * 100);
			System.out.printf(" - %.02f pre-reduction nanojoules, %.02f post-reduction nanojoules, %.02f%% nanojoule reduction\n", preReductionNanojoules, postReductionNanojoules, (1.0 - ((float)postReductionNanojoules / (float)preReductionNanojoules)) * 100);
			System.out.println("     " + storeLoadInstructionCollapseCount + " store/load instruction reductions");
			System.out.println("     " + loadAccumulatorImmediateZeroRegisterInstructionCollapseCount + " load accumulator immediate zero register instruction reductions");
			System.out.println("     " + loadAccumulatorImmediateRegisterInstructionCollapseCount + " load accumulator immediate register instruction reductions");
			System.out.println("     " + addSubtractAccumulatorImmediateRegisterInstructionCollapseCount + " add/subtract accumulator immediate register instruction reductions");
			System.out.println("     " + longBranchReductionCount + " long branch reductions");
		}
		else
		{
			System.out.println(" - " + postReductionCycles + " cycles");
			System.out.println(" - " + (float)postReductionNanojoules + " nanojoules");
		}

		if (instructionLineCount > 0)
			System.out.println(" - " + instructionLineCount + " instructions, " + (float)postReductionCycles / (float)instructionLineCount + " cycles/instruction (average)");

		System.out.println(String.format(" - %d code bytes", byteCount));

		if (outFile == null)
		{
			System.out.println(" - No output file");
		}
		else
		{
			System.out.println(" - Output file: " + outFile);
		}
	}
	
	// Reduction methods
	// Each reduction method returns a count which is the number of reductions that it
	// has performed during its pass through the code.
	
	// this reduction method looks for ld[abefdw] #0 and replaces it with the equivalent clr[abefdw]
	// differences in the handling of the carry bit between these two instructions forces us to take
	// subsequent instructions under consideration.

	// NOTE: reductionLevel > 1 means ignore the state of the carry when checking if this reduction should be applied
	public int loadAccumulatorImmediateZeroRegisterInstructionCollapse(LinkedList<Assembly> ll, int reductionLevel)
	{
		int reductionCounter = 0;
		
		for (Iterator<Assembly> i = ll.iterator(); i.hasNext(); )
		{
			Assembly as = i.next();
			
			for (int h = 0; h < as.size(); h++)
			{
				AsmLine l1 = as.get(h);
				if (l1.opcode == "") continue;
				
				// look for ld[abefdw] #0
				if (l1.opcode.substring(0,2).equals("ld") && l1.operand.substring(0,1).equals("#"))
				{
					StringBuffer error = new StringBuffer();
					try
					{
						int value = as.evaluate(l1, l1.expression);
						if (error.toString().equals("") && value == 0)
						{
							String reg = l1.opcode.substring(2);
							
							if (reg.equals("a") ||
								reg.equals("b") ||
								reg.equals("e") ||
								reg.equals("f") ||
								reg.equals("d") ||
								reg.equals("w")
							)
							{
								Boolean applyTheReduction = false;
								
								// replacing ld? with clr? means that the carry bit will always be cleared
								// we can only apply this reduction if we find a subsequent instruction that affects the carry BEFORE any instruction reads the carry
								// this loop goes through subsequent lines searching for instructions that will either read the carry bit (which invalidates the reduction)
								// or sets the carry bit (which affirms the reduction, thus we apply it)
								for (int j = h + 1; j < as.size(); j++)
								{
									// l2 holds the subsequent line
									AsmLine l2 = as.get(j);
									
									// if we encounter a blank or comment line, continue on...
									if (l2.isBlankLine() == true || l2.isCommentLine() == true || l2.opcode.equals("")) continue;
		
									// if we encounter a pseudo instruction, abandon since we don't know how it will affect the subsequent code
									
									String instructionClass = l2.instruction.getClass().getName();
									instructionClass = instructionClass.substring(instructionClass.lastIndexOf('.') + 1);
									if (instructionClass.equals("CPU$H6309Instruction") == false)
									{
										// this is probably a pseudo instruction; abort the reduction attempt
										break;
									}
		
									// if the instruction can alter flow (branch, swi, etc), we don't attempt to follow
									CPU.H6309Instruction ins = (CPU.H6309Instruction)l2.instruction;
									if (ins.canAlterFlow == true)
									{
										// this instruction is a control altering instruction (branch, etc); abort the reduction attempt
										break;
									}
									
									if (reductionLevel == 1 && ins.read != null && ins.read.contains(CPU.CCF.C))
									{
										// this instruction may read the carry bit; abort the reduction attempt 
										break;
									}
		
									if (reductionLevel > 1 || ins.set != null && ins.set.contains(CPU.CCF.C))
									{
										// this instruction alters the carry bit; allow the reduction to proceed
										applyTheReduction = true;
										break;
									}
								}
								
								if (applyTheReduction == true)
								{
									l1.replaceLine(l1.label + " " + "clr" + reg + l1.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
									as.loadAccumulatorImmediateZeroRegisterInstructionCollapseCount++;
									reductionCounter++;
								}
							}
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		
		return reductionCounter;
	}
	
	public int storeLoadInstructionCollapse(LinkedList<Assembly> ll, int reductionLevel)
	{
		int reductionCounter = 0;
		
		for (Iterator<Assembly> i = ll.iterator(); i.hasNext(); )
		{
			Assembly as = i.next();
			
			for (int h = 0; h < as.size(); h++)
			{
				AsmLine l1 = as.get(h);
				if (l1.opcode == "") continue;
				
				// look for st[abefdw]
				if (l1.opcode.substring(0,2).equals("st"))
				{
					StringBuffer error = new StringBuffer();
					try
					{
						String reg = l1.opcode.substring(2);
						
						if (reg.equals("a") ||
							reg.equals("b") ||
							reg.equals("e") ||
							reg.equals("f") ||
							reg.equals("d") ||
							reg.equals("w") ||
							reg.equals("x") ||
							reg.equals("y") ||
							reg.equals("u") ||
							reg.equals("s") ||
							reg.equals("q")
						)
						{
							// replacing ld? with clr? means that the carry bit will always be cleared
							// we can only apply this reduction if we find a subsequent instruction that affects the carry BEFORE any instruction reads the carry
							// this loop goes through subsequent lines searching for instructions that will either read the carry bit (which invalidates the reduction)
							// or sets the carry bit (which affirms the reduction, thus we apply it)
							for (int j = h + 1; j < as.size(); j++)
							{
								// l2 holds the subsequent line
								AsmLine l2 = as.get(j);
								
								// if we encounter a blank or comment line, continue on...
								if (l2.isBlankLine() == true || l2.isCommentLine() == true || l2.opcode.equals("")) continue;
	
								// if we encounter a pseudo instruction, abandon since we don't know how it will affect the subsequent code
								
								String instructionClass = l2.instruction.getClass().getName();
								instructionClass = instructionClass.substring(instructionClass.lastIndexOf('.') + 1);
								if (instructionClass.equals("CPU$H6309Instruction") == false)
								{
									// this is probably a pseudo instruction; abort the reduction attempt
									break;
								}
	
								// at this point this appears to be a real CPU instruction
								// if the instruction has a label, it is potentially a destination instruction, so abandon the reduction attempt
								if (l2.label.equals("") == false)
								{
									break;
								}

								// is this a store instruction of the same register to the same location as l1??
								if (l2.opcode.substring(0,2).equals("ld"))
								{
									String reg2 = l2.opcode.substring(2);
									
									if (reg2.equals(reg) && l2.operand.equals(l1.operand))
									{
										// we have a match... reduce!
										as.remove(l2);
										as.add(new AsmLine("* ECOAS REDUCTION *", null));
										as.storeLoadInstructionCollapseCount++;
										reductionCounter++;
									}
								}
								
								break;
							}								
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		
		return reductionCounter;
	}
	
	public int loadAccumulatorImmediateRegisterInstructionCollapse(LinkedList<Assembly> ll, int reductionLevel)
	{
		int reductionCounter = 0;
		
		for (Iterator<Assembly> i = ll.iterator(); i.hasNext(); )
		{
			Assembly as = i.next();
			
			for (int h = 0; h < as.size(); h++)
			{
				AsmLine l1 = as.get(h);
				
				// look for ld[abef] # on first line
				if (h < as.size() && l1.opcode.length() > 2 && l1.opcode.substring(0,2).equals("ld") && l1.operand.charAt(0) == '#')
				{
					// look for ldb # on next available instruction line (no intervening label!!)
					for (int j = h + 1; j < as.size(); j++)
					{	
						AsmLine l2 = as.get(j);
						if (l2.isBlankLine() || l2.isCommentLine())
						{
							continue;
						}
						
						if (l2.label.equals("") && l2.opcode.length() > 2 && l2.opcode.substring(0,2).equals("ld") && l2.operand.charAt(0) == '#')
						{
							String reg1 = l1.opcode.substring(2);
							String reg2 = l2.opcode.substring(2);
							String op1 = l1.operand.substring(1);
							String op2 = l2.operand.substring(1);
							
							if (reg1.equals("a") && reg2.equals("b"))
							{
								l1.replaceLine(l1.label + " ldd " + "#(" + op1 + ")*256+(" + op2 + ") " + l1.comment + " " + l2.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
								// remove j
								as.remove(j);
								as.loadAccumulatorImmediateRegisterInstructionCollapseCount++;
								reductionCounter++;
							}
							else if (reg1.equals("b") && reg2.equals("a"))
							{
								l1.replaceLine(l1.label + " ldd " + "#(" + op2 + ")*256+(" + op1 + ") " + l1.comment + " " + l2.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
								// remove j
								as.remove(j);
								reductionCounter++;
							}
							else if (reg1.equals("e") && reg2.equals("f"))
							{
								l1.replaceLine(l1.label + " ldw " + "#(" + op1 + ")*256+(" + op2 + ") " + l1.comment + " " + l2.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
								// remove j
								as.remove(j);
								as.loadAccumulatorImmediateRegisterInstructionCollapseCount++;
								reductionCounter++;
							}
							else if (reg1.equals("f") && reg2.equals("e"))
							{
								l1.replaceLine(l1.label + " ldw " + "#(" + op2 + ")*256+(" + op1 + ") " + l1.comment + " " + l2.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
								// remove j
								as.remove(j);
								as.loadAccumulatorImmediateRegisterInstructionCollapseCount++;
								reductionCounter++;
							}
							else if (reg1.equals("d") && reg2.equals("w"))
							{
								l1.replaceLine(l1.label + " ldq " + "#(" + op1 + ")*65536+(" + op2 + ") " + l1.comment + " " + l2.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
								// remove j
								as.remove(j);
								as.loadAccumulatorImmediateRegisterInstructionCollapseCount++;
								reductionCounter++;
							}
							else if (reg1.equals("w") && reg2.equals("d"))
							{
								l1.replaceLine(l1.label + " ldq " + "#(" + op2 + ")*65536+(" + op1 + ") " + l1.comment + " " + l2.comment  + " * ECOAS REDUCTION *", CPU.instructionTable);
								// remove j
								as.remove(j);
								as.loadAccumulatorImmediateRegisterInstructionCollapseCount++;
								reductionCounter++;
							}
							
							break;
						}
						else
						{
							break;
						}
					}
				}					
			}
		}
		
		return reductionCounter;
	}

	public int addSubtractAccumulatorImmediateRegisterInstructionCollapse(LinkedList<Assembly> ll, int reductionLevel)
	{
		int reductionCounter = 0;
		
		for (Iterator<Assembly> i = ll.iterator(); i.hasNext(); )
		{
			Assembly as = i.next();
			
			for (int h = 0; h < as.size(); h++)
			{
				AsmLine l1 = as.get(h);
				
				// look for [add/sub][abef] # on first line
				if (h < as.size() && l1.opcode.length() > 3 && (l1.opcode.substring(0,3).equals("add") || l1.opcode.substring(0,3).equals("sub") ) && l1.operand.charAt(0) == '#')
				{
					// evaluate to see if data past # is equal to 1
					try
					{
						int value = as.evaluate(l1, l1.expression);
						if (value == 1)
						{
							String reg = l1.opcode.substring(3,4);
							String op = "inc" + reg;
							
							if (l1.opcode.substring(0,3).equals("sub"))
							{
								op = "dec" + reg;
							}
		
							l1.replaceLine(l1.label + " " + op + " " + l1.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
							as.addSubtractAccumulatorImmediateRegisterInstructionCollapseCount++;
							reductionCounter++;
						}
					}
					catch (ExpressionException e)
					{
						
					}
				}					
			}
		}
		
		return reductionCounter;
	}

	public int longBranchReduction(LinkedList<Assembly> ll, int reductionLevel)
	{
		int reductionCounter = 0;
		
		for (Iterator<Assembly> i = ll.iterator(); i.hasNext(); )
		{
			Assembly as = i.next();
			
			for (int h = 0; h < as.size(); h++)
			{
				AsmLine l1 = as.get(h);
				
				// look for lb (long branch) in opcode where there is also a warning
				if (l1.opcode.length() > 2 && l1.opcode.substring(0,2).equals("lb")) // && as.symbols.symbolForName(l1.operand) != null)
				{
					try
					{
						int d1 = l1.bytes.get(1);
						int d2 = l1.bytes.get(2);
						int dist = d1 * 256 + d2 + 3;
						if (dist > 32767) dist = 65536 - dist;
						if ((dist > -128) && (dist < 127))
						{
							String op1 = l1.opcode.substring(1);
							// replace with short branch
							l1.replaceLine(l1.label + " " + op1 + " " + l1.operand + " " + l1.comment + " * ECOAS REDUCTION *", CPU.instructionTable);
							as.longBranchReductionCount++;
							reductionCounter++;
						}
					}
					catch (Exception e)
					{
					}
				}					
			}
		}
		
		return reductionCounter;
	}

	public int setLongBranchWarnings(LinkedList<Assembly> ll)
	{
		int reductionCounter = 0;
		
		for (Iterator<Assembly> i = ll.iterator(); i.hasNext(); )
		{
			Assembly as = i.next();
			
			for (int h = 0; h < as.size(); h++)
			{
				AsmLine l1 = as.get(h);
				
				// look for lb (long branch) in opcode where there is also a warning
				if (l1.opcode.length() > 2 && l1.opcode.substring(0,2).equals("lb")) // && as.symbols.symbolForName(l1.operand) != null)
				{
					try
					{
						int d1 = l1.bytes.get(1);
						int d2 = l1.bytes.get(2);
						int dist = d1 * 256 + d2;
						if (dist > 32767) dist = 65536 - dist;
						if ((dist > -128) && (dist < 127))
						{
							l1.warning = "long branch used when short branch would suffice";
							warningCount++;
						}
					}
					catch (Exception e)
					{
					}
				}					
			}
		}
		
		return reductionCounter;
	}
}


