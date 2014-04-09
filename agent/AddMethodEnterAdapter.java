import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import java.util.Arrays;


public class AddMethodEnterAdapter extends AdviceAdapter {

    private String met;
    private String des;
    private String klass;
    
    private boolean isMetStatic = false;

    private int newObj = 0;
    private String[] currentArr = null;
    
    private boolean jsr = false;

    private boolean methodEnter = true;
    private boolean methodExit = true;
    private boolean storeVar = true;
    private boolean newObjs = true;
    private boolean fieldUse = true;

    private boolean disableAll = false;

    private int varStatus = 0;
    private int varStatus2 = 0;
    private int localIndex;

    private int maxParID = 0;

    protected AddMethodEnterAdapter(int access, String name, String desc,
				 MethodVisitor mv, String owner) {
	super(ASM4, mv, access, name, desc);
	met = name;
	des = desc;
	klass = owner;	
	isMetStatic = isStatic(methodAccess);
	getMaxParameterSlot();
	
    }

    private boolean isStatic(int access) {
    	return (access & ACC_STATIC) != 0;
    }

    private void getMaxParameterSlot() {
	if (des == null)
	    return;

	int max = 0;
	if (!isMetStatic) 
	    max = 1;

	int counter = 1;
	char current;

	while ((current = des.charAt(counter)) != ')') {
	    switch (current) {
	    case '[':
		max++;
		while (current == '[') {
		    counter++;
		    current = des.charAt(counter);
		}
		if (current == 'L') {
		    while (current != ';') {
			counter++;
			current = des.charAt(counter);
		    }
		}
		break;
	    case 'L':
		max++;
		while (current != ';') {
		    counter++;
		    current = des.charAt(counter);
		}
		break;
		
		
	    default:
		max++;
		if (current == 'J' || current == 'D') {
		    max++;
		}
		break;
	    }
	    counter++;
	}

	maxParID = max;
    }

/******************************************************************************/
/* NEW NEWARRAY ANEWARRAY MULTIANEWARRAY                                      */
/******************************************************************************/


