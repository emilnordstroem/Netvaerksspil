package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.*;

import javafx.application.Platform;
import models.Player;

public class GUI extends Application {
    private static String host;
    private static final int port = 10_000;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;
    private static int localTimeStamp = 0;

    public static final int size = 20;
    public static final int scene_height = size * 20 + 100;
    public static final int scene_width = size * 20 + 200;

    public static Image image_floor;
    public static Image image_wall;
    public static Image hero_right, hero_left, hero_up, hero_down;
    public static Image fire_horizontal, fire_right, fire_left, fire_vertical, fire_up, fire_down;
    public static Image fireWall_right, fireWall_left, fireWall_up, fireWall_down;

    private final MessageFormatter messageFormatter = new MessageFormatter();
    public static Player me;
    public static List<Player> players = new ArrayList<>();

    private Label[][] fields;
    private TextArea scoreList;

    private String[] board = {    // 20x20
            "wwwwwwwwwwwwwwwwwwww",
            "w        ww        w",
            "w w  w  www w  w  ww",
            "w w  w   ww w  w  ww",
            "w  w               w",
            "w w w w w w w  w  ww",
            "w w     www w  w  ww",
            "w w     w w w  w  ww",
            "w   w w  w  w  w   w",
            "w     w  w  w  w   w",
            "w ww ww        w  ww",
            "w  w w    w    w  ww",
            "w        ww w  w  ww",
            "w         w w  w  ww",
            "w        w     w  ww",
            "w  w              ww",
            "w  w www  w w  ww ww",
            "w w      ww w     ww",
            "w   w   ww  w      w",
            "wwwwwwwwwwwwwwwwwwww"
    };


    // -------------------------------------------
    // | Maze: (0,0)              | Score: (1,0) |
    // |-----------------------------------------|
    // | boardGrid (0,1)          | scorelist    |
    // |                          | (1,1)        |
    // -------------------------------------------

    @Override
    public void start(Stage primaryStage) {
        // Initiate game
        initiateSetup();

        // TCP connectionSetup
        establishTCPConnection();

        // client.GUI grid and board setup
        GridPane grid = gridPaneSetup();

        Scene scene = new Scene(
                grid,
                scene_width,
                scene_height
        );
        primaryStage.setScene(scene);
        primaryStage.show();

        // handle key presses
        handleKeyPress(scene);
        // handle incoming instructions
        Thread readThread = new Thread(() -> readFromServer());
        readThread.start();
    }

    private void initiateSetup() {
        TextInputDialog connectionSetupDialog = new TextInputDialog();

        connectionSetupDialog.setTitle("Welcome to the Maze");
        connectionSetupDialog.setHeaderText("To connect, you must enter a Server IP address before connecting");
        connectionSetupDialog.setContentText("Please enter a valid Server IP:");
        host = connectionSetupDialog.showAndWait().orElse("localhost");

        connectionSetupDialog = new TextInputDialog();
        connectionSetupDialog.setTitle("Welcome to the Maze");
        connectionSetupDialog.setHeaderText("You need to take action on one matter before joining the game");
        connectionSetupDialog.setContentText("Please enter a player name:");

        // Wait until entered playername
        String inputPlayerName = connectionSetupDialog.showAndWait().get();
        // setup me player object
        int[] startPosition = getStartPosition();
        me = new Player(inputPlayerName, startPosition[0], startPosition[1], "up");
    }

    private void establishTCPConnection() {
        try {
            clientSocket = new Socket(host, port);
            outToServer = new DataOutputStream(
                    clientSocket.getOutputStream()
            );
            inFromServer = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()
                    )
            );
            // send player object information
            writeToServer(messageFormatter.addNewPlayerMessage(
                    me.getName(),
                    me.getXpos(),
                    me.getYpos()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Graphical user interface
    private GridPane gridPaneSetup() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 0, 10));

        Text mazeLabel = new Text("Maze:");
        mazeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Text scoreLabel = new Text("Score:");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        scoreList = new TextArea();

        // board setup
        GridPane boardGrid = new GridPane();

        image_wall = new Image(getClass().getResourceAsStream("Image/wall4.png"), size, size, false, false);
        image_floor = new Image(getClass().getResourceAsStream("Image/floor1.png"), size, size, false, false);

        hero_right = new Image(getClass().getResourceAsStream("Image/heroRight.png"), size, size, false, false);
        hero_left = new Image(getClass().getResourceAsStream("Image/heroLeft.png"), size, size, false, false);
        hero_up = new Image(getClass().getResourceAsStream("Image/heroUp.png"), size, size, false, false);
        hero_down = new Image(getClass().getResourceAsStream("Image/heroDown.png"), size, size, false, false);

