package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

public class FileSystem {
    public static void delete(File f) throws IOException {
        if (f.isDirectory()) {
          for (File c : f.listFiles())
            delete(c);
        }
        if (!f.delete())
          throw new FileNotFoundException("Failed to delete file: " + f);
      }
    
    public static FilenameFilter makeFileFilter(String e)
    {
        final String extension = e;
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("." + extension);
            }
        };
    }
    
    public static FilenameFilter makeFolderFilter()
    {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir.getAbsolutePath() + "\\" + name).isDirectory();
            }
        };
    }
}