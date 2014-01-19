import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import java.util.Arrays;
import java.util.LinkedList;


public class AddMethodEnterAdapter extends AdviceAdapter {
    
    private boolean debug = true;
    private boolean visitLocals = true;

    private String met;
    private String des;
    private String klass;
    private int acc;
    private boolean newobj = false;

    LinkedList<Integer> l = new LinkedList<Integer>();

    public AddMethodEnterAdapter(int access, String name, String desc,
				 MethodVisitor mv, String owner) {
	super(ASM4, mv, access, name, desc);
	met = name;
	des = desc;
	acc = access;
	klass = owner;
    }


/******************************************************************************/
/* Can be used to track created variables?                                    */
/******************************************************************************/





/******************************************************************************/
/* NEW NEWARRAY ANEWARRAY MULTIANEWARRAY                                      */
/******************************************************************************/




    public void insertNewCode(String desc) {
	dup(); // #2
	push(desc); // #1
	swap();
	if (acc == H_INVOKEINTERFACE) {
	    mv.visitLdcInsn(klass+"."+met);    // #3
	}
	else {
	    visitInsn(ACONST_NULL);    // #3
	}
	if (acc != H_INVOKEINTERFACE) {
	    mv.visitVarInsn(ALOAD,0);  // #4
	}
	else {
	    visitInsn(ACONST_NULL);    // #4
	}

	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); // #5

	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","newObj",
			   "(Ljava/lang/String;" +    // desc            #1
			   "Ljava/lang/Object;" +     // stored obj      #2
			   "Ljava/lang/String;" +     // caller static   #3
			   "Ljava/lang/Object;" +     // caller object   #4
			   "Ljava/lang/Thread;)V");   // current thread  #5
	
    }



    public void visitTypeInsn(int opcode,
			      String type) {
	mv.visitTypeInsn(opcode,type);
	if (opcode == ANEWARRAY) {
	    insertNewCode("[L"+type+";");
	}
    }

    public void visitIntInsn(int opcode,
			     int operand) {
	mv.visitIntInsn(opcode,operand);
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
	mv.visitMultiANewArrayInsn(desc,dims);
    
	insertNewCode(desc);
    
    }


/******************************************************************************/
/* store/load var                                                             */
/******************************************************************************/
    
    private boolean addASTORE(int var) {
	boolean exist = false;
	for (Integer i : l) {
	    if (i.intValue() == var) {
		exist = true;
	    }
	}
	if (!exist) {
	    l.add(var);
	}
	return !exist;
    }


    /*
      Possible OPCODES:
      ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, RET

      Inserting methodcalls to callbacks when OPCODE == ALOAD/ASTORE

     */
    @Override public void visitVarInsn(int opcode,
				       int var) {
	if (opcode == ASTORE) {
	    boolean isNewVar = addASTORE(var);
	    // result is true if this was the first ASTORE to var

	    dup(); // #1
	    if (isNewVar) {
		push((String)null);
	    }
	    else {
		visitVarInsn(ALOAD,var);
	    }

	    push(met); // #2
	    push(des); // #3
	    if (acc == H_INVOKEINTERFACE || acc == ACC_STATIC) {
		push(klass+"."+met);             // #4
	    }
	    else {
		push((String)null);	         // #4
	    }
	    if (acc != H_INVOKEINTERFACE && acc != ACC_STATIC && acc != 10) {
		loadThis();                      // #5
	    }
	    else {
		push((String)null);              // #5
	    }
	    mv.visitMethodInsn(INVOKESTATIC,
			       "java/lang/Thread",
			       "currentThread",
			       "()Ljava/lang/Thread;"); // #6

	    mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","storeVar",
			       "(Ljava/lang/Object;" +    // stored obj      #1
			       "Ljava/lang/Object;" +	 // old value		       
			       "Ljava/lang/String;" +     // method name     #2
			       "Ljava/lang/String;" +     // method desc     #3
			       "Ljava/lang/String;" +     // callee static   #4
			       "Ljava/lang/Object;" +     // callee obj      #5
			       "Ljava/lang/Thread;)V");   // current thread  #6
	}
    	super.visitVarInsn(opcode,var);
    }



