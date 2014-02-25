import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import java.util.Arrays;
import java.util.LinkedList;


public class AddMethodEnterAdapter extends AdviceAdapter {

    private String met;
    private String des;
    private String klass;

    boolean isMetStatic = false;

    private int newObj = 0;
    private String[] currentArr = null;

    boolean methodEnter = false;
    boolean methodExit = false;

    boolean storeVar = false;
    boolean newObjs = true;

    boolean fieldUse = true;


    boolean disableAll = false;

    protected AddMethodEnterAdapter(int access, String name, String desc,
				 MethodVisitor mv, String owner) {
	super(ASM4, mv, access, name, desc);
	met = name;
	des = desc;
	klass = owner;	
	isMetStatic = isStatic(methodAccess);
    }

    boolean isStatic(int access) {
    	return (access & ACC_STATIC) != 0;
    }

/******************************************************************************/
/* NEW NEWARRAY ANEWARRAY MULTIANEWARRAY                                      */
/******************************************************************************/


    public void insertThisOrStatic() {
	if (met.equals("<init>")) {
	    push((String)null);
	    push((String)null);
	    return;
	}

	if (isMetStatic) {
	    mv.visitLdcInsn(klass);
	}
	else {
	    push((String)null);
	}

	if (isMetStatic) {
	    push((String)null);
	}
	else {
	    loadThis();
	}
    }

    private void insertThreadAndNew() {

	    mv.visitMethodInsn(INVOKESTATIC,
	    		       "java/lang/Thread",
	    		       "currentThread",
	    		       "()Ljava/lang/Thread;");
	    mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","newObj",
			   "(Ljava/lang/String;" +
			   "Ljava/lang/Object;" + 
			   "Ljava/lang/String;" +
			   "Ljava/lang/Object;" + 
			   "Ljava/lang/Thread;)V");
    }

    public void insertNewCode(String desc) {

	dup();
	push(desc);
	swap();
	insertThisOrStatic();
	insertThreadAndNew();
    }





    public void visitTypeInsn(int opcode,
			      String type) {
	if (!newObjs || disableAll) {
	    super.visitTypeInsn(opcode,type);
	    return;
	}
	if (opcode == NEW) {
	    newObj++;
	    if (currentArr != null) {
		currentArr = Arrays.copyOf(currentArr,currentArr.length+1);
		currentArr[currentArr.length-1] = type;
	    }
	    else {
		currentArr = new String[1];
		currentArr[0] = type;
	    }
	}
	super.visitTypeInsn(opcode,type);
	if (opcode == NEW) {
	    dup();
	}

	if (opcode == ANEWARRAY) {
	    insertNewCode(type);
	}
    }


    
    public void visitMethodInsn(int opcode,
				String owner,
				String name,
				String desc) {
	if (!newObjs || disableAll) {
	    super.visitMethodInsn(opcode,owner,name,desc);
	    return;
	}

	super.visitMethodInsn(opcode,owner,name,desc);
 
	if (name.equals("<init>")  &&
	    currentArr != null &&
	    newObj > 0 &&
	    owner.equals(currentArr[newObj-1])) {

	    newObj--;
	    if (currentArr.length == 1) {
		currentArr = null;
	    }
	    else {
		currentArr = Arrays.copyOf(currentArr,currentArr.length-1);
	    }
	    push(owner);
	    swap();
	    insertThisOrStatic();
	    insertThreadAndNew();
	}
    }


    public void visitIntInsn(int opcode,
			     int operand) {
	if (!newObjs || disableAll) {
	    super.visitIntInsn(opcode,operand);
	    return;
	}

	super.visitIntInsn(opcode,operand);
	if (opcode == NEWARRAY) {
	    String result = null;
	    if (operand == T_BOOLEAN)
		result = "[Z";
	    if (operand == T_CHAR)
		result = "[C";
	    if (operand == T_FLOAT)
		result = "[F";
	    if (operand == T_DOUBLE)
		result = "[D";
	    if (operand == T_BYTE)
		result = "[B";
	    if (operand == T_SHORT)
		result = "[S";
	    if (operand == T_INT)
		result = "[I";
	    if (operand == T_LONG)
		result = "[J";

	    insertNewCode(result);
	}
    }


    public void visitMultiANewArrayInsn(String desc,
					int dims) {
	if (!newObjs || disableAll) {
	    super.visitMultiANewArrayInsn(desc,dims);
	    return;
	}
	super.visitMultiANewArrayInsn(desc,dims);
	insertNewCode(desc);
    }


