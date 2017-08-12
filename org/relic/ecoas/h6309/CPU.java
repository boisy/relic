package org.relic.ecoas.h6309;

import java.util.EnumSet;
import java.util.Iterator;
import org.relic.ecoas.util.*;

// Addressing mode for operand
enum OperandMode
{
	Inherent,			// no operand
	Immediate,			// #33  or  #$44  or  #BELL
	Immediate8,
	Indirect,
	Extended,			// $00CA  or  25  or  CAT
	ExtendedIndirect,	// [CAT]  or  [$FFFE]
	Direct,				// 32  or  $44  (8 bytes of opcode, DP is upper 8, < forces)
	Register,			// tfr x,y   or  exg a,b  or  pshs a,b,x
	Indexed,			// 0,x  or  ,s  or  BOO,y
	AccumulatorIndexed,	// B,Y  or  D,Y  or  B,X
	AutoIncDecIndexed,	// ,X+  or  ,Y++  or  ,-Y  or  ,--S
	IndexedIndirect,	// [$10,X]  or  [,X++]  or  [B,Y]
	Other,
	None
};

public class CPU
{
	public enum CCF {H, N, Z, V, C, E, F, I};
	public enum RGS {D, X, Y, U, S, PC, W, V, A, B, CC, DP, E, F};
	double clockPeriod;

	final int PAGE2   = 0x10;
	final int PAGE3   = 0x11;
	final int IPBYTE  = 0x9F;	// extended indirect postbyte
	final int SWI     = 0x3F;

	// Registers
	Register D  = new Register("D", 0);
	Register X  = new Register("X", 1);
	Register Y  = new Register("Y", 2);
	Register U  = new Register("U", 3);
	Register S  = new Register("S", 4);
	Register PC = new Register("PC", 5);
	Register W  = new Register("W", 6);
	Register V  = new Register("V", 7);
	Register A  = new Register("A", 8);
	Register B  = new Register("B", 9);
	Register CC = new Register("CC", 10);
	Register DP = new Register("DP", 11);
	Register ZERO = new Register("ZERO", 12);
	Register E  = new Register("E", 14);
	Register F  = new Register("F", 15);
	Register T  = new Register("T", 16);
	
	public static InstructionMap 		instructionTable;
	ExpressionParser	parser;
	Boolean nativeMode;
	
	public class H6309Instruction extends Instruction
	{
		public int cy68, cy63, rmaflag;
		public Boolean canAlterFlow;
		public EnumSet<CCF> set, read;
		public double power;
		
		public H6309Instruction(String mnemonic, InstructionClass iClass, int opcode, int cycles68, int cycles63, InstructionCallback cb, double power, OperandClass oClass, Boolean canAlterFlow, EnumSet<CCF> set, EnumSet<CCF> read)
		{
			super(mnemonic, iClass, opcode, oClass, cb, AssemblerSection.Type.Code);
			
			this.cy68 = cycles68;
			this.cy63 = cycles63;
			this.power = power * clockPeriod;
			this.canAlterFlow = canAlterFlow;
			this.set = set;
			this.read = read;
		}
	}

