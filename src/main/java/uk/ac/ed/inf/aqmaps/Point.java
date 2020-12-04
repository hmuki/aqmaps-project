package uk.ac.ed.inf.aqmaps;

public interface Point {

	public double getX(); // return the x-coordinate
	
	public double getY(); // return the y-coordinate
	
	public double getAngleFrom(Point point); // return the angle of elevation of this point from the specified point
	
	public double getDistanceFrom(Point point); // return the Euclidean distance from this point between this point and the specified point
	
	public boolean isCloseTo(Point point, double distance); // return true if this point within the specified distance of the specified point
}
