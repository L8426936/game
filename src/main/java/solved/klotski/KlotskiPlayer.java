package solved.klotski;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KlotskiPlayer {
    private static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "solved", "klotski", "data");
    private static final Runtime runtime = Runtime.getRuntime();
    private static int MARGIN_LEFT = Integer.MAX_VALUE, MARGIN_TOP = Integer.MAX_VALUE, SIDE = Integer.MAX_VALUE;
    private static Rect nextRound;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void autoPlay(int mode) {
        KlotskiTree klotskiTree = new KlotskiTree();
        long oldStatus = 0;
        for (int equalStatusCount = 0; equalStatusCount < 5; equalStatusCount++) {
            try {
                long startTime = System.currentTimeMillis();
                long flagTime = startTime;
                long status = analysisStatus();
                if (oldStatus != status) {
                    oldStatus = status;
                    if (checkStatus(status)) {
                        System.out.format("截图分析耗时%dms%n", System.currentTimeMillis() - flagTime);
                        printlnStatus(status);
                        System.out.println();
                        flagTime = System.currentTimeMillis();
                        List<KlotskiNode> klotskiNodes = klotskiTree.BFS(status, mode);
                        System.out.format("搜索耗时%dms%n", System.currentTimeMillis() - flagTime);
                        if (klotskiNodes != null) {
                            flagTime = System.currentTimeMillis();
                            move(klotskiNodes);
                            System.out.format("闯关耗时%dms%n", System.currentTimeMillis() - flagTime);
                            next();
                            System.out.format("总耗时%dms%n%n", System.currentTimeMillis() - startTime);
                            equalStatusCount = 0;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    /**
     * 截屏到电脑
     * @throws Exception
     */
    private static void screenshot() throws Exception {
        runtime.exec("adb shell screencap /sdcard/klotski.png").waitFor();
        runtime.exec("adb pull /sdcard/klotski.png " + BASE_PATH.toString()).waitFor();
    }

    /**
     * 分析棋盘
     * @return 返回盘面
     */
    private static long analysisStatus() throws Exception {
        screenshot();
        Mat origin = Imgcodecs.imread(Paths.get(BASE_PATH.toString(), "klotski.png").toString());

        Mat hsv = new Mat();
        Imgproc.cvtColor(origin, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar lowHSV = new Scalar(5, 150, 110);
        Scalar highHSV = new Scalar(26, 190, 180);
        Core.inRange(hsv, lowHSV, highHSV, hsv);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(hsv, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        contours = contours.stream().sorted((matOfPoint1, matOfPoint2) -> (int) (Imgproc.boundingRect(matOfPoint2).area() - Imgproc.boundingRect(matOfPoint1).area())).limit(10).collect(Collectors.toList());

        Scalar color = new Scalar(0, 0, 255);
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            Imgproc.rectangle(origin, rect, color, Imgproc.LINE_4);

            if (rect.x < MARGIN_LEFT) {
                MARGIN_LEFT = rect.x;
            }
            if (rect.y < MARGIN_TOP) {
                MARGIN_TOP = rect.y;
            }
            if (SIDE > Math.min(rect.width, rect.height)) {
                SIDE = Math.min(rect.width, rect.height);
            }
        }

        long status = 0, horizontal = 0X6L, vertical = 0X2L;
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            int row = (rect.y - MARGIN_TOP) / SIDE, col = (rect.x - MARGIN_LEFT) / SIDE;
            int index = 3 * (4 * row + col);
            if (i == 0) {
                status |= (0X7L << index) | (0X7L << (index + 3)) | (0X7L << (index + 12)) | (0X7L << (index + 15));
            } else if (i >= 6) {
                status |= (0X1L << index);
            } else {
                if (rect.width > rect.height) {
                    status |= (horizontal << index) | (horizontal << (index + 3));
                    horizontal--;
                } else {
                    status |= (vertical << index) | (vertical << (index + 12));
                    vertical++;
                }
            }
        }

        Imgproc.resize(origin, origin, new Size(origin.width() * 0.4, origin.height() * 0.4));
        HighGui.imshow("图片分析结果", origin);

        HighGui.waitKey(1);
        return status;
    }

    /**
     * 移动棋子
     * @param klotskiNodes
     * @throws Exception
     */
    private static void move(List<KlotskiNode> klotskiNodes) throws Exception {
        for (int i = 1; i < klotskiNodes.size() - 1; i++) {
            KlotskiNode klotskiNode = klotskiNodes.get(i);
            printlnStatus(klotskiNode.getStatus());
            System.out.println();
            if (klotskiNode.getSrcRow() == klotskiNode.getDestRow() || klotskiNode.getSrcCol() == klotskiNode.getDestCol()) {
                String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2));
                runtime.exec(command).waitFor();
            } else {
                int middleRow = (klotskiNode.getSrcRow() + klotskiNode.getDestRow()) / 2;
                int middleCol = (klotskiNode.getSrcCol() + klotskiNode.getDestCol()) / 2;
                String middle = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * middleCol + (SIDE / 2),
                        MARGIN_TOP + SIDE * middleRow + (SIDE / 2));
                runtime.exec(middle).waitFor();
                String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * middleCol + (SIDE / 2),
                        MARGIN_TOP + SIDE * middleRow + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2));
                runtime.exec(command).waitFor();
            }
        }
        KlotskiNode klotskiNode = klotskiNodes.get(klotskiNodes.size() - 1);
        printlnStatus(klotskiNode.getStatus());
        System.out.println();
        if (klotskiNode.getSrcCol() == klotskiNode.getDestCol()) {
            String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                    MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                    MARGIN_TOP + SIDE * (klotskiNode.getDestRow() + 1) + (SIDE / 2));
            runtime.exec(command).waitFor();
        } else {
            String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                    MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                    MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2));
            runtime.exec(command).waitFor();
            String lastStep = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                    MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                    MARGIN_TOP + SIDE * (klotskiNode.getDestRow() + 1) + (SIDE / 2));
            runtime.exec(lastStep).waitFor();
        }
    }

    /**
     * 下一关
     * @throws Exception
     */
    private static void next() throws Exception {
        if (nextRound == null) {
            screenshot();
            nextRound = matchTemplate("nextRound.png");
        }
        if (nextRound != null) {
            runtime.exec(String.format("adb shell input tap %d %d", nextRound.x + (nextRound.width / 2), nextRound.y + (nextRound.height / 2))).waitFor();
            Thread.sleep(300);
        }
    }

    private static Rect matchTemplate(String templateName) {
        Mat image = Imgcodecs.imread(Paths.get(BASE_PATH.toString(), "klotski.png").toString(), Imgcodecs.IMREAD_GRAYSCALE);
        Mat template = Imgcodecs.imread(Paths.get(BASE_PATH.toString(), templateName).toString(), Imgcodecs.IMREAD_GRAYSCALE);

        Mat result = new Mat();
        Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);
        Point maxLoc = minMaxLocResult.maxLoc;
        if (minMaxLocResult.maxVal > 0.95) {
            return new Rect((int) maxLoc.x, (int) maxLoc.y, template.width(), template.height());
        }
        return null;
    }

    public static void printlnStatus(long status) {
        for (int row = 0; row <= 4; row++) {
            for (int col = 0; col <= 3; col++) {
                int index = 3 * (4 * row + col);
                System.out.print((status >> index) & 0X7);
                if (col < 3) {
                    System.out.print(' ');
                }
            }
            System.out.println();
        }
    }

    private static boolean checkStatus(long status) {
        for (int index = 0; index < 60; index += 3) {
            long type = (status >> index) & 0X7L;
            if (type == 0X1L) {
                status ^= (type << index);
            } else if (type >= 0X2L && type <= 0X6L) {
                if (type == ((status >> (index + 3)) & 0X7L)) {
                    status ^= (type << index) | (type << (index + 3));
                } else if (type == ((status >> (index + 12)) & 0X7L)) {
                    status ^= (type << index) | (type << (index + 12));
                }
            } else if (type == 0X7L) {
                status ^= (type << index) | (type << (index + 3)) | (type << (index + 12)) | (type << (index + 15));
            }
        }
        return status == 0;
    }

}
