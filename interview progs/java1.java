class A{
	A();
	A(String s){
 }
}

class B extends A{
	String S;
    B(String s) {
		//super(""); ||super(s);
      this.S = s;
   }

B b= new B("Test");
}


int [] arr = {0,1,0,1,1,0,1,0,1,0,1,0}

for(int i =0;i<arr.length-1;i++){
	int temp;
	if(arr[i]>arr[i+1]){
		temp = arr[i+1];
		arr[i+1] = arr[i];
		arr[i] = temp;
	}
}


String s = "ABCDEFADBCDFER";

Map<String, Integer> charCountMap = new HashMap<String,Integer>();
char [] arr = s.split("");
for(int i = 0; i< arr.length;i++){
	if(charCountMap.get(arr[i]==null)){
		charCountMap.put(arr[i],1);
	}else{
		charCountMap.put(arr[i],charCountMap.get(arr[i])+1);
	}
}

class SingletonExample(){
	private static SingletonExample instance = null;
	
	
	private SingletonExample(){
		
	
	}
	
	
	public static SingletonExample getInstance(){
		if(instance==null){
			synchronized(this){
				instance = new SingletonExample();
			}
		}
		return instance;
		
	}


}



String s1= "asd"
String s2= "asd"
s1==s2;//true

s1 = "asd1";

String s1= new String("asd")//heap
String s2= new String("asd")
s1==s2//false

s1.concat("")



Emplyoee {
	name
	id
	DOB

}

List<Emplyoee> elist = new ArrayList<Emplyoee>();//10k
elist.stream().filter(
	a-> {
		(new Date().getYear() - a.getDOB().getYear())>40)	
	}
).collect(Collectors.getList);




