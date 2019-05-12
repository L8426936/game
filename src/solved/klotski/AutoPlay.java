package solved.klotski;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AutoPlay {
    private final static String[][] HASH_CODE = {
            // 兵
            {"3878EEDACB8BBEA1", "387CCEDACA8BBEA1", "387CCADACA8BBEA1", "387CEADACA8BBEA1", "3878D6DACBC9BE81",
                    "3858D2DACBC9BE81", "3878D6DACB89BEA1", "387CD2DACBC9BEA1"
            },
            // 横将
            {"F9FAFB71238283C1", "030200E1F7F5F3FB", "0F0C1199E3E2F7E7", "F7F2F9F8FB720B09", "F9F9FBF3E3E1E0C0"},
            // 竖将
            {"C793BB9CB05C2E13", "7F7FFBB5980182AE", "EFF72707278DCDDB", "6EB79B075842073B", "DB91ADA1986E7E2A"},
            // 曹操
            {"E082810E04008FCF"}
    };
    private final static Runtime runtime = Runtime.getRuntime();

    public static void play(int moveMode) {
        try {
            Path path = Paths.get("G:", "klotski", "klotski.png");
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            int oldStatusHashCode = 0, currentStatusHashCode = 0;
            do {
                screenshot();
                long startTime = System.currentTimeMillis();
                long flagTime = startTime;
                int[] status = analysisStatus(path);
                System.out.format("分析完成，耗时%dms%n", System.currentTimeMillis() - flagTime);
                KlotskiNodeUtil.printlnStatus(status);
                KlotskiNodeTree klotskiNodeTree = new KlotskiNodeTree(status, moveMode);
                oldStatusHashCode = currentStatusHashCode;
                currentStatusHashCode = klotskiNodeTree.statusCoding(status);
                flagTime = System.currentTimeMillis();
                KlotskiNode[] klotskiNodes = klotskiNodeTree.BFS();
                System.out.format("搜索完成，耗时%dms%n", System.currentTimeMillis() - flagTime);
                if (klotskiNodes != null) {
                    flagTime = System.currentTimeMillis();
                    move(klotskiNodes);
                    System.out.format("闯关完成，耗时%dms%n", System.currentTimeMillis() - flagTime);
                    next();
                }
                System.out.format("全部步骤完成，总耗时%dms%n%n", System.currentTimeMillis() - flagTime);
            } while (oldStatusHashCode != currentStatusHashCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 截屏到电脑
     * @throws Exception
     */
    private static void screenshot() throws Exception {
        runtime.exec("adb shell screencap /sdcard/klotski.png").waitFor();
        runtime.exec("adb pull /sdcard/klotski.png G:/klotski").waitFor();
    }

    /**
     * 分析棋盘
     * @param path 图片路径
     * @return 返回盘面
     * @throws Exception
     */
    private static int[] analysisStatus(Path path) throws Exception {
        String[] hashCode = imageHashCode(path);
        int[] status = new int[20];
        int soldier = 1, horizontal = 5, vertical = 10;
        for (int i = 0; i < hashCode.length; i++) {
            if (status[i] == 0) {
                char charType = hanMingDifference(hashCode[i]);
                switch (charType) {
                    case KlotskiNodeUtil.S:
                        status[i] = soldier++;
                        break;
                    case KlotskiNodeUtil.H:
                        status[i] = status[i + 1] = horizontal++;
                        break;
                    case KlotskiNodeUtil.V:
                        status[i] = status[i + 4] = vertical++;
                        break;
                    case KlotskiNodeUtil.T:
                        status[i] = status[i + 1] = status[i + 4] = status[i + 5] = 15;
                        break;
                    default:
                }
            }
        }
        return status;
    }

    private final static char[] INDEX_TO_CHAR_TYPE = {KlotskiNodeUtil.E, KlotskiNodeUtil.S,
            KlotskiNodeUtil.H, KlotskiNodeUtil.V, KlotskiNodeUtil.T};
    /**
     * 汉明距离
     * @param hashCode
     * @return
     */
    private static char hanMingDifference(String hashCode) {
        int difference = 16, index = 0;
        for (int i = 0; i < HASH_CODE.length; i++) {
            for (int j = 0; j < HASH_CODE[i].length; j++) {
                int count = 0;
                for (int k = 0; k < HASH_CODE[i][j].length(); k++) {
                    if (HASH_CODE[i][j].charAt(k) != hashCode.charAt(k)) {
                        count++;
                    }
                }
                // 取最近接近的棋子类型
                if (count < 10 && count < difference) {
                    index = i + 1;
                    difference = count;
                }
            }
        }
        return INDEX_TO_CHAR_TYPE[index];
    }

    private final static int X_START = 128, X_OFFSET = 206, Y_START = 372, Y_OFFSET = 205, X_PART_SIZE = 190, Y_PART_SIZE = 184;
    private static String[] imageHashCode(Path path) throws Exception {
        String[] imageHashCode = new String[20];
        BufferedImage image = ImageIO.read(path.toFile());
        BufferedImage part = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        int[] rgb = new int[part.getWidth() * part.getHeight()];
        Graphics graphics = part.getGraphics();
        for (int y = Y_START, row = 0; row < 5;  y += Y_OFFSET, row++) {
            for (int x = X_START, col = 0; col < 4; x += X_OFFSET, col++) {
                graphics.drawImage(image.getSubimage(x, y, X_PART_SIZE, Y_PART_SIZE), 0, 0, part.getWidth(), part.getHeight(), null);
                part.getRGB(0, 0, part.getWidth(), part.getHeight(), rgb, 0, part.getWidth());
                imageHashCode[row * 4 + col] = KlotskiNodeUtil.binariesToHex(rgbToBinariesHashCode(rgb));
                // image.getGraphics().drawRect(x, y, X_PART_SIZE, Y_PART_SIZE);
            }
        }
        graphics.dispose();
        // ImageIO.write(image, "png", Paths.get(path.getParent().toString(), "klotski_rect.png").toFile());
        return imageHashCode;
    }

    private static int[] rgbToBinariesHashCode(int[] rgb) {
        int[] hashCode = new int[rgb.length];
        double average = KlotskiNodeUtil.average(rgb);
        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] >= average) {
                hashCode[i] = 1;
            }
        }
        return hashCode;
    }

    private final static int X_START_MOVE = 200, Y_START_MOVE = 460, MOVE_OFFSET = 205;
    /**
     * 移动棋子
     * @param klotskiNodes
     * @throws Exception
     */
    private static void move(KlotskiNode[] klotskiNodes) throws Exception {
        for (int i = 0; i < klotskiNodes.length - 1; i++) {
            KlotskiNode klotskiNode = klotskiNodes[i];
            int srcCol = KlotskiNodeUtil.COL[klotskiNode.getSrc()], srcRow = KlotskiNodeUtil.ROW[klotskiNode.getSrc()],
                    destCol = KlotskiNodeUtil.COL[klotskiNode.getDest()], destRow = KlotskiNodeUtil.ROW[klotskiNode.getDest()];
            if (srcCol == destCol || srcRow == destRow) {
                String command = String.format("adb shell input swipe %d %d %d %d", X_START_MOVE + MOVE_OFFSET * srcCol,
                        Y_START_MOVE + MOVE_OFFSET * srcRow, X_START_MOVE + MOVE_OFFSET * destCol,
                        Y_START_MOVE + MOVE_OFFSET * destRow);
                runtime.exec(command).waitFor();
            } else {
                int[] status = klotskiNode.getStatus();
                int middleCol = 0, middleRow = 0;
                if (srcCol > destCol) {
                    middleCol = status[klotskiNode.getSrc() - 1] == 0 ? destCol : srcCol;
                } else {
                    middleCol = status[klotskiNode.getSrc() + 1] == 0 ? destCol : srcCol;
                }
                if (srcRow > destRow) {
                    middleRow = status[klotskiNode.getSrc() - 4] == 0 ? destRow : srcRow;
                } else {
                    middleRow = status[klotskiNode.getSrc() + 4] == 0 ? destRow : srcRow;
                }
                String middle = String.format("adb shell input swipe %d %d %d %d", X_START_MOVE + MOVE_OFFSET * srcCol,
                        Y_START_MOVE + MOVE_OFFSET * srcRow, X_START_MOVE + MOVE_OFFSET * middleCol,
                        Y_START_MOVE + MOVE_OFFSET * middleRow);
                runtime.exec(middle).waitFor();
                String command = String.format("adb shell input swipe %d %d %d %d", X_START_MOVE + MOVE_OFFSET * middleCol,
                        Y_START_MOVE + MOVE_OFFSET * middleRow, X_START_MOVE + MOVE_OFFSET * destCol,
                        Y_START_MOVE + MOVE_OFFSET * destRow);
                runtime.exec(command).waitFor();
            }
        }
        KlotskiNode klotskiNode = klotskiNodes[klotskiNodes.length - 1];
        int srcCol = KlotskiNodeUtil.COL[klotskiNode.getSrc()], srcRow = KlotskiNodeUtil.ROW[klotskiNode.getSrc()],
                destCol = KlotskiNodeUtil.COL[klotskiNode.getDest()], destRow = KlotskiNodeUtil.ROW[klotskiNode.getDest()];
        if (srcCol == destCol) {
            destRow += 1;
            String command = String.format("adb shell input swipe %d %d %d %d", X_START_MOVE + MOVE_OFFSET * srcCol,
                    Y_START_MOVE + MOVE_OFFSET * srcRow, X_START_MOVE + MOVE_OFFSET * destCol,
                    Y_START_MOVE + MOVE_OFFSET * destRow);
            runtime.exec(command).waitFor();
        } else {
            String command = String.format("adb shell input swipe %d %d %d %d", X_START_MOVE + MOVE_OFFSET * srcCol,
                    Y_START_MOVE + MOVE_OFFSET * srcRow, X_START_MOVE + MOVE_OFFSET * destCol,
                    Y_START_MOVE + MOVE_OFFSET * destRow);
            runtime.exec(command).waitFor();
            String lastStep = String.format("adb shell input swipe %d %d %d %d", X_START_MOVE + MOVE_OFFSET * destCol,
                    Y_START_MOVE + MOVE_OFFSET * destRow, X_START_MOVE + MOVE_OFFSET * destCol,
                    Y_START_MOVE + MOVE_OFFSET * (destRow + 1));
            runtime.exec(lastStep).waitFor();
        }
    }

    /**
     * 下一关
     * @throws Exception
     */
    private static void next() throws Exception {
        // 下一关
        runtime.exec("adb shell input tap 800 950").waitFor();
        Thread.sleep(500);
    }

}
