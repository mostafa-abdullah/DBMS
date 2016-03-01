package BPTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import DB.DBApp;

public class BPTree implements Serializable{
	int n;
	Node root;
	String rootPath;
	String tableName;
	String column;
	int countNodes;
	String pathToTree;
	public BPTree(int n, String tableName, String column) throws FileNotFoundException, IOException{
		this.n = n;
		this.tableName = tableName;
		this.column = column;
		countNodes = 1;
		root = new Leaf(this,null);
		root.min = 1;
		pathToTree = "data/"+tableName+"/indices/"+column;
		rootPath = pathToTree+"/0.class";
		new File(pathToTree).mkdirs();
		DBApp.writeObject(this, pathToTree+"/Btree.class");
		DBApp.writeObject(root, rootPath);
	}


	public void insert(Comparable key, String pagePath, int idx) throws ClassNotFoundException, IOException
	{
		String pathToLeaf = findLeaf(rootPath, key,null);

		System.out.println(pathToLeaf);
		Leaf leaf = (Leaf) DBApp.readObject(pathToLeaf);
		TuplePointer newPointer = new TuplePointer(idx, pagePath,key);
		if(leaf.pointers.size() == 0){
			leaf.pointers.add(newPointer);
			DBApp.writeObject(leaf, pathToLeaf);
			return;
		}
		leaf.insertSorted(newPointer);
		//		if((Integer)key == 125){
		//			System.out.println(leaf);
		//		}
		if(leaf.pointers.size() > leaf.max)
		{
			Leaf newLeaf = new Leaf(this, leaf.parent);
			newLeaf.nextLeafPath = leaf.nextLeafPath;
			newLeaf.prevLeafPath = pathToLeaf;

			String newPath = pathToTree+"/"+countNodes++ + ".class";
			leaf.nextLeafPath = newPath;
			//			System.out.println(leaf);

			newLeaf.pointers = leaf.getSecondHalf();

			//			for(TuplePointer tp : newLeaf.pointers){
			//				System.out.print(tp.key+" ");
			//			}
			DBApp.writeObject(newLeaf, newPath);
			DBApp.writeObject(leaf, pathToLeaf);

			insertIntoNonLeaf(leaf.parent, new NodeEntry(newLeaf.pointers.get(0).key,pathToLeaf,newPath));

		}
		DBApp.writeObject(leaf, pathToLeaf);

	}

	public void insertIntoNonLeaf(String pathToNode, NodeEntry ne) throws ClassNotFoundException, IOException
	{
		//		System.out.println(pathToNode);
		if(pathToNode == null)
		{

			NonLeaf root = new NonLeaf(this, null);
			if(this.root != null)
			{
				this.root.min = (int)Math.ceil((n+1)/2.0) - 1;

			}
			root.min = 1;
			rootPath = pathToTree+"/"+countNodes++ + ".class";
			root.entries.add(ne);
			this.root = root;
			DBApp.writeObject(root, rootPath);
			return;
		}
		NonLeaf nl = (NonLeaf) DBApp.readObject(pathToNode);

		nl.insertSorted(ne);

		//		System.out.println(nl);

		if(nl.entries.size() > nl.max)
		{

			ArrayList<NodeEntry> nes = nl.getSecondHalf();

			NonLeaf newNode = new NonLeaf(this, nl.parent);
			String newPath = pathToTree+"/"+(countNodes++) + ".class";
			newNode.entries = nes;

			NodeEntry first = nes.remove(0);

			first.left = pathToNode;
			first.right = newPath;


			DBApp.writeObject(newNode, newPath);			 
			insertIntoNonLeaf(nl.parent,first);


		}
		DBApp.writeObject(nl, pathToNode);

	}

