package org.relic.util;

import java.util.*;

public class Type
{
	public String	name;			// name of symbol
	public int		size;			// size of type
	public int		numeric;		// 2 = is signed numeric 1 = is unsigned numeric, 0 = is not numeric

	public Type(String name, int size, int numeric)
	{
		this.name = name;
		this.size = size;
		this.numeric = numeric;
	}

	public void show()
	{
		System.out.printf("%-20s %d %d\n", name, size, numeric);
	}
}
