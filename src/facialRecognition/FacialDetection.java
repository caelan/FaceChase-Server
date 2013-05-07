package facialRecognition;

import java.util.LinkedList;
import java.util.List;

import util.Constants;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;


//Cluster extra features?

public class FacialDetection 
{
    private final String FACE_CASCADE_FILE;
    private final String EYES_CASCADE_FILE;
    //private CvHaarClassifierCascade faceCascade;
    //private CvHaarClassifierCascade eyesCascade;
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
  
    /*public FacialDetection()
    {
        FACE_CASCADE_FILE = Constants.facialDetectLoadFile;
        EYES_CASCADE_FILE = Constants.eyesDetectLoadFile;
        Loader.load(opencv_objdetect.class); 
        faceCascade = new CvHaarClassifierCascade(cvLoad(FACE_CASCADE_FILE)); 
        eyesCascade = new CvHaarClassifierCascade(cvLoad(EYES_CASCADE_FILE)); 
    }*/
    
    public FacialDetection()
    {
        FACE_CASCADE_FILE = Constants.facialDetectLoadFile;
        EYES_CASCADE_FILE = Constants.eyesDetectLoadFile;
        faceCascade = new CascadeClassifier(FACE_CASCADE_FILE);
        eyesCascade = new CascadeClassifier(EYES_CASCADE_FILE);
    }
    
