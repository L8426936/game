package solved.evlover;

public class EvloverNode {
    private EvloverNode parent;
    private long status;
    private int x;
    private int y;
    private int z;
    private char action;

    public EvloverNode getParent() {
        return parent;
    }

    public void setParent(EvloverNode parent) {
        this.parent = parent;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public char getAction() {
        return action;
    }

    public void setAction(char action) {
        this.action = action;
    }
}
