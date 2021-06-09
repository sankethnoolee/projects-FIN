package stackImpl;

public class Runner {

	public static void main(String[] args) {
		Stack stack= new Stack();
		System.out.println("-------push-----------");
		stack.push(10);
		stack.push(20);
		stack.push(5);
		System.out.println("-------show-----------");
		stack.show();

	}

}
