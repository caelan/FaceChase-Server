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
    
    public static Long parseLong(String s)
    {
        if(s.equals("null"))
            return null;
        else
            return Long.parseLong(s);
    }
    
    public static double nanoToMilli(long nano)
    {
        return nano/1000000.0;
    }
    
    public static double minToMilli(double min)
    {
        return min/(60.0*1000);
    }
}
