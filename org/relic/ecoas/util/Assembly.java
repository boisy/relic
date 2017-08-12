package org.relic.ecoas.util;

import java.util.*;
import java.io.*;

import org.relic.ecoas.util.*;
import org.relic.ecoas.h6309.*;

public class Assembly extends ArrayList<AsmLine>
{
	private static final long serialVersionUID = 1L;
	static int 		lineCounter = 1;
	static ExpressionParser parser;
	public SymbolTable symbols;
	CPU cpu;
	ConditionStack condition;
	Stack<AssemblerSection> sectionStack;
	public AssemblerSection currentSection;
	int pass;
	public int errorCount, warningCount;
	public int lineCount, blankLineCount, commentLineCount, instructionLineCount;
	Boolean ignoreUndefinedSymbols;
	public ArrayList<Integer> objectCode, initializedData, initializedDPData;
	public int uninitializedDataCount, uninitializedDPDataCount;
	String fileName;
	public Boolean used;

	public int storeLoadInstructionCollapseCount;
	public int loadAccumulatorImmediateZeroRegisterInstructionCollapseCount;
	public int loadAccumulatorImmediateRegisterInstructionCollapseCount;
	public int addSubtractAccumulatorImmediateRegisterInstructionCollapseCount;
	public int longBranchReductionCount;

	// public member variables
	public int cycles;				// total number of cycles of all instructions in this assembly 
	public double power;				// total power consumed by all instructions in this assembly 

	public String sectionName;
	public int sectionTypeLang, sectionAttrRev, sectionEdition, sectionStackSize, sectionEntryPoint;

	public InstructionMap instructionTable;
		
	public Assembly(CPU cpu)
	{
		this(cpu, false, null);
	}
	
	public Assembly(CPU cpu, String fileName)
	{
		this(cpu, false, fileName);
	}
	
	public Assembly(CPU cpu, Boolean ignoreUndefinedSymbols)
	{
		this(cpu, ignoreUndefinedSymbols, null);
	}
	
