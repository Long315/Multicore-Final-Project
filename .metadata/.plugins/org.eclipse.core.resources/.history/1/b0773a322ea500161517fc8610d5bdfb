package lockfreepriorityqueue.nonlinearlizable;

import lockfreepriorityqueue.nonlinearlizable.PrioritySkipList.Node;

public class SkipQueue<T> implements ParallelPriorityQueue<T>{

	PrioritySkipList<T> skiplist;
	public SkipQueue(){
		skiplist = new PrioritySkipList<T>();
	}
	
	public boolean add(T item){
		//Node<T> node = (Node<T>)new Node<T>(item, score);
		return skiplist.add(item);
	}
	

	public T removeMin() {
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
//		q.add(5);
//		q.add(2);
//		q.add(7);
//		q.add(10);
//		q.add(4);
		System.out.println(q.skiplist.contains(Integer.MAX_VALUE));
//		while(!q.isEmpty()){
//			System.out.println(q.removeMin());
//		}
	}
}
