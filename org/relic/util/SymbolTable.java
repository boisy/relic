package org.relic.util;

import java.util.*;
import org.relic.util.Symbol;

public class SymbolTable extends LinkedHashMap<String, Symbol>
{
	private static final long serialVersionUID = 1L;

	public void add(Symbol s)
	{
		put(s.name, s);
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
	
	public Integer totalSize()
	{
		Integer size = 0;
		
		// comb through the symbol table and obtain sizes of all symbols
		Iterator it = this.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			
			size += s.size;
		}
		
		return size;
	}
	
	public SymbolTable getUninitializedSymbolsForProcedure(String procedure)
	{
		SymbolTable locals = new SymbolTable();
		
		// comb through the symbol table finding symbols that belong to the passed procedure
		Iterator it = this.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			if (s.name.startsWith(procedure + "_") && !s.type.equals("LABEL") && !s.type.equals("PROCEDURE") && s.global == false && s.value == null)
			{
				locals.add(s);
			}
		}
		
		return locals;
	}
	
	public SymbolTable getParametersForProcedure(String procedure)
	{
		SymbolTable params = new SymbolTable();
		
		// comb through the symbol table finding symbols that belong to the passed procedure
		Iterator it = this.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			if (s.name.startsWith(procedure + "_") && s.param > 0)
			{
				params.add(s);
			}
		}
		
		return params;
	}
	
	public Boolean symbolIsProcedure(String name)
	{
		Iterator it = this.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			if (s.name.equals(name) && s.type.equals("LABEL") && s.global == true)
			{
				return true;
			}
		}

		return false;
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

		System.out.printf("%-16s    %-12s    %-8s    %-8s    %-8s    %s\n", "NAME", "TYPE", "SIZE", "PARAM", "GLOBAL", "VALUE");
		System.out.println("-----------------------------------------------------------------------------------------------");
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			s.show();
		}
		System.out.println("-EOF");
	}
	
	public Symbol getParam(String procedureName, int paramNumber)
	{
		Iterator it = this.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s = (Symbol)e.getValue();
			int index = s.name.indexOf('_');

			if (index != -1)
			{
				String compare = s.name.substring(0, index);
				if (procedureName.equals(compare))
				{
					if (s.param == paramNumber)
					{
						return s;
					}
				}
			}
		}
		
		return null;
	}
	
	// This method computers the size of all symbols up to (but not including) the passed symbol in the symbol table
	public int getTotalSizesUpToSymbol(Symbol s)
	{
		int size = 0;
		
		Iterator it = this.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Symbol s1 = (Symbol)e.getValue();
			
			if (s1 == s)
			{
				break;
			}
			
			size += s1.size;
		}
		
		return size;
	}
}
