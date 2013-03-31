package facialRecognition;

import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class FacialDetection 
{
    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    public CvHaarClassifierCascade cascade;
    public IplImage preprocess(IplImage img)
    {
        IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1); 
        cvCvtColor(img, grayImg, CV_BGR2GRAY); 
        IplImage equImg = IplImage.create(grayImg.width(), grayImg.height(), IPL_DEPTH_8U, 1); 
        cvEqualizeHist(grayImg, equImg); 
        return equImg;
    }

    public IplImage preprocess(IplImage img, int scale)
    {
        IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1); 
        cvCvtColor(img, grayImg, CV_BGR2GRAY); 
        IplImage smallImg = IplImage.create(grayImg.width()/scale, grayImg.height()/scale, IPL_DEPTH_8U, 1); 
        cvResize(grayImg, smallImg, CV_INTER_LINEAR); 
        IplImage equImg = IplImage.create(smallImg.width(), smallImg.height(), IPL_DEPTH_8U, 1); 
        cvEqualizeHist(smallImg, equImg); 
        return equImg;
    }

    public IplImage preprocess(IplImage img, int width, int height)
    {
        IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1); 
        cvCvtColor(img, grayImg, CV_BGR2GRAY); 
        IplImage smallImg = IplImage.create(width, height, IPL_DEPTH_8U, 1); 
        cvResize(grayImg, smallImg, CV_INTER_LINEAR); 
        IplImage equImg = IplImage.create(smallImg.width(), smallImg.height(), IPL_DEPTH_8U, 1); 
        cvEqualizeHist(smallImg, equImg); 
        return equImg;
    }   


    public FacialDetection()
    {
        Loader.load(opencv_objdetect.class); 
        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE)); 
    }
    
    public IplImage findFace(IplImage img)
    {
        IplImage prImg = preprocess(img);

        CvMemStorage storage = CvMemStorage.create(); 
        CvSeq faces = cvHaarDetectObjects(prImg, cascade, storage,  1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT); 
        int total = faces.total(); 
        if (total == 0) 
        { 
            cvClearMemStorage(storage); 
            return null; 
        } 
        else 
        {
            CvRect rect = new CvRect(cvGetSeqElem(faces, 0)); 
            
            cvSetImageROI(img, rect);
            IplImage cropped = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 3);
            cvCopy(img, cropped, null);
            cvResetImageROI(img);
                       
            cvClearMemStorage(storage); 
            return cropped; 
        }    
    }

    public CvSeq detectOne(IplImage img) //detectMultiScale
    { 
        IplImage prImg = preprocess(img);

        CvMemStorage storage = CvMemStorage.create(); 
        //CvSeq faces = cvHaarDetectObjects(prImg, cascade, storage,  1.1, 1, CV_HAAR_DO_ROUGH_SEARCH | CV_HAAR_FIND_BIGGEST_OBJECT); 
        CvSeq faces = cvHaarDetectObjects(prImg, cascade, storage,  1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT); 
        cvClearMemStorage(storage); 
        
        return faces;
    } 
    
    public CvSeq detectAll(IplImage img)
    {
        IplImage prImg = preprocess(img);

        CvMemStorage storage = CvMemStorage.create(); 
        CvSeq faces = cvHaarDetectObjects(prImg, cascade, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING); 
        cvClearMemStorage(storage); 

        return faces;
    }

    public void draw(IplImage img)
    {
        CvSeq faces = detectOne(img);
        // draw thick yellow rectangles around all the faces 
        int total = faces.total(); 
        System.out.println("Found " + total + " face(s)"); 
        int SCALE = 1;
        for (int i = 0; i < total; i++) 
        { 
            CvRect r = new CvRect(cvGetSeqElem(faces, i)); 
            cvRectangle(img, cvPoint( r.x()*SCALE, r.y()*SCALE ), cvPoint( (r.x() + r.width())*SCALE, (r.y() + r.height())*SCALE ), CvScalar.YELLOW, 6, CV_AA, 0); 
        } 

        String OUT_FILE = "markedFaces.jpg";
        if (total > 0) 
        { 
            System.out.println("Saving marked-faces version in " + OUT_FILE); 
            cvSaveImage(OUT_FILE, img); 
        } 
    }  
    
    public static void main(String[] args) 
    {
        FacialDetection faceDetect = new FacialDetection();
        IplImage img = cvLoadImage("Chorallaries.jpg"); 
        //IplImage img = cvLoadImage("group.jpg"); 
        faceDetect.draw(img);
        
        cvSaveImage("face.jpg", faceDetect.findFace(cvLoadImage("Chorallaries.jpg"))); //Make sure not using already processed image
    }
}
