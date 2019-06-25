package solved.evlover;

public class EvloverNodeUtil {
    private int layer;
    private int[] rowPreviousSum, indexToX, indexToY, indexToZ;
    public final static long[] BINARY_VALUE = new long[37];
    static {
        for (int i = 0; i < BINARY_VALUE.length; i++) {
            BINARY_VALUE[i] = 1L << i;
        }
    }

    private void init() {
        rowPreviousSum = rowPreviousSum(layer);
        indexToX = new int[3 * layer * (layer + 1) + 1];
        indexToY = new int[3 * layer * (layer + 1) + 1];
        indexToZ = new int[3 * layer * (layer + 1) + 1];
        for (int row = layer * 2, col = layer, index = 0, xValue = 0, yValue = layer, zValue = -layer;
             row >= 0; row--, zValue++) {
            for (int c = 0; c <= col; c++) {
                indexToX[index] = xValue + c;
                indexToY[index] = yValue - c;
                indexToZ[index++] = zValue;
            }
            if (row > layer) {
                xValue--;
                col++;
            } else {
                yValue--;
                col--;
            }
        }
        // System.out.println(Arrays.toString(indexToX));
        // System.out.println(Arrays.toString(indexToY));
        // System.out.println(Arrays.toString(indexToZ));
    }

    public EvloverNodeUtil(int layer) {
        this.layer = layer;
        init();
    }

    /**
     * <p>六边形个数和公式: 3n * (n-1) + 1; n指六边形层数</p>
     * <p>一个六边形为0层</p>
     * @param status
     * @return 返回层数，不存在返回-1
     */
    public static int layer(int[] status) {
        if (status.length % 6 != 1) {
            return -1;
        }
        int layer = 0, sum = (status.length - 1) / 3;
        for (int i = 0; i * i - i != sum; layer = i++) {

        }
        return layer;
    }

    /**
     * 二进制形式的数组转long值
     * @param binaries
     * @return
     */
    public static long binaryToLong(int[] binaries) {
        long status = 0;
        for (int i = 0; i < binaries.length; i++) {
            if (binaries[i] == 1) {
                status += BINARY_VALUE[i];
            }
        }
        return status;
    }

    /**
     * 行数前的点数量
     * @param layer
     * @return
     */
    public static int[] rowPreviousSum(int layer) {
        int[] rowPreviousSum = new int[layer * 2 + 1];
        for (int i = 0, sum = 0, offset = 1, n = layer + 1; i < rowPreviousSum.length; i++, sum += n, n += offset) {
            if (i == layer) {
                offset = -1;
            }
            rowPreviousSum[i] = sum;
        }
        return rowPreviousSum;
    }

    /**
     * 二进制1的个数
     * @param status
     * @return
     */
    public static int bitCount(long status) {
        int bitCount = 0;
        while (status > 0) {
            status &= status - 1;
            bitCount++;
        }
        return bitCount;
    }

    public static void printlnStatus(int layer, long status) {
        int[] rowPreviousSum = rowPreviousSum(layer);
        for (int row = 0, totalRow = layer * 2 + 1, offset = 1, col = layer + 1;
             row < totalRow; row++, col += offset) {
            if (row >= layer) {
                offset = -1;
            }

            for (int k = layer * 2 + 1 - col; k > 0; k--) {
                System.out.print(' ');
            }

            for (int k = 0; k < col; k++) {
                long value = status & BINARY_VALUE[rowPreviousSum[row] + k];
                if (value != 0) {
                    System.out.format("%c ", '●');
                } else {
                    System.out.format("%c ", '○');
                }
            }
            System.out.println();
        }
    }

    /**
     * 回溯节点步数
     * @param evloverNode
     * @return
     */
    public static int step(EvloverNode evloverNode) {
        int step = 0;
        while (evloverNode.getParent() != null) {
            evloverNode = evloverNode.getParent();
            step++;
        }
        return step;
    }

    /**
     * x, y, z对应的一维数组索引
     * @param x
     * @param y
     * @param z
     * @return
     */
    public int index(int x, int y, int z) {
        int col = z <= 0 ? layer - y : layer + x;
        return rowPreviousSum[layer + z] + col;
    }

    /**
     * <p>返回对称状态</p>
     * <p>原点对称: 0</p>
     * <p>X轴对称: 1</p>
     * <p>Y轴对称: 2</p>
     * <p>Z轴对称: 3</p>
     * <p>垂直于X轴对称: 4</p>
     * <p>垂直于Y轴对称: 5</p>
     * <p>垂直于Z轴对称: 6</p>
     * @param status
     * @param symmetricType
     * @return
     */
    public long symmetricStatus(long status, int symmetricType) {
        switch (symmetricType) {
            case 0:
                return originSymmetricStatus(status);
            case 1:
                return xAxiSymmetricStatus(status);
            case 2:
                return yAxiSymmetricStatus(status);
            case 3:
                return zAxiSymmetricStatus(status);
            case 4:
                return perpendicularToXAxiSymmetricStatus(status);
            case 5:
                return perpendicularToYAxiSymmetricStatus(status);
            case 6:
                return perpendicularToZAxiSymmetricStatus(status);
            default:
        }
        return status;
    }

