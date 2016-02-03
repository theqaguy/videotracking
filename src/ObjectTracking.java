import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;


public class ObjectTracking {
	//initial min and max HSV filter values.
	//these will be changed using trackbars
	static int H_MIN = 0;
	static int H_MAX = 256;
	static int S_MIN = 0;
	static int S_MAX = 256;
	static int V_MIN = 0;
	static int V_MAX = 256;
	//default capture width and height
	final static int FRAME_WIDTH = 640;
	final static int FRAME_HEIGHT = 480;
	//max number of objects to be detected in frame
	final static int MAX_NUM_OBJECTS=50;
	//minimum and maximum object area
	final static int MIN_OBJECT_AREA = 20*20;
	final static int MAX_OBJECT_AREA = (int) Math.round(FRAME_HEIGHT*FRAME_WIDTH/1.5);
	//names that will appear at the top of each window
	final static String windowName = "Original Image";
	final static String windowName1 = "HSV Image";
	final static String windowName2 = "Thresholded Image";
	final String windowName3 = "After Morphological Operations";
	final String trackbarWindowName = "Trackbars";

	static boolean showFeeds = true;
	
	static SettingsFrame settingsFrame = null;
	
	static HashMap<String, ImageFrame> windowList = new HashMap<String, ImageFrame>();
	
	static void createTrackbars(){
		if (settingsFrame == null) {
			settingsFrame = new SettingsFrame();
		}
	}


	static void drawObject(int x, int y,Mat frame){

		//use some of the openCV drawing functions to draw crosshairs
		//on your tracked image!

	    //UPDATE:JUNE 18TH, 2013
	    //added 'if' and 'else' statements to prevent
	    //memory errors from writing off the screen (ie. (-25,-25) is not within the window!)

		Point currentPoint = new Point(x,y);
		Scalar defaultColor = new Scalar(0,255,0);
		
		Imgproc.circle(frame,currentPoint,20,defaultColor,2);
	    if(y-25>0)
	    	Imgproc.line(frame,currentPoint, new Point(x,y-25),defaultColor,2);
	    else Imgproc.line(frame,currentPoint, new Point(x,0),defaultColor,2);
	    if(y+25<FRAME_HEIGHT)
	    	Imgproc.line(frame,currentPoint, new Point(x,y+25),defaultColor,2);
	    else Imgproc.line(frame,currentPoint, new Point(x,FRAME_HEIGHT),defaultColor,2);
	    if(x-25>0)
	    	Imgproc.line(frame,currentPoint, new Point(x-25,y),defaultColor,2);
	    else Imgproc.line(frame,currentPoint,new Point(0,y),defaultColor,2);
	    if(x+25<FRAME_WIDTH)
	    	Imgproc.line(frame,currentPoint,new Point(x+25,y),defaultColor,2);
	    else Imgproc.line(frame,currentPoint,new Point(FRAME_WIDTH,y),defaultColor,2);

	    Imgproc.putText(frame,x+","+y, new Point(x,y+30),1,1,defaultColor,2);

	}
	static void morphOps(Mat thresh){

		//create structuring element that will be used to "dilate" and "erode" image.
		//the element chosen here is a 3px by 3px rectangle

		Mat erodeElement = Imgproc.getStructuringElement( Imgproc.MORPH_RECT, new Size(3,3));
	    //dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement( Imgproc.MORPH_RECT, new Size(8,8));

		Imgproc.erode(thresh,thresh,erodeElement);
		Imgproc.erode(thresh,thresh,erodeElement);


		Imgproc.dilate(thresh,thresh,dilateElement);
		Imgproc.dilate(thresh,thresh,dilateElement);
		


	}
	static void trackFilteredObject(int x, int y, Mat threshold, Mat cameraFeed){

		Mat temp = new Mat();
		threshold.copyTo(temp);
		//these two vectors needed for output of findContours
		List< MatOfPoint > contours = new ArrayList<MatOfPoint>();
		//find contours of filtered image using openCV findContours function
		Imgproc.findContours(temp,contours,new Mat(),Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE );
		//use moments method to find our filtered object
		double refArea = 0;
		boolean objectFound = false;
		if (contours.size() > 0) {
			int numObjects = contours.size();
	        //if number of objects greater than MAX_NUM_OBJECTS we have a noisy filter
	        if(numObjects<MAX_NUM_OBJECTS){
	        	for (MatOfPoint contour: contours) {
	        		
					double area = Imgproc.contourArea(contour);

					//if the area is less than 20 px by 20px then it is probably just noise
					//if the area is the same as the 3/2 of the image size, probably just a bad filter
					//we only want the object with the largest area so we safe a reference area each
					//iteration and compare it to the area in the next iteration.
	                if(area>MIN_OBJECT_AREA && area<MAX_OBJECT_AREA && area>refArea){
	                	Rect rect = Imgproc.boundingRect(contour);
	                    
						x = rect.x+(rect.width/2);
						y = rect.y+(rect.height/2);
						objectFound = true;
						refArea = area;
					}else objectFound = false;


				}
				//let user know you found an object
				if(objectFound ==true){
					Imgproc.putText(cameraFeed,"Tracking Object",new Point(0,50),2,1,new Scalar(0,255,0),2);
					//draw object location on screen
					drawObject(x,y,cameraFeed);
					settingsFrame.setCoordinates(x+"/"+y);
				} else {
					settingsFrame.setCoordinates("No object found");
				}

			}else Imgproc.putText(cameraFeed,"TOO MUCH NOISE! ADJUST FILTER",new Point(0,50),1,2,new Scalar(0,0,255),2);
		}
	}
	
