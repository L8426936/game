package solved.evlover;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AutoPlay {

    private final static Runtime runtime = Runtime.getRuntime();
    private static int[] rowPreviousSum;

    public static void play(int layer) {
        rowPreviousSum = EvloverNodeUtil.rowPreviousSum(layer);
        try {
            Path path = Paths.get("G:", "evlover", "evlover.png");
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            while (true){
                screenshot();
                long startTime = System.currentTimeMillis();
                long flagTime = startTime;
                int[] startStatus = analysisStatus(layer, 360, 900, 60, 120, 105, path);
                EvloverNodeUtil.printlnStatus(layer, EvloverNodeUtil.binaryToLong(startStatus));
                System.out.format(String.format("%%%dc%%n", layer * 2 + 1), '↓');
                int[] endStatus = analysisStatus(layer, 395, 272, 44, 88, 77, path);
                EvloverNodeUtil.printlnStatus(layer, EvloverNodeUtil.binaryToLong(endStatus));
                System.out.format("分析完成，耗时%dms%n", System.currentTimeMillis() - flagTime);
                EvloverNodeTree evloverNodeTree = new EvloverNodeTree(startStatus, endStatus);
                EvloverNode[] evloverNodes = evloverNodeTree.bidirectionalBreadthFirstSearch();
                System.out.format("搜索完成，耗时%dms%n", System.currentTimeMillis() - flagTime);
                if (evloverNodes != null) {
                    move(layer, evloverNodes);
                    next();
                    System.out.format("全部步骤完成，总耗时%dms%n%n", System.currentTimeMillis() - flagTime);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void move(int layer, EvloverNode[] evloverNodes) throws Exception {
        for (int i = 0; i < evloverNodes.length; i++) {
            printlnStep(layer, i + 1, evloverNodes[i]);
        }
    }

    /**
     * 截屏到电脑
     * @throws Exception
     */
    private static void screenshot() throws Exception {
        runtime.exec("adb shell screencap /sdcard/evlover.png").waitFor();
        runtime.exec("adb pull /sdcard/evlover.png G:/evlover").waitFor();
    }

    /**
     * 分析盘面
     * @param layer 层数
     * @param startX
     * @param startY
     * @param addOffsetX
     * @param offsetY
     * @param path 图片路径
     * @return
     * @throws Exception
     */
    private static int[] analysisStatus(int layer, int startX, int startY, int subOffsetX, int addOffsetX, int offsetY, Path path) throws Exception {
        BufferedImage image = ImageIO.read(path.toFile());
        // Graphics graphics = image.getGraphics();
        // graphics.setColor(Color.RED);
        int[] status = new int[3 * layer * (layer + 1) + 1];
        for (int row = 0, col = layer + 1, offset = 1, totalRow = layer * 2 + 1, x = startX, y = startY, index = 0;
             row < totalRow; row++, col += offset, x += subOffsetX * -offset, y += offsetY) {
            if (row >= layer) {
                offset = -1;
            }
            for (int c = 0; c < col; c++) {
                int rgb = image.getRGB(addOffsetX * c + x, y);
                // graphics.drawString("a", addOffsetX * c + x, y);
                if (rgb <= -16711423) {
                    status[index] = 1;
                }
                index++;
            }
        }
        // ImageIO.write(image, "png", Paths.get("G:", "evlover", "evlover_check.png").toFile());
        // graphics.dispose();
        return status;
    }

    private static void printlnStep(int layer, int step, EvloverNode evloverNode) throws Exception {
        System.out.format("step: %d action: %c%n", step, evloverNode.getAction());
        int clickRow = layer + evloverNode.getZ();
        int clickCol = clickRow <= layer ? layer - evloverNode.getY() : layer + evloverNode.getX();
        for (int row = 0, totalRow = layer * 2 + 1, offset = 1, col = layer + 1;
             row < totalRow; row++, col += offset) {
            if (row >= layer) {
                offset = -1;
            }

            for (int k = layer * 2 + 1 - col; k > 0; k--) {
                System.out.print(' ');
            }

            long longStatus = evloverNode.getStatus();
            for (int k = 0; k < col; k++) {
                long value = longStatus & EvloverNodeUtil.BINARY_VALUE[rowPreviousSum[row] + k];
                if (clickRow == row && clickCol == k) {
                    System.out.format("%c ", '◉');
                } else if (value != 0) {
                    System.out.format("%c ", '●');
                } else {
                    System.out.format("%c ", '○');
                }
            }
            System.out.println();
        }
        click(layer, clickRow, clickCol, evloverNode.getAction());
    }

    private static void click(int layer, int clickRow, int clickCol, char action) throws Exception {
        switch (action) {
            case 'L':
                runtime.exec("adb shell input tap 230 1785").waitFor();
                break;
            case 'C':
                runtime.exec("adb shell input tap 540 1785").waitFor();
                break;
            case 'R':
                runtime.exec("adb shell input tap 850 1785").waitFor();
                break;
            default:
        }
        String command = String.format("adb shell input tap %d %d",
                (((layer > clickRow ? layer - clickRow : clickRow - layer) * 60) + clickCol * 120) + 180,
                (clickRow * 105) + 900);
        runtime.exec(command).waitFor();
    }

    private static void next() throws Exception {
        Thread.sleep(1500);
        runtime.exec("adb shell input tap 700 1500").waitFor();
        Thread.sleep(2000);
    }
}
