import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by yar 09.09.2009
 */
public class helloworld {

    public static void main(String[] args) throws Throwable {
        ServerSocket ss = new ServerSocket(8080);
        while (true) {
            Socket s = ss.accept();
            System.err.println("Client accepted");
            new Thread(new SocketProcessor(s)).start();
        }
    }

    private static class SocketProcessor implements Runnable {

        private Socket s;
        private InputStream is;
        private OutputStream os;

        private SocketProcessor(Socket s) throws Throwable {
            this.s = s;
            this.is = s.getInputStream();
            this.os = s.getOutputStream();
        }

        public void run() {
            try {
                String params[] = readInputHeaders();
                String path = "/home/velikolepnii/docroot";
                String fileName = params[1];
                if (fileName.contains("?")) fileName = fileName.substring(0, fileName.indexOf("?"));
                if (fileName.endsWith("/")) fileName += "index.html";

                if (!params[0].equals("GET") && !params[0].equals("HEAD")){
                    writeResponse(new byte[0], 0, "text/html", 405, params[0]);
                } if (fileName.contains("../")){
                    writeResponse(new byte[0], 0, "text/html", 403, params[0]);
                }

                File file = new File(path + fileName);
                if (fileName.endsWith("index.html") && !file.exists()) {
                    writeResponse(new byte[0], 0, "text/html", 403, params[0]);
                } else {
                if (!file.exists()) {
                    writeResponse(new byte[0], 0, "text/html", 404, params[0]);
                } else {
                    Path pathVar = Paths.get(path + fileName);
                    //check if exists!
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    String mimeType = Files.probeContentType(pathVar);
                    if (mimeType.equals("application/vnd.adobe.flash.movie")) mimeType = "application/x-shockwave-flash";
                    byte[] byteArray = new byte[52428800]; //50 M
                    while((bis.read(byteArray)) != -1) {}
                    bis.close();
                    writeResponse(byteArray, file.length(), mimeType, 200, params[0]);
                }
            }

            } catch (Throwable t) {
                System.out.println(t.getMessage());
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                }
            }
            System.err.println("Client processing finished");
        }

        String getServerTime() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.format(calendar.getTime());
        }

        private void writeResponse(byte[] byteArray, long length, String mimeType, int code, String method) throws Throwable {
            String date = getServerTime();
            String response = "HTTP/1.1 ";
            switch (code){
                case 200: response += "200 OK"; break;
                case 404: response += "404 Not Found"; break;
                case 405: response += "405 Method Not Allowed"; break;
                case 403: response += "403 Forbidden";
            }
            response += "\r\n" +
                    "Date: " + date + "\r\n" +
                    "Server: KarunaServ\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + length + "\r\n" +
                    "Connection: close\r\n\r\n";
            os.write(response.getBytes());
            if (length != 0 && !method.equals("HEAD")) os.write(byteArray);
            os.flush();
        }

        private String[] readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s = br.readLine();
            String[] parameters = s.split(" ");
            if (parameters.length > 3){
                parameters[0] = parameters[0];
                for(int i=2; i<parameters.length-1; i++){
                    parameters[1] += " " + parameters[i];
                }
            }
            for (int i = 0; i<parameters.length; i++){
                System.out.println(parameters[i]);
            }
            while(true) {
                s = br.readLine();
                System.out.println(s);
                if(s == null || s.trim().length() == 0) {
                    break;
                }
            }
            parameters[1] = URLDecoder.decode(parameters[1]);
            return parameters;
        }
    }
}