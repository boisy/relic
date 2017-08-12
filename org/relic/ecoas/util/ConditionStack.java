package org.relic.ecoas.util;

import java.util.Stack;

public class ConditionStack extends Stack<Object>
{
	private static final long serialVersionUID = 1L;

	public Boolean isTrue()
	{
		if (this.isEmpty() == true || (Boolean)this.peek() == true)
		{
			return true;
		}
		
		return false;
	}
	
	public Boolean isFalse()
	{
		if (this.isEmpty() == false && (Boolean)this.peek() == false)
		{
			return true;
		}
		
		return false;
	}
	
	public void pushTrue()
	{
		this.push(new Boolean(true));
	}
	
	public void pushFalse()
	{
		this.push(new Boolean(false));
	}
}

