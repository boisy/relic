/*
 04/22/2009: Constants passed to procedures are no longer copied onto when exiting the procedure.
             Fixed a problem in PowerPC where bus error occurred when writing to constant area.
 */
grammar RaginBasic;

// START:members
@header {
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.relic.util.*;
}

@members {
/** Map variable name to Integer object holding value */
SymbolTable symbolTable = new SymbolTable();
public QuadTable quadTable = new QuadTable();
public TypeTable typeTable = new TypeTable();
int tempCounter = 1;
int tempLabelCounter = 1;
String separator = "_";

String makeTempVar(ParseState state)
{
	return state.currProcName + separator + "\$" + tempCounter++;
}

String makeTempVar(ParseState state, String postfix)
{
	return state.currProcName + separator + "\$" + postfix;
}

String makeTempLabel(ParseState state)
{
	return state.currProcName + separator + "\$L" + tempLabelCounter++;
}
}
// END:members

// START:prog
program
	@init
	{
		typeTable.add(new Type("BYTE", 1, 1));
		typeTable.add(new Type("BOOLEAN", 1, 0));
		typeTable.add(new Type("INTEGER", 2, 2));
		typeTable.add(new Type("REAL", 5, 2));
		typeTable.add(new Type("STRING", 32, 0));
	}
	:
//	PROGRAM ID
//	{
//		symbolTable.add(new Symbol($ID.text, "PROGRAM", null, 0, 0, 1, 0, 1));
//	}
//	NEWLINE
	procedure
+
	;
                
procedure
	@init
	{
		ParseState state = new ParseState();
	}
	:
	PROCEDURE ID
	{
		// determine if this ID is already in our symbol table
		if (symbolTable.get($ID.text) == null)
		{
			// capture the current procedure name (used to scope variables in the symbol table)
			state.currProcName = $ID.text;
			Integer i = quadTable.size();
			symbolTable.add(new Symbol(state.currProcName, "LABEL", i.toString(), 0, 0, true));
			quadTable.add(new Quad(state.currProcName, "nop", null, null, null, "start of procedure '" + state.currProcName + "'"));
		}
		else
		{
			RaginBasicFrontEnd.reportError($ID.text + " is already defined");
			return;
		}
	}
	NEWLINE+
	procedureBody[state]
	{
		for (int i = state.paramlist.size() - 1; i >=0; i--)
		{
			String s = (String)state.paramlist.get(i);
			Symbol sym = symbolTable.get(s);
			if (sym.value == null)
			{		
				quadTable.add(new Quad(null, "stackpoke", null, null, s, null));
			}
		}

		quadTable.add(new Quad(null, "ret", null, null, null, null));
	}
	;

procedureBody[ParseState state]
	:
	statementList[state]
	;
	
paramDeclaration[ParseState state]
	@init
	{
	}
	:	
	PARAM ID
	{
		String id = state.currProcName + separator + $ID.text;

		state.paramlist.add(id);
	}
	(identList[state]
	{
		state.paramlist.add(state.name);
	}
	)* COLON typeName[state]
	{
		Iterator iterator = state.paramlist.iterator();
		Integer i = 0;
		while (iterator.hasNext())
		{
			String s = (String)iterator.next();
		
			if (symbolTable.get(s) != null)
			{
				RaginBasicFrontEnd.reportError(id + " is already defined");
				return;
			}
			symbolTable.add(new Symbol(s, state.name, null, state.size, ++state.paramCount, false));
			quadTable.add(new Quad(null, "stackpeek", i.toString(), null, s, null));
			i++;
		}
	}
	;
	
typeDeclaration[ParseState state]
	:
	TYPE ID EQUALS recordDeclaration[state]
	;

recordDeclaration[ParseState state]
	:
	ID (identList[state])* COLON typeName[state] ( SEMI ID (identList[state])* COLON typeName[state] )*
	;

typeName[ParseState state]
	: 'STRING'
	{
		state.size = typeTable.getSizeForType("STRING");
	}
	('[' INTLIT ']'
	{
		state.size = new Integer($INTLIT.text).intValue();
	}
	) ?
	{
		state.name = "STRING";
	}
	| 'INTEGER'
	{
		state.size = 2; state.name = "INTEGER";
	}
	| 'BOOLEAN'
	{
		state.size = 1; state.name = "BOOLEAN";
	}
	| 'REAL'
	{
		state.size = 5; state.name = "REAL";
	}
	| 'BYTE'
	{
		state.size = 1; state.name = "BYTE";
	}
	;

