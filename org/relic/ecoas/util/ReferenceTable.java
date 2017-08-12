package org.relic.ecoas.util;

import org.relic.ecoas.util.*;
import java.util.*;

public class ReferenceTable extends ArrayList<Reference>
{
	private static final long serialVersionUID = 1L;

	public boolean hasLocalReferences()
	{
		Iterator<Reference> it = this.iterator();

		if (it.hasNext())
		{
			while (it.hasNext())
			{
				Reference r = (Reference)it.next();
				if (r.external == false)
					return true;
			}
		}
		
		return false;
	}
	
	public boolean containsDPReference()
	{
		Iterator<Reference> it = this.iterator();

		if (it.hasNext())
		{
			while (it.hasNext())
			{
				Reference r = (Reference)it.next();
				Symbol s = r.localReference;
				if (s != null && (s.type.equals("idpd") || s.type.equals("udpd")))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean containsExternalReference()
	{
		Iterator<Reference> it = this.iterator();

		if (it.hasNext())
		{
			while (it.hasNext())
			{
				Reference r = (Reference)it.next();
				if (r.external == true)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Reference referenceForName(String name)
	{
		Iterator<Reference> it = this.iterator();
		
		while (it.hasNext())
		{
			Reference r = (Reference)it.next();

			if (r.name.equals(name))
			{
				return r;
			}
		}
		
		return null;
	}
		
	public void show()
	{
		show(null);
	}
	
	public void show(String title)
	{
		if (title != null)
		{
			System.out.println(title);
		}
		
		Iterator<Reference> it = this.iterator();

		if (it.hasNext())
		{
			while (it.hasNext())
			{
				Reference r = (Reference)it.next();
				r.show();
			}
		}
	}

	public String toStringCompact()
	{
		String result = "";
		Iterator<Reference> it = this.iterator();

		if (it.hasNext())
		{
			while (it.hasNext())
			{
				Reference r = (Reference)it.next();
				result += r.toStringCompact() + " ";
			}
		}
		
		return result;
	}
}
