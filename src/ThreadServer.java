import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class ThreadServer {
    public static void main(String[] args) throws IOException {
        final int PORT = 4040;
        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("Server started...");
        System.out.println("Waiting for clients...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread t = new Thread() {
                public void run() {
                    try (
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                            Scanner in = new Scanner(clientSocket.getInputStream());
                    ) {
                    	 System.out.println("Client Detected ");
                    	 String ticket = in.nextLine();
                    	 System.out.println("ticket is  "+ ticket);

                         String ivFile = "initial_vector";
                         byte[] iv = Files.readAllBytes(Paths.get(ivFile));
                         IvParameterSpec ivspec = new IvParameterSpec(iv);
                         String keyFile = "keyfile";
                         byte[] keyb = Files.readAllBytes(Paths.get(keyFile));
                         SecretKeySpec skey = new SecretKeySpec(keyb, "AES");

                         Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
                         ci.init(Cipher.DECRYPT_MODE, skey, ivspec);
                         String inFile = "TicketFile.txt";
                         byte[] encoded = Files.readAllBytes(Paths.get(inFile));
                         String a=new String(encoded);
                         String encodedTicket = new String(ci.doFinal(encoded), "UTF-8");


                        if(ticket.trim().equals(encodedTicket.trim())) {
                            System.out.println("Client Connected ");
                            out.println("Connection Established");
                            while (in.hasNextLine()) {
                                String input = in.nextLine();
                                out.println(input);
                                if (input.equalsIgnoreCase("exit")) {
                                    break;
                                }
                            }
                    }
                        else {
                            System.out.println("Wrong Ticket Access Denied");
                        	out.println("Ticket not found Access to Server DENIED");
                        }
                    } catch (Exception e) {
                        System.out.println("catch exception");
                    }
                }
            };
            t.start();
        }
    }
}