    /**
     * 原点对称
     * @param status
     * @return
     */
    public long originSymmetricStatus(long status) {
        long symmetricStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & BINARY_VALUE[index]) > 0) {
                // 去除已检查点
                status ^= BINARY_VALUE[index];
                int symmetricX = -indexToX[index];
                int symmetricY = -indexToY[index];
                int symmetricZ = -indexToZ[index];
                int symmetricIndex = index(symmetricX, symmetricY, symmetricZ);
                symmetricStatus ^= BINARY_VALUE[symmetricIndex];
            }
        }
        return symmetricStatus;
    }

    /**
     * X轴对称
     * @param status
     * @return
     */
    public long xAxiSymmetricStatus(long status) {
        long symmetricStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & BINARY_VALUE[index]) > 0) {
                // 去除已检查点
                status ^= BINARY_VALUE[index];
                int symmetricX = -indexToX[index];
                int symmetricY = -indexToZ[index];
                int symmetricZ = -indexToY[index];
                int symmetricIndex = index(symmetricX, symmetricY, symmetricZ);
                symmetricStatus ^= BINARY_VALUE[symmetricIndex];
            }
        }
        return symmetricStatus;
    }

    /**
     * Y轴对称
     * @param status
     * @return
     */
    public long yAxiSymmetricStatus(long status) {
        long symmetricStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & BINARY_VALUE[index]) > 0) {
                // 去除已检查点
                status ^= BINARY_VALUE[index];
                int symmetricX = -indexToZ[index];
                int symmetricY = -indexToY[index];
                int symmetricZ = -indexToX[index];
                int symmetricIndex = index(symmetricX, symmetricY, symmetricZ);
                symmetricStatus ^= BINARY_VALUE[symmetricIndex];
            }
        }
        return symmetricStatus;
    }

    /**
     * Z轴对称
     * @param status
     * @return
     */
    public long zAxiSymmetricStatus(long status) {
        long symmetricStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & BINARY_VALUE[index]) > 0) {
                // 去除已检查点
                status ^= BINARY_VALUE[index];
                int symmetricX = -indexToY[index];
                int symmetricY = -indexToX[index];
                int symmetricZ = -indexToZ[index];
                int symmetricIndex = index(symmetricX, symmetricY, symmetricZ);
                symmetricStatus ^= BINARY_VALUE[symmetricIndex];
            }
        }
        return symmetricStatus;
    }

    /**
     * 垂直于X轴对称
     * @param status
     * @return
     */
    public long perpendicularToXAxiSymmetricStatus(long status) {
        long symmetricStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & BINARY_VALUE[index]) > 0) {
                // 去除已检查点
                status ^= BINARY_VALUE[index];
                int symmetricX = indexToX[index];
                int symmetricY = indexToZ[index];
                int symmetricZ = indexToY[index];
                int symmetricIndex = index(symmetricX, symmetricY, symmetricZ);
                symmetricStatus ^= BINARY_VALUE[symmetricIndex];
            }
        }
        return symmetricStatus;
    }

    /**
     * 垂直于Y轴对称
     * @param status
     * @return
     */
    public long perpendicularToYAxiSymmetricStatus(long status) {
        long symmetricStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & BINARY_VALUE[index]) > 0) {
                // 去除已检查点
                status ^= BINARY_VALUE[index];
                int symmetricX = indexToZ[index];
                int symmetricY = indexToY[index];
                int symmetricZ = indexToX[index];
                int symmetricIndex = index(symmetricX, symmetricY, symmetricZ);
                symmetricStatus ^= BINARY_VALUE[symmetricIndex];
            }
        }
        return symmetricStatus;
    }

    /**
     * 垂直于Z轴对称
     * @param status
     * @return
     */
    public long perpendicularToZAxiSymmetricStatus(long status) {
        long symmetricStatus = 0;
        for (int index = 0; status > 0; index++) {
            // 当前点和对称点有值
            if ((status & BINARY_VALUE[index]) > 0) {
                // 去除已检查点
                status ^= BINARY_VALUE[index];
                int symmetricX = indexToY[index];
                int symmetricY = indexToX[index];
                int symmetricZ = indexToZ[index];
                int symmetricIndex = index(symmetricX, symmetricY, symmetricZ);
                symmetricStatus ^= BINARY_VALUE[symmetricIndex];
            }
        }
        return symmetricStatus;
    }

}
