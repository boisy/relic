package org.relic.ecoas.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class Unzip
{
   static final int BUFFER = 2048;

   static public LinkedList<String> doUnzip(String file)
   {
	   LinkedList<String> files = new LinkedList<String>();
	   
      try
      {
         BufferedOutputStream dest = null;
         FileInputStream fis = new 
         FileInputStream(file);
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
         ZipEntry entry;
         while((entry = zis.getNextEntry()) != null)
         {
//            System.out.println("Extracting: " +entry);
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            FileOutputStream fos = new FileOutputStream("/tmp/" + entry.getName());
            files.add("/tmp/" + entry.getName());
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1)
            {
               dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
         }
         zis.close();
      } catch(Exception e) {
         e.printStackTrace();
      }
      
      return files;
   }
}