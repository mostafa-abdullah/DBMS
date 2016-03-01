package BPTree;

import java.io.IOException;
import java.util.ArrayList;

public class Leaf extends Node {

	String nextLeafPath;
	String prevLeafPath;
	ArrayList<TuplePointer> pointers;

	public Leaf(BPTree tree, String parent) {
		super(tree, parent);
		this.min = (tree.n+1)/2;
		pointers = new ArrayList<TuplePointer>();
	}

	public void insertSorted(TuplePointer tp)
	{
		int i = pointers.size()-1;
		while(i>=0 && pointers.get(i).key.compareTo(tp.key) > 0)
		{
			i--;
		}

		pointers.add(i+1, tp);
	}

	public ArrayList<TuplePointer> getSecondHalf(){
		int half = (int) Math.floor((max+1)/2.0);
		ArrayList<TuplePointer> secondHalf = new ArrayList<TuplePointer>();
		while(half<pointers.size()){
			secondHalf.add(pointers.remove(half));
		}
		return secondHalf;

	}

	public int deleteKey(Object key){
		for(int i=0; i<pointers.size(); i++)
			if(pointers.get(i).key.equals(key)){
				TuplePointer deleted = pointers.remove(i);
				return i;

			}
		return -1;
	}

	public void borrowTuple(Leaf sibling, NonLeaf parent, boolean left,int parentIdx,Comparable dKey) throws ClassNotFoundException, IOException
	{
		if(left)
		{
			TuplePointer toBeBorrwed = sibling.pointers.remove(sibling.pointers.size()-1);
			this.pointers.add(0,toBeBorrwed);
			//update parent
			parent.entries.get(parentIdx-1).key = toBeBorrwed.key;
		}
		else
		{
			TuplePointer toBeBorrwed = sibling.pointers.remove(0);	
			this.pointers.add(toBeBorrwed);
			Comparable newParent = sibling.pointers.get(0).key;
			parent.entries.get(parentIdx).key = newParent;

			if(this.pointers.size() == 1)
			{
				if(parentIdx==0)
					this.tree.updateUpper(dKey, toBeBorrwed.key, this.parent);
				else
					parent.entries.get(parentIdx-1).key = toBeBorrwed.key;
			}
		}
	}


	public void mergeWithLeaf(Leaf sibling, NonLeaf parent, int parentIdx, boolean left, Comparable dKey)
	{
		if(left)
		{
			this.pointers.addAll(0,sibling.pointers);
			if(parentIdx > 0)
			{
				if(parentIdx > 1){
					parent.entries.get(parentIdx - 2).right = parent.entries.get(parentIdx-1).right;
				}
				parent.entries.remove(parentIdx - 1);

			}
		}
		else
		{
			sibling.pointers.addAll(0,this.pointers);

			if(parentIdx > 0){
				parent.entries.get(parentIdx - 1).right = parent.entries.get(parentIdx).left;
			}
			parent.entries.remove(parentIdx);

		}
	}


	public String toString(){
		String res = "Start LEAF\n";

		for(TuplePointer tp : this.pointers){
			res += tp.key+" ";
		}
		return res+"\nEnd LEAF\n";
	}



}