/**
 * @author Malko Egor
 */
public class Solution implements AtomicCounter {
    // объявите здесь нужные вам поля

    final Node root = new Node(0);
    final ThreadLocal<Node> lastWrapper = ThreadLocal.withInitial(() -> root);

    public int getAndAdd(int x) {
        Node node;
        Node last = lastWrapper.get();
        do {
            node = new Node(last.val + x);
            last = last.next.decide(node);
        } while (last != node);
        lastWrapper.set(last);
        return node.val - x;
    }

    private static class Node {
        final int val;
        final Consensus<Node> next;

        private Node(int val) {
            this.val = val;
            next = new Consensus<>();
        }
    }
}
