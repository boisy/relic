package org.relic.ecoas.util;

import java.util.*;
import java.io.*;

public class AsmLine implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int 			lineNumber;
	public int      	offset;
	public String		label;
	public String		opcode;
	public String		operand;
	public String		comment;
	public int      	cycleCount;
	public Boolean  	forceByte, forceWord;
	public double   	power;
	public Instruction	instruction;
	public String		expression;
	public ReferenceTable	references;
	public AssemblerSection.Type sectionType;
	
	// emitted bytes and byte count
	public ArrayList<Integer> bytes;
	
	public String	error = "";
	public String	warning = "";

	public String	labelPostfix = "";

	public Boolean isBlankLine()
	{
		if (label == "" && opcode == "" && operand == "" && comment == "")
		{
			return true;
		}
		
		return false;
	}
	
	public Boolean isCommentLine()
	{
		if (label == "" && opcode == "" && operand == "" && comment != "")
		{
			return true;
		}
		
		return false;
	}
	
	public AsmLine(String label, String opcode, String operand, String comment, InstructionMap instructions)
	{
		this(label + " " + opcode + " " + operand + " " + comment, null);
	}

	public AsmLine(String line, InstructionMap instructions)
	{
		setLine(line, instructions);
	}
	
	public void replaceLine(String line, InstructionMap instructions)
	{
		setLine(line, instructions);
	}
	
	public void setLine(String line, InstructionMap instructions)
	{
		this.expression = "";
		this.label = "";
		this.opcode = "";
		this.operand = "";
		this.comment = "";
		this.bytes = new ArrayList<Integer>();

		this.forceByte = false;
		this.forceWord = false;

		this.error = null;
		this.warning = null;

		this.references = new ReferenceTable();

		if (line.length() == 0)
		{
			// blank line
			return;
		}
		
		if (line.charAt(0) != ' ')
		{
			if (line.charAt(0) == ';' || line.charAt(0) == '*')
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
		if (line.length() > 0 && (line.charAt(0) == ';' || line.charAt(0) == '*'))
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

		// find instruction
		instruction = null;
		if (opcode != "" && instructions != null)
		{
			instruction = (Instruction)instructions.get(opcode);
			if (instruction == null)
			{
				error = "unrecognized instruction " + opcode;
				return;
			}

			switch (instruction.oClass)
			{
				case HAS_NO_OPERAND:
					// next character is comment... just break
					break;
	
				case HAS_OPERAND:
					// non-space delimited word is an operand
					while (line.length() > 0 && line.charAt(0) != ' ' && line.charAt(0) != '\t')
					{
						operand = operand + line.charAt(0);
						line = line.substring(1);
					}
					break;
	
				case HAS_OPERAND_WITH_SPACES:
					// this operand can have spaces, so there are no comments on this line
					while (line.length() > 0)
					{
						operand = operand + line.charAt(0);
						line = line.substring(1);
					}
					break;
	
				case HAS_OPERAND_WITH_DELIMITERS:
					// this operand is delimited
					char delimiter = line.charAt(0);
					while (line.length() > 0)
					{
						operand = operand + line.charAt(0);
						line = line.substring(1);
						if (line.charAt(0) == delimiter)
						{
							operand = operand + line.charAt(0);
							line = line.substring(1);
							break;
						}
					}
					break;
			}
		}
		
		// skip whitespace
		while (line.length() > 0 && line.charAt(0) == ' ' && line.charAt(0) == '\t')
		{
			line = line.substring(1);
		}

		// rest of line is comment
		while (line.length() > 0)
		{
			comment = comment + line.charAt(0);
			line = line.substring(1);
		}
	}
	
	public void dump(int codeOffset)
	{
		String dumpString = detailedLine(codeOffset); 

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
	
	Boolean addLabelToSymbolTable(AssemblerSection section, SymbolTable symbols, int value)
	{
		String type = "code";
		
		switch (section.type)
		{
			case Code:
				type = "code";
				break;

			case Data:
				type = "udat";
				break;

			case Constant:
				type = "cnst";
				break;
		}
		
		return addLabelToSymbolTable(type, symbols, value);
	}
	
	Boolean addLabelToSymbolTable(String type, SymbolTable symbols, int value)
	{
		Boolean isGlobal = false;
		String tmpLabel = label;

		if (tmpLabel.endsWith(":") == true)
		{
			isGlobal = true;
			tmpLabel = tmpLabel.substring(0, tmpLabel.length() - 1);
		}
	
		return symbols.add(new Symbol(tmpLabel, value, type, isGlobal), false);
	}
	
	public String detailedLine(int codeOffset)
	{
		String line;
		char errorChar = ' ';
		int passedOffset = codeOffset;
		
		if (error != null)
		{
			errorChar = 'E';
		}
		else if (references.isEmpty() == false && references.hasLocalReferences() == false)
		{
			errorChar = 'R';
		}
		else if (warning != null)
		{
			errorChar = 'W';
		}
		else if (sectionType == AssemblerSection.Type.Data || sectionType == AssemblerSection.Type.DPData)
		{
			errorChar = 'D';
			passedOffset = 0;
		}
		else if (sectionType == AssemblerSection.Type.Constant)
		{
			errorChar = 'C';
			passedOffset = 0;
		}
		
		String s = "";
		for (int i = 0; i < bytes.size(); i++)
		{
			if (i == 6) break;
			s = s + String.format("%02X", bytes.get(i));
		}
		
		if (bytes.size() > 0)
		{
			if (cycleCount > 0)
			{
				line = String.format("%05d  %c %04X [~%2d] %-12s   %s", lineNumber, errorChar, this.offset + passedOffset, cycleCount, s, prettyLine());
			}
			else
			{
				line = String.format("%05d  %c %04X       %-12s   %s", lineNumber, errorChar, this.offset + passedOffset, s, prettyLine());
			}
		}
		else
		{
			if (opcode.equalsIgnoreCase("equ") || opcode.equalsIgnoreCase("set"))
			{
				line = String.format("%05d  %c %04X       %-12s   %s", lineNumber, errorChar, this.offset, s, prettyLine());
			}
			else if (opcode != "")
			{
				line = String.format("%05d  %c %04X       %-12s   %s", lineNumber, errorChar, this.offset + passedOffset, s, prettyLine());
			}
			else
			{
				line = String.format("%05d               %-12s   %s", lineNumber, s, prettyLine());
			}
		}

		return line;
	}
	
	public String prettyLine()
	{
		if (label != "")
		{
			label = label + labelPostfix;
		}

		if (opcode == "")
		{
			// no opcode... check for case where all are empty (empty line)
			if (label == "" && operand == "" && comment == "")
			{
				return "\n";
			}
			
			// no opcode.. check if there's a label
			if (label != "")
			{
				return String.format("%s\n", label);
			}
			if (comment != "")
			{
				return comment + "\n";
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
			if (comment == "" || (comment.charAt(0) != ';' || comment.charAt(0) != '*'))
			{
//				comment = ';' + " " + comment;
			}
		}
		
		String s;
		
		if (label == ""	&& opcode == "")
		{
			s = comment + "\n";
		}
		else
		{
			s = String.format("%-12s %-8s %-24s %s %s\n", label, opcode, operand, comment, references.toStringCompact());
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
	
	public int lobyte(int word)
	{
		return word & 0xFF;
	}
	
	public int hibyte(int word)
	{
		int x = (word >> 8);
		x = x & 0xFF;
		return (word >> 8) & 0xFF;
	}
	
	String referencesString()
	{
		String refString = "";
		
		for (Iterator<Reference> i = references.iterator(); i.hasNext(); )
		{
			Reference e = i.next();
			refString += e.toStringCompact() + " ";
		}
		
		return refString;
	}
	
	public String toString()
	{
		return detailedLine(0);	
	}
}
