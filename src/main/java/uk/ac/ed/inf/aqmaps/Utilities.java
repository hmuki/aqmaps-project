package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Utilities implements java.util.Comparator<Point2D> {
	
	// parse json and use the sensor data class to store the data
	public static ArrayList<SensorData> deserialize(String json) {
		// de-serialise response body
		Gson gson = new Gson();
		var listType = new TypeToken<ArrayList<SensorData>>() {
		}.getType();
		ArrayList<SensorData> data = gson.fromJson(json, listType);

		return data;
	}
	
	// get the right order in which the drone should fly around the sensors
	public Point2D[] getOrderedPoints(ArrayList<SensorData> readings, Point2D releasePoint) {
		
		var points = new ArrayList<SensorData.Location.Coordinates>();
		for (var reading : readings) {
			points.add(reading.getLocationCoordinates().getCoordinates()); // store the sensor locations
		}
		
		// Compute the geometric centre of the sensor locations
		SensorData.Location.Coordinates centre = new SensorData.Location.Coordinates();
		int size = points.size();
		centre.setLng(0);
		centre.setLat(0);
		
		for (var point : points) {
			centre.setLng(centre.getLng() + point.getLng());
			centre.setLat(centre.getLat() + point.getLat());
		}
		
		centre.setLng(centre.getLng()/size);
		centre.setLat(centre.getLat()/size);
		
		var Point2DList = new Point2D[size];
		for (int i = 0; i < size; i++) {
			Point2DList[i] = new Point2D(points.get(i), centre, i); // encapsulate sensor locations in a Point2D object
		}
		// sort the sensor locations using the comparator in this class
		Arrays.sort(Point2DList, this);
		
		var distanceMap = new HashMap<Integer, Double>();
		var distances = new double[size];
		var nearest = 0;
		// compute distances of sensor locations to the release point
		for (int i = 0; i < size; i++) {
			distances[i] = releasePoint.getDistanceFrom(Point2DList[i]);
		}
				
		for (int i = 0; i < size; i++) {
			distanceMap.put(i, distances[i]);
		}
		// sort distances and get the sensor that is closest to the release point
		Arrays.sort(distances);
		for (int i = 0; i < size; i++) {
			if (distances[0] == distanceMap.get(i)) {
				nearest = i;
			}
		}
		// reshuffle the permutation so that the sensor closest to the release point is first in the list
		var orderedPoint2DList = new Point2D[size];
		for (int i = 0; i < size; i++) {
			orderedPoint2DList[i] = Point2DList[(nearest + i) % size];
		}
		
		return orderedPoint2DList;
	}
	
	// get the marker color and symbol for a given sensor reading
	public static String toRGBAndMarkerSymbol(String reading) {

		if (reading.equals("null") || reading.equals("NaN")) {
			return "#000000,cross"; // low battery
		} else {
			var value = Double.parseDouble(reading);
			if (value >= 0 && value < 32) {
				return "#00ff00,lighthouse";
			} else if (value >= 32 && value < 64) {
				return "#40ff00,lighthouse";
			} else if (value >= 64 && value < 96) {
				return "#80ff00,lighthouse";
			} else if (value >= 96 && value < 128) {
				return "#c0ff00,lighthouse";
			} else if (value >= 128 && value < 160) {
				return "#ffc000,danger";
			} else if (value >= 160 && value < 192) {
				return "#ff8000,danger";
			} else if (value >= 192 && value < 224) {
				return "#ff4000,danger";
			} else if (value >= 224 && value < 256) {
				return "#ff0000,danger";
			} else {
				return "#aaaaaa,"; // not visited
			}
		}
	}
	
	// Checks if a line at a specified angle intersects the specified polygon
	public static boolean intersectsPolygon(ArrayList<Point2D> polygon, Point2D startPoint,
			double angle, double moveLength) {
		
		var length = moveLength;
		final var numPoints = 120;
		//	compute 120 evenly spaced points from startPoint at given angle
		var points = new ArrayList<Point2D>();
		var x0 = startPoint.getX();
		var y0 = startPoint.getY();
		var scaleFactors = new ArrayList<Double>();
		
		for (int i = 1; i <= numPoints; i++) {
			scaleFactors.add((i/(double)numPoints) * length);
		}
		
		for (var scaleFactor : scaleFactors) {
			points.add(new Point2D(x0 + Math.cos(angle)*scaleFactor, y0 + Math.sin(angle)*scaleFactor));
		}
		
		var answer = false;
		// if ANY of the points lie within the polygon, we return true otherwise false
		for (var point : points) {
			answer = answer || isWithinPolygon(polygon, point);
		}
		
		return answer;	
	}
	
	// Checks if a given point lies within the specified polygon
	public static boolean isWithinPolygon(ArrayList<Point2D> polygon, Point2D point) {
		
		int n = polygon.size()-1;
		var sum = 0.0;
		var angle1 = 0.0;
		var angle2 = 0.0;
		
		// compute the angles between the specified point and each vertex of the polygon and compute their sum
		for (int i = 0; i < n; i++) {
			angle1 = polygon.get(i).getAngleFrom(point);
			angle2 = polygon.get(i+1).getAngleFrom(point);
			var dtheta = angle2 - angle1;
			while (dtheta > Math.PI) {
				dtheta -= 2 * Math.PI;
			}
			while (dtheta < -Math.PI) {
				dtheta += 2 * Math.PI;
			}
			sum += dtheta;
		}
		// if sum is 0 point is within else outside the polygon 
		if (Math.abs(sum) < Math.PI)
			return false;
		else
			return true;
	}
	
	// compute the closest angle - which is a multiple of ten degrees - to the specified angle
	public static double getAngleOfDeviation(double angle) {
		
		var angleInDegrees = angle * 180/Math.PI;
		var angleOfDeviation = 0.0;
		
		angleOfDeviation = Math.ceil(angleInDegrees/10) * 10;
		
		return angleOfDeviation * Math.PI/180;
	}
	
	// converts an angle in radians to its equivalent in degrees within the range 0 to 360
	public static int convertToDegrees(double angle) {
		
		while (angle > Math.PI) {
			angle -= 2 * Math.PI;
		}
		while (angle < -Math.PI) {
			angle += 2 * Math.PI;
		}
		var angleInDegrees = angle * 180/Math.PI; // exact angle in degrees
		int tenthDegreeBelow = 10 * (int)Math.floor(angleInDegrees/10);
		int tenthDegreeAbove = 10 * (int)Math.ceil(angleInDegrees/10);
		
		// check which tenth degree the angle is closest to
		var down = Math.abs(angleInDegrees - tenthDegreeBelow);
		var up = Math.abs(tenthDegreeAbove - angleInDegrees);
		
		var tenthDegree = 0;
		
		if (up < down) {
			tenthDegree = tenthDegreeAbove;
		} else {
			tenthDegree = tenthDegreeBelow;
		}
		
		if (tenthDegree >= -180 && tenthDegree < 0) {
			tenthDegree += 360;
		}
		return tenthDegree;
	}
	
	// rotate an angle by minus ten degrees
	public static double rotateByMinusTenDegrees(double angle) {
		
		return angle - (Math.PI/18);

	}
	
	// rotate an angle by positive ten degrees
	public static double rotateByPlusTenDegrees(double angle) {
		
		return angle + (Math.PI/18); 
	}
	
	// nicely formats the day and month numbers to match the coursework specifications
	public static String formatDayAndMonth(int day, int month) {
		if (day < 10) {
			if (month < 10) {
				return "0" + day + "-" + "0" + month;
			} else {
				return "0" + day + "-" + month;
			}
		} else {
			if (month < 10) {
				return day + "-" + "0" + month;
			} else {
				return day + "-" + month;
			}
		}
	}
	
	@Override
	// Comparator for two Point2D objects
	public int compare(Point2D point1, Point2D point2) {
		
		// if angles are similar, sort in increasing order of Euclidean distance
		if (point1.getTheta() == point2.getTheta()) {
			if (point1.getR() < point2.getR())
				return -1;
			else if (point1.getR() > point2.getR())
				return 1;
			else
				return 0;
		} // otherwise sort in increasing order of angle of elevation
		else {
			if (point1.getTheta() < point2.getTheta())
				return -1;
			else
				return 1;
		}
	}

}
