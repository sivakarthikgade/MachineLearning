
import java.util.ArrayList;
import java.util.List;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[]) {
		List<String> l = new ArrayList<String>();
		l.add("hello");
		l.add("howdy");
		l.add("whack");
		l.add("xbox");
		
		String str = "helloo";
		System.out.println(l.contains("hello"));
	}
}
