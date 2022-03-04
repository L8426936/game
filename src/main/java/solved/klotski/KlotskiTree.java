package solved.klotski;

import java.util.*;

public class KlotskiTree {

    /**
     * <p>棋子移动模式</p>
     * <p>RIGHT_ANGLE_TURN：移动同一方块为一步</p>
     * <p>STRAIGHT：直线移动为一步</p>
     * <p>ONE_CELL_ONLY：移动一格为一步</p>
     */
    public final static int RIGHT_ANGLE_TURN = 0, STRAIGHT = 1, ONE_CELL_ONLY = 2;

    public List<KlotskiNode> BFS(long status, int mode) {
        Queue<KlotskiNode> queue = new LinkedList<>();
        Map<Long, KlotskiNode> map = new HashMap<>();

        KlotskiNode root = new KlotskiNode();
        root.setStatus(status);

        queue.offer(root);
        map.put(status, root);

        while (!queue.isEmpty()) {
            KlotskiNode parent = queue.poll();
            long parentStatus = parent.getStatus();
            if ((parentStatus & 0X1F81F8000000000L) == 0X1F81F8000000000L) {
                System.out.println("搜索节点数量：" + map.size());
                return buildPassPath(parent);
            }

            int empty1Position = 0, empty2Position = 0, doubleEmptyType = 0;
            for (int index = 0, emptyCount = 0; index < 60 && emptyCount < 2; index += 3) {
                if ((parentStatus & (0X7L << index)) == 0) {
                    empty1Position = empty2Position;
                    empty2Position = index;
                    emptyCount++;
                }
            }

            singleEmpty(map, queue, parent, empty1Position);
            singleEmpty(map, queue, parent, empty2Position);

            if ((empty1Position + 3 == empty2Position) && empty1Position / 12 == empty2Position / 12) {
                // 空格横向联立
                doubleEmptyType = 1;
            } else if (empty1Position + 12 == empty2Position) {
                // 空格纵向联立
                doubleEmptyType = 2;
            }

            if (doubleEmptyType > 0) {
                if (mode != ONE_CELL_ONLY) {
                    doubleEmpty(map, queue, parent, empty1Position, empty2Position, mode);
                    doubleEmpty(map, queue, parent, empty2Position, empty1Position, mode);
                }
                if (doubleEmptyType == 1) {
                    horizontalDoubleEmpty(map, queue, parent, empty1Position, empty2Position, mode);
                } else {
                    verticalDoubleEmpty(map, queue, parent, empty1Position, empty2Position, mode);
                }
            }
        }
        System.out.println("搜索节点数量：" + map.size());
        return null;
    }

    private int[] directions = {-12, 12, -3, 3};

    /**
     * 单个空格
     * @param map
     * @param queue
     * @param parent
     * @param emptyPosition
     */
    private void singleEmpty(Map<Long, KlotskiNode> map, Queue<KlotskiNode> queue, KlotskiNode parent, int emptyPosition) {
        for (int direction = 0; direction < directions.length; direction++) {
            int src = emptyPosition + directions[direction];
            boolean inBound = (src >= 0 && src < 60);
            if (inBound) {
                if (direction == 2 || direction == 3) {
                    inBound = (src / 12 == emptyPosition / 12);
                }
                if (inBound) {
                    long status = parent.getStatus(), type = (status >> src) & 0X7L;
                    if (type == 0X1L) {
                        status ^= (type << src) | (type << emptyPosition);
                    } else if (type >= 0X2L && type <= 0X6L) {
                        int src2 = src + directions[direction];
                        if (src2 >= 0 && src2 < 60 && type == ((status >> src2) & 0X7L)) {
                            status ^= (type << src2) | (type << emptyPosition);
                        }
                    }
                    nextChildNode(map, queue, parent, status, src, emptyPosition);
                }
            }
        }
    }

    /**
     * 空格联立
     * @param map
     * @param queue
     * @param parent
     * @param empty1Position
     * @param empty2Position
     * @param mode
     */
    private void doubleEmpty(Map<Long, KlotskiNode> map, Queue<KlotskiNode> queue, KlotskiNode parent, int empty1Position, int empty2Position, int mode) {
        for (int direction = 0; direction < directions.length; direction++) {
            int src = empty1Position + directions[direction];
            boolean inBound = (src >= 0 && src < 60);
            if (inBound) {
                if (mode == RIGHT_ANGLE_TURN && (direction == 2 || direction == 3)) {
                    inBound = (src / 12 == empty1Position / 12);
                } else if (mode == STRAIGHT) {
                    if (direction == 0 || direction == 1) {
                        inBound = (src % 12 == empty1Position % 12) && (src % 12 == empty2Position % 12);
                    } else if (direction == 2 || direction == 3) {
                        inBound = (src / 12 == empty1Position / 12) && (src / 12 == empty2Position / 12);
                    }
                }
                if (inBound) {
                    long status = parent.getStatus(), type = (status >> src) & 0X7L;
                    if (type == 0X1L) {
                        status ^= (type << src) | (type << empty2Position);
                        nextChildNode(map, queue, parent, status, src, empty2Position);
                    }
                }
            }
        }
    }

