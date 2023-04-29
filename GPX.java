
import java.io.*;
public class GPX implements Serializable {
    String path;
    File file;
    int results;

    int uid;
    
    GPX(String path, int uid){
        this.path = path;
        this.file = new File(path);
        this.uid = uid;
    }
    void setResults(int r){
        this.results = r;
    }
    int getResults(){
        return this.results;
    }

    int getUid(){
        return this.uid;
    }

    String ReadFile(String path){
        String text = "";
        try{
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line!=null){
                text += line;
                line = br.readLine();
            }

            br.close();
            
            
        }
       
        catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return text;    
            
    }


}
