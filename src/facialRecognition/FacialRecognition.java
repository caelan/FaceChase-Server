package facialRecognition;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import util.Constants;
import util.FileSystem;
import util.General;
import util.Pair;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;

/*
 * TODO
 * Retinex Algorithm to preprocess
 * rotations
 * Add faces to library
 * @author Caelan Garrett
 */

public class FacialRecognition 
{
    private final String learningData = "\\facialRecognition.dat";
    private final String helperData = "\\helperData.dat";
    public FaceRecognizer classifier;
    public HashMap<Integer, Integer> ids;
    public FacialDetection detect;
    /** type determines the type of classifier - 0 is FisherFace, 1 is LBPHFace, and default is EigenFace **/
    public int size;
    private int algorithm;
    private String directory;
    private Integer maxFacesPerPerson;

    public FacialRecognition(String directory)
    {
        this.directory = directory;
        if(!load())
            throw new RuntimeException("Unable to load file");
    }
    
    public FacialRecognition(String directory, int type, int size, Integer maxFaces)
    {        
        this.directory = directory;
        createClassifier(type);
        this.algorithm = type;
        this.size = size;
        ids = new HashMap<Integer, Integer>();
        detect = new FacialDetection();
        maxFacesPerPerson = maxFaces;
    }
    
    public FacialRecognition(int testFaces, String directory, int type, int size, Integer maxFaces)
    {        
        this(directory, type, size, maxFaces);
        
        Pair<MatVector, int[]> pair = loadTestFaces(Constants.testFacesDir, testFaces);
        if(pair != null)       
            train(pair.getFirst(), pair.getSecond());       
    }

