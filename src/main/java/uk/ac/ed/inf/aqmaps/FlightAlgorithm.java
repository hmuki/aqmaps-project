package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Random;

public class FlightAlgorithm extends DataCollector {

	protected final Point2D[] points; // sensor locations in sorted order 
	protected final Point2D releasePoint;
	private final double moveLength = 0.0003; // length of a drone move
	private final double readingRange = 0.0002; // range within which drone's receiver can download readings.
	private final int seed;
	protected ArrayList<Point2D> droneLocations = new ArrayList<>();
	protected ArrayList<Integer> droneDirections = new ArrayList<>();
	protected ArrayList<String> sensorLocations = new ArrayList<>();
	private final double leftLongitude = -3.192473; // left-most longitude of confinement area
	private final double rightLongitude = -3.184319; // right-most longitude of confinement area.
	private final double bottomLatitude = 55.942617; // bottom-most latitude of confinement area.
	private final double topLatitude = 55.946233; // top-most latitude of confinement area.
	private final ArrayList<ArrayList<Point2D>> noFlyZones;
	
	public FlightAlgorithm(String day, String month, String year, int port, Point2D releasePoint, int seed) {
		super(day, month, year, port);
		this.releasePoint = releasePoint;
		this.seed = seed;
		this.noFlyZones = super.getNoFlyZones();
		this.points = new Utilities().getOrderedPoints(sensorReadings, releasePoint); // get ordered list of sensor locations
	}
		
	public ArrayList<Point2D> getDroneLocations() { return droneLocations; }

	public ArrayList<Integer> getDroneDirections() { return droneDirections; }

	public ArrayList<String> getSensorLocations() { return sensorLocations; }

	public Point2D[] getPoints() { return points; }

	public Point2D getReleasePoint() { return releasePoint; }
	
