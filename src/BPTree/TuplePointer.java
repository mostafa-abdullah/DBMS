package BPTree;

import java.io.Serializable;

public class TuplePointer implements Comparable<TuplePointer>,Serializable{
	int idx;
	Comparable key;
	String pagePath;

	public TuplePointer(int idx, String pagePath,Comparable key){
		this.idx = idx;
		this.pagePath = pagePath;
		this.key = key;
	}

	@Override
	public int compareTo(TuplePointer tp) {
		return this.key.compareTo(tp.key);
	}
}