/******************************************************************************/
/* store/load field                                                           */
/******************************************************************************/

    /*
      Inserting methodcalls to callbacks
      GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD

      TODO(low prio):
         high prio. if the desc[0] is != L | [, then dont get oldvalue nor value
      TODO(low prio):
         rewrite this method to make it more readable
    */
    public void visitFieldInsn(int opcode,
			       String owner,
			       String name,
			       String desc) {
	if (opcode == PUTFIELD || opcode == GETFIELD || opcode == PUTSTATIC) {
	    if (opcode == PUTFIELD) {
		char c = desc.charAt(0);
		//objref value
		dup2(); //                           // #1 #2
		//objref value objref value
		
		if (c != 'L' && c != '[') {
		    
		    pop();
		    push((String)null);
		    //objref value objref null objref
		}

		dup2();
		//objref value objref value objref value
		
		pop();
		//objref value objref value objref
		
		
		if(c == 'L' || c == '[') {

		    super.visitFieldInsn(GETFIELD,owner,name,desc);
		}
		else {

		    pop();
		    push((String)null);
		}

		//objref value objref value value

		push(owner);                         // #4
		push(name);                          // #5
		push(desc);                          // #6
		if (acc == H_INVOKEINTERFACE) {
		    push(klass+"."+met);             // #7
		    
		}
		else {
		    push((String)null);	             // #7
		}
		if (acc != H_INVOKEINTERFACE) {
		    loadThis();                      // #8
		}
		else {
		    push((String)null);              // #8
		}
		mv.visitMethodInsn(INVOKESTATIC,
				   "java/lang/Thread",
				   "currentThread",
				   "()Ljava/lang/Thread;"); //9

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
	    else if (opcode == PUTSTATIC) {
		
		// value
		dup();                                        // #1
		// value value
		push((String)null);                           // #2
		// value value null
		swap();
		// value null value
		char cd = desc.charAt(0);
		if (cd != 'L' && cd != '[') {
		    pop();
		    push((String)null);
		}


		char c = desc.charAt(0);
		if(c == 'L' || c == '[') {
		    super.visitFieldInsn(GETSTATIC,owner,name,desc);
		}
		else {
		    push((String)null);
		}

		
		// value null value value
		push(owner);                         // #4
		push(name);                          // #5
		push(desc);                          // #6
		// value null value value owner name desc
		if (acc == H_INVOKEINTERFACE || acc == ACC_STATIC) {
		    
		    push(klass+"."+met);             // #7
		}
		else {
		    push((String)null);	             // #7
		}
		// value null value value owner name desc (null or klass.met)
		if (acc != H_INVOKEINTERFACE && acc != ACC_STATIC) {
		    //loadThis();                      // #8
		    mv.visitVarInsn(ALOAD,0);
		    //push((String)null);
		}
		else {
		    push((String)null);              // #8
		}
		// value null value value owner name desc (null or klass.met) (null or currentthis)

		mv.visitMethodInsn(INVOKESTATIC,
				   "java/lang/Thread",
				   "currentThread",
				   "()Ljava/lang/Thread;"); //9

		mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","storeField",
				   "(Ljava/lang/Object;" +    // objref          #1
				   "Ljava/lang/Object;" +     // stored obj      #2
				   "Ljava/lang/Object;" +     // old value       #3
				   "Ljava/lang/String;" +     // caller static   #4
				   "Ljava/lang/String;" +     // caller static   #5
				   "Ljava/lang/String;" +     // caller static   #6
				   "Ljava/lang/String;" +     // caller object   #7
				   "Ljava/lang/Object;" +     // caller object   #8
				   "Ljava/lang/Thread;)V");   // current thread  #9
		
		
	    }
	    else if (opcode == GETFIELD) {
		//objref
		dup(); 
		//objref objref
	    }
	    
	}

	super.visitFieldInsn(opcode,owner,name,desc);

	if (opcode == GETFIELD) {
	    //objref value
	    dupX1();
	    //value objref value
	    push(owner);
	    push(name);
	    push(desc);
	    if (acc == H_INVOKEINTERFACE) {
		push(klass+"."+met);
	    }
	    else {
		push((String)null);
	    }
	    if (acc != H_INVOKEINTERFACE && acc != 10) {
		loadThis();
	    }
	    else {
		push((String)null);
	    }

	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); //7

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
	else if (opcode == GETSTATIC) {
	    dup();
	    push((String)null);
	    swap();
	    //value | null value
	    push(owner);
	    push(name);
	    push(desc);
	    if (acc == H_INVOKEINTERFACE) {
		push(klass+"."+met);
	    }
	    else {
		push((String)null);
	    }
	    if (acc != H_INVOKEINTERFACE && acc != 10) {
		loadThis();
	    }
	    else {
		push((String)null);
	    }

	    
	    mv.visitMethodInsn(INVOKESTATIC,
			       "java/lang/Thread",
			       "currentThread",
			       "()Ljava/lang/Thread;"); //8

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



/******************************************************************************/
/* onMethodEnter onMethodExit visitMaxs                                       */
/******************************************************************************/

    @Override protected void onMethodExit(int opcode) {


	//ref
	if (opcode == ASTORE) {
	    dup();//#1
	}
	else {
	    push((String)null); //2
	}
	//ref ref
	push(met); //2 
	push(des); //3
	// ref ref name desc
	if (acc == H_INVOKEINTERFACE) {
	    push(klass+"."+met); //4 
	}
	else {
	    push((String)null); //4
	}
	// ref ref name desc stccaller

	if (acc != H_INVOKEINTERFACE && acc != ACC_STATIC && acc != 10) {
	    //System.out.println("a: " + acc);
	    loadThis(); //5
	}
	else {
	    push((String)null); //5
	}
	
	if (l.size() > 0) {
	    mv.visitIntInsn(BIPUSH,l.size());
	    mv.visitTypeInsn(ANEWARRAY,"Ljava/lang/Object;");
	    int index = 0;
	    for (Integer i : l) {
		mv.visitInsn(DUP);
		mv.visitIntInsn(BIPUSH,index++);
		visitVarInsn(ALOAD,i.intValue());
		mv.visitInsn(AASTORE);
	    }
	}
	else {
	   push((String)null); 
	}
	
	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); //8

	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","methodExit",
			   "(Ljava/lang/Object;" +    // returned obj    #1
			   "Ljava/lang/String;" +     // name            #2
			   "Ljava/lang/String;" +     // desc            #3
			   "Ljava/lang/String;" +     // staticcallee    #4
			   "Ljava/lang/Object;" +     // callee          #5
			   "[Ljava/lang/Object;" +    // out of scopes
			   "Ljava/lang/Thread;)V");   // current thread  #6
    }

    /*
      Adds bytecode containing methods calls to native callback with:
        * Method name/Description 
	* Callee(String) if static met
        * Callee(currentthis) if non static met
	* An object array containing all parameters that are objects/arrays.
	* The current thread

	*****TODO CLINIT??*****
     */
    @Override protected void onMethodEnter() {
	if (met.equals("<clinit>")) {
	    return;
	}
	if (met.equals("<init>")) {
	    push(klass);//#1
	    loadThis();//#2
	    visitInsn(ACONST_NULL);//#3
	    visitInsn(ACONST_NULL);//#4
	    mv.visitMethodInsn(INVOKESTATIC,
	    		       "java/lang/Thread",
	    		       "currentThread",
	    		       "()Ljava/lang/Thread;"); //#5


	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","newObj",
			   "(Ljava/lang/String;" +    // desc            #1
			   "Ljava/lang/Object;" +     // stored obj      #2
			   "Ljava/lang/String;" +     // caller static   #3
			   "Ljava/lang/Object;" +     // caller object   #4
			   "Ljava/lang/Thread;)V");   // current thread  #5
	}



    	int parametersCounter = countParameters();
    	int[] parameters;

	mv.visitLdcInsn(met);          // #1
	mv.visitLdcInsn(des);          // #2
	if (acc == H_INVOKEINTERFACE) {
	    mv.visitLdcInsn(klass);    // #3
	}
	else {
	    visitInsn(ACONST_NULL);    // #3
	}

	if (acc != H_INVOKEINTERFACE) {
	    mv.visitVarInsn(ALOAD,0);  // #5
	}
	else {
	    visitInsn(ACONST_NULL);    // #5
	}
	if (parametersCounter > 0) {
	    parameters = new int[parametersCounter];
	    fillParameters(parameters);
	    
	    mv.visitIntInsn(BIPUSH,parameters.length);
	    mv.visitTypeInsn(ANEWARRAY,"Ljava/lang/Object;"); // #6
	    for (int i = 0; i < parameters.length;i++) {
		mv.visitInsn(DUP);
		mv.visitIntInsn(BIPUSH,i);
		if (acc == H_INVOKEINTERFACE) {
		    mv.visitVarInsn(ALOAD,parameters[i]-1);
		}
		else {
		    mv.visitVarInsn(ALOAD,parameters[i]);
		}
		mv.visitInsn(AASTORE);
	    }
	}
	else {
	    visitInsn(ACONST_NULL);     // 6
	}

	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); //7

	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","methodEnter",
			   "(Ljava/lang/String;" +    // method name    #1
			   "Ljava/lang/String;" +     // desc           #2
			   "Ljava/lang/String;" +     // callee static  #3
			   "Ljava/lang/Object;" +     // callee object  #5
			   "[Ljava/lang/Object;" +    // args           #6
			   "Ljava/lang/Thread;)V");   // current thread #7
    }



    @Override public void visitMaxs(int maxStack, int maxLocals) {
	// Using ClassWriter.COMPUTE_FRAMES, so arguments to visitMaxs
	// is calculated automaticly for the cost of some performance
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
