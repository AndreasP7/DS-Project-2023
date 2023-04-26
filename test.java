import java.io.*;
import java.util.*;
import java.util.stream.*;

public class Test  {
    public static void main(String[] args) {
        
        List<Integer> chunk = Arrays.asList(1,2,3,4);
        
        int square_sum = chunk.stream().map(x -> x*x).reduce(0 ,(a,b) -> a+b);

        

        System.out.println(square_sum);
     

    }

    
    
}
