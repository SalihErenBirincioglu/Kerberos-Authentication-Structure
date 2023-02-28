import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


public class TicketServer {
    static ArrayList<String> ticketGivenUsers = new ArrayList();

    public static void main(String[] args) throws IOException {
        final int PORT = 5050;
        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("Ticket server started ...");
        System.out.println("Waiting for clients ...");
            while(true) {

                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread() {
                    public void run() {
                        try (
                                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                                Scanner in = new Scanner(clientSocket.getInputStream());
                        ) {
                            Boolean found=false;
                            Boolean alreadyExists=false;
                            String ivFile = "initial_vector";
                            byte[] iv = Files.readAllBytes(Paths.get(ivFile));
                            IvParameterSpec ivspec = new IvParameterSpec(iv);
                            String keyFile = "keyfile";
                            byte[] keyb = Files.readAllBytes(Paths.get(keyFile));
                            SecretKeySpec skey = new SecretKeySpec(keyb, "AES");

                            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            ci.init(Cipher.DECRYPT_MODE, skey, ivspec);

                            String inFile = "outputAES";
                            byte[] encoded = Files.readAllBytes(Paths.get(inFile));
                            String plainText = new String(ci.doFinal(encoded), "UTF-8");
                            int size= ticketGivenUsers.size();

                            for(int iter=0;iter<size;iter++){
                                System.out.println("user " +ticketGivenUsers.get(iter));
                                if(ticketGivenUsers.get(iter).contains(plainText.trim())){
                                    alreadyExists=true;
                                }
                            }
                            String Temp = " ";
                            try {
                            	Scanner scn=new Scanner(new File("Users.txt"));
                            	scn.useDelimiter("[\n]");
                            	while(scn.hasNext() && !found) {
                                    if(alreadyExists){
                                        break;
                                    }
                            		Temp=scn.next();
                            	
                            		if(Temp.trim().equals(plainText)) {
                            			found=true;
                                        ticketGivenUsers.add(Temp);
                            			break;
                            		}
                            	}
                            }
                            catch(Exception e) {
                            }
                            
                            if(found==true) {
                                ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);
                                int min = 100000;
                                int max = 1000000000;
                                  
                           
                                System.out.println("Random ticket in int from "+min+" to "+max+ ":");
                                int ticketGenerated = (int)Math.floor(Math.random()*(max-min+1)+min);
                                System.out.println(ticketGenerated);
                                String strTicket=Integer.toString(ticketGenerated);
                                	
                                byte[] array = strTicket.getBytes(StandardCharsets.UTF_8);
                                String outFile = "TicketFile.txt";
                                try (FileOutputStream out1 = new FileOutputStream(outFile)) {
                                    byte[] input = array;
                                    byte[] encoded2 = ci.doFinal(input);
                                    out1.write(encoded2);
                            
                            }
                             out.println(strTicket);   
                            }
                            if(!found && !alreadyExists){
                                System.out.println("User not found ");
                                String ret="not found";
                                out.println(ret);
                            }
                            if(alreadyExists){
                                System.out.println("User already exists");
                                String ret="exists";
                                out.println(ret);
                            }
                            
                        } catch (Exception e) {
                        }

                    }

                };
                t.start();
            }
    }
}