	public CPU(Boolean nativeMode, double clockPeriod)
	{
		this.nativeMode = nativeMode;
		this.clockPeriod = clockPeriod;
		parser = new ExpressionParser();
		instructionTable = new InstructionMap();
		
		//								MNEMONIC						CLASS						BASE    68C	63C	CLASS	, 		WATTS	  	OPERAND_FLAG					CHGFLOW	SETS	READS
		instructionTable.put("abx",	 	new H6309Instruction("abx",		InstructionClass.INH,		0x3A,   3,	1,	new _inh(),		.011899,	OperandClass.HAS_NO_OPERAND,	false,	null,	null));
		instructionTable.put("adca", 	new H6309Instruction("adca",	InstructionClass.GEN,		0x89,	2,	2,	new _gen(),		.011899, 	OperandClass.HAS_OPERAND, 		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("adcb", 	new H6309Instruction("adcb",	InstructionClass.GEN,		0xC9,	2,	2,	new _gen(),		.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("adcd", 	new H6309Instruction("adcd",	InstructionClass.P2GEN,		0x89,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("adcr", 	new H6309Instruction("adcr",	InstructionClass.P2RTOR,	0x31,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND, 		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("adda", 	new H6309Instruction("adda",	InstructionClass.GEN,		0x8B,   2,	2,	new _gen(),		.011899,	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("addb", 	new H6309Instruction("addb",	InstructionClass.GEN,		0xCB,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("addd", 	new H6309Instruction("addd",	InstructionClass.LONGIMM,	0xC3,   4,	3,	new _longimm(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("adde", 	new H6309Instruction("adde",	InstructionClass.P3GEN,		0x8B,   3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("addf", 	new H6309Instruction("addf",	InstructionClass.P3GEN,		0xCB,	3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("addr", 	new H6309Instruction("addr",	InstructionClass.P2RTOR,	0x30,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("addw", 	new H6309Instruction("addw",	InstructionClass.P2GEN,		0x8B,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("aim",	 	new H6309Instruction("aim",		InstructionClass.GEN,		0x02,	6,	6,	new _imgen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("anda", 	new H6309Instruction("anda",	InstructionClass.GEN,		0x84,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));	
		instructionTable.put("andb", 	new H6309Instruction("andb",	InstructionClass.GEN,		0xC4,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("andcc",	new H6309Instruction("andcc",	InstructionClass.IMM,		0x1C,   3,	3,	new _imm(),		.011899,   	OperandClass.HAS_OPERAND, 		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("andd", 	new H6309Instruction("andd",	InstructionClass.P2GEN,		0x84,	5,	4,	new _p2gen(),	.011899, 	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("andr",	new H6309Instruction("andr",	InstructionClass.P2RTOR,	0x34,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND, 		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("asl",		new H6309Instruction("asl",		InstructionClass.GRP2,		0x08,	6,	6,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND, 		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("asla",	new H6309Instruction("asla",	InstructionClass.INH,		0x48,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND, 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("aslb",	new H6309Instruction("aslb",	InstructionClass.INH,		0x58,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND, 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("asld",	new H6309Instruction("asld",	InstructionClass.INH,		0x48,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND, 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("asr",		new H6309Instruction("asr",		InstructionClass.GRP2,		0x07,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND, 		false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("asra",	new H6309Instruction("asra",	InstructionClass.INH,		0x47,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("asrb",	new H6309Instruction("asrb",	InstructionClass.INH,		0x57,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("asrd",	new H6309Instruction("asrd",	InstructionClass.P2INH,		0x47,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("band",	new H6309Instruction("band",	InstructionClass.P3GEN,		0x30,	7,	6,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("bcc",		new H6309Instruction("bcc",		InstructionClass.REL,		0x24,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND, 		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("bcs",		new H6309Instruction("bcs",		InstructionClass.REL,		0x25,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND, 		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("beor",	new H6309Instruction("beor",	InstructionClass.P3GEN,		0x34,	7,	6,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("beq",		new H6309Instruction("beq",		InstructionClass.REL,		0x27,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND, 		true,	null, EnumSet.of(CCF.Z)));
		instructionTable.put("bge",		new H6309Instruction("bge",		InstructionClass.REL,		0x2C,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND, 		true,	null, EnumSet.of(CCF.N, CCF.V)));
		instructionTable.put("bgt",		new H6309Instruction("bgt",		InstructionClass.REL,		0x2E,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND, 		true,	null, EnumSet.of(CCF.N, CCF.V, CCF.Z)));
		instructionTable.put("bhi",		new H6309Instruction("bhi",		InstructionClass.REL,		0x22,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND, 		true,	null, EnumSet.of(CCF.C, CCF.Z)));
		instructionTable.put("bhs",		new H6309Instruction("bhs",		InstructionClass.REL,		0x24,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND, 		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("biand",	new H6309Instruction("biand",	InstructionClass.P3GEN,		0x31,	7,	6,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("bieor",	new H6309Instruction("bieor",	InstructionClass.P3GEN,		0x35,	7,	6,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("bior",	new H6309Instruction("bior",	InstructionClass.P3GEN,		0x33,	7,	6,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("bita",	new H6309Instruction("bita",	InstructionClass.GEN,		0x85,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("bitb",	new H6309Instruction("bitb",	InstructionClass.GEN,		0xC5,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("bitd",	new H6309Instruction("bitd",	InstructionClass.P2GEN,		0x85,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("bitmd",	new H6309Instruction("bitmd",	InstructionClass.P3IMM,		0x3C,	4,	4,	new _p3imm(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ble",		new H6309Instruction("ble",		InstructionClass.REL,		0x2F,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N, CCF.Z, CCF.V)));
		instructionTable.put("blo",		new H6309Instruction("blo",		InstructionClass.REL,		0x25,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("bls",		new H6309Instruction("bls",		InstructionClass.REL,		0x23,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C, CCF.Z)));
		instructionTable.put("blt",		new H6309Instruction("blt",		InstructionClass.REL,		0x2D,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N, CCF.V)));
		instructionTable.put("bmi",		new H6309Instruction("bmi",		InstructionClass.REL,		0x2B,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N)));
		instructionTable.put("bne",		new H6309Instruction("bne",		InstructionClass.REL,		0x26,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.Z)));
		instructionTable.put("bor",		new H6309Instruction("bor",		InstructionClass.P3GEN,		0x32,	7,	6,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("bpl",		new H6309Instruction("bpl",		InstructionClass.REL,		0x2A,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N)));
		instructionTable.put("bra",		new H6309Instruction("bra",		InstructionClass.REL,		0x20,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("brn",		new H6309Instruction("brn",		InstructionClass.REL,		0x21,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("bsr",		new H6309Instruction("bsr",		InstructionClass.REL,		0x8D,   7,	6,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("bvc",		new H6309Instruction("bvc",		InstructionClass.REL,		0x28,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.V)));
		instructionTable.put("bvs",		new H6309Instruction("bvs",		InstructionClass.REL,		0x29,	3,	3,	new _rel(),		.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.V)));
		instructionTable.put("clr",		new H6309Instruction("clr",		InstructionClass.GRP2,		0x0F,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("clra",	new H6309Instruction("clra",	InstructionClass.INH,		0x4F,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("clrb",	new H6309Instruction("clrb",	InstructionClass.INH,		0x5F,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("clrd",	new H6309Instruction("clrd",	InstructionClass.P2INH,		0x4F,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("clre",	new H6309Instruction("clre",	InstructionClass.P3INH,		0x4F,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("clrf",	new H6309Instruction("clrf",	InstructionClass.P3INH,		0x5F,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("clrw",	new H6309Instruction("clrw",	InstructionClass.P2INH,		0x5F,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpa",	new H6309Instruction("cmpa",	InstructionClass.GEN,		0x81,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpb",	new H6309Instruction("cmpb",	InstructionClass.GEN,		0xC1,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpd",	new H6309Instruction("cmpd",	InstructionClass.P2GEN,		0x83,   5,	4,	new _p2gen(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpe",	new H6309Instruction("cmpe",	InstructionClass.P3GEN,		0x81,   3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpf",	new H6309Instruction("cmpf",	InstructionClass.P3GEN,		0xC1,   3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpr",	new H6309Instruction("cmpr",	InstructionClass.P2RTOR,	0x37,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("cmps",	new H6309Instruction("cmps",	InstructionClass.P3GEN,		0x8C,   5,	4,	new _p3gen(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpu",	new H6309Instruction("cmpu",	InstructionClass.P3GEN,		0x83,   5,	4,	new _p3gen(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpw",	new H6309Instruction("cmpw",	InstructionClass.P2GEN,		0x81,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpx",	new H6309Instruction("cmpx",	InstructionClass.LONGIMM,	0x8C,   4,	3,	new _longimm(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cmpy",	new H6309Instruction("cmpy",	InstructionClass.P2GEN,		0x8C,   5,	4,	new _p2gen(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("com",		new H6309Instruction("com",		InstructionClass.GRP2,		0x03,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("coma",	new H6309Instruction("coma",	InstructionClass.INH,		0x43,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("comb",	new H6309Instruction("comb",	InstructionClass.INH,		0x53,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("comd",	new H6309Instruction("comd",	InstructionClass.P2INH,		0x43,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("come",	new H6309Instruction("come",	InstructionClass.P3INH,		0x43,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("comf",	new H6309Instruction("comf",	InstructionClass.P3INH,		0x53,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("comw",	new H6309Instruction("comw",	InstructionClass.P2INH,		0x53,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("cpx",		new H6309Instruction("cpx",		InstructionClass.LONGIMM,	0x8C,   4,	3,	new _longimm(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null)); /* for compatibility with old code */
		instructionTable.put("cwai",	new H6309Instruction("cwai",	InstructionClass.IMM,		0x3C,   22,	20,	new _imm(),		.011899,   	OperandClass.HAS_OPERAND, 		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("daa",		new H6309Instruction("daa",		InstructionClass.INH,		0x19,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("dec",		new H6309Instruction("dec",		InstructionClass.GRP2,		0x0A,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("deca",	new H6309Instruction("deca",	InstructionClass.INH,		0x4A,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("decb",	new H6309Instruction("decb",	InstructionClass.INH,		0x5A,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("decd",	new H6309Instruction("decd",	InstructionClass.P2INH,		0x4A,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("dece",	new H6309Instruction("dece",	InstructionClass.P3INH,		0x4A,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("decf",	new H6309Instruction("decf",	InstructionClass.P3INH,		0x5A,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("decw",	new H6309Instruction("decw",	InstructionClass.P2INH,		0x5A,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("divd",	new H6309Instruction("divd",	InstructionClass.P3GEN,		0x8D,	25,	25,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("divq",	new H6309Instruction("divq",	InstructionClass.P3GEN,		0x8E,	36,	36,	new _p3gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("eim",		new H6309Instruction("eim",		InstructionClass.GEN,		0x05,	6,	6,	new _imgen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("emubrk",	new H6309Instruction("emubrk",	InstructionClass.P3INH,		0xFC,	0,	0,	new _p3inh(),	.011899,   	OperandClass.HAS_OPERAND,		false,	null, null));
		instructionTable.put("eora",	new H6309Instruction("eora",	InstructionClass.GEN,		0x88,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("eorb",	new H6309Instruction("eorb",	InstructionClass.GEN,		0xC8,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("eord",	new H6309Instruction("eord",	InstructionClass.P2GEN,		0x88,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("eorr",	new H6309Instruction("eorr",	InstructionClass.P2RTOR,	0x36,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("exg",		new H6309Instruction("exg",		InstructionClass.RTOR,		0x1E,   8,	5,	new _rtor(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("inc",		new H6309Instruction("inc",		InstructionClass.GRP2,		0x0C,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		true,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("inca",	new H6309Instruction("inca",	InstructionClass.INH,		0x4C,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("incb",	new H6309Instruction("incb",	InstructionClass.INH,		0x5C,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("incd",	new H6309Instruction("incd",	InstructionClass.P2INH,		0x4C,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ince",	new H6309Instruction("ince",	InstructionClass.P3INH,		0x4C,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("incf",	new H6309Instruction("incf",	InstructionClass.P3INH,		0x5C,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("incw",	new H6309Instruction("incw",	InstructionClass.P2INH,		0x5C,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("jmp",		new H6309Instruction("jmp",		InstructionClass.GRP2,		0x0E,   3,	2,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("jsr",		new H6309Instruction("jsr",		InstructionClass.NOIMM,		0x8D,   7,	6,	new _noimm(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("lbcc",	new H6309Instruction("lbcc",	InstructionClass.P2REL,		0x24,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("lbcs",	new H6309Instruction("lbcs",	InstructionClass.P2REL,		0x25,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("lbeq",	new H6309Instruction("lbeq",	InstructionClass.P2REL,		0x27,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.Z)));
		instructionTable.put("lbge",	new H6309Instruction("lbge",	InstructionClass.P2REL,		0x2C,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N, CCF.V)));
		instructionTable.put("lbgt",	new H6309Instruction("lbgt",	InstructionClass.P2REL,		0x2E,	6,	6,	new _p2rel(),	.011899,  	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N, CCF.V, CCF.Z)));
		instructionTable.put("lbhi",	new H6309Instruction("lbhi",	InstructionClass.P2REL,		0x22,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C, CCF.Z)));
		instructionTable.put("lbhs",	new H6309Instruction("lbhs",	InstructionClass.P2REL,		0x24,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("lble",	new H6309Instruction("lble",	InstructionClass.P2REL,		0x2F,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N, CCF.V, CCF.Z)));
		instructionTable.put("lblo",	new H6309Instruction("lblo",	InstructionClass.P2REL,		0x25,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C)));
		instructionTable.put("lbls",	new H6309Instruction("lbls",	InstructionClass.P2REL,		0x23,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.C, CCF.Z)));
		instructionTable.put("lblt",	new H6309Instruction("lblt",	InstructionClass.P2REL,		0x2B,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N, CCF.V)));
		instructionTable.put("lbmi",	new H6309Instruction("lbmi",	InstructionClass.P2REL,		0x2B,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N)));
		instructionTable.put("lbne",	new H6309Instruction("lbne",	InstructionClass.P2REL,		0x26,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.Z)));
		instructionTable.put("lbpl",	new H6309Instruction("lbpl",	InstructionClass.P2REL,		0x2A,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.N)));
		instructionTable.put("lbra",	new H6309Instruction("lbra",	InstructionClass.P1REL,		0x16,   5,	4,	new _p1rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("lbrn",	new H6309Instruction("lbrn",	InstructionClass.P2REL,		0x21,   5,	5,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("lbsr",	new H6309Instruction("lbsr",	InstructionClass.P1REL,		0x17,   9,	7,	new _p1rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, null));
		instructionTable.put("lbvc",	new H6309Instruction("lbvc",	InstructionClass.P2REL,		0x28,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.V)));
		instructionTable.put("lbvs",	new H6309Instruction("lbvs",	InstructionClass.P2REL,		0x29,	6,	6,	new _p2rel(),	.011899,   	OperandClass.HAS_OPERAND,		true,	null, EnumSet.of(CCF.V)));
		instructionTable.put("lda",		new H6309Instruction("lda",		InstructionClass.GEN,		0x86,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldb",		new H6309Instruction("ldb",		InstructionClass.GEN,		0xC6,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldbt",	new H6309Instruction("ldbt",	InstructionClass.P3GEN,		0x36,	7,	6,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("ldd",		new H6309Instruction("ldd",		InstructionClass.LONGIMM,	0xCC,   3,	3,	new _longimm(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("lde",		new H6309Instruction("lde",		InstructionClass.P3GEN,		0x86,	3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldf",		new H6309Instruction("ldf",		InstructionClass.P3GEN,		0xC6,	3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldmd",	new H6309Instruction("ldmd",	InstructionClass.P3IMM,		0x3D,	5,	5,	new _p3imm(),	.011899,  	OperandClass.HAS_OPERAND,		false,	null, null));
		instructionTable.put("ldq",		new H6309Instruction("ldq",		InstructionClass.P2GEN,		0xDC,	5,	5,	new _ldqgen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("lds",		new H6309Instruction("lds",		InstructionClass.P2GEN,		0xCE,   4,	4,	new _p2gen(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldu",		new H6309Instruction("ldu",		InstructionClass.LONGIMM,	0xCE,   3,	3,	new _longimm(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldw",		new H6309Instruction("ldw",		InstructionClass.P2GEN,		0x86,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldx",		new H6309Instruction("ldx",		InstructionClass.LONGIMM,	0x8E,   3,	3,	new _longimm(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ldy",		new H6309Instruction("ldy",		InstructionClass.P2GEN,		0x8E,   5,	4,	new _p2gen(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("leas",	new H6309Instruction("leas",	InstructionClass.INDEXED,	0x32,   4,	4,	new _indexed(),	.011899,   	OperandClass.HAS_OPERAND,		false,	null, null));
		instructionTable.put("leau",	new H6309Instruction("leau",	InstructionClass.INDEXED,	0x33,   4,	4,	new _indexed(),	.011899,   	OperandClass.HAS_OPERAND,		false,	null, null));
		instructionTable.put("leax",	new H6309Instruction("leax",	InstructionClass.INDEXED,	0x30,   4,	4,	new _indexed(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.Z), null));
		instructionTable.put("leay",	new H6309Instruction("leay",	InstructionClass.INDEXED,	0x31,   4,	4,	new _indexed(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.Z), null));
		instructionTable.put("lsl",		new H6309Instruction("lsl",		InstructionClass.GRP2,		0x08,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("lsla",	new H6309Instruction("lsla",	InstructionClass.INH,		0x48,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("lslb",	new H6309Instruction("lslb",	InstructionClass.INH,		0x58,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("lsld",	new H6309Instruction("lsld",	InstructionClass.P2INH,		0x48,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("lsr",		new H6309Instruction("lsr",		InstructionClass.GRP2,		0x04,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("lsra",	new H6309Instruction("lsra",	InstructionClass.INH,		0x44,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("lsrb",	new H6309Instruction("lsrb",	InstructionClass.INH,		0x54,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("lsrd",	new H6309Instruction("lsrd",	InstructionClass.P2INH,		0x44,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("lsrw",	new H6309Instruction("lsrw",	InstructionClass.P2INH,		0x54,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("mul",		new H6309Instruction("mul",		InstructionClass.INH,		0x3D,   11,	10,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.Z, CCF.C), null));
		instructionTable.put("muld",	new H6309Instruction("muld",	InstructionClass.P3GEN,		0x8F,	28,	28,	new _p3gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), null));
		instructionTable.put("neg",		new H6309Instruction("neg",		InstructionClass.GRP2,		0x00,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("nega",	new H6309Instruction("nega",	InstructionClass.INH,		0x40,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("negb",	new H6309Instruction("negb",	InstructionClass.INH,		0x50,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("negd",	new H6309Instruction("negd",	InstructionClass.P2INH,		0x40,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("nop",		new H6309Instruction("nop",		InstructionClass.INH,		0x12,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	null, null));
		instructionTable.put("oim",		new H6309Instruction("oim",		InstructionClass.GEN,		0x01,	6,	6,	new _imgen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ora",		new H6309Instruction("ora",		InstructionClass.GEN,		0x8A,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("orb",		new H6309Instruction("orb",		InstructionClass.GEN,		0xCA,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("orcc",	new H6309Instruction("orcc",	InstructionClass.IMM,		0x1A,   3,	2,	new _imm(),		.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("ord",		new H6309Instruction("ord",		InstructionClass.P2GEN,		0x8A,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("orr",		new H6309Instruction("orr",		InstructionClass.P2RTOR,	0x35,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("os9",		new H6309Instruction("os9",		InstructionClass.SYS,		0x3F,   20,	22,	new _sys(),		.011899,   	OperandClass.HAS_OPERAND,		true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("pshs",	new H6309Instruction("pshs",	InstructionClass.RLIST,		0x34,   5,	4,	new _rlist(),	.011899,   	OperandClass.HAS_OPERAND,		false,	null, null));
		instructionTable.put("pshsw",	new H6309Instruction("pshsw",	InstructionClass.P2INH,		0x38,	6,	6,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	null, null));
		instructionTable.put("pshu",	new H6309Instruction("pshu",	InstructionClass.RLIST,		0x36,   5,	4,	new _rlist(),	.011899,   	OperandClass.HAS_OPERAND,		false,	null, null));
		instructionTable.put("pshuw",	new H6309Instruction("pshuw",	InstructionClass.P2INH,		0x3A,	6,	6,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	null, null));
		instructionTable.put("puls",	new H6309Instruction("puls",	InstructionClass.RLIST,		0x35,   5,	4,	new _rlist(),	.011899,   	OperandClass.HAS_OPERAND,		true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("pulsw",	new H6309Instruction("pulsw",	InstructionClass.P2INH,		0x39,	6,	6,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND, 	false,	null, null));
		instructionTable.put("pulu",	new H6309Instruction("pulu",	InstructionClass.RLIST,		0x37,   5,	4,	new _rlist(),	.011899,   	OperandClass.HAS_OPERAND, 		true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("puluw",	new H6309Instruction("puluw",	InstructionClass.P2INH,		0x3B,	6,	6,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	null, null));
		instructionTable.put("rol",		new H6309Instruction("rol",		InstructionClass.GRP2,		0x09,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("rola",	new H6309Instruction("rola",	InstructionClass.INH,		0x49,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("rolb",	new H6309Instruction("rolb",	InstructionClass.INH,		0x59,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("rold",	new H6309Instruction("rold",	InstructionClass.P2INH,		0x49,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("rolw",	new H6309Instruction("rolw",	InstructionClass.P2INH,		0x59,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.V, CCF.C)));
		instructionTable.put("ror",		new H6309Instruction("ror",		InstructionClass.GRP2,		0x06,	6,	5,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("rora",	new H6309Instruction("rora",	InstructionClass.INH,		0x46,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("rorb",	new H6309Instruction("rorb",	InstructionClass.INH,		0x56,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("rord",	new H6309Instruction("rord",	InstructionClass.P2INH,		0x46,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("rorw",	new H6309Instruction("rorw",	InstructionClass.P2INH,		0x56,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("rti",		new H6309Instruction("rti",		InstructionClass.INH,		0x3B,   15,	17,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), null));
		instructionTable.put("rts",		new H6309Instruction("rts",		InstructionClass.INH,		0x39,   5,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	true,	null, null));
		instructionTable.put("sbca",	new H6309Instruction("sbca",	InstructionClass.GEN,		0x82,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("sbcb",	new H6309Instruction("sbcb",	InstructionClass.GEN,		0xC2,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("sbcd",	new H6309Instruction("sbcd",	InstructionClass.P2GEN,		0x82,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), EnumSet.of(CCF.C)));
		instructionTable.put("sbcr",	new H6309Instruction("sbcr",	InstructionClass.P2RTOR,	0x33,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.C)));
		instructionTable.put("sex",		new H6309Instruction("sex",		InstructionClass.INH,		0x1D,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z), null));
		instructionTable.put("sexw",	new H6309Instruction("sexw",	InstructionClass.INH,		0x14,	4,	4,	new _inh(),		.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z), null));
		instructionTable.put("sta",		new H6309Instruction("sta",		InstructionClass.NOIMM,		0x87,   4,	3,	new _noimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("stb",		new H6309Instruction("stb",		InstructionClass.NOIMM,		0xC7,   4,	3,	new _noimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("stbt",	new H6309Instruction("stbt",	InstructionClass.P3GEN,		0x37,	8,	7,	new _bitgen(),	.011899,  	OperandClass.HAS_OPERAND,		false,	null, EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("std",		new H6309Instruction("std",		InstructionClass.NOIMM,		0xCD,   5,	4,	new _noimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("ste",		new H6309Instruction("ste",		InstructionClass.P3NOIMM,	0x87,	5,	4,	new _p3noimm(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("stf",		new H6309Instruction("stf",		InstructionClass.P3NOIMM,	0xC7,	5,	4,	new _p3noimm(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("stq",		new H6309Instruction("stq",		InstructionClass.P2NOIMM,	0xCD,	8,	7,	new _p2noimm(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("sts",		new H6309Instruction("sts",		InstructionClass.P2NOIMM,	0xCF,   6,	5,	new _p2noimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("stu",		new H6309Instruction("stu",		InstructionClass.NOIMM,		0xCF,   5,	4,	new _noimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("stw",		new H6309Instruction("stw",		InstructionClass.P2NOIMM,	0x87,	6,	5,	new _p2noimm(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("stx",		new H6309Instruction("stx",		InstructionClass.NOIMM,		0x8F,   5,	4,	new _noimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("sty",		new H6309Instruction("sty",		InstructionClass.P2NOIMM,	0x8F,   6,	5,	new _p2noimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("suba",	new H6309Instruction("suba",	InstructionClass.GEN,		0x80,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("subb",	new H6309Instruction("subb",	InstructionClass.GEN,		0xC0,	2,	2,	new _gen(),		.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("subd",	new H6309Instruction("subd",	InstructionClass.LONGIMM,	0x83,   4,	3,	new _longimm(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("sube",	new H6309Instruction("sube",	InstructionClass.P3GEN,		0x80,	3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("subf",	new H6309Instruction("subf",	InstructionClass.P3GEN,		0xC0,	3,	3,	new _p3gen8(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("subr",	new H6309Instruction("subr",	InstructionClass.P2RTOR,	0x32,	4,	4,	new _p2rtor(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("subw",	new H6309Instruction("subw",	InstructionClass.P2GEN,		0x80,	5,	4,	new _p2gen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V, CCF.C), null));
		instructionTable.put("swi",		new H6309Instruction("swi",		InstructionClass.INH,		0x3F,   19,	21,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("swi2",	new H6309Instruction("swi2",	InstructionClass.P2INH,		0x3F,   20,	22,	new _p2inh(),	.011899,   	OperandClass.HAS_NO_OPERAND,	true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("swi3",	new H6309Instruction("swi3",	InstructionClass.P3INH,		0x3F,   20,	22,	new _p3inh(),	.011899,   	OperandClass.HAS_NO_OPERAND,	true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("sync",	new H6309Instruction("sync",	InstructionClass.INH,		0x13,   2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	null, null));
		instructionTable.put("tfm",		new H6309Instruction("tfm",		InstructionClass.P3RTOR,	0x38,	6,	6,	new _p3rtor(),	.011899,  	OperandClass.HAS_OPERAND,		false,	EnumSet.of(CCF.Z), null));
		instructionTable.put("tfr",		new H6309Instruction("tfr",		InstructionClass.RTOR,		0x1F,   6,	4,	new _rtor(),	.011899,   	OperandClass.HAS_OPERAND,	 	true,	EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I), EnumSet.of(CCF.H, CCF.N, CCF.Z, CCF.V, CCF.C, CCF.E, CCF.F, CCF.I)));
		instructionTable.put("tim",		new H6309Instruction("tim",		InstructionClass.GEN,		0x0B,	6,	6,	new _imgen(),	.011899,  	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("tst",		new H6309Instruction("tst",		InstructionClass.GRP2,		0x0D,	6,	4,	new _grp2(),	.011899,   	OperandClass.HAS_OPERAND,	 	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("tsta",	new H6309Instruction("tsta",	InstructionClass.INH,		0x4D,	2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("tstb", 	new H6309Instruction("tstb",	InstructionClass.INH,		0x5D,   2,	1,	new _inh(),		.011899,   	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("tstd",	new H6309Instruction("tstd",	InstructionClass.P2INH,		0x4D,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("tste",	new H6309Instruction("tste",	InstructionClass.P3INH,		0x4D,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("tstf",	new H6309Instruction("tstf",	InstructionClass.P3INH,		0x5D,	3,	2,	new _p3inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
		instructionTable.put("tstw",	new H6309Instruction("tstw",	InstructionClass.P2INH,		0x5D,	3,	2,	new _p2inh(),	.011899,  	OperandClass.HAS_NO_OPERAND,	false,	EnumSet.of(CCF.N, CCF.Z, CCF.V), null));
	}
	
	class _p3rtor implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
		}
	}

	class _inh implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			/* Emit opcode. */
			asm.emit(asm, line, i.opcode);
	
			return;
		}
	}
	
	class _p2inh implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			/* Emit opcode. */
			asm.emit(asm, line, PAGE2);
	
			_inh ii = new _inh();
			ii.process(asm, line, i);
			
			return;
		}
	}

	class _p3inh implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			/* Emit opcode. */
			asm.emit(asm, line, PAGE3);
	
			_inh ii = new _inh();
			ii.process(asm, line, i);
			
			return;
		}
	}		

	class _gen implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			OperandMode amode = addressingMode(line);
			
			if (amode == OperandMode.None)
			{
				return;
			}
			
			/* Do general addressing */
			do_gen(asm, line, i, amode, false);
			
			return;
		}
	}
		
	class _imgen implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int	result = 0;
			String operand = line.operand;
			OperandMode amode = addressingMode(line);
	
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			/* Verify immediate addressing. */
			if (amode == OperandMode.Immediate)
			{
				operand = operand.substring(1);
			}
			
			if (operand.indexOf(',') == -1)
			{
				line.error = "comma required between operands";
				return;
			}

			try
			{
				result = evaluate(asm, line, operand.substring(0, operand.indexOf(',')));
			}
			catch (ExpressionException e)
			{
				return;
			}
			setBranchType(line, Reference.BranchType.absolute);
			
			operand = operand.substring(operand.indexOf(','));		
			
			if (result > 255)
			{
				asm.emit(asm, line, line.lobyte(result));
				line.error = "result >255";
				return;
			}
	
			if (operand.charAt(0) == '[')
			{
				asm.emit(asm, line, i.opcode + 0x60);
				int o = i.opcode;
				i.opcode = result;
				do_indexed(asm, line, i);
				i.opcode = o;
				return;
			}
		
			amode = OperandMode.Other;		/* default */
	
			if (operand.indexOf(',') != -1)
			{
				amode = OperandMode.Indexed;    /* indexed addressing */
			}

			setOffsetAndSize(line, asm.currentSection.counter, 1);
			asm.emit(asm, line, line.lobyte(result));
		
			
			/* General addressing */
			do_gen(asm, line, i, amode, false);
		
			/* Fix up output */
	//		as->E_bytes[old] = as->E_bytes[old + 1];
	//		as->E_bytes[old + 1] = result;
	//		as->P_bytes[0] = as->P_bytes[1];
	//		as->P_bytes[1] = result;
		
	//		if ((as->P_bytes[0] & 0xf0) == 0x10)
	//		{
	//			as->E_bytes[old] &= 0x0f;
	//			as->P_bytes[0] &= 0x0f;
	//		}
	//		else
	//		{
	//			as->E_bytes[old] |= 0x40;
	//			as->P_bytes[0] |= 0x40;
	//		}
		
			return;
		}
	}
		
	class _imm implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int	result = 0;
			String operand = line.operand;		
			OperandMode amode = addressingMode(line);
		
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			/* Immediate addressing ONLY. */
			if (amode != OperandMode.Immediate)
			{
				line.error = "immediate operand required";
				return;
			}
		
			/* skip over # */
			operand = operand.substring(1);
	
			try
			{
				result = evaluate(asm, line, operand);
			}
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.absolute);

			asm.emit(asm, line, i.opcode);
		
			if (result > 255)
			{
				line.error = "result >255";
			}
		
			setOffsetAndSize(line, asm.currentSection.counter, 1);
			asm.emit(asm, line, line.lobyte(result));
		
			return;
		}
	}

	class _p3imm implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			asm.emit(asm, line, PAGE3);
			
			_imm ii = new _imm();
			ii.process(asm, line, i);
		}
	}

	class _rel implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int	result = 0;
			int dist = 0;
			String operand = line.operand;
		
			/* Short relative branches */
			try
			{
				result = evaluate(asm, line, operand);
			}
			catch (ExpressionException e)
			{
			}

			// BGP: NOTE!!! If result == 0, we ASSUME that the symbol wasn't found, NOT that we are branching to the next
			// instruction.  We really need to check to see if there was a symbol not found situation to cover the case where
			// someone might do:
			// bra x
			// x:
			dist = result - (line.offset + 2);
			if ((dist > 127 || dist < -128) && result != 0) // && as->pass == 2)
			{
				line.error = "branch out of range";
			}
			setBranchType(line, Reference.BranchType.pcr);
		
			asm.emit(asm, line, i.opcode);
			setOffsetAndSize(line, asm.currentSection.counter, 1);
			asm.emit(asm, line, line.lobyte(dist));
		
			return;
		}
	}
	
	class _p1rel implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int	result = 0;
			int dist = 0;
			String operand = line.operand;
			OperandMode amode = addressingMode(line);
		
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			/* lbra and lbsr */
			if (amode == OperandMode.Immediate)
			{
				operand = operand.substring(1); /* kludge for C compiler */
			}
		
			try
			{
				result = evaluate(asm, line, operand);
				dist = result - (line.offset + 3);
			}
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.pcr);
			
			asm.emit(asm, line, i.opcode);
			setOffsetAndSize(line, asm.currentSection.counter, 2);
			asm.eword(asm, line, dist);
			
			return;
		}
	}
	
	class _p2rel implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int	result = 0;
			int	dist = 0;
			String operand = line.operand;
		
			/* Long relative branches */
			try
			{
				result = evaluate(asm, line, operand);
				dist = result - (line.offset + 4);
//				if ((dist > -128) && (dist < 127))
				{
//					line.warning = "long branch used when short branch would suffice";
				}
			}
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.pcr);
			
			asm.emit(asm, line, PAGE2);
			asm.emit(asm, line, i.opcode);		
			setOffsetAndSize(line, asm.currentSection.counter, 2);
			asm.eword(asm, line, dist);
			
			return;
		}
	}
	
	class _noimm implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			OperandMode amode = addressingMode(line);
		
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			if (amode == OperandMode.Immediate)
			{
				line.error = "immediate addressing illegal";
			}
			
			do_gen(asm, line, i, amode, false);
		
			return;
		}
	}
		
	class _p2noimm implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			asm.emit(asm, line, PAGE2);
			
			_noimm ii = new _noimm();
			ii.process(asm, line, i);
		}
	}
		
	class _p3noimm implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			asm.emit(asm, line, PAGE3);
			
			_noimm ii = new _noimm();
			ii.process(asm, line, i);
		}
	}

	class _pxgen implements InstructionCallback
	{
		OperandMode amode;
		
		_pxgen(OperandMode amode)
		{
			this.amode = amode;
		}
		
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			if ((amode == OperandMode.Immediate) || (amode == OperandMode.Immediate8))
			{
				String operand = line.operand;				
				int	result = 0;

				asm.emit(asm, line, i.opcode);
				
				operand = operand.substring(1);
				StringBuffer error = new StringBuffer();
				try
				{
					result = evaluate(asm, line, operand);
				}
				catch (ExpressionException e)
				{
				}
				setBranchType(line, Reference.BranchType.absolute);
		
				if (amode == OperandMode.Immediate)
				{
					setOffsetAndSize(line, asm.currentSection.counter, 2);
					asm.eword(asm, line, result);
				}
				else
				{
					setOffsetAndSize(line, asm.currentSection.counter, 1);
					asm.emit(asm, line, line.lobyte(result));
				}
		
				return;
			}
		
			do_gen(asm, line, i, amode, false);
			
			return;
		}
	}
		
	class _ldqgen implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			String operand = line.operand;
			OperandMode amode = addressingMode(line);
			
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			if (amode == OperandMode.Immediate)
			{
				int result = 0;
		
				operand = operand.substring(1);
				try
				{
					result = evaluate(asm, line, operand);
				}
				catch (ExpressionException e)
				{
				}
				setBranchType(line, Reference.BranchType.absolute);
				asm.emit(asm, line, 0xcd);
				setOffsetAndSize(line, asm.currentSection.counter, 4);
				asm.emit(asm, line, (result >> 24) & 0xff);
				asm.emit(asm, line, (result >> 16) & 0xff);
				asm.emit(asm, line, (result >> 8) & 0xff);
				asm.emit(asm, line, result & 0xff);
		
				return;
			}
		
			_p2gen ii = new _p2gen();
			ii.process(asm, line, i);
			
			return;
		}
	}
		
	class _p2gen implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			OperandMode amode = addressingMode(line);
			
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			asm.emit(asm, line, PAGE2);
		
			_pxgen ii = new _pxgen(amode);
			ii.process(asm, line, i);
			
			return;
		}
	}

	class _p3gen implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			OperandMode amode = addressingMode(line);
			
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			asm.emit(asm, line, PAGE3);
		
			_pxgen ii = new _pxgen(amode);
			ii.process(asm, line, i);
			
			return;
		}
	}
	
	class _p3gen8 implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			OperandMode amode = addressingMode(line);
			
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			asm.emit(asm, line, PAGE3);
			
			if (amode == OperandMode.Immediate)
			{
				amode = OperandMode.Immediate8;
			}
			
			_pxgen ii = new _pxgen(amode);
			ii.process(asm, line, i);
			
			return;
		}
	}
			
	class _rtor implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			Register src, dst;
			int srcsz, dstsz;
			String operand = line.operand;
			
			/* tfr and exg */
			asm.emit(asm, line, i.opcode);
			
			if (operand.indexOf(',') == -1)
			{
				line.error = "destination register name required";
				asm.emit(asm, line, 0);
				return;
			}
			
			String regSource = operand.substring(0, operand.indexOf(','));
			String regDest = operand.substring(operand.indexOf(',') + 1);
			src = identifyRegister(regSource);
			dst = identifyRegister(regDest);
			
			if (src == null)
			{
				line.error = "source register name required";
				asm.emit(asm, line, 0);
				return;
			}
			
			if (dst == null)
			{
				line.error = "illegal destination register";
				asm.emit(asm, line, 0);
				return;
			}
			
			if (src == PC || dst == PC)
			{
				line.error = "PCR illegal here";
				asm.emit(asm, line, 0);
				return;
			}
		
			if (src == T) src = PC;	/* _DIRTY_ hack for T register */
			if (dst == T) dst = PC;	
			
			if (dst == ZERO)
			{
				line.error = "destination zero register is illegal";
				return;
			}
		
			srcsz = (((src.identifier & 8) > 0) && (src != PC)) ? 8 : 16;
			dstsz = (((dst.identifier & 8) > 0) && (dst != PC)) ? 8 : 16;
			if ((src == ZERO) && (dstsz == 8)) srcsz = 8;
		
			if ((srcsz != dstsz) && (i.opcode == 30)) /* EXG disallows R16->R8 */
			{
				line.error = "register size mismatch";
				asm.emit(asm, line, 0);
				return;
			}
	
			asm.emit(asm, line, (src.identifier << 4) + dst.identifier);
			
			return;
		}
	}
		
	class _p2rtor implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			asm.emit(asm, line, PAGE2);
		
			_rtor ii = new _rtor();
			ii.process(asm, line, i);
			
			return;
		}
	}
		
	class _indexed implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			OperandMode amode = addressingMode(line);
			
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			if (amode != OperandMode.Indexed)
			{
				line.error = "indexed addressing required";
				return;
			}
		
			do_indexed(asm, line, i);
			
			return;
		}
	}
		
	class _rlist implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			String operand = line.operand;
			int pbyte;
			Register j;
			/* convert tfr/exg reg number into psh/pul format */
			int     _regs[] = { 6,16,32,64,64,128,0,0,2,4,1,8,0};
			int     rcycl[]= { 2,2, 2, 2, 2, 2,  0,0,1,1,1,1,0};
			
			/* pushes and pulls */
			if (operand.length() == 0)
			{
				line.error = "register list required";
				return;
			}
		
			asm.emit(asm, line, i.opcode);
			pbyte = 0;
		
			do
			{
				String regName = operand;
				
				if (operand.indexOf(',') != -1)
				{
					regName = regName.substring(0, operand.indexOf(','));
					operand = operand.substring(operand.indexOf(',') + 1);
				}
				else
				{
					operand = null;
				}
				
				j = identifyRegister(regName);
				
				if (j == null)
				{
					line.error = "illegal register name";
					break;
				}
				
				/* check for valid registers which can be used in push/pull operations */
				if (!(j == PC || j == U || j == Y || j == X || j == DP || j == D || j == A || j == B ||  j == CC))
				{
					line.error = "illegal register name";
				}
				else if (j == S && (i.opcode == 52))
				{
					line.error = "can't push S on S";
				}
				else if (j == U && (i.opcode == 54))
				{
					line.error = "can't push U on U";
				}
				else if (j == S && (i.opcode == 53))
				{
					line.error = "can't pull S from S";
				}
				else if (j == U && (i.opcode == 55))
				{
					line.error = "can't pull U from U";
				}
				else
				{
					pbyte |= _regs[j.identifier];
					line.cycleCount += rcycl[j.identifier];
				}
			} while (operand != null);
		
			asm.emit(asm, line, line.lobyte(pbyte));
		
			return;
		}
	}
			
	class _longimm implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int result = 0;
			String operand = line.operand;
			OperandMode amode = addressingMode(line);
		
			if (amode == OperandMode.None)
			{
				return;
			}		
			
			if (amode == OperandMode.Immediate)
			{
				asm.emit(asm, line, i.opcode);
				operand = operand.substring(1);
				try
				{
					result = evaluate(asm, line, operand);
				}
				catch (ExpressionException e)
				{
				}
				setBranchType(line, Reference.BranchType.absolute);
				setOffsetAndSize(line, asm.currentSection.counter, 2);
				asm.eword(asm, line, result);
			}
			else
			{
				do_gen(asm, line, i, amode, false);
			}
		
			return;
		}
	}
			
	class _grp2 implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int result = 0;
			String operand = line.operand;
			OperandMode amode = addressingMode(line);
			
			if (amode == OperandMode.None)
			{
				return;
			}		
				
			if (amode == OperandMode.Indexed)
			{
				/* Indexed mode (i.e. $5,y) */
				i.opcode += 0x60;
				do_indexed(asm, line, i);
				i.opcode -= 0x60;
				return;
			}
			else
			if (amode == OperandMode.Indirect)
			{
				/* Indirect mode (i.e. [$FFFE]) */
				operand = operand.substring(1);
				int index = operand.indexOf(']');
				if (index == -1)
				{
					asm.eword(asm, line, 0);
					line.error = "missing ']'";
					return;
				}
				operand = operand.substring(0, index);
				asm.emit(asm, line, i.opcode + 0x60);
				asm.emit(asm, line, IPBYTE);
				try
				{
					result = evaluate(asm, line, operand);
				}
				catch (ExpressionException e)
				{
				}
				setBranchType(line, Reference.BranchType.absolute);
				setOffsetAndSize(line, asm.currentSection.counter, 2);
				asm.eword(asm, line, result);
				line.cycleCount += 7;
		
				return;
			}
		
			/* Evaluate result */
			try
			{
				result = evaluate(asm, line, operand);
			}
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.absolute);
			
			/* Check for inconsistency in force mode and DP */
			if (line.forceByte == true && line.hibyte(result) != DP.value)
			{
				line.error = "DP out of range";
				return;
			}
		
			if (line.forceWord == true || line.hibyte(result) != DP.value)
			{
				if ((line.hibyte(result) == DP.value))
				{
					line.warning = "DP is same as high byte";
				}
				
				asm.emit(asm, line, i.opcode + 0x70);
				setOffsetAndSize(line, asm.currentSection.counter, 2);
				asm.eword(asm, line, result);
				line.cycleCount += 3;
			}
			else
			{
				if (line.hibyte(result) != DP.value)
				{
					line.error = "DP out of range";
					return;
				}
				
				asm.emit(asm, line, i.opcode);		
				setOffsetAndSize(line, asm.currentSection.counter, 1);
				asm.emit(asm, line, line.lobyte(result));
				
				line.cycleCount += 2;
			}
			
			return;
		}
	}
	
	class _sys implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int	result = 0;
			String operand = line.operand;
			
			/* system call */
			asm.emit(asm, line, PAGE2);
			asm.emit(asm, line, i.opcode);
			try
			{
				result = evaluate(asm, line, operand);
			}
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.absolute);
			setOffsetAndSize(line, asm.currentSection.counter, 1);
			asm.emit(asm, line, line.lobyte(result));
		
			return;
		}
	}
	
	int evaluate(Assembly asm, AsmLine line, String expression) throws ExpressionException
	{
		if (expression.charAt(0) == '<')
		{
			line.forceByte = true;
			expression = expression.substring(1);
		}
		else
		if (expression.charAt(0) == '>')
		{
			line.forceWord = true;
			expression = expression.substring(1);
		}

		ReferenceTable references = new ReferenceTable();
		
		parser.setSymbolTable(asm.symbols);
		int result = 0;
		try
		{
			result = parser.evaluate(expression, references);
		}
		catch (ExpressionException e)
		{
		}
		
		// for each reference that the parser added, determine:
		// 1. if the symbol exists in the symbol table.  if so:
		//    1a. mark the reference as a local or external one depending upon the symbol's status
		// 2. If the symbol does not exist in the symbol table, then
		//    2a. mark the reference as an external one
		// 3. assign the local reference's type based on the section it is in (vsect, psect, csect)
		for (Iterator<Reference> i = references.iterator(); i.hasNext(); )
		{
			Reference r = i.next();
			
			Symbol s = asm.symbols.symbolForName(r.name); 

			if (s != null)
			{
				// since the symbol exists in the symbol table, the reference is local
				// if it is referencing code or a constant , remove it
				if (s.typeString().equals("code") || s.typeString().equals("cnst"))
				{
					i.remove();
				}
				else
				{
					r.external = false;
					r.localReference = s;
				}
			}
			else
			{
				// the symbol doesn't exist in the symbol table, so the reference must be external
				r.external = true;
				// we don't know what type of data this symbol will reference
				r.type = "code";
			}
		}

		// assign line's references to this one
		line.references.addAll(references);
		
		// copy the expression into the line's buffer for possible resolution later
		line.expression = new String(expression);

		if (line.error != null)
		{
			throw new ExpressionException(line.error);
		}
		
		return result;
	}

	void setBranchType(AsmLine line, Reference.BranchType branchType)
	{
		// for each reference, set its branch type to what was passed
		Iterator<Reference> it = line.references.iterator();
		
		while (it.hasNext())
		{
			Reference r = it.next();

			r.branchType = branchType;
		}
	}
	
	void setOffsetAndSize(AsmLine line, int offset, int size)
	{
		// for each reference, set its branch type and size to what was passed
		Iterator<Reference> it = line.references.iterator();
		
		while (it.hasNext())
		{
			Reference r = it.next();

			r.offset = offset;
			r.size = size;
		}
	}
	
	void do_gen(Assembly asm, AsmLine line, Instruction i, OperandMode mode, Boolean alwaysWord)
	{
		String operand = line.operand;
		int	result = 0;
	
		if (mode == OperandMode.Immediate)
		{
			/* Immediate addressing mode (i.e. #$123) */
			operand = operand.substring(1);
			asm.emit(asm, line, i.opcode);
			
			/* Evaluate the result. */
			try
			{
				result = evaluate(asm, line, operand);
			}
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.absolute);
			
			/* If the result is > 255, return error. */
			if (result > 255)
			{
				line.error = "result >255";
			}
			
			/* Emit the low byte result. */
			setOffsetAndSize(line, asm.currentSection.counter, 1);
			asm.emit(asm, line, line.lobyte(result));
	
			return;
		}
		else
		if (mode == OperandMode.Indexed)
		{
			/* Indexed mode (i.e. $5,y) */	
			i.opcode += 0x20;
			do_indexed(asm, line, i);
			i.opcode -= 0x20;
	
			return;
		}
		else
		if (mode == OperandMode.Indirect)
		{
			/* Indirect mode (i.e. [$FFFE] */
			operand = operand.substring(1);
			int index = operand.indexOf(']');
			if (index == -1)
			{
				asm.eword(asm, line, 0);
				line.error = "missing ']'";
				return;
			}
			operand = operand.substring(0, index);
	
			asm.emit(asm, line, i.opcode + 0x20);
			asm.emit(asm, line, IPBYTE);
	
			/* Evaluate. */
			try
			{
				result = evaluate(asm, line, operand);
			}			
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.absolute);

			/* Emit word. */
			setOffsetAndSize(line, asm.currentSection.counter, 2);
			asm.eword(asm, line, result);
	
			line.cycleCount += 7;
	
			return;
		}
		else
		if (mode == OperandMode.Other)
		{
			/* Evaluate result */
			try
			{
				result = evaluate(asm, line, operand);
			}
			catch (ExpressionException e)
			{
			}
			setBranchType(line, Reference.BranchType.absolute);
			
			if (line.forceByte == true)
			{
				/* Case #1: < has been prepended to expression */
				
				/* If we are still in pass 1, ignore DP check as there may
				 * be a forward reference
				 */
//				if (as->pass > 1 && line.hibyte(result) != as->DP)
				{
//					line.error = "DP out of range");
				}
//				else
				{
					asm.emit(asm, line, i.opcode + 0x10);
					setOffsetAndSize(line, asm.currentSection.counter, 1);
					asm.emit(asm, line, line.lobyte(result));
	
					line.cycleCount += 2;
				}
				
				return;
			}
			else
			if (line.forceWord == true)
			{
				/* Case #2: > has been prepended to expression */
				
				/* If we are still in pass 1, ignore DP check as there may
				 * be a forward reference
				 */
//				if (as->pass > 1 && line.hibyte(result) == as->DP)
				{
//					as->line.has_warning = 1;
				}
				
				asm.emit(asm, line, i.opcode + 0x30);
				setOffsetAndSize(line, asm.currentSection.counter, 2);
				asm.eword(asm, line, result);
	
				line.cycleCount += 3;
				
				return;
			}
			else
			{
				/* Case #3: Ambiguous... look to as->DP for guidance. */
//				if (line.hibyte(result) == DP.value && line.references.isEmpty() == true)
				if (line.references.isEmpty() || (line.references.isEmpty() == false && line.references.containsDPReference() == true))
				{
					asm.emit(asm, line, i.opcode + 0x10);
					setOffsetAndSize(line, asm.currentSection.counter, 1);
					asm.emit(asm, line, line.lobyte(result));
					
					line.cycleCount += 2;
				}
				else
				{
					asm.emit(asm, line, i.opcode + 0x30);
					setOffsetAndSize(line, asm.currentSection.counter, 2);
					asm.eword(asm, line, result);
					
					line.cycleCount += 3;
				}
				
				return;
			}
		}
		else
		{
			line.error = "unknown addressing mode";
			return;
		}
	}	

	void do_indexed(Assembly asm, AsmLine line, Instruction i)
	{
		String operand = line.operand;
		int     pbyte;
		Register j;
		int    k;
		int     predec,pstinc;
		int	result = 0, noOffset = 0;
	
		line.cycleCount += 2;    /* indexed is always 2+ base cycle count */
		predec = 0;
		pstinc = 0;
		pbyte = 128;
		asm.emit(asm, line, i.opcode);
	
		if (operand.charAt(0) == '[')
		{
			operand = operand.substring(1);
			int index = operand.indexOf(']');
			if (index == -1)
			{
				asm.eword(asm, line, 0);
				line.error = "missing ']'";
			}
			else
			{
				operand = operand.substring(0, operand.indexOf(']'));
			}
			pbyte |= 0x10;    /* set indirect bit */
				
			line.cycleCount += 3;    /* indirection takes this much longer */
		}
	
		j = identifyRegister(operand.substring(0, operand.indexOf(',')));
	
		if (j == A)
		{
			line.cycleCount++;
			String fixOperand = line.operand;
			line.operand = operand;
			abd_index(asm, line, pbyte + 6);
			line.operand = fixOperand;
	
			return;
		}
	
		if (j == B)
		{
			line.cycleCount++;
			String fixOperand = line.operand;
			line.operand = operand;
			abd_index(asm, line, pbyte + 5);
			line.operand = fixOperand;
	
			return;
		}
	
		if (j == D)
		{
			line.cycleCount += 4;
			String fixOperand = line.operand;
			line.operand = operand;
			abd_index(asm, line, pbyte + 11);
			line.operand = fixOperand;
	
			return;
		}
	
		if (j == E)
		{
			line.cycleCount++;
			String fixOperand = line.operand;
			line.operand = operand;
			abd_index(asm, line, pbyte + 7);
			line.operand = fixOperand;
	
			return;
		}
	
		if (j == F)
		{
			line.cycleCount++;
			String fixOperand = line.operand;
			line.operand = operand;
			abd_index(asm, line, pbyte + 10);
			line.operand = fixOperand;
	
			return;
		}
	
		if (j == W)
		{
			line.cycleCount += 4;
			String fixOperand = line.operand;
			line.operand = operand;
			abd_index(asm, line, pbyte + 14);
			line.operand = fixOperand;
	
			return;
		}
	
		/* check if operand's first char is ',' */
		if (operand.charAt(0) == ',' || (operand.charAt(0) == '0' && operand.charAt(1) == ','))
		{
			noOffset = 1;
			result = 0;
		}
		else
		{
			String expression = operand.substring(0, operand.indexOf(','));
			try
			{
				result = evaluate(asm, line, expression);
			}
			catch (ExpressionException e)
			{
				// in case we are referencing an undefined symbol, we set forceWord to true
				// so that subsequent passes don't throw off the counter
				line.forceWord = true;
			}
		}
		
		operand = operand.substring(operand.indexOf(',') + 1);
	
		while (operand.length() > 0 && operand.charAt(0) == '-')
		{
			predec++;
			operand = operand.substring(1);
		}
	
		if (operand.indexOf('+') != -1)
		{
			j = identifyRegister(operand.substring(0, operand.indexOf('+')));
		}
		else
		{
			j = identifyRegister(operand);
		}
		
		if (j == null)
		{
			line.error = "illegal register name";
			return;
		}
		
		operand = operand.substring(j.name.length());
	
		while (operand.length() > 0 && operand.charAt(0) == '+')
		{
			pstinc++;
			operand = operand.substring(1);
		}
	
		if (j == PC)
		{
			setBranchType(line, Reference.BranchType.pcr);

			if (line.forceByte == false)
			{
				line.forceWord = true;
			}
			if (pstinc > 0 || predec > 0)
			{
				line.error = "auto inc/dec illegal on PCR";
				return;
			}
			
			/* PC addressing */
			if (line.forceWord == true)
			{
				asm.emit(asm, line, pbyte + 13);
				setOffsetAndSize(line, asm.currentSection.counter, 2);
				asm.eword(asm, line, result - (line.offset + 4));				
				line.cycleCount += 5;
	
				return;
			}
	
			if (line.forceByte == true)
			{
				asm.emit(asm, line, pbyte + 12);
				setOffsetAndSize(line, asm.currentSection.counter, 1);
//				asm.emit(asm, line, line.lobyte(result - (line.offset + 1)));
				asm.emit(asm, line, line.lobyte(result - (line.offset + line.bytes.size() + 1)));
				line.cycleCount++;
	
				return;
			}
	
			k = result - (line.offset + 2);
	
			if (k >= -128 && k <= 127)
			{
				asm.emit(asm, line, pbyte + 12);
				setOffsetAndSize(line, asm.currentSection.counter, 1);
				asm.emit(asm, line, line.lobyte(result - (line.offset + 1)));
				line.cycleCount++;
	
				return;
			}
			else
			{
				asm.emit(asm, line, pbyte + 13);
				setOffsetAndSize(line, asm.currentSection.counter, 2);
				asm.eword(asm, line, result - (line.offset + 2));
				line.cycleCount += 5;
	
				return;
			}
		}
	
		setBranchType(line, Reference.BranchType.absolute);

		if (predec > 0 || pstinc > 0)
		{
			if (result != 0)
			{
				line.error = "offset must be zero";
				return;
			}
	
			if (predec > 2 || pstinc > 2)
			{
				line.error = "auto inc/dec by 1 or 2 only";
				return;
			}
	
			if ((predec == 1 && ((pbyte & 0x10) != 0)) ||
				(pstinc == 1 && ((pbyte & 0x10) != 0)))
			{
				line.error = "no auto increment/decrement by 1 for indirect";
				return;
			}
	
			if (predec > 0 && pstinc > 0)
			{
				line.error = "can't do both pre decrement and post increment";
				return;
			}
	
			k = registerType(line, j);
	
			if (k < 0x100)
			{
				if (predec > 0)
				{
					pbyte += predec + 1;
				}
				if (pstinc > 0)
				{
					pbyte += pstinc - 1;
				}
			  
				pbyte += k;
				asm.emit(asm, line, pbyte);
				line.cycleCount += 1 + predec + pstinc;

				return;
			}
	
			if ((predec != 2) && (pstinc != 2))
			{
				line.error = "only ,--W and ,W++ allowed for W indexing";
				return;
			}
	
			/* handle ,W++  ,--W */
			if ((pbyte & 0x10) != 0)  /* [,W++] */
			{
				if (predec == 2)
				{
					asm.emit(asm, line, 0xf0);
					line.cycleCount += 6;
	
					return;
				}
				else if (pstinc == 2)
				{
					asm.emit(asm, line, 0xd0);
					line.cycleCount += 6;
	
					return;
				}
			}
			else		/* ,W++ */
			{
				if (predec == 2)
				{
					asm.emit(asm, line, 0xef);
					line.cycleCount += 2;
	
					return;
				}
				else if (pstinc == 2)
				{
					asm.emit(asm, line, 0xcf);
					line.cycleCount += 2;
	
					return;
				}
			}
		}
	
		k = registerType(line, j);
	
		if (k != 0x100)
		{
			pbyte += k;
	
			if (line.forceWord == true)
			{
				if ((line.hibyte(result) == 0))
				{
//					line.warning = "forcing word when byte would do";
				}
				asm.emit(asm, line, pbyte + 0x09);
				setOffsetAndSize(line, asm.currentSection.counter, 2);
				asm.eword(asm, line, result);
				line.cycleCount += 4;
	
				return;
			}
	
			if (line.forceByte)
			{
				asm.emit(asm, line, pbyte + 0x08);
				if (result <- 128 || result > 127)
				{
					/* it is permissible to specify a larger range, we just
					 * flag it with a warning and downgrade the value
					 */
//	#if 0
//					line.error = "value out of range 2");
//					return 0;
//	#else
					line.warning = "demoting word to byte";
//	#endif
				}
	
				if ((result >= -16) && (result <= 15) && ((pbyte & 16) == 0))
				{
					line.warning = "smaller value could be ok";
				}
	
				setOffsetAndSize(line, asm.currentSection.counter, 1);
				asm.emit(asm, line, line.lobyte(result));
				line.cycleCount++;
	
				return;
			}

			if (line.forceWord == false)
			{
				if (line.references == null || line.references.isEmpty() == true)
				{
					if (result == 0 && ((noOffset == 1) || ((pbyte & 16) != 0)))
					{
						asm.emit(asm, line, pbyte + 0x04);
			
						return;
					}
			
					if ((result >= -16) && (result <= 15) && ((pbyte & 16) == 0))
					{
						pbyte &= 127;
						pbyte += result & 31;
						asm.emit(asm, line, pbyte);
						line.cycleCount++;
			
						return;
					}
				}

				if (line.references == null ||  line.references.containsDPReference() == true)
				{
					if (result >= -128 && result <= 127)
					{
						asm.emit(asm, line, pbyte + 0x08);
						setOffsetAndSize(line, asm.currentSection.counter, 1);
						asm.emit(asm, line, line.lobyte(result));
						line.cycleCount++;
			
						return;
					}
				}
			}
			

			asm.emit(asm, line, pbyte + 0x09);
			setOffsetAndSize(line, asm.currentSection.counter, 2);
			asm.eword(asm, line, result);
			if (nativeMode == true)
			{
				line.cycleCount += 3;
			}
			else
			{
				line.cycleCount += 4;
			}
	
			return;
		}
		else
		{
			/* ,W  n,W [n,W] */
			if (line.forceByte)
			{
				line.error = "byte indexing is invalid for W";
	
				return;
			}
	
			if ((pbyte & 0x10) != 0)
			{
				/* [,W] */
				if (line.forceWord == true || (result != 0))
				{
					asm.emit(asm, line, 0xb0);
					asm.eword(asm, line, result);
					line.cycleCount += 6;
					return;
				}
	
				asm.emit(asm, line, 0x90);
	
				return;
			}
			else
			{		
				/* ,W */
				if (line.forceWord == true || result != 0)
				{
					asm.emit(asm, line, 0xaf);
					asm.eword(asm, line, result);
					line.cycleCount += 3;
	
					return;
				}
	
				asm.emit(asm, line, 0x8f);
	
				return;
			}
		}
	}

	class _bitgen implements InstructionCallback
	{
		public void process(Assembly asm, AsmLine line, Instruction i)
		{
			int src = 0, dst = 0, addr = 0;
			Register r;
			String operand = line.operand;
	
			if (addressingMode(line) == OperandMode.None)
			{
				return;
			}
			
			// parse out individual bits
			if (operand.indexOf(',') == -1)
			{
				line.error = "missing source value";
				return;
			}
			String register = operand.substring(0, operand.indexOf(','));
			operand = operand.substring(operand.indexOf(',') + 1);
			if (operand.indexOf(',') == -1)
			{
				line.error = "missing destination value";
				return;
			}
			
			String expression1 = operand.substring(0, operand.indexOf(','));
			operand = operand.substring(operand.indexOf(',') + 1);
			if (operand.indexOf(',') == -1)
			{
				line.error = "missing address value";
				return;
			}
	
			String expression2 = operand.substring(0, operand.indexOf(','));
			operand = operand.substring(operand.indexOf(',') + 1);
	
			String expression3 = operand;
	
			/* get register number */
			r = identifyRegister(register);
		
			/* check for legal register */
			if (r != A && r != B && r != CC)
			{
				line.error = "illegal register";
				return;
			}
	
			/* capture source bit */
			{
				try
				{
					src = parser.evaluate(expression1, line.references);
				}
				catch (ExpressionException e)
				{
				}
			}
			
			/* capture destination bit */
			{
				try
				{
					dst = parser.evaluate(expression2, line.references);
				}
				catch (ExpressionException e)
				{
				}
			}
		
			/* if src or dst bit > 7, error */
			if (src > 7 || dst > 7)
			{
				line.error = "illegal bit number";
				return;
			}
			
			/* capture 8-bit address (DP offsetted) */
			{
				try
				{
					addr = parser.evaluate(expression3, line.references);
				}
				catch (ExpressionException e)
				{
				}
			}
		
			asm.emit(asm, line, PAGE3);
			asm.emit(asm, line, i.opcode);
		
			/* emit encoded byte */
			{
				int b = 0;
	
				if (r == A)
				{
					b = 1 << 6;
				}
				
				if (r == B)
				{
					b = 2 << 6;
				}
				
				if (r == CC)
				{
					b = 0 << 6;
				}
				
				b |= (dst - 1) << 3;
				b |= (src - 1);
				
				asm.emit(asm, line, b);
			}
			
			/* emit direct page address */
			asm.emit(asm, line, addr);
				
			return;
		}
	}
	
	Register identifyRegister(String registerName)
	{
		String name = registerName;

		if (name.equalsIgnoreCase("PCR"))
		{
			return PC;
		}
		if (name.equalsIgnoreCase("DP"))
		{
			return DP;
		}
		if (name.equalsIgnoreCase("CC"))
		{
			return CC;
		}
		if (name.equalsIgnoreCase("PC"))
		{
			return PC;
		}
		if (name.equalsIgnoreCase("D"))
		{
			return D;
		}
		if (name.equalsIgnoreCase("X"))
		{
			return X;
		}
		if (name.equalsIgnoreCase("Y"))
		{
			return Y;
		}
		if (name.equalsIgnoreCase("U"))
		{
			return U;
		}
		if (name.equalsIgnoreCase("S"))
		{
			return S;
		}
		if (name.equalsIgnoreCase("W"))
		{
			return W;
		}
		if (name.equalsIgnoreCase("V"))
		{
			return V;
		}
		if (name.equalsIgnoreCase("A"))
		{
			return A;
		}
		if (name.equalsIgnoreCase("B"))
		{
			return B;
		}
		if (name.equalsIgnoreCase("D"))
		{
			return D;
		}
		if (name.equalsIgnoreCase("0") || name.equalsIgnoreCase("Z"))
		{
			return ZERO;
		}
		if (name.equalsIgnoreCase("E"))
		{
			return E;
		}
		if (name.equalsIgnoreCase("F"))
		{
			return F;
		}
		if (name.equalsIgnoreCase("T"))
		{
			return T;
		}
		
		return null;
	}
	
	// Turn the instruction represented on the assembler line into
	// proper byte code.
	public void assembleLine(Assembly asm, AsmLine line)
	{
		H6309Instruction i1 = (H6309Instruction)line.instruction;
		
		if (line.opcode == "")
		{
			// nothing to assemble
			return;
		}
				
		// we have the instruction object for this line
		// capture the cycle count and bytes
		line.cycleCount = i1.cy63;
		line.power = i1.power;
		line.bytes.clear();

		i1.cb.process(asm, line, i1);

		return;
	}

	void abd_index(Assembly asm, AsmLine line, int pbyte)
	{
		Register     k;
		int l;
	
//		line.operand = line.operand.substring(2);

		k = identifyRegister(line.operand.substring(line.operand.indexOf(',') + 1));
		l = registerType(line, k);
	
		if (l == 0x100)
		{
			line.error = "cannot use W for register indirect";
			return;
		}
	
		pbyte += l;
		asm.emit(asm, line, pbyte);
	
		return;
	}
	
	int registerType(AsmLine line, Register r)
	{
		if (r == X)
		{
			return(0x00);
		}
	
		if (r == Y)
		{
			return(0x20);
		}
	
		if (r == U)
		{
			return(0x40);
		}
	
		if (r == S)
		{
			return(0x60);
		}
	
		if (r == W)
		{
			return(0x100);
		}
	
		line.error = "illegal register for indexed";
		
		return 0;
	}
	
	OperandMode addressingMode(AsmLine line)
	{
		String operand = line.operand;
		
		if (operand.length() == 0)
		{
			line.error = "operand expected";
			return OperandMode.None;
		}

		if (operand.charAt(0) == '#')
		{
			return(OperandMode.Immediate);          /* immediate addressing */
		}

		if (operand.indexOf(',') != -1)
		{
			return(OperandMode.Indexed);    /* indexed addressing */
		}
	
		if (operand.charAt(0) == '[')
		{
			return(OperandMode.Indirect);          /* indirect addressing */
		}
		
		return(OperandMode.Other);
	}
}