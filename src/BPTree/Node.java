package BPTree;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable{
	String tree;
	int max;
	int min;
	String parent;
	int nodeNo;
	
	public Node(String tree, String parent, int n)
	{
		this.parent = parent;
		this.tree = tree;
		this.max = n;
	}
}
