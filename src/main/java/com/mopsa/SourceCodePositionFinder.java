
package com.mopsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import magpiebridge.util.SourceCodeInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class SourceCodePositionFinder {

  public static SourceCodeInfo findCode(File cFile, int lineNumber) {
    SourceCodeInfo info = new SourceCodeInfo();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(cFile));
      String line;
      int i = 0;
      while ((line = reader.readLine()) != null) {
        i++;
        if (i == lineNumber) {
          int column = 0;
          line = line.split("//")[0];
          for (char c : line.toCharArray()) {
            if (c != ' ') {
              break;
            }
            column++;
          }
          info.code = line.trim();
          Position start = new Position(lineNumber - 1, column);
          Position end = new Position(lineNumber - 1, column + info.code.length());
          info.range = new Range(start, end);
          info.url = new URL("file://" + cFile.getAbsolutePath());
          break;
        }
      }
      reader.close();
      return info;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static File find(File dir, String fileName) {
    Collection<File> files = FileUtils.listFiles(dir, null, true);
    for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
      File file = iterator.next();
      String fileExtension = getFileExtension(file);
      if ((fileExtension.equals(".c") || fileExtension.equals(".h"))
          && file.getName().equals(fileName + fileExtension)) {
        return file;
      }
    }
    System.out.println("Couldn't find " + fileName + " in directory " + dir.toString());
    return null;
  }

  private static String getFileExtension(File file) {
    String name = file.getName();
    int lastIndexOf = name.lastIndexOf(".");
    if (lastIndexOf == -1) {
      return ""; // Empty extension
    }
    return name.substring(lastIndexOf);
  }
}
