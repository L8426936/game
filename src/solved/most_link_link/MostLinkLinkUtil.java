package solved.most_link_link;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class MostLinkLinkUtil {
	
	public final static char BAN = ' ', EMPTY = '□', EXIST = '■';
	
	private final static Runtime runtime = Runtime.getRuntime();
	
	private final static int startH = 420, startW = 210, offsetH = 130, offsetW = 130, rowSize = 7, colSize = 6;
	
	// 自动玩
	public static void autoPlay() {
		boolean proceed = true;
		try {
			if (Files.notExists(Paths.get("G:/link")))
				Files.createDirectory(Paths.get("G:/link"));
			while (proceed) {
				long startTime = System.currentTimeMillis();
				screenshot();
				GameStatus gameStatus = analysisScreenshot();
				if (gameStatus != null) {
					NodeTree nodeTree = new NodeTree(gameStatus.getStatus(), gameStatus.getCount(), gameStatus.getStartRow(), gameStatus.getStartCol());
					Node[] nodes = nodeTree.DFS();
					if (nodes != null) {
						play(nodes);
						nextGame();
					} else {
						System.out.println("游戏搜索失败，重新开始");
					}
				} else {
					System.out.println("游戏分析失败，重新开始");
				}
				System.out.format("耗费总时长:%d毫秒%n", System.currentTimeMillis() - startTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
			proceed = false;
		}
	}

	// 截屏到电脑
	private static void screenshot() throws Exception {
		runtime.exec("adb shell screencap /sdcard/most_link_link.png").waitFor();
		runtime.exec("adb pull /sdcard/most_link_link.png G:/link").waitFor();
	}

	// 分析游戏状态
	private static GameStatus analysisScreenshot() throws Exception {
		GameStatus gameStatus = new GameStatus();
		char[][] status = new char[rowSize][colSize];
		int count = 0, startPoint = 0;
		BufferedImage screenshot = ImageIO.read(new File("G:/link/most_link_link.png"));
//		Graphics graphics = screenshot.getGraphics();
//		graphics.setColor(Color.red);
//		graphics.setFont(new Font("华文行楷", Font.BOLD, 100));
		for (int h = startH, rowIndex = 0; rowIndex < status.length && startPoint <= 1; h += offsetH, rowIndex++) {
			for (int w = startW, colIndex = 0; colIndex < status[rowIndex].length && startPoint <= 1; w += offsetW, colIndex++) {
				int rgb = screenshot.getRGB(w, h);
//				graphics.drawString(".", w, h);
				if (rgb == -3355444) { // -3355444灰色
					status[rowIndex][colIndex] = EMPTY;
					count++;
				} else if (rgb != -14472389) { // -14472389背景色
					status[rowIndex][colIndex] = EXIST;
					count++;
					gameStatus.setStartRow(rowIndex);
					gameStatus.setStartCol(colIndex);
					startPoint++;
				} else {
					status[rowIndex][colIndex] = BAN;
				}
			}
		}
//		ImageIO.write(screenshot, "png", new FileOutputStream("G:/link/draw_point.png"));
		gameStatus.setCount(count);
		gameStatus.setStatus(status);
		printStatus(status);
		return startPoint == 1 ? gameStatus : null;
	}

	// 触摸滑动灰格
	private static void play(Node[] nodes) throws Exception {
		for (int i = 1; i < nodes.length; i++) {
			int j = i;
			while (j + 1 < nodes.length && (nodes[i].getRow() == nodes[j + 1].getRow() || nodes[i].getCol() == nodes[j + 1].getCol())) {
				j++;
			}
			String command = String.format("adb shell input swipe %d %d %d %d", nodes[i].getCol() * offsetW + startW, nodes[i].getRow() * offsetH + startH, nodes[j].getCol() * offsetW + startW, nodes[j].getRow() * offsetH + startH);
//			System.out.println(command);
			runtime.exec(command).waitFor();
			i = j;
		}
		Thread.sleep(300);
	}
	
	// 下一关
	private static void nextGame() throws Exception {
		runtime.exec("adb shell input tap 929 660").waitFor(); // 关闭双倍奖励
		runtime.exec("adb shell input tap 540 1420").waitFor(); // 下一关
	}

	// 打印盘面状态
	public static void printStatus(char[][] status) {
		for (int row = 0; row < status.length; row++) {
			for (int col = 0; col < status[row].length - 1; col++) {
				System.out.format("%c ", status[row][col]);
			}
			System.out.format("%c%n", status[row][status[row].length - 1]);
		}
	}
	
	private static class GameStatus {
		private char[][] status;
		private int count, startRow, startCol; // count方格数量
		public char[][] getStatus() {
			return status;
		}
		public void setStatus(char[][] status) {
			this.status = status;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public int getStartRow() {
			return startRow;
		}
		public void setStartRow(int startRow) {
			this.startRow = startRow;
		}
		public int getStartCol() {
			return startCol;
		}
		public void setStartCol(int startCol) {
			this.startCol = startCol;
		}
	}
}
