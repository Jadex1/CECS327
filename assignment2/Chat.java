import java.net.*;
import java.io.*;
import java.util.*;

/*****************************//**
* \brief It implements a distributed chat.
* It creates a ring and delivers messages
* using flooding
**********************************/
public class Chat  implements Serializable {
  public enum enum_MSG {
      JOIN, //0
      ACCEPT,   //1
      LEAVE,   //2
      PUT      //3
   };




public class Message implements Serializable  {
  public enum_MSG msgid;
  public int port;
  public String text;
  /*
  public Message(String text){
      this.text = text;
  }*/
//    JOIN    : Id, Port
     void join(int port,String id){
       this.msgid = enum_MSG.JOIN;
       this.port = port;
     }

     /*
     ACCEPT  : Id_pred, Port_pred, IP_pred
     LEAVE   : Id_pred, Port_pred, IP_pred
     PUT     : idSender, idDest, payload
     */
}

/*****************************//**
* \class Server class "chat.java"
* \brief It implements the server
**********************************/
private class Server implements Runnable
{
    int port;
    public Server(int p)//Server takes a port only
    {
       this.port = p;
    }
/*****************************//**
* \brief It allows the system to interact with the participants.
**********************************/
    public void run() {
      try{
        ServerSocket servSock = new ServerSocket(port);
        System.out.println("Waiting for client on port " + servSock.getLocalPort() + "...");

        while (true)
        {
            Socket clntSock = servSock.accept(); // Get client connections
            System.out.println("[Server] Just connected to " + clntSock.getRemoteSocketAddress());
            ObjectInputStream  ois = new ObjectInputStream(clntSock.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());
            try{
              Message m = (Message)ois.readObject();
              System.out.println("[Server]: " + m.text);
            }
            catch (ClassNotFoundException e){
              System.out.println("[Server] IO Class: " + e.getMessage());
            }
            // Handle Messages
            //clntSock.close();
            }

      }
      catch (SocketException e){
        System.out.println("[Server] Socket: " + e.getMessage());
      }
      catch (IOException e){
        System.out.println("[Server] IO: " + e.getMessage());
      }
    }
  }

/*****************************//*
* \brief It implements the client
**********************************/
  private class Client implements Runnable
  {
    String id;
    int port;

    public Client(String id, int p)
    {
       this.port = p;
       this.id = id;
    }

  /*****************************//**
* \brief It allows the user to interact with the system.
**********************************/
    public void run()
    {


      while (true)
      {
          // Read commands form the keyboard
          //Prepare message m
          try{
            Socket socket = new Socket(id, port);
          System.out.println("[Client] Just connected to " + socket.getRemoteSocketAddress());
          ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
          ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

          Message m = new Message();
          String input = System.console().readLine();
          port = Integer.parseInt(input);
          m.text = Integer.toString(port);
          oos.writeObject(m);
          ois.read();
          socket.close();

        }
        catch (SocketException e){
          System.out.println("[Client] Socket: " + e.getMessage());
        }
        catch (IOException e){
          System.out.println("[Client] IO: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }


  public Chat(String Id, int port) {

      // Initialization of the peer
      Thread server = new Thread(new Server(port));
      Thread client = new Thread(new Client(Id, port));
      server.start();
      client.start();
      try{
        client.join();
        server.join();
      }
      catch (InterruptedException e){
        System.out.println("Thread: " + e.getMessage());
      }
  }

/*****************************//**
* Starts the threads with the client and server:
* \param Id unique identifier of the process
* \param port where the server will listen
**********************************/
public static void main(String[] args) {

      if (args.length < 2 ) {
          throw new IllegalArgumentException("Parameter: <id> <port>");
      }
      Chat chat = new Chat(args[0], Integer.parseInt(args[1]));
    }
}
