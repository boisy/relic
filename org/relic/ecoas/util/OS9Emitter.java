package org.relic.ecoas.util;

import java.util.Iterator;
import java.util.LinkedList;

public class OS9Emitter implements Emitter
{
	public OS9Emitter()
	{
	}

	public int[] generateBinary(String moduleName, LinkedList<Assembly> ll)
	{
		// find the mainline psect
		int codeSize = 0, stackSize = 0;
		int executionOffset = 14 + moduleName.length();
		int[] crc = {0xFF, 0xFF, 0xFF};
		Assembly mainline = null;
		int n = 0;
		
		int[] b = new int[65536];

		for (Iterator<Assembly> asit = ll.iterator(); asit.hasNext(); )
		{
			Assembly as = asit.next();

			codeSize += as.objectCode.size() + as.initializedData.size() + as.initializedDPData.size();
			stackSize += as.sectionStackSize + as.uninitializedDataCount + + as.uninitializedDPDataCount + as.initializedData.size() + as.initializedDPData.size();
			
			if (as.sectionTypeLang != 0)
			{
				mainline = as;
				executionOffset += as.sectionEntryPoint;
			}
			
			if (mainline == null)
			{
				executionOffset += as.objectCode.size();
			}
		}

		if (mainline == null)
		{
			return null;	//"No mainline psect -- cannot emit object";
		}

		/* Emit sync bytes */
		b[n++] = 0x87;
		b[n++] = 0xCD;
		
		/* Emit module size */
		int module_size = codeSize + 14 + 3 + moduleName.length();
		b[n++] = ((module_size >> 8 ) & 0xFF);
		b[n++] = ((module_size >> 0 ) & 0xFF);
		
		/* Emit name offset */
		int name_offset = 13;
		b[n++] = ((name_offset >> 8 ) & 0xFF);
		b[n++] = ((name_offset >> 0 ) & 0xFF);
		
		/* Emit type/language and attribute/revision */
		b[n++] = ((mainline.sectionTypeLang >> 0 ) & 0xFF);
		b[n++] = ((mainline.sectionAttrRev >> 0 ) & 0xFF);
		
		/* Emit header check */
		b[n++] = 0; // will patch later
		
		/* Module type specific output */
		switch (mainline.sectionTypeLang & 0xF0)
		{
			case 0x10:
			case 0x20:
			case 0x30:
			case 0x40:
			case 0x50:
			case 0x60:
			case 0x70:
			case 0x80:
			case 0x90:
			case 0xA0:
			case 0xB0:
			case 0xC0:	/* Systm */
			case 0xD0:	/* FMgr */
			case 0xE0:	/* Drvr */
			case 0xF0:	/* Desc */
				/* output exec offset */
				b[n++] = ((executionOffset >> 8 ) & 0xFF);
				b[n++] = ((executionOffset >> 0 ) & 0xFF);
				/* output storage size */
				b[n++] = ((stackSize >> 8 ) & 0xFF);
				b[n++] = ((stackSize >> 0 ) & 0xFF);
				break;
		}

		int ii;
		for (ii = 0; ii < moduleName.length() - 1; ii++)
		{
			b[n++] = ((moduleName.charAt(ii) >> 0 ) & 0xFF);
		}
		b[n++] = ((moduleName.charAt(ii) >> 0 ) & 0xFF | 0x80);

		b[n++] = ((mainline.sectionEdition >> 0 ) & 0xFF);

		// TEXT section: write out object code
		for (Iterator<Assembly> asit = ll.iterator(); asit.hasNext(); )
		{				
			Assembly as = asit.next();
			
			for (int i = 0; i < as.objectCode.size(); i++)
			{
				b[n++] = as.objectCode.get(i);
			}
		}

		// DATA section: write out initialized DP data
		int totalInitializedDP = 0;
		for (Iterator<Assembly> asit = ll.iterator(); asit.hasNext(); )
		{				
			Assembly as = asit.next();
			totalInitializedDP += as.initializedDPData.size();
		}
		
		// write out total initialized DP data size;
		b[n++] = ((totalInitializedDP >> 8) & 0xFF);
		b[n++] = ((totalInitializedDP >> 0) & 0xFF);
	
		for (Iterator<Assembly> asit = ll.iterator(); asit.hasNext(); )
		{				
			Assembly as = asit.next();
			
			for (int i = 0; i < as.initializedDPData.size(); i++)
			{
				b[n++] = as.initializedDPData.get(i);
			}
		}

		// DATA section: write out initialized data
		int totalInitialized = 0;
		for (Iterator<Assembly> asit = ll.iterator(); asit.hasNext(); )
		{				
			Assembly as = asit.next();
			totalInitialized += as.initializedData.size();
		}
		
		// write out total initialized DP data size;
		b[n++] = ((totalInitialized >> 8) & 0xFF);
		b[n++] = ((totalInitialized >> 0) & 0xFF);

		for (Iterator<Assembly> asit = ll.iterator(); asit.hasNext(); )
		{				
			Assembly as = asit.next();
			
			for (int i = 0; i < as.initializedData.size(); i++)
			{
				b[n++] = as.initializedData.get(i);
			}
		}
		
		// Data-text and Data-data reference counts
		b[n++] = 0;
		b[n++] = 0;

		b[n++] = 0;
		b[n++] = 0;

		// write nul terminated program name
		for (int i = 0; i < moduleName.length(); i++)
		{
			b[n++] = (moduleName.charAt(i));
		}
		b[n++] = 0;
		
		// patch module header with new module size
		b[2] = ((n + 3) >> 8) & 0xFF;
		b[3] = ((n + 3) >> 0) & 0xFF;

		int header_check = 0;
		for (int i = 0; i < 8; i++)
		{
			header_check ^= b[i];
		}
		b[8] = ((~header_check ) & 0xFF);
		
		// iterate through and compute CRC
		for (int i = 0; i < n; i++)
		{
			computeCRC(b[i], crc);
		}
		// write final CRC
		b[n++] = ~crc[0];
		b[n++] = ~crc[1];
		b[n++] = ~crc[2];

		int[] bcopy = new int[n];
		System.arraycopy(b, 0, bcopy, 0, n);

		return bcopy;
	}
	
	void computeCRC(int a, int[] crc)
	{
		a ^= ((crc[0] & 0xFF) & 0xFF);
		crc[0] = (crc[1] & 0xFF);
		crc[1] = (crc[2] & 0xFF);
		crc[1] ^= (((a & 0xFF) >> 7) & 0xFF);
		crc[2] = (((a & 0xFF) << 1) & 0xFF);
		crc[1] ^= (((a & 0xFF) >> 2) & 0xFF);
		crc[2] ^= (((a & 0xFF) << 6) & 0xFF);
		a ^= ((a & 0xFF) << 1);
		a ^= ((a & 0xFF) << 2);
		a ^= ((a & 0xFF) << 4);

		if ((a & 0x80) != 0)
		{
			crc[0] ^= 0x80;
			crc[2] ^= 0x21;
		}
		
		crc[0] &= 0xFF;
		crc[1] &= 0xFF;
		crc[2] &= 0xFF;

		return;
	}	
}
