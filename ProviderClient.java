import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ProviderClient {

   public static void main(String[] args) {
      try {
         Socket sock = new Socket("localhost", 8081);

         PrintWriter out = new PrintWriter(sock.getOutputStream());
         out.println(args[0]);
         out.flush();
         BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

         String line=null;
         while((line =in.readLine()) != null) {
            System.out.println(line);
         }
         sock.close();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
         System.out.println("java ProviderClient <articleId>");
      }


   }

}