/******************************************************************************/
/* store/load var                                                             */
/******************************************************************************/



    /*
      Possible OPCODES:
      ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, RET

      Inserting methodcalls to callbacks when OPCODE == ALOAD/ASTORE

     */
    @Override public void visitVarInsn(int opcode,
				       int var) {
	if (!storeVar || disableAll) {
	    super.visitVarInsn(opcode,var);
	    return;
	}
	if (opcode == ASTORE) {
	    dup(); // #1
	    if (true) {
		push((String)null); //#2
	    }
	    else {
		// PROBLEMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
		// --------------------------------------
		mv.visitVarInsn(ALOAD,var); //#2
		// --------------------------------------
	    }

	    push(met); // #3
	    push(des); // #4
	    insertThisOrStatic(); // #5,6
	    mv.visitMethodInsn(INVOKESTATIC,
			       "java/lang/Thread",
			       "currentThread",
			       "()Ljava/lang/Thread;"); // #7

	    mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","storeVar",
			       "(Ljava/lang/Object;" +    // stored obj      #1
			       "Ljava/lang/Object;" +	 // old value        #2		       
			       "Ljava/lang/String;" +     // method name     #3
			       "Ljava/lang/String;" +     // method desc     #4
			       "Ljava/lang/String;" +     // callee static   #5
			       "Ljava/lang/Object;" +     // callee obj      #6
			       "Ljava/lang/Thread;)V");   // current thread  #7
	}
    	super.visitVarInsn(opcode,var);
    }



