import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;


public class ThreadClient {

	static String ticketForServer;
    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        
    	final String HOST = "127.0.0.1";
    	final String SERVERHOST ="127.0.0.2";
        final int PORT = 5050;
        final int SERVERPORT = 4040;
        
        Scanner scan=new Scanner(System.in);

        SecureRandom srandom = new SecureRandom();
        byte[] iv = new byte[128 / 8];
        srandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        String ivFile = "initial_vector";
        try (FileOutputStream out1 = new FileOutputStream(ivFile)) {
            out1.write(iv);
        }

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecretKey skey = kgen.generateKey();

        String keyFile = "keyfile";
        try (FileOutputStream out1 = new FileOutputStream(keyFile)) {
            byte[] keyb = skey.getEncoded();
            out1.write(keyb);
        }

        Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);


        System.out.println("Enter your username and password ");
        String p = scan.nextLine();
        scan.nextLine();

        byte[] array = p.getBytes(StandardCharsets.UTF_8);
        String outFile = "outputAES";
        try (FileOutputStream out1 = new FileOutputStream(outFile)) {
            byte[] input = array;
            byte[] encoded = ci.doFinal(input);
            out1.write(encoded);
        }

        System.out.println("Client for ticketServer started.");
        try (
                Socket socket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner in = new Scanner(socket.getInputStream());
        ) {
            while (true) {
            try {

            	ticketForServer=in.nextLine();
                if(ticketForServer.equals("not found")){
                    System.out.println("Your credentials are wrong ");
                    System.exit(1);
                }
                if(ticketForServer.equals("exists")){
                    System.out.println("You already have a connection to server ");
                    System.exit(1);
                }
            	System.out.println("Your Ticket is "+ ticketForServer );
            	break;

            }
            catch(Exception e){
                    }
                }
            }
        
        System.out.println("Client for real Server started.");



            Thread t = new Thread() {
            	public void run() {
            	try (
                Socket socket1 = new Socket(SERVERHOST,SERVERPORT);
                PrintWriter out = new PrintWriter(socket1.getOutputStream(), true);
                Scanner in1 = new Scanner(socket1.getInputStream());
                Scanner s1 = new Scanner(System.in);
        ) {
                    System.out.println("Enter your ticket for Server" );
                    String input = scan.nextLine();
                    out.println(input);
        	while (true) {
                System.out.println("Echoed from server: " +in1.nextLine());
                input=scan.nextLine();
                out.println(input);
                if (input.equalsIgnoreCase("exit") && input.equals("Ticket not found Access to Server DENIED")){
                    System.out.println("Exiting server");
                    break;
                }

            }
        	}
            catch(IOException e){
            	System.out.print("exception");
            }
            	}
            };
            t.start();
        
        }
    }