    public void insertThisOrStatic() {
	// if (met.equals("<init>")) {
	//     push((String)null);
	//     push((String)null);
	//     return;
	// }

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





    @Override public void visitTypeInsn(int opcode,
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


    
    @Override public void visitMethodInsn(int opcode,
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


    @Override public void visitIntInsn(int opcode,
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


    @Override public void visitMultiANewArrayInsn(String desc,
					int dims) {
	if (!newObjs || disableAll) {
	    super.visitMultiANewArrayInsn(desc,dims);
	    return;
	}
	super.visitMultiANewArrayInsn(desc,dims);
	insertNewCode(desc);
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
			       // "Ljava/lang/String;" +     // desc   #6
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
			       // "Ljava/lang/String;" +     // DESC   #5
			       "Ljava/lang/String;" +     // caller object   #6
			       "Ljava/lang/Object;" +     // caller object   #7
			       "Ljava/lang/Thread;)V");   // current thread  #8
	}
    }

    /*
      Inserting methodcalls to callbacks
      GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD
    */
    @Override public void visitFieldInsn(int opcode,
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
	    // push(desc);
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
	    // push(desc);
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
    	    // push(desc);
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
    	    // push(desc);
    	    insertThisOrStatic();
    	    addThreadAndField(0);
    	}
    }

/******************************************************************************/
/* store/load var                                                             */
/******************************************************************************/
    
    @Override public void visitJumpInsn(int opcode,
			      Label label) {
	if (opcode == JSR) {
	    jsr = true;
	}
	super.visitJumpInsn(opcode,label);
    }

    /*
      Possible OPCODES:
      ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, RET

      Inserting methodcalls to callbacks when OPCODE == ALOAD/ASTORE

     */



    private void pushLocal() {
	mv.visitVarInsn(ALOAD,localIndex);
	mv.visitMethodInsn(INVOKEVIRTUAL,
			   "LocalVariableTableXYZ",
			   "getLocalVariables",
			   "()[I");
    }

    private void setLocal(int var) {
	int slot = var < localIndex ? var : var+1;
	mv.visitVarInsn(ALOAD,localIndex);
	push(slot);
	mv.visitMethodInsn(INVOKEVIRTUAL,
			   "LocalVariableTableXYZ",
			   "add",
			   "(I)V");
    }

    private void removeLocal(int var) {
	int slot = var < localIndex ? var : var+1;
	mv.visitVarInsn(ALOAD,localIndex);
	push(slot);
	mv.visitMethodInsn(INVOKEVIRTUAL,
			   "LocalVariableTableXYZ",
			   "remove",
			   "(I)V");
    }


    private void pushOldOrNull(int var) {
	Label elseLabel = new Label();
	Label endLabel = new Label();
	int slot = var < localIndex ? var : var + 1;

	mv.visitVarInsn(ALOAD,localIndex);
	push(slot);
	mv.visitMethodInsn(INVOKEVIRTUAL,
			   "LocalVariableTableXYZ",
			   "exist",
			   "(I)I");

	push(-1);
	// mv.visitInsn(ICONST_M1);

	mv.visitJumpInsn(IF_ICMPEQ,elseLabel);
	mv.visitVarInsn(ALOAD,slot); 
	// mv.visitInsn(ACONST_NULL);
	mv.visitJumpInsn(GOTO,endLabel);
	mv.visitLabel(elseLabel);
	//mv.visitInsn(ACONST_NULL);
	push((String) null);
	mv.visitLabel(endLabel);
	
	// push((String) null);
    }


    @Override public void visitVarInsn(int opcode,
				       int var) {
	if (!storeVar || disableAll) {
	    super.visitVarInsn(opcode,var);
	    return;
	}

	if (opcode == DSTORE || opcode == LSTORE) {
	    removeLocal(var);
	    removeLocal(var+1);
	}
	else if (opcode == ISTORE || opcode == FSTORE) {
	    removeLocal(var);
	}

	if (opcode == ASTORE) {
	    if (jsr) {
		jsr = false;
		removeLocal(var);
		super.visitVarInsn(opcode,var);
		return;
	    }
	    dup(); // #1
	    int slot = var < localIndex ? var : var + 1;
	    mv.visitVarInsn(ALOAD,localIndex);
	    push(slot);
	    mv.visitMethodInsn(INVOKEVIRTUAL,
			       "LocalVariableTableXYZ",
			       "exist",
			       "(I)I"); // #2

	    insertThisOrStatic(); // #3-4
	    mv.visitMethodInsn(INVOKESTATIC,
	    		       "java/lang/Thread",
	    		       "currentThread",
	    		       "()Ljava/lang/Thread;"); // #5
	    mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","storeVar",
	    		       "(Ljava/lang/Object;" +    // stored obj      #1
			       // "Ljava/lang/Object;" +     // old obj         #2
			       "I" +
	    		       "Ljava/lang/String;" +     // callee static   #3
	    		       "Ljava/lang/Object;" +     // callee obj      #4
	    		       "Ljava/lang/Thread;)V");   // current thread  #5



	}
    	super.visitVarInsn(opcode,var);

	// Set bit indicating that local variable var holds an obj.
	if (opcode==ASTORE) {
	    setLocal(var);
	}
    }

/******************************************************************************/
/* onMethodEnter onMethodExit visitMaxs                                       */
/******************************************************************************/

