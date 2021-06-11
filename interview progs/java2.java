class Node{
  int val;
  Node next;
}
Input : 1 -  2 - 3 - 4 -5 
Output : 5- 4 -3 - 2- 1
public Node reverse(Node head){
	Node prev = null;
	Node next = null;
	Node current = head;
	
	
	while (current!=null){
		next = current.next;
		current.next = prev;
		prev = current;
		current = next;
	
	}
	
	return prev;
	

}



Q - Input: nums = [1,2,7,11,15], target = 9
Output: [0,1]


public List<Integer> getSum(List<Integer> list, int target){
	Map<Integer,Integer> valIndexMap = new HashMap<Integer,Integer>();
	List<Integer> indexValues = new ArrayList<Integer>();
	//will using streams
	for(int i = o; i<list.size; i++){
		valIndexMap.put(list.get(i),i);
	}
	
	for(int i = o; i<list.size; i++){
		if(valIndexMap.get(target - list.get(i))!=null && valIndexMap.get(target - list.get(i))!=i){
			indexValues.add(i);
			indexValues.add(valIndexMap.get(target - list.get(i)));
		}
	}
	
	return indexValues;

}






class Node{
  int val;
  Node left;
  Node right;
}

public boolean isSumPresent(Node root, Integer currentSum ,Integer sum){
	
	boolean res = false;
	
	
	if(root.left==null && root.right==null && sum!=currentSum){
		return false;
	}else if(){
	
	}
	
	boolean leftVal = (isSumPresent(root.left.val,currentSum,sum)+currentSum) + root.val;
	boolean rightVal = (isSumPresent(root.right.val,currentSum,sum)+currentSum) + root.val;
	
	if(leftVal || rightVal){
		return true;
	}
	return res;
	

}




class Ratio implements Comparable<Ratio>{
	int a;
	int b;
	
	public int compareTo(Ratio o1, Ratio o2){
		if(){
		
		
		}
	}
}

public void ratio(Ratio[] ratios){



}






class SingletonEx{
	private SingletonEx instance = null;
	
	private SingletonEx(){
	
	
	}
	
	public static SingletonEx getInstance(){
		if(instance == null){
			instance = new SingletonEx()
		}
		return instance;
	}


}






public boolean hasPathSum(TreeNode root, int sum) {
    if (root == null)
      return false;

    sum -= root.val;
    if ((root.left == null) && (root.right == null))
      return (sum == 0);
    return hasPathSum(root.left, sum) || hasPathSum(root.right, sum);
  }