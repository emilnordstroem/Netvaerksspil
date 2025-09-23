package client;

public class BoardPointer {
    private final int xPosition;
    private final int yPosition;

    public BoardPointer(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }
}