/******************************************************************************/
/* store/load field                                                           */
/******************************************************************************/


    public void addThreadAndField(int n) {
	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); //9
	if (n == 1) {
	    mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","storeField",
			       "(Ljava/lang/Object;" +    // desc            #1
			       "Ljava/lang/Object;" +     // stored obj      #2
			       "Ljava/lang/Object;" +     // old value       #3
			       "Ljava/lang/String;" +     // caller static   #4
			       "Ljava/lang/String;" +     // caller static   #5
			       "Ljava/lang/String;" +     // caller static   #6
			       "Ljava/lang/String;" +     // caller object   #7
			       "Ljava/lang/Object;" +     // caller object   #8
			       "Ljava/lang/Thread;)V");   // current thread  #9
	}
	else if (n == 0) {
	    mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","loadField",
			       "(Ljava/lang/Object;" +    // desc            #1
			       "Ljava/lang/Object;" +     // stored obj      #2
			       "Ljava/lang/String;" +     // caller static   #3
			       "Ljava/lang/String;" +     // caller static   #4
			       "Ljava/lang/String;" +     // caller static   #5
			       "Ljava/lang/String;" +     // caller object   #6
			       "Ljava/lang/Object;" +     // caller object   #7
			       "Ljava/lang/Thread;)V");   // current thread  #8
	}
    }

    /*
      Inserting methodcalls to callbacks
      GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD
    */
    public void visitFieldInsn(int opcode,
    			       String owner,
    			       String name,
    			       String desc) {
	if (!fieldUse || disableAll) {
	    super.visitFieldInsn(opcode,owner,name,desc);
	    return;
	}

    	char fc = desc.charAt(0);
	if (name.equals("this$0")) {
	    super.visitFieldInsn(opcode,owner,name,desc);
	    return;
	}

	if (opcode == PUTFIELD) {
	    //stack: objref value |

	    // value is object or array
	    if (fc == 'L' || fc == '[') {
		dup2();
	    //stack: objref value | objref value
	    }
	    // value is long or double
	    else if (fc == 'J' || fc == 'D') {
		dup2X1();
		//stack: value | objref value
		pop2();
		//stack: value | objref
		dupX2();
		//stack: objref value | objref
		push((String)null);
		//stack: objref value | objref null		
	    }
	    // value is primitive
	    else {
		dup2();
		//stack: objref value | objref value
		pop();
		//stack: objref value | objref	
		push((String)null);
		//stack: objref value | objref null
	    }


	    //stack: objref value | objref (obj/arr/null)
	    // if (name.equals("this$0")) {
	    // 	push((String)null);    
	    // }
	    // else
		if(fc == 'L' || fc == '[') {
	    	dup2();
	    	//stack objref value | objref (obj/arr/null) objref (obj/arr/null)
	    	pop();
	    	//stack objref value | objref (obj/arr/null) objref
	    	mv.visitFieldInsn(GETFIELD,owner,name,desc);
	    	//stack objref value | objref (obj/arr/null) oldvalue
	    }
	    else {
	    	push((String)null);
	    }
	    //stack: objref value | objref (obj/arr/null) (oldvalue/null)
	    push(owner);
	    push(name);
	    push(desc);
	    insertThisOrStatic();
	    addThreadAndField(1);
	}


	else if (opcode == PUTSTATIC) {
	    if (fc == 'L' || fc == '[') {
		dup(); // #1,2 callee value
	    }
	    else {
		push((String)null);
	    }
	    push((String)null);
	    swap();

	    if(fc == 'L' || fc == '[') {
		mv.visitFieldInsn(GETSTATIC,owner,name,desc);
	    }
	    else {
		push((String)null);
	    }

	    push(owner);
	    push(name);
	    push(desc);
	    insertThisOrStatic();
	    addThreadAndField(1);
	}


	else if (opcode == GETFIELD) {
	    //objref |
	    dup();
	    //objref | objref
	}



    	super.visitFieldInsn(opcode,owner,name,desc);



    	//getfield objref -> value
    	if (opcode == GETFIELD) {
    	    //objref value
    	    if (fc == 'L' || fc == '[') {
    		dupX1();
    		//value | objref value
    	    }
    	    // value is long or double
    	    else if (fc == 'J' || fc == 'D') {
    		dup2X1();
    		//value objref value
    		pop2();
		
    		//value objref
    		push((String)null);
    		//value objref null
    	    }
    	    // value is primitive
    	    else {
    		dupX1();
    		pop();
    		push((String) null);
    		//value objref null
    	    }
    	    push(owner);
    	    push(name);
    	    push(desc);
    	    insertThisOrStatic();
    	    addThreadAndField(0);
	    
    	}



    	else if (opcode == GETSTATIC ) {
    	    push((String)null);
    	    if (fc == 'L' || fc == '[') {
    		dup();
    	    }
    	    else {
    		push((String)null);	
    	    }
    	    push(owner);
    	    push(name);
    	    push(desc);
    	    insertThisOrStatic();
    	    addThreadAndField(0);
    	}
    }


