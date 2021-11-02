package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private final AtomicRef<Node> head;
    private final AtomicRef<Node> tail;

    public MSQueue() {
        Node fictional = new Node(2007);
        head = new AtomicRef<>(fictional);
        tail = new AtomicRef<>(fictional);
    }

    @Override
    public void enqueue(int x) {
        Node nx = new Node(x);
        while (true) {
            Node oldTail = tail.getValue();
            Node next = oldTail.next.getValue();
            if (next == null) {
                if (oldTail.next.compareAndSet(null, nx)) {
                    return;
                }
            } else {
                tail.compareAndSet(oldTail, next);
            }
        }
    }

    @Override
    public int dequeue() {
        while(true) {
            Node oldHead = head.getValue();
            Node oldTail = tail.getValue();
            Node next = oldHead.next.getValue();

            if (oldHead == oldTail) {
                if (next == null) {
                    return Integer.MIN_VALUE;
                } else {
                    tail.compareAndSet(oldTail, next);
                }
            } else {
                if (head.compareAndSet(oldHead, next)) {
                    return next.x;
                }
            }
//            if (next == null) {
//                return Integer.MIN_VALUE;
//            } else {
//                tail.compareAndSet(oldHead, next);
//            }
//            if (head.compareAndSet(oldHead, next)) {
//                return oldHead.x;
//            }
        }
    }

    @Override
    public int peek() {
        Node val = head.getValue().next.getValue();
        if (val == null) {
            return Integer.MIN_VALUE;
        } else {
            return val.x;
        }
    }

    private static class Node {
        final int x;
        AtomicRef<Node> next = new AtomicRef<>(null);

        Node(int x) {
            this.x = x;
        }
    }
}