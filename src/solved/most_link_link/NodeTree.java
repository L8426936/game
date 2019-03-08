package solved.most_link_link;

public class NodeTree {

	private char[][] status;
	private Node[] nodes;
	private int count, nodesIndex, sum; // count方格数量，sum搜索的节点数量
	
	public NodeTree(char[][] status, int count, int startRow, int startCol) {
		this.status = status;
		this.count = count;
		this.nodes = new Node[count];
		
		Node root = new Node();
		root.setRow(startRow);
		root.setCol(startCol);
		this.nodes[nodesIndex] = root;
		this.sum++;
		this.count--;
	}
	
	private int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}; // 上、右、下、左
	// 深度优先搜索
	public Node[] DFS() {
		long startTime = System.currentTimeMillis();
		OVER:
			while (nodes[0].getDirectionCount() < directions.length) {
				Node parent = nodes[nodesIndex];
				int direction = parent.getDirection();
				while (true) {
					if (nodesIndex > 0 && parent.getDirectionCount() >= directions.length) { // 节点改变方向次数大于等于4次，即4个方向均尝试过，回退一个节点
						status[parent.getRow()][parent.getCol()] = MostLinkLinkUtil.EMPTY;
						count++;
						parent = nodes[--nodesIndex]; // 回退一个节点
						parent.setDirection((parent.getDirection() + 1) % directions.length); // 回退的节点改变方向
						parent.setDirectionCount(parent.getDirectionCount() + 1);
						break;
					}
					Node child = nextStep(parent, direction);
					if (child != null) {
						nodes[++nodesIndex] = parent = child;
						sum++;
						if (--count == 0) {
							System.out.format("搜索时间:%d毫秒, 搜索的节点数量:%d%n", System.currentTimeMillis() - startTime, sum);
							break OVER;
						}
					} else {
						direction = (direction + 1) % directions.length;
						parent.setDirection(direction);
						parent.setDirectionCount(parent.getDirectionCount() + 1);
					}
				}
			}
		return count == 0 ? nodes : null;
	}
	
	// 走一步
	private Node nextStep(Node parent, int direction) {
		int childRow = parent.getRow() + directions[direction][0];
		int childCol = parent.getCol() + directions[direction][1];
		if (childRow < 0 || childRow >= status.length || childCol < 0 || childCol >= status[0].length || status[childRow][childCol] != MostLinkLinkUtil.EMPTY) {
			return null;
		}
		Node child = nodes[nodesIndex + 1]; // 之前丢弃的节点重新利用，减少new的次数
		if (child != null) {
			child.setDirectionCount(0);
		} else {
			child = new Node();
		}
		child.setRow(childRow);
		child.setCol(childCol);
		child.setDirection(direction);
		status[childRow][childCol] = MostLinkLinkUtil.EXIST;
		return child;
	}
}
