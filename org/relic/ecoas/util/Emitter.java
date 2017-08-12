package org.relic.ecoas.util;

import java.util.LinkedList;
import org.relic.ecoas.util.*;

public interface Emitter
{
	int[] generateBinary(String moduleName, LinkedList<Assembly> ll);
}
