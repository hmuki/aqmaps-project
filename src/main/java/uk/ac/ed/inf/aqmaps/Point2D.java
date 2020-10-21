package uk.ac.ed.inf.aqmaps;

public class Point2D  {
	
	public int index;
	public double x;
	public double y;
	public double r;
	public double theta;
	
	public Point2D(SensorData.Location.Coordinates point, SensorData.Location.Coordinates centre, int index) {
		this.x = point.lng;
		this.y = point.lat;
		this.index = index;
		this.setAngleDistancePair(centre);
	}
	
	public Point2D(SensorData.Location.Coordinates point) {
		this.x = point.lng;
		this.y = point.lat;
		this.index = -1;
		this.r = 0;
		this.theta = 0;
	}
	
	// copy constructor
	public Point2D(Point2D point) {
		this.x = point.x;
		this.y = point.y;
		this.index = -1;
		this.r = 0;
		this.theta = 0;
	}
	
	public void setAngleDistancePair(SensorData.Location.Coordinates centre) {
		
		var dy = this.y - centre.lat;
		var dx = this.x - centre.lng;
		
		var distance = Math.sqrt(dx*dx + dy*dy);
		var angle = Math.atan2(dy, dx);

		this.r = distance;
		this.theta = angle;
	}
	
	public Double getDistanceFrom(Point2D point) {
		
		var dy = this.y - point.y;
		var dx = this.x - point.x;
		
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public Double getAngleFrom(Point2D point) {
		
		var dy = this.y - point.y;
		var dx = this.x - point.x;
		
		return Math.atan2(dy, dx);
	}
	
}
