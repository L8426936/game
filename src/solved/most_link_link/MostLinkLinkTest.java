package solved.most_link_link;

public class MostLinkLinkTest {
	
	public static void main(String[] args) {
		// ------------------------------------------ 测试使用 ----------------------------------------------------
		char[][] status = {
				{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EXIST, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN},
				{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY},
				{MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY},
				{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY},
				{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY}
		};
		MostLinkLinkUtil.printStatus(status);
		int count = 25, startRow = 0, startCol = 2;
		NodeTree nodeTree = new NodeTree(status, count, startRow, startCol);
		nodeTree.DFS();
		// --------------------------------------------------------------------------------------------------------
//		MostLinkLinkUtil.autoPlay();
	}
	
}
