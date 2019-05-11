package solved.klotski;

public class KlotskiNodeHashMap {
    /**
     * 盘面编码类
     */
    private KlotskiNodeStatusCode klotskiNodeStatusCode;
    /**
     * 已生成的布局
     */
    private KlotskiNode[] klotskiNodes;

    public KlotskiNodeHashMap(int[] status) {
        klotskiNodeStatusCode = new KlotskiNodeStatusCode(status);
        klotskiNodes = new KlotskiNode[klotskiNodeStatusCode.getTotal()];
    }

    public KlotskiNode put(KlotskiNode klotskiNode) {
        int[] status = klotskiNode.getStatus();
        int statusCode = klotskiNodeStatusCode.statusCoding(status);
        if (klotskiNodes[statusCode] != null) {
            return klotskiNodes[statusCode];
        }
        int mirrorSymmetryStatusCode = klotskiNodeStatusCode.mirrorSymmetryStatusCoding(status);
        if (klotskiNodes[mirrorSymmetryStatusCode] != null) {
            return klotskiNodes[mirrorSymmetryStatusCode];
        }
        klotskiNodes[statusCode] = klotskiNodes[mirrorSymmetryStatusCode] = klotskiNode;
        return null;
    }

}
