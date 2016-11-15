import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class MD5{
  public static BigInteger getMD5(String input){
    try{
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] messageDigest = md.digest(input.getBytes());
      BigInteger number = new BigInteger(1, messageDigest);
      BigInteger aMod = new BigInteger("32768");
      return number.mod(aMod);
    } catch(Exception e){// generic Exception
      throw new RuntimeException(e);
    }
  }
  public static void main(String[] args) throws NoSuchAlgorithmException {
    System.out.println((getMD5(args[0])));
  }
}
