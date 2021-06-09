package stackImpl;

public class Stack {

	int[] stack = new int[5];
	int top=0;
	
	public void push(int data) {
		stack[top]=data;
		top++;
	}
	public void show() {
		for (int i = 0; i <top; i++) {
			System.out.println(stack[i]+"->");
		}
	}
}
