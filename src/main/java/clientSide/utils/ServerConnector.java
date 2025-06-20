package clientSide.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerConnector {
    private Socket heartbeatSocket;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private final int HEARTBEAT = 500;
    private PrintWriter heartbeatPrinter;
    private Thread heartbeatThread;
    private Thread listenerThread;

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
        heartbeatPrinter = new PrintWriter(heartbeatOut, true);

        System.out.println("connect to heartbeat");

        // start the heartbeat
        heartbeatThread = Thread.startVirtualThread(() -> heartbeatHandler(heartbeatPrinter));
        System.out.println("heartbeat started");
    }

    private void heartbeatHandler(PrintWriter heartbeatWriter) {
        while (isConnected.get()){
            try {
                heartbeatWriter.println("HEARTBEAT");
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

        System.out.println("Heartbeat thread ended");

        // Clean up heartbeat resources
        try {
            if (heartbeatWriter != null) {
                heartbeatWriter.close();
            }
            if (heartbeatSocket != null && !heartbeatSocket.isClosed()) {
                heartbeatSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing heartbeat resources: " + e.getMessage());
        }
    }

    public void serverConnectionListener() {
        try {
            InputStream inputStream = heartbeatSocket.getInputStream();
            Scanner heartbeatScanner = new Scanner(inputStream);

            listenerThread = Thread.startVirtualThread(() -> {
                System.out.println("started listening about connection");
                while (isConnected.get()){
                    try {
                        if(heartbeatScanner.hasNextLine()){
                            String message = heartbeatScanner.nextLine();
                            if(message.equals("GAME_END")){
                                System.out.println("Game ended-heartbeat");
                                isConnected.set(false);
                                break;
                            }
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Connection listener interrupted");
                        break;
                    } catch (Exception e) {
                        System.out.println("Connection listener error: " + e.getMessage());
                        break;
                    }
                }

                // Clean up scanner
                try {
                    heartbeatScanner.close();
                } catch (Exception e) {
                    System.out.println("Error closing heartbeat scanner: " + e.getMessage());
                }

                System.out.println("Connection listener ended");
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Add method to properly shutdown the connector
    public void shutdown() {
        System.out.println("Shutting down ServerConnector...");

        // Set connected to false to stop threads
        isConnected.set(false);

        try {
            // Interrupt threads if they exist
            if (heartbeatThread != null) {
                heartbeatThread.interrupt();
            }

            if (listenerThread != null) {
                listenerThread.interrupt();
            }

            // Close printer
            if (heartbeatPrinter != null) {
                heartbeatPrinter.close();
            }

            // Close socket
            if (heartbeatSocket != null && !heartbeatSocket.isClosed()) {
                heartbeatSocket.close();
            }

            System.out.println("ServerConnector shutdown complete");

        } catch (Exception e) {
            System.out.println("Error during ServerConnector shutdown: " + e.getMessage());
        }
    }
}