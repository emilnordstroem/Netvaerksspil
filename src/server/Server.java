package server;

import models.Player;
import client.MessageFormatter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Server {
    private static ServerSocket welcomeSocket;
    private static final MessageFormatter messageFormatter = new MessageFormatter();
    private static Map<Socket, DataOutputStream> clientSockets;
    private static Map<String, Player> players;
    private static Queue<String> receivedRequestQueue;
    private static int globalTimeStamp;

    public static void main(String[] args) {
        try {
            welcomeSocket = new ServerSocket(10_000);
            clientSockets = new HashMap<>();
            players = new HashMap<>();
            receivedRequestQueue = createRequestQueue();
            globalTimeStamp = 0;

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

    private static PriorityQueue<String> createRequestQueue () {
        return new PriorityQueue<>((message1, message2) -> {
            int timeStamp1 = Integer.parseInt(message1.split(" ")[1]);
            int timeStamp2 = Integer.parseInt(message2.split(" ")[1]);

            // prioritize time stamp by lower first
            if (timeStamp1 != timeStamp2) {
                return Integer.compare(timeStamp1, timeStamp2);
            }
            // Tie-breaker - then organize by player name
            String player1 = message1.split(" ")[2];
            String player2 = message2.split(" ")[2];
            return player1.compareTo(player2);
        });
    }

    private static void enableDataOutputFromClient(Socket connectionSocket) {
        try {
            DataOutputStream outputStream = new DataOutputStream(
                    connectionSocket.getOutputStream()
            );
            System.out.println("[client connected to server.Server from IP: " + connectionSocket.getInetAddress() + "]");
            clientSockets.put(connectionSocket, outputStream);

            // Write all existing players to new client
            for (Player currentPlayer : players.values()) {
                String addPlayerMessage = messageFormatter.addPlayerMessage(
                        currentPlayer.getName(),
                        currentPlayer.getXpos(),
                        currentPlayer.getYpos(),
                        currentPlayer.getDirection()
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

                receivedRequestQueue.add(messageFromClient);

                processRequestQueue();
            }
        } catch (IOException e) {
            clientSockets.remove(connectionSocket);
        }
    }

    private static void processRequestQueue () {
        while (!receivedRequestQueue.isEmpty()) {
            // get next received request from queue
            String receivedMessageFromClient = receivedRequestQueue.poll();

            // format message into string array
            String[] requestMessageFormat = receivedMessageFromClient.trim().split(" ");

            // Update global time stamp
            int requestTimeStamp = Integer.parseInt(requestMessageFormat[1]);
            updateGlobalTimeStamp(requestTimeStamp);

            // process the request message - update player
            processRequestMessage(requestMessageFormat);

            // reply to all clients
            writeToAllClients(receivedMessageFromClient);
        }
    }

    private static void updateGlobalTimeStamp(int requestTimeStamp) {
        globalTimeStamp = Math.max(globalTimeStamp, requestTimeStamp);
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

    // process message
    private static void processRequestMessage (String[] messageFormat) {
        String messageType = messageFormat[2];
        String playerName = messageFormat[3];

        switch (messageType) {
            case "move_player" -> {
                int xDirectionMove = Integer.parseInt(messageFormat[4]);
                int yDirectionMove = Integer.parseInt(messageFormat[5]);
                String newDirection = messageFormat[6];

                updatePlayerPosition(playerName, xDirectionMove, yDirectionMove, newDirection);
            }
            case "add_player" -> {
                int xPosition = Integer.parseInt(messageFormat[4]);
                int yPosition = Integer.parseInt(messageFormat[5]);
                String direction = messageFormat[6];

                addNewPlayer(playerName, xPosition, yPosition, direction);
            }
            case "update_player_points" -> {
                int pointChange = Integer.parseInt(messageFormat[4]);
                updatePlayerPoints(playerName, pointChange);
            }
            case "player_hit_by_shot" -> {
                int newXPosition = Integer.parseInt(messageFormat[4]);
                int newYPosition = Integer.parseInt(messageFormat[5]);
                updatePlayerPosition(playerName, newXPosition, newYPosition);
            }
        }
    }

    // Player logic
    private static void updatePlayerPosition (String playerName, int xDirectionMove, int yDirectionMove, String newDirection) {
        players.forEach((currentPlayerName, player) -> {
            if (currentPlayerName.equals(playerName)) {
                int newXPosition = player.getXpos() + xDirectionMove;
                int newYPosition = player.getYpos() + yDirectionMove;

                player.setXpos(newXPosition);
                player.setYpos(newYPosition);
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

    private static void updatePlayerPosition (String playerName, int newXPosition, int newYPosition) {
        players.forEach((currentPlayerName, player) -> {
            if (currentPlayerName.equals(playerName)) {
                player.setXpos(newXPosition);
                player.setYpos(newYPosition);
            }
        });
    }

}