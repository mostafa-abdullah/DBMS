package DB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import Exceptions.DBAppException;
import Exceptions.DBEngineException;


public class DBApp {


	/**
	 * This method reads the configuration file, and returns the the maximum
	 * number of tuples in a single page.
	 * It is static because it may be called in different contexts and it is
	 * not attached to this DB only.
	 * @return
	 * @throws IOException
	 */
	public static int getMaximumNumber() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("config/DBApp.config"));
		String maxTuplesLine = br.readLine();
		StringTokenizer st = new StringTokenizer(maxTuplesLine,"=");
		st.nextToken();
		return Integer.parseInt(st.nextToken());
	}

	/**
	 * init() method creates the meta-data csv file if it is not already existing.
	 * It also creates the config file and writes the maximum number of tuples in a page
	 * into it
	 * 
	 * @throws IOException
	 */
	public void init( ) throws IOException{


		File f = new File("data/metadata.csv");
		PrintWriter pw;
		if(!f.exists()){
			pw = new PrintWriter(f);
			pw.println("Table Name,Column Name,Column Type,Key,Indexed,References");
			pw.flush();
			pw.close();
		}

		File config = new File("config/DBApp.config");
		if(!config.exists()){
			pw = new PrintWriter(config);
			pw.println("N=200");
			pw.flush();
			pw.close();
		}
	}


	/**
	 * createTable is designed to create a new table in the database. Its input is the table
	 * name, the columns types, information about the foreign key references and the primary
	 * key of the table.  
	 * It may throw two different exceptions: the first one is thrown if the table already
	 * exists in the database, the other one is thrown if the provided foreign key are invalid:
	 * either the data types or the column names don't match.
	 * 
	 * @param strTableName
	 * @param htblColNameType
	 * @param htblColNameRefs
	 * @param strKeyColName
	 * @throws DBAppException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void createTable(String strTableName,    Hashtable<String,String> htblColNameType, 
			Hashtable<String,String> htblColNameRefs, String strKeyColName)  throws DBAppException, FileNotFoundException, IOException, ClassNotFoundException{
		Table tb =new Table(strTableName, htblColNameType, htblColNameRefs, strKeyColName);
		File table = new File("data/"+strTableName+"/table.class");
		if(table.exists()){
			throw new DBAppException("Error: Table "+ strTableName +" already exists.");
		}
		boolean validReference = checkValidReference(htblColNameRefs,htblColNameType);
		if(!validReference){
			throw new DBAppException("Error: Invalid references.");
		}
		
		writeObject(tb,"data/"+strTableName+"/table.class");
		try {

			for(String x: htblColNameType.keySet()){
				String text = strTableName+","+x+","+htblColNameType.get(x)+","+(x.equals(strKeyColName)?"True":"False")+",False,"+(htblColNameRefs.containsKey(x)?htblColNameRefs.get(x):"null\n");
				Files.write(Paths.get("data/metadata.csv"), text.getBytes(), StandardOpenOption.APPEND);
			}


		}catch (IOException e) {
			//handle exception
		}

	}


	/**
	 * This method checks if the provided references are valid or not.
	 * It validates 3 things:
	 * 		1) The referenced table exists.
	 * 		2) The name of the referenced column exists in the referenced table
	 * 		3) The data-type of the referencing column matches with that of the referenced one.
	 *
	 * @param htblColNameRefs
	 * @param htblColNameType
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private boolean checkValidReference(
			Hashtable<String, String> htblColNameRefs,
			Hashtable<String, String> htblColNameType) throws ClassNotFoundException, IOException {
		//			System.out.println(htblColNameRefs);
		//			System.out.println(htblColNameType);
		for(String key : htblColNameRefs.keySet()){
			if(!htblColNameType.containsKey(key))
				return false;

			StringTokenizer st = new StringTokenizer(htblColNameRefs.get(key),".");
			String tableName = st.nextToken();
			String foreignKey = st.nextToken();
			if(!checkTableExists(tableName))
				return false;

			Table t = (Table)readObject("data/"+tableName+"/table.class");
			if(!foreignKey.equals(t.strKeyColName))
				return false;
			if(!htblColNameType.get(key).equals(t.htblColNameType.get(t.strKeyColName)))
				return false;
		}
		return true;
	}



	/**
	 * This method selects certain data from a certain table in the DB.
	 * Its input is the target table name, the conditions to filter the records and the
	 * operator to use in filtration (either AND or OR).
	 * Before selection, it validates 2 things:
	 *		1) The table name matches with an already existing table.
	 *		2) The provided columns match with existing columns in the table.
	 * Technique of searching:
	 * 		It accesses the pages of the table, then reads these pages one page at a time,
	 * 		searches for the record in this page according to two scenarios:
	 * 		If the provided operator is OR, only matching one field is sufficient for 
	 * 		matching the whole record. If it is AND, all the fields must match.
	 * 		The matched fields are added to an ArrayList, then its iterator is returned.
	 * @param strTable
	 * @param htblColNameValue
	 * @param strOperator
	 * @return
	 * @throws DBEngineException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws DBAppException
	 */

	public Iterator selectFromTable(String strTable,  Hashtable<String,Object> htblColNameValue, 
			String strOperator) throws DBEngineException, FileNotFoundException, ClassNotFoundException, IOException, DBAppException{
		boolean tableExists = checkTableExists(strTable);
		if(!tableExists){
			//throw exception;
			throw new DBAppException("Error: Table "+ strTable +" not found.");
		}
		Table t = (Table)readObject("data/"+strTable+"/table.class");
		boolean validColoumns = checkValidColoumns(t, htblColNameValue);
		if(!validColoumns)
		{
			throw new DBAppException("Error: Invalid provided coloumns.");
		}
		ArrayList<Tuple> result = new ArrayList<Tuple>();


		for(String pagePath : t.pages){
			Page p = (Page)readObject(pagePath);
			for(Tuple tup : p.tuples){
				if(tup == null)
					continue;
				boolean addAnd = true;
				for(String key : htblColNameValue.keySet()){
					if(htblColNameValue.get(key).equals(tup.record.get(key))){
						if(strOperator.equalsIgnoreCase("or")){
							result.add(tup);
							break;
						}
					}
					else if(strOperator.equalsIgnoreCase("and")){
						addAnd = false;
						break;
					}
				}
				if(strOperator.equalsIgnoreCase("and") && addAnd){
					result.add(tup);
				}
			}
		}


		return result.iterator();
	}



	/**
	 * This method inserts records in a table. Its input is the target table's name and the
	 * values of inserted record's fields.
	 * It validates 5 things:
	 * 		1) The target table exists.
	 * 		2) The input columns exist in the provided table, and their data types match.
	 * 		3) The value of the primary key is provided
	 * 		4) The primary key of the inserted tuple is not existing before.
	 * 		5) If there is a reference to record in another table, it checks if this
	 *		   column exists in the later table.
	 *
	 * To insert the tuple, we fetch the last page in the table and there are two scenarios:
	 * Either this page is full or not. If it is full, a new page is created and the record is
	 * written into it. If it is not, the tuple is inserted into this page.
	 * 
	 * @param strTableName
	 * @param htblColNameValue
	 * @throws DBAppException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void insertIntoTable(String strTableName, Hashtable<String,Object> htblColNameValue)  throws DBAppException, FileNotFoundException, IOException, ClassNotFoundException{
		boolean tableExists = checkTableExists(strTableName);

		if(!tableExists){
			throw new DBAppException("Error: Table "+ strTableName +" not found.");
		}

		Table t = (Table)readObject("data/"+strTableName+"/table.class");
		boolean validColoumns = checkValidColoumns(t,htblColNameValue);
		if(!validColoumns){
			throw new DBAppException("Error: Invalid provided coloumns.");
		}
		String tableKey = t.strKeyColName;
		Object tupleKey = htblColNameValue.get(tableKey);
		if(tupleKey == null){
			throw new DBAppException("Error: Primary key constraint violation: Table key not found.");
		}
		if(t.presentKeys.contains(tupleKey)){
			throw new DBAppException("Error: Primary key constraint violation: Entry with key: "+ tupleKey +" already exists.");
		}

		boolean referencedExists = checkRefrencedExists(t, htblColNameValue);
		if(!referencedExists){
			throw new DBAppException("Error: Foreign key constraint violated.");
		}
		String pagePath = t.pages.get(t.pages.size() - 1);
		Page p = (Page)readObject(pagePath);
		if(p.numObjects == p.N){
			//create new page;
			Page newP = new Page(strTableName, getMaximumNumber());
			t.pages.add("data/"+strTableName+"/"+t.pages.size()+".class");
			newP.tuples[newP.numObjects++] = new Tuple(htblColNameValue);
			t.presentKeys.add(tupleKey);
			writeObject(newP,t.pages.get(t.pages.size()-1));			
			writeObject(t,"data/"+strTableName+"/table.class");
		}
		else{
			p.tuples[p.numObjects++] = new Tuple(htblColNameValue);
			t.presentKeys.add(tupleKey);
			writeObject(t,"data/"+strTableName+"/table.class");
			writeObject(p, t.pages.get(t.pages.size()-1));
		}
		
	}
	
	/**
	 * This method updates an existing tuple in a table.
	 * It validates 2 things:
	 * 		1) The table exists in the database
	 * 		2) The columns to update exist in this table
	 * It loops over the table pages searching for the given key, and updates the tuple
	 * whenever it finds it. Then it rewrites the page and table files.
	 * 
	 * 
	 * @param strTableName
	 * @param strKey
	 * @param htblColNameValue
	 * @throws DBAppException
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */

	public void updateTable(String strTableName,
			Object strKey,
			Hashtable<String,Object> htblColNameValue
			)throws DBAppException, ClassNotFoundException, IOException{
		boolean tableExists = checkTableExists(strTableName);
		if(!tableExists)
			throw new DBAppException("Error: Table doesn't exist");
		Table t = (Table) readObject("data/"+strTableName+"/table.class");
		boolean validColumns = checkValidColoumns(t, htblColNameValue);
		if(!validColumns)
			throw new DBAppException("Error: Input columns mismatch with table columns");
		boolean found = false;
		for(String pagePath: t.pages){
			if(found)
				break;
			Page p = (Page) readObject(pagePath);
			for(Tuple tup : p.tuples){
				if(found)
					break;
				if(tup.record.get(t.strKeyColName).equals(strKey)){
					for(String key : htblColNameValue.keySet()){
						tup.record.put(key, htblColNameValue.get(key));
					}
					found = true;
					writeObject(p, pagePath);
				}
			}
		}
		
		
	}
	
	/**
	 * This method deletes one or more entries from a target table.
	 * Its input is the table's name, the fields to search for, and the logical
	 * operator to use (Either AND or OR).
	 * It loops over the table's page one by one, searches in each page for the
	 * target tuple, and whenever it finds it, it sets it to null.
	 * 
	 * The method validates 2 things:
	 * 		1) The table exists in the database.
	 * 		2) The input column names and types match with those in the target table.
	 * @param strTableName
	 * @param htblColNameValue
	 * @param strOperator
	 * @throws DBEngineException
	 * @throws DBAppException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	
	public void deleteFromTable(String strTableName,
			Hashtable<String,Object> htblColNameValue,
			String strOperator)
			throws DBEngineException, DBAppException, ClassNotFoundException, IOException{
		boolean tableExists = checkTableExists(strTableName);
		if(!tableExists)
			throw new DBAppException("Error: Table doesn't exist.");
		Table t = (Table) readObject("data/"+strTableName+"/table.class");
		
		boolean validColumns = checkValidColoumns(t, htblColNameValue);
		if(!validColumns)
			throw new DBAppException("Error: Input columns names or types don't match with table's columns.");
		for(String pagePath : t.pages){
			Page p = (Page)readObject(pagePath);
//			for(Tuple tup : p.tuples){
			for(int i=0; i<p.tuples.length; i++){
				Tuple tup = p.tuples[i];
				if(tup == null)
					continue;
				boolean addAnd = true;
				for(String key : htblColNameValue.keySet()){
					if(htblColNameValue.get(key).equals(tup.record.get(key))){
						if(strOperator.equalsIgnoreCase("or")){
							p.tuples[i] = null;
							break;
						}
					}
					else if(strOperator.equalsIgnoreCase("and")){
						addAnd = false;
						break;
					}
				}
				if(strOperator.equalsIgnoreCase("and") && addAnd){
					p.tuples[i] = null;
				}
			}
			writeObject(p, pagePath);
		}
		
	}
	
	
	
	


	

	/**
	 * This method checks if foreign keys in the provided columns actually exist in the
	 * referenced table or not.
	 * 
	 * @param t
	 * @param htblColNameValue
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private boolean checkRefrencedExists(Table t,
			Hashtable<String, Object> htblColNameValue) throws ClassNotFoundException, IOException {
		for(String key : t.htblColNameRefs.keySet()){
			if(!htblColNameValue.containsKey(key))
				continue;
			StringTokenizer st = new StringTokenizer(t.htblColNameRefs.get(key),".");
			String refrencedTableName = st.nextToken();
			Table refTable = (Table)readObject("data/"+refrencedTableName+"/table.class");
			if(!refTable.presentKeys.contains(htblColNameValue.get(key))){
				return false;
			}
		}
		return true;
	}



	/**
	 * This method checks if the provided columns exist in a certain table,
	 * and that their data types match.
	 * @param t
	 * @param htblColNameValue
	 * @return
	 */
	private boolean checkValidColoumns(Table t,
			Hashtable<String, Object> htblColNameValue) {
		for(String x: htblColNameValue.keySet()){
			if(!t.htblColNameType.keySet().contains(x))
				return false;
			if(htblColNameValue.get(x) == null)
				continue;
			if(t.htblColNameType.get(x).contains("Double")){
				if(!(htblColNameValue.get(x) instanceof Double))
					return false;
			}
			else if(t.htblColNameType.get(x).contains("Integer")){
				if(!(htblColNameValue.get(x) instanceof Integer))
					return false;
			}
			else if(t.htblColNameType.get(x).contains("String")){
				if(!(htblColNameValue.get(x) instanceof String))
					return false;
			}
			else if(t.htblColNameType.get(x).contains("Data")){
				if(!(htblColNameValue.get(x) instanceof Date))
					return false;
			}
			else if(t.htblColNameType.get(x).contains("Boolean")){
				if(!(htblColNameValue.get(x) instanceof Boolean))
					return false;
			}
			else
				return false;
		}
		return true;
	}


	/**
	 * This method checks if the provided table exists in the database.
	 * @param strTableName
	 * @return
	 */
	private boolean checkTableExists(String strTableName) {
		File f = new File("data/"+strTableName+"/table.class");
		if(!f.exists())
			return false;
		return true;
	}


	/**
	 * This method reads a serializable object form a certain path, and returns it.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object readObject(String path) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path)));
		Object o = ois.readObject();
		ois.close();
		return o;
	}


	/**
	 * This method writes a serializable object to a certain path.
	 * 
	 * @param x
	 * @param path
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void writeObject(Object x,String path) throws FileNotFoundException, IOException{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
		oos.writeObject(x);
		oos.close();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, DBAppException, IOException, DBEngineException {
//		DBApp x = new DBApp();
//		Hashtable<String,Object> ht = new Hashtable<String,Object>();
//		ht.put("Last_Name", "LN21");
//		ht.put("Age", 35);
////		x.updateTable("Student", 20, ht);
//		x.deleteFromTable("Student", ht, "OR");
//		new File("data/bla").mkdir();
	}







}
