package FingerTracker;

import static com.googlecode.javacv.cpp.opencv_core.*;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Vector;
import java.math.*;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;

public class demo {
	public static void main(String[] args) {
		// Load image img1 as IplImage
		String name = "hand1.jpg";
		final IplImage image = cvLoadImage(name);
		IplImage image_gray = cvLoadImage(name, CV_LOAD_IMAGE_GRAYSCALE);

		/*
		 * Load RGB image and convert that to grayscale later: IplImage im_rgb =
		 * cvLoadImage("image.jpg"); IplImage im_gray =
		 * cvCreateImage(cvGetSize(im_rgb),IPL_DEPTH_8U,1);
		 * cvCvtColor(im_rgb,im_gray,CV_RGB2GRAY);
		 */

		IplImage image_bw = cvCreateImage(cvGetSize(image_gray), IPL_DEPTH_8U,
				1);
		cvThreshold(image_gray, image_bw, 128, 255, CV_THRESH_BINARY
				| CV_THRESH_OTSU);

		// create canvas frame named 'Demo'
		final CanvasFrame canvas1 = new CanvasFrame("Object Contour");

		final CanvasFrame canvas2 = new CanvasFrame("Hull");

		// Show the object image in canvas frame
		IplImage im_obj = image.clone();
		CvSeq objContour = detectBiggestObject(image_bw, image);
		cvDrawContours(im_obj, objContour, CvScalar.BLUE, CvScalar.BLUE, 0, 5,
				8);
		canvas1.showImage(im_obj);

		// Show the hull and the finger tips
		IplImage im_hull = image.clone();
		CvSeq hullContour = detectConvexHull(objContour);
		cvDrawContours(im_hull, hullContour, CvScalar.RED, CvScalar.RED, 0, 2,
				8);
		//im_hull = drawContourPoints(hullContour, 10, im_hull);
		im_hull = drawFingersStupid(hullContour, im_hull);
		
		canvas2.showImage(im_hull);

		// This will close canvas frame on exit
		canvas1.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		canvas2.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
	}

	
	
	/**
	 * Attempt to try and find one point per finger
	 * Not working yet...
	 * 
	 * @param contour
	 * @param threshold
	 * @param image
	 * @return
	 */
	public static IplImage /* ArrayList<CvPoint> */drawContourPoints(
			CvSeq contour, int threshold, IplImage image) {

		ArrayList<Integer> edgePoints = new ArrayList<Integer>();

		if (threshold < 2) {
			threshold = 5;
		}

		// Find all the edgepoints of the clusters
		for (int i = 0; i < contour.total(); i++) {
			CvPoint v = new CvPoint(cvGetSeqElem(contour, i));
			CvPoint prev = new CvPoint(cvGetSeqElem(contour,
					(i - 1) % contour.total()));
			CvPoint next = new CvPoint(cvGetSeqElem(contour,
					(i + 1) % contour.total()));

			// Use threshold as factor to determine a large gap
			if ((distance(v, next) * threshold) < distance(v, prev)) {
				// Edgepoint found!! v is the first point in a cluster
				edgePoints.add(i);
			}
		}

		/*
		 * cvDrawCircle(image, v, 5, CvScalar.BLUE, -1, 8, 0);
		 * System.out.println(" X value = " + v.x() + " ; Y value =" + v.y() +
		 * "; Distance to prev: " + distance(v, prev));
		 */

		// Group all clusters by average
		ArrayList<CvPoint> avPts = new ArrayList<CvPoint>();
		CvSeq seq = new CvSeq();
		CvMat mat = new CvMat();

		// CvSeq avSeq = new CvSeq(arr);
		int count = 0;
		for (int i = 0; i < edgePoints.size(); i++) {
			int first = edgePoints.get(i);
			int last = edgePoints.get((i + 1) % edgePoints.size());

			//for(int k = edgePoints.get(i); k < ; k++)
			
			
			
			
			
			int middle = (int) Math.floor(first + (last - first) / 2);
			CvPoint mp = new CvPoint(cvGetSeqElem(contour, middle));
			avPts.add(mp);
			mat.put(mp);

			/*
			 * //For every cluster find the average int totalx; int totaly;
			 * //Make sure to get all the endpoints and dont get a nullPointer
			 * exception at the end for(int j = edgePoints.get(i); j <
			 * edgePoints.get((i+1)%edgePoints.size()); j++){ //edgepoints i =
			 * first point in a cluster CvPoint pt = new
			 * CvPoint(cvGetSeqElem(contour, j)); }
			 */

		}
		seq = new CvSeq(mat);
		for (int i = 0; i < seq.total(); i++) {
			CvPoint v = new CvPoint(cvGetSeqElem(seq, i));
			cvDrawCircle(image, v, 5, CvScalar.BLUE, -1, 8, 0);
			System.out.println(" X value = " + v.x() + " ; Y value =" + v.y());
		}

		cvDrawCircle(image, new CvPoint(10, 10), 5, CvScalar.BLUE, -1, 8, 0);

		return image;
	}

