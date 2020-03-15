package solved.util;

public class Queue<V> {

    private final static class Node<V> {
        V value;
        Node<V> next;
    }

    private Node<V> head, rear;
    private int size;

    public Queue() {
        head = new Node<>();
        head.next = head;
        rear = head;
    }

    public void offer(V value) {
        Node<V> node = new Node<>();
        node.value = value;
        rear.next = node;
        rear = node;
        rear.next = head;
        size++;
    }

    public V poll() {
        if (!isEmpty()) {
            size--;
        }
        head = head.next;
        V value = head.value;
        head.value = null;
        rear.next = head;
        return value;
    }

    public boolean isEmpty() {
        return head == rear;
    }

    public int size() {
        return size;
    }

}
