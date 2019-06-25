package solved.evlover;

import solved.util.AVLTree;
import solved.util.LinkedList;

public class EvloverNodeTree {
    private int layer;

    private EvloverNodeUtil evloverNodeUtil;

    private int[] clickPointX, clickPointY, clickPointZ;

    private LinkedList<EvloverNode> startQueue = new LinkedList<>();
    private LinkedList<EvloverNode> endQueue = new LinkedList<>();
    private AVLTree<EvloverNode> startAVLTree = new AVLTree<>();
    private AVLTree<EvloverNode> endAVLTree = new AVLTree<>();

    /**
     * 初始化有效的点击位置
     */
    private void initClickPoint() {
        clickPointX = new int[3 * layer * (layer - 1) + 1];
        clickPointY = new int[3 * layer * (layer - 1) + 1];
        clickPointZ = new int[3 * layer * (layer - 1) + 1];
        for (int row = (layer - 1) * 2, col = layer - 1, index = 0, xValue = 0, yValue = layer - 1, zValue = -(layer - 1);
             row >= 0; row--, zValue++) {
            for (int c = 0; c <= col; c++) {
                clickPointX[index] = xValue + c;
                clickPointY[index] = yValue - c;
                clickPointZ[index++] = zValue;
            }
            if (row > layer - 1) {
                xValue--;
                col++;
            } else {
                yValue--;
                col--;
            }
        }
        // System.out.println(Arrays.toString(clickPointX));
        // System.out.println(Arrays.toString(clickPointY));
        // System.out.println(Arrays.toString(clickPointZ));
    }

    public EvloverNodeTree(int[] startStatus, int[] endStatus) {
        long startEvloverStatus = EvloverNodeUtil.binaryToLong(startStatus);
        long endEvloverStatus = EvloverNodeUtil.binaryToLong(endStatus);
        if (startEvloverStatus != endEvloverStatus
                && EvloverNodeUtil.bitCount(startEvloverStatus) == EvloverNodeUtil.bitCount(endEvloverStatus)) {
            layer = EvloverNodeUtil.layer(startStatus);

            initClickPoint();

            evloverNodeUtil = new EvloverNodeUtil(layer);

            EvloverNode startEvloverNode = new EvloverNode();
            startEvloverNode.setStatus(startEvloverStatus);
            EvloverNode endEvloverNode = new EvloverNode();
            endEvloverNode.setStatus(endEvloverStatus);

            startAVLTree.put(startEvloverNode.getStatus(), startEvloverNode);
            startQueue.offer(startEvloverNode);

            endAVLTree.put(endEvloverNode.getStatus(), endEvloverNode);
            endQueue.offer(endEvloverNode);
        }
    }

