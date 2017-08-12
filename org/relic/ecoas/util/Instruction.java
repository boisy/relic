package org.relic.ecoas.util;

import org.relic.ecoas.util.*;


public class Instruction
{
	public InstructionClass iClass;
	public InstructionCallback cb;
	public OperandClass oClass;
	public String mnemonic;
	public int opcode;
	public AssemblerSection.Type section;
	
	public Instruction(String mnemonic, InstructionClass iClass, int opcode, OperandClass oClass, InstructionCallback cb)
	{
		this(mnemonic, iClass, opcode, oClass, cb, AssemblerSection.Type.Any);
	}
	
	public Instruction(String mnemonic, InstructionClass iClass, int opcode, OperandClass oClass, InstructionCallback cb, AssemblerSection.Type section)
	{
		this.iClass = iClass;
		this.cb = cb;
		this.mnemonic = mnemonic;
		this.opcode = opcode;
		this.oClass = oClass;
		this.section = section;
	}
}