	public Assembly(CPU cpu, Boolean ignoreUndefinedSymbols, String fileName)
	{
		super();
		this.uninitializedDataCount = 0;
		this.uninitializedDPDataCount = 0;
		this.fileName = fileName; if (this.fileName == null) this.fileName = "";
		this.ignoreUndefinedSymbols = ignoreUndefinedSymbols;
		this.symbols = new SymbolTable();
		this.cpu = cpu;
		parser = new ExpressionParser(this.symbols);
		condition = new ConditionStack();
		sectionStack = new Stack<AssemblerSection>();
		currentSection = new AssemblerSection(AssemblerSection.Type.Any);
		this.objectCode = new ArrayList<Integer>();
		this.initializedData = new ArrayList<Integer>();
		this.initializedDPData = new ArrayList<Integer>();
		this.used = false;
		
		instructionTable = new InstructionMap();
//										    MNEMONIC							INSTRUCTION CLASS			OPERAND CLASS								PROCESSING CLASS
		instructionTable.put("=", 			new PseudoInstruction("=",			InstructionClass.EQU,		OperandClass.HAS_OPERAND,					new _equ(), AssemblerSection.Type.Any));
		instructionTable.put("absolute", 	new PseudoInstruction("absolute",	InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _null_op(), AssemblerSection.Type.Any));
		instructionTable.put("align", 		new PseudoInstruction("align",   	InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _align(), AssemblerSection.Type.Any));
		instructionTable.put("bsz", 		new PseudoInstruction("bsz",		InstructionClass.FC,		OperandClass.HAS_NO_OPERAND,				new _zmb(), AssemblerSection.Type.Code));
		instructionTable.put("byte", 		new PseudoInstruction("byte",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _fcb(), AssemblerSection.Type.Any));
		instructionTable.put("byte", 		new PseudoInstruction("code",		InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _null_op(), AssemblerSection.Type.Code));
		instructionTable.put("conde", 		new PseudoInstruction("cond",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifne(), AssemblerSection.Type.Any));
		instructionTable.put("db", 			new PseudoInstruction("db",			InstructionClass.RM,		OperandClass.HAS_OPERAND,					new _fcb(), AssemblerSection.Type.Data));
		instructionTable.put("ds", 			new PseudoInstruction("ds",			InstructionClass.RM,		OperandClass.HAS_OPERAND,					new _rmb(), AssemblerSection.Type.Data));
		instructionTable.put("dtb", 		new PseudoInstruction("dtb",		InstructionClass.FC,		OperandClass.HAS_NO_OPERAND,				new _dtb(), AssemblerSection.Type.Code));
		instructionTable.put("dts",			new PseudoInstruction("dts",		InstructionClass.FC,		OperandClass.HAS_NO_OPERAND,				new _dts(), AssemblerSection.Type.Code));
		instructionTable.put("dw", 			new PseudoInstruction("dw",			InstructionClass.RM,		OperandClass.HAS_OPERAND,					new _fdb(), AssemblerSection.Type.Code));
		instructionTable.put("dword", 		new PseudoInstruction("dword",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _fqb(), AssemblerSection.Type.Code));
		instructionTable.put("else", 		new PseudoInstruction("else",		InstructionClass.ELSE,   	OperandClass.HAS_NO_OPERAND,				new _else(), AssemblerSection.Type.Any));
		instructionTable.put("end", 		new PseudoInstruction("end",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _end(), AssemblerSection.Type.Any));
		instructionTable.put("endc", 		new PseudoInstruction("endc",		InstructionClass.ENDC,   	OperandClass.HAS_NO_OPERAND,				new _endc(), AssemblerSection.Type.Any));
		instructionTable.put("endif",		new PseudoInstruction("endif",		InstructionClass.ENDC,   	OperandClass.HAS_NO_OPERAND,				new _endc(), AssemblerSection.Type.Any));
		instructionTable.put("equ",			new PseudoInstruction("equ",		InstructionClass.EQU,    	OperandClass.HAS_OPERAND,					new _equ(), AssemblerSection.Type.Any));
		instructionTable.put("even", 		new PseudoInstruction("even",		InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _even(), AssemblerSection.Type.Code));
		instructionTable.put("fcb",			new PseudoInstruction("fcb",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _fcb(), AssemblerSection.Type.Code));
		instructionTable.put("fcc",			new PseudoInstruction("fcc",		InstructionClass.FC,		OperandClass.HAS_OPERAND_WITH_DELIMITERS,	new _fcc(), AssemblerSection.Type.Code));
		instructionTable.put("fcn", 		new PseudoInstruction("fcn",		InstructionClass.FC,		OperandClass.HAS_OPERAND_WITH_DELIMITERS,	new _fcz(), AssemblerSection.Type.Code));
		instructionTable.put("fcr", 		new PseudoInstruction("fcr",		InstructionClass.FC,		OperandClass.HAS_OPERAND_WITH_DELIMITERS,	new _fcr(), AssemblerSection.Type.Code));
		instructionTable.put("fcs", 		new PseudoInstruction("fcs",		InstructionClass.FC,		OperandClass.HAS_OPERAND_WITH_DELIMITERS,	new _fcs(), AssemblerSection.Type.Code));
		instructionTable.put("fcz", 		new PseudoInstruction("fcz",		InstructionClass.FC,		OperandClass.HAS_OPERAND_WITH_DELIMITERS,	new _fcz(), AssemblerSection.Type.Code));
		instructionTable.put("fdb", 		new PseudoInstruction("fdb",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _fdb(), AssemblerSection.Type.Code));
		instructionTable.put("fill", 		new PseudoInstruction("fill",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _fill(), AssemblerSection.Type.Code));
		instructionTable.put("fqb", 		new PseudoInstruction("fqb",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _fqb(), AssemblerSection.Type.Code));
		instructionTable.put("fzb", 		new PseudoInstruction("fzb",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _zmb(), AssemblerSection.Type.Code));
		instructionTable.put("fzd", 		new PseudoInstruction("fzd",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _zmd(), AssemblerSection.Type.Code));
		instructionTable.put("fzq", 		new PseudoInstruction("fzq",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _zmq(), AssemblerSection.Type.Code));
		instructionTable.put("if", 			new PseudoInstruction("if",			InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifne(), AssemblerSection.Type.Any));
		instructionTable.put("ifeq", 		new PseudoInstruction("ifeq",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifeq(), AssemblerSection.Type.Any));
		instructionTable.put("ifge", 		new PseudoInstruction("ifge",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifge(), AssemblerSection.Type.Any));
		instructionTable.put("ifgt", 		new PseudoInstruction("ifgt",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifgt(), AssemblerSection.Type.Any));
		instructionTable.put("ifle",		new PseudoInstruction("ifle",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifle(), AssemblerSection.Type.Any));
		instructionTable.put("iflt", 		new PseudoInstruction("iflt",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _iflt(), AssemblerSection.Type.Any));
		instructionTable.put("ifn", 		new PseudoInstruction("ifn",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifne(), AssemblerSection.Type.Any));
		instructionTable.put("ifne", 		new PseudoInstruction("ifne",		InstructionClass.IF,		OperandClass.HAS_OPERAND,					new _ifne(), AssemblerSection.Type.Any));
		instructionTable.put("ifp1", 		new PseudoInstruction("ifp1",		InstructionClass.IF,		OperandClass.HAS_NO_OPERAND,				new _ifp1(), AssemblerSection.Type.Any));
		instructionTable.put("ifp2", 		new PseudoInstruction("ifp2",		InstructionClass.IF,		OperandClass.HAS_NO_OPERAND,				new _ifp2(), AssemblerSection.Type.Any));
		instructionTable.put("include", 	new PseudoInstruction("include", 	InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _use(), AssemblerSection.Type.Any));
		instructionTable.put("lib", 		new PseudoInstruction("lib",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _use(), AssemblerSection.Type.Any));
		instructionTable.put("nam", 		new PseudoInstruction("nam",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND_WITH_SPACES,		new _nam(), AssemblerSection.Type.Any));
		instructionTable.put("name", 		new PseudoInstruction("name",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND_WITH_SPACES,		new _nam(), AssemblerSection.Type.Any));
		instructionTable.put("odd", 		new PseudoInstruction("odd",		InstructionClass.EQU,    	OperandClass.HAS_NO_OPERAND,				new _odd(), AssemblerSection.Type.Code));
		instructionTable.put("opt", 		new PseudoInstruction("opt",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _opt(), AssemblerSection.Type.Any));
		instructionTable.put("org", 		new PseudoInstruction("org",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _org(), AssemblerSection.Type.Any));
		instructionTable.put("origin", 		new PseudoInstruction("origin",  	InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _org(), AssemblerSection.Type.Code));
		instructionTable.put("pag", 		new PseudoInstruction("pag",		InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _page(), AssemblerSection.Type.Any));
		instructionTable.put("page", 		new PseudoInstruction("page",		InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _page(), AssemblerSection.Type.Any));
		instructionTable.put("page0", 		new PseudoInstruction("page0", 	  	InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _null_op(), AssemblerSection.Type.Any));
		instructionTable.put("page1", 		new PseudoInstruction("page1",  	InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _null_op(), AssemblerSection.Type.Any));
		instructionTable.put("relative", 	new PseudoInstruction("relative",	InstructionClass.RM,		OperandClass.HAS_OPERAND,					new _rmb(), AssemblerSection.Type.ConstantOrData));
		instructionTable.put("rmb", 		new PseudoInstruction("rmb",		InstructionClass.RM,		OperandClass.HAS_OPERAND,					new _rmb(), AssemblerSection.Type.ConstantOrData));
		instructionTable.put("rmd", 		new PseudoInstruction("rmd",		InstructionClass.RM,		OperandClass.HAS_OPERAND,					new _rmd(), AssemblerSection.Type.ConstantOrData));
		instructionTable.put("rmq", 		new PseudoInstruction("rmq",		InstructionClass.RM,		OperandClass.HAS_OPERAND,					new _rmq(), AssemblerSection.Type.ConstantOrData));
		instructionTable.put("set", 		new PseudoInstruction("set",		InstructionClass.EQU,    	OperandClass.HAS_OPERAND,					new _set(), AssemblerSection.Type.Any));
		instructionTable.put("setdp", 		new PseudoInstruction("setdp",   	InstructionClass.EQU,    	OperandClass.HAS_OPERAND,					new _setdp(), AssemblerSection.Type.Code));
		instructionTable.put("spc", 		new PseudoInstruction("spc",		InstructionClass.OTHER,  	OperandClass.HAS_NO_OPERAND,				new _spc(), AssemblerSection.Type.Any));
		instructionTable.put("title", 		new PseudoInstruction("title",  	InstructionClass.OTHER,  	OperandClass.HAS_OPERAND_WITH_SPACES,		new _ttl(), AssemblerSection.Type.Any));
		instructionTable.put("ttl", 		new PseudoInstruction("ttl",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND_WITH_SPACES,		new _ttl(), AssemblerSection.Type.Any));
		instructionTable.put("use", 		new PseudoInstruction("use",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _use(), AssemblerSection.Type.Any));
		instructionTable.put("word", 		new PseudoInstruction("word",		InstructionClass.FC,		OperandClass.HAS_OPERAND,					new _fdb(), AssemblerSection.Type.Data));
		instructionTable.put("zmb", 		new PseudoInstruction("zmb",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _zmb(), AssemblerSection.Type.Code));
		instructionTable.put("zmd", 		new PseudoInstruction("zmd",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _zmd(), AssemblerSection.Type.Code));
		instructionTable.put("zmq", 		new PseudoInstruction("zmq",		InstructionClass.OTHER,  	OperandClass.HAS_OPERAND,					new _zmq(), AssemblerSection.Type.Code));
		instructionTable.put("psect",		new PseudoInstruction("psect", 		InstructionClass.OTHER, 	OperandClass.HAS_OPERAND, 					new _psect(), AssemblerSection.Type.Any));
		instructionTable.put("csect", 		new PseudoInstruction("csect",		InstructionClass.OTHER, 	OperandClass.HAS_NO_OPERAND,				new _csect(), AssemblerSection.Type.Any));
		instructionTable.put("vsect",		new PseudoInstruction("vsect",		InstructionClass.OTHER,		OperandClass.HAS_NO_OPERAND,				new _vsect(), AssemblerSection.Type.Any));
		instructionTable.put("vsectdp",		new PseudoInstruction("vsectdp",	InstructionClass.OTHER,		OperandClass.HAS_NO_OPERAND,				new _vsectdp(), AssemblerSection.Type.Any));
		instructionTable.put("endsect",		new PseudoInstruction("endsect",	InstructionClass.OTHER,		OperandClass.HAS_NO_OPERAND,				new _endsect(), AssemblerSection.Type.Any));

		// copy CPU's instructon table to here
		instructionTable.putAll(cpu.instructionTable);
	}
	
