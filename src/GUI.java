
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

public class GUI extends Application {
	private static String host = "localhost";
	private static int port = 10_000;
	private static Socket clientSocket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;

	public static final int size = 20; 
	public static final int scene_height = size * 20 + 100;
	public static final int scene_width = size * 20 + 200;

	public static Image image_floor;
	public static Image image_wall;
	public static Image hero_right,hero_left,hero_up,hero_down;

	private final MessageFormatter messageFormatter = new MessageFormatter();
	public static Player me;
	public static List<Player> players = new ArrayList<>();

	private Label[][] fields;
	private TextArea scoreList;
	
	private  String[] board = {    // 20x20
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
		// TCP connectionSetup
		establishTCPConnection();

		// GUI grid and board setup
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

		// Setting up standard players
//		settingUpStandardPlayers();
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
		} catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	// Graphical user interface
	private GridPane gridPaneSetup () {
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

		image_wall  = new Image(getClass().getResourceAsStream("Image/wall4.png"),size,size,false,false);
		image_floor = new Image(getClass().getResourceAsStream("Image/floor1.png"),size,size,false,false);

		hero_right  = new Image(getClass().getResourceAsStream("Image/heroRight.png"),size,size,false,false);
		hero_left   = new Image(getClass().getResourceAsStream("Image/heroLeft.png"),size,size,false,false);
		hero_up     = new Image(getClass().getResourceAsStream("Image/heroUp.png"),size,size,false,false);
		hero_down   = new Image(getClass().getResourceAsStream("Image/heroDown.png"),size,size,false,false);

		fields = new Label[20][20];
		try {
			for (int j=0; j<20; j++) {
				for (int i=0; i<20; i++) {
					switch (board[j].charAt(i)) {
						case 'w':
							fields[i][j] = new Label("", new ImageView(image_wall));
							break;
						case ' ':
							fields[i][j] = new Label("", new ImageView(image_floor));
							break;
						default: throw new Exception("Illegal field value: "+board[j].charAt(i) );
					}
					boardGrid.add(fields[i][j], i, j);
				}
			}
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
        scoreList.setEditable(false);

		// Add elements
		grid.add(mazeLabel,  0, 0);
		grid.add(scoreLabel, 1, 0);
		grid.add(boardGrid,  0, 1);
		grid.add(scoreList,  1, 1);

		scoreList.setText(getScoreList());

		return grid;
	}

	// I/O component action
	private void handleKeyPress(Scene scene){
		scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			switch (event.getCode()) {
				// Concept:
				// eventListener på key pressed (fortæller server move)
				// bevæger sig kun ved server message (instruktion)
				// formel for besked: player, move, x-move, y-move
				// direction refere til billede - brug move.tolowercase()
				case UP:
					writeToServer(messageFormatter.movePlayerMessage(me.name, 0, -1, "up"));
					break;
				case DOWN:
					writeToServer(messageFormatter.movePlayerMessage(me.name, 0, +1, "down"));
					break;
				case LEFT:
					writeToServer(messageFormatter.movePlayerMessage(me.name, -1, 0, "left"));
					break;
				case RIGHT:
					writeToServer(messageFormatter.movePlayerMessage(me.name, +1, 0, "right"));
					break;
				default: break;
			}
		});
	}

	// Read/Write to central node (Server)
	private void writeToServer(String messageToServer){
		try {
			outToServer.writeBytes(messageToServer + '\n');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void readFromServer(){
		while (true) {
			try {
				String messageFromServer = inFromServer.readLine();
				System.out.println("received by server: " + messageFromServer);

				String[] messageFormat = messageFromServer.trim().split(" ");
				String messageType = messageFormat[0];
				String playerName = messageFormat[1];

				switch (messageType) {
					case "move_player":
						int xDirectionMove = Integer.parseInt(messageFormat[2]);
						int yDirectionMove = Integer.parseInt(messageFormat[3]);
						String newDirection = messageFormat[4];

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
						int xPosition = Integer.parseInt(messageFormat[2]);
						int yPosition = Integer.parseInt(messageFormat[3]);
						String direction = messageFormat[4];

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
						int pointChange = Integer.parseInt(messageFormat[2]);

						Platform.runLater(() -> {
							updatePlayerPoints(
									playerName,
									pointChange
							);
						});
					default: break;
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

//	private void settingUpStandardPlayers () {
//		me = new Player("Orville",9,4,"up");
//		players.add(me);
//		fields[9][4].setGraphic(new ImageView(hero_up));
//
//		Player harry = new Player("Harry",14,15,"up");
//		players.add(harry);
//		fields[14][15].setGraphic(new ImageView(hero_up));
//	}

	private void settingUpNewPlayer(String name, int xPosition, int yPosition, String direction){
		Player newPlayer =  new Player(
				name,
				xPosition,
				yPosition,
				direction
		);
		if (players.isEmpty()) {
			me = newPlayer;
		}
		players.add(newPlayer);
		fields[xPosition][yPosition].setGraphic(new ImageView(hero_up));
	}

	// Game Mechanics
	public void playerMoved(String playerName, int delta_x, int delta_y, String direction) {
		Player playerToMoved = null;
		for (Player player : players) {
			if (player.name.equals(playerName)) {
				playerToMoved = player;
			}
		}
		if (playerToMoved == null) {
			return;
		}

		playerToMoved.direction = direction;
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
			Player playerAtNewPosition = getPlayerAt(currentXPosition + delta_x,currentYPosition + delta_y);
			if (playerAtNewPosition != null) {
				// This needs to be messages
				playerToMoved.addPoints(10);
				playerAtNewPosition.addPoints(-10);
			} else {
				// needs to be a message
				writeToServer(
						messageFormatter.updatePlayerPoint(
								playerName,
								1
						)
				);

				fields[currentXPosition][currentYPosition].setGraphic(new ImageView(image_floor));
				currentXPosition+=delta_x;
				currentYPosition+=delta_y;

				if (direction.equals("right")) {
					fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_right));
				};
				if (direction.equals("left")) {
					fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_left));
				};
				if (direction.equals("up")) {
					fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_up));
				};
				if (direction.equals("down")) {
					fields[currentXPosition][currentYPosition].setGraphic(new ImageView(hero_down));
				};

				playerToMoved.setXpos(currentXPosition);
				playerToMoved.setYpos(currentYPosition);
			}
		}

        scoreList.setText(getScoreList());
	}

	public void updatePlayerPoints(String playerName, int point) {
		for (Player player : players) {
			if (player.name.equals(playerName)) {
				player.addPoints(point);
			}
		}
		scoreList.setText(getScoreList());
	}

	public String getScoreList() {
		StringBuffer buffer = new StringBuffer(100);

		for (Player player : players) {
			buffer.append(player+"\r\n");
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
	
}