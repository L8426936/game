package solved.most_link_link;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MostLinkLinkUtil {


    public final static char BAN = ' ', EMPTY = '□', EXIST = '■';

    private final static String BASE_PATH = MostLinkLinkUtil.class.getResource("").getPath().replaceFirst("^/(.:/)", "$1");
    private final static Runtime runtime = Runtime.getRuntime();

    // 好友排行榜
    public static void autoPlayRankingList() {
        for (int tryAgainCount = 0; tryAgainCount < 15; tryAgainCount++) {
            try {
                long startTime = System.currentTimeMillis();
                screenshot();
                GameStatusInfo gameStatusInfo = analysisScreenshot();
                if (gameStatusInfo != null) {
                    MostLinkLinkNodeTree nodeTree = new MostLinkLinkNodeTree(gameStatusInfo.getStatus(), gameStatusInfo.getCount(), gameStatusInfo.getStartRow(), gameStatusInfo.getStartCol());
                    MostLinkLinkNode[] nodes = nodeTree.DFS();
                    if (nodes != null) {
                        play(nodes, gameStatusInfo);
                        nextGame(gameStatusInfo);
                        tryAgainCount = 0;
                    } else {
                        System.out.println("游戏搜索失败，重新开始");
                    }
                } else {
                    System.out.println("游戏分析失败，重新开始");
                }
                System.out.format("耗费总时长:%d毫秒%n", System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 挑战赛
    public static void autoPlayChallenge() {
        for (int tryAgainCount = 0; tryAgainCount < 15; tryAgainCount++) {
            try {
                long startTime = System.currentTimeMillis();
                screenshot();
                GameStatusInfo gameStatusInfo = analysisScreenshot();
                if (gameStatusInfo != null) {
                    MostLinkLinkNodeTree nodeTree = new MostLinkLinkNodeTree(gameStatusInfo.getStatus(), gameStatusInfo.getCount(), gameStatusInfo.getStartRow(), gameStatusInfo.getStartCol());
                    MostLinkLinkNode[] nodes = nodeTree.DFS();
                    if (nodes != null) {
                        play(nodes, gameStatusInfo);
                        tryAgainCount = 0;
                    } else {
                        System.out.println("游戏搜索失败，重新开始");
                    }
                } else {
                    System.out.println("游戏分析失败，重新开始");
                }
                System.out.format("耗费总时长:%d毫秒%n", System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 截屏到电脑
    private static void screenshot() throws Exception {
        runtime.exec("adb shell screencap /sdcard/most_link_link.png").waitFor();
        runtime.exec("adb pull /sdcard/most_link_link.png " + BASE_PATH).waitFor();
    }

    // 分析游戏状态
    private static GameStatusInfo analysisScreenshot() throws Exception {
        BufferedImage screenshot = ImageIO.read(new File(BASE_PATH + "most_link_link.png"));
        // Graphics graphics = screenshot.getGraphics();
        int height = screenshot.getHeight();
        int width = screenshot.getWidth();

        GameStatusInfo gameStatusInfo = new GameStatusInfo();
        gameStatusInfo.setWidth(width);
        gameStatusInfo.setHeight(height);

        int topBorderY = 0, bottomBorderY = 0, blockXSize = 0, blockSize = 0, blockSpace = 0, leftBorderX = width, rightBorderX = 0;
        analysisGameStatusInfoOver:
        for (int y = (int) (0.165 * height); y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (screenshot.getRGB(x, y) == -3355444) { // -3355444灰色
                    topBorderY = y;
                    // graphics.drawLine(0, topBorderY, width - 1, topBorderY);
                    // System.out.println("关卡上边界：" + topBorderY);

                    while (x + blockXSize < width && screenshot.getRGB(x + blockXSize, y) == -3355444) { // -3355444灰色
                        blockXSize++;
                    }
                    // System.out.println("关卡色块大小：" + blockXSize);
                    while (screenshot.getRGB(x, y + blockSize) == -3355444) { // -3355444灰色
                        blockSize++;
                    }
                    gameStatusInfo.setStartY(topBorderY + (blockSize >> 1));
                    gameStatusInfo.setBlockSize(blockSize);
                    // graphics.drawLine(0, topBorderY, width - 1, topBorderY);
                    // graphics.drawLine(0, topBorderY + (blockSize >> 1), width - 1, topBorderY + (blockSize >> 1));
                    // graphics.drawLine(0, topBorderY + blockSize, width - 1, topBorderY + blockSize);
                    // System.out.println("关卡色块大小：" + blockSize);

                    int borderX = x + (blockSize >> 1), borderY = y + (blockSize >> 1);
                    while (borderX < width && screenshot.getRGB(borderX, y) != -14472389) { // -14472389背景色
                        borderX++;
                    }
                    while (screenshot.getRGB(x, borderY) != -14472389) { // -14472389背景色
                        borderY++;
                    }
                    while ((borderX + blockSpace < width && screenshot.getRGB(borderX + blockSpace, y) != -3355444 || screenshot.getRGB(borderX - 1, y) == -3355444) // -3355444灰色
                            && screenshot.getRGB(x, borderY + blockSpace) != -3355444) { // -3355444灰色
                        blockSpace++;
                    }
                    gameStatusInfo.setBlockSpace(blockSpace);
                    // graphics.drawLine(0, topBorderY + blockSize, width - 1, topBorderY + blockSize);
                    // graphics.drawLine(0, topBorderY + blockSize + blockSpace, width - 1, topBorderY + blockSize + blockSpace);
                    // System.out.println("关卡色块间隔：" + blockSpace);

                    for (y = topBorderY + (blockSize >> 1); y < height; y += blockSize + blockSpace) {
                        boolean hasBlock = false;
                        for (x = 0; x < width; x++) {
                            if (screenshot.getRGB(x, y) == -3355444) { // -3355444灰色
                                if (x < leftBorderX) {
                                    leftBorderX = x;
                                    rightBorderX = width - x;
                                }
                                hasBlock = true;
                                break;
                            }
                        }
                        if (!hasBlock) {
                            break;
                        }
                    }
                    gameStatusInfo.setStartX(blockSize - blockXSize > (blockSize >> 3) ? leftBorderX + ((blockSize >> 1) - (blockSize - blockXSize)) : leftBorderX + (blockSize >> 1));
                    // gameStatusInfo.setStartX(leftBorderX + (blockSize >> 1));
                    // graphics.drawLine(leftBorderX, 0, leftBorderX, height - 1);
                    // graphics.drawLine(rightBorderX, 0, rightBorderX, height - 1);
                    // graphics.drawLine(leftBorderX, 0, leftBorderX, height - 1);
                    // graphics.drawLine(leftBorderX + (blockSize >> 1), 0, leftBorderX + (blockSize >> 1), height - 1);
                    // graphics.drawLine(leftBorderX + blockSize, 0, leftBorderX + blockSize, height - 1);
                    // System.out.println("关卡左右边界：" + leftBorderX + " " + rightBorderX);

                    while (y > topBorderY) {
                        boolean hasBlock = false;
                        for (x = leftBorderX + (blockSize >> 1); x < rightBorderX; x += blockSize + blockSpace) {
                            if (screenshot.getRGB(x, y) == -3355444) { // -3355444灰色
                                hasBlock = true;
                                break;
                            }
                        }
                        if (hasBlock) {
                            break;
                        }
                        bottomBorderY = --y;
                    }
                    // graphics.drawLine(0, bottomBorderY, width - 1, bottomBorderY);
                    // System.out.println("关卡下边界：" + bottomBorderY);
                    break analysisGameStatusInfoOver;
                }
            }
        }

        List<List<Character>> lists = new ArrayList<>();
        int startPoint = 0;
        for (int y = topBorderY + (blockSize >> 1), index = 0, row = 0; startPoint <= 1 && y < bottomBorderY; y += blockSpace + blockSize, row++) {
            List<Character> rowStatus = new ArrayList<>();
            for (int x = blockSize - blockXSize > (blockSize >> 3) ? leftBorderX + ((blockSize >> 1) - (blockSize - blockXSize)) : leftBorderX + (blockSize >> 1), col = 0; startPoint <= 1 && x < rightBorderX; x += blockSpace + blockSize, index++, col++) {
                int rgb = screenshot.getRGB(x, y);
                if (rgb == -3355444) { // -3355444灰色
                    rowStatus.add(EMPTY);
                    gameStatusInfo.setCount(gameStatusInfo.getCount() + 1);
                } else if (rgb != -14472389) { // -14472389背景色
                    rowStatus.add(EXIST);
                    gameStatusInfo.setStartRow(row);
                    gameStatusInfo.setStartCol(col);
                    gameStatusInfo.setCount(gameStatusInfo.getCount() + 1);
                    startPoint++;
                } else {
                    rowStatus.add(BAN);
                }
                // graphics.drawLine(x, y, x, y);
            }
            lists.add(rowStatus);
        }
        // ImageIO.write(screenshot, "png", new File(BASE_PATH + "most_link_link_test.png"));

        char[][] status = new char[lists.size()][];
        for (int i = 0; i < lists.size(); i++) {
            List<Character> rowStatus = lists.get(i);
            status[i] = new char[rowStatus.size()];
            for (int j = 0; j < rowStatus.size(); j++) {
                status[i][j] = rowStatus.get(j);
            }
        }
        gameStatusInfo.setStatus(status);
        printStatus(status);
        return startPoint == 1 ? gameStatusInfo : null;
    }

    // 触摸滑动灰格
    private static void play(MostLinkLinkNode[] nodes, GameStatusInfo gameStatusInfo) throws Exception {
        int startX = gameStatusInfo.getStartX();
        int startY = gameStatusInfo.getStartY();
        int offset = gameStatusInfo.getBlockSpace() + gameStatusInfo.getBlockSize();
        StringBuilder commands = new StringBuilder("cmd /c ");
        for (int i = 1; i < nodes.length; i++) {
            int j = i;
            while (j + 1 < nodes.length && (nodes[i].getRow() == nodes[j + 1].getRow() || nodes[i].getCol() == nodes[j + 1].getCol())) {
                j++;
            }
            String command = String.format("adb shell input swipe %d %d %d %d %d&&", nodes[i].getCol() * offset + startX, nodes[i].getRow() * offset + startY, nodes[j].getCol() * offset + startX, nodes[j].getRow() * offset + startY, (j - i) * 100);
            commands.append(command);
            i = j;
        }
        runtime.exec(commands.substring(0, commands.length() - 2)).waitFor();
        Thread.sleep(800);
    }

    // 下一关
    private static void nextGame(GameStatusInfo gameStatusInfo) throws Exception {
        int width = gameStatusInfo.getWidth();
        int height = gameStatusInfo.getHeight();
        runtime.exec(String.format("adb shell input tap %d %d", (int) (width * 0.86), (int) (height * 0.343))).waitFor(); // 关闭双倍奖励
        runtime.exec(String.format("adb shell input tap %d %d", (int) (width * 0.5), (int) (height * 0.625))).waitFor(); // 下一关
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

    private static class GameStatusInfo {
        private char[][] status;
        private int count, startRow, startCol, startX, startY, blockSize, blockSpace, width, height; // count方格数量

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

        public int getStartX() {
            return startX;
        }

        public void setStartX(int startX) {
            this.startX = startX;
        }

        public int getStartY() {
            return startY;
        }

        public void setStartY(int startY) {
            this.startY = startY;
        }

        public int getBlockSize() {
            return blockSize;
        }

        public void setBlockSize(int blockSize) {
            this.blockSize = blockSize;
        }

        public int getBlockSpace() {
            return blockSpace;
        }

        public void setBlockSpace(int blockSpace) {
            this.blockSpace = blockSpace;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
