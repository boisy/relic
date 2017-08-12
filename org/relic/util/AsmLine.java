package org.relic.util;

import java.lang.String;
import java.util.*;
import java.io.*;

public class AsmLine implements Serializable
{
	static int lineCounter = 0;
	
	public int 		lineNumber;
	public int      offset;
	public String	label;
	public String	opcode;
	public String	operand;
	public String	comment;
	public int      cycleCount;
	public Boolean  forceByte, forceWord;
	public double   power;

	// emitted bytes and byte count
	public String   bytes = "";

	public String	error = "";
	public String	warning = "";

	public Boolean unresolvedSymbol = false;
	
    public char   	commentPrefix = '*';
	public String	labelPostfix = "";

	public AsmLine(String label, String opcode, String operand, String comment)
	{
		this.lineNumber = ++lineCounter;
		this.label = label;
		this.opcode = opcode;
		this.operand = operand;
		this.comment = comment;
		this.bytes = "";

		this.forceByte = false;
		this.forceWord = false;
		
		this.error = null;
		this.warning = null;
	}

	public AsmLine(String line)
	{
		this.lineNumber = ++lineCounter;
		this.label = "";
		this.opcode = "";
		this.operand = "";
		this.comment = "";
		this.bytes = "";

		this.forceByte = false;
		this.forceWord = false;

		this.error = null;
		this.warning = null;

		if (line.length() == 0)
		{
			// blank line
			return;
		}
		
		if (line.charAt(0) != ' ')
		{
			if (line.charAt(0) == commentPrefix)
			{
				comment = line;
				return;
			}
			else
			{
				while (line.length() > 0 && line.charAt(0) != ' ' && line.charAt(0) != '\t')
				{
					label = label + line.charAt(0);
					line = line.substring(1);
				}
			}
		}

		// if next char is a comment char, then the rest of this line is a comment
		if (line.length() > 0 && line.charAt(0) == commentPrefix)
		{
			comment = line;
			return;
		}
		
		while (line.length() > 0 && (line.charAt(0) == ' ' || line.charAt(0) == '\t'))
		{
			line = line.substring(1);
		}

		while (line.length() > 0 && line.charAt(0) != ' ' && line.charAt(0) != '\t')
		{
			opcode = opcode + line.charAt(0);
			line = line.substring(1);
		}

		while (line.length() > 0 && (line.charAt(0) == ' ' || line.charAt(0) == '\t'))
		{
			line = line.substring(1);
		}

		// if next char is NOT a comment char, then this is an operand
		if (line.length() > 0 && line.charAt(0) != commentPrefix)
		{
			while (line.length() > 0)
			{
				operand = operand + line.charAt(0);
				line = line.substring(1);
			}

			while (line.length() > 0 && line.charAt(0) != ' ' && line.charAt(0) != '\t')
			{
				opcode = opcode + line.charAt(0);
				line = line.substring(1);
			}
		}

		while (line.length() > 0)
		{
			comment = comment + line.charAt(0);
			line = line.substring(1);
		}
	}
	
	public void dump(FileWriter out)
	{
		String dumpString;
		char errorChar = ' ';
		
		if (error != null)
		{
			errorChar = 'E';
		}
		else if (warning != null)
		{
			errorChar = 'W';
		}
		else if (unresolvedSymbol == true)
		{
			errorChar = 'S';
		}
		
		if (bytes != null && bytes != "")
		{
			dumpString = String.format("%05d  %c %04X [~%2d] [w%.4f] %-12s   %s", lineNumber, errorChar, offset, cycleCount, power, bytes, prettyLine());
		}
		else
		{
			dumpString = String.format("%05d  %c      [~%2d] [w%.4f] %-12s   %s", lineNumber, errorChar, cycleCount, power, bytes, prettyLine());
		}

		if (out == null)
		{
			System.out.print(dumpString);
			if (error != null)
			{
				System.out.print("**** Error: " + error + "\n");
			}
			else if (warning != null)
			{
				System.out.print("**** Warning: " + warning + "\n");
			}
		}
		else
		{
			try 
			{
				out.write(dumpString);
				if (error != null)
				{
					out.write("**** Error: " + error + "\n");
				}
				else if (warning != null)
				{
					out.write("**** Warning: " + warning + "\n");
				}
			}
			catch (IOException e)
			{
			}
		}
	}
	
	public String prettyLine()
	{
		if (label != null && label != "")
		{
			label = label + labelPostfix;
		}
		if (opcode == null)
		{
			// no opcode... check for case where all are empty (empty line)
			if (label == null && operand == null && comment == null)
			{
				return "\n";
			}
			
			// no opcode.. check if there's a label
			if (label != null)
			{
				return String.format("%s\n", label);
			}
			if (comment != null)
			{
				return String.format("%s\n", commentPrefix + " " + comment);
			}

			return "";
		}

		if (label == null)
		{
			label = "";
		}
		
		if (operand == null)
		{
			operand = "";
		}
		
		if (comment == null || comment == "")
		{
			comment = "";
		}
		else
		{
			if (comment == "" || (comment.charAt(0) != commentPrefix))
			{
				comment = commentPrefix + " " + comment;
			}
		}
		
		String s;
		
		if (label == ""	&& opcode == "")
		{
			s = comment + "\n";
		}
		else
		{
			s = String.format("%-20s %-8s %-24s %s\n", label, opcode, operand, comment);
		}
		
		return s;
	}

	public void show(FileWriter out)
	{
		String s = prettyLine();

		if (out != null)
		{
			try
			{
				out.write(s);
			}
			catch (IOException e)
			{
			}
		}
		else
		{
			System.out.print(s);
		}
	}
}
