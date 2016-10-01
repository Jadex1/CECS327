import java.net.*;
import java.io.*;
import java.util.*;
 /*****************************//**
  * \brief It implements a distributed chat.
  * It creates a ring and delivers messages
  * using flooding
  **********************************/
public class Chat implements Serializable {
  public static int predPort;// port before me
  public static int succPort;// port after me
  public enum enum_MSG {
    JOIN,     // 0
    ACCEPT,   // 1
    LEAVE,    // 2
    PUT,      // 3
  };
  public class Message implements Serializable {
    public enum_MSG msgid;
    public int portSrc;
    public int portDest;
    public String text;
    public Boolean fromInput;
  }
  /*****************************//**
  * \class Server class "chat.java"
  * \brief It implements the server
  **********************************/
  private class Server implements Runnable {
    int intialPort;
    //NOTE: control the connections to the ports in the server class.
    // this why you have the void "run" method.
    //  String id;
    public Server(int p) {//Server takes a port only
      System.out.println("The Server was created and was assigned to port: "+p);
      this.intialPort = p;
    }
    /*****************************//**
    * \class Message class "chat.java"
    * \brief JOIN: id, port
    **********************************/
    public void joinAnotherServer(int port) {
      // take any number not currently assigned to me, and assign it to my successor.
      System.out.println("Join Another Sever Method called");
      // I want to wrap this in a try/catch block.
      if(this.succPort == port) {
        System.out.println("Already, connected, can not connect to self again.");
      } else{
        this.succPort = port;
      }
      // what if this port doesn't exist?
      // answer: throw an error. and don't change.
      // what if I'm already assigned to one?
      // answer: reassign me.
      // in theory all my connection is a record of what port i'm suppose to
      // be connected too. s
    }
    /*****************************//**
    * \class Message class "chat.java"
    * \brief ACCEPT: Id_pred, Port_pred, IP_pred
    **********************************/
    public void acceptAnotherServer(int port) {
      // take any number not currently assigned to me, and assign it to my successor.
      System.out.println("Accept Another Sever Method called");
      // I want to wrap this in a try/catch block.
      if(this.predPort == port) {
        System.out.println("Already, connected, can not connect to self again.");
      } else{
        this.predPort = port;
      }
    }
    public void sendMsgToNode(Message m, int toPort){
      try{
        System.out.println("[Send MSG] Sending message to port:" +toPort);
        Socket socket = new Socket("localhost", toPort);
        System.out.println("[Client] Just connected to " + socket.getRemoteSocketAddress());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(m);
      } catch(SocketException e) {
        System.out.println("[Send MSG] Socket: " + e.getMessage());
      } catch(IOException e) {
        System.out.println("[Send MSG] IO: " + e.getMessage());
        e.printStackTrace();
      }
    }
    public void run(){
      /*****************************//**
      * \brief It allows the system to interact with the participants.
      **********************************/
      // This is from the abstract class.
      try {
        ServerSocket servSock = new ServerSocket(intialPort);// create the server off port
        System.out.println("Waiting for client on port " + servSock.getLocalPort() + "...");
        while(true) {
          Socket clntSock = servSock.accept(); // .accept() returns a socket object
          //System.out.println("The Server Run method was called In while loop.");
          System.out.println("[Server] Just connected to " + clntSock.getRemoteSocketAddress());
          ObjectInputStream  ois = new ObjectInputStream(clntSock.getInputStream());
          ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());
          try{
            Message m = (Message)ois.readObject();// not sure what's going on here (magic).
            //System.out.println("Please enter a port to connect to: ");
            ///PUT
            if(m.msgid == enum_MSG.PUT){//if message is PUT
              if(m.portDest == intialPort){//AND its meant for me
                System.out.println(m.text + " portSrc:" + m.portSrc);
              } else if(m.portSrc == intialPort){
                System.out.println("User not availible");
              } else{
                sendMsgToNode(m, succPort);
              }
            }
            ///JOIN
            if(m.msgid == enum_MSG.JOIN) {//if message is JOIN

              if(m.fromInput == true) {

                m.fromInput = false;
                // reading the contents of the message and updating the succPort
                succPort = m.portDest;

                sendMsgToNode(m, m.portDest);// i don't think this should be here.

              } else {//from someone else
                predPort = m.portSrc;
                //printRoutingTable();
              }
            }
            System.out.println(predPort + "--->" + "[" + intialPort + "] " + "--->" + succPort);
            //  clntSock.close();
          } catch(ClassNotFoundException e) {
            System.out.println("[Server] IO Class: " + e.getMessage());
          }
          //printRoutingTable();
        }
      } catch(SocketException e) {
        // Handle Messages
        //clntSock.close();
        System.out.println("[Server] Socket: " + e.getMessage());
      } catch(IOException e) {
        System.out.println("[Server] IO: " + e.getMessage());
      }
    }
  }
  /*****************************//*
  * \brief It implements the client
  **********************************/
  private class Client implements Runnable {
    String id;
    int port;
    public Client(String id, int p) {
      System.out.println("The Client was created on port: "+p+" with Id: "+id);
      this.port = p;
      this.id = id;
    }

    /*****************************//**
    * \brief It allows the user to interact with the system.
    **********************************/
    public void run() {
      //System.out.println("The Client-run-Method was called.");
      while(true){
        try{
          Socket socket = new Socket(id, port);
          System.out.println("[Client] Just connected to " + socket.getRemoteSocketAddress());
          ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
          ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
          //System.out.println("Enter join <port> to join a node");
          System.out.println("Enter put <message> to chat to node("+port +")");
          String input = System.console().readLine();

          //split input by spaces
          List<String> list = new ArrayList<String>();
          for (String s : input.split("\\s")) {
            list.add(s);
          }
          list.toArray();
          list.forEach((temp) -> {
            System.out.println(temp);
          });
          if(list.contains("put")) { //send msg from clint(input) to Node server
            Message m = new Message();
            m.text = list.get(2);
            m.msgid = enum_MSG.PUT;
            m.portDest = Integer.parseInt(list.get(1));
            m.portSrc = port;
            oos.writeObject(m);//send msg to my node
          }
          if(list.contains("join")) {
            System.out.println("joining!");
            Message m = new Message();
            m.fromInput = true;
            m.msgid = enum_MSG.JOIN;
            m.portDest = Integer.parseInt(list.get(1));
            m.portSrc = port;
            oos.writeObject(m);//send msg to my node
          } else {
            Message m = new Message();
            m.fromInput = true;
            m.msgid = enum_MSG.ACCEPT;
            oos.writeObject(m);
          }
        } catch(SocketException e) {
          System.out.println("[Client] Socket: " + e.getMessage());
        } catch(IOException e) {
          System.out.println("[Client] IO: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }
  /* NOTE:
   * This is the first method that gets called when the main method is called.
   * The "localhost" and the "8000" or any string : number combination will give the
   * number of the port.
   */
  public Chat(String idThing, int port) {// for example: localhost 8000
    /* NOTE:
     * The chat method is like a "main" method sort of.
     * It get's passed the localhost and 8000, but it passes those to the other client and
     * server classe which are seperate threads. If you remmber from CECS 326 threads are all
     * that make up a server. A single thread can listen one port.
     */
    System.out.println("The Chat Method was called, port: "+port+" id: "+idThing);
    // Initialization of the peer
    // on seperate threads

    // On instanitate of this class make sure port this node points to itself.
    this.predPort = port;
    this.succPort = port;
    Thread server = new Thread(new Server(port));// 4200
    Thread client = new Thread(new Client(idThing, port)); // Localhost, 4200
    server.start();
    client.start();
    try {
      client.join();// not our server join methods.
      server.join();
    } catch (InterruptedException e){
      System.out.println("Thread: " + e.getMessage());
    }
  }
  /*****************************//**
  * Starts the threads with the client and server:
  * \param Id unique identifier of the process
  * \param port where the server will listen
  **********************************/
  public static void main(String[] args) {
    System.out.println("The main thread was hit.");
    if (args.length < 2 ) {
      throw new IllegalArgumentException("Parameter: <id> <port>");
    }
    Chat chat = new Chat(args[0], Integer.parseInt(args[1]));
  }
}