        fire_horizontal = new Image(getClass().getResourceAsStream("Image/fireHorizontal.png"), size, size, false, false);
        fire_right = new Image(getClass().getResourceAsStream("Image/fireLeft.png"), size, size, false, false);
        fire_left = new Image(getClass().getResourceAsStream("Image/fireRight.png"), size, size, false, false);
        fire_vertical = new Image(getClass().getResourceAsStream("Image/fireVertical.png"), size, size, false, false);
        fire_up = new Image(getClass().getResourceAsStream("Image/fireDown.png"), size, size, false, false);
        fire_down = new Image(getClass().getResourceAsStream("Image/fireUp.png"), size, size, false, false);

        fireWall_right = new Image(getClass().getResourceAsStream("Image/fireWallEast.png"), size, size, false, false);
        fireWall_left = new Image(getClass().getResourceAsStream("Image/fireWallWest.png"), size, size, false, false);
        fireWall_up = new Image(getClass().getResourceAsStream("Image/fireWallNorth.png"), size, size, false, false);
        fireWall_down = new Image(getClass().getResourceAsStream("Image/fireWallSouth.png"), size, size, false, false);

        fields = new Label[20][20];
        try {
            for (int j = 0; j < 20; j++) {
                for (int i = 0; i < 20; i++) {
                    switch (board[j].charAt(i)) {
                        case 'w':
                            fields[i][j] = new Label("", new ImageView(image_wall));
                            break;
                        case ' ':
                            fields[i][j] = new Label("", new ImageView(image_floor));
                            break;
                        default:
                            throw new Exception("Illegal field value: " + board[j].charAt(i));
                    }
                    boardGrid.add(fields[i][j], i, j);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        scoreList.setEditable(false);

        // Add elements
        grid.add(mazeLabel, 0, 0);
        grid.add(scoreLabel, 1, 0);
        grid.add(boardGrid, 0, 1);
        grid.add(scoreList, 1, 1);

        scoreList.setText(getScoreList());

        return grid;
    }

    // I/O component action
    private void handleKeyPress(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (me == null) {
                // if there is no me object yet - ignore the press of keys
                return;
            }
            switch (event.getCode()) {
                case UP:
                    writeToServer(messageFormatter.movePlayerMessage(me.getName(), 0, -1, "up"));
                    break;
                case DOWN:
                    writeToServer(messageFormatter.movePlayerMessage(me.getName(), 0, +1, "down"));
                    break;
                case LEFT:
                    writeToServer(messageFormatter.movePlayerMessage(me.getName(), -1, 0, "left"));
                    break;
                case RIGHT:
                    writeToServer(messageFormatter.movePlayerMessage(me.getName(), +1, 0, "right"));
                    break;
                case SPACE:
                    System.out.println("You pressed SPACE");
                    switch (me.getDirection()) {
                        case "up":
                            writeToServer(messageFormatter.shotFired(me.getName(), 0, -1, me.getDirection()));
                            break;
                        case "left":
                            writeToServer(messageFormatter.shotFired(me.getName(), -1, 0, me.getDirection()));
                            break;
                        case "right":
                            writeToServer(messageFormatter.shotFired(me.getName(), +1, 0, me.getDirection()));
                            break;
                        case "down":
                            writeToServer(messageFormatter.shotFired(me.getName(), 0, +1, me.getDirection()));
                            break;
                    }
                    break;
                default:
                    break;
            }
        });
    }