variableList[ParseState state]
	@init
	{
		ArrayList idlist = new ArrayList();
	}
	:
	ID
	{
		String id = state.currProcName + separator + $ID.text;

		idlist.add(id);
	}
	(identList[state]
	{
		idlist.add(state.name);
	}
	)* COLON typeName[state]
	{
		Iterator iterator = idlist.iterator();
		while (iterator.hasNext())
		{
			String s = (String)iterator.next();

		
			if (symbolTable.get(s) != null)
			{
				RaginBasicFrontEnd.reportError(id + " is already defined");
				return;
			}
			
			symbolTable.add(new Symbol(s, state.name, null, state.size, 0, false));
		}
	}
	;
	
variableDeclaration[ParseState state]
	:	
	DIM
	variableList[state]
	((SEMI | AS) variableList[state])*
	;
	
identList[ParseState state]
	:
	(COMMA ID
	{
		state.name = state.currProcName + separator + $ID.text;
	}
	)
	;

statementList[ParseState state]
	:	
	((INTLIT
	{
		String id = makeTempVar(state, "LINE" + $INTLIT.text);
		Integer i = quadTable.size();
		symbolTable.add(new Symbol(id, "LABEL", i.toString(), 0, 0, false));
		quadTable.add(new Quad(id, "nop", null, null, null, null));
	}
	NEWLINE? COMMENT?)?
	statement[state] (NEWLINE)+ COMMENT?)*
	;
	
statement[ParseState state]
	:
	variableDeclaration[state]
	| typeDeclaration[state]
	| paramDeclaration[state]
	| gotoStatement[state]
	| exitStatement[state]
	| printStatement[state]
	| inputStatement[state]
	| forStatement[state]
	| ifStatement[state]
	| repeatStatement[state]
	| whileStatement[state]
	| endStatement[state]
	| runStatement[state]
	| pokeStatement[state]
	| shellStatement[state]
	| assignmentStatement[state]
	;

assignmentStatement[ParseState state]
	@init
	{
		int vartype;
	}
	:
	LET? variableReference[state]
	{
		// capture the state information for this variable reference
		String vname = state.name;
		String vtype = state.type;
		int vsize = state.size;
	}
	EQUALS expression[state]
	{
		if (typeTable.validAssignment(vtype, state.type) == true)
		{
			// create a new variable
			if (vtype == "STRING")
			{
				// if the string we're assigning to is smaller, warn the user
				if (vsize < state.size)
				{
					RaginBasicFrontEnd.reportWarning("copying a string of greater size to one of lesser size; truncation will occur");
				}
			}
			quadTable.add(new Quad(null, "cp", state.name, null, vname, null));
		}	
		else
		{
			RaginBasicFrontEnd.reportError("type mismatch (" + vtype + " = " + state.type + ")");
			return;
		}		
	}
	;

ifStatement[ParseState state]
	:
	IF ifPart[state]
	;

ifPart[ParseState state]
	: expression[state]
	{
		if (!state.type.equals("BOOLEAN"))
		{
			RaginBasicFrontEnd.reportError("IF statement requires BOOLEAN expression, " + state.type + " used instead");
			return;
		}
		
		// create a temp variable
		String id = makeTempVar(state);
		symbolTable.add(new Symbol(id, "BOOLEAN", "TRUE", typeTable.getSizeForType("BOOLEAN"), 0, false));
		String thenName = makeTempLabel(state);
		quadTable.add(new Quad(null, "gt!=", state.name, id, thenName, null));
	}
	THEN NEWLINE?
	statementList[state]
	{
		String endName = makeTempLabel(state);
		quadTable.add(new Quad(null, "gt", null, null, endName, null));
		Integer i = quadTable.size();
		symbolTable.add(new Symbol(thenName, "LABEL", i.toString(), 0, 0, false));
		quadTable.add(new Quad(thenName, "nop", null, null, null, null));
	}
	( ELSE NEWLINE? statementList[state] ) ?
	ENDIF
	{
		i = quadTable.size();
		symbolTable.add(new Symbol(endName, "LABEL", i.toString(), 0, 0, false));
		quadTable.add(new Quad(endName, "nop", null, null, null, null));
	}
	;
	
repeatStatement[ParseState state]
	:
	REPEAT
	{
		String repeatName = makeTempLabel(state);
		Integer i = quadTable.size();
		symbolTable.add(new Symbol(repeatName, "LABEL", i.toString(), 0, 0, false));
		quadTable.add(new Quad(repeatName, "nop", null, null, null, null));
	}
	NEWLINE statementList[state]
	UNTIL
	expression[state]
	{
		if (!state.type.equals("BOOLEAN"))
		{
			RaginBasicFrontEnd.reportError("UNTIL requires BOOLEAN expression, " + state.type + " used instead");
			return;
		}
		
		// create a temp variable
		String id = makeTempVar(state);
		symbolTable.add(new Symbol(id, "BOOLEAN", "1", typeTable.getSizeForType("BOOLEAN"), 0, false));
		quadTable.add(new Quad(null, "gt!=", state.name, id, repeatName, null));
	}
	;

