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
						if (tokens[0].equals("write") && tokens.length == 2) {
							try {
								int guid = MD5(tokens[1]);
								// If you are using windows you have to use
								String path = "./"+  port +"/"+tokens[1]; // path to file
								FileStream file = new FileStream(path);
								ChordMessageInterface peer = chord.locateSuccessor(guid);
								peer.put(guid, file); // put file into ring
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (tokens[0].equals("read") && tokens.length == 2) {
							try {
								int guid = MD5(tokens[1]);
								// If you are using windows you have to use
								// 			path = ".\\"+  port +"\\"+Integer.parseInt(tokens[1]); // path to file
			  				String path = "./"+  port +"/"+tokens[1]; // path to file
			  				ChordMessageInterface peer = chord.locateSuccessor(guid);
			  				InputStream stream = peer.get(guid); // put file into ring
								try {
									FileOutputStream output = new FileOutputStream(path);
									while (stream.available() > 0){
										output.write(stream.read());
									}
								} catch (IOException e) {
									System.out.println(e);
								}
							} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if  (tokens[0].equals("delete") && tokens.length == 2) {
						try {
							int guid = MD5(tokens[1]);
							ChordMessageInterface peer = chord.locateSuccessor(guid);
							peer.delete(guid);
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
			BigInteger aMod = new BigInteger("32768");
			smallerNumber = bigNumber.mod(aMod).intValue();
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Could not put file!");
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
