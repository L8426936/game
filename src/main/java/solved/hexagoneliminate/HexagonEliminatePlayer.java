package solved.hexagoneliminate;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HexagonEliminatePlayer {

    private static final long[] SHAPE_TYPE_HASH = new long[25];

    private static final String DATA_PATH = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "solved", "hexagoneliminate", "data").toString() + File.separator;
    private static final Runtime runtime = Runtime.getRuntime();

    static {
        OpenCV.loadShared();

        // 制作template图片的hash值
        try {
            Files.list(Paths.get(DATA_PATH + "template")).filter(path -> path.toString().endsWith(".jpg") || path.toString().endsWith(".png")).forEach(path -> {
                long hash = shapeHash(Imgcodecs.imread(path.toString()));
                String fileName = path.getName(path.getNameCount() - 1).toString();
                SHAPE_TYPE_HASH[Integer.parseInt(fileName.substring(0, fileName.lastIndexOf('.')))] = hash;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void autoPlayOnMobile() {
        try {
            screenshot();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String winname = "按指示完成操作后，按下任意键";
        Rect[] statusRect = analysisStatusRect();
        long oldStatus = 0;
        for (int failCount = 0; failCount < 15; failCount++) {
            try {
                long status = analysisStatus(statusRect);
                if (oldStatus != status) {
                    failCount = 0;
                }
                oldStatus = status;

                ShapeType[] shapeTypes = analysisShape();
                if (shapeTypes != null) {
                    HexagonEliminateTree.Move move = HexagonEliminateTree.bestMove(status, shapeTypes);
                    if (move != null) {
                        HexagonEliminateUtil.printlnStatus(HexagonEliminateUtil.longToArray(status));
                        System.out.println();

                        Mat image = Imgcodecs.imread(DATA_PATH + "hexagoneliminate.png");

                        Rect destRect = destRect(image, statusRect, move.getShapePosition());
                        Imgproc.rectangle(image, shapeTypes[move.getShapeTypeIndex()].rect, new Scalar(0, 0, 255), Imgproc.LINE_4);

                        Imgproc.resize(image, image, new Size(image.width() * 0.4, image.height() * 0.4));
                        HighGui.imshow(winname, image);

                        //////////////////// 不使用move方法
                        // HighGui.waitKey();
                        ////////////////////

                        //////////////////////// 使用move方法
                        HighGui.waitKey(1);
                        move(move.getShapeTypeIndex(), shapeTypes[move.getShapeTypeIndex()].rect, destRect, (int) (image.height() / 0.4));
                        ////////////////////////

                        HighGui.windows.get(winname).alreadyUsed = false;
                    }
                }
                screenshot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public static void autoPlayOnPC() {
        User32 instance = User32.INSTANCE;
        instance.EnumChildWindows(instance.FindWindow(null, "六边形消消"), (window, data) -> {
            if (window != null) {
                try {
                    ImageIO.write(GDI32Util.getScreenshot(window), "png", Paths.get(DATA_PATH, "hexagoneliminate.png").toFile());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Rect[] statusRect = analysisStatusRect();
                // 显示器缩放百分比
                AffineTransform defaultTransform = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
                double scaleX = defaultTransform.getScaleX();
                double scaleY = defaultTransform.getScaleY();

                int WM_MOUSEMOVE = 0x200;
                int WM_LBUTTONDOWN = 0x201;
                int WM_LBUTTONUP = 0x202;
                long oldStatus = 0;

                for (int failCount = 0; failCount < 15; failCount++) {
                    try {
                        long status = analysisStatus(statusRect);
                        if (oldStatus != status) {
                            failCount = 0;
                        }
                        oldStatus = status;
                        ShapeType[] shapeTypes = analysisShape();
                        if (shapeTypes != null) {
                            HexagonEliminateTree.Move move = HexagonEliminateTree.bestMove(status, shapeTypes);
                            if (move != null) {
                                HexagonEliminateUtil.printlnStatus(HexagonEliminateUtil.longToArray(status));
                                System.out.println();

                                Mat origin = Imgcodecs.imread(DATA_PATH + "hexagoneliminate.png");

                                Rect srcRect = shapeTypes[move.getShapeTypeIndex()].rect;
                                Rect destRect = destRect(origin, statusRect, move.getShapePosition());
                                Imgproc.rectangle(origin, srcRect, new Scalar(0, 0, 255), Imgproc.LINE_4);

                                Mat image = origin.clone();
                                Imgproc.resize(origin, image, new Size(origin.width() / scaleX, origin.height() / scaleY));
                                HighGui.imshow("分析结果", image);
                                HighGui.waitKey(1);

                                // (x & 0xffff) | (y << 16)
                                long srcPosition = ((srcRect.x + (srcRect.width / 2)) & 0xffff) | ((srcRect.y + (srcRect.height / 2)) << 16);
                                instance.SendMessage(window, WM_LBUTTONDOWN, null, new WinDef.LPARAM(srcPosition));
                                Thread.sleep(50);

                                long floatPosition = ((srcRect.x + (srcRect.width / 2) + ((move.getShapeTypeIndex() - 1) * (srcRect.width / 8))) & 0xffff) | ((srcRect.y + (srcRect.height / 2) + (origin.height() - srcRect.y - srcRect.height)) << 16);
                                instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(floatPosition));
                                Thread.sleep(50);

                                ImageIO.write(GDI32Util.getScreenshot(window), "png", Paths.get(DATA_PATH, "hexagoneliminate.png").toFile());
                                Rect floatRect = analysisShape()[move.getShapeTypeIndex()].rect;

                                long destPosition = ((destRect.x + (destRect.width / 2)) & 0xffff) | (((destRect.y + (destRect.height / 2)) + ((srcRect.y + (srcRect.height / 2) + (origin.height() - srcRect.y - srcRect.height)) - (floatRect.y + (floatRect.height / 2)))) << 16);
                                instance.SendMessage(window, WM_MOUSEMOVE, null, new WinDef.LPARAM(destPosition));
                                Thread.sleep(50);

                                instance.SendMessage(window, WM_LBUTTONUP, null, null);
                                Thread.sleep(350);
                            }
                        }
                        ImageIO.write(GDI32Util.getScreenshot(window), "png", Paths.get(DATA_PATH, "hexagoneliminate.png").toFile());
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
     * 绘制要摆放的位置
     * @param image
     * @param statusRect
     * @param shapePosition
     */
    private static Rect destRect(Mat image, Rect[] statusRect, long shapePosition) {
        int leftMargin = Integer.MAX_VALUE, rightMargin = Integer.MIN_VALUE, topMargin = Integer.MAX_VALUE, bottomMargin = Integer.MIN_VALUE;
        Scalar green = new Scalar(0, 255, 0);
        for (int index = 0; shapePosition > 0; index++) {
            if ((shapePosition & (1L << index)) > 0) {
                shapePosition ^= 1L << index;
                Rect rect = statusRect[index];
                if (rect.x < leftMargin) {
                    leftMargin = rect.x;
                }
                if (rect.x + rect.width > rightMargin) {
                    rightMargin = rect.x + rect.width;
                }
                if (rect.y < topMargin) {
                    topMargin = rect.y;
                }
                if (rect.y + rect.height > bottomMargin) {
                    bottomMargin = rect.y + rect.height;
                }
                Imgproc.rectangle(image, rect, green, Imgproc.LINE_4);
            }
        }
        return  new Rect(leftMargin, topMargin, rightMargin - leftMargin, bottomMargin - topMargin);
    }

    private static void move(int shapeTypeIndex, Rect source, Rect dest, int height) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("sendevent /dev/input/event3 3 47 0\n");
        builder.append("sendevent /dev/input/event3 3 57 0\n");
        builder.append(String.format("sendevent /dev/input/event3 3 53 %d%n", source.x + (source.width >> 1)));
        builder.append(String.format("sendevent /dev/input/event3 3 54 %d%n", source.y + (source.height >> 1)));
        builder.append("sleep 0.01s\n");
        builder.append("sendevent /dev/input/event3 0 0 0\n");
        builder.append(String.format("sendevent /dev/input/event3 3 53 %d%n", source.x + (source.width >> 1) + ((shapeTypeIndex - 1) * (source.width / 8))));
        builder.append(String.format("sendevent /dev/input/event3 3 54 %d%n", source.y + (source.height >> 1) + (height - source.y - source.height)));
        builder.append("sendevent /dev/input/event3 0 0 0");
        Files.write(Paths.get(DATA_PATH, "commands.sh"), builder.toString().getBytes());
        runtime.exec("adb push " + DATA_PATH + "commands.sh /mnt/sdcard/commands.sh").waitFor();
        runtime.exec("adb shell sh /mnt/sdcard/commands.sh").waitFor();
        builder.delete(0, builder.length());

        screenshot();
        Rect floatRect = analysisShape()[shapeTypeIndex].rect;
        builder.append(String.format("sendevent /dev/input/event3 3 53 %d%n", dest.x + (dest.width >> 1)));
        builder.append(String.format("sendevent /dev/input/event3 3 54 %d%n", (dest.y + (dest.height >> 1)) + ((source.y + (source.height >> 1) + (height - source.y - source.height)) - (floatRect.y + (floatRect.height >> 1)))));
        builder.append("sendevent /dev/input/event3 0 0 0\n");
        builder.append("sendevent /dev/input/event3 3 57 1\n");
        builder.append("sendevent /dev/input/event3 0 0 0");
        Files.write(Paths.get(DATA_PATH, "commands.sh"), builder.toString().getBytes());
        runtime.exec("adb push " + DATA_PATH + "commands.sh /mnt/sdcard/commands.sh").waitFor();
        runtime.exec("adb shell sh /mnt/sdcard/commands.sh").waitFor();
    }

    // 截屏到电脑
    private static void screenshot() throws Exception {
        runtime.exec("adb shell screencap /sdcard/hexagoneliminate.png").waitFor();
        runtime.exec("adb pull /sdcard/hexagoneliminate.png " + DATA_PATH).waitFor();
    }

    /**
     * 分析当前状态
     * @return
     */
    private static long analysisStatus(Rect[] rects) {
        // 点：255, 140, 137
        // 线：150, 223, 113
        // 堆：129, 243, 206
        // 半环：110, 162, 245
        // 钩：138, 220, 255
        // 钩：181, 137, 254
        Mat origin = Imgcodecs.imread(DATA_PATH + "hexagoneliminate.png");
        long status = 0;
        byte[] data = new byte[3];
        int[][] colors = {{255, 140, 137}, {150, 223, 113}, {129, 243, 206}, {110, 162, 245}, {138, 220, 255}, {181, 137, 254}};
        int deviation = 10;
        for (int i = 0; i < rects.length; i++) {
            Rect rect = rects[i];
            origin.get(rect.y + (rect.height >> 1), rect.x + (rect.width >> 1), data);
            for (int j = 0; j < colors.length; j++) {
                int[] color = colors[j];
                if (Math.abs((data[0] & 0XFF) - color[0]) < deviation && Math.abs((data[1] & 0XFF) - color[1]) < deviation && Math.abs((data[2] & 0XFF) - color[2]) < deviation) {
                    status |= 1L << i;
                    break;
                }
            }
        }
        return status;
    }

    private static Rect[] analysisStatusRect() {
        Mat origin = Imgcodecs.imread(DATA_PATH + "hexagoneliminate.png");

        Mat image = origin.clone();
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        // 40~60
        Imgproc.threshold(image, image, 50, 255, Imgproc.THRESH_BINARY);
        Imgproc.medianBlur(image, image, 11);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Rect statusRangeRect = Imgproc.boundingRect(contours.stream().max(Comparator.comparingInt(Mat::height)).get());
        contours.clear();

        Mat hsv = new Mat();
        image = origin.submat(statusRangeRect);
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);
        // 灰：30, 7, 77
        // 点：119, 118, 255
        // 线：70, 126, 223
        // 堆：40, 120, 243
        // 半环：12, 141, 245
        // 钩：21, 117, 255
        // 钩：169, 117, 254
        double[][] low = {{30, 7, 77}, {116, 115, 252}, {67, 123, 220}, {37, 117, 240}, {9, 38, 242}, {18, 114, 252}, {166, 114, 251}};
        double[][] high = {{30, 7, 77}, {122, 121, 255}, {73, 129, 226}, {43, 123, 246}, {15, 144, 248}, {24, 120, 255}, {172, 120, 255}};
        Scalar lowHSV = new Scalar(3);
        Scalar highHSV = new Scalar(3);
        Mat mat = new Mat();
        Mat hexagon = Mat.zeros(hsv.rows(), hsv.cols(), CvType.CV_8U);
        for (int i = 0; i < low.length; i++) {
            lowHSV.set(low[i]);
            highHSV.set(high[i]);
            Core.inRange(hsv, lowHSV, highHSV, mat);
            Core.bitwise_or(hexagon, mat, hexagon);
        }
        Imgproc.medianBlur(hexagon, hexagon, 3);

        Imgproc.findContours(hexagon, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Rect[] rects = new Rect[contours.size()];
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            rect.y += statusRangeRect.y;
            rect.x += statusRangeRect.x;
            rects[contours.size() - i - 1] = rect;
        }
        for (int i = 0; i < rects.length; i++) {
            Rect firstRect = rects[i];
            for (int j = i + 1; j < rects.length; j++) {
                Rect nRect = rects[j];
                if (Math.abs(firstRect.y - nRect.y) < (firstRect.height >> 1)) {
                    if (firstRect.x > nRect.x) {
                        rects[i] = nRect;
                        rects[j] = firstRect;
                        firstRect = nRect;
                    }
                } else {
                    break;
                }
            }
        }
        return rects;
    }

    /**
     * 分析可选形状类型
     * @return
     */
    private static ShapeType[] analysisShape() {
        Mat origin = Imgcodecs.imread(DATA_PATH + "hexagoneliminate.png");

        Mat hsv = new Mat();
        Imgproc.cvtColor(origin, hsv, Imgproc.COLOR_BGR2HSV);

        // 点：119, 118, 255
        // 线：70, 126, 223
        // 堆：40, 120, 243
        // 半环：12, 141, 245
        // 钩：21, 117, 255
        // 钩：169, 117, 254
        double[][] low = {{69, 68, 205}, {20, 76, 173}, {0, 70, 93}, {0, 91, 95}, {0, 67, 205}, {119, 67, 204}};
        double[][] high = {{169, 168, 255}, {120, 176, 255}, {90, 170, 255}, {52, 191, 255}, {71, 167, 255}, {219, 167, 255}};
        Scalar lowHSV = new Scalar(3);
        Scalar highHSV = new Scalar(3);
        Mat mat = new Mat();
        Mat image = Mat.zeros(hsv.rows(), hsv.cols(), CvType.CV_8U);
        for (int i = 0; i < low.length; i++) {
            lowHSV.set(low[i]);
            highHSV.set(high[i]);
            Core.inRange(hsv, lowHSV, highHSV, mat);
            Core.bitwise_or(image, mat, image);
        }
        Imgproc.medianBlur(image, image, 13);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        if (contours.size() < 3) {
            return null;
        }
        ShapeType[] shapes = new ShapeType[3];
        for (int i = 0; i < shapes.length; i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            ShapeType shape = new ShapeType();
            shape.rect = rect;
            shape.type = shapeType(shapeHash(origin.submat(rect)));
            shapes[i] = shape;
        }
        for (int i = 0; i < shapes.length; i++) {
            ShapeType firstShapeType = shapes[i];
            for (int j = i + 1; j < shapes.length; j++) {
                ShapeType nShapeType = shapes[j];
                if (firstShapeType.rect.x < nShapeType.rect.x) {
                    shapes[i] = nShapeType;
                    shapes[j] = firstShapeType;
                    firstShapeType = nShapeType;
                }
            }
        }
        return shapes;
    }

    /**
     * 形状类型
     * @param hash
     * @return
     */
    private static int shapeType(long hash) {
        int shapeType = 0, minValue = Integer.MAX_VALUE;
        for (int i = 0; i < SHAPE_TYPE_HASH.length; i++) {
            int count = HexagonEliminateUtil.bitCount(SHAPE_TYPE_HASH[i] ^ hash);
            if (count < minValue) {
                minValue = count;
                shapeType = i;
            }
        }
        return shapeType;
    }

    /**
     * 图片hash值
     * @param image
     * @return
     */
    private static long shapeHash(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Size size = new Size(8, 9);
        byte[] beforePixel = new byte[3], nextPixel = new byte[3];
        Imgproc.resize(image, image, size);
        long hash = 0;
        for (int col = 0; col < image.width(); col++) {
            for (int row = 1; row < image.height(); row++) {
                image.get(row - 1, col, beforePixel);
                image.get(row, col, nextPixel);
                int beforeRGB = ((beforePixel[0] & 0XFF) << 16) | ((beforePixel[1] & 0XFF) << 8) | beforePixel[2];
                int nextRGB = ((nextPixel[0] & 0XFF) << 16) | ((nextPixel[1] & 0XFF) << 8) | nextPixel[2];
                if (nextRGB > beforeRGB) {
                    hash |= 1L << ((8 * row) + col);
                }
            }
        }
        return hash;
    }

    public static class ShapeType {
        private Rect rect;
        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
