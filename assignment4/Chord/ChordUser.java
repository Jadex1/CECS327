import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChordUser {
	int port;
	public ChordUser(int p) {
		port = p;
		Timer timer1 = new Timer();
		timer1.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					Chord chord = new Chord(port);
					System.out.println("Usage: \n\tjoin <port>\n\twrite <file> (the file must be an integer stored in the working directory, i.e, ./port/file");
					System.out.println("\tread <file>\n\tdelete <file>\n\tprint\n\telect");
					Scanner scan= new Scanner(System.in);
					String delims = "[ ]+";
					String command = "";
					while (true) {
						String text= scan.nextLine();
						String[] tokens = text.split(delims);
						if (tokens[0].equals("join") && tokens.length == 2) {
							try {
								chord.joinRing("localhost", Integer.parseInt(tokens[1]));
							} catch (IOException e) {
								System.out.println("Error joining the ring!");
							}
						}
						if (tokens[0].equals("print")) {
							chord.Print();
						}
						if (tokens[0].equals("write")) {
							try {
								try {
	               chord.atomicWrite(tokens[1]);
							 } catch(FileNotFoundException e) {
								 System.out.println("Atomic write error!:"+e);
							 }
						 } catch(IOException e) {
							 System.out.println(e);
						 }
						}
						if (tokens[0].equals("read") && tokens.length == 2) {
							read(tokens, chord);
						}
					  if (tokens[0].equals("delete") && tokens.length == 2) {
							delete(tokens,chord);
						}
					}
				} catch(RemoteException e) {}
			}
		}, 1000, 1000);
	}
  /*! \fn MD5
      \brief if user specifies a filename the file will be hashed and copyed onto the corresponding peer.
      \param aStringtoHash
  */
	public int MD5(String aStringtoHash){
		int smallerNumber = 0;
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(aStringtoHash.getBytes());
			BigInteger bigNumber = new BigInteger(1, messageDigest);
			BigInteger aMod = new BigInteger("32768");
			smallerNumber = bigNumber.mod(aMod).intValue();
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Could not put file!");
		}
		return smallerNumber;
	}
  /*! \fn write
      \brief if user specifies a filename the file will be hashed and copied onto the corresponding peer.
      \breif if user types 'write' the current directoy of the ChordUser will be uploaded and distributed accordingly to DFSS.
			\param tokens[] user entered info
			\param Chord ChordUser
  */
  public void write(String[] tokens,Chord chord){
      if(tokens.length == 2){
        try {
          String path = "./"+port+"/"+tokens[1]; // path to file
          System.out.println("Open path to file: "+path);
          FileStream file = new FileStream(path);
          for (int i = 1; i < 3; i++ ) {
            int guid = MD5(tokens[1]+i);
            ChordMessageInterface peer = chord.locateRightNode(guid);
            peer.put(guid, file); // put file into ring
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if(tokens.length == 1) {
        File[] files = new File("./"+port).listFiles();
        try {
          for(final File fileEntry: files){//for all files in directory
            if(fileEntry.isFile()){//if its a file
              String inputFilePath = "./"+port+"/"+fileEntry.getName();
              System.out.println("Open path to file:"+inputFilePath);
              for(int i = 1; i < 3;i++){
                FileStream file = new FileStream(inputFilePath);
                int guid = MD5(fileEntry.getName()+i);
                ChordMessageInterface peer = chord.locateRightNode(guid);
                peer.put(guid, file); // put file into ring
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
  }
  /*! \fn read
      \brief Hashes file name and attempts to read the file contained at succ of guid
      \param tokens[] user entered info
			\param Chord ChordUser
  */
  public void read(String[] tokens,Chord chord){
    try {
      // Just before or around here we begin the protocol
      for (int i = 0; i < 2; i++) {
        int guid = MD5(tokens[1]+i);
        ChordMessageInterface peer = chord.locateRightNode(guid);
        String path ="./"+ port +"/"+tokens[1]; // path to file
        InputStream stream = peer.get(guid); // put file into ring
        try {
          FileOutputStream output = new FileOutputStream(path);
          while (stream.available() > 0){
            output.write(stream.read());
          }
        } catch (IOException e) {
          System.out.println(e);
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
	/*! \fn delete
      \brief Hases <filename> and deletes files at guid1,guid2...
			\param tokens[] user entered info
			\param Chord ChordUser
  */
  public void delete(String[] tokens,Chord chord){
    try {
      for(int i = 1; i < 3; i++){
        int guid = MD5(tokens[1]+i);// translate file we want into HASH (3 times)
        ChordMessageInterface peer = chord.locateRightNode(guid);
        peer.delete(guid);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
	static public void main(String args[]){
		if (args.length < 1 ) {
			throw new IllegalArgumentException("Parameter: <port>");
		}
		try{
			ChordUser chordUser=new ChordUser( Integer.parseInt(args[0]));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