	public boolean add(AsmLine line)
	{
		line.lineNumber = lineCounter++;
		return super.add(line);
	}
	
	public void add(String line)
	{
		AsmLine l = new AsmLine(line, instructionTable);
		l.lineNumber = lineCounter++;
		super.add(l);
	}
	
	public AsmLine remove(AsmLine l)
	{
		AsmLine ret = null;
		
		int index = indexOf(l);
		if (index != -1)
		{
			ret = remove(index);
		}
		
		return ret;
	}
	
	public AsmLine remove(int l)
	{
		AsmLine ret = null;
		
		int index = l;
		if (index != -1)
		{
			ret = super.remove(index);

			// renumber subsequent lines
			for (int j = index; j < size(); j++)
			{
				AsmLine ll = get(j);
				ll.lineNumber--;
			}
			
			// adjust our global counter
			lineCounter--;
		}
		
		return ret;
	}
	
	void assembleLine(AsmLine line)
	{
		line.sectionType = currentSection.type;
		
		if (line.opcode == "")
		{
			// nothing to assemble
			return;
		}
		
		Instruction ii = line.instruction;
		
		if (ii == null)
		{
			// mnemonic not found
			line.error = "unrecognized mnemonic";
			return;
		}

		// check if this instruction is allowed in our current section
		try
		{
			if (ii.section != AssemblerSection.Type.Any && ii.section != currentSection.type
					&& (ii.section == AssemblerSection.Type.ConstantOrData && currentSection.type != AssemblerSection.Type.Constant && currentSection.type != AssemblerSection.Type.Data && currentSection.type != AssemblerSection.Type.DPData))
			{
				line.error = "instruction is not allowed in " + currentSection + " section";
				return;
			}
		}
		catch (EmptyStackException e)
		{
		}
		
		parser.setSymbolTable(symbols);
		
		if (ii instanceof PseudoInstruction)
		{
			// this is a pseudo instruction
			PseudoInstruction i = (PseudoInstruction)ii;
		
			i.cb.process(this, line, i);
		}
		else
		{
			if (line.label != null && line.label != "")
			{
				if (line.addLabelToSymbolTable(currentSection, symbols, currentSection.counter) == false)
				{
					line.error = "duplicate label";
				}
			}

			// this is a CPU instruction
			cpu.assembleLine(this, line);
			cycles += line.cycleCount;
			power += (line.power * line.cycleCount);
			instructionLineCount++;
		}
	}
	