    public Pair<MatVector, int[]> loadTestFaces(String directory, int number)
    {
        File root = new File(directory);
        if(!root.exists() || !root.isDirectory())
            return null;

        File[] people = root.listFiles(FileSystem.makeFolderFilter());
        General.shuffleArray(people);
        
        //ArrayList<File> people2 = new ArrayList<File>(Arrays.asList(people));
        //Collections.shuffle(people2, new Random(System.nanoTime()));
        //people2.toArray(people);

        if(people.length == 0)
        {
            System.out.println("No Test Faces to Load");
            return null;
        }
        else if(people.length < number)
        {
            System.out.println("Could Only Load " + people.length + " TestFaces (Wanted " + number + ")");
            number = people.length;
        }

        FilenameFilter pgmFilter = FileSystem.makeFileFilter("pgm");

        LinkedList<File> faceFileList = new LinkedList<File>();
        LinkedList<Integer> labelList = new LinkedList<Integer>();
        for(int i = 0; i < number; i ++)
        {
            File person = people[i];
            int id = Integer.parseInt(person.getName().substring(1));
            File[] faces = person.listFiles(pgmFilter);
            General.shuffleArray(faces);
            
            int amount;
            if(maxFacesPerPerson == null)
                amount = faces.length;
            else
                amount = Math.min(maxFacesPerPerson, faces.length);
            for(int k = 0; k < amount; k++)
            {
                File faceFile = faces[k];
                faceFileList.add(faceFile);
                labelList.add(-1*id);
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
        
        return new Pair<MatVector, int[]>(faces, labels);
    }
    
    private void createClassifier(int type)
    {
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
    }
        
    public synchronized void train(MatVector faces, int[] labels)
    {
        //long startTime = System.nanoTime();
        
        for(int i = 0; i < labels.length; i++)
        {
            int id = labels[i];
            if(!ids.containsKey(id))
            {
                ids.put(id, 1);
              
            }
            else if(maxFacesPerPerson == null || ids.get(id) <= maxFacesPerPerson)
            {
                ids.put(id, ids.get(id) + 1);
            }
            cvSaveImage(System.getProperty("user.dir") + "\\Processed Images\\face" + id +"_" + ids.get(id) + ".jpg", faces.getIplImage(i));
        }
                
        classifier.train(faces, labels);  

        //long trainTime = System.nanoTime();
        //System.out.println("Train Time: " + (trainTime - startTime)/1000000000.0 + " seconds");
    }
    
    public synchronized void update(MatVector faces, int[] labels) //Run in background?
    {
        //long startTime = System.nanoTime();        
        
        for(int i = 0; i < labels.length; i++)
        {
            int id = labels[i];
            if(!ids.containsKey(id))
            {
                ids.put(id, 1);
              
            }
            else if(maxFacesPerPerson == null || ids.get(id) <= maxFacesPerPerson)
            {
                ids.put(id, ids.get(id) + 1);
            }
            cvSaveImage(System.getProperty("user.dir") + "\\Processed Images\\face" + id +"_" + ids.get(id) + ".jpg", faces.getIplImage(i));
        }
        
        classifier.update(faces, labels);  
        
        //long updateTime = System.nanoTime();
        //System.out.println("Update Time: " + (updateTime - startTime)/1000000000.0 + " seconds");
    }
    
    public IplImage extractFace(IplImage img)
    {
        //IplImage face = detect.findRotatedFace(img);
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
    
    public IplImage extractFaceFail(IplImage img)
    {
        //IplImage face = detect.findRotatedFace(img);
        IplImage face = detect.findFace(img);
        if(face == null)
        {
            return null;
        }
        else
        {
            return detect.preprocess(face, size, size);
        }
    }
    
    public synchronized int predict(IplImage img)
    {
        return classifier.predict(extractFace(img));
    }
    
    public synchronized Pair<Integer, Double> predictConfidence(IplImage img)
    {
        int[] predictedLabel = new int[1];
        double[] confidence = new double[1];
        classifier.predict(extractFace(img), predictedLabel, confidence);
        return new Pair<Integer, Double>(predictedLabel[0], confidence[0]);
    }
    
    public synchronized Pair<Integer, Double> predictConfidenceFail(IplImage img)
    {
        int[] predictedLabel = new int[1];
        double[] confidence = new double[1];
        IplImage processed = extractFaceFail(img);
        if(processed == null)
            return null;
        cvSaveImage(System.getProperty("user.dir") + "\\Processed Images\\sentkill.jpg", processed); 
        classifier.predict(processed, predictedLabel, confidence);
        return new Pair<Integer, Double>(predictedLabel[0], confidence[0]);
    }
    
    public synchronized Pair<Integer, Double> predictConfidenceThreshold(IplImage img, double threshold)
    {
        int[] predictedLabel = new int[1];
        double[] confidence = new double[1];
        classifier.predict(extractFace(img), predictedLabel, confidence);
        if(confidence[0] < threshold) // 0.0 is the best
            return new Pair<Integer, Double>(predictedLabel[0], confidence[0]);
        else
            return new Pair<Integer, Double>(null, confidence[0]);
    }
    
    public synchronized boolean save()
    {
        //long startTime = System.nanoTime();                
        
        try{
            classifier.save(directory + learningData);
            
            FileWriter fstream = new FileWriter(directory + helperData);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("" + algorithm +"\n");
            out.write("" + size +"\n");
            for(int elem: ids.keySet())
                out.write("" + elem + " " + ids.get(elem) + "\n");

            out.close();
        }
        catch (Exception e)
        {
            System.err.println("Could not save classifier: " + e.getMessage());
            return false;
        }
        
        //long saveTime = System.nanoTime();
        //System.out.println("Save Time: " + (saveTime - startTime)/1000000000.0 + " seconds");
        return true;
    }
    
    public synchronized boolean load()
    {
        //long startTime = System.nanoTime();
        
        try 
        {
            detect = new FacialDetection();
            ids = new HashMap<Integer, Integer>();
            
            Scanner scanner =  new Scanner(new File(directory + helperData));
            algorithm = Integer.parseInt(scanner.nextLine());
            size = Integer.parseInt(scanner.nextLine());
           
            while (scanner.hasNextLine())
            {
                String[] split = scanner.nextLine().split(" ");
                ids.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            }
            
            createClassifier(algorithm);
            classifier.load(directory + learningData);
        }
        catch (Exception e)
        {
            System.err.println("Could not load classifier: " + e.getMessage());
            return false;
        }        
     
        //long loadTime = System.nanoTime();
        //System.out.println("Load Time: " + (loadTime - startTime)/1000000000.0 + " seconds");
        return true;
    }
        
    private static void test1()
    {
        String directory = System.getProperty("user.dir");
        FacialRecognition facialRec = new FacialRecognition(40, directory, 2, 100, null);
        //FacialRecognition facialRec = new FacialRecognition(directory);

        Random rand = new Random();
        int selectedID = (Integer)facialRec.ids.keySet().toArray()[rand.nextInt(facialRec.ids.size())];
        File person = new File(System.getProperty("user.dir") + "\\TestFaces\\s" + selectedID);
        
        FilenameFilter pgmFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pgm");
            }
        };
        File[] faces = person.listFiles(pgmFilter);
        File testImage = faces[rand.nextInt(faces.length)];
        int predictedID = -1*facialRec.predict(cvLoadImage(testImage.getAbsolutePath()));
        System.out.println("Actual ID: " + selectedID + ", Selected ID: " + predictedID);
    }
    
    private static void test2()
    {
        String directory = System.getProperty("user.dir") + "\\facialRecTest";
        FacialRecognition facialRec = new FacialRecognition(3, directory, 1, 100, 1);
        
        IplImage train = cvLoadImage(directory + "\\miriam1.jpg");
        IplImage test = cvLoadImage(directory + "\\caelan1.jpg");
        
        int label = 0;

        final MatVector faces = new MatVector(1);
        final int[] labels = new int[1];

        labels[0] = label;
        IplImage stuff = facialRec.extractFace(train);
        cvSaveImage(directory + "\\miriam1Face.jpg", stuff);
        cvSaveImage(directory + "\\caelan1Face.jpg", facialRec.extractFace(test));
        faces.put(0, stuff);
        
        facialRec.update(faces, labels);
        Pair<Integer, Double> pair = facialRec.predictConfidence(test);
        System.out.println("Actual ID: " + label + ", Selected ID: " + pair.getFirst() + ", Confidence: " + pair.getSecond());
    }
    
    public static void main(String[] args) 
    {
        //test1();
        test2();
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
