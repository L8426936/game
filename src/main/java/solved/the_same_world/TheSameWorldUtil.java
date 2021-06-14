package solved.the_same_world;

public class TheSameWorldUtil {
	public final static char BAN = ' ', EMPTY = '○', EXIST = '●';
	// 盘面值
	public static long getStatusValue(int[][] status, int flag) {
		long value = 0;
		for (int i = 0; i < status.length; i++) {
			for (int j = 0; j < status[i].length; j++) {
				if (status[i][j] == flag) {
					int index = i * status[i].length + j;
					value ^= (long) Math.pow(2, index);
				}
			}
		}
		return value;
	}
	// 打印通关路径
	public static void printPath(int[][] status, TheSameWorldNode[] solvedNodes) {
		for (int i = 0; i < solvedNodes.length; i++) {
			char[][] singlePath = new char[status.length][];
			for (int j = 0; j < status.length; j++) {
				singlePath[j] = new char[status[j].length];
				for (int k = 0; k < singlePath[j].length; k++) {
					if (status[j][k] == 0) {
						singlePath[j][k] = BAN;
					} else {
						singlePath[j][k] = EMPTY;
					}
				}
			}
			TheSameWorldNode node = solvedNodes[i];
			while (node != null) {
				singlePath[node.getRow()][node.getCol()] = EXIST;
				node = node.getParent();
			}
			for (int j = 0; j < singlePath.length; j++) {
				for (int k = 0; k < singlePath[j].length - 1; k++) {
					System.out.format("%c ", singlePath[j][k]);
				}
				System.out.format("%c%n", singlePath[j][singlePath[j].length - 1]);
			}
			System.out.println();
		}
	}
}
