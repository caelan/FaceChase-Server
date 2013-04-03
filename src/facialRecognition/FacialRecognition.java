package facialRecognition;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import util.Pair;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/*
 * TODO
 * Retinex Algorithm to preprocess
 * rotations
 * Shuffle inputs
 * Add faces to library
 * @author Caelan Garrett
 */

public class FacialRecognition 
{
    private final String learningData = "facialRecognition.dat";
    private final String helperData = "helperData.dat";
    public FaceRecognizer classifier;
    public HashSet<Integer> ids;
    public FacialDetection detect;
    public int size;
    private int algorithm;
    
    /**
     * 
     * @param type determines the type of classifier - 0 is FisherFace, 1 is LBPHFace, and default is EigenFace
     */
    public FacialRecognition(boolean loadLast, String directory, int type, int s)
    {
        File loadFile1 = new File(System.getProperty("user.dir") + "\\" + learningData);      
        File loadFile2 = new File(System.getProperty("user.dir") + "\\" + helperData);         

        if(loadLast && loadFile1.exists() && loadFile2.exists())
        {
            System.out.println("Loading Data");
            if(load())
                return;
                
        }
        System.out.println("Training Data");
        train(directory, type, s);
    }
        
    public void train(String directory, int type, int s)
    {
        detect = new FacialDetection();
        size = s;
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
        for (int i = 0; i < faceFileList.size(); i++) {
            img = extractFace(cvLoadImage(faceFileList.get(i).getAbsolutePath()));
            labels[i] = labelList.get(i);
            
            faces.put(i, img);
        }

        switch(type){
        case 0:
                classifier = createFisherFaceRecognizer();
                algorithm = 0;
                break;
        case 1:
                classifier = createLBPHFaceRecognizer();
                algorithm = 1;
                break;
        default:
                classifier = createEigenFaceRecognizer();
                algorithm = 2;
                break;
        }
        
        long loadTime = System.nanoTime();
        System.out.println("Load Time: " + (loadTime - startTime)/1000000000.0 + " seconds");
        
        classifier.train(faces, labels);  
        
        long trainTime = System.nanoTime();
        System.out.println("Train Time: " + (trainTime - loadTime)/1000000000.0 + " seconds");
        
        save();      
    }
    
    public void update(MatVector faces, int[] labels)
    {
        classifier.update(faces, labels);  
    }
    
    public IplImage extractFace(IplImage img)
    {
        IplImage face = detect.findFace(img);
        if(face == null)
        {
            return detect.preprocess(img, size, size);
        }
        else
        {
            return detect.preprocess(face, size, size);
        }
    }
    
    public int predict(IplImage img)
    {
        return classifier.predict(extractFace(img));
    }
    
    public Pair<Integer, Double> predictConfidence(IplImage img)
    {
        int[] predictedLabel = new int[1];
        double[] confidence = new double[1];
        classifier.predict(extractFace(img), predictedLabel, confidence);
        return new Pair<Integer, Double>(predictedLabel[0], confidence[0]);
    }
    
    public boolean save()
    {
        long startTime = System.nanoTime();                
        
        try{
            classifier.save(learningData);
            
            FileWriter fstream = new FileWriter(helperData);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("" + algorithm +"\n");
            out.write("" + size +"\n");
            for(int elem: ids)
                out.write("" + elem +"\n");

            out.close();
        }
        catch (Exception e)
        {
            System.err.println("Could not save classifier: " + e.getMessage());
            return false;
        }
        
        long saveTime = System.nanoTime();
        System.out.println("Save Time: " + (saveTime - startTime)/1000000000.0 + " seconds");
        return true;
    }
    
    public boolean load()
    {
        long startTime = System.nanoTime();
        
        try 
        {
            detect = new FacialDetection();
            ids = new HashSet<Integer>();
            
            Scanner scanner =  new Scanner(new File(helperData));
            algorithm = Integer.parseInt(scanner.nextLine());
            size = Integer.parseInt(scanner.nextLine());
           
            while (scanner.hasNextLine())
            {
                ids.add(Integer.parseInt(scanner.nextLine()));
            }
            
            classifier = createEigenFaceRecognizer();
            classifier.load(learningData);
        }
        catch (Exception e)
        {
            System.err.println("Could not load classifier: " + e.getMessage());
            return false;
        }        
     
        long loadTime = System.nanoTime();
        System.out.println("Load Time: " + (loadTime - startTime)/1000000000.0 + " seconds");
        return true;
    }
        
    public static void main(String[] args) {
        String directory = System.getProperty("user.dir") + "\\TestFaces";
        FacialRecognition facialRec = new FacialRecognition(true, directory, 2, 100);

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
