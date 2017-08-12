package org.relic.ecoas.util;

import java.util.LinkedHashMap;

public class InstructionMap extends LinkedHashMap<String, Object>
{
	private static final long serialVersionUID = 1L;

	public Object get(String key)
	{
		return super.get(key.toLowerCase());
	}
}
