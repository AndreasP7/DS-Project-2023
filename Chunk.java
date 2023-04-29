import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chunk {

    List<Map<String,String>> waypoints = new ArrayList<Map<String, String>>();
    int userId;

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

}