    /**
     * <p>优先点对称交换</p>
     * <p>C: 点对称交换</p>
     * <p>L: 顺时针旋转</p>
     * <p>R: 逆时针旋转</p>
     */
    private final static char[] ACTIONS = {'C', 'L', 'R'};
    /**
     * 双向广度优先搜索 bidirectionalSearch
     * @return
     */
    public EvloverNode[] bidirectionalBreadthFirstSearch() {
        LinkedList<EvloverNode> queue;
        AVLTree<EvloverNode> insertAVLTree, checkAVLTree;
        while (!startQueue.isEmpty() || !endQueue.isEmpty()) {
            // 优先扩展节点少的树
            if (startQueue.size() < endQueue.size() && !startQueue.isEmpty()) {
                queue = startQueue;
                insertAVLTree = startAVLTree;
                checkAVLTree = endAVLTree;
				ACTIONS[1] = 'L';
				ACTIONS[2] = 'R';
            } else {
                queue = endQueue;
                insertAVLTree = endAVLTree;
                checkAVLTree = startAVLTree;
				ACTIONS[1] = 'R';
				ACTIONS[2] = 'L';
            }
            LinkedList<EvloverNode> nextLayer = new LinkedList<>();
            while (!queue.isEmpty()) {
                EvloverNode parent = queue.poll();
                for (int i = 0; i < ACTIONS.length; i++) {
					int startIndex = 0, flag = clickPointX.length, offset = 1;
					if (queue == endQueue) {
						startIndex = clickPointX.length - 1;
						flag = -1;
						offset = -1;
					}
                    for (int j = startIndex; j != flag; j += offset) {
                        int x = clickPointX[j];
                        int y = clickPointY[j];
                        int z = clickPointZ[j];
                        long childStatus = nextStep(parent.getStatus(), x, y, z, ACTIONS[i]);
                        
						if (childStatus != parent.getStatus() && insertAVLTree.search(childStatus) == null) {
							boolean add = true;
							for (int symmetricType = 0; symmetricType < 7; symmetricType++) {
								long symmetricStatus = evloverNodeUtil.symmetricStatus(childStatus, symmetricType);
								if (symmetricStatus != childStatus && symmetricStatus != parent.getStatus() && insertAVLTree.search(symmetricStatus) == parent) {
									add = false;
									break;
								}
							}
							if (add) {
								EvloverNode child = new EvloverNode();
								child.setParent(parent);
								child.setStatus(childStatus);
								child.setX(x);
								child.setY(y);
								child.setZ(z);
								child.setAction(ACTIONS[i]);
								insertAVLTree.put(childStatus, child);
								nextLayer.offer(child);
								EvloverNode[] evloverNodes = searchPassPath(child, checkAVLTree);
								if (evloverNodes != null) {
                                    System.out.format("开始队列长度%d 结束队列长度%d 开始树节点%d 结束树节点%d%n", startQueue.size(), endQueue.size(), startAVLTree.size(), endAVLTree.size());
                                    return evloverNodes;
								}
							}
						}
						
                    }
                }
                // 点击次数：((layer - 1) * 2 + 1)^3；上面方式的点击次数：3 * layer * (layer - 1) + 1
                // for (int i = 0; i < ACTIONS.length; i++) {
                //     for (int x = -(this.layer - 1); x < this.layer; x++) {
                //         for (int y = -(this.layer - 1); y < this.layer; y++) {
                //             for (int z = -(this.layer - 1); z < this.layer; z++) {
                //                 if (x + y + z == 0) {
                //                      ......
                //                 }
                //             }
                //         }
                //     }
                // }
            }
            if (queue == startQueue) {
                startQueue = nextLayer;
            } else {
                endQueue = nextLayer;
            }
        }
        return null;
    }

    private long nextStep(long parentStatus, int x, int y, int z, char action) {
        long childStatus = parentStatus;
        switch (action) {
            case 'C':
                childStatus = pointSymmetricShift(parentStatus, x, y, z);
                break;
            case 'L':
                childStatus = clockwiseRotateShift(parentStatus, x, y, z);
                break;
            case 'R':
                childStatus = anticlockwiseRotateShift(parentStatus, x, y, z);
                break;
            default:
        }
        return childStatus;
    }

    /**
     * 点对称交换
     * @param parentStatus
     * @param x
     * @param y
     * @param z
     * @return
     */
    private long pointSymmetricShift(long parentStatus, int x, int y, int z) {
        long childStatus = 0;
        for (int xStart = x - 1, xEnd = x + 1; xStart <= xEnd ; xStart++) {
            for (int yStart = y - 1, yEnd = y + 1; yStart <= yEnd ; yStart++) {
                for (int zStart = z - 1, zEnd = z + 1; zStart <= zEnd ; zStart++) {
                    if (xStart + yStart + zStart == 0) {
                        int index = evloverNodeUtil.index(xStart, yStart, zStart);
                        long value = parentStatus & EvloverNodeUtil.BINARY_VALUE[index];
                        if (value != 0) {
                            int pointSymmetricX = -xStart + x - y - z;
                            int pointSymmetricY = -yStart - x + y - z;
                            int pointSymmetricZ = -zStart - x - y + z;
                            index = evloverNodeUtil.index(pointSymmetricX, pointSymmetricY, pointSymmetricZ);
                            childStatus |= EvloverNodeUtil.BINARY_VALUE[index];
                            parentStatus ^= value;
                        }
                    }
                }
            }
        }
        return childStatus | parentStatus;
    }

