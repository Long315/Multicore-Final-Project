package benchMarking;

import java.util.Random;

import interfacepackage.ParallelPriorityQueue;

public class Alternate extends Thread{
	  int Id;
	  ParallelPriorityQueue queue;
	  int[] counts;
	  Random r = new Random();
	  int[] valuelist;
	  
	  public Alternate(int Id, ParallelPriorityQueue queue, int[] counts, int[] valuelist) {
		this.Id = Id;
	    this.queue = queue;
	    this.counts = counts;
//	    this.gate = gate;
	    this.valuelist = valuelist;
	    this.setPriority(MAX_PRIORITY);
	  }
	  
	  public void run() {
			  int i = r.nextInt(5000000);
			  while(!running);
//			  before = Instant.now().toEpochMilli();
			  while(running){
				  try {
					  queue.add(valuelist[i]);
					  i = (i + 1) % 5000000;
					  counts[Id] = counts[Id] + 1;
					  queue.poll();
					  counts[Id] =  counts[Id] + 1;
				  } catch (NullPointerException e) {
					  System.err.println("here");
				  }
			  }
	  }
}
