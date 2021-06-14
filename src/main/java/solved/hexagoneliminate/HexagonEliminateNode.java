package solved.hexagoneliminate;

public class HexagonEliminateNode {
    private HexagonEliminateNode parent;
    private long status;
    private long shapePosition;
    private int shapeTypeIndex;
    private int score;

    public HexagonEliminateNode getParent() {
        return parent;
    }

    public void setParent(HexagonEliminateNode parent) {
        this.parent = parent;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getShapePosition() {
        return shapePosition;
    }

    public void setShapePosition(long shapePosition) {
        this.shapePosition = shapePosition;
    }

    public int getShapeTypeIndex() {
        return shapeTypeIndex;
    }

    public void setShapeTypeIndex(int shapeTypeIndex) {
        this.shapeTypeIndex = shapeTypeIndex;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