forStatement[ParseState state]
	@init
	{
		String vartype, varname;
		String fromtype, fromname;
		String totype, toname;
		String steptype, stepname = null;
		String stepvalue = "1";
		String nexttype, nextname;
		String loopTop, loopBottom;
	}
	:
	FOR variableReference[state] 
	{
		vartype = state.type;
		varname = state.name;

		if (!vartype.equals("INTEGER") && !vartype.equals("REAL"))
		{
			RaginBasicFrontEnd.reportError("FOR loop variable must be INTEGER or REAL");
			return;
		}
	}
	EQUALS expression[state]
	{
		fromtype = state.type;
		fromname = state.name;

		if (!fromtype.equals("BYTE") && !fromtype.equals("INTEGER") && !fromtype.equals("REAL"))
		{
			RaginBasicFrontEnd.reportError("FOR loop FROM must be BYTE, INTEGER or REAL");
			return;
		}

		if (typeTable.validAssignment(vartype, fromtype) == false)
		{
			RaginBasicFrontEnd.reportError("type mismatch (" + fromtype + " = " + vartype + ")");
			return;
		}

		quadTable.add(new Quad(null, "cp", fromname, null, varname, null));
	}
	(TO | DOWNTO {stepvalue = "-1";} ) expression[state]
	{
		totype = state.type;
		toname = state.name;

		if (!totype.equals("BYTE") && !totype.equals("INTEGER") && !totype.equals("REAL"))
		{
			RaginBasicFrontEnd.reportError("FOR loop TO must be BYTE, INTEGER or REAL");
			return;
		}
	}
	(STEP expression[state]
	{
		steptype = state.type;
		stepname = state.name;
		
		if (!steptype.equals("BYTE") && !steptype.equals("INTEGER") && !steptype.equals("REAL"))
		{
			RaginBasicFrontEnd.reportError("FOR loop STEP must be BYTE, INTEGER or REAL");
			return;
		}
	}
	)?
	{
		/* If stepname = null then there was no STEP specified.. set one */
		if (stepname == null)
		{
			steptype = "INTEGER";
			stepname = makeTempVar(state);	
			symbolTable.add(new Symbol(stepname, steptype, stepvalue, typeTable.getSizeForType(steptype), 0, false));
		}

		loopBottom = makeTempLabel(state);
		
		/* Here we test the loop constraint */
		loopTop = makeTempLabel(state);
		Integer i = quadTable.size();
		symbolTable.add(new Symbol(loopTop, "LABEL", i.toString(), 0, 0, false));

		if (stepvalue.equals("1"))
		{
			quadTable.add(new Quad(loopTop, "gt>", varname, toname, loopBottom, null));
		}
		else
		{
			quadTable.add(new Quad(loopTop, "gt<", varname, toname, loopBottom, null));
		}

	}
	NEWLINE
	statementList[state]
	NEXT
	(variableReference[state]
	{
		if (!state.name.equals(varname))
		{
			RaginBasicFrontEnd.reportError("FOR loop variable must match NEXT variable");
			return;
		}
	})?	
	{
		/* Apply STEP value and loop to top of loop */
		quadTable.add(new Quad(null, "+", varname, stepname, varname, null));
		quadTable.add(new Quad(null, "gt", null, null, loopTop, null));
		quadTable.add(new Quad(loopBottom, "nop", null, null, null, null));
		i = quadTable.size() - 1;
		symbolTable.add(new Symbol(loopBottom, "LABEL", i.toString(), 0, 0, false));
	}
	;

whileStatement[ParseState state]
	:
	WHILE
	{
		String topName = makeTempLabel(state);
		Integer i = quadTable.size();
		symbolTable.add(new Symbol(topName, "LABEL", i.toString(), 0, 0, false));
		quadTable.add(new Quad(topName, "nop", null, null, null, null));
	}
	expression[state]
	{
		if (!state.type.equals("BOOLEAN"))
		{
			RaginBasicFrontEnd.reportError("WHILE statement requires BOOLEAN expression, " + state.type + " used instead");
			return;
		}
		
		// create a temp variable
		String id = makeTempVar(state);
		symbolTable.add(new Symbol(id, "BOOLEAN", "1", typeTable.getSizeForType("BOOLEAN"), 0, false));
		String endName = makeTempLabel(state);
		quadTable.add(new Quad(null, "gt!=", state.name, id, endName, null));
	}
	DO NEWLINE?
	statementList[state]
	{
		quadTable.add(new Quad(null, "gt", null, null, topName, null));
		i = quadTable.size();
		symbolTable.add(new Symbol(endName, "LABEL", i.toString(), 0, 0, false));
		quadTable.add(new Quad(endName, "nop", null, null, null, null));
	}
	ENDWHILE
	;
	
