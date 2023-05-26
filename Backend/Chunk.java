import java.io.Serializable;
import java.util.*;

public class Chunk implements Serializable {

    List<Map<String,String>> waypoints = new ArrayList<Map<String, String>>();
    int userId;

    Map<String, Double> iResults = new HashMap<String,Double>();

    Chunk(List<Map<String,String>> waypoints, int userId){
        this.userId = userId;
        this.waypoints = waypoints;
    }

    Chunk(int userId){
        this.userId = userId;
    }

    public int getID(){
        return this.userId;
    }



    public void addWp(Map<String,String> waypoint){
        waypoints.add(waypoint);

    }

    public int getSize(){
        return this.waypoints.size();
    }

    public List<Map<String,String>> getWaypoints(){
        return this.waypoints;
    }

}
