package solved.mostlinklink;

public class MostLinkLinkTest {
	
	public static void main(String[] args) throws Exception {
		// ------------------------------------------ 测试使用 ----------------------------------------------------
		// char[][] status = {
		// 		{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EXIST, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN},
		// 		{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY},
		// 		{MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY},
		// 		{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY},
		// 		{MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.BAN, MostLinkLinkUtil.EMPTY, MostLinkLinkUtil.EMPTY}
		// };
		// MostLinkLinkUtil.printStatus(status);
		// GameInfo gameInfo = new GameInfo();
		// gameInfo.setCount(25);
		// gameInfo.setStartRow(0);
		// gameInfo.setStartCol(2);
		// gameInfo.setStatus(status);
		// MostLinkLinkNodeTree nodeTree = new MostLinkLinkNodeTree(gameInfo);
		// nodeTree.DFS();
		// --------------------------------------------------------------------------------------------------------
		MostLinkLinkUtil.autoPlay(MostLinkLinkUtil.OPENCV_ANALYSIS, MostLinkLinkUtil.RANK_LIST);
	}
	
}
