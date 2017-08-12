package org.relic.ecoas.util;

import org.relic.ecoas.util.*;
import java.util.*;

public class SymbolTable extends LinkedHashMap<String, Symbol>
{
	private static final long serialVersionUID = 1L;

	public Boolean add(Symbol s)
	{
		return add(s, false);
	}
	
	public Boolean add(Symbol s, Boolean override)
	{
		Symbol o = (Symbol)get(s.name);
		
		if (o != null && override == false)
		{
			// an object with the desired set name already exists and override is false.
			// check if the values are the same.  if they are, return true, else return false
			return o.value == s.value;
		}

		put(s.name, s);
		return true;
	}
	
	public void removeTemporaryLabels()
	{	
		Iterator it = this.entrySet().iterator();
		Stack<String> stack = new Stack<String>();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();

			if (s.name.indexOf('@') != -1)
			{
				stack.push(s.name);
			}
		}
		
		it = stack.iterator();
		
		while (it.hasNext())
		{
			String e = (String)it.next();
			this.remove(e);
		}
	}
	
	public void removeLocalSymbols()
	{	
		Iterator it = this.entrySet().iterator();
		Stack stack = new Stack();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();

			if (s.global == false)
			{
				stack.push(s.name);
			}
		}
		
		it = stack.iterator();
		
		while (it.hasNext())
		{
			String e = (String)it.next();
			this.remove(e);
		}
	}
	
	public Symbol symbolForName(String name)
	{
		Iterator it = this.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();

			if (s.name.equals(name))
			{
				return s;
			}
		}
		
		return null;
	}
	
	public Boolean symbolIsGlobal(String name)
	{
		Iterator it = this.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			if (s.name.equals(name))
			{
				return s.global;
			}
		}
		
		System.out.printf("Cannot find symbol %s", name);
		return false;
	}
	
	public void show()
	{
		Iterator it = this.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			s.show();
		}
	}
}
