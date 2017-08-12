package org.relic.ecoas.util;

import java.io.*;

public class Reference implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum BranchType
	{
		absolute,
		pcr
	}
	
	public String		name;			// name of symbol
	public int			size;			// size of reference
	public String   	type;			// type
	public BranchType   branchType;		// branch type
	public int          offset;         // offset to reference
	public Boolean		external;		// true if external, false if local
	public Symbol       localReference;	// reference to the local symbol (if external == false)
	
	public Reference(String name, int size, String type)
	{
		this(name, size, type, BranchType.absolute, 0, true);
	}
	
	public Reference(String name, int size, String type, Boolean external)
	{
		this(name, size, type, BranchType.absolute, 0, external);
	}
	
	public Reference(String name, int size, String type, BranchType branchType, int offset, Boolean external)
	{
		this.name = name;
		this.size = size;
		this.type = type;
		this.branchType = branchType;
		this.offset = offset;
		this.external = external;
	}

	public String toString()
	{
		String info = type;
		
		if (size == 1) info += "/byte";
		else if (size == 2) info += "/word";
		else if (size == 4) info += "/quad";
		
		if (branchType == BranchType.pcr) info += "/pcr";

		if (external == true)
		{
			return String.format("%16s %04X in %s", name, offset, info);
		}
		else
		{
			return String.format("%16s %04X in %s - to %s", name, offset, info, localReference.typeString());
		}
	}

	public String toStringCompact()
	{
		String referenceType = "L";
		if (external == true) referenceType = "E";
		return String.format("[%s (%02X/%s/%s/%02X/%s)]", name, size, type, branchType, offset, referenceType);
	}
	
	public void show()
	{
		System.out.printf("%s\n", toString());
	}
}