    public IplImage preprocess(IplImage img)
    {
        if(img.nChannels() != 1)
        {
            IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1); 
            cvCvtColor(img, grayImg, CV_BGR2GRAY); 
            IplImage equImg = IplImage.create(grayImg.width(), grayImg.height(), IPL_DEPTH_8U, 1); 
            cvEqualizeHist(grayImg, equImg); 
            return equImg;
        }
        else
        {
            IplImage equImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1); 
            cvEqualizeHist(img, equImg); 
            return equImg;
        }
    }

    public IplImage preprocess(IplImage img, int scale)
    {
        if(img.nChannels() != 1)
        {
            IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1); 
            cvCvtColor(img, grayImg, CV_BGR2GRAY); 
            IplImage smallImg = IplImage.create(grayImg.width()/scale, grayImg.height()/scale, IPL_DEPTH_8U, 1); 
            cvResize(grayImg, smallImg, CV_INTER_LINEAR); 
            IplImage equImg = IplImage.create(smallImg.width(), smallImg.height(), IPL_DEPTH_8U, 1); 
            cvEqualizeHist(smallImg, equImg); 
            return equImg;
        }
        else
        {
            IplImage smallImg = IplImage.create(img.width()/scale, img.height()/scale, IPL_DEPTH_8U, 1); 
            cvResize(img, smallImg, CV_INTER_LINEAR); 
            IplImage equImg = IplImage.create(smallImg.width(), smallImg.height(), IPL_DEPTH_8U, 1); 
            cvEqualizeHist(smallImg, equImg); 
            return equImg;
        }        
    }

    public IplImage preprocess(IplImage img, int width, int height)
    {
        if(img.nChannels() != 1)
        {
            IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1); 
            cvCvtColor(img, grayImg, CV_BGR2GRAY); 
            IplImage smallImg = IplImage.create(width, height, IPL_DEPTH_8U, 1); 
            cvResize(grayImg, smallImg, CV_INTER_LINEAR); 
            IplImage equImg = IplImage.create(smallImg.width(), smallImg.height(), IPL_DEPTH_8U, 1); 
            cvEqualizeHist(smallImg, equImg); 
            return equImg;
        }
        else
        {
            IplImage smallImg = IplImage.create(width, height, IPL_DEPTH_8U, 1); 
            cvResize(img, smallImg, CV_INTER_LINEAR); 
            IplImage equImg = IplImage.create(smallImg.width(), smallImg.height(), IPL_DEPTH_8U, 1); 
            cvEqualizeHist(smallImg, equImg); 
            return equImg;
        }    
    }   
    
    /*public IplImage findFace(IplImage img)
    {
        IplImage prImg = preprocess(img);

        CvMemStorage storage = CvMemStorage.create(); 
        CvSeq faces = cvHaarDetectObjects(prImg, faceCascade, storage,  1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT); 
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
    }*/
    
    public IplImage findFace(IplImage img)
    {
        IplImage prImg = preprocess(img);
        CvRect faces = new CvRect();
        
        faceCascade.detectMultiScale(prImg, faces, 1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT, new CvSize(30, 30), new CvSize(prImg.width(), prImg.height()));
              
        if (faces.capacity() == 0) 
        { 
            return null; 
        } 
        else 
        {
            CvRect rect = faces.position(0);
            int size = Math.max(rect.width(), rect.height());
            if(size < 30)
            {
                return null;
            }
            
            cvSetImageROI(img, rect);
            IplImage cropped = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, img.nChannels());
            cvCopy(img, cropped, null);
            cvResetImageROI(img);
                       
            return cropped; 
        }    
    }
    
    public IplImage rotateImage(IplImage src, double angleDegrees)
    {    
        IplImage imageRotated = cvCloneImage(src);

        if(angleDegrees!=0){
            CvMat rot_mat = cvCreateMat(2,3,CV_32FC1);
        
            // Compute rotation matrix
            CvPoint2D32f center = cvPoint2D32f( cvGetSize(imageRotated).width()/2, cvGetSize(imageRotated).height()/2 );
            cv2DRotationMatrix( center, angleDegrees, 1, rot_mat );

            // Do the transformation
            cvWarpAffine( src, imageRotated, rot_mat );
        }

        return imageRotated;
    }
    
    public List<CvRect> selectLargest(List<CvRect> input, int num)
    {
        int size = Math.max(0, Math.min(num, input.size()));
        List<CvRect> list = new LinkedList<CvRect>(input);
        List<CvRect> ret = new LinkedList<CvRect>();
        for(int i = 0; i < size; i++)
        {
            int best = 0;
            for(int j = 1; j < list.size(); j ++)
            {
                if(Math.min(list.get(j).height(), list.get(j).width()) > Math.min(list.get(best).height(), list.get(best).width()))
                {
                    best = j;
                }
            }
            ret.add(list.remove(best));
        }
        return ret;
    }
    
    public double computeAngle(CvRect a, CvRect b)
    {
        CvPoint one =  new CvPoint(a.x() + a.width()/2, a.y() + a.height()/2);
        CvPoint two =  new CvPoint(b.x() + b.width()/2, b.y() + b.height()/2);

        CvPoint vector;
        
        if(one.x() < two.x())
        {
            vector = cvPoint(two.x() - one.x(), two.y() - one.y());
        }
        else
        {
            vector = cvPoint(one.x() - two.x(), one.y() - two.y());            
        }
        return (180.0*Math.atan2(vector.y(), vector.x()))/Math.PI;
    }
    
    public double selectMinAngle(CvRect largest, List<CvRect> list)
    {
        double min = computeAngle(largest, list.get(0));
        for(int i = 1; i < list.size(); i++)
        {
            double angle = computeAngle(largest, list.get(i));
            if(Math.abs(angle) < Math.abs(min))
                min = angle; 
        }
        return min;
    }
    
    public IplImage findRotatedFace(IplImage image)
    {
        IplImage img = findFace(image);
        if(img == null)
            return null;
        
        List<CvRect> eyes = detectEyes(img);
        if(eyes.size() < 2)
            return img;
       
        List<CvRect> largest = selectLargest(eyes, 4);
        CvRect one = largest.remove(0);
        
        double angle = selectMinAngle(one, largest);
        
        if(Math.abs(angle) > 45)
            return img;
        
        
        return findFace(rotateImage(image, angle));        
    }
    
    /*public CvSeq detectEyes(IplImage img) //TODO
    {
        IplImage prImg = preprocess(img);
        CvMemStorage storage = CvMemStorage.create(); 
        
        
        //eyes_cascade.detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CV_HAAR_SCALE_IMAGE, Size(30, 30) );
        //CvSeq eyes = cvHaarDetectObjects(prImg, eyesCascade, storage,  1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT);
        CvSeq eyes = cvHaarDetectObjects(prImg, eyesCascade, storage, 1.1, 2, 0);
        //CvSeq eyes = cvHaarDetectObjects(prImg, eyesCascade, storage,  1.1, 3, 0);
        cvClearMemStorage(storage); 
        
        return eyes;
    }*/
    
    
    public List<CvRect> detectEyes(IplImage img) //TODO
    {
        IplImage prImg = preprocess(img);       
        CvRect eyes = new CvRect();
        
        eyesCascade.detectMultiScale(prImg, eyes, 1.1, 2, 0, new CvSize(), new CvSize(prImg.width(), prImg.height()));
        int maxSize = Math.max(prImg.width()/4, prImg.height()/4);
        int minSize = Math.min(prImg.width()/20, prImg.height()/20);
        //eyesCascade.detectMultiScale(prImg, eyes, 1.1, 2, 0, new CvSize(minSize, minSize), new CvSize(maxSize, maxSize));
        
        LinkedList<CvRect> eyeList = new LinkedList<CvRect>();
        for (int i = 0; i < eyes.capacity(); i++) { 
            CvRect r = new CvRect(eyes.position(i).x(), eyes.position(i).y(), eyes.position(i).width(), eyes.position(i).height()) ;
            eyeList.add(r); 
        }       
        
        return eyeList;
    }

    /*public CvSeq detectOne(IplImage img) //detectMultiScale
    { 
        IplImage prImg = preprocess(img);

        CvMemStorage storage = CvMemStorage.create(); 
        //CvSeq faces = cvHaarDetectObjects(prImg, faceCascade, storage,  1.1, 1, CV_HAAR_DO_ROUGH_SEARCH | CV_HAAR_FIND_BIGGEST_OBJECT); 
        CvSeq faces = cvHaarDetectObjects(prImg, faceCascade, storage,  1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT); 
        cvClearMemStorage(storage); 
        
        return faces;
    }*/
    
    public CvRect detectOne(IplImage img) //detectMultiScale
    { 
        IplImage prImg = preprocess(img);
        CvRect faces = new CvRect();

        faceCascade.detectMultiScale(prImg, faces, 1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT, new CvSize(), new CvSize(prImg.width(), prImg.height()));
        
        if(faces.capacity() > 0)
            return faces.position(0);
        else
            return null;
    } 
  
    /*public CvSeq detectAll(IplImage img)
    {
        IplImage prImg = preprocess(img);
   
        CvMemStorage storage = CvMemStorage.create(); 
        CvSeq faces = cvHaarDetectObjects(prImg, faceCascade, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING); 
        cvClearMemStorage(storage); 

        return faces;
    }*/
    
    public List<CvRect> detectAll(IplImage img)
    {
        IplImage prImg = preprocess(img);
        CvRect faces = new CvRect();

        faceCascade.detectMultiScale(prImg, faces, 1.1, 2, 0, new CvSize(), new CvSize(prImg.width(), prImg.height()));
        
        LinkedList<CvRect> faceList = new LinkedList<CvRect>();
        
        for (int i = 0; i < faces.capacity(); i++) { 
            CvRect r = new CvRect(faces.position(i).x(), faces.position(i).y(), faces.position(i).width(), faces.position(i).height()) ;
            faceList.add(r); 
        }       

        return faceList;
    }

    public void draw(IplImage img)
    {
        List<CvRect> faces = detectAll(img);
        //CvSeq eyes = detectEyes(img);
        
        // draw thick yellow rectangles around all the faces 
        int total = faces.size(); 
        System.out.println("Found " + total + " face(s)"); 
        int SCALE = 1;
        for (int i = 0; i < total; i++) 
        { 
            CvRect r = faces.get(i); 
            System.out.println(r.x() + ", " + r.y() + ", " + (r.x() + r.width()) + ", " + (r.y() + r.height()));  
            cvRectangle(img, cvPoint( r.x()*SCALE, r.y()*SCALE ), cvPoint( (r.x() + r.width())*SCALE, (r.y() + r.height())*SCALE ), CvScalar.YELLOW, 6, CV_AA, 0); 
        } 
        
        /*for(int i = 0 ; i < eyes.total(); i++ ) 
        {
            CvRect r = new CvRect(cvGetSeqElem(eyes, i)); 
            System.out.println(r.x() + ", " + r.y() + ", " + (r.x() + r.width()) + ", " + (r.y() + r.height()));  
            cvRectangle(img, cvPoint( r.x()*SCALE, r.y()*SCALE ), cvPoint( (r.x() + r.width())*SCALE, (r.y() + r.height())*SCALE ), CvScalar.RED, 6, CV_AA, 0); 
        }*/

        String OUT_FILE = "facialRecTest\\markedFaces.jpg";
        if (total > 0) 
        { 
            System.out.println("Saving marked-faces version in " + OUT_FILE); 
            cvSaveImage(OUT_FILE, img); 
        } 
    }  
    
    public void drawEyes(IplImage image)
    {
        IplImage img = findFace(image);
        List<CvRect> eyes = detectEyes(img);
        
        // draw thick yellow rectangles around all the faces 
        int total = eyes.size(); 
        System.out.println("Found " + total + " eyes(s)"); 
        int SCALE = 1;
        
        for(int i = 0 ; i < eyes.size(); i++ ) 
        {
            CvRect r = eyes.get(i); 
            System.out.println(r.x() + ", " + r.y() + ", " + (r.x() + r.width()) + ", " + (r.y() + r.height()));  
            cvRectangle(img, cvPoint( r.x()*SCALE, r.y()*SCALE ), cvPoint( (r.x() + r.width())*SCALE, (r.y() + r.height())*SCALE ), CvScalar.RED, 6, CV_AA, 0); 
        }

        String OUT_FILE = "facialRecTest\\markedFaces.jpg";
        System.out.println("Saving marked-faces version in " + OUT_FILE); 
        cvSaveImage(OUT_FILE, img); 
    }  
    
    public static void main(String[] args) 
    {
        FacialDetection faceDetect = new FacialDetection();
        String filename = "facialRecTest\\caelan1.jpg";
        IplImage img = cvLoadImage(filename); 
        //IplImage img = cvLoadImage("group.jpg"); 
        faceDetect.drawEyes(img);
        
        //faceDetect.detectEyes(img);
        cvSaveImage("facialRecTest\\caelan1Face.jpg", faceDetect.findRotatedFace(cvLoadImage(filename))); //Make sure not using already processed image
        cvSaveImage("facialRecTest\\caelan1FaceGray.jpg", faceDetect.preprocess(faceDetect.findRotatedFace(cvLoadImage(filename)))); //Make sure not using already processed image
    }
}
