import java.io.*;
import java.util.*;

public class Test implements Serializable {
    public static void main(String[] args) {
        
        List<Integer> chunk = new ArrayList<Integer>();
        chunk.add(1);
        chunk.add(2);
        chunk.add(3);
        chunk.remove(0);
        System.out.println(chunk.get(0));

    }
    
}
