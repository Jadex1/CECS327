import java.io.*;
import java.util.HashMap;
public class Details
{
  static class FileTimes implements Serializable{
   int lastTimeWritten;
   int lastTimeRead;
  }
      public static void main(String [] args)
      {

           HashMap<Integer, FileTimes> hmap = new HashMap<Integer, FileTimes>();
           //Adding elements to HashMap
           FileTimes times = new FileTimes();
           times.lastTimeRead = 100;
           times.lastTimeWritten = 200;
           hmap.put(13513512, times);
           try
           {
                  FileOutputStream fos =
                     new FileOutputStream("hashmap.ser");
                  ObjectOutputStream oos = new ObjectOutputStream(fos);
                  oos.writeObject(hmap);
                  oos.close();
                  fos.close();
                  System.out.printf("Serialized HashMap data is saved in hashmap.ser");
           }catch(IOException ioe)
            {
                  ioe.printStackTrace();
            }
      }
}
