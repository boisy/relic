package org.relic.util;

import java.lang.String;
import java.util.*;
import java.io.*;

public class GNUAsmLine extends AsmLine
{
	public GNUAsmLine(String label, String opcode, String operand, String comment)
	{
      super(label, opcode, operand, comment);
      commentPrefix = ';';
      labelPostfix = ":";
	}
}
