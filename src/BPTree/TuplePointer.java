package BPTree;

import java.io.Serializable;

public class TuplePointer implements Comparable<TuplePointer>,Serializable{
	int idx;
	String pagePath;
	Comparable key;
	

	
	
	public TuplePointer(int idx, String pagePath,Comparable key){
		this.idx = idx;
		this.pagePath = pagePath;
		this.key = key;
	}

	@Override
	public int compareTo(TuplePointer tp) {
		return this.key.compareTo(tp.key);
	}
	
	public int getIdx() {
		return idx;
	}

	public Comparable getKey() {
		return key;
	}
	
	public String getPagePath(){
		return this.pagePath;
	}
}