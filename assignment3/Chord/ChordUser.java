import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.MessageDigest;
public class ChordUser{
  int port;
  public ChordUser(int p){
    port = p;
    Timer timer1 = new Timer();
    timer1.scheduleAtFixedRate(new TimerTask(){
      @Override
      public void run(){
        System.out.println("Usage: \n\tjoin <port>\n\twrite <file> (the file must be an integer stored in the working directory, i.e, ./port/file");
        System.out.println("\tread <file>\n\tdelete <file>\n\tprint");
        System.out.println("Enter: ");
        try{

          Chord chord = new Chord(port);//The errors are coming from here.
          Scanner scan= new Scanner(System.in);
          String delims = "[ ]+";
          String command = "";

          while (true) {
            String text = scan.nextLine();
            String[] tokens = text.split(delims);
            if (tokens[0].equals("join") && tokens.length == 2) {
              try {
                chord.joinRing("localhost", Integer.parseInt(tokens[1]));
              } catch (IOException e) {
                System.out.println("Error joining the ring!");
              }
            }
            if (tokens[0].equals("print")){
              chord.Print();
            }
            if (tokens[0].equals("write") && tokens.length == 2) {
              try{
                // "write_ *anything* "
                // string = antying;
                // MD5(anything)
                // filename = md5(anything)
                String path;
                // this is either a known number or a created number.
                int guid = Integer.parseInt(tokens[1]);// name of a file.]
                // token
                String thingOfaKey = Integer.toString(guid);// equal to token[1]
                MessageDigest md = MessageDigest.getInstance("MD5");

                md.update(thingOfaKey.getBytes());// should be "big integer"
                byte[] byteData = md.digest();
              //  convert the byte to hex format method 1
               StringBuffer sb = new StringBuffer();
               for (int i = 0; i < byteData.length; i++) {
                 sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
               }

               System.out.println("Digest(in hex format):: " + sb.toString());

               System.out.println("MD5: "+ new BigInteger(1, md.digest()).toString(16));

                // If you are using windows you have to use
                path = ".\\"+  port +"\\"+Integer.parseInt(tokens[1]); // path to file
  			        //path = "./"+  port +"/"+guid; // path to file
  			        FileStream file = new FileStream(path);
  		          ChordMessageInterface peer = chord.locateSuccessor(guid);
                peer.put(guid, file); // put file into ring
                //file is just an object,
                //NOTE: I'm not sure where to get the file or find it.
              }catch(FileNotFoundException e1){
                e1.printStackTrace();
                System.out.println("File was not found!");
              }catch (RemoteException e1) {
                e1.printStackTrace();
                System.out.println("File was not found!");
              }catch(IOException e){
                e.printStackTrace();
                System.out.println("Could not put file!");
              }
            }
            if (tokens[0].equals("read") && tokens.length == 2) {
              try { //TODO:
                chord.get(Integer.parseInt(tokens[1]));
              }catch (IOException e) {
                System.out.println("Could not get file!");
              }
            }
            if (tokens[0].equals("delete") && tokens.length == 2) {
              try {
                chord.delete(Integer.parseInt(tokens[1]));
              } catch (IOException e) {
                System.out.println("Could not delete file!");
              }
            }
          }
        }catch(RemoteException e){
        }
      }
    }, 1000, 1000);
  }
  static public void main(String args[]){
    if (args.length < 1 ) {
      throw new IllegalArgumentException("Parameter: <port>");
    }
    try {
      ChordUser chordUser = new ChordUser(Integer.parseInt(args[0]));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
}
