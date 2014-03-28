import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

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
                String path = "/home/doocroot" + params[1];
                String typeParse[] = path.split("\\.");
                File file = new File(path);
                writeResponse("");
            } catch (Throwable t) {
                System.out.println(t.getMessage());
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.err.println("Client processing finished");
        }

        private void writeResponse(String s) throws Throwable {
            File file = new File("/home/velikolepnii/docroot/slon.jpg");
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: Karuna\r\n" +
                    "Content-Type: image/jpeg\r\n" +
                    "Content-Length: " + file.length() + "\r\n" +
                    "muuuu: KOpa\r\n" +
                    "Connection: close\r\n\r\n";
            String result = response + s;
            byte[] byteArray = new byte[52428800];

            FileInputStream fi = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fi);
            while((bis.read(byteArray)) != -1) {}
            bis.close();
            os.write(response.getBytes());
            os.write(byteArray);
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
            return parameters;
        }
    }
}