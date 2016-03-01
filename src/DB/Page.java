package DB;
import java.io.Serializable;


public class Page implements Serializable{
	String tableName;
	Tuple[] tuples;
	int numObjects;
	int N;
	
	public Page(String tableName, int N){
		this.tableName = tableName;
		this.N = N;
		numObjects = 0;
		tuples = new Tuple[N];
	}
	
	
	
}
