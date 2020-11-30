package uk.ac.ed.inf.aqmaps;

public class Point2D implements Point {

	private int index = -1;
	private double x;
	private double y;
	private double r = 0;
	private double theta = 0;
	
	public Point2D(SensorData.Location.Coordinates point, SensorData.Location.Coordinates centre, int index) {
		this.x = point.lng;
		this.y = point.lat;
		this.index = index;
		this.setAngleDistancePair(centre);
	}
	
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;		
	}
	
	// copy constructor
	public Point2D(Point2D point) {
		this.x = point.x;
		this.y = point.y;
	}
	
	public int getIndex() {	return index; }

	public double getX() {	return x; }

	public double getY() { return y; }

	public double getR() { return r; }

	public double getTheta() { return theta; }
	
	public void setAngleDistancePair(SensorData.Location.Coordinates centre) {
		
		var dy = this.y - centre.lat;
		var dx = this.x - centre.lng;
		
		var distance = Math.sqrt(dx*dx + dy*dy);
		var angle = Math.atan2(dy, dx);

		this.r = distance;
		this.theta = angle;
	}
	
	public double getAngleFrom(Point point) {
		
		var dy = this.y - point.getY();
		var dx = this.x - point.getX();
		
		return Math.atan2(dy, dx);
	}
	
	public double getDistanceFrom(Point point) {
		
		var dy = this.y - point.getY();
		var dx = this.x - point.getX();
		
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public boolean isCloseTo(Point point, double distance) {
		
		var dx = this.getX() - point.getX();
		var dy = this.getY() - point.getY();

		return Math.sqrt(dx*dx + dy*dy) < distance;
	}

}
