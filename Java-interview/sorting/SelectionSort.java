package sorting;

public class SelectionSort {

	public static void main(String[] args) {
		int[] arr= {19,1,2,7,5,5,9,11};
		int temp=0;
		for (int i = 0; i < arr.length; i++) {
			int min=i;
			for (int j = i+1; j < arr.length-1; j++) {
				if(arr[j]<=arr[min]) {
					min=j;
				}
			}
			temp=arr[i];
			arr[i]=arr[min];
			arr[min]=temp;
		}
		for(int i:arr) {
			System.out.println(i);
		}
	

	}

}
