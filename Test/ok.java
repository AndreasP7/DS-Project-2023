package Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ok {



    public static void main(String[] args) {

        Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
        String key = "mango";
        int number = 42;
        List<Integer> list= new ArrayList<>();
        list.add(number);
        map.put(key,list);

        System.out.println(map);

        map.get(key).add(43);System.out.println(map);


    }
}
