package benchMarking;

import java.util.concurrent.Callable;

import Interface.ParallelPriorityQueue;

public class Consumer<T> implements Callable<T>{
	  int Id;
	  ParallelPriorityQueue<Integer> queue;
	  int[] counts;
	  public Consumer(int Id, ParallelPriorityQueue<Integer> queue, int[] counts) {
		this.Id = Id;
	    this.queue = queue;
	    this.counts = counts;
	  }
	  public T call() {
		  while(true){
			  queue.removeMin();
			  counts[Id] = counts[Id] + 1;
		  }
	  }
}
