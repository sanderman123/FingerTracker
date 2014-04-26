package FingerTracker;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;

public class demo {
	public static void main(String[] args) {
		// Load image img1 as IplImage
		String name = "hand2.jpg";
		final IplImage image = cvLoadImage(name);
		IplImage image_gray = cvLoadImage(name,CV_LOAD_IMAGE_GRAYSCALE);			
		
		/*	Load RGB image and convert that to grayscale later:
			IplImage im_rgb  = cvLoadImage("image.jpg");
			IplImage im_gray = cvCreateImage(cvGetSize(im_rgb),IPL_DEPTH_8U,1);
			cvCvtColor(im_rgb,im_gray,CV_RGB2GRAY);
		*/
		
		IplImage image_bw = cvCreateImage(cvGetSize(image_gray),IPL_DEPTH_8U,1);
		cvThreshold(image_gray, image_bw, 128, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);
		
		// create canvas frame named 'Demo'
		final CanvasFrame canvas1 = new CanvasFrame("Black and White");
		
		final CanvasFrame canvas2 = new CanvasFrame("Objects");
		
		// Show image in canvas frame
		canvas1.showImage(image_bw);
		canvas2.showImage(detectObjects(image_bw, image));
		
		// This will close canvas frame on exit
		canvas1.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		canvas2.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
	}
	
	/*
	 * Detects the largest white object only
	 */
	public static IplImage detectObjects(IplImage srcImage, IplImage image){

	    IplImage resultImage = cvCloneImage(image);

	    CvMemStorage mem = CvMemStorage.create();
	    CvSeq contours = new CvSeq();
	    CvSeq ptr = new CvSeq();

	    cvFindContours(srcImage, mem, contours, Loader.sizeof(CvContour.class) , CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));

	    CvRect boundbox;
	    CvRect biggestBoundbox;
	    
	    CvSeq bigContour = null;
	    
	    //Detect biggest contour
	    biggestBoundbox = cvBoundingRect(contours, 0);
	    
	    for (ptr = contours; ptr != null; ptr = ptr.h_next()) {
	        boundbox = cvBoundingRect(ptr, 0);
	        if(boundbox.width()*boundbox.height() > biggestBoundbox.width()*biggestBoundbox.height()){
	        	biggestBoundbox = boundbox;	        	
	        	bigContour = ptr;
	        } 
	    }	    
	    
	    cvRectangle( resultImage , cvPoint( biggestBoundbox.x(), biggestBoundbox.y() ), 
                cvPoint( biggestBoundbox.x() + biggestBoundbox.width(), biggestBoundbox.y() + biggestBoundbox.height()),
                cvScalar( 0, 255, 0, 0 ), 2, 0, 0 );
	    
	    //cvDrawContours( resultImage, contours, CvScalar.BLUE, CvScalar.BLUE, -1, 1 /*CV_FILLED*/, CV_AA );
	    cvDrawContours( resultImage, bigContour, CvScalar.BLUE, CvScalar.BLUE, 0, 5, 8);
	    return resultImage;
	}
	
	/*
	public static IplImage detectConvexHull(IplImage srcImage, IplImage image){
		 IplImage resultImage = cvCloneImage(image);

	    CvMemStorage mem = CvMemStorage.create();
	    CvSeq contours = new CvSeq();
	    CvSeq ptr = new CvSeq();

	    cvFindContours(srcImage, mem, contours, Loader.sizeof(CvContour.class) , CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));
	
	  /// Find the convex hull object for each contour
	    vector<vector<Point> >hull( contours.size() );
	    for( int i = 0; i < contours.size(); i++ )
	       {  convexHull( Mat(contours[i]), hull[i], false ); }
		
		return null;
	}*/
	
}