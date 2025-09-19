import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private static ServerSocket welcomeSocket;
    private static final MessageFormatter messageFormatter = new MessageFormatter();
    private static Map<Socket, DataOutputStream> clientSockets;
    private static Map<Socket, Player> players;

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

    private static void readFromClient(Socket connectionSocket){
        while (true) {
            try {
                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader(
                                connectionSocket.getInputStream()
                        )
                );
                String messageFromClient = inFromClient.readLine();
                System.out.println("received from client: " + messageFromClient);

                String[] messageFormat = messageFromClient.trim().split(" ");
                String messageType = messageFormat[0];
                String playerName = messageFormat[1];

                if (messageType.equals("add_player")) {
                    int xPosition = Integer.parseInt(messageFormat[2]);
                    int yPosition = Integer.parseInt(messageFormat[3]);
                    String direction = messageFormat[4];

                    // Construct new player and add to map
                    Player newPlayer = new Player(
                            playerName,
                            xPosition,
                            yPosition,
                            direction
                    );
                    players.put(connectionSocket, newPlayer);
                }

                writeToAllClients(messageFromClient);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void writeToClientsExcept(Socket exception, String messageFromClient){
        String messageToClients = messageFromClient.trim();
        clientSockets.forEach((socket, outputStream) -> {
            if (socket.equals(exception)) {
                return;
            }
            try {
                System.out.println("message forwarded to: " + socket.getInetAddress());
                outputStream.writeBytes(messageToClients + '\n');
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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

}