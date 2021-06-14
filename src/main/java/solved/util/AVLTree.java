package solved.util;

public class AVLTree<V> {

    private final static class Node<V> {
        private Node<V> parent;
        private Node<V> left;
        private Node<V> right;
        private long key;
        private V value;
        private int height;

        public Node<V> getParent() {
            return parent;
        }

        public void setParent(Node<V> parent) {
            this.parent = parent;
        }

        public Node<V> getLeft() {
            return left;
        }

        public void setLeft(Node<V> left) {
            this.left = left;
        }

        public Node<V> getRight() {
            return right;
        }

        public void setRight(Node<V> right) {
            this.right = right;
        }

        public long getKey() {
            return key;
        }

        public void setKey(long key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    private Node<V> root;
    private int size;

    public boolean put(long key, V value) {
        if (root == null) {
            root = new Node<>();
            root.setKey(key);
            root.setValue(value);
            size++;
            return true;
        }
        Node<V> node = root;
        while (true) {
            long oldKey = node.getKey();
            if (key == oldKey) {
                return false;
            }
            Node<V> parent = node;
            boolean goLeft = oldKey > key;
            node = goLeft ? node.getLeft() : node.getRight();
            if (node == null) {
                Node<V> childNode = new Node<>();
                childNode.setParent(parent);
                childNode.setKey(key);
                childNode.setValue(value);
                if (goLeft) {
                    parent.setLeft(childNode);
                } else {
                    parent.setRight(childNode);
                }
                reBalance(parent);
                break;
            }
        }
        size++;
        return true;
    }

    public V get(long key) {
        Node<V> node = root;
        while (node != null) {
            long oldKey = node.getKey();
            if (oldKey == key) {
                return node.getValue();
            }
            if (oldKey > key) {
                node = node.getLeft();
            } else {
                node = node.getRight();
            }
        }
        return null;
    }

    public boolean remove(long key) {
        Node<V> node = root;
        while (node != null) {
            long oldKey = node.getKey();
            if (oldKey == key) {
                remove(node);
                size--;
                return true;
            }
            if (oldKey > key) {
                node = node.getLeft();
            } else {
                node = node.getRight();
            }
        }
        return false;
    }

    private void remove(Node<V> node) {
        if (node.getLeft() == null && node.getRight() == null) {
            if (node.getParent() == null) {
                root = null;
            } else {
                Node<V> parent = node.getParent();
                if (parent.getLeft() == node) {
                    parent.setLeft(null);
                } else {
                    parent.setRight(null);
                }
                reBalance(parent);
            }
            return;
        }

        if (node.getLeft() != null) {
            Node<V> child = node.getLeft();
            while (child.getRight() != null) {
                child = child.getRight();
            }
            node.setKey(child.getKey());
            node.setValue(child.getValue());
            remove(child);
        } else {
            Node<V> child = node.getRight();
            while (child.getLeft() != null) {
                child = child.getLeft();
            }
            node.setKey(child.getKey());
            node.setValue(child.getValue());
            remove(child);
        }
    }

    public int size() {
        return size;
    }

    private void reBalance(Node<V> node) {
        while (true) {
            resetHeight(node);
            int balance = height(node.getRight()) - height(node.getLeft());
            switch (balance) {
                case -2:
                    if (height(node.getLeft().getLeft()) >= height(node.getLeft().getRight())) {
                        node = rotateRight(node);
                    } else {
                        node = rotateLeftThenRight(node);
                    }
                    break;
                case 2:
                    if (height(node.getRight().getRight()) >= height(node.getRight().getLeft())) {
                        node = rotateLeft(node);
                    } else {
                        node = rotateRightThenLeft(node);
                    }
                    break;
                default:
            }
            if (node.getParent() != null) {
                node = node.getParent();
            } else {
                root = node;
                break;
            }
        }
    }

    private Node<V> rotate(Node<V> parentNode, Node<V> childNode) {
        parentNode.setParent(childNode);
        if (childNode.getParent() != null) {
            if (childNode.getParent().getRight() == parentNode) {
                childNode.getParent().setRight(childNode);
            } else {
                childNode.getParent().setLeft(childNode);
            }
        }
        resetHeight(parentNode);
        resetHeight(childNode);
        return childNode;
    }

    private Node<V> rotateLeft(Node<V> node) {
        Node<V> rightChildNode = node.getRight();
        rightChildNode.setParent(node.getParent());
        node.setRight(rightChildNode.getLeft());
        if (node.getRight() != null) {
            node.getRight().setParent(node);
        }
        rightChildNode.setLeft(node);
        rotate(node, rightChildNode);
        return rightChildNode;
    }

    private Node<V> rotateRight(Node<V> node) {
        Node<V> leftChildNode = node.getLeft();
        leftChildNode.setParent(node.getParent());
        node.setLeft(leftChildNode.getRight());
        if (node.getLeft() != null) {
            node.getLeft().setParent(node);
        }
        leftChildNode.setRight(node);
        rotate(node, leftChildNode);
        return leftChildNode;
    }

    private Node<V> rotateLeftThenRight(Node<V> node) {
        node.setLeft(rotateLeft(node.getLeft()));
        return rotateRight(node);
    }

    private Node<V> rotateRightThenLeft(Node<V> node) {
        node.setRight(rotateRight(node.getRight()));
        return rotateLeft(node);
    }

    private void resetHeight(Node<V> node) {
        if (node != null) {
            int leftHeight = height(node.getLeft());
            int rightHeight = height(node.getRight());
            node.setHeight(1 + Math.max(leftHeight, rightHeight));
        }
    }

    private int height(Node<V> node) {
        if (node == null) {
            return -1;
        }
        return node.getHeight();
    }

}
