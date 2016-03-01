package DB;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Date;
public class Tuple implements Serializable{
	Hashtable<String, Object> record;
	Date lastUpdated;
	
	public Tuple(Hashtable<String, Object> r){
		record = r;
		updateDate();
	}
	
	public void updateDate(){
		lastUpdated = new Date();
	}
	
	public String toString(){
		return record+", Last Updated: "+lastUpdated;
	}
	
	public static void main(String[] args) {
		Hashtable<String,Object> ctblColNameValue1 = new Hashtable<String,Object>();
		ctblColNameValue1.put("ID", Integer.valueOf( "1" ) );
		ctblColNameValue1.put("Name", "Data Bases II");
		ctblColNameValue1.put("Code", "CSEN 604");
		ctblColNameValue1.put("Hours", Integer.valueOf( "4" ));
		ctblColNameValue1.put("Semester", Integer.valueOf( "6" ));
		ctblColNameValue1.put("Major_ID", Integer.valueOf( "1" ));
		Tuple t = new Tuple(ctblColNameValue1);
		System.out.println(t);
	}
}
