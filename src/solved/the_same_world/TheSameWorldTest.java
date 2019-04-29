package solved.the_same_world;

public class TheSameWorldTest {

	public static void main(String[] args) {
		int[][] status = {
				{0, -1, -1, -1, -1, 0},
				{-1, 1, 1, 1, 1, -1},
				{-1, 0, 1, 1, 0, -1},
				{-1, 0, 1, 1, 0, -1},
				{-1, 0, 1, 1, 0, -1},
				{-1, 1, -1, -1, 1, -1},
				{0, -1, -1, -1, -1, 0}
		};
		int[][] startPoints = {
				{5, 0},
				{5, 5}
		};
		MostLinkLinkNodeTree nodeTree = new MostLinkLinkNodeTree(status, startPoints, TheSameWorldUtil.getStatusValue(status, 1));
		nodeTree.search(true);
	}

}