	void makePass()
	{
		cycles = 0;
		power = 0;
		warningCount = 0;
		errorCount = 0;
		lineCount = 0;
		blankLineCount = 0;
		commentLineCount = 0;
		instructionLineCount = 0;
		sectionStack.clear();
		currentSection = new AssemblerSection(AssemblerSection.Type.Any);
		objectCode.clear();
		initializedData.clear();
		initializedDPData.clear();
		uninitializedDataCount = 0;
		uninitializedDPDataCount = 0;
		currentSection.counter = 0;
		
		// remove all temporary labels
		symbols.removeTemporaryLabels();
		
		for (Iterator<AsmLine> it = this.iterator(); it.hasNext(); )
		{
			AsmLine l = it.next();
			l.bytes.clear();
			l.error = null;
			l.warning = null;
			l.references.clear();
			symbols.add(new Symbol("*", currentSection.counter, "code", true), true);
			lineCount++;
			l.offset = currentSection.counter;

			// do checking of operand class
			// special case: no opcode but there is a label
			if (l.label != "" && l.opcode == "")
			{
				if (l.addLabelToSymbolTable(currentSection, symbols, l.offset) == false)
				{
					l.error = "attempt at redefining a symbol (current value = " + symbols.symbolForName(l.label).value + ", attempted set value = "+ l.offset + ")";
				}
			}

			// special case: blank line or comment line
			if (l.label == "" && l.opcode == "")
			{
				if (l.comment == "")
				{
					blankLineCount++;
					
					// remove all temporary labels
					symbols.removeTemporaryLabels();
				}
				else
				{
					commentLineCount++;
				}
			}

			l.error = null;
			this.assembleLine(l);

			if (l.error != null)
			{
				if (ignoreUndefinedSymbols == true && l.error.equals("undefined symbol"))
				{
					l.error = null;
				}
				else
				{
					errorCount++;
				}
			}
			
			if (l.warning != null)
			{
				warningCount++;
			}
		}
	}

	// Public interface method to assemble an assembly
	public void assemble()
	{
		// first pass is used to gather all forward referencing symbols
		pass = 1;
		makePass();

		// second pass picks up forward references
		pass = 2;
		makePass();

		if (condition.isEmpty() == false)
		{
			// too may IFs
			showError("too many conditional IFs, not enough ENDCs");
		}

		if (sectionStack.isEmpty() == false)
		{
			// not enough endsects
			showError("missing an endsect");
		}
		
		// remove "*" global symbol in symbol table since we're done with it now
		symbols.remove("*");
	}

	public void showError(String message)
	{
		if (fileName.equals(""))
		{
			System.out.println(String.format("**** Error: %s", message));
		}
		else
		{
			System.out.println(String.format("**** Error: %s (file: %s)", message, fileName));
		}
	}
	
	public void show(int codeOffset)
	{
		for (Iterator<AsmLine> it = this.iterator(); it.hasNext(); )
		{
			AsmLine l = it.next();
			l.dump(codeOffset);
		}		
	}
	
	public void show()
	{
		show(0);
	}
	
