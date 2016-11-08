package lockfreepriorityqueue.nonlinearlizable;

import interfacepackage.ParallelPriorityQueue;
import lockfreepriorityqueue.nonlinearlizable.PrioritySkipList.Node;

public class SkipQueue<T> implements ParallelPriorityQueue<T>{

	PrioritySkipList<T> skiplist;
	public SkipQueue(){
		skiplist = new PrioritySkipList<T>();
	}
	
	public boolean add(T item, int priority){
		Node<T> node = (Node<T>)new Node<T>(item, priority);
		return skiplist.add(node);
	}
	

	public T poll() {
		Node<T> node = skiplist.findAndMarkMin();
		if (node != null){
			skiplist.remove(node);
			return node.item;
		} else {
			return null;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SkipQueue<Integer> q = new SkipQueue<Integer>();
		q.add(5);
		q.add(4);
		q.add(5);
		q.add(4);
		q.add(4);
		q.add(8);
		System.out.print(q.poll());
		System.out.print(q.poll());
		System.out.print(q.poll());
		System.out.print(q.poll());
		System.out.print(q.poll());
		System.out.print(q.poll());
		System.out.print(q.poll());
		
	}

	public boolean add(T item) {
		// TODO Auto-generated method stub
		return add(item, (Integer) item);
	}
}
