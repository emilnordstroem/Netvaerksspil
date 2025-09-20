import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static ServerSocket welcomeSocket;
    private static final MessageFormatter messageFormatter = new MessageFormatter();
    private static Map<Socket, DataOutputStream> clientSockets;
    private static Map<String, Player> players;

    public static void main(String[] args) {
        try {
            welcomeSocket = new ServerSocket(10_000);
            clientSockets = new HashMap<>();
            players = new HashMap<>();

            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                enableDataOutputFromClient(connectionSocket);
                Thread readThread = new Thread(() -> readFromClient(connectionSocket));
                readThread.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enableDataOutputFromClient(Socket connectionSocket) {
        try {
            DataOutputStream outputStream = new DataOutputStream(
                    connectionSocket.getOutputStream()
            );
            System.out.println("[client connected to Server from IP: " + connectionSocket.getInetAddress() + "]");
            clientSockets.put(connectionSocket, outputStream);

            // Write all existing players to new client
            for (Player currentPlayer : players.values()) {
                String addPlayerMessage = messageFormatter.addPlayerMessage(
                        currentPlayer.name,
                        currentPlayer.xpos,
                        currentPlayer.ypos,
                        currentPlayer.direction
                );
                outputStream.writeBytes(addPlayerMessage + "\n"); // <- send only to new client
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readFromClient(Socket connectionSocket) {
        try {
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(
                            connectionSocket.getInputStream()
                    )
            );
            while (true) {
                String messageFromClient = inFromClient.readLine();
                System.out.println("received from client: " + messageFromClient);

                if (messageFromClient == null) {
                    return;
                }

                String[] messageFormat = messageFromClient.trim().split(" ");
                String messageType = messageFormat[0];
                String playerName = messageFormat[1];

                switch (messageType) {
                    case "move_player" -> {
                        int xDirectionMove = Integer.parseInt(messageFormat[2]);
                        int yDirectionMove = Integer.parseInt(messageFormat[3]);
                        String newDirection = messageFormat[4];

                        updatePlayerPosition(playerName, xDirectionMove, yDirectionMove, newDirection);
                    }
                    case "add_player" -> {
                        int xPosition = Integer.parseInt(messageFormat[2]);
                        int yPosition = Integer.parseInt(messageFormat[3]);
                        String direction = messageFormat[4];

                        addNewPlayer(playerName, xPosition, yPosition, direction);
                    }
                    case "update_player_points" -> {
                        int pointChange = Integer.parseInt(messageFormat[2]);
                        updatePlayerPoints(playerName, pointChange);
                    }
                }
                writeToAllClients(messageFromClient);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeToAllClients(String messageFromClient){
        String messageToClients = messageFromClient.trim();
        clientSockets.forEach((socket, outputStream) -> {
            try {
                System.out.println("message forwarded to: " + socket.getInetAddress());
                outputStream.writeBytes(messageToClients + '\n');
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Player logic
    private static void updatePlayerPosition (String playerName, int xDirectionMove, int yDirectionMove, String newDirection) {
        players.forEach((currentPlayerName, player) -> {
            if (currentPlayerName.equals(playerName)) {
                player.setXpos(
                        player.xpos += xDirectionMove
                );
                player.setYpos(
                        player.ypos += yDirectionMove
                );
                player.setDirection(newDirection);
            }
        });
    }

    private static void addNewPlayer (String playerName, int xPosition, int yPosition, String direction) {
        Player newPlayer = new Player(
                playerName,
                xPosition,
                yPosition,
                direction
        );
        players.put(playerName, newPlayer);
    }

    private static void updatePlayerPoints (String playerName, int pointChange) {
        players.forEach((currentPlayerName, player) -> {
            if (currentPlayerName.equals(playerName)) {
                player.addPoints(pointChange);
            }
        });
    }


}