import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//TODO: Write a Method for MD5, that takes the string and returns a string.

public class ChordUser {
	int port;
	int[] ports = {1,2,3};
	public ChordUser(int p) {
		port = p;
		Timer timer1 = new Timer();
		timer1.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					Chord chord = new Chord(port);
					System.out.println("Usage: \n\tjoin <port>\n\twrite <file> (the file must be an integer stored in the working directory, i.e, ./port/file");
					System.out.println("\tread <file>\n\tdelete <file>\n\tprint");
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
							if(tokens.length == 2){
							try {
  								for(int i =0;i<ports.length;i++){
										String inputFilePath = "./"+port+"/"+tokens[1];
										System.out.println("Open path to file:"+inputFilePath);
	                  FileStream file = new FileStream(inputFilePath);
	  								int guid = MD5(tokens[1]+ports[i]);
									  ChordMessageInterface peer = chord.locateSuccessor(guid);
									  peer.put(guid, file); // put file into ring
									}
								} catch (Exception e) {
								e.printStackTrace();
								}
							}
							else if(tokens.length == 1) {
								File[] files = new File("./"+port).listFiles();
								try {
									for(final File fileEntry: files){//for all files in directory
										if(fileEntry.isFile()){//if its a file
											String inputFilePath = "./"+port+"/"+fileEntry.getName();
											System.out.println("Open path to file:"+inputFilePath);
											for(int i =0;i<ports.length;i++){
												FileStream file = new FileStream(inputFilePath);
												int guid = MD5(fileEntry.getName()+ports[i]);
												ChordMessageInterface peer = chord.locateSuccessor(guid);
												peer.put(guid, file); // put file into ring
											}
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						if (tokens[0].equals("read") && tokens.length == 2) {
                for(int i =0;i<ports.length;i++){
                  int guid = MD5(tokens[1]+ports[i]);// translate file we want into HASH (3 times)
                  ChordMessageInterface peer = chord.locateSuccessor(guid);//get the peer where that file is
  								try {
										if(peer.get(guid) != null){//if we can locate file by its guid
											InputStream stream = peer.get(guid);
		  			  				String path = "./"+  port +"/"+tokens[1]; // output path to local file
	  									FileOutputStream output = new FileOutputStream(path);
	  									while (stream.available() > 0){
	  										output.write(stream.read());
	  									}
								   	}
  								} catch (IOException e) {
  									System.out.println(e);
  								}
              	}
						}

					if  (tokens[0].equals("delete") && tokens.length == 2) {
						try {
							for(int i =0;i<ports.length;i++){
								int guid = MD5(tokens[1]+ports[i]);// translate file we want into HASH (3 times)
								ChordMessageInterface peer = chord.locateSuccessor(guid);
								peer.delete(guid);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch(RemoteException e) {}
			}
		}, 1000, 1000);
	}
	public int MD5(String aStringtoHash){
		int smallerNumber = 0;
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(aStringtoHash.getBytes());
			BigInteger bigNumber = new BigInteger(1, messageDigest);
			BigInteger aMod = new BigInteger("2768");
			smallerNumber = bigNumber.mod(aMod).intValue();
		} catch(Exception e){
			e.printStackTrace();
		}
		return smallerNumber;
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
