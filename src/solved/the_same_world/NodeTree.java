package solved.the_same_world;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NodeTree {

	private int[][] status; // 盘面
	private long target; // 目标状态值
	private Node[] solvedNodes; // 通关节点
	private List<Map<Long, Node>> nodes; // 已搜索路径
	private Deque<Node> deque; // 未搜索路径
	private long sum; // 搜索节点数量
	private long[] values; // 存放2^n

	public NodeTree(int[][] status, int[][] startPoints, long target) {
		this.status = status;
		this.target = target;
		nodes = new ArrayList<>(startPoints.length);
		for (int i = 0; i < startPoints.length; i++) {
			nodes.add(new HashMap<>());
		}
		
		values = new long[status.length * status[0].length];
		for (int i = 0; i < values.length; i++) {
			values[i] = (long) Math.pow(2, i);
		}
		
		deque = new LinkedList<>();
		// 起点
		for (int i = 0; i < startPoints.length; i++) {
			int index = getIndex(startPoints[i][0], startPoints[i][1]);
			long value = getValue(index);

			Node node = new Node();
			node.setValue(value);
			node.setRow(startPoints[i][0]);
			node.setCol(startPoints[i][1]);
			node.setIndex(i);
			deque.offer(node);
			nodes.get(i).put(value, node);
			sum++;
		}
		solvedNodes = new Node[startPoints.length];
	}

	private int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}; // 上、右、下、左
	
	// 搜索
	public void search(boolean dfs) {
		long startTime = System.currentTimeMillis();
		while (!deque.isEmpty()) {
			Node parent = deque.poll();
			for (int direction = 0; direction < directions.length; direction++) {
				if (solved(parent)) {
					printSearchInfo(startTime);
					TheSameWorldUtil.printPath(status, solvedNodes);
					return;
				} else {
					Node childNode = nextStep(parent, direction);
					if (childNode != null) {
						sum++;
						if (dfs) {
							deque.offerFirst(childNode);
						} else {
							deque.offerLast(childNode);
						}
					}
				}
			}
		}
		printSearchInfo(startTime);
	}

	// 走一步
	private Node nextStep(Node parent, int direction) {
		int childRow = parent.getRow() + directions[direction][0];
		int childCol = parent.getCol() + directions[direction][1];
		if (childRow < 0 || childRow >= status.length || childCol < 0 || childCol >= status[childRow].length || status[childRow][childCol] == 0) {
			return null;
		}
		int childIndex = getIndex(childRow, childCol);
		if (passed(parent.getValue(), childIndex)) {
			return null;
		}
		long childValue = parent.getValue() | getValue(childIndex);
		Node childNode = new Node();
		childNode.setParent(parent);
		childNode.setValue(childValue);
		childNode.setRow(childRow);
		childNode.setCol(childCol);
		childNode.setIndex(parent.getIndex());
		nodes.get(parent.getIndex()).put(childValue, childNode);
		return childNode;
	}

	// 搜索完成后的信息
	private void printSearchInfo(long startTime) {
		System.out.format("搜索时间:%d毫秒, 搜索的节点数量:%d%n", System.currentTimeMillis() - startTime, sum);
	}

	// 检查是否是符合目标的路径
	private boolean solved(Node node) {
		long nextTarget = target;
		for (int i = 0; i < solvedNodes.length && node != null; i++) {
			nextTarget = nextTarget ^ node.getValue();
			solvedNodes[i] = node;
			if (nextTarget == 0) {
				return true;
			}
			node = nodes.get((node.getIndex() + 1) % nodes.size()).get(nextTarget);
		}
		return false;
	}
	
	// 二维数组对应的一维数组索引
	private int getIndex(int row, int col) {
		return row * status[row].length + col;
	}

	// 2的index次方
	private long getValue(int index) {
		return values[index];
	}
	
	// 经过该点
	private boolean passed(long parentValue, int childIndex) {
		return (parentValue >> childIndex & 1) == 1;
	}
}
