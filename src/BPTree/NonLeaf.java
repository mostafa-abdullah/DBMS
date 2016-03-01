package BPTree;

import java.io.IOException;
import java.util.ArrayList;

import DB.DBApp;

public class NonLeaf extends Node {

	ArrayList<NodeEntry> entries;
	
	public NonLeaf(BPTree tree, String parent) {
		super(tree, parent);
		this.min = (int) Math.ceil((tree.n+1)/2.0) - 1;
		entries = new ArrayList<NodeEntry>();

	}
	
	public void insertSorted(NodeEntry e)
	{
		int i = entries.size()-1;
		while(i>=0 && entries.get(i).key.compareTo(e.key) > 0)
		{
			i--;
		}
		entries.add(i+1, e);
		if(i>= 0 )
			entries.get(i).right = e.left;
		if(i+2 < entries.size())
			entries.get(i+2).left = e.right;

		
	}
	
	public ArrayList<NodeEntry> getSecondHalf(){
		int half = (int) Math.floor((max+1)/2.0);
		ArrayList<NodeEntry> secondHalf = new ArrayList<NodeEntry>();
		while(half<entries.size()){
			secondHalf.add(entries.remove(half));
		}
		return secondHalf;
		
	}
	
	
	public void borrow(NonLeaf sibling, NonLeaf parent, boolean left, int parentIdx)
	{
		if(left)
		{
			NodeEntry toBeBorrwed = sibling.entries.remove(sibling.entries.size()-1);
			this.entries.add(0,toBeBorrwed);
			//update parent
			parent.entries.get(parentIdx-1).key = toBeBorrwed.key;
		}
		else
		{
			NodeEntry toBeBorrwed = sibling.entries.remove(0);	
			this.entries.add(toBeBorrwed);
			Comparable newParent = sibling.entries.get(0).key;
			parent.entries.get(parentIdx).key = newParent;

			if(this.entries.size() == 1)
			{
				parent.entries.get(parentIdx-1).key = toBeBorrwed.key;
			}
		}
	}
	
	public String toString(){
		String res = "Start LEAF\n";
		
		for(NodeEntry ent : this.entries){
			res += ent.key+" ";
		}
		return res+"\nEnd LEAF\n";
	}
	
	
	
	
}
