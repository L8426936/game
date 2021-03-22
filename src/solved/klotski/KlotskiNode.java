package solved.klotski;

public class KlotskiNode {
    private KlotskiNode parent;
    private long status;
    private int srcDest;

    public KlotskiNode getParent() {
        return parent;
    }

    public void setParent(KlotskiNode parent) {
        this.parent = parent;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public int getSrcRow() {
        return getSrc() / 12;
    }

    public int getSrcCol() {
        return (getSrc() % 12) / 3;
    }

    public int getDestRow() {
        return getDest() / 12;
    }

    public int getDestCol() {
        return (getDest() % 12) / 3;
    }

    public int getSrc() {
        return srcDest >>> 16;
    }

    public void setSrc(int src) {
        srcDest = (srcDest & 0XFFFF) | (src << 16);
    }

    public int getDest() {
        return srcDest & 0XFFFF;
    }

    public void setDest(int dest) {
        srcDest = (srcDest & 0XFFFF0000) | dest;
    }
}
