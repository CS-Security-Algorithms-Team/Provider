import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;


public class ProviderServer implements Runnable {
   Socket sock= null;

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
		 if(request == null) {
			sock.getOutputStream().write("Request failed, inadequate arguments".getBytes());
			return;
		 }
         System.out.println(request);
		 
		 String query= null;
		 try {
		 	query = makeArticleQuery(Integer.parseInt(request));
		 }
		 catch(NumberFormatException e){
		 			query = makeContentQuery(request);
		}
         System.out.println(query);

         Class.forName("com.mysql.jdbc.Driver").newInstance();
         Connection con = DriverManager.getConnection("jdbc:mysql://cs.okstate.edu:3306/"+user, user, pass);

         ResultSet rs = con.createStatement().executeQuery(query);
		 
		 try {
			Integer.parseInt(request);
			if(rs.next()==true) {
				 String articlePath = rs.getString("articlePath").trim();
				 FileInputStream fis = new FileInputStream(new File(articlePath));
				 byte[] buffer = new byte[1024];
				 int bytes=-1;
				 while((bytes=fis.read(buffer)) != -1) {
					System.out.println(new String(buffer));
					sock.getOutputStream().write(buffer);
				 }
			} else {
				sock.getOutputStream().write("Cannot retrieve file".getBytes());
			}
		}
		 catch(NumberFormatException e){
			 while(rs.next()) {
				int articleId = rs.getInt("articleId");
				String articlePath = rs.getString("articlePath").trim();
				System.out.println(articleId + "\t" + articlePath);
				String fileName = (new File(articlePath)).getName();
				if(fileName.contains(".")) {
					fileName = fileName.substring(0, fileName.indexOf("."));
				}
				sock.getOutputStream().write((articleId + ": " + fileName +"\n").getBytes());
			}
		} 
         con.close();        
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

   private String makeArticleQuery(int request) {
      int articleId = request; 
      String query = "SELECT articlePath FROM content WHERE articleId = " + articleId;
      return query;
   }
   
   private String makeContentQuery(String request) {
      String query = "SELECT * FROM content";
      return query;
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
