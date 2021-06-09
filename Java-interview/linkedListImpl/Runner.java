package linkedListImpl;

public class Runner {

	public static void main(String[] args) {
		LinkedList l=new LinkedList();
		l.insert(10);
		l.insert(13);
		l.insert(19);
		l.show();
		System.out.println("--------insert-------------------------");
		l.insertAtStart(1);
		l.show();
		System.out.println("----------insertAtStart-----------------------");
		l.insertAtStart(100);
		l.show();
		System.out.println("---------insertAt------------------------");
		l.insertAt(1,230);
		l.insertAt(0,210);
		l.show();
		System.out.println("---------delete------------------------");
		l.delete(5);
		l.show();
	}

}
