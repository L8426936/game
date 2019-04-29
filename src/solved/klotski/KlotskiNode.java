package solved.klotski;

public class KlotskiNode {
    private KlotskiNode parent;
    private int[] status;

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
}
