package solved.klotski;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import nu.pattern.OpenCV;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KlotskiPlayer {
    private static final String DATA_PATH = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "solved", "klotski", "data").toString() + File.separator;
    private static final Runtime runtime = Runtime.getRuntime();
    private static final int WM_MOUSEMOVE = 0x200;
    private static final int WM_LBUTTONDOWN = 0x201;
    private static final int WM_LBUTTONUP = 0x202;
    private static int MARGIN_LEFT = Integer.MAX_VALUE, MARGIN_TOP = Integer.MAX_VALUE, SIDE = Integer.MAX_VALUE;
    private static Rect nextRound;

    static {
        OpenCV.loadShared();
    }

    public static void autoPlayOnMobile(int mode) {
        KlotskiTree klotskiTree = new KlotskiTree();
        long oldStatus = 0;
        for (int equalStatusCount = 0; equalStatusCount < 5; equalStatusCount++) {
            try {
                long startTime = System.currentTimeMillis();
                long flagTime = startTime;
                long status = analysisStatus(null);
                if (oldStatus != status) {
                    equalStatusCount = 0;
                }
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
                        move(klotskiNodes, null);
                        System.out.format("闯关耗时%dms%n", System.currentTimeMillis() - flagTime);
                        next(null);
                        System.out.format("总耗时%dms%n%n", System.currentTimeMillis() - startTime);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public static void autoPlayOnPC(int mode) {
        User32 instance = User32.INSTANCE;
        instance.EnumChildWindows(instance.FindWindow(null, "经典三国华容道"), (window, data) -> {
            if (window != null) {
                KlotskiTree klotskiTree = new KlotskiTree();
                long oldStatus = 0;
                for (int equalStatusCount = 0; equalStatusCount < 5; equalStatusCount++) {
                    try {
                        long startTime = System.currentTimeMillis();
                        long flagTime = startTime;
                        long status = analysisStatus(window);
                        if (oldStatus != status) {
                            equalStatusCount = 0;
                        }
                        oldStatus = status;
                        if (checkStatus(status)) {
                            System.out.format("截图分析耗时%dms%n", System.currentTimeMillis() - flagTime);
                            printlnStatus(status);
                            System.out.println();
                            flagTime = System.currentTimeMillis();
                            List<KlotskiNode> passPath = klotskiTree.BFS(status, mode);
                            System.out.format("搜索耗时%dms%n", System.currentTimeMillis() - flagTime);
                            if (passPath != null) {
                                flagTime = System.currentTimeMillis();
                                move(passPath, window);
                                System.out.format("闯关耗时%dms%n", System.currentTimeMillis() - flagTime);
                                next(window);
                                System.out.format("总耗时%dms%n%n", System.currentTimeMillis() - startTime);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.exit(0);
            return false;
        }, Pointer.NULL);
    }

    /**
     * 截屏到电脑
     * @throws Exception
     */
    private static void screenshot() throws Exception {
        runtime.exec("adb shell screencap /sdcard/klotski.png").waitFor();
        runtime.exec("adb pull /sdcard/klotski.png " + DATA_PATH).waitFor();
    }

    /**
     * 分析棋盘
     * @return 返回盘面
     */
    private static long analysisStatus(WinDef.HWND window) throws Exception {
        if (window == null) {
            screenshot();
        } else {
            ImageIO.write(GDI32Util.getScreenshot(window), "png", Paths.get(DATA_PATH, "klotski.png").toFile());
        }
        Mat origin = Imgcodecs.imread(DATA_PATH + "klotski.png");

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

        if (window == null) {
            Imgproc.resize(origin, origin, new Size(origin.width() * 0.4, origin.height() * 0.4));
        } else {
            // 显示器缩放百分比
            AffineTransform defaultTransform = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
            double scaleX = defaultTransform.getScaleX();
            double scaleY = defaultTransform.getScaleY();
            Imgproc.resize(origin, origin, new Size(origin.width() / scaleX, origin.height() / scaleY));
        }
        HighGui.imshow("图片分析结果", origin);

        HighGui.waitKey(1);
        return status;
    }

    /**
     * 移动棋子
     * @param passPath
     * @param window
     * @throws Exception
     */
    private static void move(List<KlotskiNode> passPath, WinDef.HWND window) throws Exception {
        User32 instance = User32.INSTANCE;
        for (int i = 1; i < passPath.size() - 1; i++) {
            KlotskiNode klotskiNode = passPath.get(i);
            printlnStatus(klotskiNode.getStatus());
            System.out.println();
            if (klotskiNode.getSrcRow() == klotskiNode.getDestRow() || klotskiNode.getSrcCol() == klotskiNode.getDestCol()) {
                if (window == null) {
                    String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                            MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                            MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2));
                    runtime.exec(command).waitFor();
                } else {
                    // (x & 0xffff) | (y << 16)
                    long srcPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2)) << 16);
                    long destPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2)) << 16);
                    instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(srcPosition));
                    instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(destPosition));
                    instance.SendMessage(window, WM_LBUTTONUP, null, null);
                    Thread.sleep(200);
                }
            } else {
                int middleRow = (klotskiNode.getSrcRow() + klotskiNode.getDestRow()) / 2;
                int middleCol = (klotskiNode.getSrcCol() + klotskiNode.getDestCol()) / 2;
                if (window == null) {
                    String middle = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                            MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * middleCol + (SIDE / 2),
                            MARGIN_TOP + SIDE * middleRow + (SIDE / 2));
                    runtime.exec(middle).waitFor();
                    String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * middleCol + (SIDE / 2),
                            MARGIN_TOP + SIDE * middleRow + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                            MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2));
                    runtime.exec(command).waitFor();
                } else {
                    // (x & 0xffff) | (y << 16)
                    long srcPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2)) << 16);
                    long middlePosition = ((MARGIN_LEFT + SIDE * middleCol + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * middleRow + (SIDE / 2)) << 16);
                    long destPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2)) << 16);

                    instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(srcPosition));
                    instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(middlePosition));
                    instance.SendMessage(window, WM_LBUTTONUP, null, null);
                    Thread.sleep(200);

                    instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(middlePosition));
                    instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(destPosition));
                    instance.SendMessage(window, WM_LBUTTONUP, null, null);
                    Thread.sleep(200);
                }
            }
        }
        KlotskiNode klotskiNode = passPath.get(passPath.size() - 1);
        printlnStatus(klotskiNode.getStatus());
        System.out.println();
        if (klotskiNode.getSrcCol() == klotskiNode.getDestCol()) {
            if (window == null) {
                String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * (klotskiNode.getDestRow() + 1) + (SIDE / 2));
                runtime.exec(command).waitFor();
            } else {
                // (x & 0xffff) | (y << 16)
                long srcPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2)) << 16);
                long destPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * (klotskiNode.getDestRow() + 1) + (SIDE / 2)) << 16);
                instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(srcPosition));
                instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(destPosition));
                instance.SendMessage(window, WM_LBUTTONUP, null, null);
                Thread.sleep(200);
            }
        } else {
            if (window == null) {
                String command = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2));
                runtime.exec(command).waitFor();
                String lastStep = String.format("adb shell input swipe %d %d %d %d 120", MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2), MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2),
                        MARGIN_TOP + SIDE * (klotskiNode.getDestRow() + 1) + (SIDE / 2));
                runtime.exec(lastStep).waitFor();
            } else {
                // (x & 0xffff) | (y << 16)
                long srcPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getSrcCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * klotskiNode.getSrcRow() + (SIDE / 2)) << 16);
                long middlePosition = ((MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * klotskiNode.getDestRow() + (SIDE / 2)) << 16);
                long destPosition = ((MARGIN_LEFT + SIDE * klotskiNode.getDestCol() + (SIDE / 2)) & 0xffff) | ((MARGIN_TOP + SIDE * (klotskiNode.getDestRow() + 1) + (SIDE / 2)) << 16);

                instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(srcPosition));
                instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(middlePosition));
                instance.SendMessage(window, WM_LBUTTONUP, null, null);
                Thread.sleep(200);

                instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(middlePosition));
                instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(destPosition));
                instance.SendMessage(window, WM_LBUTTONUP, null, null);
                Thread.sleep(200);
            }
        }
    }

    /**
     * 下一关
     * @throws Exception
     */
    private static void next(WinDef.HWND window) throws Exception {
        if (nextRound == null) {
            if (window == null) {
                screenshot();
                nextRound = matchTemplate("mobileNextRound.png");
            } else {
                Thread.sleep(1500);
                ImageIO.write(GDI32Util.getScreenshot(window), "png", Paths.get(DATA_PATH, "klotski.png").toFile());
                nextRound = matchTemplate("PCNextRound.png");
            }
        }
        if (nextRound != null) {
            if (window == null) {
                runtime.exec(String.format("adb shell input tap %d %d", nextRound.x + (nextRound.width / 2), nextRound.y + (nextRound.height / 2))).waitFor();
                Thread.sleep(300);
            } else {
                Thread.sleep(1500);
                // ImageIO.write(GDI32Util.getScreenshot(window), "png", Paths.get(DATA_PATH, System.currentTimeMillis() + ".png").toFile());
                User32 instance = User32.INSTANCE;
                // (x & 0xffff) | (y << 16)
                long nextRoundPosition = ((nextRound.x + (nextRound.width / 2)) & 0xffff) | ((nextRound.y + (nextRound.height / 2)) << 16);
                instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(nextRoundPosition));
                instance.SendMessage(window, WM_LBUTTONUP, null, new WinDef.LPARAM(nextRoundPosition));
                Thread.sleep(1500);
            }
        }
    }

    private static Rect matchTemplate(String templateName) {
        Mat image = Imgcodecs.imread(DATA_PATH + "klotski.png", Imgcodecs.IMREAD_GRAYSCALE);
        Mat template = Imgcodecs.imread(DATA_PATH + templateName, Imgcodecs.IMREAD_GRAYSCALE);

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
