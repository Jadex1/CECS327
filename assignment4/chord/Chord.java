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
  long aDate;
  HashMap<Integer, FileTimes> atomicMap;

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
  /*! \fn atomicTransaction does an atomic transaction.
      \brief Hashes file name and attempts to read the file contained at succ of guid
      \param String fileName the name of the file you watn to write.
      \param Transaction Operaiton, the operatoin you want to perform.
  */
  public void atomicTransaction(String fileName, Transaction.Operation op) throws RemoteException, FileNotFoundException, IOException {
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

    Transaction t = new Transaction(op, fileHashed, true, file);

    Boolean p1 = peer1.canCommit(t);
    Boolean p2 = peer2.canCommit(t);
    Boolean p3 = peer3.canCommit(t);
    if (p1 && p2 && p3){
      System.out.println("we can commit!");
      peer1.doCommit(t, guid1);
      peer2.doCommit(t, guid2);
      peer3.doCommit(t, guid3);
    } else {
      System.out.println("we must abort");
      peer1.doAbort(t);// needs to end the cycle.
      peer2.doAbort(t);
      peer3.doAbort(t);
    }
  }
  /*! \fn canCommit
      \brief
      \param transaction trans
  */
  public boolean canCommit(Transaction trans) throws RemoteException, FileNotFoundException, IOException{
    HashMap<Integer, FileTimes> log = decodeLog();

    if(log == null){
      System.out.println("log does not exist");
      return true;
    }
    FileTimes times = (FileTimes) log.get(trans.id);// get our file times
    if( times == null){
      System.out.println("time is null!!!");
      return true;
    }
    // else log exits, get the times for our transaction id
    long lastTimeRead = times.lastTimeRead;
    long lastTimeWritten = times.lastTimeWritten;
    System.out.println("trans time:"+trans.time);
    if (trans.time > lastTimeRead && trans.time > lastTimeWritten) {
      //save transaction to temp dir
      String fileName = "./"+i+"/temp/"+trans.id;
      try {
        FileOutputStream output = new FileOutputStream(fileName);
        while (trans.fileStream.available() > 0) {
          output.write(trans.fileStream.read());
        }
      } catch (IOException e) {
        System.out.println(e);
      }
      System.out.println(i+": can commit!");
      return true;
    } else {
      return false;
    }
  }
  /*! \fn doCommit
      \brief
      \param Transaction trans
      \param int guid
  */
  public void doCommit(Transaction trans, int guid) throws RemoteException {
    atomicMap = decodeLog();
    FileTimes times = new FileTimes();

    aDate = new Date().getTime();
    switch(trans.op) {
    case READ:
      this.get(guid);
      times.lastTimeRead = aDate;
      break;
    case WRITE:
      this.put(guid, trans.fileStream);
      System.out.println("Writing guid:"+guid+" time:"+aDate);
      times.lastTimeWritten = aDate;
      break;
    case DELETE:
      this.delete(guid);
      atomicMap.remove(guid);
      times.lastTimeWritten = aDate;
    default :
      System.out.println("Not a proper transaction");
    }
    if(atomicMap != null) {
      atomicMap.put(trans.id, times);
      encodeLog(atomicMap);
    } else {// log does not exist
      HashMap<Integer, FileTimes> atomicMap =  new HashMap<Integer, FileTimes>();
      atomicMap.put(trans.id, times);
      encodeLog(atomicMap);
    }
    this.doAbort(trans);//cleanup temp files
  }
  public void doAbort(Transaction trans) throws RemoteException {
    String fileName = "./"+i+"/temp/"+trans.id;
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
  /*! \fn encodeLog
      \brief
      \param HashMap<Integer, FileTimes> map
  */
  public void encodeLog(HashMap<Integer, FileTimes> map) {
    try{
        FileOutputStream fos =
        new FileOutputStream("transaction.log");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(map);
        oos.close();
        fos.close();
        System.out.println("Serialized HashMap data is saved in transaction.log");
    } catch(IOException ioe) {
       ioe.printStackTrace();
     }
  }
  /*! \fn decodeLog
      \brief Hashes file name and attempts to read the file contained at succ of guid
  */
  public HashMap<Integer, FileTimes> decodeLog(){
    HashMap<Integer, FileTimes> map = null;
    try
    {
       FileInputStream fis = new FileInputStream("transaction.log");
       ObjectInputStream ois = new ObjectInputStream(fis);
       map = (HashMap) ois.readObject();
       ois.close();
       fis.close();
    }catch(IOException ioe) {
       return null;
    }catch(ClassNotFoundException c) {
       System.out.println("Class not found");
       return null;
    }
    System.out.println("Deserialized HashMap..");
    return map;
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
