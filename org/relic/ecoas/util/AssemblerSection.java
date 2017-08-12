package org.relic.ecoas.util;

public class AssemblerSection
{
	public enum Type
	{
		Any,
		Code,
		Constant,
		Data,
		DPData,
		ConstantOrData
	}

	public Type type;
	public int counter;
	
	AssemblerSection(Type type)
	{
		this.type = type;
		counter = 0;
	}
	
	public String toString()
	{
		return type + " counter=" + counter;
	}
}
