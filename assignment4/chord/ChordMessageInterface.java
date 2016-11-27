import java.rmi.*;
import java.io.*;

public interface ChordMessageInterface extends Remote
{
    public ChordMessageInterface getleftNode()  throws RemoteException;
    ChordMessageInterface locateRightNode(int key) throws RemoteException;
    ChordMessageInterface closestPrecedingNode(int key) throws RemoteException;
    public void joinRing(String Ip, int port)  throws RemoteException;
    public void notify(ChordMessageInterface j) throws RemoteException;
    public void beginElection() throws RemoteException;
    public void electLeader(ChordMessageInterface anotherChord) throws RemoteException;
    public void setALeader(ChordMessageInterface anotherChord) throws RemoteException;
    public boolean isAlive() throws RemoteException;
    public int getId() throws RemoteException;
    // public boolean canCommit();
    // public void doCommit();
    // public void doAbort();
    // public void haveCommitted(trans, participant);
    // public boolean getDecision(trans);
    public void put(int guid, InputStream file) throws IOException, RemoteException;
    public InputStream get(int id) throws IOException, RemoteException;
    public void delete(int id) throws IOException, RemoteException;
}