    /**
     * 顺时针旋转交换
     * @param parentStatus
     * @param x
     * @param y
     * @param z
     * @return
     */
    private long clockwiseRotateShift(long parentStatus, int x, int y, int z) {
        long childStatus = 0;
        for (int xStart = x - 1, xEnd = x + 1; xStart <= xEnd ; xStart++) {
            for (int yStart = y - 1, yEnd = y + 1; yStart <= yEnd ; yStart++) {
                for (int zStart = z - 1, zEnd = z + 1; zStart <= zEnd ; zStart++) {
                    if (xStart + yStart + zStart == 0) {
                        int index = evloverNodeUtil.index(xStart, yStart, zStart);
                        long value = parentStatus & EvloverNodeUtil.BINARY_VALUE[index];
                        if (value != 0) {
                            int clockwiseRotateX = -zStart - y;
                            int clockwiseRotateY = -xStart - z;
                            int clockwiseRotateZ = -yStart - x;
                            index = evloverNodeUtil.index(clockwiseRotateX, clockwiseRotateY, clockwiseRotateZ);
                            childStatus |= EvloverNodeUtil.BINARY_VALUE[index];
                            parentStatus ^= value;
                        }
                    }
                }
            }
        }
        return childStatus | parentStatus;
    }

    /**
     * 逆时针旋转交换
     * @param parentStatus
     * @param x
     * @param y
     * @param z
     * @return
     */
    private long anticlockwiseRotateShift(long parentStatus, int x, int y, int z) {
        long childStatus = 0;
        for (int xStart = x - 1, xEnd = x + 1; xStart <= xEnd ; xStart++) {
            for (int yStart = y - 1, yEnd = y + 1; yStart <= yEnd ; yStart++) {
                for (int zStart = z - 1, zEnd = z + 1; zStart <= zEnd ; zStart++) {
                    if (xStart + yStart + zStart == 0) {
                        int index = evloverNodeUtil.index(xStart, yStart, zStart);
                        long value = parentStatus & EvloverNodeUtil.BINARY_VALUE[index];
                        if (value != 0) {
                            int anticlockwiseRotateX = -yStart - z;
                            int anticlockwiseRotateY = -zStart - x;
                            int anticlockwiseRotateZ = -xStart - y;
                            index = evloverNodeUtil.index(anticlockwiseRotateX, anticlockwiseRotateY, anticlockwiseRotateZ);
                            childStatus |= EvloverNodeUtil.BINARY_VALUE[index];
                            parentStatus ^= value;
                        }
                    }
                }
            }
        }
        return childStatus | parentStatus;
    }

    /**
     * 搜索通关路径
     * @param evloverNode
     * @param checkAVLTree
     * @return
     */
    private EvloverNode[] searchPassPath(EvloverNode evloverNode, AVLTree<EvloverNode> checkAVLTree) {
        // 检查另一棵树是否存在相同状态的节点
        EvloverNode junction = checkAVLTree.search(evloverNode.getStatus());
        if (junction != null) {
            int step1 = EvloverNodeUtil.step(evloverNode), step2 = EvloverNodeUtil.step(junction);
            EvloverNode[] evloverNodes = new EvloverNode[step1 + step2];
            if (checkAVLTree == endAVLTree) {
                build(evloverNodes, evloverNode, step1 - 1, -1, false);
                build(evloverNodes, junction, step1, 1, true);
            } else {
                build(evloverNodes, junction, step2 - 1, -1, false);
                build(evloverNodes, evloverNode, step2, 1, true);
            }
            return evloverNodes;
        }
        return null;
    }

    /**
     * 构建通关路径
     * @param evloverNodes
     * @param evloverNode
     * @param index
     * @param offset
     * @param reverse
     */
    private void build(EvloverNode[] evloverNodes, EvloverNode evloverNode, int index, int offset, boolean reverse) {
        while (evloverNode.getParent() != null) {
            if (reverse) {
                char action = evloverNode.getAction();
                switch (action) {
                    case 'L':
                        evloverNode.setAction('R');
                        break;
                    case 'R':
                        evloverNode.setAction('L');
                        break;
                    default:
                }
            }
            evloverNodes[index] = evloverNode;
            evloverNode = evloverNode.getParent();
            index += offset;
        }
    }

}
