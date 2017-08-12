package org.relic.ecoas.h6309;

public class Register
{
	public String name;
	public int identifier;
	public int value;
	
	Register(String name, int identifier, int value)
	{
		this.name = name;
		this.identifier = identifier;
		this.value = value;
	}

	Register(String name, int identifier)
	{
		this(name, identifier, 0);
	}
}

