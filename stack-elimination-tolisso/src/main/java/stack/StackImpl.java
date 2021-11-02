package stack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import kotlinx.atomicfu.AtomicRef;

public class StackImpl implements Stack {

    private static class Node {

        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    private final int BUFFER_SIZE = 8;
    private final int BUFFER_ATTEMPTS = 2;

    private final List<AtomicRef<Integer>> buffer = new ArrayList<>();
    // head pointer
    private final AtomicRef<Node> head = new AtomicRef<>(null);

    public StackImpl() {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer.add(new AtomicRef<Integer>(null));
        }
    }

    private void pushInHead(int x) {
        while (true) {
            Node oldHead = head.getValue();
            Node newHead = new Node(x, oldHead);
            if (head.compareAndSet(oldHead, newHead)) {
                return;
            }
        }
    }

    private int popInHead() {
        while (true) {
            Node curHead = head.getValue();
            if (curHead == null) {
                return Integer.MIN_VALUE;
            }
            Node next = curHead.next.getValue();
            if (head.compareAndSet(curHead, next)) {
                return curHead.x;
            }
        }
    }

    @Override
    public void push(int x) {
        int pos = ThreadLocalRandom.current().nextInt(BUFFER_SIZE);
        for (int i = 0; i < BUFFER_ATTEMPTS; i++) {

            AtomicRef<Integer> cell = buffer.get(pos);
            Integer val = x;

            if (cell.compareAndSet(null, val)) {
                for (int j = 0; j < 30; j++) {
                    if (cell.getValue() != val) {
                        return;
                    }
                }
                if (cell.compareAndSet(val, null)) {
                    pushInHead(x);
                }
                return;
            }

            pos = (pos + 1) % BUFFER_SIZE;
        }
        pushInHead(x);
    }

    @Override
    public int pop() {
        int pos = ThreadLocalRandom.current().nextInt(BUFFER_SIZE);
        for (int i = 0; i < BUFFER_ATTEMPTS; i++) {

            AtomicRef<Integer> cell = buffer.get(pos);
            Integer val = cell.getValue();

            if (val != null && cell.compareAndSet(val, null)) {
                return val;
            }
            pos = (pos + 1) % BUFFER_SIZE;
        }
        return popInHead();
    }
}