endStatement[ParseState state]
	: END
	{
		quadTable.add(new Quad(null, "ret", null, null, null, null));
	}
	;

gotoStatement[ParseState state]
	:
	GOTO INTLIT
	{
		String id = state.currProcName + separator + "\$LINE" + $INTLIT.text;
		quadTable.add(new Quad(null, "gt", null, null, id, null));
	}
	;
	
pokeStatement[ParseState state]
	:
	POKE
	expression[state]
	{
		if (typeTable.isNumber(state.type) == false)
		{
			RaginBasicFrontEnd.reportError("invalid type for POKE");
			return;
		}

//		String id = makeTempVar(state);
//		symbolTable.add(new Symbol(id, "INTEGER", null, typeTable.getSizeForType("INTEGER"), 0, 0));
		symbolTable.add(new Symbol("poke_p0", "INTEGER", null, typeTable.getSizeForType("INTEGER"), 1, true));
		quadTable.add(new Quad(null, "cp", state.name, null, "poke_p0", null));
	}
	COMMA
	expression[state]
	{
		if (typeTable.isNumber(state.type) == false)
		{
			RaginBasicFrontEnd.reportError("invalid type for POKE");
			return;
		}

//		id = makeTempVar(state);
//		symbolTable.add(new Symbol(id, "BYTE", null, typeTable.getSizeForType("BYTE"), 0, 0));
		symbolTable.add(new Symbol("poke_p1", "BYTE", null, typeTable.getSizeForType("BYTE"), 2, true));
		quadTable.add(new Quad(null, "cp", state.name, null, "poke_p1", null));
		quadTable.add(new Quad(null, "call", null, null, "poke", null));
	}
	;
	
shellStatement[ParseState state]
	:
	SHELL
	expression[state]
	{
		if (typeTable.isNumber(state.type) == true)
		{
			RaginBasicFrontEnd.reportError("invalid type for POKE");
			return;
		}

//		String id = makeTempVar(state);
//		symbolTable.add(new Symbol(id, "INTEGER", null, typeTable.getSizeForType("INTEGER"), 0, 0));
		symbolTable.add(new Symbol("shell_p0", "STRING", null, typeTable.getSizeForType("STRING"), 1, true));
		quadTable.add(new Quad(null, "cp", state.name, null, "shell_p0", null));
		quadTable.add(new Quad(null, "call", null, null, "_shell", null));
	}
	;
	
exitStatement[ParseState state]
	:
	EXIT
	expression[state]
	{
		if (!(state.type.equals("INTEGER") || state.type.equals("BYTE")))
		{
			RaginBasicFrontEnd.reportError("EXIT requires an INTEGER or BYTE value");
			return;
		}
		quadTable.add(new Quad(null, "exit", null, null, state.name, null));
	}
	;
	
