import java.math.BigInteger;
import java.io.*;
import java.util.*;

public class Transaction implements Serializable  {

  public enum Operation {
    WRITE, READ, DELETE
  }
  Integer id;
  Integer time;
  Integer guid;
  Operation op;
  Boolean vote;
  FileStream fileStream;
  public Transaction(Operation op, Integer id, Boolean vote, FileStream stream)
  {
    this.op = op;
    this.id = id;
    this.vote = vote;
    this.time = (int)(new Date().getTime()/1000);
    this.fileStream = stream;
  }
}
