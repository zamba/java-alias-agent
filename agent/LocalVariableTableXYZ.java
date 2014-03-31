public class LocalVariableTableXYZ {
    private int[] arr = new int[10];
    private int index = 0;
    private int ownIndex;

    public LocalVariableTableXYZ(int index) {
	for (int i = 0;i<arr.length;i++) {
	    arr[i] = -1;
	}
	ownIndex = index;
    }
    public void add(int n) {
	if (n == ownIndex) {
	    System.err.println("Attempting to store to own slot");
	    System.exit(0);
	    return;
	}
	else if (exist(n) != -1) {
	    return;
	}
	else {
	    arr[index] = n;
	    if (index == (arr.length - 1)) {
		int[] temp = new int[arr.length*2];
		for (int i = 0;i < arr.length;i++) {
		    temp[i] = arr[i];
		}
		for (int i = arr.length; i < arr.length*2;i++) {
		    temp[i] = -1;
		}
		arr = temp;
		index++;
	    }
	    else {
		index++;
	    }
	}
    }
    
    public int exist(int n) {
	for (int i = 0; i<arr.length;i++) {
	    if (arr[i] == n) {
		return n;
	    }
	}
	return -1;
    }

    public void remove(int n) {
	for (int i = 0; i<arr.length;i++) {
	    if (arr[i] == n) {
		arr[i] = -1;
	    }
	}
    }


    public int[] getLocalVariables() {
	return arr;
    }
    
}

