import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class WorkerThread extends Thread{
    int wid;

    Chunk Request;

    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    WorkerThread( int wid, Chunk Request, ObjectOutputStream out){
        this.Request = Request;
        this.wid = wid;
        this.out = out;




    }
    public WorkerThread() {
        super();
    }

    public void run(){

        try{
            Map<String, Double> results = Calculate(Request);
            System.out.println("Calculated chunk");
            out.writeObject(results);
            out.flush();
            System.out.println("Chunk Sent");


        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

    private static Map<String, Double> Calculate(Chunk chunk) {
        List<Map<String, String>> waypoints = chunk.getWaypoints();

        double totalDistance = 0.0;
        double totalTime = 0.0;
        double totalElevation = 0.0;
        int numWaypoints = waypoints.size();

        double[] latitudes = new double[numWaypoints];
        double[] longitudes = new double[numWaypoints];
        double[] elevations = new double[numWaypoints];
        Date[] times = new Date[numWaypoints];
        for (int i = 0; i < numWaypoints; i++) {
            Map<String, String> waypoint = waypoints.get(i);
            latitudes[i] = Double.parseDouble(waypoint.get("lat"));
            longitudes[i] = Double.parseDouble(waypoint.get("lon"));
            elevations[i] = Double.parseDouble(waypoint.get("ele"));
            times[i] = parseDate(waypoint.get("time"));
        }

        double elevation_diff;
        for (int i = 1; i < numWaypoints; i++) {
            double distance = haversineDistance(latitudes[i-1], longitudes[i-1], latitudes[i], longitudes[i]);
            totalDistance += distance;
            totalTime += (times[i].getTime() - times[i-1].getTime()) / 1000.0;
            elevation_diff = elevations[i] - elevations[i-1];
            if (elevation_diff >0){
                totalElevation += elevation_diff;
            }
        }

        double averageSpeed = totalDistance / totalTime;


        // Store the results in a map and return it
        Map<String, Double> result = new HashMap<String, Double>();
        result.put("totalDistance", totalDistance);
        result.put("averageSpeed", averageSpeed);
        result.put("totalElevation", totalElevation);
        result.put("totalTime", totalTime);
        return result;
    }


    private static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
    
}