    private int bzz = 0;
    /*
      TODO:
      
      SIDE EFFECTS
        - Adds a local variable to the visited method, and stores the index in the private
	  field "localIndex".
	- Makes a new LocalVariableTable and stores it to localIndex.

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
	
  
	//localIndex = newLocal(Type.INT_TYPE);
	localIndex = newLocal(Type.getObjectType("LocalVariableTableXYZ")); 
	mv.visitTypeInsn(NEW,"LocalVariableTableXYZ");
	dup();
	push(localIndex);
	mv.visitMethodInsn(INVOKESPECIAL,
			   "LocalVariableTableXYZ",
			   "<init>",
			   "(I)V");


	mv.visitVarInsn(ASTORE,localIndex);

	
    	int parametersCounter = countParameters();
	int[] parameters = null;

	mv.visitLdcInsn(met);          // #1
	insertThisOrStatic();          // #2-3

	if (parametersCounter > 0) {
	    parameters = new int[parametersCounter];
	    fillParameters(parameters);
	    mv.visitIntInsn(BIPUSH,parameters.length);
	    mv.visitTypeInsn(ANEWARRAY,"java/lang/Object"); // #4

	    for (int i = 0; i < parameters.length;i++) {
		mv.visitInsn(DUP);
		mv.visitIntInsn(BIPUSH,i);
		mv.visitVarInsn(ALOAD,parameters[i]);
		mv.visitInsn(AASTORE);
	    }
	    for (int i = 0; i < parameters.length;i++) {
		setLocal(parameters[i]);
	    }
	}
	else {
	    mv.visitInsn(ACONST_NULL);     // #4
	}
	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); // #5
	mv.visitMethodInsn(INVOKESTATIC,
			   "NativeInterface",
			   "methodEnter",
			   "(Ljava/lang/String;" +    // method name    #1
			   "Ljava/lang/String;" +     // callee static  #2
			   "Ljava/lang/Object;" +     // callee object  #3
			   "[Ljava/lang/Object;" +    // args           #4
			   "Ljava/lang/Thread;)V");   // current thread #5
    }


    @Override protected void onMethodExit(int opcode) {
	if (!methodExit || disableAll) {
	    return;
	}
	if (opcode == ARETURN) {
	    dup();
	}
	else {
	    push((String)null);
	}
	push(met);
	insertThisOrStatic();
	pushLocal();
	// push((String) null);
	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;");
	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","methodExit",
			   "(Ljava/lang/Object;" +    // returned obj    #1
			   "Ljava/lang/String;" +     // name            #2
			   "Ljava/lang/String;" +     // staticcallee    #4
			   "Ljava/lang/Object;" +     // callee          #5
			   "[I" +
			   "Ljava/lang/Thread;)V");   // current thread  #8
    }


    @Override public void visitMaxs(int maxStack, int maxLocals) {
	super.visitMaxs(0,0);
    }



/******************************************************************************/
/* Trivial workers                                                            */
/******************************************************************************/

    // Counts parameters that are objects/arrays
    private int countParameters() {
	if (des == null) {
	    return 0;
	}
    	int parameterCounter = 0;
    	int i = 1;
	char current = des.charAt(i);
    	while (current != ')') {
    	    if (current == '[') {
		parameterCounter++;
		while (current == '[') {
		    current = des.charAt(++i);
		}
    		if (current != 'L') {
    		    current = des.charAt(++i);
		}
    		else {
    		    while (current != ';') {
    			current = des.charAt(++i);
    		    }
    		    current = des.charAt(++i);
    		}
    	    }
    	    else if (current == 'L') {
		parameterCounter++;
    		while (current != ';') {
    		    current = des.charAt(++i);
    		}
    		current = des.charAt(++i);
    	    }
    	    else {
    		current = des.charAt(++i);
    	    }
    	}
    	return parameterCounter;
    }


    // Fills arr with numbers that corresponds to local variable 
    // indices where objects/arrays are stored.
    private void fillParameters(int[]arr) {
	if (des == null) 
	    return;
    	int i = 1;
    	int arrayindex = 0;
    	int currentslot = 1;
	if (isMetStatic) {
	    currentslot = 0;
	}
    	char current = des.charAt(i);
    	while (current != ')') {
    	    if (current == '[') {
    		arr[arrayindex++] = currentslot++;
		while (current == '[') {
		    current = des.charAt(++i);
		}
    		if (current != 'L') {
    		    current = des.charAt(++i);
    		}
    		else {
    		    while (current != ';') {
    			current = des.charAt(++i);
    		    }
    		    current = des.charAt(++i);
    		}
    	    }
    	    else if (current == 'L') {
    		arr[arrayindex++] = currentslot++;
    		while (current != ';') {
    		    current = des.charAt(++i);
    		}
    		current = des.charAt(++i);
    	    }
    	    else if (current == 'D' || current == 'J') {
    		currentslot+=2;
    	        current = des.charAt(++i);
    	    }
    	    else {
    		currentslot++;
    		current = des.charAt(++i);
    	    }
    	}
	bzz = currentslot;
    }
}