	public static void main(String[] argv)
	{
	    // Load the native library.
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    
		//some boolean variables for different functionality within this
		//program
	    boolean trackObjects = false;
	    boolean useMorphOps = false;
		//Matrix to store each frame of the webcam feed
		Mat cameraFeed = new Mat();
		//matrix storage for HSV image
		Mat HSV = new Mat();
		//matrix storage for binary threshold image
		Mat threshold = new Mat();
		//x and y values for the location of the object
		int x=0, y=0;
		//create slider bars for HSV filtering
		createTrackbars();
		//video capture object to acquire webcam feed
		VideoCapture capture = new VideoCapture();
		//open capture object at location zero (default location for webcam)
		capture.open(0);
		//set height and width of capture frame
		capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH,FRAME_WIDTH);
		capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT,FRAME_HEIGHT);
		//start an infinite loop where webcam feed is copied to cameraFeed matrix
		//all of our operations will be performed within this loop
		while(true){
			long start = new Date().getTime();
			//store image to matrix
			capture.read(cameraFeed);
			//convert frame from BGR to HSV colorspace
			Imgproc.cvtColor(cameraFeed,HSV,Imgproc.COLOR_BGR2HSV);
			// grab values from trackbars
			H_MIN = settingsFrame.hminTrackbar.getValue();
			S_MIN = settingsFrame.sminTrackbar.getValue();
			V_MIN = settingsFrame.vminTrackbar.getValue();
			H_MAX = settingsFrame.hmaxTrackbar.getValue();
			S_MAX = settingsFrame.smaxTrackbar.getValue();
			V_MAX = settingsFrame.vmaxTrackbar.getValue();
			//filter HSV image between values and store filtered image to
			//threshold matrix
			Core.inRange(HSV,new Scalar(H_MIN,S_MIN,V_MIN),new Scalar(H_MAX,S_MAX,V_MAX),threshold);
			//perform morphological operations on thresholded image to eliminate noise
			//and emphasize the filtered object(s)
			if(settingsFrame.morphCheckbox.isSelected()) {
				morphOps(threshold);
			}
			//pass in thresholded frame to our object tracking function
			//this function will return the x and y coordinates of the
			//filtered object
			if(settingsFrame.trackCheckbox.isSelected()) {
				trackFilteredObject(x,y,threshold,cameraFeed);
			}
			showFeeds = settingsFrame.feedCheckbox.isSelected();
			//show frames
			if(showFeeds) {
				imshow(windowName2,threshold);
				imshow(windowName,cameraFeed);
				imshow(windowName1,HSV);
			} else {
				// close all frame windows
				for(String windowName: windowList.keySet()) {
					ImageFrame frame = windowList.get(windowName);
					frame.setVisible(false);
					frame.dispose();
				}
				windowList.clear();
			}
			long endprocessing = new Date().getTime();
			settingsFrame.setFramerate(1000/(endprocessing-start));
		}
	}


	private static void imshow(String windowName, Mat mat) {
		ImageFrame frame = windowList.get(windowName);
		if(frame == null) {
			frame = new ImageFrame(windowName);
			frame.setSize(mat.width(), mat.height());
			// create window
			windowList.put(windowName, frame);
		} 
		
		if(frame.isOpen) {
			Image bufImage = toBufferedImage(mat);
			frame.getGraphics().drawImage(bufImage , 0, 0, null);
		}
	}

    public static Image toBufferedImage(Mat m){
          int type = BufferedImage.TYPE_BYTE_GRAY;
          if ( m.channels() > 1 ) {
              type = BufferedImage.TYPE_3BYTE_BGR;
          }
          int bufferSize = m.channels()*m.cols()*m.rows();
          byte [] b = new byte[bufferSize];
          m.get(0,0,b); // get all the pixels
          BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
          final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
          System.arraycopy(b, 0, targetPixels, 0, b.length);  
          return image;

      }

    
}
