import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;


public class ProviderServer implements Runnable {
   Socket sock= null;
   String articleId;
   String ARTICLE_QUERY = "SELECT articlePath FROM content WHERE articleId = "+articleId;
   String user;
   String pass;

   public ProviderServer(Socket _sock, String _user, String _pass) {
      sock = _sock;
	  user=_user;
	  pass=_pass;
   }

   public void run() {
      try {
         BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
         String request = in.readLine();
         System.out.println(request);
         String query = makeQuery(request);
         System.out.println(query);

         Class.forName("com.mysql.jdbc.Driver").newInstance();
         Connection con = DriverManager.getConnection("jdbc:mysql://cs.okstate.edu:3306/"+user, user, pass);

         ResultSet rs = con.createStatement().executeQuery(query);
         rs.next();
         String _articlePath = rs.getString("articlePath").trim();
         System.out.println(articleId + "\t" + _articlePath);

         con.close();

         FileInputStream fis = new FileInputStream(new File(_articlePath));
         byte[] buffer = new byte[1024];
         int bytes=-1;
         while((bytes=fis.read(buffer)) != -1) {
            System.out.println(new String(buffer));
            sock.getOutputStream().write(buffer);
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            sock.close();
         } catch (IOException e1) {
            e1.printStackTrace();
            sock=null;
         }
      }
   }

   private String makeQuery(String request) {
      //String[] fields = request.split(",");
      articleId = request; //fields[0];
      ARTICLE_QUERY = "SELECT articlePath FROM content WHERE articleId = " + articleId;
      return ARTICLE_QUERY;
   }

   public static void main(String[] args) {
      ServerSocket ss = null;

      try {
         ss = new ServerSocket(8081);
      }
      catch (IOException e) {
         e.printStackTrace();
         System.out.println("Unable to create Provider Server");
         return;
      }

      while(true) {
         try {
            Thread t = new Thread(new ProviderServer(ss.accept(), args[0], args[1]));
            t.start();
         } catch (IOException e) {
            System.out.println("Unable to accept new connection.");
            try {
               ss.close();
            } catch (IOException e1) {
               e1.printStackTrace();
               ss=null;
            }
         } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
		    System.out.println("java ProviderServer <db username> <password>");
		 }
      }
   }
}