    // Server communication
    private void writeToServer(String messageToServer) {
        try {
            // increment local timestamp
            localTimeStamp++;
            // send message
            String requestMessage = messageFormatter.requestMessage(
                    localTimeStamp,
                    messageToServer
            );
            outToServer.writeBytes(requestMessage + '\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFromServer() {
        while (true) {
            try {
                String messageFromServer = inFromServer.readLine();
                System.out.println("received by server: " + messageFromServer);
                if (messageFromServer == null || messageFromServer.isEmpty()) {
                    return;
                }
                String[] messageFormat = messageFromServer.trim().split(" ");

                int receivedTimeStamp = Integer.parseInt(messageFormat[1]);
                updateLocalTimeStamp(receivedTimeStamp);

                String messageType = messageFormat[2];
                String playerName = messageFormat[3];

                switch (messageType) {
                    case "move_player":
                        int xDirectionMove = Integer.parseInt(messageFormat[4]);
                        int yDirectionMove = Integer.parseInt(messageFormat[5]);
                        String newDirection = messageFormat[6];

                        // Thread overreach fix with Platform.runLater()
                        // Background thread
                        Platform.runLater(() -> {
                            playerMoved(
                                    playerName,
                                    xDirectionMove,
                                    yDirectionMove,
                                    newDirection
                            );
                        });
                        break;
                    case "add_player":
                        int xPosition = Integer.parseInt(messageFormat[4]);
                        int yPosition = Integer.parseInt(messageFormat[5]);
                        String direction = messageFormat[6];

                        Platform.runLater(() -> {
                            settingUpNewPlayer(
                                    playerName,
                                    xPosition,
                                    yPosition,
                                    direction
                            );
                        });
                        break;
                    case "update_player_points":
                        int pointChange = Integer.parseInt(messageFormat[4]);

                        Platform.runLater(() -> {
                            updatePlayerPoints(
                                    playerName,
                                    pointChange
                            );
                        });
                        break;
                    case "shot_fired":
                        int xDirection = Integer.parseInt(messageFormat[4]);
                        int yDirection = Integer.parseInt(messageFormat[5]);
                        String shotDirection = messageFormat[6];
                        Platform.runLater(() -> {
                            shotFired(
                                    playerName,
                                    xDirection,
                                    yDirection,
                                    shotDirection
                            );
                        });
                        break;
                    case "player_hit_by_shot":
                        int newXPosition = Integer.parseInt(messageFormat[4]);
                        int newYPosition = Integer.parseInt(messageFormat[5]);
                        Platform.runLater(() -> {
                            updatePlayerPosition(
                                    playerName,
                                    newXPosition,
                                    newYPosition
                            );
                        });
                        break;
                    default:
                        break;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateLocalTimeStamp(int receivedTimeStamp) {
        localTimeStamp = Math.max(localTimeStamp, receivedTimeStamp);
    }

    // Game Mechanics
    private void settingUpNewPlayer(String name, int xPosition, int yPosition, String direction) {
        for (Player player : players) {
            if (player.getName().equals(name)) { // already exists, just skip (avoid duplicates)
                player.setXpos(xPosition);
                player.setYpos(yPosition);
                player.setDirection(direction);
                return;
            }
        }

        Player newPlayer = new Player(
                name,
                xPosition,
                yPosition,
                direction
        );
        if (name.equals(me.getName())) {
            me = newPlayer;
        }
        players.add(newPlayer);
        fields[xPosition][yPosition].setGraphic(new ImageView(hero_up));
    }

    public void playerMoved(String playerName, int delta_x, int delta_y, String direction) {
        Player playerToMoved = null;
        for (Player player : players) {
            if (player.getName().equals(playerName)) {
                playerToMoved = player;
            }
        }
        if (playerToMoved == null) {
            return;
        }

        playerToMoved.setDirection(direction);
        int currentXPosition = playerToMoved.getXpos();
        int currentYPosition = playerToMoved.getYpos();

        if (board[currentYPosition + delta_y].charAt(currentXPosition + delta_x) == 'w') {
            writeToServer(
                    messageFormatter.updatePlayerPoint(
                            playerName,
                            -1
                    )
            );
        } else {
            Player playerAtNewPosition = getPlayerAt(currentXPosition + delta_x, currentYPosition + delta_y);
            if (playerAtNewPosition != null) {
                // This needs to be messages
                writeToServer(
                        messageFormatter.updatePlayerPoint(
                                playerToMoved.getName(),
                                10
                        )
                );
                writeToServer(
                        messageFormatter.updatePlayerPoint(
                                playerAtNewPosition.getName(),
                                -10
                        )
                );
            } else {
                // needs to be a message
                writeToServer(
                        messageFormatter.updatePlayerPoint(
                                playerName,
                                1
                        )
                );

                floorImage(currentXPosition, currentYPosition);
                currentXPosition += delta_x;
                currentYPosition += delta_y;

                if (direction.equals("right")) {
                    fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_right));
                }
                ;
                if (direction.equals("left")) {
                    fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_left));
                }
                ;
                if (direction.equals("up")) {
                    fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_up));
                }
                ;
                if (direction.equals("down")) {
                    fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_down));
                }
                ;

                playerToMoved.setXpos(currentXPosition);
                playerToMoved.setYpos(currentYPosition);
            }
        }

        scoreList.setText(getScoreList());
    }

    public void updatePlayerPoints(String playerName, int point) {
        for (Player player : players) {
            if (player.getName().equals(playerName)) {
                player.addPoints(point);
            }
        }
        scoreList.setText(getScoreList());
    }

    public void shotFired(String playerName, int xDirection, int yDirection, String direction) {
        Player shootingPlayer = null;
        for (Player currentPlayer : players) {
            if (currentPlayer.getName().equals(playerName)) {
                shootingPlayer = currentPlayer;
            }
        }
        if (shootingPlayer == null) {
            return;
        }

        List<BoardPointer> positionsImpactedByShot = new ArrayList<>();
        int currentShotXPosition = shootingPlayer.getXpos() + xDirection;
        int currentShotYPosition = shootingPlayer.getYpos() + yDirection;

        while (board[currentShotYPosition].charAt(currentShotXPosition) != 'w') {
            if (!positionsImpactedByShot.isEmpty()) {
                BoardPointer previousPointer = positionsImpactedByShot.getLast();
                int xPosition = previousPointer.getxPosition();
                int yPosition = previousPointer.getyPosition();
                if (direction.equals("left") || direction.equals("right")) {
                    fireDirectionImage(
                            xPosition,
                            yPosition,
                            "horizontal"
                    );
                } else {
                    fireDirectionImage(
                            xPosition,
                            yPosition,
                            "vertical"
                    );
                }
            }

            positionsImpactedByShot.add(
                    new BoardPointer(
                            currentShotXPosition,
                            currentShotYPosition
                    )
            );
            fireDirectionImage(currentShotXPosition, currentShotYPosition, direction);

            Player playerAtCurrentShotPosition = getPlayerAt(
                    currentShotXPosition,
                    currentShotYPosition
            );
            if (playerAtCurrentShotPosition != null) {
                int[] newPosition = getStartPosition();
                writeToServer(
                        messageFormatter.playerHitByShot(
                                playerAtCurrentShotPosition.getName(),
                                newPosition[0],
                                newPosition[1]
                        )
                );
                writeToServer(
                        messageFormatter.updatePlayerPoint(
                                me.getName(),
                                +50
                        )
                );
                writeToServer(
                        messageFormatter.updatePlayerPoint(
                                playerAtCurrentShotPosition.getName(),
                                -50
                        )
                );
                break;
            }

            currentShotXPosition += xDirection;
            currentShotYPosition += yDirection;
        }

        // Reset after time duration
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    resetFieldsImpactedByShot(positionsImpactedByShot);
                });
            }
        }, 300);
    }

    private void resetFieldsImpactedByShot(List<BoardPointer> positionsImpactedByShot) {
        positionsImpactedByShot.forEach(pointer -> {
            Player playerAtPosition = getPlayerAt(pointer.getxPosition(), pointer.getyPosition());
            if (playerAtPosition == null) {
                floorImage(
                        pointer.getxPosition(),
                        pointer.getyPosition()
                );
            }
        });
    }

    private void updatePlayerPosition(String playerName, int newXPosition, int newYPosition) {
        players.forEach(currentPlayer -> {
            if (currentPlayer.getName().equals(playerName)) {
                currentPlayer.setXpos(newXPosition);
                currentPlayer.setYpos(newYPosition);
            }
        });
    }


    public int[] getStartPosition() {
        int startXPosition = new Random().nextInt(0, size);
        int startYPosition = new Random().nextInt(0, size);

        while (board[startYPosition].charAt(startXPosition) == 'w') {
            startXPosition = new Random().nextInt(0, size);
            startYPosition = new Random().nextInt(0, size);
        }
        return new int[]{startXPosition, startYPosition};
    }

    public String getScoreList() {
        StringBuffer buffer = new StringBuffer(100);

        for (Player player : players) {
            buffer.append(player + "\r\n");
        }

        return buffer.toString();
    }

    public Player getPlayerAt(int x, int y) {
        for (Player player : players) {
            if (player.getXpos() == x && player.getYpos() == y) {
                return player;
            }
        }
        return null;
    }

    // Set images and animations
    public void floorImage(int xPosition, int yPosition) {
        fields[xPosition][yPosition].setGraphic(new ImageView(image_floor));
    }

    public void fireDirectionImage(int xPosition, int yPosition, String direction) {
        if (direction.equals("horizontal")) {
            fields[xPosition][yPosition].setGraphic(new ImageView(fire_horizontal));
        }
        ;
        if (direction.equals("right")) {
            fields[xPosition][yPosition].setGraphic(new ImageView(fireWall_right));
        }
        ;
        if (direction.equals("left")) {
            fields[xPosition][yPosition].setGraphic(new ImageView(fireWall_left));
        }
        ;
        if (direction.equals("vertical")) {
            fields[xPosition][yPosition].setGraphic(new ImageView(fire_vertical));
        }
        ;
        if (direction.equals("up")) {
            fields[xPosition][yPosition].setGraphic(new ImageView(fireWall_up));
        }
        ;
        if (direction.equals("down")) {
            fields[xPosition][yPosition].setGraphic(new ImageView(fireWall_down));
        }
        ;
    }

}