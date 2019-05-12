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
        if (klotskiNodes[key] != null) {
            return klotskiNodes[key];
        }
        klotskiNodes[key] = value;
        return null;
    }

    public KlotskiNode get(int key) {
        return klotskiNodes[key];
    }
}
