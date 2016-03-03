package BPTree;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable{
	BPTree tree;
	int max;
	int min;
	String parent;
	int nodeNo;
	
	public Node(BPTree tree, String parent)
	{
		this.parent = parent;
		this.tree = tree;
		this.max = tree.n;
	}
}
