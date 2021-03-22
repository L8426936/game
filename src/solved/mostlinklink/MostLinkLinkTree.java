package solved.mostlinklink;

public class MostLinkLinkTree {

	private char[][] status;
	private MostLinkLinkNode[] nodes;
	private int count, nodesIndex, sum; // count方格数量，sum搜索的节点数量
	
	public MostLinkLinkTree(GameInfo gameInfo) {
		this.status = gameInfo.getStatus();
		this.count = gameInfo.getCount();
		this.nodes = new MostLinkLinkNode[count];

		MostLinkLinkNode root = new MostLinkLinkNode();
		root.setRow(gameInfo.getStartRow());
		root.setCol(gameInfo.getStartCol());
		this.nodes[nodesIndex] = root;
		this.sum++;
		this.count--;
	}

	private int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}; // 上、右、下、左
	// 深度优先搜索
	public MostLinkLinkNode[] DFS() {
		OVER:
			while (nodes[0].getDirectionCount() < directions.length) {
				MostLinkLinkNode parent = nodes[nodesIndex];
				int direction = parent.getDirection();
				while (true) {
					if (nodesIndex > 0 && parent.getDirectionCount() >= directions.length) { // 节点改变方向次数大于等于4次，即4个方向均尝试过，回退一个节点
						status[parent.getRow()][parent.getCol()] = MostLinkLinkPlayer.EMPTY;
						count++;
						parent = nodes[--nodesIndex]; // 回退一个节点
						parent.setDirection((parent.getDirection() + 1) % directions.length); // 回退的节点改变方向
						parent.setDirectionCount(parent.getDirectionCount() + 1);
						break;
					}
					MostLinkLinkNode child = nextStep(parent, direction);
					if (child != null) {
						nodes[++nodesIndex] = parent = child;
						sum++;
						if (--count == 0) {
							break OVER;
						}
					} else {
						direction = (direction + 1) % directions.length;
						parent.setDirection(direction);
						parent.setDirectionCount(parent.getDirectionCount() + 1);
					}
				}
			}
		System.out.format("搜索的节点数量:%d%n", sum);
		return count == 0 ? nodes : null;
	}
	
	// 走一步
	private MostLinkLinkNode nextStep(MostLinkLinkNode parent, int direction) {
		int childRow = parent.getRow() + directions[direction][0];
		int childCol = parent.getCol() + directions[direction][1];
		if (childRow < 0 || childRow >= status.length || childCol < 0 || childCol >= status[0].length || status[childRow][childCol] != MostLinkLinkPlayer.EMPTY) {
			return null;
		}
		MostLinkLinkNode child = nodes[nodesIndex + 1]; // 之前丢弃的节点重新利用，减少new的次数
		if (child != null) {
			child.setDirectionCount(0);
		} else {
			child = new MostLinkLinkNode();
		}
		child.setRow(childRow);
		child.setCol(childCol);
		child.setDirection(direction);
		status[childRow][childCol] = MostLinkLinkPlayer.EXIST;
		return child;
	}
}
