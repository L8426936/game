package solved.mostlinklink;

public class MostLinkLinkTest {
	
	public static void main(String[] args) throws Exception {
		// ------------------------------------------ 测试使用 ----------------------------------------------------
		// char[][] status = {
		// 		{MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EXIST, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.BAN},
		// 		{MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY},
		// 		{MostLinkLinkPlayer.BAN, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.BAN, MostLinkLinkPlayer.EMPTY},
		// 		{MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.BAN, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY},
		// 		{MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.BAN, MostLinkLinkPlayer.EMPTY, MostLinkLinkPlayer.EMPTY}
		// };
		// MostLinkLinkPlayer.printStatus(status);
		// GameInfo gameInfo = new GameInfo();
		// gameInfo.setCount(25);
		// gameInfo.setStartRow(0);
		// gameInfo.setStartCol(2);
		// gameInfo.setStatus(status);
		// MostLinkLinkTree mostLinkLinkTree = new MostLinkLinkTree(gameInfo);
		// mostLinkLinkTree.DFS();
		// --------------------------------------------------------------------------------------------------------
		MostLinkLinkPlayer.autoPlay(MostLinkLinkPlayer.OPENCV_ANALYSIS, MostLinkLinkPlayer.RANK_LIST);
	}
	
}
