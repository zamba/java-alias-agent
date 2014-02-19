public enum Color {
    WHITE(21), BLACK(22), RED(23), YELLOW(24), BLUE(25);
 
    private int code;
 
    private Color(int c) {
	code = c;
	System.out.println("Inside enum constructor. Code="+code);
    }
 
    public int getCode() {
	return code;
    }
}
