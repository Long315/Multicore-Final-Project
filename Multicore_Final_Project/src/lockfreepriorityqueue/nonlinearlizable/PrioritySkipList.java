package lockfreepriorityqueue.nonlinearlizable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicMarkableReference;

public final class PrioritySkipList<T> implements Iterable<T> {
	static final int MAX_LEVEL = 32;
	static int randomSeed = (int) (System.currentTimeMillis()) | 0x0100;
	final Node<T> head = new Node<T>(Integer.MIN_VALUE);
	final Node<T> tail = new Node<T>(Integer.MAX_VALUE);

	public PrioritySkipList() {
		for (int i = 0; i < head.next.length; i++) {
			head.next[i] = new AtomicMarkableReference<Node<T>>(tail, false);
		}
	}

	// public static int randomLevel() {
	// int lvl = (int)(Math.log(1.-Math.random())/Math.log(1.-0.5));
	// return Math.min(lvl, MAX_LEVEL);
	// }

	private static int randomLevel() {
		int x = randomSeed;
		x ^= x << 13;
		x ^= x >>> 17;
		randomSeed = x ^= x << 5;
		if ((x & 0x80000001) != 0)
			return 0;
		int level = 1;
		while (((x >>>= 1) & 1) != 0)
			++level;
		return Math.min(level, MAX_LEVEL - 1);
	}

	boolean add(Node node) {
		int bottomLevel = 0;
		Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
		Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];
		while (true) {
			boolean found = find(node, preds, succs);

			for (int level = bottomLevel; level <= node.topLevel; level++) {
				Node<T> succ = succs[level];
				node.next[level].set(succ, false);
			}

			Node<T> pred = preds[bottomLevel];
			Node<T> succ = succs[bottomLevel];
			node.next[bottomLevel].set(succ, false);
			if (!pred.next[bottomLevel].compareAndSet(succ, node, false, false)) {
				continue;
			}

			for (int level = bottomLevel + 1; level <= node.topLevel; level++) {
				while (true) {
					pred = preds[level];
					succ = succs[level];
					if (pred.next[level]
							.compareAndSet(succ, node, false, false))
						break;
					find(node, preds, succs);
				}
			}
			return true;

		}
	}

	boolean remove(Node<T> node) {
		int bottomLevel = 0;
		Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
		Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];
		Node<T> succ;
		while (true) {
			boolean found = find(node, preds, succs);

			for (int level = node.topLevel; level >= bottomLevel + 1; level--) {
				boolean[] marked = { false };
				succ = node.next[level].get(marked);
				while (!marked[0]) {
					node.next[level].attemptMark(succ, true);
					succ = node.next[level].get(marked);
				}
			}

			boolean[] marked = { false };
			succ = node.next[bottomLevel].get(marked);

			while (true) {
				boolean iMarkedIt = node.next[bottomLevel].compareAndSet(succ,
						succ, false, true);
				succ = succs[bottomLevel].next[bottomLevel].get(marked);
				if (iMarkedIt) {
					find(node, preds, succs);
					return true;
				} else if (marked[0])
					return false;
			}

		}
	}

	public Node<T> findAndMarkMin() {
		Node<T> curr = null, succ = null;
		curr = head.next[0].getReference();
		while (curr != tail) {
			if (!curr.marked.get()) {
				if (curr.marked.compareAndSet(false, true)) {
					return curr;
				} else {
					curr = curr.next[0].getReference();
				}
			}
		}
		return null;
	}

	boolean find(Node<T> node, Node<T>[] preds, Node<T>[] succs) {
		int bottomLevel = 0;
		boolean[] marked = { false };
		boolean snip;
		Node<T> pred = null, curr = null, succ = null;
		retry: while (true) {
			pred = head;
			for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
				curr = pred.next[level].getReference();
				while (true) {
					succ = curr.next[level].get(marked);
					while (marked[0]) {
						snip = pred.next[level].compareAndSet(curr, succ,
								false, false);
						if (!snip)
							continue retry;
						curr = pred.next[level].getReference();
						succ = curr.next[level].get(marked);
					}
					if (curr.priority < node.priority) {
						pred = curr;
						curr = succ;
					} else {
						break;
					}
				}
				preds[level] = pred;
				succs[level] = curr;
			}
			return (curr.priority == node.priority);
		}
	}

	// not thread safe!
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Node<T> cursor = head;

			public boolean hasNext() {
				return cursor.next[0].getReference() != tail;
			}

			public T next() {
				cursor = cursor.next[0].getReference();
				return cursor.item;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static final class Node<T> {
		final T item;
		final int priority;
		AtomicBoolean marked;
		final AtomicMarkableReference<Node<T>>[] next;
		int topLevel;

		public Node(int myPriority) {
			item = null;
			priority = myPriority;
			marked = new AtomicBoolean(false);
			next = (AtomicMarkableReference<Node<T>>[]) new AtomicMarkableReference[MAX_LEVEL + 1];
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<Node<T>>(null, false);
			}
			topLevel = MAX_LEVEL;
		}

		public Node(T x, int myPriority) {
			item = x;
			priority = myPriority;
			marked = new AtomicBoolean(false);
			int height = randomLevel();
			next = (AtomicMarkableReference<Node<T>>[]) new AtomicMarkableReference[height + 1];
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<Node<T>>(null, false);
			}

			topLevel = height;
		}
	}
}
