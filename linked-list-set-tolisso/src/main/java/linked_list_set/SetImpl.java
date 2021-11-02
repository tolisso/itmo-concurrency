package linked_list_set;

import kotlinx.atomicfu.AtomicRef;

public class SetImpl implements Set {
    private static class Node {
        AtomicRef<Node> next;
        int x;
        
        boolean isDeleted() {
            return false;
        }

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
        AtomicRef<Node> getNext() {
            return next;
        }
        Node restore() {
            throw new RuntimeException("You shouldn't be here");
        }
    }
    private static class DeletedNode extends Node {
        Node node;

        DeletedNode(Node n) {
            super(n.x, null);
            node = n;
        }
        boolean isDeleted() {
            return true;
        }
        AtomicRef<Node> getNext() {
            return node.getNext();
        }
        Node restore() {
            return node;
        }
    }

    private static class Window {
        Node cur, next;
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
        outer: while(true) {
            Window w = new Window();
            w.cur = head;
            w.next = w.cur.getNext().getValue();
            if (w.next.isDeleted()) {
                throw new RuntimeException();
            }
            while (w.next.x < x) {
                Node node = w.next.getNext().getValue();
                if (node.isDeleted()) {
                    node = node.restore();
                    if (!w.cur.getNext().compareAndSet(w.next, node)) {
                        continue outer;
                    }
                } else {
                    w.cur = w.next;
                }
                w.next = node;
            }
            if (w.next.isDeleted()) {
                continue;
            }
            return w;
        }
    }

    @Override
    public boolean add(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x == x && isPresent(w.next)) {
                return false;
            }
            Node node = new Node(x, w.next);
            if (!w.next.isDeleted() &&
                w.cur.getNext().compareAndSet(w.next, node)) {
                return true;
            }
        }
    }

    private boolean isPresent(Node n) {
        return n.x != Integer.MAX_VALUE &&
            (!n.getNext().getValue().isDeleted());
    }

    @Override
    public boolean remove(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x != x) {
                return false;
            }
            Node node = w.next.getNext().getValue();
            if (node.isDeleted()) return false;
            Node deleted = new DeletedNode(node);
            if (w.next.getNext().compareAndSet(node, deleted)) {
                w.cur.getNext().compareAndSet(w.next, node);
                return true;
            }
        }
    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        return isPresent(w.next) &&
            (w.next.x == x);
    }
}
