package models;

public class Player {
	private String name;
	private int xpos;
	private int ypos;
	private int point;
	private String direction;
	private String color;

	public Player(String name, int xpos, int ypos, String direction, String color) {
		this.name = name;
		this.xpos = xpos;
		this.ypos = ypos;
		this.direction = direction;
		this.point = 0;
		this.color = color;
	}

	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getXpos() {
		return xpos;
	}
	public void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return ypos;
	}
	public void setYpos(int ypos) {
		this.ypos = ypos;
	}

	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}

	public int getPoint() {
		return point;
	}
	public void addPoints(int p) {
		point+=p;
	}

	public String toString() {
		return name+":   "+point;
	}
}
