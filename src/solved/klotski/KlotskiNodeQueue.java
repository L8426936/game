package solved.klotski;

public class KlotskiNodeQueue {
    /**
     * queue的长度必须为2^n，取模运算可用位运算
     */
    private KlotskiNode[] queue = new KlotskiNode[1024];
    private int head, rear, length = queue.length - 1;

    public void offer(KlotskiNode klotskiNode) {
        if (((rear + 1) & length) == head) {
            resizeQueue();
        }
        queue[rear++] = klotskiNode;
        // 取模运算
        rear &= length;
    }

    public KlotskiNode poll() {
        if (head == rear) {
            return null;
        }
        KlotskiNode klotskiNode = queue[head++];
        head &= length;
        return klotskiNode;
    }

    /**
     * 队列扩容
     */
    private void resizeQueue() {
        KlotskiNode[] klotskiNodes = new KlotskiNode[queue.length << 1];
        System.arraycopy(queue, head, klotskiNodes, klotskiNodes.length - queue.length + head, queue.length - head);
        head = klotskiNodes.length - queue.length + head;
        System.arraycopy(queue, 0, klotskiNodes, 0, rear);
        queue = klotskiNodes;
        length = queue.length - 1;
    }

    public boolean isEmpty() {
        return head == rear;
    }
}
