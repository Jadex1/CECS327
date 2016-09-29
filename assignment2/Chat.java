import java.net.*;
import java.io.*;
import java.util.*;
 /*****************************//**
 * \brief It implements a distributed chat.
  * It creates a ring and delivers messages
  * using flooding
  **********************************/
public class Chat implements Serializable {
  /*
   * Routing table
   */
  int pred;
  int succ;
  public enum enum_MSG {
    JOIN,     // 0
    ACCEPT,   // 1
    LEAVE,    // 2
    PUT,      // 3
  };

  void join(String id, int port) {
    System.out.println("The messagee join method was called.");
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
    // this why you have the void "run" method.
    // String id;
    int port;
    public Server(int p) {//Server takes a port only
      //set the r
      System.out.println("The Server method was called and was assigned to port: "+p);
      pred = p;// this instances local variable.
      port = p;
      //this.id = id;
    }
    public void sendMsgToNode(Message m, int toPort){
      try{
          System.out.println("[Send MSG] Sending message to port:" +toPort);
          Socket socket = new Socket("localhost", toPort);
          System.out.println("[Client] Just connected to " + socket.getRemoteSocketAddress());
          ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
          oos.writeObject(m);
        }
        catch(SocketException e) {
          System.out.println("[Send MSG] Socket: " + e.getMessage());
        }
        catch(IOException e) {
          System.out.println("[Send MSG] IO: " + e.getMessage());
          e.printStackTrace();
        }
    }

    public void run(){
      //System.out.println("The Server Run method was called.");
      /*****************************//**
      * \brief It allows the system to interact with the participants.
      **********************************/
      // This is from the abstract class.
      try {
        ServerSocket servSock = new ServerSocket(port);// create the server off port
        System.out.println("Waiting for client on port " + servSock.getLocalPort() + "...");
        while(true) {
          Socket clntSock = servSock.accept(); // .accept() returns a socket object
          //System.out.println("The Server Run method was called In while loop.");
          System.out.println("[Server] Just connected to " + clntSock.getRemoteSocketAddress());
          ObjectInputStream  ois = new ObjectInputStream(clntSock.getInputStream());
          ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());
          try{
            Message m = (Message)ois.readObject();// not sure what's going on here (magic).
            ///PUT
            if(m.msgid == enum_MSG.PUT){//if message is PUT
              if(m.portDest == port){//AND its meant for me
                System.out.println(m.text + " portSrc:" + m.portSrc);
              } else if(m.portSrc == port){
                System.out.println("User not available");
              } else{
                sendMsgToNode(m,succ);
              }
            }
            ///JOIN
            //NOTE: James works on Join
            //NOTE: James works on Leave too.
            if(m.msgid == enum_MSG.JOIN){//if message is JOIN
              if(m.fromInput == true) {
                m.fromInput = false;
                succ = m.portDest;
                sendMsgToNode(m,m.portDest);
              } else{//from someone else
                pred = m.portSrc;
                //printRoutingTable();
              }
            }
            System.out.println(pred + "--->" + "[" + port + "] " + "--->" + succ);
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
      System.out.println("The Client was created on port:"+p+"with Id: "+id);
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

          System.out.println("Enter put <message> to chat to node("+port +")");
          String input = System.console().readLine();

          //split input by spaces
          List<String> list = new ArrayList<String>();
          for (String s : input.split("\\s"))
          {
            list.add(s);
          }
          list.toArray();

          list.forEach((temp) -> {
            System.out.println(temp);
          });
          if(list.contains("put")){//send msg from clint(input) to Node server
            Message m = new Message();
            m.text = list.get(2);
            m.msgid = enum_MSG.PUT;
            m.portDest = Integer.parseInt(list.get(1));
            m.portSrc = port;
            oos.writeObject(m);//send msg to my node
          }
          if(list.contains("join")){
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
    this.pred = port;
    this.succ = port;

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
