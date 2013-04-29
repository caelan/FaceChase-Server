package facialRecognition;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;

import java.awt.Color;
import java.nio.ByteBuffer;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ImageFormat {
    public static int[] rgbToGray(int image1[], int w, int h)
    {
        int[] image2 = new int[w*h];
        for(int i = 0; i < w*h; i++)
        {
            image2[i] = (int)(.299*image1[3*i + 0] + .587*image1[3*i + 1] + .114*image1[3*i + 2]);
        }
        return image2;
    }
    
    public static String writeImageAsString(int data[], int w, int h)
    {
        String s = "" + w + " " + h;
        for(int i = 0; i < w*h; i++)
        {
            s += " " + data[i];
        }
        return s;
    }
    
    /*public static String bitmapToString(Bitmap b) //One on the device is different and is correct
    {
        int w = b.getWidth();
        int h = b.getHeight();
        int size = w*h;
        int[] array = new int[size*3];

        for(int i = 0; i < size; i++)
        {
            int r = i/w;
            int c = i%w;
            Color color = b.getPixel(r, c);
            array[3*i + 0] = color.getRed();
            array[3*i + 1] = color.getGreen();
            array[3*i + 2] = color.getBlue();            
        }
        int[] gray = rgbToGray(array, w, h);
        return writeImageAsString(gray, w, h);
    }*/
    
    public static IplImage convertToImage(String s)
    {
        try{
            String[] split = s.split(" ");
            int w = Integer.parseInt(split[0]);
            int h = Integer.parseInt(split[1]);
            
            int offset = 2;

            IplImage image = IplImage.create(w, h, IPL_DEPTH_8U, 1);
            
            ByteBuffer buffer = image.getByteBuffer();

            for(int y = 0; y < image.height(); y++) {
                for(int x = 0; x < image.width(); x++) {
                    int index = y * image.widthStep() + x * image.nChannels();

                    // Used to read the pixel value - the 0xFF is needed to cast from
                    // an unsigned byte to an int.
                    //int value = buffer.get(index) & 0xFF;

                    // Sets the pixel to a value (greyscale).
                    buffer.put(index, (byte)Integer.parseInt(split[offset + y*w + x]));

                    // Sets the pixel to a value (RGB, stored in BGR order).
                    //buffer.put(index, blue);
                    //buffer.put(index + 1, green);
                    //buffer.put(index + 2, red);
                }
            }
                       
            return image;
        } catch(Exception e)
        {
            return null;
        }
    }
}