runStatement[ParseState state]
	@init
	{
		int paramCount = 1;
		String procedureName = null;
		ArrayList fromList = new ArrayList();
		ArrayList toList = new ArrayList();
	}
	:
	CALL ID
	{
		procedureName = $ID.text;
		Symbol s = (Symbol)symbolTable.get(procedureName);
		if (s == null || !s.type.equals("LABEL"))
		{
			RaginBasicFrontEnd.reportError("procedure '" + procedureName + "' not found");
			return;
		}
	}
	(
	LPAREN
	(expression[state]
	{
		Symbol p = symbolTable.getParam(procedureName, paramCount);
		if (p == null)
		{
			RaginBasicFrontEnd.reportError("undefined parameter #" + paramCount + " for procedure '" + procedureName + "'");
			return;
		}
		// Here we allow REAL, BYTE and INTEGER constants or variables to be passed as parameters to REAL, BYTE or INTEGER
		if (!(typeTable.isNumber(state.type) && typeTable.isNumber(p.type)) &&
			!state.type.equals(p.type))
		{
			RaginBasicFrontEnd.reportError("type mismatch at parameter #" + paramCount + " for procedure '" + procedureName + "'");
			return;
		}

		// check if the passed variable is initialized or not.. if not, we will copy values back to it upon exiting the procedure
//		if (state.value == null)
		{
			fromList.add(state.name);
			toList.add(p.name);
		}
		paramCount++;
	}
	(COMMA expression[state]
	{
		p = symbolTable.getParam(procedureName, paramCount);
		if (p == null)
		{
			RaginBasicFrontEnd.reportError("undefined parameter #" + paramCount + " for procedure '" + procedureName + "'");
			return;
		}
		// Here we allow REAL, BYTE and INTEGER constants or variables to be passed as parameters to REAL, BYTE or INTEGER
		if (!(typeTable.isNumber(state.type) && typeTable.isNumber(p.type)) &&
			!state.type.equals(p.type))
		{
			RaginBasicFrontEnd.reportError("type mismatch at parameter #" + paramCount + " for procedure '" + procedureName + "'");
			return;
		}
		
		// check if the passed variable is initialized or not.. if not, we will copy values back to it upon exiting the procedure
//		if (state.value == null)
		{
			fromList.add(state.name);
			toList.add(p.name);
		}
		paramCount++;
	}
	)* )?
	RPAREN
	)?
	{
		// stack parameters
		for (int i = toList.size() - 1; i >=0; i--)
		{
			quadTable.add(new Quad(null, "stack", null, null, (String)fromList.get(i), null));
		}

		quadTable.add(new Quad(null, "call", null, null, procedureName, null));

		// unstack and copy back parameters
		for (int i = 0; i < toList.size(); i++)
		{
			String ss = (String)fromList.get(i);
			Symbol sym = symbolTable.get(ss);
			if (sym.value == null)
			{
				quadTable.add(new Quad(null, "unstack", null, null, (String)ss, null));
			}
			else
			{
				quadTable.add(new Quad(null, "unstack", null, null, null, null));
			}
		}
	}
	;
	
printExpression[ParseState state]
	:
	expression[state]
	{
		// Create a temporary variable for the standard output path which will be copied to p0
		String id0 = makeTempVar(state);
		symbolTable.add(new Symbol(id0, "BYTE", "1", typeTable.getSizeForType("BOOLEAN"), 0, false));

		// Create p0 and p1 symbols
		String pid = "print" + state.type.toLowerCase();

		symbolTable.add(new Symbol(pid + "_p0", "BYTE", null, typeTable.getSizeForType("BYTE"), 1, true));
		if (state.type.equals("STRING"))
		{
			Symbol v = (Symbol)symbolTable.get(pid + "_p1");
			Integer size = state.size;
			if (v != null && size < v.size)
			{
				size = v.size;
			}
			symbolTable.add(new Symbol(pid + "_p1", "STRING", null, size, 2, true));
		}
		else
		{
			symbolTable.add(new Symbol(pid + "_p1", state.type, null, state.size, 2, true));
		}
		
		quadTable.add(new Quad(null, "cp", id0, null, pid + "_p0", null));
		quadTable.add(new Quad(null, "cp", state.name, null, pid + "_p1", null));
		quadTable.add(new Quad(null, "call", null, null, pid, null));
	}
	;
	
printStatement[ParseState state]
	@init
	{
		int newline = 1;
	}
	:
	PRINT
	(printExpression[state] ((SEMI
	{
		newline = 0;
	}
	| COMMA)
	(printExpression[state]
	{
		newline = 1;
	}
	)?)*)?
 	{
		if (newline == 1)
		{
			// Create a temporary variable for the standard output path which will be copied to p0
			String id0 = makeTempVar(state);
			symbolTable.add(new Symbol(id0, "BYTE", "1", typeTable.getSizeForType("BYTE"), 0, false));
	
			// Create p0 and p1 symbols
			String pid = "printcr";
	
			symbolTable.add(new Symbol(pid + "_p0", "BYTE", null, typeTable.getSizeForType("BYTE"), 1, true));
			
			quadTable.add(new Quad(null, "cp", id0, null, pid + "_p0", null));
	
			quadTable.add(new Quad(null, "call", null, null, "printcr", null));
		}
	}
	;




modFunction[ParseState state]
	: MOD LPAREN expression[state] COMMA expression[state] RPAREN
	;

notFunction[ParseState state]
	: NOT LPAREN expression[state]RPAREN
	;

