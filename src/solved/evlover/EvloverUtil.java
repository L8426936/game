package solved.evlover;

import solved.util.AVLTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EvloverUtil {

    /**
     * <p>C: clockwise swap 顺时针交换</p>
     * <p>P: point swap 点对称交换</p>
     * <p>A: anticlockwise swap 逆时针交换</p>
     */
    public static final int C = 1, P = 2, A = 3;
    private int layer;
    private int[] indexToX, indexToY, indexToZ, xyzToIndex;

    public EvloverUtil(int layer) {
        this.layer = layer;

        indexToX = new int[3 * layer * (layer + 1) + 1];
        indexToY = new int[3 * layer * (layer + 1) + 1];
        indexToZ = new int[3 * layer * (layer + 1) + 1];

        for (int index = 0, z = -layer; z <= layer; z++) {
            for (int x = -layer; x <= layer; x++) {
                for (int y = -layer; y <= layer; y++) {
                    if (x + y + z == 0) {
                        indexToX[index] = x;
                        indexToY[index] = y;
                        indexToZ[index++] = z;
                    }
                }
            }
        }
        // System.out.println(Arrays.toString(indexToX));
        // System.out.println(Arrays.toString(indexToY));
        // System.out.println(Arrays.toString(indexToZ));

        int size = 2 * layer + 1, index = 0;
        xyzToIndex = new int[size * size];
        for (int row = -layer; row <= layer; row++) {
            for (int col = 0; col < size; col++) {
                xyzToIndex[size * (row + layer) + col] = col < size - Math.abs(row) ? index++ : -1;
            }
        }
        // System.out.println(Arrays.toString(xyzToIndex));
    }

    /**
     * <p>返回一个数组型随机状态</p>
     * @param layer 六边形层数
     * @param count 点的数量
     * @return if count > 3 * layer * (layer + 1) + 1, return []
     */
    public static char[] randomStatus(int layer, int count) {
        if (count > 3 * layer * (layer + 1) + 1) {
            return new char[0];
        }

        char[] randomStatus = new char[3 * layer * (layer + 1) + 1];
        for (int index = 3 * layer * (layer + 1); index >= 0; index--) {
            randomStatus[index] = count > index ? '1' : '0';
        }
        // System.out.println(Arrays.toString(randomStatus));

        Random random = new Random();
        for (int i = randomStatus.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char value = randomStatus[j];
            randomStatus[j] = randomStatus[i];
            randomStatus[i] = value;
        }
        // System.out.println(Arrays.toString(randomStatus));
        return randomStatus;
    }

    /**
     * <p>返回所有数组型状态</p>
     * @param layer 六边形层数
     * @param count 点的数量
     * @return
     */
    public static List<char[]> allStatus(int layer, int count) {
        if (count > 3 * layer * (layer + 1) + 1) {
            return new ArrayList<>(0);
        }

        char[] originStatus = new char[3 * layer * (layer + 1) + 1];
        for (int index = 3 * layer * (layer + 1); index >= 0; index--) {
            originStatus[index] = count > index ? '1' : '0';
        }

        List<char[]> allStatus = new ArrayList<>();
        allStatus.add(originStatus);

        for (int i = count - 1, j = 0; i >= 0; i--) {
            for (int k = allStatus.size(); j < k; j++) {
                char[] status = allStatus.get(j).clone();
                for (int m = i; m + 1 < status.length && status[m + 1] == '0'; m++) {
                    status[m + 1] = status[m];
                    status[m] = '0';
                    allStatus.add(status.clone());
                }
            }
        }
        return allStatus;
    }

    /**
     * <p>返回所有数组型状态，去除所有旋转、对称</p>
     * @param count 点的数量
     * @return
     */
    public List<char[]> allUniqueStatus(int count) {
        List<char[]> allStatus = allStatus(layer, count);
        System.out.format("allStatusSum: %d%n", allStatus.size());
        List<char[]> allUniqueStatus = new ArrayList<>();
        AVLTree<char[]> allStatusAVLTree = new AVLTree<>();
        for (int i = 0; i < allStatus.size(); i++) {
            long status = charsStatusToLongStatus(allStatus.get(i));
            if (allStatusAVLTree.get(status) == null) {
                allUniqueStatus.add(allStatus.get(i));
                for (int symmetryType = 1; symmetryType <= 0X7F; symmetryType <<= 1) {
                    allStatusAVLTree.put(symmetryStatus(status, symmetryType), allStatus.get(i));
                }
                for (int rotationCount = 1; rotationCount <= 5; rotationCount++) {
                    status = clockwiseRotationStatus(status);
                    allStatusAVLTree.put(status, allStatus.get(i));
                }
            }
        }
        return allUniqueStatus;
    }

    /**
     * <p>返回六边形层数</p>
     * <p>六边形个数和公式: 3n(n + 1) + 1; n指六边形层数，一个点为0层</p>
     * @param status 数组型状态
     * @return 六边形不存在返回-1
     */
    public static int layer(char[] status) {
        int layer = 0;
        while (3 * layer * (layer + 1) + 1 < status.length) {
            layer++;
        }
        return 3 * layer * (layer + 1) + 1 == status.length ? layer : -1;
    }

    /**
     * <p>数组型状态转整型状态，最多支持4层的六边形</p>
     * @param charsStatus 数组型状态
     * @return 整型状态
     */
    public static long charsStatusToLongStatus(char[] charsStatus) {
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
     * @param layer 六边形层数
     * @param longStatus 整型状态
     * @return 数组型状态
     */
    public static char[] longStatusToCharsStatus(int layer, long longStatus) {
        char[] charsStatus = new char[3 * layer * (layer + 1) + 1];
        for (int index = 3 * layer * (layer + 1); index >= 0; index--) {
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
        int layer = layer(status);
        for (int index = 0, z = -layer; z <= layer; z++) {
            for (int col = Math.abs(z); col > 0; col--) {
                System.out.print(' ');
            }
            /*
             * 当前行点的个数
             * h(z) = 2 * layer + 1 - |z|；-layer <= z <= layer
             */
            for (int col = 2 * layer + 1 - Math.abs(z); col > 0; col--) {
                if (status[index++] != '0') {
                    System.out.print('●');
                } else {
                    System.out.print('○');
                }
                if (col > 1) {
                    System.out.print(' ');
                }
            }
            System.out.println();
        }
    }

    /**
     * <p>返回x的对称坐标</p>
     * <p>原点对称: 1</p>
     * <p>X轴对称: 2</p>
     * <p>Y轴对称: 4</p>
     * <p>Z轴对称: 8</p>
     * <p>垂直于X轴对称: 16</p>
     * <p>垂直于Y轴对称: 32</p>
     * <p>垂直于Z轴对称: 64</p>
     * @param x
     * @param y
     * @param z
     * @param symmetryType
     * @return
     */
    public static int symmetryX(int x, int y, int z, int symmetryType) {
        switch (symmetryType) {
            case 1:
                return -x;
            case 2:
                return -y;
            case 4:
                return -x;
            case 8:
                return -z;
            case 16:
                return y;
            case 32:
                return x;
            case 64:
                return z;
            default:
        }
        return x;
    }

    /**
     * <p>返回y的对称坐标</p>
     * <p>原点对称: 1</p>
     * <p>X轴对称: 2</p>
     * <p>Y轴对称: 4</p>
     * <p>Z轴对称: 8</p>
     * <p>垂直于X轴对称: 16</p>
     * <p>垂直于Y轴对称: 32</p>
     * <p>垂直于Z轴对称: 64</p>
     * @param x
     * @param y
     * @param z
     * @param symmetryType
     * @return
     */
    public static int symmetryY(int x, int y, int z, int symmetryType) {
        switch (symmetryType) {
            case 1:
                return -y;
            case 2:
                return -x;
            case 4:
                return -z;
            case 8:
                return -y;
            case 16:
                return x;
            case 32:
                return z;
            case 64:
                return y;
            default:
        }
        return y;
    }

    /**
     * <p>返回z的对称坐标</p>
     * <p>原点对称: 1</p>
     * <p>X轴对称: 2</p>
     * <p>Y轴对称: 4</p>
     * <p>Z轴对称: 8</p>
     * <p>垂直于X轴对称: 16</p>
     * <p>垂直于Y轴对称: 32</p>
     * <p>垂直于Z轴对称: 64</p>
     * @param x
     * @param y
     * @param z
     * @param symmetryType
     * @return
     */
    public static int symmetryZ(int x, int y, int z, int symmetryType) {
        switch (symmetryType) {
            case 1:
                return -z;
            case 2:
                return -z;
            case 4:
                return -y;
            case 8:
                return -x;
            case 16:
                return z;
            case 32:
                return y;
            case 64:
                return x;
            default:
        }
        return z;
    }

    /**
     * <p>返回action的对称操作</p>
     * <p>原点对称: 1</p>
     * <p>X轴对称: 2</p>
     * <p>Y轴对称: 4</p>
     * <p>Z轴对称: 8</p>
     * <p>垂直于X轴对称: 16</p>
     * <p>垂直于Y轴对称: 32</p>
     * <p>垂直于Z轴对称: 64</p>
     * @param symmetryType
     * @return
     */
    public static int symmetryAction(int action, int symmetryType) {
        switch (symmetryType) {
            case 2: case 4: case 8: case 16: case 32: case 64:
                switch (action) {
                    case A:
                        return C;
                    case C:
                        return A;
                    default:
                }
            default:
        }
        return action;
    }

    /**
     * <p>逆向action</p>
     * @param action
     * @return
     */
    public static int reverseAction(int action) {
        switch (action) {
            case A:
                return C;
            case C:
                return A;
            default:
        }
        return action;
    }

    /**
     * <p>操作，整型转字符</p>
     * @param action
     * @return
     */
    public static char actionIntToChar(int action) {
        switch (action) {
            case A:
                return 'A';
            case P:
                return 'P';
            case C:
                return 'C';
            default:
        }
        return ' ';
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
     * <p>x，y，z对应一维坐标</p>
     * @param x
     * @param y
     * @param z
     * @return
     */
    public int index(int x, int y, int z) {
        return xyzToIndex[((layer << 1) + 1) * (z + layer) + (z <= 0 ? layer - y : layer + x)];
    }

    public int indexToX(int index) {
        return indexToX[index];
    }

    public int indexToY(int index) {
        return indexToY[index];
    }

    public int indexToZ(int index) {
        return indexToZ[index];
    }

    /**
     * <p>返回对称状态</p>
     * <p>原点对称: 1</p>
     * <p>X轴对称: 2</p>
     * <p>Y轴对称: 4</p>
     * <p>Z轴对称: 8</p>
     * <p>垂直于X轴对称: 16</p>
     * <p>垂直于Y轴对称: 32</p>
     * <p>垂直于Z轴对称: 64</p>
     * @param status
     * @param symmetryType
     * @return
     */
    public long symmetryStatus(long status, int symmetryType) {
        switch (symmetryType) {
            case 1:
                return originSymmetryStatus(status);
            case 2:
                return xAxisSymmetryStatus(status);
            case 4:
                return yAxisSymmetryStatus(status);
            case 8:
                return zAxisSymmetryStatus(status);
            case 16:
                return perpendicularToXAxisSymmetryStatus(status);
            case 32:
                return perpendicularToYAxisSymmetryStatus(status);
            case 64:
                return perpendicularToZAxisSymmetryStatus(status);
            default:
        }
        return status;
    }

    /**
     * <p>原点对称状态</p>
     * @param status
     * @return
     */
    public long originSymmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int symmetryX = -indexToX[index], symmetryY = -indexToY[index], symmetryZ = -indexToZ[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                symmetryStatus |= 1L << symmetryIndex;
            }
        }
        return symmetryStatus;
    }

    /**
     * <p>X轴对称状态</p>
     * @param status
     * @return
     */
    public long xAxisSymmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int symmetryX = -indexToY[index], symmetryY = -indexToX[index], symmetryZ = -indexToZ[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                symmetryStatus |= 1L << symmetryIndex;
            }
        }
        return symmetryStatus;
    }

    /**
     * <p>Y轴对称状态</p>
     * @param status
     * @return
     */
    public long yAxisSymmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int symmetryX = -indexToX[index], symmetryY = -indexToZ[index], symmetryZ = -indexToY[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                symmetryStatus |= 1L << symmetryIndex;
            }
        }
        return symmetryStatus;
    }

    /**
     * <p>Z轴对称状态</p>
     * @param status
     * @return
     */
    public long zAxisSymmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int symmetryX = -indexToZ[index], symmetryY = -indexToY[index], symmetryZ = -indexToX[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                symmetryStatus |= 1L << symmetryIndex;
            }
        }
        return symmetryStatus;
    }

    /**
     * <p>垂直于X轴对称状态</p>
     * @param status
     * @return
     */
    public long perpendicularToXAxisSymmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int symmetryX = indexToY[index], symmetryY = indexToX[index], symmetryZ = indexToZ[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                symmetryStatus |= 1L << symmetryIndex;
            }
        }
        return symmetryStatus;
    }

    /**
     * <p>垂直于Y轴对称状态</p>
     * @param status
     * @return
     */
    public long perpendicularToYAxisSymmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int symmetryX = indexToX[index], symmetryY = indexToZ[index], symmetryZ = indexToY[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                symmetryStatus |= 1L << symmetryIndex;
            }
        }
        return symmetryStatus;
    }

    /**
     * <p>垂直于Z轴对称状态</p>
     * @param status
     * @return
     */
    public long perpendicularToZAxisSymmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int symmetryX = indexToZ[index], symmetryY = indexToY[index], symmetryZ = indexToX[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                symmetryStatus |= 1L << symmetryIndex;
            }
        }
        return symmetryStatus;
    }

    /**
     * <p>返回所有对称类型，每种对称一位二进制保存</p>
     * <p>原点对称: 1</p>
     * <p>X轴对称: 2</p>
     * <p>Y轴对称: 4</p>
     * <p>Z轴对称: 8</p>
     * <p>垂直于X轴对称: 16</p>
     * <p>垂直于Y轴对称: 32</p>
     * <p>垂直于Z轴对称: 64</p>
     * @param status
     * @return
     */
    public int allSymmetryType(long status) {
        int allSymmetryType = 0;
        if (isOriginSymmetry(status)) {
            allSymmetryType = 1;
        }
        if (isXAxisSymmetry(status)) {
            allSymmetryType |= 2;
        }
        if (isYAxisSymmetry(status)) {
            allSymmetryType |= 4;
        }
        if (isZAxisSymmetry(status)) {
            allSymmetryType |= 8;
        }
        if (isPerpendicularToXAxisSymmetry(status)) {
            allSymmetryType |= 16;
        }
        if (isPerpendicularToYAxisSymmetry(status)) {
            allSymmetryType |= 32;
        }
        if (isPerpendicularToZAxisSymmetry(status)) {
            allSymmetryType |= 64;
        }
        return allSymmetryType;
    }

    /**
     * <p>返回两个形状的共同对称，每种对称一位二进制保存</p>
     * <p>原点对称: 1</p>
     * <p>X轴对称: 2</p>
     * <p>Y轴对称: 4</p>
     * <p>Z轴对称: 8</p>
     * <p>垂直于X轴对称: 16</p>
     * <p>垂直于Y轴对称: 32</p>
     * <p>垂直于Z轴对称: 64</p>
     * @param status1
     * @param status2
     * @return
     */
    public int allCommonSymmetryType(long status1, long status2) {
        int allCommonSymmetryType = 0;
        if (isOriginSymmetry(status1) && isOriginSymmetry(status2)) {
            allCommonSymmetryType = 1;
        }
        if (isXAxisSymmetry(status1) && isXAxisSymmetry(status2)) {
            allCommonSymmetryType |= 2;
        }
        if (isYAxisSymmetry(status1) && isYAxisSymmetry(status2)) {
            allCommonSymmetryType |= 4;
        }
        if (isZAxisSymmetry(status1) && isZAxisSymmetry(status2)) {
            allCommonSymmetryType |= 8;
        }
        if (isPerpendicularToXAxisSymmetry(status1) && isPerpendicularToXAxisSymmetry(status2)) {
            allCommonSymmetryType |= 16;
        }
        if (isPerpendicularToYAxisSymmetry(status1) && isPerpendicularToYAxisSymmetry(status2)) {
            allCommonSymmetryType |= 32;
        }
        if (isPerpendicularToZAxisSymmetry(status1) && isPerpendicularToZAxisSymmetry(status2)) {
            allCommonSymmetryType |= 64;
        }
        return allCommonSymmetryType;
    }

    /**
     * <p>关于原点对称</p>
     * @param status
     * @return
     */
    public boolean isOriginSymmetry(long status) {
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                int symmetryX = -indexToX[index], symmetryY = -indexToY[index], symmetryZ = -indexToZ[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                if ((status & (1L << symmetryIndex)) > 0) {
                    // 对称点有值，移除检查的点、对称点
                    status ^= (1L << index) | (1L << symmetryIndex);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>关于X轴对称</p>
     * @param status
     * @return
     */
    public boolean isXAxisSymmetry(long status) {
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                int symmetryX = -indexToY[index], symmetryY = -indexToX[index], symmetryZ = -indexToZ[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                if ((status & (1L << symmetryIndex)) > 0) {
                    // 对称点有值，移除检查的点、对称点
                    status ^= (1L << index) | (1L << symmetryIndex);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>关于Y轴对称</p>
     * @param status
     * @return
     */
    public boolean isYAxisSymmetry(long status) {
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                int symmetryX = -indexToX[index], symmetryY = -indexToZ[index], symmetryZ = -indexToY[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                if ((status & (1L << symmetryIndex)) > 0) {
                    // 对称点有值，移除检查的点、对称点
                    status ^= (1L << index) | (1L << symmetryIndex);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>关于Z轴对称</p>
     * @param status
     * @return
     */
    public boolean isZAxisSymmetry(long status) {
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                int symmetryX = -indexToZ[index], symmetryY = -indexToY[index], symmetryZ = -indexToX[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                if ((status & (1L << symmetryIndex)) > 0) {
                    // 对称点有值，移除检查的点、对称点
                    status ^= (1L << index) | (1L << symmetryIndex);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>关于垂直于X轴对称</p>
     * @param status
     * @return
     */
    public boolean isPerpendicularToXAxisSymmetry(long status) {
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                int symmetryX = indexToY[index], symmetryY = indexToX[index], symmetryZ = indexToZ[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                if ((status & (1L << symmetryIndex)) > 0) {
                    // 对称点有值，移除检查的点、对称点
                    status ^= (1L << index) | (1L << symmetryIndex);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>关于垂直于Y轴对称</p>
     * @param status
     * @return
     */
    public boolean isPerpendicularToYAxisSymmetry(long status) {
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                int symmetryX = indexToX[index], symmetryY = indexToZ[index], symmetryZ = indexToY[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                if ((status & (1L << symmetryIndex)) > 0) {
                    // 对称点有值，移除检查的点、对称点
                    status ^= (1L << index) | (1L << symmetryIndex);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>关于垂直于Z轴对称</p>
     * @param status
     * @return
     */
    public boolean isPerpendicularToZAxisSymmetry(long status) {
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                int symmetryX = indexToZ[index], symmetryY = indexToY[index], symmetryZ = indexToX[index];
                int symmetryIndex = index(symmetryX, symmetryY, symmetryZ);
                if ((status & (1L << symmetryIndex)) > 0) {
                    // 对称点有值，移除检查的点、对称点
                    status ^= (1L << index) | (1L << symmetryIndex);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>以六边形的中心顺时针旋转60°后的状态</p>
     * @param status
     * @return
     */
    public long clockwiseRotationStatus(long status) {
        long rotationStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int rotationX = -indexToZ[index], rotationY = -indexToX[index], rotationZ = -indexToY[index];
                int rotationIndex = index(rotationX, rotationY, rotationZ);
                rotationStatus |= 1L << rotationIndex;
            }
        }
        return rotationStatus;
    }

    /**
     * <p>以六边形的中心逆时针旋转60°后的状态</p>
     * @param status
     * @return
     */
    public long anticlockwiseRotationStatus(long status) {
        long rotationStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点有值
            if ((status & (1L << index)) > 0) {
                // 去除已检查点
                status ^= 1L << index;
                int rotationX = -indexToY[index], rotationY = -indexToZ[index], rotationZ = -indexToX[index];
                int rotationIndex = index(rotationX, rotationY, rotationZ);
                rotationStatus |= 1L << rotationIndex;
            }
        }
        return rotationStatus;
    }

}
