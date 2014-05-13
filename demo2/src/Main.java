import java.util.ArrayList;
import java.util.List;

import org.opencv.highgui.*;
import org.opencv.core.*;
import org.opencv.imgproc.*;

import com.atul.JavaOpenCV.Imshow;

public class Main {
	VideoCapture cap;
	Imshow im;
	double pointCounter = 0;
	Point fitP;
	ArrayList<Point> pList;

	public void colorSampling() {
		Mat img = new Mat();
		// wait 50 frame
		for (int i = 0; i < 50; i++) {
			cap.read(img);
			Core.flip(img, img, 1);
			Core.rectangle(img, new Point(500, 300), new Point(600, 400),
					new Scalar(0, 255, 0));
			im.showImage(img);
		}
		// start sampling
		for (int i = 0; i < 30; i++) {
			cap.read(img);
			Core.flip(img, img, 1);
			Core.rectangle(img, new Point(500, 300), new Point(600, 400),
					new Scalar(255, 0, 0));
			im.showImage(img);
		}

	}

	public void run() throws InterruptedException {
		cap = new VideoCapture("c.avi");
		im = new Imshow("test");
		pList = new ArrayList<>();

		Mat orig = new Mat();
		while (cap.read(orig)) {
			// reduce noise and get binary hand segmentation
			Mat img = segment(orig);

			// detect pointing point
			Point p = process(img, orig);

			fitPoint(p, orig);

			Thread.sleep(30);
		}
		System.exit(0);
	}

	public void fitPoint(Point p, Mat orig) {
		final double alpha = 0.6;
		if (p == null) {
			if (pointCounter > -3) {
				pointCounter -= 0.25;
			}
		} else {
			if (pointCounter < 5) {
				pointCounter += 1;
			}
			if (pointCounter < 0) {
				fitP = p;
			} else {
				fitP = (new Point(fitP.x * (1 - alpha) + p.x * alpha, fitP.y
						* (1 - alpha) + p.y * alpha));
			}
		}
		if (pointCounter > 0) {
			pList.add(fitP);
			Core.circle(orig, fitP, 5, new Scalar(0, 255, 255), 2);
			List<MatOfPoint> polys = new ArrayList<>();
			polys.add(new MatOfPoint(pList.toArray(new Point[0])));
			Core.polylines(orig, polys, false, new Scalar(255, 255, 255), 2);
		}else{
			pList.clear();
		}
		im.showImage(orig);
	}

	public Mat segment(Mat orig) {
		Mat elem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(
				5, 5));
		Mat elem2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(
				5, 5));

		Mat img = orig.clone();

		Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
		Imgproc.GaussianBlur(img, img, new Size(5, 5), 0.7);
		Imgproc.threshold(img, img, 80, 255, Imgproc.THRESH_BINARY);
		Imgproc.erode(img, img, elem);
		Imgproc.dilate(img, img, elem2);

		return img;
	}

	public MatOfPoint convexHull(Point[] pt) {
		MatOfInt hull = new MatOfInt();
		Imgproc.convexHull(new MatOfPoint(pt), hull);
		int[] hulli = hull.toArray();
		ArrayList<Point> hullt = new ArrayList<>();
		for (int i = 0; i < hulli.length; i++) {
			hullt.add(pt[hulli[i]]);
		}
		return new MatOfPoint(hullt.toArray(new Point[0]));
	}

	public Point process(Mat img, Mat orig) {
		boolean flag = false;
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Mat tmp = img.clone();
		Mat h = new Mat();
		Imgproc.findContours(tmp, contours, h, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);
		if (contours.size() == 0) {
			return null;
		}
		int maxContour = 0;
		for (int i = 1; i < contours.size(); i++) {
			if (Imgproc.contourArea(contours.get(i)) > Imgproc
					.contourArea(contours.get(maxContour))) {
				maxContour = i;
			}
		}

		tmp = new Mat();
		Core.findNonZero(img, tmp);
		ArrayList<Point> mp = new ArrayList<>();
		for (int i = 0; i < tmp.rows(); i++) {
			mp.add(new Point(tmp.get(i, 0)));
		}

		RotatedRect rr = Imgproc.fitEllipse(new MatOfPoint2f(mp
				.toArray(new Point[0])));
		Core.ellipse(orig, rr, new Scalar(255, 0, 0), 2, 8);
		double ratio = rr.size.width / rr.size.height;
		Point[] finger = contours.get(maxContour).toArray();
		if (ratio > 1)
			ratio = 1 / ratio;
		if (ratio < 0.3) {
			flag = true;
		} else {
			Imgproc.drawContours(orig, contours, maxContour, new Scalar(0, 0,
					255), 3);

			ArrayList<MatOfPoint> hulls = new ArrayList<>();
			hulls.add(convexHull(finger));

			double chRatio = Imgproc.contourArea(contours.get(maxContour))
					/ Imgproc.contourArea(hulls.get(0));

			if (chRatio < 0.8) {
				flag = true;
			}
			Imgproc.drawContours(orig, hulls, -1, new Scalar(255, 0, 255), 2);
		}
		if (flag) {
			int mx = 10000, my = 10000;
			for (int i = 0; i < finger.length; i++) {
				if (finger[i].x + finger[i].y < mx + my) {
					mx = (int) finger[i].x;
					my = (int) finger[i].y;
				}
			}
			return new Point(mx, my);
		}
		return null;
	}

	public void process2(Mat img, Mat orig) {
		Mat img2 = new Mat();
		Mat img3 = new Mat();
		Mat h = new Mat();
		Mat elem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(
				100, 100));
		Mat elem2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(
				120, 120));

		Imgproc.cvtColor(img, img2, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(img2, img2, 128, 255, Imgproc.THRESH_BINARY);

		Imgproc.cvtColor(img, img3, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(img3, img3, 128, 255, Imgproc.THRESH_BINARY);

		Imgproc.erode(img2, img2, elem);
		Imgproc.dilate(img2, img2, elem2);
		Core.bitwise_not(img2, img2);
		Core.bitwise_and(img2, img3, img2);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Imgproc.findContours(img2, contours, h, Imgproc.RETR_CCOMP,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Rect maxRect = null;
		int maxContour = -1;
		for (int i = 0; i < contours.size(); i++) {
			Rect r = Imgproc.boundingRect(contours.get(i));
			if (maxRect == null || maxRect.area() < r.area()) {
				maxRect = r;
				maxContour = i;
			}
		}
		if (maxRect != null
				&& (double) maxRect.height / (double) maxRect.width > 1
				&& maxRect.area() > 100) {
			Imgproc.drawContours(img, contours, maxContour, new Scalar(0, 0,
					255), 3);
			Point[] finger = contours.get(maxContour).toArray();
			Point top = null;
			for (int i = 0; i < finger.length; i++) {
				if (top == null || finger[i].y < top.y) {
					top = finger[i];
				}
			}
			Core.circle(img, top, 10, new Scalar(0, 255, 0), 3);
		}

		im.showImage(img);
	}

	public static void main(String[] args) throws Throwable {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		(new Main()).run();
	}
}
