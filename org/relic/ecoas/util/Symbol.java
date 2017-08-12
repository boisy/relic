package org.relic.ecoas.util;

import java.io.*;

public class Symbol implements Serializable
{
	private static final long serialVersionUID = -3529553156299596500L;
	public String	name;			// name of symbol
	public int		value;			// value of symbol
	public String   type;			// type (code, dpdata, data, constant)
	public Boolean  directPage;		// direct page = true, non-direct page = false (valid if code == false)
	public Boolean	global;			// this symbol is global

	public Symbol(String name, int value, String type, Boolean global)
	{
		this.name = name;
		this.value = value;
		this.type = type;
		this.global = global;
	}

	public String typeString()
	{
		String info = type;
		
		if (info == "idpd") info = "dp data";
		else if (info == "udpd") info = "dp bss";
		else if (info == "idat") info = "data";
		else if (info == "udat") info = "non-dp bss";
		
		return info;
	}
	
	public String toString()
	{
		char scope = 'L';
		
		if (global) scope = 'G';
		
		return String.format("%16s %04X to %-10s (%c)", name, value, typeString(), scope);
	}
	
	public void show()
	{
		System.out.printf("%s\n", toString());
	}
}
