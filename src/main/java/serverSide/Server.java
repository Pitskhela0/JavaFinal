package serverSide;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

                System.out.println(players.size());

                ClientHandler clientHandler = new ClientHandler(socket);

                // spectators and players list are updated in ClientHandler.java

                new Thread(clientHandler::handleClient).start();

            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    static void startGame() {
        // start the game
        while (true){

            gameStarted = true;
            // initialize game

            // white player
            Move whiteMove = players.get(0).handleMove("white");
            // send update to spectators and black player (second in the list is always black)
            broadcast(whiteMove);
            // update black player board with white move
            ClientHandler.broadcastPlayer(players.get(1), whiteMove);

            // black player
            Move blackMove = players.get(1).handleMove("black");
            // send updates to spectators and white player (first in the list is always white)
            broadcast(blackMove);
            // update white player board with black move
            ClientHandler.broadcastPlayer(players.get(0), blackMove);


            // todo update board after every move

            // todo problems:

            // ertma motamashem mxolod erti modzraoba unda gaaketos, user inputi gamosasworebeli

            // klienti roca ukukavshirdeba:
            // playeri ukukavshirda: daasrule tamashi
            // spektatori ukukavshirda: aranairi cvlileba, mxolod spektatori gadis


            // todo: after problems
            // integracia tamashtan

            // bazebshi shenaxva: advili
            // PGN_shi gadatana arcise advili

            // daregistrireba, eg servershi unda moxdes tamashis dawyebamde
            // BOT_tan tamashi, ar vici magram cool

        }
    }

    // broadcasts
    public static void broadcast(Move move){
        new Thread(() -> {
            for(ClientHandler client : spectators){
                client.sendUpdate(move);
            }
        }).start();
    }
}
