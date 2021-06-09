package searchAlgorithms;

public class BinarySearch {

	public static void main(String[] args) {
		int[] arr=new int[10];
		arr[0]=2;
		arr[1]=4;
		arr[2]=5;
		arr[3]=6;
		arr[4]=7;
		arr[5]=9;
		arr[6]=10;
		arr[7]=13;
		arr[8]=15;
		arr[9]=16;

		int index=Search(arr,15);
		System.out.println(index);
	}

	private static int Search(int[] arr, int ele) {
		int low=0;
		int high=arr.length-1;
		int mid=0;
		while(low<=high) {
			mid=(low+high)/2;
			if(arr[mid]==ele) {
				return mid;
			}else if(arr[mid]>ele) {
				high=mid-1;
			}else {
				low=mid+1;
			}
		}
		return -1;
	}

}