peekFunction[ParseState state]
	:
	PEEK
	LPAREN expression[state]
	{
		String pid = "peek";
	
		symbolTable.add(new Symbol(pid + "_p0", "INTEGER", null, typeTable.getSizeForType("INTEGER"), 1, true));
		symbolTable.add(new Symbol(pid + "_r0", "BYTE", null, typeTable.getSizeForType("BYTE"), 0, true));

		quadTable.add(new Quad(null, "cp", state.name, null, pid + "_p0", null));

		quadTable.add(new Quad(null, "call", null, null, pid, null));
		quadTable.add(new Quad(null, "cp", pid + "_r0", null, state.name, null));
	}
	RPAREN
	;

trueFunction[ParseState state]
	:
	TRUE
	{
		String id = makeTempVar(state);

		state.type = "BOOLEAN";
		state.value = "TRUE";
		state.name = id;
		symbolTable.add(new Symbol(id, state.type, state.value, typeTable.getSizeForType("BOOLEAN"), 0, false));
	}
	;

inputStatement[ParseState state]
	:
	INPUT
	(STRING
	{
		// Create a temporary variable for the standard output path which will be copied to p0
		String id0 = makeTempVar(state);
		symbolTable.add(new Symbol(id0, "BYTE", "1", typeTable.getSizeForType("BYTE"), 0, false));

		// Create p0 and p1 symbols
		String pid = "printstring";

		symbolTable.add(new Symbol(pid + "_p0", "BYTE", null, typeTable.getSizeForType("BYTE"), 1, true));
		String s = $STRING.text;
		Symbol v = (Symbol)symbolTable.get(pid + "_p1");
		int size = s.length() - 2;
		if (v != null && size < v.size)
		{
			size = v.size;
		}
		symbolTable.add(new Symbol(pid + "_p1", "STRING", null, size, 2, true));
			
		// Create a temporary variable for the standard output path which will be copied to p0
		String id1 = makeTempVar(state);
		symbolTable.add(new Symbol(id1, "STRING", s, s.length() - 2, 0, false));

		quadTable.add(new Quad(null, "cp", id0, null, pid + "_p0", null));
		quadTable.add(new Quad(null, "cp", id1, null, pid + "_p1", null));
		quadTable.add(new Quad(null, "call", null, null, pid, null));
	}
	)?
	((COMMA | SEMI)?
	variableReference[state]
	{
		// Create a temporary variable for the standard input path which will be copied to p0
		String id0 = makeTempVar(state);
		symbolTable.add(new Symbol(id0, "BYTE", "0", typeTable.getSizeForType("BYTE"), 0, false));
//		String id1 = state.currProcName + separator + state.name;
//		symbolTable.add(new Symbol(state.name, state.type, state.value, state.size, 0, false));

		// Create p0 and p1 symbols
		String pid = "input" + state.type.toLowerCase();
		
		symbolTable.add(new Symbol(pid + "_p0", "BYTE", null, typeTable.getSizeForType("BYTE"), 1, true));
		quadTable.add(new Quad(null, "cp", id0, null, pid + "_p0", null));

		Symbol v = (Symbol)symbolTable.get(pid + "_p1");
		Integer size = state.size;
		if (v != null && size < v.size)
		{
			size = v.size;
		}
//		symbolTable.add(new Symbol(pid + "_p1", "STRING", null, size, 0, 1));
		symbolTable.add(new Symbol(pid + "_p1", state.type, null, size, 2, true));

		if (state.type.equals("STRING"))
		{
			Symbol s = (Symbol)symbolTable.get(pid + "_p1");
			
			if (state.size > s.size)
			{
				s.size = state.size;
				symbolTable.add(s);
			}

			id0 = makeTempVar(state);
			size = s.size;
			symbolTable.add(new Symbol(id0, "INTEGER", size.toString(), typeTable.getSizeForType("INTEGER"), 0, false));
			symbolTable.add(new Symbol(pid + "_p2", "INTEGER", null, typeTable.getSizeForType("INTEGER"), 3, true));
			quadTable.add(new Quad(null, "cp", id0, null, pid + "_p2", null));
		}

		quadTable.add(new Quad(null, "call", null, null, pid, null));
		quadTable.add(new Quad(null, "cp", pid + "_p1", null, state.name, null));
	}
	)+
	;
	
falseFunction[ParseState state]
	:
	FALSE
	{
		String id = makeTempVar(state);

		state.type = "BOOLEAN";
		state.value = "FALSE";
		state.name = id;
		symbolTable.add(new Symbol(id, state.type, state.value, typeTable.getSizeForType("BOOLEAN"), 0, false));
	}
	;

