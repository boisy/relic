package org.relic.ecoas.util;

import org.relic.ecoas.util.Instruction;

public class PseudoInstruction extends Instruction
{
	public PseudoInstruction(String mnemonic, InstructionClass iClass, OperandClass oClass, InstructionCallback cb, AssemblerSection.Type section)
	{
		super(mnemonic, iClass, 0, oClass, cb, section);
	}

}