	public void delete(Comparable key) throws ClassNotFoundException, IOException
	{
		String pathToLeaf = findLeaf(rootPath, key, null);
		Leaf leaf = (Leaf) DBApp.readObject(pathToLeaf);
		int deletedIdx = leaf.deleteKey(key);
		if(leaf.parent == null)
			return;
		if(leaf.pointers.size() < leaf.min){
			
			NonLeaf parent = (NonLeaf) DBApp.readObject(leaf.parent);
			
			LeftAndRightSiblings lrs = getSibLings(pathToLeaf, parent);
			String siblingLeft = lrs.sibLingLeft;
			String siblingRight = lrs.sibLingRight;
			int parentIdx = lrs.idx;
			
			Leaf leftLeaf = null;
			Leaf rightLeaf = null;
			if(siblingLeft != null)
			{
				// borrow from left
				leftLeaf = (Leaf) DBApp.readObject(siblingLeft);
				if(leftLeaf.pointers.size() > leftLeaf.min)
				{
					leaf.borrowTuple(leftLeaf, parent, true, parentIdx, key);
				}
				else if(siblingRight != null)
				{

					rightLeaf = (Leaf) DBApp.readObject(siblingRight);
					if(rightLeaf.pointers.size() > rightLeaf.min)
					{
						leaf.borrowTuple(rightLeaf, parent, false, parentIdx, key);
					}
					else
					{
						//etfshe5
					}
				}
					
			}
			else if(siblingRight != null)
			{
				// borrow from right
				rightLeaf = (Leaf) DBApp.readObject(siblingRight);
				if(rightLeaf.pointers.size() > rightLeaf.min)
				{
					leaf.borrowTuple(rightLeaf, parent, false, parentIdx, key);
				}
				else
				{
					//etfshe5
				}
			}
			else
			{
				//rawwa7 le omak
			}




			
		}
		else
		{
			if(deletedIdx == 0)
			{
				Comparable newKey = leaf.pointers.get(0).key;
				updateUpper(key,newKey,leaf.parent);
			}
		}

	}
	
	
	
	public void handleParent(String pathToNode) throws ClassNotFoundException, IOException
	{
		NonLeaf currentNode = (NonLeaf) DBApp.readObject(pathToNode);
		if(currentNode.entries.size() >= currentNode.min)
			return;
		if(currentNode.parent == null)
		{
			// current node is the root
		}
		else
		{
			NonLeaf parent = (NonLeaf) DBApp.readObject(currentNode.parent);
			LeftAndRightSiblings lrs = getSibLings(pathToNode, parent);
			String siblingLeft = lrs.sibLingLeft;
			String siblingRight = lrs.sibLingRight;
			int parentIdx = lrs.idx;
			Leaf leftLeaf = null;
			Leaf rightLeaf = null;
			if(siblingLeft != null)
			{
				// borrow from left
				leftLeaf = (Leaf) DBApp.readObject(siblingLeft);
				if(leftLeaf.pointers.size() > leftLeaf.min)
				{
				//	currentNode.borrowTuple(leftLeaf, parent, true, parentIdx);
				}
				else if(siblingRight != null)
				{

					rightLeaf = (Leaf) DBApp.readObject(siblingRight);
					if(rightLeaf.pointers.size() > rightLeaf.min)
					{
						//currentNode.borrowTuple(rightLeaf, parent, false, parentIdx);
					}
					else
					{
						//etfshe5
					}
				}
					
			}
			else if(siblingRight != null)
			{
				// borrow from right
				rightLeaf = (Leaf) DBApp.readObject(siblingRight);
				if(rightLeaf.pointers.size() > rightLeaf.min)
				{
					//currentNode.borrowTuple(rightLeaf, parent, false, parentIdx);
				}
				else
				{
					//etfshe5
				}
			}
			else
			{
				//rawwa7 le omak
			}
		}
	}
	
	
	public LeftAndRightSiblings getSibLings(String pathToNode, NonLeaf parent)
	{
		String siblingLeft = null;
		String siblingRight = null;
		int parentIdx = -1;
		for(int i=0; i<parent.entries.size(); i++)
		{
			NodeEntry e = parent.entries.get(i);
			if(e.left.equals(pathToNode)){
				parentIdx = i;
				if(i > 0)
					siblingLeft = parent.entries.get(i-1).right;
				siblingRight = e.right;
				break;
			}
		}

		if(parent.entries.get(parent.entries.size()-1).right.equals(pathToNode)){
			
			parentIdx = parent.entries.size();
			siblingLeft = parent.entries.get(parent.entries.size()-1).left;
		}
		
		return new LeftAndRightSiblings(siblingLeft, siblingRight, parentIdx);
	}

	
	
