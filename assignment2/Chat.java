import java.net.*;
import java.io.*;
import java.util.*;
 /*****************************//**
 * \brief It implements a distributed chat.
  * It creates a ring and delivers messages
  * using flooding
  **********************************/
public class Chat implements Serializable {
  public enum enum_MSG {
    JOIN,     // 0
    ACCEPT,   // 1
    LEAVE,    // 2
    PUT       // 3
  };
  public class Message implements Serializable {
    public enum_MSG msgid;
    public int port;
    public String text;
    /* NOTE:
     * Somehow the message needs to contain a list of all the variables and stuff.
     *
     */
    // public Message(String text){
    //     this.text = text;
    // }
    /*****************************//**
    * \class Message class "chat.java"
     * \brief JOIN: Id, Port
     **********************************/
    void join(String id, int port) {
      System.out.println("The messagee join method was called.");
      this.msgid = enum_MSG.JOIN;
      this.port = port;
    }
    /*****************************//**
    * \class Message class "chat.java"
    * \brief ACCEPT: Id_pred, Port_pred, IP_pred
    **********************************/
    void accept(String idPred, String portPred, int ipPred) {
      //TODO: Fill in here.
    }
    /*****************************//**
    * \class Message class "chat.java"
    * \brief LEAVE: Id_pred, Port_pred, IP_pred
    **********************************/
    void leave(String idPred, String portPred, int ipPred) {
      //TODO: Fill in here.
    }
    /*****************************//**
    * \class Message class "chat.java"
    * \brief PUT: idSender, idDest, payload
    **********************************/
    void put(int senderId, int destId, String someText) {
      //TODO: Fill in here.
    }
  }
  /*****************************//**
  * \class Server class "chat.java"
  * \brief It implements the server
  **********************************/
  private class Server implements Runnable {
    // this why you have the void "run" method.
  //  String id;
    int port;
    public Server(int p) {//Server takes a port only
      System.out.println("The Server method was called and was assigned to port: "+p);
      this.port = p;// this instances local variable.
      //this.id = id;
    }
    public void run(){
      System.out.println("The Server Run method was called.");
      /*****************************//**
      * \brief It allows the system to interact with the participants.
      **********************************/
      // This is from the abstract class.
      try {
        ServerSocket servSock = new ServerSocket(port);// create the server off port
        System.out.println("Waiting for client on port " + servSock.getLocalPort() + "...");
        while(true) {
          Socket clntSock = servSock.accept(); // .accept() returns a socket object
          System.out.println("The Server Run method was called In while loop.");
          System.out.println("[Server] Just connected to " + clntSock.getRemoteSocketAddress());
          ObjectInputStream  ois = new ObjectInputStream(clntSock.getInputStream());
          ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());
          try{
            Message m = (Message)ois.readObject();// not sure what's going on here.
            System.out.println("[Server]: " + m.text);//
          //  clntSock.close();
          } catch(ClassNotFoundException e) {
            System.out.println("[Server] IO Class: " + e.getMessage());
          }
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
      System.out.println("The Client was create on port:"+p+"with Id: "+id);
      this.port = p;
      this.id = id;
    }
    /*****************************//**
    * \brief It allows the user to interact with the system.
    **********************************/
    public void run() {
      System.out.println("The Client-run-Method was called.");
      while (true) {
        System.out.println("The Client Run method was called In while loop.");
        // Read commands form the keyboard
        //Prepare message m
        try {
          System.out.println("Enter a Port to connect to: ");
          String input = System.console().readLine();
          if(!input.toLowerCase().contains("put")){
            port = Integer.parseInt(input);
          }
          Socket socket = new Socket(id, port);
          System.out.println("[Client] Just connected to " + socket.getRemoteSocketAddress());
          ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
          ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
          Message m = new Message();
          m.text = input + " from:" + socket.getLocalPort();
          oos.writeObject(m);
          ois.read();
          socket.close();
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
  public Chat(String Id, int port) {// for example: localhost 8000
    /* NOTE:
     * The chat method is like a "main" method sort of.
     * It get's passed the localhost and 8000, but it passes those to the other client and
     * server classe which are seperate threads. If you remmber from CECS 326 threads are all
     * that make up a server. A single thread can listen one port.
     */
    System.out.println("The Chat Method was called");
    // Initialization of the peer
    // on seperate threads.
    Thread server = new Thread(new Server(port));// 8000
    Thread client = new Thread(new Client(Id, port)); // Localhost, 8000
    server.start();
    client.start();
    try {
      client.join();
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
