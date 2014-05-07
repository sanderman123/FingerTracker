import java.util.ArrayList;
import java.util.List;

import org.opencv.highgui.*;
import org.opencv.core.*;
import org.opencv.imgproc.*;
import org.opencv.*;

import com.atul.JavaOpenCV.Imshow;

public class Main {
	VideoCapture cap;
	Imshow im;

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

	public void run() {

		cap = new VideoCapture(0);
		im = new Imshow("test");

		Mat elem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(15, 15));
		Mat elem2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(20, 20));

		Mat img = new Mat();
		while (true) {
			cap.read(img);
			Core.flip(img, img, 1);

			Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
			Imgproc.threshold(img, img, 100, 255, Imgproc.THRESH_BINARY);
			Imgproc.erode(img, img, elem);
			Imgproc.dilate(img, img, elem2);
			Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2RGB);

			process(img);
		}

	}

	public void process(Mat img) {
		Mat img2 = new Mat();
		Mat img3 = new Mat();
		Mat h = new Mat();
		Mat elem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(100, 100));
		Mat elem2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(120, 120));

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

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		(new Main()).run();
	}
}