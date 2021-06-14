package solved.mostlinklink;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.highgui.ImageWindow;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MostLinkLinkPlayer {

    public static final char BAN = ' ', EMPTY = '□', EXIST = '■';
    /**
     * <p>OPENCV_ANALYSIS：基于opencv的分析方式，一定程度减少弹窗对图片分析的影响，有较大区域像素值接近游戏色块的话会分析错误</p>
     * <p>OPENCV_TEMPLATE_ANALYSIS：基于opencv的模板分析方式，一定程度减少弹窗对图片分析的影响，左右边界游戏色块显示不全会分析错误</p>
     * <p>ANALYSIS：基于像素值的分析，有消息弹窗或是有较大区域像素值接近游戏色块的话会分析错误</p>
     * <p>RANK_LIST：排行榜模式</p>
     * <p>CHALLENGE：挑战赛模式</p>
     */
    public static final int OPENCV_ANALYSIS = 0, OPENCV_TEMPLATE_ANALYSIS = 1, ANALYSIS = 2, RANK_LIST = 0, CHALLENGE = 1;
    private static final String IMAGE_PATH = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "solved", "mostlinklink", "data").toString() + File.separator;
    private static final Runtime runtime = Runtime.getRuntime();
    private static GameInfo.Point closePoint, nextPoint;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     *
     * @param analysisType 分析方式，可选值OPENCV_ANALYSIS、OPENCV_TEMPLATE_ANALYSIS、ANALYSIS
     * @param playMode 游戏模式，可选值RANK_LIST、CHALLENGE
     */
    public static void autoPlay(int analysisType, int playMode) {
        long startTime, timestamp;
        for (int tryAgainCount = 0; tryAgainCount < 15; tryAgainCount++) {
            try {
                startTime = System.currentTimeMillis();
                screenshot();
                System.out.format("截屏耗时:%d毫秒%n", System.currentTimeMillis() - startTime);
                timestamp = System.currentTimeMillis();
                GameInfo gameInfo = null;
                switch (analysisType) {
                    case OPENCV_ANALYSIS:
                        gameInfo = opencvAnalysis();
                        break;
                    case OPENCV_TEMPLATE_ANALYSIS:
                        gameInfo = opencvTemplateAnalysis();
                        break;
                    case ANALYSIS:
                        gameInfo = analysis();
                        break;
                }
                if (gameInfo != null) {
                    System.out.format("图片分析耗时:%d毫秒%n", System.currentTimeMillis() - timestamp);
                    printStatus(gameInfo.getStatus());
                    MostLinkLinkTree mostLinkLinkTree = new MostLinkLinkTree(gameInfo);
                    timestamp = System.currentTimeMillis();
                    MostLinkLinkNode[] nodes = mostLinkLinkTree.DFS();
                    if (nodes != null) {
                        System.out.format("搜索耗时:%d毫秒%n", System.currentTimeMillis() - timestamp);
                        drawPassPath(nodes, gameInfo);
                        timestamp = System.currentTimeMillis();
                        play(nodes, gameInfo);
                        System.out.format("滑动屏幕耗时:%d毫秒%n", System.currentTimeMillis() - timestamp);
                        tryAgainCount = 0;
                        if (playMode == RANK_LIST) {
                            Thread.sleep(1000);
                        } else {
                            Thread.sleep(800);
                        }
                    } else {
                        System.out.println("游戏搜索失败，重新开始截屏");
                    }
                } else {
                    System.out.println("分析失败，重新开始截屏");
                }
                if (playMode == RANK_LIST) {
                    nextGame(analysisType);
                }
                System.out.format("耗费总时长:%d毫秒%n%n", System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    // 截屏到电脑
    private static void screenshot() throws Exception {
        runtime.exec("adb shell screencap /sdcard/mostlinklink.png").waitFor();
        runtime.exec("adb pull /sdcard/mostlinklink.png " + IMAGE_PATH).waitFor();
    }

    // 一定程度减少弹窗对图片分析的影响，有较大区域像素值接近游戏色块的话会分析错误
    private static GameInfo opencvAnalysis() {
        Mat original = Imgcodecs.imread(IMAGE_PATH + "mostlinklink.png");
        // 去除无关游戏色块像素
        Mat source = original.clone();
        int gray = 187, deviation = 20;
        byte[] pixels = new byte[3], black = new byte[3], white = {(byte) 255, (byte) 255, (byte) 255};
        for (int r = 0; r < source.rows(); r++) {
            for (int c = 0; c < source.cols(); c++) {
                source.get(r, c, pixels);
                if (pixels[0] == pixels[1] && pixels[1] == pixels[2]
                        && Math.abs((pixels[0] & 0XFF) - gray) < deviation
                        && Math.abs((pixels[1] & 0XFF) - gray) < deviation
                        && Math.abs((pixels[2] & 0XFF) - gray) < deviation) {
                    source.put(r, c, white);
                } else {
                    source.put(r, c, black);
                }
            }
        }
        // 去除多余噪点
        Imgproc.medianBlur(source, source, 9);

        Scalar color = new Scalar(0, 0, 255);
        // 找到图中所有矩形，并保留矩形中心的x，y
        Imgproc.cvtColor(source, source, Imgproc.COLOR_RGB2GRAY);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(source, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Set<Integer> xSet = new HashSet<>();
        Set<Integer> ySet = new HashSet<>();
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            xSet.add(rect.x + rect.width / 2);
            ySet.add(rect.y + rect.height / 2);
            Imgproc.rectangle(original, rect, color, Imgproc.LINE_4);
        }

        // 矩形中心的x，y排序，组合出所有有效位置
        List<Integer> xList = xSet.stream().sorted().collect(Collectors.toList());
        List<Integer> yList = ySet.stream().sorted().collect(Collectors.toList());
        GameInfo.Point[][] points = new GameInfo.Point[yList.size()][xList.size()];
        char[][] status = new char[yList.size()][xList.size()];
        GameInfo gameInfo = new GameInfo();
        int[] navy = {54, 39, 32};
        int startPointCount = 0;
        for (int i = 0; i < yList.size(); i++) {
            int y = yList.get(i);
            for (int j = 0; j < xList.size(); j++) {
                int x = xList.get(j);
                original.get(y, x, pixels);
                points[i][j] = new GameInfo.Point(x, y);
                if (pixels[0] == pixels[1] && pixels[1] == pixels[2]
                        && Math.abs((pixels[0] & 0XFF) - gray) < deviation
                        && Math.abs((pixels[1] & 0XFF) - gray) < deviation
                        && Math.abs((pixels[2] & 0XFF) - gray) < deviation) {
                    status[i][j] = EMPTY;
                    gameInfo.setCount(gameInfo.getCount() + 1);
                } else if (Math.abs((pixels[0] & 0XFF) - navy[0]) < deviation
                        && Math.abs((pixels[1] & 0XFF) - navy[1]) < deviation
                        && Math.abs((pixels[2] & 0XFF) - navy[2]) < deviation) {
                    status[i][j] = BAN;
                } else {
                    status[i][j] = EXIST;
                    gameInfo.setCount(gameInfo.getCount() + 1);
                    gameInfo.setStartRow(i);
                    gameInfo.setStartCol(j);
                    startPointCount++;
                }
            }
        }
        gameInfo.setStatus(status);
        gameInfo.setPoints(points);

        showMat("处理结果", source);
        showMat("分析结果", original);

        return startPointCount == 1 ? gameInfo : null;
    }

    // 一定程度减少弹窗对图片分析的影响，左右边界游戏色块显示不全会分析错误
    private static GameInfo opencvTemplateAnalysis() {
        Mat original = Imgcodecs.imread(IMAGE_PATH + "mostlinklink.png");
        // 去除无关游戏色块像素
        Mat source = original.clone();
        Imgproc.cvtColor(source, source, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(source, source, 50, 255, Imgproc.THRESH_BINARY);

        // 将图中的闭合形状染成白色，避免二值化没有将起点色块染成白色，导致模板图片无法匹配
        // List<MatOfPoint> contours = new ArrayList<>();
        // Imgproc.findContours(source, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        // Imgproc.fillPoly(source, contours, new Scalar(255, 255, 255));

        // 模板匹配
        Scalar color = new Scalar(0, 0, 255);
        Mat template = Imgcodecs.imread(IMAGE_PATH + "block.png", Imgcodecs.IMREAD_GRAYSCALE);
        Mat result = new Mat();
        Imgproc.matchTemplate(source, template, result, Imgproc.TM_CCOEFF_NORMED);

        // 游戏色块边界
        int leftMargin = source.width(), rightMargin = 0, topMargin = source.height(), bottomMargin = 0;

        for (int r = 0; r < result.rows(); r++) {
            for (int c = 0; c < result.cols(); c++) {
                double value = result.get(r, c)[0];
                if (value > 0.95) {
                    if (leftMargin > c) {
                        leftMargin = c;
                    }
                    if (rightMargin < c) {
                        rightMargin = c;
                    }
                    if (topMargin > r) {
                        topMargin = r;
                    }
                    if (bottomMargin < r) {
                        bottomMargin = r;
                    }
                    Imgproc.rectangle(original, new Rect(c, r, template.width(), template.height()), color, Imgproc.LINE_4);
                }
            }
        }
        rightMargin += template.width();
        bottomMargin += template.height();
        // Imgproc.rectangle(original, new Rect(leftMargin, topMargin, rightMargin - leftMargin, bottomMargin - topMargin), color, Imgproc.LINE_4);

        int row = Math.round((float) (bottomMargin - topMargin) / template.height()), col = Math.round((float) (rightMargin - leftMargin) / template.width());
        GameInfo.Point[][] points = new GameInfo.Point[row][col];
        char[][] status = new char[row][col];
        GameInfo gameInfo = new GameInfo();
        byte[] navy = {54, 39, 32}, pixels = new byte[3];
        int gray = 187, deviation = 20, startPointCount = 0;
        for (int r = 0; r < row; r++) {
            int y = topMargin + r * template.height() + template.height() / 2;
            for (int c = 0; c < col; c++) {
                int x = leftMargin + c * template.width() + template.width() / 2;
                original.get(y, x, pixels);
                points[r][c] = new GameInfo.Point(x, y);
                if (pixels[0] == pixels[1] && pixels[1] == pixels[2]
                        && Math.abs((pixels[0] & 0XFF) - gray) < deviation
                        && Math.abs((pixels[1] & 0XFF) - gray) < deviation
                        && Math.abs((pixels[2] & 0XFF) - gray) < deviation) {
                    status[r][c] = EMPTY;
                    gameInfo.setCount(gameInfo.getCount() + 1);
                } else if (Math.abs((pixels[0] & 0XFF) - navy[0]) < deviation
                        && Math.abs((pixels[1] & 0XFF) - navy[1]) < deviation
                        && Math.abs((pixels[2] & 0XFF) - navy[2]) < deviation) {
                    status[r][c] = BAN;
                } else {
                    status[r][c] = EXIST;
                    gameInfo.setCount(gameInfo.getCount() + 1);
                    gameInfo.setStartRow(r);
                    gameInfo.setStartCol(c);
                    startPointCount++;
                }
            }
        }
        gameInfo.setStatus(status);
        gameInfo.setPoints(points);

        showMat("处理结果", source);
        showMat("分析结果", original);

        return startPointCount == 1 ? gameInfo : null;
    }

    // 基于像素值的分析，有消息弹窗或是有较大区域像素值接近游戏色块的话会分析错误
    private static GameInfo analysis() throws IOException {
        BufferedImage image = ImageIO.read(new File(IMAGE_PATH + "mostlinklink.png"));
        int height = image.getHeight(), width = image.getWidth();
        if (closePoint == null) {
            closePoint = new GameInfo.Point((int) (width * 0.856), (int) (height * 0.342));
        }
        if (nextPoint == null) {
            nextPoint = new GameInfo.Point((int) (width * 0.5), (int) (height * 0.738));
        }

        // 搜索游戏色块边界
        for (int y = (int) (0.165 * height); y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 游戏色块上边界
                if (image.getRGB(x, y) == -3355444) { // -3355444灰色
                    int leftMarginX = width - 1, rightMarginX = 0, topMarginY = y, bottomMarginY = 0, blockWidth = 0, blockHeight = 0, blockSpaceSize = 0;

                    // 游戏色块宽
                    while (x + blockWidth < width && image.getRGB(x + blockWidth, y) == -3355444) {
                        blockWidth++;
                    }
                    // 游戏色块高
                    while (image.getRGB(x, topMarginY + blockHeight) == -3355444) {
                        blockHeight++;
                    }

                    // 游戏色块间距
                    int blockSpaceWidth = 0, blockSpaceHeight = 0;
                    while (blockSpaceWidth < blockHeight && x + blockWidth + blockSpaceWidth < width && image.getRGB(x + blockWidth + blockSpaceWidth, y) != -3355444) {
                        blockSpaceWidth++;
                    }
                    while (blockSpaceHeight < blockHeight && y + blockHeight + blockSpaceHeight < height && image.getRGB(x, y + blockHeight + blockSpaceHeight) != -3355444) { // -3355444灰色
                        blockSpaceHeight++;
                    }
                    blockSpaceSize = Math.min(blockSpaceWidth, blockSpaceHeight); // 游戏色块间距

                    // 游戏色块下边界
                    int row = 0;
                    for (y = topMarginY + (blockHeight / 2); y < height; y += blockSpaceSize + blockHeight) {
                        boolean hasBlock = false;
                        for (x = 0; x < width; x++) {
                            if (image.getRGB(x, y) == -3355444) {
                                if (leftMarginX > x) {
                                    leftMarginX = x; // 游戏色块左边界
                                    rightMarginX = width - x; // 游戏色块右边界
                                }
                                bottomMarginY = y;
                                hasBlock = true;
                                row++;
                                break;
                            }
                        }
                        if (!hasBlock) {
                            bottomMarginY += (blockHeight / 2);
                            break;
                        }
                    }

                    int col = 0;
                    if (leftMarginX == 0) {
                        // 用于左右两边游戏色块显示不全
                        col = 2;
                        for (int j = 0; true; j++) {
                            x = leftMarginX + blockWidth + ((blockSpaceSize + blockHeight) * j) + (blockSpaceSize + (blockHeight / 2));
                            if (x < rightMarginX - blockWidth - blockSpaceSize) {
                                col++;
                            } else {
                                break;
                            }
                        }
                    } else {
                        for (int j = 0; true; j++) {
                            x = leftMarginX + ((blockSpaceSize + blockHeight) * j) + (blockHeight / 2);
                            if (x < rightMarginX) {
                                col++;
                            } else {
                                break;
                            }
                        }
                    }

                    GameInfo gameInfo = new GameInfo();
                    int startPointCount = 0;

                    Graphics graphics = image.getGraphics();
                    graphics.setColor(new Color(255, 0, 0));
                    // graphics.drawRect(leftMarginX, topMarginY, rightMarginX - leftMarginX, bottomMarginY - topMarginY);

                    GameInfo.Point[][] points = new GameInfo.Point[row][col];
                    for (int i = 0; i < row; i++) {
                        y = topMarginY + ((blockSpaceSize + blockHeight) * i) + (blockHeight / 2);
                        for (int j = 1; j < col - 1; j++) {
                            x = leftMarginX + blockWidth + ((blockSpaceSize + blockHeight) * (j - 1)) + (blockSpaceSize + (blockHeight / 2));
                            points[i][j] = new GameInfo.Point(x, y);
                        }
                        points[i][0] = new GameInfo.Point(leftMarginX + (blockWidth / 2), y);
                        points[i][col - 1] = new GameInfo.Point(rightMarginX - (blockWidth / 2), y);
                    }
                    gameInfo.setPoints(points);

                    char[][] status = new char[row][col];
                    for (int i = 0; i < row; i++) {
                        for (int j = 0; j < col; j++) {
                            GameInfo.Point point = points[i][j];
                            graphics.fillArc(point.getX(), point.getY(), 15, 15, 0, 360);
                            int rgb = image.getRGB(point.getX(), point.getY());
                            if (rgb == -3355444) {
                                status[i][j] = EMPTY;
                                gameInfo.setCount(gameInfo.getCount() + 1);
                            } else if (rgb != -14472389) { // -14472389背景色
                                status[i][j] = EXIST;
                                gameInfo.setStartRow(i);
                                gameInfo.setStartCol(j);
                                gameInfo.setCount(gameInfo.getCount() + 1);
                                startPointCount++;
                            } else {
                                status[i][j] = BAN;
                            }
                        }
                    }
                    gameInfo.setStatus(status);


                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", byteArrayOutputStream);
                    byteArrayOutputStream.flush();
                    showMat("分析结果", Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_COLOR));

                    return startPointCount == 1 ? gameInfo : null;
                }
            }
        }
        return null;
    }

    // 触摸滑动灰格
    private static void play(MostLinkLinkNode[] nodes, GameInfo gameInfo) throws Exception {
        GameInfo.Point[][] points = gameInfo.getPoints();
        for (int i = 1; i < nodes.length; i++) {
            int j = i;
            MostLinkLinkNode startNode = nodes[i];
            while (j + 1 < nodes.length && (startNode.getRow() == nodes[j + 1].getRow() || startNode.getCol() == nodes[j + 1].getCol())) {
                j++;
            }
            MostLinkLinkNode endNode = nodes[j];
            GameInfo.Point startPoint = points[startNode.getRow()][startNode.getCol()], endPoint = points[endNode.getRow()][endNode.getCol()];
            String command = String.format("adb shell input touchscreen swipe %d %d %d %d %d", startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY(), (j - i) * 50);
            runtime.exec(command).waitFor();
            i = j;
        }
    }

    // 下一关
    private static void nextGame(int analysisType) throws Exception {
        switch (analysisType) {
            case OPENCV_ANALYSIS: case OPENCV_TEMPLATE_ANALYSIS:
                if (closePoint == null) {
                    screenshot();
                    closePoint = matchTemplate("close.png");
                }
                if (closePoint != null) {
                    runtime.exec(String.format("adb shell input touchscreen tap %d %d", closePoint.getX(), closePoint.getY())).waitFor();
                }
                if (nextPoint == null) {
                    screenshot();
                    nextPoint = matchTemplate("next.png");
                }
                if (nextPoint != null) {
                    runtime.exec(String.format("adb shell input touchscreen tap %d %d", nextPoint.getX(), nextPoint.getY())).waitFor();
                }
                break;
            case ANALYSIS:
                runtime.exec(String.format("adb shell input touchscreen tap %d %d", closePoint.getX(), closePoint.getY())).waitFor(); // 关闭双倍奖励
                runtime.exec(String.format("adb shell input touchscreen tap %d %d", nextPoint.getX(), nextPoint.getY())).waitFor(); // 下一关
                break;
        }
    }

    private static GameInfo.Point matchTemplate(String templateName) {
        Mat source = Imgcodecs.imread(IMAGE_PATH + "mostlinklink.png");
        Mat template = Imgcodecs.imread(IMAGE_PATH + templateName, Imgcodecs.IMREAD_GRAYSCALE);

        Mat image = source.clone();
        Imgproc.cvtColor(source, image, Imgproc.COLOR_RGB2GRAY);

        Mat result = new Mat();
        Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);
        Point maxLoc = minMaxLocResult.maxLoc;
        if (minMaxLocResult.maxVal > 0.95) {
            Imgproc.rectangle(source,
                    new Point(maxLoc.x, maxLoc.y),
                    new Point(maxLoc.x + template.width(), maxLoc.y + template.height()),
                    new Scalar(0, 0, 255), Imgproc.LINE_4);
            showMat("分析结果", source);
            return new GameInfo.Point((int) (maxLoc.x + (template.width() / 2)), (int) (maxLoc.y + (template.height() / 2)));
        }
        return null;
    }

    private static void drawPassPath(MostLinkLinkNode[] nodes, GameInfo gameInfo) {
        Mat original = Imgcodecs.imread(IMAGE_PATH + "mostlinklink.png");
        Scalar color = new Scalar(0, 0, 255);
        GameInfo.Point[][] points = gameInfo.getPoints();
        for (int i = 0; i < nodes.length - 1; i++) {
            MostLinkLinkNode startNode = nodes[i], endNode = nodes[i + 1];
            GameInfo.Point startPoint = points[startNode.getRow()][startNode.getCol()], endPoint = points[endNode.getRow()][endNode.getCol()];
            Imgproc.line(original, new Point(startPoint.getX(), startPoint.getY()), new Point(endPoint.getX(), endPoint.getY()), color, Imgproc.LINE_AA);
        }
        showMat("通关路径", original);
    }

    private static void showMat(String winame, Mat mat) {
        Imgproc.resize(mat, mat, new Size(mat.width() * 0.4, mat.height() * 0.4));
        HighGui.imshow(winame, mat);
        HighGui.waitKey(1);
        // 不会关闭已存在的同名窗口，只刷新图片
        for (ImageWindow win : HighGui.windows.values()) {
            win.alreadyUsed = false;
        }
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
}
