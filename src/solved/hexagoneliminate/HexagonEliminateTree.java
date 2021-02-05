package solved.hexagoneliminate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class HexagonEliminateTree {
    // public static final long[] DOTS = {0X1}; // 点
    // public static final long[] HEAPS = {0X63, 0X1061, 0XC3}; // 堆
    // public static final long[] HOOKS = {0X47, 0X2061, 0X1043, 0XE2, 0X20C1, 0X1821, 0XE1, 0X2043, 0X861, 0X87, 0X3041, 0X1062}; // 钩
    // public static final long[] SMALL_HALF_RINGS = {0X1023, 0XA3, 0X2083, 0X3021, 0XC5, 0X1841}; // 半环
    // public static final long[] LINES = {0X202041, 0XF, 0X40821}; // 线

    public static final int LAYER = 4;
    private static final long[] ELIMINATE_LINE = new long[LAYER > 0 ? 3 * (2 * LAYER + 1) : 1];  // 六边形所有消除线
    private static final long[] RING = new long[LAYER + 1];  // 六边形环
    private static final long[][] SHAPE_POSITION = new long[25][]; // 所有形状的有效位置

    static {
        // 六边形环
        int[][] neighbor = {{1, -1, 0}, {0, -1, 1}, {-1, 0, 1}, {-1, 1, 0}, {0, 1, -1}, {1, 0, -1}};
        for (int layer = LAYER; layer >= 0; layer--) {
            int x = 0, y = layer, z = -layer;
            long status = 1L << HexagonEliminateUtil.index(x, y, z);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < layer; j++) {
                    x += neighbor[i][0];
                    y += neighbor[i][1];
                    z += neighbor[i][2];
                    status |= 1L << HexagonEliminateUtil.index(x, y, z);
                }
            }
            RING[layer] = status;
        }
        // for (int i = 0; i < RING.length; i++) {
        //     HexagonEliminateUtil.printlnStatus(HexagonEliminateUtil.longToArray(RING[i]));
        //     System.out.println();
        // }

        for (int index = 0, z = -LAYER; z <= LAYER; z++, index += 3) {
            for (int x = -LAYER; x <= LAYER; x++) {
                for (int y = -LAYER; y <= LAYER; y++) {
                    if (x + y + z == 0) {
                        ELIMINATE_LINE[index] |= 1L << HexagonEliminateUtil.index(x, y, z);
                        ELIMINATE_LINE[index + 1] |= 1L << HexagonEliminateUtil.index(y, z, x);
                        ELIMINATE_LINE[index + 2] |= 1L << HexagonEliminateUtil.index(z, x, y);
                    }
                }
            }
        }
        // System.out.println(Arrays.toString(ELIMINATE_LINE));
        // for (int i = 0; i < ELIMINATE_LINE.length; i++) {
        //     HexagonEliminateUtil.printlnStatus(HexagonEliminateUtil.longToArray(ELIMINATE_LINE[i]));
        //     System.out.println();
        // }

        // 所有形状的所有可能位置
        int[] shapeTypeCount = {
                1, // 点的数量
                3, // 线的数量
                3, // 堆的数量
                6, // 半环的数量
                12, // 钩的数量
        }, shapePositionCount = {
                3 * LAYER * (LAYER + 1) + 1, // 点的有效位置数量
                LAYER > 1 ? 3 * LAYER * (LAYER - 1) - 2 : 0, // 线的有效位置数量
                3 * LAYER * (LAYER - 1) + 2 * LAYER, // 堆的有效位置数量
                3 * LAYER * (LAYER - 1) + LAYER, // 半环的有效位置数量
                3 * LAYER * (LAYER - 1) + LAYER, // 钩的有效位置数量
        };
        for (int i = 0, type = 0; i < shapeTypeCount.length; i++) {
            for (int j = 0; j < shapeTypeCount[i]; j++) {
                SHAPE_POSITION[type++] = new long[shapePositionCount[i]];
            }
        }
        /*
         *  0 1
         * 2 3 4
         *  5 6
         */
        int[] smallHexagon = new int[7];
        int[] shapeTypeIndex = new int[Arrays.stream(shapeTypeCount).sum()];
        for (int index = 0; index < 3 * LAYER * (LAYER + 1) + 1; index++) {
            int x = HexagonEliminateUtil.indexToX(index), y = HexagonEliminateUtil.indexToY(index), z = HexagonEliminateUtil.indexToZ(index);
            smallHexagon[0] = (y + 1 <= LAYER && -LAYER <= z - 1) ? HexagonEliminateUtil.index(x, y + 1, z - 1) : -1;
            smallHexagon[1] = (x + 1 <= LAYER && -LAYER <= z - 1) ? HexagonEliminateUtil.index(x + 1, y, z - 1) : -1;
            smallHexagon[2] = (-LAYER <= x - 1 && y + 1 <= LAYER) ? HexagonEliminateUtil.index(x - 1, y + 1, z) : -1;
            smallHexagon[3] = HexagonEliminateUtil.index(x, y, z);
            smallHexagon[4] = (x + 1 <= LAYER && -LAYER <= y - 1) ? HexagonEliminateUtil.index(x + 1, y - 1, z) : -1;
            smallHexagon[5] = (-LAYER <= x - 1 && z + 1 <= LAYER) ? HexagonEliminateUtil.index(x - 1, y, z + 1) : -1;
            smallHexagon[6] = (-LAYER <= y - 1 && z + 1 <= LAYER) ? HexagonEliminateUtil.index(x, y - 1, z + 1) : -1;
            SHAPE_POSITION[0][index] = 1L << index; // 点的有效位置
            // 线的所有有效位置
            if (-LAYER <= y - 3 && y - 3 <= LAYER && -LAYER <= z + 3 && z + 3 <= LAYER) {
                SHAPE_POSITION[1][shapeTypeIndex[1]++] = (1L << HexagonEliminateUtil.index(x, y, z)) | (1L << HexagonEliminateUtil.index(x, y - 1, z + 1)) | (1L << HexagonEliminateUtil.index(x, y - 2, z + 2)) | (1L << HexagonEliminateUtil.index(x, y - 3, z + 3));
            }
            if (-LAYER <= x + 3 && x + 3 <= LAYER && -LAYER <= y - 3 && y - 3 <= LAYER) {
                SHAPE_POSITION[2][shapeTypeIndex[2]++] = (1L << HexagonEliminateUtil.index(x, y, z)) | (1L << HexagonEliminateUtil.index(x + 1, y - 1, z)) | (1L << HexagonEliminateUtil.index(x + 2, y - 2, z)) | (1L << HexagonEliminateUtil.index(x + 3, y - 3, z));
            }
            if (-LAYER <= x - 3 && x - 3 <= LAYER && -LAYER <= z + 3 && z + 3 <= LAYER) {
                SHAPE_POSITION[3][shapeTypeIndex[3]++] = (1L << HexagonEliminateUtil.index(x, y, z)) | (1L << HexagonEliminateUtil.index(x - 1, y, z + 1)) | (1L << HexagonEliminateUtil.index(x - 2, y, z + 2)) | (1L << HexagonEliminateUtil.index(x - 3, y, z + 3));
            }
            // 堆的所有有效位置
            if ((smallHexagon[0] | smallHexagon[1] | smallHexagon[2] | smallHexagon[3]) > 0) {
                SHAPE_POSITION[4][shapeTypeIndex[4]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[1]) | (1L << smallHexagon[2]) | (1L << smallHexagon[3]);
            }
            if ((smallHexagon[0] | smallHexagon[2] | smallHexagon[3] | smallHexagon[5]) > 0) {
                SHAPE_POSITION[5][shapeTypeIndex[5]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[2]) | (1L << smallHexagon[3]) | (1L << smallHexagon[5]);
            }
            if ((smallHexagon[0] | smallHexagon[1] | smallHexagon[3] | smallHexagon[4]) > 0) {
                SHAPE_POSITION[6][shapeTypeIndex[6]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[1]) | (1L << smallHexagon[3]) | (1L << smallHexagon[4]);
            }
            // 半环的所有有效位置
            if ((smallHexagon[0] | smallHexagon[1] | smallHexagon[2] | smallHexagon[5]) > 0) {
                SHAPE_POSITION[7][shapeTypeIndex[7]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[1]) | (1L << smallHexagon[2]) | (1L << smallHexagon[5]);
            }
            if ((smallHexagon[0] | smallHexagon[1] | smallHexagon[2] | smallHexagon[4]) > 0) {
                SHAPE_POSITION[8][shapeTypeIndex[8]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[1]) | (1L << smallHexagon[2]) | (1L << smallHexagon[4]);
            }
            if ((smallHexagon[0] | smallHexagon[1] | smallHexagon[4] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[9][shapeTypeIndex[9]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[1]) | (1L << smallHexagon[4]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[0] | smallHexagon[2] | smallHexagon[5] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[10][shapeTypeIndex[10]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[2]) | (1L << smallHexagon[5]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[2] | smallHexagon[4] | smallHexagon[5] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[11][shapeTypeIndex[11]++] = (1L << smallHexagon[2]) | (1L << smallHexagon[4]) | (1L << smallHexagon[5]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[1] | smallHexagon[4] | smallHexagon[5] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[12][shapeTypeIndex[12]++] = (1L << smallHexagon[1]) | (1L << smallHexagon[4]) | (1L << smallHexagon[5]) | (1L << smallHexagon[6]);
            }
            // 钩的所有有效位置
            if ((smallHexagon[2] | smallHexagon[3] | smallHexagon[4] | smallHexagon[5]) > 0) {
                SHAPE_POSITION[13][shapeTypeIndex[13]++] = (1L << smallHexagon[2]) | (1L << smallHexagon[3]) | (1L << smallHexagon[4]) | (1L << smallHexagon[5]);
            }
            if ((smallHexagon[0] | smallHexagon[2] | smallHexagon[3] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[14][shapeTypeIndex[14]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[2]) | (1L << smallHexagon[3]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[0] | smallHexagon[1] | smallHexagon[3] | smallHexagon[5]) > 0) {
                SHAPE_POSITION[15][shapeTypeIndex[15]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[1]) | (1L << smallHexagon[3]) | (1L << smallHexagon[5]);
            }
            if ((smallHexagon[1] | smallHexagon[2] | smallHexagon[3] | smallHexagon[4]) > 0) {
                SHAPE_POSITION[16][shapeTypeIndex[16]++] = (1L << smallHexagon[1]) | (1L << smallHexagon[2]) | (1L << smallHexagon[3]) | (1L << smallHexagon[4]);
            }
            if ((smallHexagon[0] | smallHexagon[3] | smallHexagon[4] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[17][shapeTypeIndex[17]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[3]) | (1L << smallHexagon[4]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[1] | smallHexagon[3] | smallHexagon[5] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[18][shapeTypeIndex[18]++] = (1L << smallHexagon[1]) | (1L << smallHexagon[3]) | (1L << smallHexagon[5]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[0] | smallHexagon[2] | smallHexagon[3] | smallHexagon[4]) > 0) {
                SHAPE_POSITION[19][shapeTypeIndex[19]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[2]) | (1L << smallHexagon[3]) | (1L << smallHexagon[4]);
            }
            if ((smallHexagon[0] | smallHexagon[1] | smallHexagon[3] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[20][shapeTypeIndex[20]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[1]) | (1L << smallHexagon[3]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[1] | smallHexagon[3] | smallHexagon[4] | smallHexagon[5]) > 0) {
                SHAPE_POSITION[21][shapeTypeIndex[21]++] = (1L << smallHexagon[1]) | (1L << smallHexagon[3]) | (1L << smallHexagon[4]) | (1L << smallHexagon[5]);
            }
            if ((smallHexagon[2] | smallHexagon[3] | smallHexagon[4] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[22][shapeTypeIndex[22]++] = (1L << smallHexagon[2]) | (1L << smallHexagon[3]) | (1L << smallHexagon[4]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[0] | smallHexagon[3] | smallHexagon[5] | smallHexagon[6]) > 0) {
                SHAPE_POSITION[23][shapeTypeIndex[23]++] = (1L << smallHexagon[0]) | (1L << smallHexagon[3]) | (1L << smallHexagon[5]) | (1L << smallHexagon[6]);
            }
            if ((smallHexagon[1] | smallHexagon[2] | smallHexagon[3] | smallHexagon[5]) > 0) {
                SHAPE_POSITION[24][shapeTypeIndex[24]++] = (1L << smallHexagon[1]) | (1L << smallHexagon[2]) | (1L << smallHexagon[3]) | (1L << smallHexagon[5]);
            }
        }
        // for (int i = 0; i < SHAPE_POSITION.length; i++) {
        //     System.out.println("type: " + i);
        //     HexagonEliminateUtil.printlnStatus(HexagonEliminateUtil.longToArray(SHAPE_POSITION[i][0]));
        //     System.out.println();
        // }
    }

    public static Move bestMove(long status, HexagonEliminatePlayer.ShapeType[] shapeTypes) {
        int[] sequence = new int[shapeTypes.length];
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = i;
        }
        HexagonEliminateNode best = new HexagonEliminateNode();
        best.setScore(Integer.MIN_VALUE);
        int count = 0;
        do {
            Queue<HexagonEliminateNode> treeLayer = new LinkedList<>();
            HexagonEliminateNode root = new HexagonEliminateNode();
            root.setStatus(status);
            treeLayer.offer(root);
            for (int i = 0; i < sequence.length; i++) {
                long[] position = SHAPE_POSITION[shapeTypes[sequence[i]].getType()];
                for (int j = 0, k = treeLayer.size(); j < k; j++) {
                    HexagonEliminateNode parent = treeLayer.poll();
                    for (int m = 0; m < position.length; m++) {
                        if ((parent.getStatus() & position[m]) == 0) {
                            HexagonEliminateNode hexagonEliminateNode = move(parent, position[m]);
                            hexagonEliminateNode.setShapeTypeIndex(sequence[i]);
                            treeLayer.offer(hexagonEliminateNode);
                        }
                    }
                }
            }
            count += treeLayer.size();
            while (!treeLayer.isEmpty()) {
                HexagonEliminateNode hexagonEliminateNode = treeLayer.poll();
                if (hexagonEliminateNode.getScore() > best.getScore()) {
                    best = hexagonEliminateNode;
                }
            }
        } while (nextSequence(sequence));

        while (best.getParent() != null) {
            HexagonEliminateNode parent = best.getParent();
            if (parent.getParent() == null) {
                break;
            }
            best = parent;
        }
        Move move = new Move();
        move.shapePosition = best.getShapePosition();
        move.shapeTypeIndex = best.getShapeTypeIndex();
        move.score += best.getScore();
        move.status += best.getStatus();
        System.out.println("一共有" + count + "种摆法方式");
        return best.getParent() != null ? move : null;
    }

    public static boolean nextSequence(int[] array) {
        for (int i = array.length - 2; i >= 0; i--) {
            if (array[i] < array[i + 1]) {
                int j = -1;
                int min = Integer.MAX_VALUE;
                for (int k = i; k < array.length; k++) {
                    if (array[k] > array[i] && array[k] <= min) {
                        min = array[k];
                        j = k;
                    }
                }
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
                for (int left = i + 1, right = array.length - 1; left < right; left++, right--) {
                    temp = array[left];
                    array[left] = array[right];
                    array[right] = temp;
                }
                return true;
            }
        }
        return false;
    }

    private static HexagonEliminateNode move(HexagonEliminateNode parent, long shape) {
        long parentStatus = parent.getStatus();
        long childStatus = shape | parentStatus, eliminateLines = 0;
        int score = 0;
        int eliminateLineCount = 0;
        for (int i = 0; i < ELIMINATE_LINE.length; i++) {
            long eliminateLine = childStatus & ELIMINATE_LINE[i];
            if (eliminateLine == ELIMINATE_LINE[i]) {
                // 消除越多越好
                eliminateLines |= eliminateLine;
                eliminateLineCount++;
            } else {
                // 铺满消除线的数量越多越好
                score += 5 * HexagonEliminateUtil.bitCount(ELIMINATE_LINE[i] ^ eliminateLine);
            }
        }
        score += (25 * eliminateLineCount) * HexagonEliminateUtil.bitCount(eliminateLines);

        // 越贴近最外环最好
        childStatus ^= eliminateLines;
        for (int i = 0; i < RING.length; i++) {
            if ((childStatus & RING[i]) > 0) {
                score += i * HexagonEliminateUtil.bitCount(childStatus & RING[i]);
            }
        }

        HexagonEliminateNode child = new HexagonEliminateNode();
        child.setParent(parent);
        child.setStatus(childStatus);
        child.setShapePosition(shape);
        child.setScore(parent.getScore() + score);

        return child;
    }

    public static class Move {
        private long shapePosition;
        private int shapeTypeIndex;
        public long score, status;

        public long getShapePosition() {
            return shapePosition;
        }

        public void setShapePosition(long shapePosition) {
            this.shapePosition = shapePosition;
        }

        public int getShapeTypeIndex() {
            return shapeTypeIndex;
        }

        public void setShapeTypeIndex(int shapeTypeIndex) {
            this.shapeTypeIndex = shapeTypeIndex;
        }
    }
}
