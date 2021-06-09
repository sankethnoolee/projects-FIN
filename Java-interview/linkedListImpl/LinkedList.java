package linkedListImpl;

public class LinkedList {
	Node head;
	
	public void insert(int data) {
		Node node=new Node();
		node.data=data;
		node.next=null;
		if(head==null) {
			head=node;
		}else {
			Node n =head;
			while(n.next!=null) {
				n=n.next;
			}
			n.next=node;
		}
	}

	public void show() {
		Node node=head;
		while(node.next!=null) {
			System.out.print(node.data+"->");
			node=node.next;
		}
		System.out.print(node.data);
	}
	public void insertAtStart(int data) {
		Node node= new Node();
		node.data=data;
		node.next=null;
		if(head==null) {
			head=node;
		}else {
			node.next=head;	
			head=node;
		}
	}
	public void insertAt(int pos, int data) {
		Node node=new Node();
		node.data=data;
		node.next=null;
		if(pos==0) {
			insertAtStart(data);
		}
		Node n= head;
		for (int i = 0; i < pos; i++) {
			if(i==pos-1) {
				Node prev=n;
				Node next=n.next;
				prev.next=node;
				node.next=next;
			}else {
				n=n.next;
			}
		}
	}
	public void delete(int index) {
		Node n=head;
		if(n.next==null) {
			head=null;
		}else if(index==0){
			head=head.next;
		}
		else {
			for (int i = 0; i < index; i++) {
				if(i==index-1) {
					Node prev=n;
					Node curr=n.next;
					Node next=curr.next;
					prev.next=next;
				}else {
					n=n.next;
				}
			}
		}
	}
}
