package facialRecognition;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

/*
 * TODO - save/load algorithm
 * Facial Detection
 * Retinex Algorithm to preprocess
 * Add faces to library
 * @author Caelan Garrett
 */

public class FacialRecognitionOld
{
    public FaceRecognizer classifier;
    public HashSet<Integer> ids;
    
    /**
     * 
     * @param type determines the type of classifier - 0 is FisherFace, 1 is LBPHFace, and default is EigenFace
     */
    public FacialRecognitionOld(String directory, int type)
    {
        ids = new HashSet<Integer>();
        long startTime = System.nanoTime();
        File root = new File(directory);

        FilenameFilter directoryFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir.getAbsolutePath() + "\\" + name).isDirectory();
            }
        };
        
        File[] people = root.listFiles(directoryFilter);
        
        if(people.length == 0)
        {
            System.out.println("No People to Load in Current Directory");
            return;
        }
        
        FilenameFilter pgmFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pgm");
            }
        };
        
        LinkedList<File> faceFileList = new LinkedList<File>();
        LinkedList<Integer> labelList = new LinkedList<Integer>();
        for(File person: people)
        {
            int id = Integer.parseInt(person.getName().substring(1));
            if(ids.contains(id))
                throw new RuntimeException("Two Folders with Same ID");
            ids.add(id);
            for(File faceFile: person.listFiles(pgmFilter))
            {
                faceFileList.add(faceFile);
                labelList.add(id);
            }
        }
        
        
        MatVector faces = new MatVector(faceFileList.size());
        int[] labels = new int[faceFileList.size()];

        IplImage img;
        IplImage grayImg;
        for (int i = 0; i < faceFileList.size(); i++) {
            img = cvLoadImage(faceFileList.get(i).getAbsolutePath());
            labels[i] = labelList.get(i);
            grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
            cvCvtColor(img, grayImg, CV_BGR2GRAY);
            cvEqualizeHist(grayImg, grayImg);
            faces.put(i, grayImg);
        }

        switch(type){
        case 0:
                classifier = createFisherFaceRecognizer();
                break;
        case 1:
                classifier = createLBPHFaceRecognizer();
                break;
        default:
                classifier = createEigenFaceRecognizer();
                break;
        }
        
        long loadTime = System.nanoTime();
        System.out.println("Load Time: " + (loadTime - startTime)/1000000000.0 + " seconds");
        
        classifier.train(faces, labels);  
        
        long trainTime = System.nanoTime();
        System.out.println("Train Time: " + (trainTime - loadTime)/1000000000.0 + " seconds");
        
    }
    
    public int predict(IplImage img)
    {
        IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(img, grayImg, CV_BGR2GRAY);
        cvEqualizeHist(grayImg, grayImg);
        return classifier.predict(grayImg);
    }
        
    public static void main(String[] args) {
        String directory = System.getProperty("user.dir") + "\\TestFaces";
        FacialRecognitionOld facialRec = new FacialRecognitionOld(directory, 2);

        Random rand = new Random();
        int selectedID = (Integer)facialRec.ids.toArray()[rand.nextInt(facialRec.ids.size())];
        File person = new File(System.getProperty("user.dir") + "\\TestFaces\\s" + selectedID);
        
        FilenameFilter pgmFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pgm");
            }
        };
        File[] faces = person.listFiles(pgmFilter);
        File testImage = faces[rand.nextInt(faces.length)];
        int predictedID = facialRec.predict(cvLoadImage(testImage.getAbsolutePath()));
        System.out.println("Actual ID: " + selectedID + ", Selected ID: " + predictedID);
    }
}

/*
 *Java CV 2.4.4
 *Install in C:/ (directly in)
 *Make sure the CLASSPATH and Path are correct
 *Restart when the paths are edited
 *https://code.google.com/p/javacv/ ----> get javacv-0.4-bin.zip
 *http://ganeshtiwaridotcomdotnp.blogspot.com/2011/12/opencv-javacv-eclipse-project.html
*/
