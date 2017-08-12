package org.relic.util;

import java.util.*;
import org.relic.util.Type;

public class TypeTable extends LinkedHashMap
{
	public void add(Type t)
	{
		put(t.name, t);
	}
	
	public void show()
	{
		Iterator it = this.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			Type t = (Type)e.getValue();
			System.out.println(t.name);
		}
		System.out.println("-EOF");
	}

	public int getSizeForType(String type)
	{
		Type t = (Type)this.get(type);
		if (t == null)
		{
			return 0;
		}

		return t.size;
	}
	
	public String largerType(String type1, String type2)
	{
		if (getSizeForType(type1) > getSizeForType(type2))
		{
			return type1;
		}
		else
		{
			return type2;
		}
	}
	
    public boolean isNumber(String type)
    {
		Type t = (Type)this.get(type);
		if (t == null)
		{
			return false;
		}

		return t.numeric != 0;
    }

    public boolean isSignedNumber(String type)
    {
		Type t = (Type)this.get(type);
		if (t == null)
		{
			return false;
		}

		return t.numeric == 2;
    }

	public boolean validAssignment(String typeTo, String typeFrom)
	{
		if (typeTo.equals(typeFrom))
		{
			// same type -- always a valid assignment
			return true;
		}

		if ((isNumber(typeTo) == true) && (isNumber(typeFrom) == true))
		{
			return true;
		}

		// invalid assignment
		return false;
	}
}
