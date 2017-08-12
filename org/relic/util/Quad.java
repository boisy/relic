package org.relic.util;

import java.util.*;
import java.io.*;

public class Quad implements Serializable
{
	public String	label;
	public String	opcode;
	public String	src1;
	public String	src2;
	public String	dst;
	public String	comment;

	public Quad(String label, String opcode, String src1, String src2, String dst, String comment)
	{
		this.label = label;
		this.opcode = opcode;
		this.src1 = src1;
		this.src2 = src2;
		this.dst = dst;
		this.comment = comment;
	}

	public String prettyLine()
	{
		// redress label
		if (label == null)
		{
			label = "";
		}
		
		// redress parameters
		if (src1 == null)
		{
			src1 = "";
		}
		if (src2 == null)
		{
			src2 = "";
		}
		if (dst == null)
		{
			dst = "";
		}
		
		// redress comment
		if (comment != null)
		{
			comment = "; " + comment;
		}
		else
		{
			comment = "";
		}

		return label + " " + opcode.toLowerCase() + " " + src1 + " " + src2 + " " + dst + comment;
	}
	
	public void show()
	{
		// redress label
		if (label == null)
		{
			label = "";
		}
		
		// redress parameters
		if (src1 == null)
		{
			src1 = "";
		}
		if (src2 == null)
		{
			src2 = "";
		}
		if (dst == null)
		{
			dst = "";
		}
		
		// redress comment
		if (comment != null)
		{
			comment = "; " + comment;
		}
		else
		{
			comment = "";
		}

		System.out.printf("%-20s %-12s %-20s %-20s %-20s %s\n", label, opcode.toLowerCase(), src1, src2, dst, comment);
	}
}
