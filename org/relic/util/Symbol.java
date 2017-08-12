 package org.relic.util;

import java.util.*;
import java.io.*;

public class Symbol implements Serializable
{
	public String	name;			// name of symbol
	public String	type;			// data type
	public String	value;			// value of symbol
	public int		size;			// size of type
	public int		param;			// parameter ordinal position (0 = not a parameter)
	public Boolean	global;			// this symbol is global

	public Symbol(String name, String type, String value, int size, int param, Boolean global)
	{
		this.name = name;
		this.type = type;
		this.value = value;
		this.size = size;
		this.param = param;
		this.global = global;
	}

	public void show()
	{
		System.out.printf("%-16s    ", name);
		System.out.printf("%-12s    %-8d    %-8d    %-8s    %s\n", type, size, param, global, value);
	}
}
