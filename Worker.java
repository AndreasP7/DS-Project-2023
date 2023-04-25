import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;


public class Worker extends Thread{
    int port;
    //connect to server on socket
    Worker(int port){
        this.port = port;
    }

    public void run(){
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket requestSocket = null;

        try{
            String host = "localhost";
            requestSocket = new Socket(host, this.port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            List<Map<String,String>> chunk = (List<Map<String,String>> ) in.readObject();
    
            Map<String,Integer> results = new HashMap<String, Integer>();
            results.put("Result 1",1);
            out.writeObject(results);

            int d = (int) in.readObject();
            out.writeObject(d+1);
            out.flush();
            

            //send results

            out.flush();
        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

   

    private static Map<String, Double> Calculate(List<Map<String, String>> waypoints) {
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
        Map<String, Double> result = new HashMap<>();
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
  
        

    public static void main(String[] args) {
        GPX gpx = new GPX("gpxs/route1.gpx");
        List<Map<String,String>> chunk = Master.parseGPX(gpx);
        System.out.println(Calculate(chunk));
    

        
        
        /*new Worker(4019);
        new Worker(4019);
        new Worker(4019);
        new Worker(4019);
        new Worker(4019);
        new Worker(4019);
        new Worker(4019);
        new Worker(4019);*/
        
    }
}
