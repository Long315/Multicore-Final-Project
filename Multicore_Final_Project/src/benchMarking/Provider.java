package benchMarking;

import interfacepackage.ParallelPriorityQueue;

import java.util.concurrent.Callable;

public class Provider<T> implements Callable<T>{
	  int Id;
	  ParallelPriorityQueue<Integer> queue;
	  int[] counts;
	  public Provider(int Id, ParallelPriorityQueue<Integer> queue, int[] counts) {
		this.Id = Id;
	    this.queue = queue;
	    this.counts = counts;
	  }
	  public T call() {
		  int item = 0;
		  while(true){
			  queue.add(++item);
			  counts[Id] = counts[Id] + 1;
		  }
	  }
}
