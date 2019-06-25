package solved.util;

public class LinkedList<V> {

    private final static class Node<V> {
        private V value;
        private Node<V> next;

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Node<V> getNext() {
            return next;
        }

        public void setNext(Node<V> next) {
            this.next = next;
        }
    }

    private Node<V> head, rear;
    private int size;

    public LinkedList() {
        head = new Node<>();
        head.setNext(head);
        rear = head;
    }

    public void offer(V value) {
        Node<V> node = new Node<>();
        node.setValue(value);
        rear.setNext(node);
        rear = node;
        rear.setNext(head);
        size++;
    }

    public V poll() {
        head = head.getNext();
        size--;
        return head.getValue();
    }

    public boolean isEmpty() {
        return head == rear;
    }

    public int size() {
        return size;
    }

}
