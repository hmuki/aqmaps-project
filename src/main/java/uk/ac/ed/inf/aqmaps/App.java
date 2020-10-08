package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException
    {
        var client = HttpClient.newHttpClient();
        var url = "http://localhost:80/buildings/no-fly-zones.geojson";
        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        var response = client.send(request, BodyHandlers.ofString());
        
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
}