	/**
	 * Adds points to the image at the place of the fingers
	 * !!Multiple points per finger...
	 * 
	 * @param contour
	 * @param image
	 * @return
	 */
	private static IplImage drawFingersStupid(CvSeq contour, IplImage image) {
		for (int i = 0; i < contour.total(); i++) {
			CvPoint v = new CvPoint(cvGetSeqElem(contour, i));
			if(i == 0){
				cvDrawCircle(image, v, 5, CvScalar.GREEN, -1, 8, 0);
			} else {
				cvDrawCircle(image, v, 5, CvScalar.BLUE, -1, 8, 0);
			}
			System.out.println(" X value = " + v.x() + " ; Y value =" + v.y());
		}

		return image;
	}

	private static int distance(CvPoint p1, CvPoint p2) {
		int dx = Math.abs(p1.x() - p2.x());
		int dy = Math.abs(p1.y() - p2.y());
		int dist = (int) Math.sqrt(dx * dx + dy * dy);
		return dist;
	}

	/*
	 * Detects the largest white object only
	 */
	public static CvSeq detectBiggestObject(IplImage srcImage, IplImage image) {

		// IplImage resultImage = cvCloneImage(image);

		CvMemStorage mem = CvMemStorage.create();
		CvSeq contours = new CvSeq();
		CvSeq ptr = new CvSeq();

		cvFindContours(srcImage, mem, contours, Loader.sizeof(CvContour.class),
				CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

		CvRect boundbox;
		CvRect biggestBoundbox;

		CvSeq bigContour = null;

		// Detect biggest contour
		biggestBoundbox = cvBoundingRect(contours, 0);

		for (ptr = contours; ptr != null; ptr = ptr.h_next()) {
			boundbox = cvBoundingRect(ptr, 0);
			if (boundbox.width() * boundbox.height() > biggestBoundbox.width()
					* biggestBoundbox.height()) {
				biggestBoundbox = boundbox;
				bigContour = ptr;
			}
		}

		/*
		 * cvRectangle( resultImage, cvPoint(biggestBoundbox.x(),
		 * biggestBoundbox.y()), cvPoint(biggestBoundbox.x() +
		 * biggestBoundbox.width(), biggestBoundbox.y() +
		 * biggestBoundbox.height()), cvScalar(0, 255, 0, 0), 2, 0, 0);
		 */
		// cvDrawContours( resultImage, contours, CvScalar.BLUE, CvScalar.BLUE,
		// -1, 1 /*CV_FILLED*/, CV_AA );
		/*
		 * cvDrawContours(resultImage, bigContour, CvScalar.BLUE, CvScalar.BLUE,
		 * 0, 5, 8);
		 */
		return bigContour;
	}

	/*
	 * Detects the convex hull of the contour
	 */
	public static CvSeq detectConvexHull(CvSeq contour) {
		// IplImage resultImage = cvCloneImage(image);

		CvMemStorage mem = CvMemStorage.create();
		CvSeq hull = new CvSeq();

		int points = 1;

		hull = cvConvexHull2(contour, mem, CV_CLOCKWISE, points);

		System.out.println("Hull: " + hull.elem_size());

		/*
		 * cvDrawContours(resultImage, hull, CvScalar.RED, CvScalar.RED, 0, 5,
		 * 8);
		 */
		return hull;
	}

	public static IplImage drawPoint(IplImage img, CvPoint center,
			CvScalar colour) {
		int thickness = -1;
		int lineType = 8;

		CvArr arr = img.asCvMat();

		cvCircle(arr, center, 5, colour, thickness, lineType, 1);
		IplImage result = new IplImage(arr);

		return result;
	}

}