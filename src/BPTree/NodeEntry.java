package BPTree;

import java.io.Serializable;

public class NodeEntry implements Serializable{
	Comparable key;
	String left;
	String right;
	public NodeEntry(Comparable key, String left, String right)
	{
		this.key	= key;
		this.left	= left;
		this.right	= right;
	}
}
