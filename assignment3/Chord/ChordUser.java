import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
                // make the second token look for any string.
                // "write_ *anything* "
                // string = antying;
                // MD5(anything)
                // filename = md5(anything)
                String path;
                int guid = Integer.parseInt(tokens[1]);// name of a file.

                System.out.println("Shit's here"+guid);
                String thingOfaKey = Integer.toString(guid);// equal to token[1]
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(thingOfaKey.getBytes());
                BigInteger bigNumber = new BigInteger(1, messageDigest);// now make big int small number. in the 1000s
                BigInteger aMod = new BigInteger("2768");

                // TODO: add small number here.
                int smallerNumber = bigNumber.mod(aMod).intValue();
                String hashtext = Integer.toString(smallerNumber);
                // Now we need zero pad it if you actually want the full 32 chars.
                // while(hashtext.length() < 32){
                //   hashtext = "0" + hashtext;
                // }

                System.out.println("The result of the Hash:"+hashtext);// hashtext works
                path = "./"+port+"/"+hashtext; // path to file
                //NOTE: Still haven't wrote anything yet?
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
              }catch(Exception e){// generic Exception
                e.printStackTrace();
                throw new RuntimeException(e);
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
