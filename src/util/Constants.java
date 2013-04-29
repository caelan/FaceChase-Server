package util;

public interface Constants {
    public static final int classifierType = 1;
    public static final int faceSize = 100;
    public static final String testFacesDir = System.getProperty("user.dir") + "\\TestFaces";
    public static final String facialDetectLoadFile = System.getProperty("user.dir") + "\\haarcascades\\haarcascade_frontalface_alt.xml";
    //public static final String eyesDetectLoadFile = System.getProperty("user.dir") + "\\haarcascades\\haarcascade_mcs_eyepair_small.xml";
    //public static final String eyesDetectLoadFile = System.getProperty("user.dir") + "\\haarcascades\\haarcascade_mcs_eyepair_big.xml";
    public static final String eyesDetectLoadFile = System.getProperty("user.dir") + "\\haarcascades\\haarcascade_eye.xml";
    //public static final String eyesDetectLoadFile = System.getProperty("user.dir") + "\\haarcascades\\haarcascade_mcs_lefteye.xml";
    //public static final String eyesDetectLoadFile = System.getProperty("user.dir") + "\\haarcascades\\haarcascade_mcs_righteye.xml";
    //public static final String eyesDetectLoadFile = System.getProperty("user.dir") + "\\haarcascades\\haarcascade_eye_tree_eyeglasses.xml";

    

    //public static final String itemDelim = "ß";
    public static final String itemDelim = "\\|";
}
