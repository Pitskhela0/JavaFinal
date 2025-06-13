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
    static List<ClientHandler> spectators = Collections.synchronizedList(new ArrayList<>());
    static List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>());
    static Board board;
    static boolean gameFinished = false;
    static boolean gameStarted = false;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
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
                throw new RuntimeException(e);
            }
        }
    }

    static void startGame() {
        gameStarted = true;

        // start the game
        while (!gameFinished){
            // initialize game

            if(!players.get(0).getIsAlive()){
                System.out.println("White player is out");
                break;
            }

            Move whiteMove = players.get(0).handleMessagePlayer("white");

            ClientHandler.broadcastPlayer(players.get(1), whiteMove); // send update to black player

            broadcast(whiteMove);


            if(!players.get(1).getIsAlive()){
                System.out.println("black player is out");
                break;
            }

            // black player
            Move blackMove = players.get(1).handleMessagePlayer("black");

            broadcast(blackMove);

            ClientHandler.broadcastPlayer(players.get(0), blackMove); // send update to white player

        }
    }

    public static void endGame(){
        gameFinished = true;
        // send to every client closing acknowledgement
        players.forEach(ClientHandler::close);
        spectators.forEach(ClientHandler::close);
    }

    public static void kickoutSpectator(){

    }


    // broadcasts
    public static void broadcast(Move move){
        Thread.startVirtualThread(() -> {

            spectators = spectators.stream().filter((client -> client.getMoveSocket() != null)).collect(Collectors.toList());

            spectators.forEach((client) -> client.sendUpdate(move));
        });
    }
}


// change game loop, server should send to player when it is his turn



// todo update board after every move

// thread with virtual thread

// todo problems:

// spectators unda sheedzlos gasvla da aseve playersac, optional unda iyos gasvla

// klienti roca ukukavshirdeba:
// playeri ukukavshirda: daasrule tamashi
// spektatori ukukavshirda: aranairi cvlileba, mxolod spektatori gadis
// resources should be closed




// todo: after problems
// integracia tamashtan

// bazebshi shenaxva: advili
// PGN_shi gadatana arcise advili

// daregistrireba, eg servershi unda moxdes tamashis dawyebamde
// BOT_tan tamashi, ar vici magram cool