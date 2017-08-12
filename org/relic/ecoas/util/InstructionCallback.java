package org.relic.ecoas.util;


public interface InstructionCallback
{
	public void process(Assembly asm, AsmLine line, Instruction i);
}
