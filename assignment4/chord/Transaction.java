import java.math.BigInteger;
import java.io.*;

public class Transaction implements Serializable  {

  public enum Operation { WRITE, DELETE}//Need read here?
  Integer TransactionId;
  Integer guid;
  Operation op;
  Boolean vote;
  FileStream fileStream;
  public Transaction(Operation op,Integer id,Boolean vote,FileStream stream)
  {
    this.op = op;
    this.TransactionId = id;
    this.vote = vote;
    this.fileStream = stream;
  }
}
