import java.net.*;
import java.io.*;
import java.util.*;
/*****************************//**
* \brief It implements a distributed chat.
* It creates a ring and delivers messages
* using flooding
**********************************/
public class Chat   {
  public enum enum_MSG {
    JOIN, //0
    ACCEPT,   //1
    LEAVE,   //2
    PUT      //3
  };
  // General Message class
  public class Message implements Serializable  {
    enum_MSG messageID;     // Id of the message
    public MainMessage() {
    /*
    JOIN    : Id, Port
    ACCEPT  : Id_pred, Port_pred, IP_pred
    LEAVE   : Id_pred, Port_pred, IP_pred
    PUT     : idSender, idDest, payload
    */
   }
  }
  /***********************************
  * \class Server class "chat.java"
  * * \brief It implements the server
  * **********************************/
  private class Server implements Runnable {
    String id;
    int port;
    public Server(String id, int p) {
       this.port = p;
       this.id = id;
    }
    /*****************************//**
    * \brief It allows the system to interact with the participants.
    **********************************/
    public void run() {
      ServerSocket servSock = new ServerSocket(port);
      while (true) {
        Socket clntSock = servSock.accept(); // Get client connections
        ObjectInputStream  ois = new
        ObjectInputStream(clntSock.getInputStream());
        ObjectOutputStream oos = new
        ObjectOutputStream(clntSock.getOutputStream());
        MainMessage m = (MainMessage)ois.readObject();
        // Handle Messages
        clntSock.close();
      }
    }
  }
  /***********************************
  *
  * \class Client class "chat.java"
  /***********************************
  * \brief It implements the client
  * **********************************/
  private class Client implements Runnable {
    String id;
    int port;
    MainMessage m;
    public Client(String id, int p) {
      this.port = p;
      this.ip = id;
    }
    /*****************************//**
    * \brief It allows the user to interact with the system.
    * **********************************/
    public void run() {
      while (true) {
        // Read commands form the keyboard
        //Prepare message m
        Socket socket = new Socket(ip, port);
        ObjectOutputStream oos = new
        ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new
        ObjectInputStream(socket.getInputStream());
        oos.writeObject(m);
        ois.read();
        socket.close();
      }
    }
  }
  /*****************************//**
  * Starts the threads with the client and server:
  * \param Id unique identifier of the process
  * \param port where the server will listen
  **********************************/
  public Chat(String Id, int port) {
    // Initialization of the peer
    Thread server = new Thread(new Server(port));
    Thread client = new Thread(new Client(Id, port));
    server.start();
    client.start();
    client.join();
    server.join();
  }
  public static void main(String[] args) {
    if (args.length < 2 ) {
      throw new IllegalArgumentException("Parameter: <id> <port>");
    }
    Chat chat = new Chat(args[0], Integer.parseInt(args[1]));
  }
}
