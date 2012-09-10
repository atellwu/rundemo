import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TODO Comment of Demo
 * 
 * @author wukezhu
 * @rundemo_name 输入输出的简单demo
 */
public class Demo {

   /**
    * @param args
    * @throws InterruptedException
    * @throws IOException
    */
   public static void main(String[] args) throws IOException {
      String CurLine = ""; // Line read from standard in

      System.out.println("Enter a line of text (type 'quit' to exit): ");
      InputStreamReader converter = new InputStreamReader(System.in);

      BufferedReader in = new BufferedReader(converter);

      while (!(CurLine.equals("quit"))) {
         CurLine = in.readLine();

         if (!(CurLine.equals("quit"))) {
            System.out.println("You typed: " + CurLine);
         }
      }
   }
}