constantValue[ParseState state]
	:
	INTLIT
	{
		String id = makeTempVar(state);

		state.type = "INTEGER";
		state.value = $INTLIT.text;
		state.name = id;
		state.size = 2;
		symbolTable.add(new Symbol(id, state.type, state.value, state.size, 0, false));
	}
	| REALLIT
	{
		String id = makeTempVar(state);

		state.type = "REAL";
		state.value = $REALLIT.text;
		state.name = id;
		state.size = 5;
		symbolTable.add(new Symbol(id, state.type, state.value, state.size, 0, false));
	}
	| STRING
	{
		String id = makeTempVar(state);

		state.type = "STRING";
		state.value = $STRING.text;
		state.name = id;
		state.size = state.value.length() - 2;
		symbolTable.add(new Symbol(id, state.type, state.value, state.size, 0, false));
	}
	;
	
variableReference[ParseState state]
	@init
	{
		/* Ragin' Basic assumes undeclared variables of type REAL. */
		String idPostfix = "";
		state.type = "REAL";
		state.value = "0.0";
		state.size = 5;
	}:	
	ID ('$'
	{
		/* $ follows ID, so this is a string */
		idPostfix = "$";
		state.type = "STRING";
		state.value = null;
		state.size = 80;
	}
	)?
	{
		String id = state.currProcName + separator + $ID.text + idPostfix;
		Symbol s = (Symbol)symbolTable.get(id);
		
		state.name = id;

		if (s == null)
		{
			s = new Symbol(id, state.type, state.value, state.size, 0, false);
			symbolTable.add(s);
		}
		else
		{
			state.type = s.type;
			state.value = s.value;
			state.size = s.size;
		}
	}
	;
	
primitiveElement[ParseState state]
	:	
	trueFunction[state]
	| falseFunction[state]
	| notFunction[state]
	| modFunction[state]
	| peekFunction[state]
	| variableReference[state]
	| constantValue[state]
	| LPAREN expression[state] RPAREN
	;

signExpression[ParseState state]
	@init
	{
		int neg = 0, pos = 0;
	}
	:	
	(ADD_OP
	{
		// count up number of +'s and -'s
		String sn = $ADD_OP.text;
		
		if (sn.equals("-"))
		{
			neg++;
		}
		else
		{
			pos++;
		}
	}
	)* primitiveElement[state]
	{
		// check types (allow real/integer only to have +/- in front)
		if ((pos > 0 || neg > 0) && (typeTable.isNumber(state.type) == false))
		{
			RaginBasicFrontEnd.reportError("sign token is only applicable to BYTE, INTEGER and REAL types");
			return;
		}

		// If we have an odd number of negatives, negate element
		if (neg \% 2 == 1)
		{
			String id = makeTempVar(state);
			Symbol s = new Symbol(id, state.type, null, state.size, 0, false);
			symbolTable.add(s);
			quadTable.add(new Quad(null, "cp", state.name, null, id, null));
			quadTable.add(new Quad(null, "neg", null, null, id, null));
			state.name = id;
		}
	}
	;
	
multiplyingExpression[ParseState state]
	@init
	{
		ParseState r = new ParseState();
		r.currProcName = state.currProcName;
	}
	:	
	signExpression[state]
	(MULT_OP signExpression[r]
	{
		if ((typeTable.isNumber(state.type) == false) || (typeTable.isNumber(r.type) == false))
		{
			RaginBasicFrontEnd.reportError("illegal expression: " + state.type + " " + $MULT_OP.text + " " + r.type);
			return;
		}

		String larger = typeTable.largerType(typeTable.largerType(state.type, r.type), "INTEGER");
		int size = typeTable.getSizeForType(larger);
		String id = makeTempVar(state);
		Symbol s = new Symbol(id, larger, null, size, 0, false);
		symbolTable.add(s);
		quadTable.add(new Quad(null, $MULT_OP.text, state.name, r.name, s.name, null));
		
		state.name = id;
	}
	)*
	;
	
addingExpression[ParseState state]
	@init
	{
		ParseState r = new ParseState();
		r.currProcName = state.currProcName;
	}
	:
	multiplyingExpression[state]
	(ADD_OP multiplyingExpression[r]
	{
		if (state.type.equals("STRING") && r.type.equals("STRING"))
		{
			if ($ADD_OP.text.equals("+") == false)
			{
				RaginBasicFrontEnd.reportError("illegal expression: " + state.type + " " + $ADD_OP.text + " " + r.type);
				return;
			}
			state.size = state.size + r.size;
		}
		else
		{
			state.size = typeTable.getSizeForType(state.type);

			if ((typeTable.isNumber(state.type) == false) || (typeTable.isNumber(r.type) == false))
			{
				RaginBasicFrontEnd.reportError("illegal expression: " + state.type + " " + $ADD_OP.text + " " + r.type);
				return;
			}
		}


		String id = makeTempVar(state);
		String larger = typeTable.largerType(typeTable.largerType(state.type, r.type), "INTEGER");
		int size = typeTable.getSizeForType(larger);
		Symbol s = new Symbol(id, larger, null, size, 0, false);
		symbolTable.add(s);
		quadTable.add(new Quad(null, $ADD_OP.text, state.name, r.name, s.name, null));
		state.name = id;
	}
	)*
	;
	
