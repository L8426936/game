package solved.klotski;

public class KlotskiTest {
    public static void main(String[] args) {
        int[] status = { 1,  2,  3,  5,
                        15, 15,  6,  5,
                        15, 15,  6,  7,
                         0, 10, 10,  7,
                         0,  4, 11, 11,
        };
        KlotskiNodeTree klotskiNodeTree = new KlotskiNodeTree(status);
        klotskiNodeTree.BFS();
    }

}
