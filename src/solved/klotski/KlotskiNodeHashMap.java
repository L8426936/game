package solved.klotski;

public class KlotskiNodeHashMap {
    /**
     * 已生成的布局
     */
    private KlotskiNode[] klotskiNodes;

    public KlotskiNodeHashMap(int total) {
        klotskiNodes = new KlotskiNode[total];
    }

    public KlotskiNode put(int key, KlotskiNode value) {
        KlotskiNode klotskiNode = klotskiNodes[key];
        klotskiNodes[key] = value;
        return klotskiNode;
    }

    public KlotskiNode get(int key) {
        return klotskiNodes[key];
    }
}
