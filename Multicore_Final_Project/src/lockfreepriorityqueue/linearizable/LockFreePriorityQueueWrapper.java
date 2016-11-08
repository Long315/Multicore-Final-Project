package lockfreepriorityqueue.linearizable;

import interfacepackage.ParallelPriorityQueue;

public class LockFreePriorityQueueWrapper implements ParallelPriorityQueue{
	LockFreePriorityQueue<Integer> pq;
	public LockFreePriorityQueueWrapper(){
		pq = new LockFreePriorityQueue<Integer>();
	}

	public boolean add(Integer item) {
		return pq.add(item);
	}

	public Integer poll() {
		// TODO Auto-generated method stub
		return pq.poll();
	}
	 public static void main(String[] args) {
			// TODO Auto-generated method stub
			LockFreePriorityQueue<Integer> q = new LockFreePriorityQueue<Integer>();
			q.add(5);
			q.add(2);
			q.add(7);
			q.add(10);
			q.add(4);
			while(!q.isEmpty()){
				System.out.println(q.poll());
			}
			System.out.println(q.poll());
		}
}