/******************************************************************************/
/* onMethodEnter onMethodExit visitMaxs                                       */
/******************************************************************************/


    @Override protected void onMethodExit(int opcode) {
	// System.out.println("ASM onMetExit: "+ des + " "  + met );
	if (!methodExit || disableAll) {
	    return;
	}
	
	if (opcode == ATHROW) {
	    
	    return;
	}


	if (opcode == ARETURN) {
	    dup();
	}
	else {
	    push((String)null);
	}
	//ref ref
	push(met);
	push(des);
	insertThisOrStatic();

	// todo make a new array with out of scopes
	push((String)null);
	
	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;");

	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","methodExit",
			   "(Ljava/lang/Object;" +    // returned obj    #1
			   "Ljava/lang/String;" +     // name            #2
			   "Ljava/lang/String;" +     // desc            #3
			   "Ljava/lang/String;" +     // staticcallee    #4
			   "Ljava/lang/Object;" +     // callee          #5
			   "[Ljava/lang/Object;" +    // out of scopes   #6
			   "Ljava/lang/Thread;)V");   // current thread  #7
    }



    /*
      Adds bytecode containing methods calls to native callback with:
        * Method name/Description 
	* Callee(String) if static met
        * Callee(currentthis) if non static met
	* An object array containing all parameters that are objects/arrays.
	* The current thread
     */
    @Override protected void onMethodEnter() {
	if (!methodEnter || disableAll) {
	    return;
	}


    	int parametersCounter = countParameters();
    	int[] parameters;

	mv.visitLdcInsn(met);          // #1
	mv.visitLdcInsn(des);          // #2
	insertThisOrStatic();          // #3-4


	if (parametersCounter > 0) {
	    parameters = new int[parametersCounter];
	    fillParameters(parameters);
	    // if (met.equals("<init>") && klass.equals("org/sunflow/core/LightServer$1")) {
	    // 	System.out.println(met + " " + des);
	    // 	System.out.println(parametersCounter + " " + Arrays.toString(parameters));
	    // }


	    mv.visitIntInsn(BIPUSH,parameters.length);
	    mv.visitTypeInsn(ANEWARRAY,"java/lang/Object"); // #5

	    for (int i = 0; i < parameters.length;i++) {
		mv.visitInsn(DUP);
		mv.visitIntInsn(BIPUSH,i);
		if (isMetStatic) {
		    mv.visitVarInsn(ALOAD,parameters[i]-1);
		}
		else {
		    mv.visitVarInsn(ALOAD,parameters[i]);
		}
		mv.visitInsn(AASTORE);
	    }
	}
	else {
	    mv.visitInsn(ACONST_NULL);     // 5
	}

	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); //6

	mv.visitMethodInsn(INVOKESTATIC,
			   "NativeInterface",
			   "methodEnter",
			   "(Ljava/lang/String;" +    // method name    #1
			   "Ljava/lang/String;" +     // desc           #2
			   "Ljava/lang/String;" +     // callee static  #3
			   "Ljava/lang/Object;" +     // callee object  #4
			   "[Ljava/lang/Object;" +    // args           #5
			   "Ljava/lang/Thread;)V");   // current thread #6

    }



    @Override public void visitMaxs(int maxStack, int maxLocals) {
	super.visitMaxs(0,0);
    }



/******************************************************************************/
/* Trivial workers                                                            */
/******************************************************************************/

    // Count parameters that are objects/arrays
    private int countParameters() {
	if (des == null) {
	    return 0;
	}
    	int parameterCounter = 0;
    	int i = 1;
    	char current;
    	while ((current = des.charAt(i)) != ')') {
    	    if (current == '[') {
    		if (des.charAt(i+1) != 'L') {
    		    i = i + 2;
    		}
    		else {
    		    while (des.charAt(i+1) != ';') {
    			i++;
    		    }
    		    i++;
    		}
    		parameterCounter++;
    	    }
    	    else if (current == 'L') {
    		while (des.charAt(i) != ';') {
    		    i++;
    		}
    		i++;
    		parameterCounter++;
    	    }

    	    else {
    		i++;
    	    }
    	}
    	return parameterCounter;
    }

    // Fill arr with slot numbers that corresponds to where objects/arrays are
    // located.
    private void fillParameters(int[]arr) {
	if (des == null) 
	    return;
    	int i = 1;
    	int arrayindex = 0;
    	int currentslot = 1;
    	char current;
    	while ((current = des.charAt(i)) != ')') {
    	    if (current == '[') {
    		arr[arrayindex++] = currentslot++;
    		if (des.charAt(i+1) != 'L') {
    		    i = i+2;
    		}
    		else {
    		    while (des.charAt(i) != ';') {
    			i++;
    		    }
    		    i++;
    		}
    	    }
    	    else if (current == 'L') {
    		arr[arrayindex++] = currentslot++;
    		while (des.charAt(i) != ';') {
    		    i++;
    		}
    		i++;
		
    	    }
    	    else if (current == 'D' || current == 'J') {
    		currentslot+=2;
    	        i++;
    	    }
    	    else {
    		currentslot++;
    		i++;
    	    }
    	}
	
    }

}
