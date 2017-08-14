package org.relic.util;

import java.util.*;
import org.antlr.runtime.RecognizerSharedState;

public class ParseState extends RecognizerSharedState
{
	public String currProcName;
	public String name;
	public String type;
	public String value;
	public int scope;
	public int level;
	public int paramCount;
	public int index;
	public int size;
	public int idCount;
	public ArrayList paramlist;

	public ParseState()
	{
		currProcName = null;
		name = null;
		type = null;
		value = null;
		scope = 0;
		level = 0;
		paramCount = 0;
		index = 0;
		size = 0;
		idCount = 0;
		paramlist = new ArrayList();
	}
}
