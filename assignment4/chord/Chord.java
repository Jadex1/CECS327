import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Chord extends java.rmi.server.UnicastRemoteObject implements ChordMessageInterface {
  public static final int M = 2;
  Registry registry;    // rmi registry for lookup the remote objects.
  ChordMessageInterface rightNode;
  ChordMessageInterface leftNode;
  ChordMessageInterface[] finger;
  ChordMessageInterface leader = null;
  ChordMessageInterface holderChord = null;
  boolean participated = false;
  int nextFinger;
  int i;   		// GUID
  Date date;
  Map<Integer, String> atomicMap;

  class FileTimes implements Serializable{
   int lastTimeWritten;
   int lastTimeRead;
  }
  public ChordMessageInterface rmiChord(String ip, int port) {
    ChordMessageInterface chord = null;
    try{
      Registry registry = LocateRegistry.getRegistry(ip, port);
      chord = (ChordMessageInterface)(registry.lookup("Chord"));
      return chord;
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch(NotBoundException e){
      e.printStackTrace();
    }
    return null;
  }
  public Boolean isKeyInSemiCloseInterval(int key, int key1, int key2){
    if (key1 < key2){
      return (key > key1 && key <= key2);
    }else{
      return (key > key1 || key <= key2);
    }
  }
  public Boolean isKeyInOpenInterval(int key, int key1, int key2){
    if (key1 < key2){
      return (key > key1 && key < key2);
    }else{
      return (key > key1 || key < key2);
    }
  }
  public void put(int guid, InputStream stream) throws RemoteException {
    // so the atomic commit should be called from the put i think.
    try {
      String fileName = "./"+i+"/repository/" + guid;
	    FileOutputStream output = new FileOutputStream(fileName);
      while (stream.available() > 0){
        output.write(stream.read());
      }
    } catch (IOException e){
      System.out.println(e);
    }
  }
  public InputStream get(int guid) throws RemoteException {
    String fileName = "./"+i+"/repository/" + guid;
	  FileStream file= null;
    //TODO: create print statement for success or failur of read.
    try{
      file = new FileStream(fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return file;
  }
  public void delete(int guid) throws RemoteException {
    String fileName = "./"+i+"/repository/" + guid;
    File file = new File(fileName);
    file.delete();
  }
  public void atomicWrite(String fileName) throws RemoteException, FileNotFoundException, IOException {
    String path = "./"+i+"/"+fileName; // path to input file
		FileStream file = new FileStream(path);
    //figure out which of the peers will be invloved in transaction
    Integer fileHashed = MD5(fileName);
    // add loop here.
    Integer guid1 = MD5(fileName +"1");
    Integer guid2 = MD5(fileName +"2");
    Integer guid3 = MD5(fileName +"3");
    ChordMessageInterface peer1 = this.locateRightNode(guid1);
    ChordMessageInterface peer2 = this.locateRightNode(guid2);
    ChordMessageInterface peer3 = this.locateRightNode(guid3);

    Transaction t = new Transaction(Transaction.Operation.WRITE, fileHashed, true, file);

    Boolean p1 = peer1.canCommit(t);
    Boolean p2 = peer2.canCommit(t);
    Boolean p3 = peer3.canCommit(t);
    // I feel like something is suppose to go here.
    if (p1 && p2 && p3){
      System.out.println("we can commit!");
      peer1.doCommit(t, guid1);
      peer2.doCommit(t, guid2);
      peer3.doCommit(t, guid3);
    } else {
      System.out.println("we must abort");
      // is this process aborting?
<<<<<<< HEAD
      self.doAbort();//delete the temp files; what do we use the t, for?
=======
      // Everyone abort.
      peer1.doAbort();// needs to end the cycle.
      peer2.doAbort();
      peer3.doAbort();
      // NOTE: the abort means someone said no, so we all reset, back to zero.
      // everyone cleans up their tmp folders.
      //delete the temp files; what do we use the t, for? <- after i edited
>>>>>>> 0c5ca8f1ba549e8765fa15f169cb35846de6e3de
    }
  }
  public boolean canCommit(Transaction trans) throws RemoteException {// when does canCommit get called?

    //from class
    //use dictionary 'filecontrol' guid:int, lastTimeWritten:timestamp, lastTimeRead:timestammp
    // if(transLogExists()) {
    //   return true;
    // }
    // else if (T.Time > FileControl(T.id.lastTimeRead) && T.time > filecontrol(t.TransactionId.lastTimeWritten)) {
    //  return true;
    //  else {
    //    return false;
    //  }
    //}
    // store transaction in local .temp file
    // check local transaction to make sure it matches the one we wish to execute
    // if YES, prepare the file in ./i/temp, return true
    // if NO, return false
    //
    System.out.println(i+": can commit!");
    return true;
  }
  public void doCommit(Transaction trans, int guid) throws RemoteException {

    // calling .put() here?
    atomicMap = new Hashmap<Integer, String>();
    atomicMap.put(guid, trans);
    FileControl control = new FileControl();
<<<<<<< HEAD
    
    if (trans.Operation == Transaction.Operation.READ){
      self.put(guid, trans.fileStream);
      aDate = new Date();
=======
    Date date;
    if (trans.Operation == Transaction.Operation.READ){
      self.put(guid, trans.fileStream);
      aDate = new Date();// convert 
>>>>>>> 0c5ca8f1ba549e8765fa15f169cb35846de6e3de
      control.lastTimeRead = aDate;
    }

    if (trans.Operation == Transacton.Operation.WRITE) {
      self.get(guid);
      aDate = new Date();
      control.lastTimeWritten = aDate;
    }

    if (trans.Operation == Transaction.Opertion.DELETE){
      self.delete(guid);
      atomicMap.delete(guid);
      aDate = new Date();
      control.lastTimeWritten = aDate;
    }
    // control.lastTimeRead
    // atomicMap.put(trans.id, control);
  }
<<<<<<< HEAD
  public void doAbort(Transaction t) throws RemoteException {
=======
  public void doAbort() throws RemoteException {
>>>>>>> 0c5ca8f1ba549e8765fa15f169cb35846de6e3de
    // In one of the methods we pass the t to the doAbort, we need to change that.
    // cleanUpTempFiles();
    // delete tmp files if they exist
    // each process should have a tmp at ./tmp/i
    // do we need to resart any cycles or loops?
    String fileName = "./"+tmp+"/"+i;
    File file = new File(fileName);
    file.delete();
  }
  public void haveCommitted(Transaction trans, ChordMessageInterface participant) throws RemoteException {

  }
  public boolean getDecision(Transaction trans) throws RemoteException {
    return true;
  }
  public int getId() throws RemoteException {
    return i;
  }
  public boolean isAlive() throws RemoteException {
    return true;
  }
  public ChordMessageInterface getleftNode() throws RemoteException {
    return leftNode;
  }
  public ChordMessageInterface locateRightNode(int key) throws RemoteException {
    if (key == i){
      throw new IllegalArgumentException("Key must be distinct that  " + i);
    }
    if (rightNode.getId() != i){
      if(isKeyInSemiCloseInterval(key, i, rightNode.getId())){
        return rightNode;
      }
      ChordMessageInterface j = closestPrecedingNode(key);
      if(j == null){
        return null;
      }
      return j.locateRightNode(key);
    }
    return rightNode;
  }
  public ChordMessageInterface closestPrecedingNode(int key) throws RemoteException {
    int count = M-1;
    if (key == i)  throw new IllegalArgumentException("Key must be distinct that  " + i);
    for (count = M-1; count >= 0; count--) {
      if (finger[count] != null && isKeyInOpenInterval(finger[count].getId(), i, key))
      return finger[count];
    }
    return rightNode;
  }
  public void joinRing(String ip, int port)  throws RemoteException {
    try{
      System.out.println("Get Registry to joining ring");
      Registry registry = LocateRegistry.getRegistry(ip, port);
      ChordMessageInterface chord = (ChordMessageInterface)(registry.lookup("Chord"));//NOTE: Stopping right here.
      System.out.println("Found Chord");
      leftNode = null;
      System.out.println("locating the node to your right." + port);
      rightNode = chord.locateRightNode(this.getId());
      if(rightNode != null){
        System.out.println("Right Node"+rightNode.getId());
      }
	    System.out.println("Joining ring");
    } catch(RemoteException | NotBoundException e){
      rightNode = this;
    }
  }
  public void findingNextRightNode(){
    int i;
    rightNode = this;
    for (i = 0;  i< M; i++) {
      try {
        if (finger[i].isAlive()){
          rightNode = finger[i];
        }
      } catch(RemoteException | NullPointerException e) {
        finger[i] = null;
      }
    }
  }
  public void stabilize() {
    boolean error = false;
    try {
      if (rightNode != null) {
        ChordMessageInterface x = rightNode.getleftNode();
        if (x != null && x.getId() != this.getId() && isKeyInOpenInterval(x.getId(), this.getId(), rightNode.getId())){
          rightNode = x;
        }
        if (rightNode.getId() != getId()){
          rightNode.notify(this);
        }
      }
    } catch(RemoteException | NullPointerException e1) {
      error = true;
    }
    if (error){
      findingNextRightNode();
    }
  }
  public void notify(ChordMessageInterface j) throws RemoteException {
    if (leftNode == null || (leftNode != null && isKeyInOpenInterval(j.getId(), leftNode.getId(), i))){
      findingNextRightNode();
    }
	 // TODO
	 //transfer keys in the range [j,i) to j;
	 }
  public void fixFingers() {// && rightNode != null)
     int id= i;
     try {
       int nextId;
       if(nextFinger == 0){
         nextId = (this.getId() + (1 << nextFinger));
       }else{
         nextId = finger[nextFinger -1].getId();
       }
       finger[nextFinger] = locateRightNode(nextId);
       if (finger[nextFinger].getId() == i){
         finger[nextFinger] = null;
       }else{
         nextFinger = (nextFinger + 1) % M;
       }
     } catch(RemoteException | NullPointerException e){
       finger[nextFinger] = null;
       e.printStackTrace();
     }
   }
  public void checkleftNode() {
    try {
      if (leftNode != null && !leftNode.isAlive()){
        leftNode = null;
      }
    }catch(RemoteException e){
      leftNode = null;
    }//e.printStackTrace();
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
  public Chord(final int port) throws RemoteException {
    int j;
    finger = new ChordMessageInterface[M];
    for(j=0;j<M; j++){
      finger[j] = null;
    }
    i = port;
    leftNode = null;
    rightNode = this;
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        stabilize();
	      fixFingers();
	      checkleftNode();
      }
    }, 500, 500);
    try{
      // create the registry and bind the name and object.
      System.out.println("Starting RMI at port="+port);
	    registry = LocateRegistry.createRegistry( port );
      registry.rebind("Chord", this);
    } catch(RemoteException e){
      throw e;
    }
  }
  void Print() {
    int i;
    try {
      if (rightNode != null){
        System.out.println("rightNode "+ rightNode.getId());
      }
      if (leftNode != null){

        System.out.println("leftNode "+ leftNode.getId());
      }
      for (i=0; i<M; i++){
        try {
          if (finger != null){
            System.out.println("Finger "+ i + " " + finger[i].getId());
          }
        } catch(NullPointerException e) {
            finger[i] = null;
          }
        }
      } catch(RemoteException e){
        System.out.println("Cannot retrive id");
      }
    }
  }
