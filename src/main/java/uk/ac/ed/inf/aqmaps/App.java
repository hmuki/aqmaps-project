package uk.ac.ed.inf.aqmaps;


public class App 
{
    public static void main(String[] args)  
    {
    	final var port = Integer.parseInt(args[6]);
    	final var releasePoint = new Point2D(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
    	final var seed = Integer.parseInt(args[5]);
    	final var flight = new DroneFlight(args[0], args[1], args[2], port, releasePoint, seed);
    	flight.execute();
    }
}








































































