	static class LeftAndRightSiblings{
		String sibLingLeft;
		String sibLingRight;
		int idx;
		
		public LeftAndRightSiblings(String left, String right, int i){
			this.sibLingLeft = left;
			this.sibLingRight = right;
			this.idx = i;
			
		}
	}

	public void updateUpper(Comparable oldKey, Comparable newKey ,String pathToNode) throws ClassNotFoundException, IOException
	{
		if(pathToNode == null)
			return;
		NonLeaf nextNode = (NonLeaf) DBApp.readObject(pathToNode);

		for(NodeEntry e: nextNode.entries){
			if(e.key.equals(oldKey))
			{
				e.key = newKey;
				return;
			}
		}
		updateUpper(oldKey,newKey, nextNode.parent);
	}

	public TuplePointer find(Comparable key) throws ClassNotFoundException, IOException{
		if(root instanceof Leaf)
		{
			Leaf r = (Leaf) root;
			for(int i = 0; i<r.pointers.size(); i++)
			{
				Object k = r.pointers.get(i);
				if(k.equals(key))
				{
					return r.pointers.get(i);
				}
			}
		}
		else
		{
			Leaf leaf = (Leaf)DBApp.readObject(findLeaf(rootPath,key,null));
			for(int i=0; i<leaf.pointers.size(); i++){
				if(leaf.pointers.get(i).key.equals(key))
				{
					return leaf.pointers.get(i);
				}
				else if(leaf.pointers.get(i).key.compareTo(key) > 0)
				{
					break;
				}
			}
		}
		return null;
	}


	public String findLeaf(String current, Object key, String parent) throws ClassNotFoundException, IOException
	{
		Node cur = (Node) DBApp.readObject(current);
		cur.parent = parent;
		DBApp.writeObject(cur, current);
		if(cur instanceof Leaf)
		{
			return current;
		}

		NonLeaf curr = (NonLeaf) cur;
		String path= "";
		for(int i = 0; i<curr.entries.size(); i++)
		{
			if(curr.entries.get(i).key.compareTo(key) > 0){
				path = curr.entries.get(i).left;
				break;
			}
			else if(i==curr.entries.size()-1){
				path = curr.entries.get(i).right;
			}

		}

		return findLeaf(path, key, current);



	}


	public String toString(){
		try {
			return printTree(rootPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERROR";

		}
	}

	public String printTree(String path) throws ClassNotFoundException, IOException{

		Node n = (Node) DBApp.readObject(path);
		if(n instanceof Leaf)
		{
			String res = "";
			Leaf l = (Leaf) n;
			res+= "LEAF: ";
			for(TuplePointer tp : l.pointers)
			{
				res += tp.key.toString()+" ";
			}

			res+="\n";
			return res;
		}
		NonLeaf nl = (NonLeaf) n;
		String res = "";
		for(NodeEntry e : nl.entries)
		{
			res += e.key.toString()+" ";
		}
		res += "\n";

		res+= printTree(nl.entries.get(0).left);
		for(NodeEntry e : nl.entries)
		{
			res +=  printTree(e.right);
		}


		return res;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		BPTree tr = new BPTree(2,"Student", "ID");

		tr.insert(12, "", 5);
		tr.insert(8, "", 5);
		tr.insert(1, "", 5);

		tr.insert(23, "", 5);

		tr.insert(5, "", 5);
		System.out.println(tr+"--------------------------------");


		tr.insert(7, "", 5);
		System.out.println(tr+"--------------------------------");
		tr.insert(2, "", 5);
		System.out.println(tr+"--------------------------------");
		tr.insert(28, "", 5);
		System.out.println(tr+"--------------------------------");
		tr.insert(9, "", 5);
		System.out.println(tr+"--------------------------------");
		tr.insert(18, "", 5);
		System.out.println(tr+"--------------------------------");
		tr.insert(24, "", 5);
		System.out.println(tr+"--------------------------------");
		tr.insert(40, "", 5);
		System.out.println(tr+"--------------------------------");
		tr.insert(48, "", 5);
		System.out.println(tr+"--------------------------------");

		//		NonLeaf nl = (NonLeaf) DBApp.readObject("data/Student/indices/ID/7.class");
		//		System.out.println(nl.entries.get(0).right);
	}

}
