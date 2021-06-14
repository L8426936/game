package solved.hexagoneliminate;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HexagonEliminateUtil {
    public static final int LAYER = 4;
    private static final int[] INDEX_TO_X = new int[3 * LAYER * (LAYER + 1) + 1]; // 一维坐标映射到三维坐标轴的X轴
    private static final int[] INDEX_TO_Y = new int[3 * LAYER * (LAYER + 1) + 1]; // 一维坐标映射到三维坐标轴的Y轴
    private static final int[] INDEX_TO_Z = new int[3 * LAYER * (LAYER + 1) + 1]; // 一维坐标映射到三维坐标轴的Z轴
    private static final int[] XYZ_TO_INDEX = new int[(2 * LAYER + 1) * (2 * LAYER + 1)]; // 用于三维坐标轴映射到一维坐标

    static {
        for (int index = 0, z = -LAYER; z <= LAYER; z++) {
            for (int x = -LAYER; x <= LAYER; x++) {
                for (int y = -LAYER; y <= LAYER; y++) {
                    if (x + y + z == 0) {
                        INDEX_TO_X[index] = x;
                        INDEX_TO_Y[index] = y;
                        INDEX_TO_Z[index++] = z;
                    }
                }
            }
        }
        // System.out.println(Arrays.toString(INDEX_TO_X));
        // System.out.println(Arrays.toString(INDEX_TO_Y));
        // System.out.println(Arrays.toString(INDEX_TO_Z));
        // for (int index = 0, z = -LAYER; z <= LAYER; z++) {
        //     for (int col = Math.abs(z); col > 0; col--) {
        //         System.out.print(' ');
        //     }
        //     for (int col = 2 * LAYER + 1 - Math.abs(z); col > 0; col--) {
        //         System.out.format("%2d", INDEX_TO_X[index++]);
        //         // System.out.format("%2d", INDEX_TO_Y[index++]);
        //         // System.out.format("%2d", INDEX_TO_Z[index++]);
        //         if (col > 1) {
        //             System.out.print(' ');
        //         }
        //     }
        //     System.out.println();
        // }

        for (int size = 2 * LAYER + 1, index = 0, row = -LAYER; row <= LAYER; row++) {
            for (int col = 0; col < size - Math.abs(row); col++) {
                XYZ_TO_INDEX[size * (row + LAYER) + col] = index++;
            }
            for (int col = size - Math.abs(row); col < size; col++) {
                XYZ_TO_INDEX[size * (row + LAYER) + col] = -1;
            }
        }
        // System.out.println(Arrays.toString(XYZ_TO_INDEX));
        // for (int index = 0, z = -LAYER; z <= LAYER; z++) {
        //     for (int col = Math.abs(z); col > 0; col--) {
        //         System.out.print(' ');
        //     }
        //     for (int col = 2 * LAYER + 1 - Math.abs(z); col > 0; col--) {
        //         System.out.format("%2d", XYZ_TO_INDEX[index++]);
        //         if (col > 1) {
        //             System.out.print(' ');
        //         }
        //     }
        //     index += Math.abs(z);
        //     System.out.println();
        // }
    }

    /**
     * <p>x，y，z对应一维坐标</p>
     * @param x
     * @param y
     * @param z
     * @return index, 0<=index<=3*layer*(layer + 1)
     */
    public static int index(int x, int y, int z) {
        return XYZ_TO_INDEX[(2 * LAYER + 1) * (z + LAYER) + (z <= 0 ? LAYER - y : LAYER + x)];
    }

    public static int indexToX(int index) {
        return INDEX_TO_X[index];
    }

    public static int indexToY(int index) {
        return INDEX_TO_Y[index];
    }

    public static int indexToZ(int index) {
        return INDEX_TO_Z[index];
    }

    /**
     * <p>数组型状态转整型状态，最多支持4层的六边形</p>
     * @param charsStatus 数组型状态
     * @return 整型状态
     */
    public static long arrayToLong(char[] charsStatus) {
        long longStatus = 0;
        for (int index = 0; index < charsStatus.length; index++) {
            if (charsStatus[index] != '0') {
                longStatus |= 1L << index;
            }
        }
        return longStatus;
    }

    /**
     * <p>整型状态转数组型状态</p>
     * @param longStatus 整型状态
     * @return 数组型状态
     */
    public static char[] longToArray(long longStatus) {
        char[] charsStatus = new char[3 * LAYER * (LAYER + 1) + 1];
        for (int index = 3 * LAYER * (LAYER + 1); index >= 0; index--) {
            charsStatus[index] = ((longStatus & (1L << index)) != 0 ? '1' : '0');
        }
        // System.out.println(Arrays.toString(charsStatus));
        return charsStatus;
    }

    /**
     * <p>打印数组型状态</p>
     * @param status
     */
    public static void printlnStatus(char[] status) {
        for (int index = 0, z = -LAYER; z <= LAYER; z++) {
            for (int col = Math.abs(z); col > 0; col--) {
                System.out.print(' ');
            }
            /*
             * 当前行点的个数
             * h(z) = 2 * layer + 1 - |z|；-layer <= z <= layer
             */
            for (int col = 2 * LAYER + 1 - Math.abs(z); col > 0; col--) {
                if (status[index++] == '0') {
                    System.out.print('⬡');
                } else {
                    System.out.print('⬢');
                }
                if (col > 1) {
                    System.out.print(' ');
                }
            }
            System.out.println();
        }
    }

    /**
     * <p>二进制1的个数</p>
     * @param status
     * @return
     */
    public static int bitCount(long status) {
        int count = 0;
        while (status != 0) {
            status &= status - 1;
            count++;
        }
        return count;
    }

    /**
     * 制作模板
     */
    public static void makeTemplate() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            String dir = Paths.get(System.getProperty("user.dir"), "src", "solved", "hexagoneliminate", "data").toString();
            Files.list(Paths.get(dir + File.separator + "origin")).filter(path -> path.toString().endsWith(".jpg") || path.toString().endsWith(".png")).forEach(path -> {
                Mat origin = Imgcodecs.imread(path.toString());

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
                Mat image = new Mat(hsv.rows(), hsv.cols(), CvType.CV_8U);
                for (int i = 0; i < low.length; i++) {
                    lowHSV.set(low[i]);
                    highHSV.set(high[i]);
                    Core.inRange(hsv, lowHSV, highHSV, mat);
                    Core.bitwise_or(image, mat, image);
                }
                Imgproc.medianBlur(image, image, 13);

                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                for (MatOfPoint contour : contours) {
                    Imgcodecs.imwrite(dir + File.separator + "template" + File.separator + Math.random() + ".png", origin.submat(Imgproc.boundingRect(contour)));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
