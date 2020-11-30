package uk.ac.ed.inf.aqmaps;

public interface Point {

	public double getX();
	
	public double getY();
	
	public double getAngleFrom(Point point);
	
	public double getDistanceFrom(Point point);
	
	public boolean isCloseTo(Point point, double distance);
}
