import clientSide.Client;
import serverSide.Server;
import startMenu.ClientConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private List<Server> games = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        ClientConnection client = new ClientConnection();
        client.start();

    }
}
