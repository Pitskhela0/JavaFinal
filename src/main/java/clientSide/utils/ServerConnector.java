package clientSide.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

// established communication between server and client for heartbeat
// checks connection break with server
public class ServerConnector {
    private Socket heartbeatSocket;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private final int HEARTBEAT = 500;

    public Socket getHeartbeatSocket() {
        return heartbeatSocket;
    }

    public AtomicBoolean getIsConnected() {
        return isConnected;
    }

    public ServerConnector(Socket gameSocket) throws IOException {
        try{
            Scanner scanner = new Scanner(gameSocket.getInputStream());
            String message = scanner.nextLine(); // should get heartbeat socket port
            int heartbeatPort = Integer.parseInt(message.split(":")[1]);
            heartbeatSocket = new Socket("localhost",heartbeatPort);
            isConnected.set(true);
        }
        catch (IOException e){
            throw new IOException("Error during establishing connection, in connector socket");
        }

    }

    public void handleHeartbeat() throws IOException {
        OutputStream heartbeatOut = heartbeatSocket.getOutputStream();
        PrintWriter heartbeatPrinter = new PrintWriter(heartbeatOut, true);

        System.out.println("connect to heartbeat");

        // start the heartbeat
        Thread.startVirtualThread(() -> heartbeatHandler(heartbeatPrinter));
        System.out.println("heartbeat started");
    }

    private void heartbeatHandler(PrintWriter heartbeatWriter) {
        while (isConnected.get()){
            heartbeatWriter.println("HEARTBEAT");
            try {
                Thread.sleep(HEARTBEAT);
            } catch (InterruptedException e) {
                System.out.println("Heartbeat thread stopping...");
                break;
            }
            catch (Exception e){
                System.out.println("Heartbeat error: " + e.getMessage());
                break;
            }
        }
        isConnected.set(false);
        heartbeatWriter.close();
        try {
            heartbeatSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serverConnectionListener() {
        InputStream inputStream = null;
        try {
            inputStream = heartbeatSocket.getInputStream();
            Scanner heartbeatScanner = new Scanner(inputStream);
            Thread.startVirtualThread(() -> {
                System.out.println("started listening about connection");
                while (true){
                    if(heartbeatScanner.hasNextLine()){
                        System.out.println("Game ended-heartbeat");
                        if(heartbeatScanner.nextLine().equals("GAME_END")){
                            isConnected.set(false);
                            break;
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