	// move the drone around to collect data from all sensors and return to the release point
	public void moveDrone() {
		
		final var size = points.length; // 33 sensors present
		final var maxMoves = 150;
		
		var r = new Random(); // random boolean generator
		r.setSeed(seed);
		var i = 0; // keeps count of the number of sensors visited
		var moves = 0; // records the number of moves so far
		var initialPoint = new Point2D(releasePoint);
		var d = 0.0; // for storing the distance between the current drone location and the next target
		var angle = 0.0; // for storing exact angles
		var correctedAngle = 0.0; // for storing angles that are multiples of 10 degrees
		Point2D nextMovePoint;  // for storing the drone location after a drone move
		Point2D startPoint; // for storing a copy of nextMovePoint
		// while drone has not finished reading from all sensors
		while (i < size && moves < maxMoves) {
			// compute corrected angle to next sensor from current location
			angle = points[i].getAngleFrom(initialPoint);
			correctedAngle = Utilities.getAngleOfDeviation(angle);
			nextMovePoint = move(initialPoint, correctedAngle); // make a move
			// test if location lies within the proper zones for drone movement
			if (isWithinConfinementArea(nextMovePoint) && !isWithinNoFlyZone(initialPoint, correctedAngle)) {
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
			} else {
				// correct nextMovePoint if current value lies outside the required areas
				while(!isWithinConfinementArea(nextMovePoint) || isWithinNoFlyZone(initialPoint, correctedAngle)) {
					if (r.nextBoolean()) {
						correctedAngle = Utilities.rotateByMinusTenDegrees(correctedAngle);
					} else {
						correctedAngle = Utilities.rotateByPlusTenDegrees(correctedAngle);
					}
					nextMovePoint = move(initialPoint, correctedAngle);
				}
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
			}
			moves += 1;
			d = nextMovePoint.getDistanceFrom(points[i]); // compute remaining distance to target
			if (d > readingRange) {
				sensorLocations.add("null");
			} else {
				sensorLocations.add(sensorReadings.get(points[i].getIndex()).getLocation());
			}
			while (d > readingRange) {
				
				startPoint = new Point2D(nextMovePoint); // make a copy
				angle = points[i].getAngleFrom(nextMovePoint); // compute corrected angle to next sensor from current location
				correctedAngle = Utilities.getAngleOfDeviation(angle);
				nextMovePoint = move(nextMovePoint, correctedAngle); // make a move
				// make sure it's in the proper zones
				if (isWithinConfinementArea(nextMovePoint) && !isWithinNoFlyZone(startPoint, correctedAngle)) {
					// add location and direction to logs
					droneLocations.add(nextMovePoint);
					droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				} 
				else {
					// correct nextMovePoint if current value lies outside the required areas
					while(!isWithinConfinementArea(nextMovePoint) || isWithinNoFlyZone(startPoint, correctedAngle)) {
						if (r.nextBoolean()) {
							correctedAngle = Utilities.rotateByMinusTenDegrees(correctedAngle);
						} else {
							correctedAngle = Utilities.rotateByPlusTenDegrees(correctedAngle);
						}
						nextMovePoint = move(startPoint, correctedAngle);
					}
					// add location and direction to logs
					droneLocations.add(nextMovePoint);
					droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				}
				moves += 1;
				d = nextMovePoint.getDistanceFrom(points[i]);
				if (d > readingRange) {
					sensorLocations.add("null");
				} else {
					sensorLocations.add(sensorReadings.get(points[i].getIndex()).getLocation());
				}
			}
			i += 1;
			initialPoint = new Point2D(nextMovePoint);
		}
		// ... after having read from all sensors
		while (moves < maxMoves && !initialPoint.isCloseTo(releasePoint, moveLength)) {
			// compute corrected angle to relasePoint from current location
			angle = releasePoint.getAngleFrom(initialPoint);
			correctedAngle = Utilities.getAngleOfDeviation(angle);
			nextMovePoint = move(initialPoint, correctedAngle); // make a move
			// make sure it's within the required zones
			if (isWithinConfinementArea(nextMovePoint) && !isWithinNoFlyZone(initialPoint, correctedAngle)) {
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				sensorLocations.add("null");
				initialPoint = new Point2D(nextMovePoint); // make a copy
				moves += 1;
				continue;
			} else {
				// correct nextMovePoint if current value lies outside the required areas
				while (!isWithinConfinementArea(nextMovePoint) || isWithinNoFlyZone(initialPoint, correctedAngle)) {
					if (r.nextBoolean()) {
						correctedAngle = Utilities.rotateByMinusTenDegrees(correctedAngle);
					} else {
						correctedAngle = Utilities.rotateByPlusTenDegrees(correctedAngle);
					}
					nextMovePoint = move(initialPoint, correctedAngle);
				}
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				sensorLocations.add("null");
				initialPoint = new Point2D(nextMovePoint); // make a copy
				moves += 1;
				continue;
			}
		}
		System.out.println(droneLocations.size()); // print number of moves
	}
	
	// moves the drone from initialPoint along correctedAngle
	public Point2D move(Point2D initialPoint, double correctedAngle) {
		
		var dx = moveLength * Math.cos(correctedAngle);
		var dy = moveLength * Math.sin(correctedAngle);
		
		return new Point2D(initialPoint.getX() + dx, initialPoint.getY() + dy);

	}
	
	// checks if the drone lies within the confinement area
	public boolean isWithinConfinementArea(Point2D point) {
		
		return point.getX() > leftLongitude && point.getX() < rightLongitude && 
				point.getY() > bottomLatitude && point.getY() < topLatitude;		
	}
	
	// checks if the drone's trajectory from the given point along the specified angle lies within a No Fly Zone
	public boolean isWithinNoFlyZone(Point2D point, double angle) {
		
		boolean answer = false;
		for (var polygon : noFlyZones) {
			// If trajectory lies with ANY of the No Fly Zones, return true
			answer = answer || Utilities.intersectsPolygon(polygon, point, angle, moveLength);
		}
		return answer;
	}
	
}
