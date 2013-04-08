package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class General {
    public static void shuffleArray(Object[] array)
    {
        ArrayList<Object> arrayList = new ArrayList<Object>(Arrays.asList(array));
        Collections.shuffle(arrayList, new Random(System.nanoTime()));
        arrayList.toArray(array);
    }
    public static Integer parseInt(String s)
    {
        if(s.equals("null"))
            return null;
        else
            return Integer.parseInt(s);
    }
    
    public static Double parseDouble(String s)
    {
        if(s.equals("null"))
            return null;
        else
            return Double.parseDouble(s);
    }
}
