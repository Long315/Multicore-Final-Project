package lockfreepriorityqueue.nonlinearlizable;

public interface ParallelPriorityQueue<T> {
	public boolean add(T item);
	public T removeMin();
}
