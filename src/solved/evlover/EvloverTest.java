package solved.evlover;

public class EvloverTest {
    public static void main(String[] args) {
        int[] startStatus = {
                   0,0,0,0,
                  1,0,0,0,1,
                 0,0,0,0,0,0,
                0,0,0,1,0,0,0,
                 0,1,0,0,1,0,
                  0,0,1,0,0,
                   0,0,0,0
        };
        int[] endStatus = {
                   0,0,0,0,
                  0,0,0,0,0,
                 0,0,1,1,0,0,
                0,0,0,1,0,0,0,
                 0,0,1,0,0,0,
                  0,1,1,0,0,
                   0,0,0,0
        };
        EvloverNodeTree evloverNodeTree = new EvloverNodeTree(startStatus, endStatus);
        long millis = System.currentTimeMillis();
        EvloverNode[] evloverNodes = evloverNodeTree.bidirectionalBreadthFirstSearch();
        System.out.format("搜索时间: %d%n", System.currentTimeMillis() - millis);
        for (int i = 0; i < evloverNodes.length; i++) {
            EvloverNode evloverNode = evloverNodes[i];
            System.out.format("step:%d x:%d y:%d z:%d action:%c%n", i + 1, evloverNode.getX(), evloverNode.getY(), evloverNode.getZ(), evloverNode.getAction());
            EvloverNodeUtil.printlnStatus(3, evloverNode.getStatus());
            System.out.println();
        }

        // AutoPlay.play(3);
    }
}