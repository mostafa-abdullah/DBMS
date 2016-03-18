package DB;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

import BPTree.BPTree;
public class Table implements Serializable{
	String tableName;
	Hashtable<String,String> htblColNameType;
	Hashtable<String,String> htblColNameRefs;
	String strKeyColName;
	ArrayList<String> pages;
	TreeSet<Object> presentKeys;
	ArrayList<String> indices = new ArrayList<String>();
	
	/**
	 * The table has a name, columns with types, some of which may reference columns in another
	 * tables.
	 * It has a primary key, ArrayList of paths to its pages, and a set containing the keys of the 
	 * current existing tuples in this table.
	 * Once a table is created, a new page is created and attached to it.
	 * @param tName
	 * @param nt
	 * @param nr
	 * @param skc
	 * @throws IOException
	 */
	public Table(String tName, Hashtable<String,String> nt, Hashtable<String,String> nr, String skc) throws IOException{
		tableName = tName;
		htblColNameType = nt;
		htblColNameRefs = nr;
		strKeyColName = skc;
		pages = new ArrayList<String>();
		Page p = new Page(tableName,DBApp.getMaximumNumber());
		new File("data/"+tableName).mkdir();
		new File("data/"+tableName+"/indices").mkdir();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("data/"+tableName+"/0.class")));
		oos.writeObject(p);
		oos.close();
		pages.add("data/"+tableName+"/0.class");
		presentKeys = new TreeSet<Object>();
		
	}
	
	public String toString(){
		return tableName +" "+htblColNameType.size();
	}
	
}
