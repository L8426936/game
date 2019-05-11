package solved.klotski;

public class KlotskiNode {
    private KlotskiNode parent;
    private int[] status;
    private int src, dest;

    public KlotskiNode getParent() {
        return parent;
    }

    public void setParent(KlotskiNode parent) {
        this.parent = parent;
    }

    public int[] getStatus() {
        return status;
    }

    public void setStatus(int[] status) {
        this.status = status;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

}
