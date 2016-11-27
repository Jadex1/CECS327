import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

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
  public void beginElection() throws RemoteException{
    try{
      rightNode.electLeader(this);
      holderChord = this;
      participated = true;
    } catch (RemoteException e){
      e.printStackTrace();
    }
  }
  public void electLeader(ChordMessageInterface anotherChord) throws RemoteException {
    if(anotherChord.getId() > holderChord.getId()){
      rightNode.electLeader(anotherChord);
      anotherChord = holderChord;
      participated = true;
    } else if(anotherChord.getId() == this.getId()){
      leader = this;
      rightNode.electLeader(this);
    } else if( (anotherChord.getId() < holderChord.getId()) && (!(participated))){
      rightNode.electLeader(this);
      participated = true;
    }
  }
  public void setALeader(ChordMessageInterface anotherChord) throws RemoteException{
    if (anotherChord.getId() != this.getId()) {
      rightNode.setALeader(anotherChord);
      leader = anotherChord;
    }
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
      rightNode = chord.locateRightNode(this.getId());
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
