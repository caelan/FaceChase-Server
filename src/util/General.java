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
}
