
package multiqueue;
import interfacepackage.ParallelPriorityQueue;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Integer;
import java.util.Comparator;

public class MultiQueue implements ParallelPriorityQueue{
	ArrayList<PriorityBlockingQueue<Integer>> q;      
    int size;	
	Random r = new Random();
	public MultiQueue(int c, int t) {
	    // c = constant (defines constant time access to queue as 1/c)
	    // t = num threads accessing queue
		size = c * t;
	    q = new ArrayList<PriorityBlockingQueue<Integer>>();
        for (int i = 0; i < size; i++){
			q.add(new PriorityBlockingQueue<Integer>());
		}
	}
	public boolean add(Integer item){
		int rand = randomIdx();
		// guaranteed to find a space to add eventually for c > 1
		// adding 1 instead of new rand so that we don't repeat
		// means bounded wait time b4 finding space
		// instead of O(infinity) in the worst case, while we repeatedly
		// roll the same locked queue and never get in.
		// though i guess we could be incrementing and having the queue
		// 1 in front of us fill up forever...
	    while(!q.get(rand++ % size).add(item));
//		System.out.format("added %d to queue %d\n", item, rand-1);
		return true;
	}
	public Integer poll() {
	    Integer result = null;
		int startIdx = randomIdx();
//		System.out.format("startIdx = %d\n", startIdx);
		int index = startIdx;
		boolean wrapped = false;
	    while(result == null){
			Integer[] c = new Integer[2];  // candidates
			int[] idx = new int[2];  // indices
			for(int i = 0; i < 2; i++){
				do {
					idx[i] = index;
					index = (index + 1) % size;
					if (index == startIdx) wrapped = true;
					c[i] = q.get(idx[i]).peek();
					if (c[i] == null && wrapped){
						if (i == 1) return c[0];
						return null;
					}
				} while(c[i] == null);
//				System.out.format("candidate %d = %d\n", i, c[i]);
			}
			int mindex;
			if ( c[0] < c[1] )
				mindex = 0;
			else
				mindex = 1;
			result = q.get(idx[mindex]).pollAfterPeek();
	    }
		return result;
	}
	
	private int randomIdx(){
		return Math.abs(r.nextInt()) % size;
	}
	
	public static void main(String[] args) {
        MultiQueue mq = new MultiQueue(2, 1);
	    mq.add(5);
	    mq.add(9);
	    mq.add(3);
	    System.out.format("%d\n", mq.poll());
	}
}
