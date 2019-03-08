package solved.most_link_link;

public class Node {
	
	private int row, col, direction, directionCount; // directionCount累计节点改变方向次数

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getDirectionCount() {
		return directionCount;
	}

	public void setDirectionCount(int directionCount) {
		this.directionCount = directionCount;
	}

}
