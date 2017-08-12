package org.relic.util;

import java.util.*;
import org.relic.util.Quad;

public class QuadTable extends ArrayList
{
	public void show()
	{
		System.out.printf("%-20s %-12s %-20s %-20s %-20s %s\n", "ADDRESS", "OPCODE", "SRC1", "SRC2", "DST", "COMMENT");
		System.out.println("--------------------------------------------------------------------------------------------------------------------");

		for (Iterator it = this.iterator(); it.hasNext(); )
		{
			Quad q = (Quad)it.next();
			q.show();
		}
		System.out.println("-EOF");
	}

//	public void remove(Quad q)
//	{
//	}


	public Boolean symbolIsDestinationOfAnyInstruction(Symbol s)
	{
		for (int ip = 0; ip < size(); ip++)
		{
			Quad qq = (Quad)get(ip);
			
			if (s.name.equals(qq.dst))
			{
				return true;
			}
		}

		return false;
	}


	public Boolean symbolIsReadOnlyConstant(Symbol s)
	{
		// parameters and globals don't qualify
		if (s.global == true || s.param != 0)
		{
			return false;
		}
		
		for (int ip = 0; ip < size(); ip++)
		{
			Quad qq = (Quad)get(ip);
			
			if (s.name.equals(qq.dst))
			{
				return false;
			}
		}

		return true;
	}


	public Boolean symbolIsDestinationOfAnyOtherInstruction(Symbol s, int i)
	{
		for (int ip = 0; ip < size(); ip++)
		{
			Quad qq = (Quad)get(ip);
			
			if (i != ip && s.name.equals(qq.dst))
			{
				return true;
			}
		}

		return false;
	}
}
