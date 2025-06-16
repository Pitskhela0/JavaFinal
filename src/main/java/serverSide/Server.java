package serverSide;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private static List<ClientHandler> spectators = Collections.synchronizedList(new ArrayList<>());
    private static List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>());
    private static Board board;

    public static Board getBoard() {
        return board;
    }

    private static boolean gameFinished = false;
    private static boolean gameStarted = false;
    private static ServerSocket serverSocket;

    public static void setGameStarted(boolean gameStarted) {
        Server.gameStarted = gameStarted;
    }

    public static boolean isGameStarted() {
        return gameStarted;
    }

    public static List<ClientHandler> getSpectators() {
        return spectators;
    }

    public static List<ClientHandler> getPlayers() {
        return players;
    }

    public static void main(String[] args) {
        serverSocket = null;
        try {if(!gameFinished)
                serverSocket = new ServerSocket(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (!gameFinished) {
            try {
                Socket socket = serverSocket.accept();

                ServerSocket heartbeatServerSocket = new ServerSocket(0);// 0 means that os will find avaliable random port

                int heartbeatPort = heartbeatServerSocket.getLocalPort();// gives me port

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("HEARTBEAT_PORT:"+heartbeatPort);

                Socket heartbeatSocket = heartbeatServerSocket.accept();
                System.out.println(players.size());

                ClientHandler clientHandler = new ClientHandler(socket,heartbeatSocket);

                // spectators and players list are updated in ClientHandler.java
                Thread.startVirtualThread(clientHandler::handleClient);
            }
            catch (IOException e){
                System.out.println("Server is closing");
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                break;
            }
        }
    }

    static void startGame() {
        gameStarted = true;

        // start the game
        while (!gameFinished){
            // initialize game

            if(!players.get(0).getIsAlive() || !players.get(1).getIsAlive()){
                System.out.println("White player is out");
                break;
            }

            Move whiteMove = players.get(0).handleMessagePlayer("white");

            ClientHandler.broadcastPlayer(players.get(1), whiteMove); // send update to black player

            broadcast(whiteMove);


            if(!players.get(0).getIsAlive() || !players.get(1).getIsAlive()){
                System.out.println("black player is out");
                break;
            }

            // black player
            Move blackMove = players.get(1).handleMessagePlayer("black");

            broadcast(blackMove);

            ClientHandler.broadcastPlayer(players.get(0), blackMove); // send update to white player

        }
    }

    public static void endGame() {
        gameFinished = true;

        // Send to every client closing acknowledgement
        System.out.println("Closing the game :: endGame()");
        players.forEach(ClientHandler::close);
        spectators.forEach(ClientHandler::close);

        // Use a separate thread to close the server socket after a brief delay
        // This ensures all client close messages are sent before shutting down
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(500); // Give time for messages to be sent
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    System.out.println("Server socket closed");
                }
            } catch (Exception e) {
                System.out.println("Error during server shutdown: " + e.getMessage());
            }
        });


    }

    public static void kickoutSpectator(){
        spectators = spectators.stream().filter((element) -> !element.getIsAlive()).collect(Collectors.toList());;
    }


    // broadcasts
    public static void broadcast(Move move){
        Thread.startVirtualThread(() -> {

            spectators = spectators.stream().filter((client -> client.getMoveSocket() != null)).collect(Collectors.toList());

            spectators.forEach((client) -> client.sendUpdate(move));
        });
    }
}
