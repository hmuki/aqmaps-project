package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Random;

public class FlightAlgorithm extends DataCollector {

	protected final Point2D[] points;
	protected final Point2D releasePoint;
	private final double moveLength = 0.0003;
	private final double readingRange = 0.0002;
	private final int seed;
	protected ArrayList<Point2D> droneLocations = new ArrayList<>();
	protected ArrayList<Integer> droneDirections = new ArrayList<>();
	protected ArrayList<String> sensorLocations = new ArrayList<>();
	private final double leftLongitude = -3.192473;
	private final double rightLongitude = -3.184319;
	private final double bottomLatitude = 55.942617;
	private final double topLatitude = 55.946233;
	private final ArrayList<ArrayList<Point2D>> noFlyZones;
	
	public FlightAlgorithm(String day, String month, String year, int port, Point2D releasePoint, int seed) {
		super(day, month, year, port);
		this.releasePoint = releasePoint;
		this.seed = seed;
		this.noFlyZones = super.getNoFlyZones();
		this.points = new Utilities().getOrderedPoints(sensorReadings, releasePoint);
	}
		
	public ArrayList<Point2D> getDroneLocations() { return droneLocations; }

	public ArrayList<Integer> getDroneDirections() { return droneDirections; }

	public ArrayList<String> getSensorLocations() { return sensorLocations; }

	public Point2D[] getPoints() { return points; }

	public Point2D getReleasePoint() { return releasePoint; }
	
	public void moveDrone() {
		
		final var size = points.length;
		final var maxMoves = 150;
		
		var r = new Random();
		r.setSeed(seed);
		var i = 0;
		var moves = 0;
		var initialPoint = new Point2D(releasePoint);
		var d = 0.0;
		var angle = 0.0;
		var correctedAngle = 0.0;
		Point2D nextMovePoint;
		Point2D startPoint; // dummy
		// while drone has not finished reading from all sensors
		while (i < size) {
			// compute corrected angle to next sensor from current location
			angle = points[i].getAngleFrom(initialPoint);
			correctedAngle = Utilities.getAngleOfDeviation(angle);
			nextMovePoint = move(initialPoint, correctedAngle); // make a move
			// make sure it is a valid location
			if (isWithinConfinementArea(nextMovePoint) && !isWithinNoFlyZone(initialPoint, correctedAngle)) {
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
			} else {
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
				// make sure it is a valid location
				if (isWithinConfinementArea(nextMovePoint) && !isWithinNoFlyZone(startPoint, correctedAngle)) {
					// add location and direction to logs
					droneLocations.add(nextMovePoint);
					droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				} 
				else {
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
		moves = droneLocations.size(); // get number of moves so far
		while (moves < maxMoves && !initialPoint.isCloseTo(releasePoint, moveLength)) {
			// compute corrected angle to relasePoint from current location
			angle = releasePoint.getAngleFrom(initialPoint);
			correctedAngle = Utilities.getAngleOfDeviation(angle);
			nextMovePoint = move(initialPoint, correctedAngle); // make a move
			// make sure it is a valid location
			if (isWithinConfinementArea(nextMovePoint) && !isWithinNoFlyZone(initialPoint, correctedAngle)) {
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				sensorLocations.add("null");
				initialPoint = new Point2D(nextMovePoint); // make a copy
				moves += 1;
				continue;
			} else {
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
	
	public Point2D move(Point2D initialPoint, double correctedAngle) {
		
		var dx = moveLength * Math.cos(correctedAngle);
		var dy = moveLength * Math.sin(correctedAngle);
		
		return new Point2D(initialPoint.getX() + dx, initialPoint.getY() + dy);

	}
	
	public boolean isWithinConfinementArea(Point2D point) {
		
		return point.getX() > leftLongitude && point.getX() < rightLongitude && 
				point.getY() > bottomLatitude && point.getY() < topLatitude;		
	}
	
	public boolean isWithinNoFlyZone(Point2D point, double angle) {
		
		boolean answer = false;
		for (var polygon : noFlyZones) {
			answer = answer || Utilities.intersectsPolygon(polygon, point, angle, moveLength);
		}
		return answer;
	}
	
}