relationalExpression[ParseState state]
	@init
	{
		ParseState r = new ParseState();
		r.currProcName = state.currProcName;
		String op = null;
	}
	:
	addingExpression[state]
	(
	(RELATIONAL_OP {op = $RELATIONAL_OP.text;} | EQUALS {op = $EQUALS.text;}) addingExpression[r]
	{
		state.type = "BOOLEAN";
		String id = makeTempVar(state);
		Symbol s = new Symbol(id, "BOOLEAN", null, typeTable.getSizeForType("BOOLEAN"), 0, false);
		symbolTable.add(s);
		quadTable.add(new Quad(null, op, state.name, r.name, s.name, null));
		state.name = s.name;
	}
	)*
	;


expression[ParseState state]
	@init
	{
		ParseState r = new ParseState();
		r.currProcName = state.currProcName;
	}
	:	
	relationalExpression[state]
	(BOOLEAN_OP relationalExpression[r]
	{
//		if ((typeTable.isNumber(l.type) == 1) && (typeTable.isNumber(r.type) == 1))
		{
			state.type = "BOOLEAN";
			String id = makeTempVar(state);
			Symbol s = new Symbol(id, "BOOLEAN", null, typeTable.getSizeForType("BOOLEAN"), 0, false);
			symbolTable.add(s);
			quadTable.add(new Quad(null, $BOOLEAN_OP.text, state.name, r.name, s.name, null));
			state.name = s.name;
		}
	}
	)*
	;
	

// START:tokens
INTLIT  	: '0'..'9'+ | '$' ('0'..'9'|'A'..'F'|'a'..'f')+ ;
REALLIT 	: '0'..'9'+ ( '.' ('0'..'9')* (('e' | 'E') ('+' | '-')? ('0'..'9')+)?) ;
ADD_OP		: ('+' | '-') ;
BOOLEAN_OP 	: ('AND' | 'OR' | 'XOR') ;
MULT_OP 	: ('*' | '/' | '%') ;
EQUALS   	: '=' ;
RELATIONAL_OP 	: ('<>' | '>' | '>=' | '<' | '<=');
RPAREN		: (')') ;
LPAREN		: ('(') ;
COMMA		: (',') ;
SEMI		: (';') ;
COLON		: (':') ;
AS	 	: ('AS') ;
PROGRAM 	: ('PROGRAM') ;
PROCEDURE 	: ('PROCEDURE') ;
DIM 		: ('DIM') ;
PARAM 		: ('PARAM') ;
TYPE 		: ('TYPE');
GOTO 		: ('GOTO') ;
LET 		: ('LET');
CALL 		: ('RUN');
EXIT 		: ('EXIT');
POKE 		: ('POKE') ;
SHELL 		: ('SHELL') ;
END 		: ('END') ;
DO 		: ('DO') ;
WHILE		: ('WHILE') ;
ENDWHILE	: ('ENDWHILE') ;
IF 		: ('IF') ;
THEN 		: ('THEN') ;
ELSE 		: ('ELSE') ;
ENDIF 		: ('ENDIF') ;
REPEAT 		: ('REPEAT') ;
UNTIL 		: ('UNTIL') ;
FOR 		: ('FOR') ;
TO 		: ('TO') ;
DOWNTO 		: ('DOWNTO') ;
STEP 		: ('STEP') ;
NEXT 		: ('NEXT') ;
INPUT 		: ('INPUT') ;
PRINT 		: ('PRINT') ;
TRUE    	: ('TRUE') ;
FALSE    	: ('FALSE') ;
MOD    		: ('MOD') ;
NOT	    	: ('NOT') ;
PEEK	    	: ('PEEK') ;
ID  		: ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9' | '_')* ;
NEWLINE		: (('\r' ? '\n') | '\\') ;
WS  		: (' ' | '\t')+ {skip();} ;
//STRING 		: '"' ('a' .. 'z')* '"' ;
STRING		: '"' (~'"')* '"' ;
COMMENT		: ('(*' | 'REM ') ~'\n'* '\n' {skip();} ;
// END:tokens
