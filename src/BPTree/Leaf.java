package BPTree;

import java.io.IOException;
import java.util.ArrayList;

import DB.DBApp;

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

	public void borrowTuple(BPTree tree, Leaf sibling, NonLeaf parent, boolean left,int parentIdx,Comparable dKey) throws ClassNotFoundException, IOException
	{
		boolean willUpdateUpper = this.pointers.size() == 0 || this.pointers.get(0).key.compareTo(dKey) > 0;
		if(left)
		{
//			willUpdateUpper = false;
			TuplePointer toBeBorrwed = sibling.pointers.remove(sibling.pointers.size()-1);
			this.pointers.add(0,toBeBorrwed);
			//update parent
			parent.entries.get(parentIdx-1).key = toBeBorrwed.key;
		}
		else
		{
			willUpdateUpper &= parentIdx == 0;
			TuplePointer toBeBorrwed = sibling.pointers.remove(0);	
			this.pointers.add(toBeBorrwed);
			Comparable newParent = sibling.pointers.get(0).key;
			parent.entries.get(parentIdx).key = newParent;

			if(willUpdateUpper)
			{
				if(parentIdx==0){
					
					tree.updateUpper(dKey, toBeBorrwed.key, this.parent);
				}
				else
					parent.entries.get(parentIdx-1).key = toBeBorrwed.key;
			}
		}
//		System.out.println(willUpdateUpper);
	}


	public void mergeWithLeaf(BPTree tree, Leaf sibling, NonLeaf parent, int parentIdx, boolean left, Comparable dKey) throws ClassNotFoundException, IOException
	{
		String tmpPath = null;
		boolean willUpdateUpper = this.pointers.size() == 0 || this.pointers.get(0).key.compareTo(dKey) > 0; 
		Comparable newKey = null;
		if(left)
		{
			willUpdateUpper = false;
			this.pointers.addAll(0,sibling.pointers);
			if(parentIdx > 0)
			{
				if(parentIdx > 1){
					parent.entries.get(parentIdx - 2).right = parent.entries.get(parentIdx-1).right;
				}
				else if(parent.entries.size() == 1)
				{
					tmpPath = parent.entries.get(parentIdx-1).right;
				}
				parent.entries.remove(parentIdx - 1);

			}
		}
		else
		{
			willUpdateUpper &= parentIdx == 0;
			sibling.pointers.addAll(0,this.pointers);

			if(parentIdx > 0){
				parent.entries.get(parentIdx - 1).right = parent.entries.get(parentIdx).right;
			}
			else if(parent.entries.size() == 1)
			{
				tmpPath = parent.entries.get(parentIdx).right;
			}
			parent.entries.remove(parentIdx);
			newKey = sibling.pointers.get(0).key;
		}
		if(willUpdateUpper){
			tree.updateUpper(dKey, newKey, this.parent);
			System.out.println(((NonLeaf)DBApp.readObject(this.parent)));
//			System.out.println(dKey+" " +newKey +" "+willUpdateUpper);
		}
		tree.handleParent(parent, this.parent,tmpPath);
		
		
	}


	public String toString(){
		String res = "Start LEAF\n";

		for(TuplePointer tp : this.pointers){
			res += tp.key+" ";
		}
		return res+"\nEnd LEAF\n";
	}



}
