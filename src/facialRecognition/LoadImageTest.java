package facialRecognition;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class LoadImageTest {
    public static void main(final String[] args) {
        
        String image = "3 3 0 255 0 0 255 0 0 255 0";
        cvSaveImage("test.jpg", ImageFormat.convertToImage(image));

        
        /*String filename = "Chorallaries.jpg";
        IplImage image = cvLoadImage(filename);
        if (image != null) {
            cvSmooth(image, image, CV_GAUSSIAN, 3);
            cvSaveImage(filename, image);
            cvReleaseImage(image);
            System.out.println("Smoothed image");
        }
        else
        {
            System.out.println("Could not find image");
        }*/
    }
}
