public class MessageFormatter {

    public MessageFormatter() {}

    public String movePlayerMessage (String playerName, int xDirectionMove, int yDirectionMove, String nextPosition) {
        return String.format("move_player %s %d %d %s",
                playerName,
                xDirectionMove,
                yDirectionMove,
                nextPosition
        );
    }

    public String addPlayerMessage (String playerName, int currentXPosition, int currentYPosition, String currentPosition) {
        return String.format("add_player %s %d %d %s",
                playerName,
                currentXPosition,
                currentYPosition,
                currentPosition
        );
    }

    public String addNewPlayerMessage (String playerName, int defaultXPosition, int defaultYPosition) {
        return String.format("add_player %s %d %d up",
                playerName,
                defaultXPosition,
                defaultYPosition
        );
    }

    public String updatePlayerPoint (String playerName, int pointChange) {
        return String.format("update_player_points %s %d",
                playerName,
                pointChange
        );
    }

}
