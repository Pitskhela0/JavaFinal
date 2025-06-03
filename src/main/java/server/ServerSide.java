package server;


import server.server_logic.Player;
import server.server_logic.Spectator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerSide {
    private static List<Player> players;
    private static List<Spectator> spectators;

    private static AtomicBoolean whiteTurn = new AtomicBoolean(false);


    public static void main(String[] args) {
        try {
            // initialize players and spectators with thread-safe lists
            players = Collections.synchronizedList(new ArrayList<>());
            spectators = Collections.synchronizedList(new ArrayList<>());

            ServerSocket serverSocket = new ServerSocket(10002);
            System.out.println("Server running on port: "+serverSocket.getLocalPort());

            while (true){
                    try {
                        Socket socket = serverSocket.accept();
                        new Thread(() -> {
                            try {
                                clientHandler(socket);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to connect server");
        }
    }


    private static void clientHandler(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream,true);

        printWriter.println(LocalTime.now());

        InputStream inputStream = socket.getInputStream();
        Scanner scanner = new Scanner(inputStream);

        assignRole(scanner, printWriter);

        // listening to client after assigning role
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();

            if(line.equals("bye")){
                break;
            }
            printWriter.println(line);
            System.out.println(line);
        }
        printWriter.close();
        scanner.close();
    }



    private static void assignRole(Scanner scanner, PrintWriter printWriter) {
        boolean roleAssigned = false;

        while (!roleAssigned){
            String line = scanner.nextLine();
            System.out.println(players.size());
            if(line.equals("player") && players.size() < 2){
                if(players.isEmpty()){
                    // assign white player
                    players.add(new Player());
//                    printWriter.println("white");
                }
                else {
                    // assign black player
                    players.add(new Player());
//                    printWriter.println("black");
                }
                printWriter.println("ok");
                roleAssigned = true;
                line = scanner.nextLine();
                System.out.println("player is assigned");
            }
            else if(line.equals("spectator")){
                spectators.add(new Spectator());
                roleAssigned = true;
                printWriter.println("ok");
                line = scanner.nextLine();
            }
            else {
                printWriter.println("not okay");
                System.out.println("could not find any role in serverside for: "+line);
            }
        }
    }
}