    /**
     * 空格水平联立
     * @param map
     * @param queue
     * @param parent
     * @param empty1Position
     * @param empty2Position
     * @param mode
     */
    private void horizontalDoubleEmpty(Map<Long, KlotskiNode> map, Queue<KlotskiNode> queue, KlotskiNode parent, int empty1Position, int empty2Position, int mode) {
        for (int direction = 0; direction < directions.length; direction++) {
            int src1 = empty1Position + directions[direction];
            int src2 = empty2Position + directions[direction];
            boolean inBound = (src1 >= 0 && src1 < 60 && src2 >= 0 && src2 < 60);
            if (inBound) {
                if ((direction == 2 || direction == 3) && (mode == RIGHT_ANGLE_TURN || mode == STRAIGHT)) {
                    src1 += directions[direction];
                    src2 += directions[direction];
                    inBound = (src1 / 12 == empty1Position / 12);
                }
                if (inBound) {
                    long status = parent.getStatus(), type1 = (status >> src1) & 0X7L, type2 = (status >> src2) & 0X7L;
                    if (type1 == type2 && type1 >= 0X2L && type2 <= 0X6L) {
                        status ^= (type1 << src1) | (type2 << src2) | (type1 << empty1Position) | (type2 << empty2Position);
                    } else if (type1 == 0X7L && type2 == 0X7L && (direction == 0 || direction == 1)) {
                        status ^= (type1 << (src1 + directions[direction])) | (type2 << (src2 + directions[direction])) | (type1 << empty1Position) | (type2 << empty2Position);
                    }
                    nextChildNode(map, queue, parent, status, src1, empty1Position);
                }
            }
        }
    }

    /**
     * 空格竖直联立
     * @param map
     * @param queue
     * @param parent
     * @param empty1Position
     * @param empty2Position
     * @param mode
     */
    private void verticalDoubleEmpty(Map<Long, KlotskiNode> map, Queue<KlotskiNode> queue, KlotskiNode parent, int empty1Position, int empty2Position, int mode) {
        for (int direction = 0; direction < directions.length; direction++) {
            int src1 = empty1Position + directions[direction];
            int src2 = empty2Position + directions[direction];
            if ((direction == 0 || direction == 1) && (mode == RIGHT_ANGLE_TURN || mode == STRAIGHT)) {
                src1 += directions[direction];
                src2 += directions[direction];
            }
            boolean inBound = (src1 >= 0 && src1 < 60 && src2 >= 0 && src2 < 60);
            if (inBound) {
                if (direction == 2 || direction == 3) {
                    inBound = (src1 / 12 == empty1Position / 12);
                }
                if (inBound) {
                    long status = parent.getStatus(), type1 = (status >> src1) & 0X7L, type2 = (status >> src2) & 0X7L;
                    if (type1 == type2 && type1 >= 0X2L && type2 <= 0X6L) {
                        status ^= (type1 << src1) | (type2 << src2) | (type1 << empty1Position) | (type2 << empty2Position);
                    } else if (type1 == 0X7L && type2 == 0X7L && (direction == 2 || direction == 3)) {
                        status ^= (type1 << (src1 + directions[direction])) | (type2 << (src2 + directions[direction])) | (type1 << empty1Position) | (type2 << empty2Position);
                    }
                    nextChildNode(map, queue, parent, status, src1, empty1Position);
                }
            }
        }
    }

    private void nextChildNode(Map<Long, KlotskiNode> map, Queue<KlotskiNode> queue, KlotskiNode parent, long status, int src, int dest) {
        if (map.get(status) == null && map.get(symmetryStatus(status)) == null) {
            KlotskiNode child = new KlotskiNode();
            child.setParent(parent);
            child.setStatus(status);
            child.setSrc(src);
            child.setDest(dest);
            queue.offer(child);
            map.put(status, child);
        }
    }

    private List<KlotskiNode> buildPassPath(KlotskiNode parent) {
        List<KlotskiNode> passPath = new LinkedList<>();
        while (parent != null) {
            passPath.add(0, parent);
            parent = parent.getParent();
        }
        return passPath;
    }

    private static long symmetryStatus(long status) {
        long symmetryStatus = 0;
        for (int index = 0; index < 60; index += 12) {
            symmetryStatus |= ((status >> index) & 0X7L) << (index + 9);
            symmetryStatus |= ((status >> (index + 3)) & 0X7L) << (index + 6);
            symmetryStatus |= ((status >> (index + 6)) & 0X7L) << (index + 3);
            symmetryStatus |= ((status >> (index + 9)) & 0X7L) << index;
        }
        return symmetryStatus;
    }
}