	public Boolean read(String infile) throws IOException
	{
		BufferedReader in;
		
		try
		{
			in = new BufferedReader(new FileReader(infile));
		}
		catch (IOException e)
		{
			System.err.println("failed to open file '" + infile + "'for reading");
			return false;
		}

		Boolean eof = false;
		
		do
		{
			String line;
			
			line = in.readLine();

			if (line == null)
			{
				eof = true;
			}
			else
			{
				AsmLine l = new AsmLine(line, instructionTable);
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
		
		for (Iterator<AsmLine> it = this.iterator(); it.hasNext(); )
		{
			AsmLine l = it.next();
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

	class _align implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _dtb implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _dts implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _even implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _page implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _ttl implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _nam implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _odd implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _opt implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	enum Conditional
	{
		_IF,
		_IFP1,
		_IFP2,
		_IFNE,
		_IFEQ,
		_IFGT,
		_IFGE,
		_IFLT,
		_IFLE
	};

	void _generic_if(AsmLine line, Instruction i, Conditional whichOne)
	{
		// check the state of the current condition
		if (condition.isFalse())
		{
			// current condition false, make this one false too
			condition.pushFalse();
			return;
		}
		
		int result = 0;
		
		// previous condition true, evaluate this one
		if (whichOne != Conditional._IFP1 && whichOne != Conditional._IFP2)
		{
			try
			{
				result = evaluate(line, line.operand);
			}
			catch (ExpressionException e)
			{
				line.error = e.getMessage();
			}
		}
		
		switch (whichOne)
		{
			case _IFP1:
				if (pass == 1) condition.pushTrue(); else condition.pushFalse();
				break;

			case _IFP2:
				if (pass == 2) condition.pushTrue(); else condition.pushFalse();
				break;

			case _IFEQ:
				if (result == 0) condition.pushTrue(); else condition.pushFalse();
				break;

			case _IF:
			case _IFNE:
				if (result != 0) condition.pushTrue(); else condition.pushFalse();
				break;

			case _IFGT:
				if (result > 0) condition.pushTrue(); else condition.pushFalse();
				break;

			case _IFGE:
				if (result >= 0) condition.pushTrue(); else condition.pushFalse();
				break;

			case _IFLT:
				if (result < 0) condition.pushTrue(); else condition.pushFalse();
				break;

			case _IFLE:
				if (result <= 0) condition.pushTrue(); else condition.pushFalse();
				break;
		}
	}
	
	class _ifp1 implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFP1);
		}		
	}
	
	class _ifp2 implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFP2);
		}
	}
	
	class _ifeq implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFEQ);
		}
	}
	
	class _ifne implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFNE);
		}
	}
	
	class _iflt implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFLT);
		}
	}
	
	class _ifle implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFLE);
		}
	}
	
	class _ifgt implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFGT);
		}
	}
	
	class _ifge implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_generic_if(line, i, Conditional._IFGE);
		
		}
	}
	
	class _endc implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// check the state of the current condition
			if (condition.isEmpty())
			{
				line.error = "ENDC without a conditional IF";
				return;
			}
			
			condition.pop();
			return;
		}
	}

	class _else implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			if (condition.isEmpty() == true)
			{
				line.error = "ELSE without a conditional IF";
				return;
			}
	
			// determine if the previous conditional was false
			Object o = condition.pop();
			Boolean previousCondition = condition.isFalse();
			condition.push(o);
			if (previousCondition == true)
			{
				// ignore this one
				return;
			}
			
			// invert the sense of the conditional
			if (condition.isFalse())
			{
				condition.pop();
				condition.pushTrue();
			}
			else
			{			
				condition.pop();
				condition.pushFalse();
			}
			
			return;
		}
	}
	
	class _fill implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			String operand = line.operand;
			
			String fillCount, fillValue;
			
			if (operand.indexOf(',') == -1)
			{
				line.error = "fill requires two comma separated values";
				return;
			}
			
			fillValue = operand.substring(0, operand.indexOf(','));
			fillCount = operand.substring(operand.indexOf(',') + 1);

			
			int fc;
			try
			{
				fc = evaluate(line, fillCount);
			}
			catch (ExpressionException e)
			{
				line.error = e.getMessage();
				return;
			}
			
			int fv;
			
			try
			{
				fv = evaluate(line, fillValue);
			}
			catch (ExpressionException e)
			{
				line.error = e.getMessage();
				return;
			}
			
			String labelType = null;
			
			switch (currentSection.type)
			{
				case DPData:
					line.offset = initializedDPData.size();
					labelType = "idpd";
					break;
				case Data:
					line.offset = initializedData.size();
					labelType = "idat";
					break;
				default:
					labelType = "code";
					break;
			}
			
			if (line.label != null && line.label != "")
			{
				if (line.addLabelToSymbolTable(labelType, symbols, line.offset) == false)
				{
					line.error = "duplicate label";
				}
			}

			while (fc-- > 0)
			{
				emit(asm, line, fv);
				if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
				{
					currentSection.counter -= 1;
				}
			}
		}
	}
		
	class _fcb implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_fill_constant(asm, line, i, 1);
		}
	}
	
	class _fdb implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_fill_constant(asm, line, i, 2);
		}
	}
	
	class _fqb implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_fill_constant(asm, line, i, 4);
		}
	}

	
	/*****************************************************************************
	 *
	 * FILL CONSTANT DATA WITH VALUE
	 *
	 *****************************************************************************/
	class _fcs implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			String operand = line.operand;
			
			char delim = operand.charAt(0);
			operand = operand.substring(1);
			if (operand.indexOf(delim) == -1)
			{
				line.error = "missing delimiter";
				return;
			}
	
			operand = operand.substring(0, operand.indexOf(delim));
			
			if (line.label != null && line.label != "")
			{
				if (line.addLabelToSymbolTable(currentSection, symbols, line.offset) == false)
				{
					line.error = "duplicate label";
				}
			}
			
			switch (currentSection.type)
			{
				case DPData:
					line.offset = initializedDPData.size();
					break;
				case Data:
				default:
					line.offset = initializedData.size();
					break;
			}

			int ii;
			for (ii = 0; ii < operand.length() - 1; ii++)
			{
				emit(asm, line, operand.charAt(ii));
				if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
				{
					currentSection.counter -= 1;
				}
			}
			emit(asm, line, operand.charAt(ii) + 128);	
			if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
			{
				currentSection.counter -= 1;
			}
			
			return;
		}
	}

	class _fcc implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			String operand = line.operand;
			
			char delim = operand.charAt(0);
			operand = operand.substring(1);
			if (operand.indexOf(delim) == -1)
			{
				line.error = "missing delimiter";
				return;
			}
	
			operand = operand.substring(0, operand.indexOf(delim));
			
			String labelType = null;
			
			switch (currentSection.type)
			{
				case DPData:
					line.offset = initializedDPData.size();
					labelType = "idpd";
					break;
				case Data:
					line.offset = initializedData.size();
					labelType = "idat";
					break;
				default:
					labelType = "code";
					break;
			}

			if (line.label != null && line.label != "")
			{
				if (line.addLabelToSymbolTable(labelType, symbols, line.offset) == false)
				{
					line.error = "duplicate label";
				}
			}
			
			int ii;
			for (ii = 0; ii < operand.length(); ii++)
			{
				emit(asm, line, operand.charAt(ii));
				if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
				{
					currentSection.counter -= 1;
				}
			}
	
			return;
		}
	}
	
	class _fcr implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			_fcc c = new _fcc();
			c.process(asm, line, i);
			
			emit(asm, line, 0x0D);
			emit(asm, line, 0x00);
			if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
			{
				currentSection.counter -= 2;
			}
		}
	}
		
	class _fcz implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			_fcc c = new _fcc();
			c.process(asm, line, i);
			
			emit(asm, line, 0x00);
			if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
			{
				currentSection.counter -= 1;
			}
		}
	}
		
	class _org implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			int result;
			
			try
			{
				result = evaluate(line, line.operand);
			}
			catch (ExpressionException e)
			{
				line.error = e.getMessage();
				return;
			}
			
			currentSection.counter = result; 
		}
	}
		
	class _equ implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			if (line.label.length() == 0)
			{
				line.error = "label required";
				return;
			}

			int result;
			try
			{
				result = evaluate(line, line.operand);
			}
			catch (ExpressionException e)
			{
				line.error = e.getMessage();
				return;
			}
			
			if (line.addLabelToSymbolTable("cnst", symbols, result) == false)
			{
				line.error = "duplicate label";
			}
	
			line.offset = result;

			return;
		}
	}

	class _set implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
					
			if (line.label.length() == 0)
			{
				line.error = "label required";;
				return;
			}

			int result;
			try
			{
				result = evaluate(line, line.operand);
			}
			catch (ExpressionException e)
			{
				line.error = e.getMessage();
				return;
			}
			
			if (line.addLabelToSymbolTable("cnst", symbols, result) == false)
			{
				line.error = "duplicate label";
			}

			line.offset = result;

			return;
		}	
	}
	
	class _null_op implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}
		
	class _setdp implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
		}
	}
		
	class _spc implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
		}
	}
		
	class _use implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			if (line.label != "")
			{
				line.error = "label not allowed";
			}
			
			Assembly as = new Assembly(cpu);

			try
			{
				as.read(line.operand);
			}
			catch (IOException e)
			{
				line.error = "cannot open file " + line.operand;
				return;
			}
			
			as.makePass();
		}
	}
		
	class _end implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}
			
			if (line.label != "")
			{
				line.error = "label not allowed";
			}
		}
	}
		
	void _reserve_memory(AsmLine line, Instruction i, int size)
	{
		// if we are currently in a FALSE conditional, just return.
		if (condition.isFalse())
		{
			return;
		}
		
		String operand = line.operand;
		
		int result;
		try
		{
			result = evaluate(line, operand);
		}
		catch (ExpressionException e)
		{
			line.error = e.getMessage();
			return;
		}
		
		// assume we are in a vsect...  
		String type;
		
		switch (currentSection.type)
		{
			case Data:
				type = "udat";
				break;
			case DPData:
				type = "udpd";
				break;
			case Constant:
			default:
				type = "cnst";
				break;
		}

		if (line.label != null && line.label != "")
		{
			if (line.addLabelToSymbolTable(type, symbols, currentSection.counter) == false)
			{
				line.error = "duplicate label";
			}
		}
		
		switch (currentSection.type)
		{
			case Data:
				line.offset = uninitializedDataCount;
				currentSection.counter += result * size;
				uninitializedDataCount += result * size;				
				break;
			case DPData:
				line.offset = uninitializedDPDataCount;
				currentSection.counter += result * size;
				uninitializedDPDataCount += result * size;
				break;
			case Constant:
				line.offset = currentSection.counter;
				currentSection.counter += result * size;
				break;
		}
	}
		
	class _rmb implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_reserve_memory(line, i, 1);
		}
	}
		
	class _rmd implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_reserve_memory(line, i, 2);
		}
	}
		
	class _rmq implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_reserve_memory(line, i, 4);
		}
	}
		
	void _fill_constant(Assembly asm, AsmLine line, Instruction i, int size)
	{
		// if we are currently in a FALSE conditional, just return.
		if (condition.isFalse())
		{
			return;
		}
		
		String operand = line.operand;
		
		while (operand != null)
		{
			String currentOperand = null;
			
			if (operand.indexOf(',') != -1)
			{
				currentOperand = operand.substring(0, operand.indexOf(','));
				operand = operand.substring(operand.indexOf(',') + 1);
			}
			else
			{
				currentOperand = operand;
				operand = null;
			}
			
			int result;
			try
			{
				result = evaluate(line, currentOperand);
			}
			catch (ExpressionException e)
			{
				line.error = e.getMessage();
				return;
			}

			String labelType = null;
			
			switch (currentSection.type)
			{
				case DPData:
					line.offset = initializedDPData.size();
					labelType = "idpd";
					break;
				case Data:
					line.offset = initializedData.size();
					labelType = "idat";
					break;
				default:
					labelType = "code";
					break;
			}
			
			if (line.label != null && line.label != "")
			{
				if (line.addLabelToSymbolTable(labelType, symbols, line.offset) == false)
				{
					line.error = "duplicate label";
				}
			}

			switch (size)
			{
				case 1:
					if (result > 0xFF && line.forceByte == false)
					{
						line.warning = "value truncated to 8 bits";
					}
					result = line.lobyte(result);
					emit(asm, line, result);
					if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
					{
						currentSection.counter -= 1;
					}
					break;

				case 2:
					if (result > 0xFFFF && line.forceByte == false)
					{
						line.warning = "value truncated to 16 bits";
					}
					eword(asm, line, result);
					if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
					{
						currentSection.counter -= 2;
					}
					break;

				case 4:
					equad(asm, line, result);
					if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
					{
						currentSection.counter -= 4;
					}
					break;
			}
		}

		return;
	}

	
	void _fill_constant_with_value(Assembly asm, AsmLine line, Instruction i, int size, int value)
	{
		// if we are currently in a FALSE conditional, just return.
		if (condition.isFalse())
		{
			return;
		}
		
		String operand = line.operand;
		
		int result;
		try
		{
			result = evaluate(line, operand);
		}
		catch (ExpressionException e)
		{
			line.error = e.getMessage();
			return;
		}
		
		switch (currentSection.type)
		{
			case DPData:
				line.offset = initializedDPData.size();
				break;
			case Data:
			default:
				line.offset = initializedData.size();
				break;
		}

		while (result-- > 0)
		{
			switch (size)
			{
				case 1:
					if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
					emit(asm, line, value);
					{
						currentSection.counter -= 1;
					}
					break;

				case 2:
					eword(asm, line, value);
					if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
					{
						currentSection.counter -= 2;
					}
					break;

				case 4:
					equad(asm, line, value);
					if (currentSection.type == AssemblerSection.Type.Data || currentSection.type == AssemblerSection.Type.DPData)
					{
						currentSection.counter -= 4;
					}
					break;
			}
		}

		return;
	}

	class _zmb implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_fill_constant_with_value(asm, line, i, 1, 0);
		}
	}
	
	class _zmd implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_fill_constant_with_value(asm, line, i, 2, 0);
		}
	}
	
	class _zmq implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			_fill_constant_with_value(asm, line, i, 4, 0);
		}
	}
	
	class _psect implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			// if we are currently in a FALSE conditional, just return.
			if (condition.isFalse())
			{
				return;
			}

			if (sectionStack.isEmpty() == false)
			{
				line.error = "program section cannot be contained in another section";
				return;
			}

			AssemblerSection newCurrentSection = new AssemblerSection(AssemblerSection.Type.Code);
			newCurrentSection.counter = currentSection.counter;				// steal counter from previous section (is this really a good idea to do it this way?) 
			sectionStack.push(currentSection);
			currentSection = newCurrentSection;
			line.sectionType = currentSection.type;
			String operand = line.operand;

			line.forceWord = true;

			// parse out individual parts
			if (operand.indexOf(',') == -1) {
				line.error = "missing module name";
			} else {
				sectionName = operand.substring(0, operand.indexOf(','));
				operand = operand.substring(operand.indexOf(',') + 1);
				if (operand.indexOf(',') == -1) {
					line.error = "missing type/language";
				} else {
					String typeLang = operand
							.substring(0, operand.indexOf(','));
					operand = operand.substring(operand.indexOf(',') + 1);
					if (operand.indexOf(',') == -1) {
						line.error = "missing attribute/revision";
					} else {
						try
						{
							sectionTypeLang = evaluate(line, typeLang);
						}
						catch (ExpressionException e)
						{
							line.error = e.getMessage();
						}

						String attrRev = operand.substring(0, operand
								.indexOf(','));
						operand = operand.substring(operand.indexOf(',') + 1);
						if (operand.indexOf(',') == -1) {
							line.error = "missing edition";
						} else {
							try
							{
								sectionAttrRev = evaluate(line, attrRev);
							}
							catch (ExpressionException e)
							{
								line.error = e.getMessage();
							}

							String edition = operand.substring(0, operand
									.indexOf(','));
							operand = operand
									.substring(operand.indexOf(',') + 1);
							if (operand.indexOf(',') == -1) {
								line.error = "missing stack size";
							} else {
								try
								{
									sectionEdition = evaluate(line, edition);
								}
								catch (ExpressionException e)
								{
									line.error = e.getMessage();
								}

								String stackSize = operand.substring(0, operand
										.indexOf(','));
								operand = operand.substring(operand
										.indexOf(',') + 1);

								try
								{
									sectionStackSize = evaluate(line, stackSize);
								}
								catch (ExpressionException e)
								{
									line.error = e.getMessage();
								}

								String entryPoint = operand;

								parser.setSymbolTable(symbols);
								try
								{
									sectionEntryPoint = evaluate(line, entryPoint);
								}
								catch (ExpressionException e)
								{
									line.error = e.getMessage();
								}
							}
						}
					}
				}
			}
		}
	}

	class _csect implements InstructionCallback 
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			line.offset = 0;
			symbols.add(new Symbol("*", line.offset, "code", true), true);
			sectionStack.push(currentSection);
			currentSection = new AssemblerSection(AssemblerSection.Type.Constant);
			line.sectionType = currentSection.type;
		}
	}

	class _vsect implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			sectionStack.push(currentSection);
			// because of the way the line routines parse vsect, any text following vsect will be
			// seen as a comment.  We do a special hack to see if the comment is "dp" and if so,
			// move it from the comment line to the operand line.
			if (line.comment.trim().equals("dp"))
			{
				line.comment = "";
				line.operand = "dp";
			}

			if (line.operand.equals("dp"))
			{
				currentSection = new AssemblerSection(AssemblerSection.Type.DPData);
			}
			else
			{
				currentSection = new AssemblerSection(AssemblerSection.Type.Data);
			}
			currentSection.counter = uninitializedDataCount;
			line.sectionType = currentSection.type;
		}
	}

	class _vsectdp implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			sectionStack.push(currentSection);
			currentSection = new AssemblerSection(AssemblerSection.Type.DPData);
			currentSection.counter = uninitializedDPDataCount; 
			line.sectionType = currentSection.type;
		}
	}

	class _endsect implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			if (sectionStack.isEmpty() == true)
			{
				line.error = "too many endsect directives";
				return;
			}
			
			switch (currentSection.type)
			{
				case Data:
					line.offset = initializedData.size();
					break;

				case DPData:
					line.offset = initializedDPData.size();
					break;
			}

			currentSection = (AssemblerSection)sectionStack.pop();
			if (currentSection.type == AssemblerSection.Type.Any)
			{
//				emitter.end(asm, line);
			}
		}
	}
	
	public int evaluate(AsmLine line, String expression) throws ExpressionException
	{
		ReferenceTable references = new ReferenceTable();
		
		parser.setSymbolTable(symbols);
		int result = parser.evaluate(expression, references);
		
		// for each reference that the parser added, determine:
		// 1. if the symbol exists in the symbol table.  if so:
		//    1a. mark the reference as a local or external one depending upon the symbol's status
		// 2. If the symbol does not exist in the symbol table, then
		//    2a. mark the reference as an external one
		// 3. assign the local reference's type based on the section it is in (vsect, psect, csect)
		for (Iterator i = references.iterator(); i.hasNext(); )
		{
			Reference r = (Reference)i.next();
			
			Symbol s = symbols.symbolForName(r.name); 

			if (s != null)
			{
				// since the symbol exists in the symbol table, the reference follows the symbol's
				// global flag
				r.external = s.global;
				r.type = s.type;
			}
			else
			{
				// the symbol doesn't exist in the symbol table, so the reference must be external
				r.external = true;
			}
		}
		
		return result;
	}

	public void showErrors(int codeOffset)
	{
		for (Iterator<AsmLine> it = this.iterator(); it.hasNext(); )
		{
			AsmLine l = it.next();
			if (l.error != null)
			{
				l.dump(codeOffset);
			}
		}
	}
	
	public void showWarnings(int codeOffset)
	{
		for (Iterator<AsmLine> it = this.iterator(); it.hasNext(); )
		{
			AsmLine l = it.next();
			if (l.warning != null)
			{
				l.dump(codeOffset);
			}
		}
	}
	
	public void showSummary(Boolean showLocalSymbols, Boolean showGlobalSymbols, Boolean showReferences)
	{
		System.out.printf("Section name: %s\n", sectionName);
		System.out.printf("TyLa/RvAt:    %02X/%02X\n", sectionTypeLang, sectionAttrRev);
		System.out.printf("Asm valid:    %s\n", errorCount == 0 ? "Yes" : "No");
		System.out.printf("Edition:      %d\n", sectionEdition);
		System.out.printf("  Section     Init Uninit\n");
		System.out.printf("   Code:      %04X (%d bytes / %d cycles / %f nanojoules)\n", objectCode.size(), objectCode.size(), cycles, power * 1000000);
		System.out.printf("     DP:        %02X   %02X\n", initializedDPData.size(), uninitializedDPDataCount);
		System.out.printf("   Data:      %04X %04X\n", initializedData.size(), uninitializedDataCount);
		System.out.printf("  Stack:      %04X\n", sectionStackSize);
		System.out.printf("Entry Point:  %04X\n", sectionEntryPoint);
		System.out.printf("\n");
				
		if (showLocalSymbols == true)
		{
			SymbolTable gt = new SymbolTable();
			for (Iterator i = symbols.entrySet().iterator(); i.hasNext(); )
			{
				Map.Entry e = (Map.Entry)i.next();
				Symbol s = (Symbol)e.getValue();
				
				if (s.global == true)
				{
					gt.add(s);
				}
			}
	
			System.out.printf("%d global symbols defined:\n",gt.size());
			gt.show();
		}
		
		if (showReferences == true)
		{
			ReferenceTable ert = new ReferenceTable();
			ReferenceTable lrt = new ReferenceTable();
			for (Iterator i = this.iterator(); i.hasNext(); )
			{
				AsmLine l = (AsmLine)i.next();
				
				for (Iterator j = l.references.iterator(); j.hasNext(); )
				{
					Reference r = (Reference)j.next();
					
					if (r.external == true) ert.add(r);
					else lrt.add(r);
				}
			}
			
			System.out.printf("\n%d external references:\n", ert.size());
			ert.show();

			System.out.printf("\n%d local references:\n", lrt.size());
			lrt.show();
		}

		System.out.printf("\n");
	}
	
	public String toString()
	{
		String s = String.format("%s: Code=%d Instructions=%d Cycles=%d Energy=%fnj Store/Load=%d, loadImmediateZero=%d, aloadImmediate=%d, Add/Sub=%d, longBranch=%d, IDat=%d UDat=%d IDpD=%d UDpD=%d",
					sectionName, 
					objectCode.size(),
					instructionLineCount,
					cycles,
					power * 1000000000,
					storeLoadInstructionCollapseCount,
					loadAccumulatorImmediateZeroRegisterInstructionCollapseCount,
					loadAccumulatorImmediateRegisterInstructionCollapseCount,
					addSubtractAccumulatorImmediateRegisterInstructionCollapseCount,
					longBranchReductionCount,
					initializedData.size(),
					uninitializedDataCount,
					initializedDPData.size(),
					uninitializedDPDataCount);

		return s;
	}
	
	public void emit(Assembly asm, AsmLine line, int value)
	{
		line.bytes.add( new Integer(value & 0xFF) );
		asm.currentSection.counter++;

		switch (currentSection.type)
		{
			case Code:
				objectCode.add(value);
				break;

			case Data:
				initializedData.add(value);
				break;

			case DPData:
				initializedDPData.add(value);
				break;
		}
	}

	public void eword(Assembly asm, AsmLine line, int value)
	{
		emit(asm, line, (value >> 8) & 0xFF);
		emit(asm, line, (value >> 0) & 0xFF);
	}

	public void equad(Assembly asm, AsmLine line, int value)
	{
		emit(asm, line, (value >> 24) & 0xFF);
		emit(asm, line, (value >> 16) & 0xFF);
		emit(asm, line, (value >>  8) & 0xFF);
		emit(asm, line, (value >>  0) & 0xFF);
	}
}
