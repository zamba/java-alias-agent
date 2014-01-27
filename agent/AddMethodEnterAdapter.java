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

    private String currentNew = null;

    int listIndex;
    
    boolean isMetStatic = false;

    LinkedList<Integer> l = new LinkedList<Integer>();

    public AddMethodEnterAdapter(int access, String name, String desc,
				 MethodVisitor mv, String owner) {
	super(ASM4, mv, access, name, desc);
	met = name;
	des = desc;
	acc = access;
	klass = owner;

	isMetStatic = isStatic(access);

	//System.out.println("Instrumenting: " + owner + " " + name + " "+ desc + " access: " + access + " is static=" + isMetStatic);
    }

    boolean isStatic(int access) {
	return (access >= 8);
    }
    
/******************************************************************************/
/* Can be used to track created variables?                                    */
/******************************************************************************/




/******************************************************************************/
/* NEW NEWARRAY ANEWARRAY MULTIANEWARRAY                                      */
/******************************************************************************/

//     Object[] currentLocals;

// public void visitFrame(int type,
// 		       int nLocal,
// 		       Object[] local,
// 		       int nStack,
// 		       Object[] stack) {

//     // System.out.println(met + type + " " + nLocal + " " + nStack);
//     // System.out.println("Opcodes.Double=" + Opcodes.DOUBLE);
//     // System.out.println("Opcodes.INTEGER=" + Opcodes.INTEGER);
//     // System.out.println(UNINITIALIZED_THIS);
//     // System.out.println(NULL);   
//     // for (int i = 0; i < nLocal;i++){

//     // 	System.out.println("\t local "+ local[i]); 
//     // }
//     // for (int i = 0; i < nStack;i++){

//     // 	System.out.println("\t stack "+ stack[i]); 
//     // }
//     currentLocals = local;
//     super.visitFrame(type,nLocal,local,nStack,stack);
// }

//     public void visitLabel(Label label) {
// 	if (met.equals("main"))
// 	    super.visitLabel(label);
// 	System.out.println("Visits: "+ label);
//     }



// public void visitTryCatchBlock(Label start,
// 			       Label end,
// 			       Label handler,
// 			       String type) {
//     System.out.println(start + " " + end + " " + handler + " " + type);

//     super.visitTryCatchBlock(start,
// 			     end,
// 			     handler,
// 			     type);
// }

    public void insertThisOrStatic() {
	if (isMetStatic) {
	    mv.visitLdcInsn(klass+"."+met);
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

    public void insertNewCode(String desc) {
	dup(); // #2
	push(desc); // #1
	swap();
	insertThisOrStatic();

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
	if (opcode == NEW) {
	    currentNew = type;
	}
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
	    // Label elseLabel = new Label();
	    // Label endLabel = new Label();

	    // push(var);
	    // super.visitVarInsn(ALOAD,listIndex);
	    // super.visitMethodInsn(INVOKESTATIC,
	    // 			  "LNativeInterface;",
	    // 			  "exists",
	    // 			  "(ILjava/util/LinkedList;)I");


	    // super.visitJumpInsn(IFEQ,elseLabel);

	    // super.visitVarInsn(ALOAD,listIndex);
	    // push(var);
	    // super.visitMethodInsn(INVOKESTATIC,
	    // 			  "Ljava/lang/Integer;",
	    // 			  "valueOf",
	    // 			  "(I)Ljava/lang/Integer;");  
	    // super.visitMethodInsn(INVOKEVIRTUAL,
	    // 			  "Ljava/util/LinkedList;",
	    // 			  "add",
	    // 			  "(Ljava/lang/Object;)Z");
	    // pop();
	    // super.visitJumpInsn(GOTO,endLabel);

	    // super.visitLabel(elseLabel);


	    // super.visitLabel(endLabel);


	    boolean isNewVar = addASTORE(var);
	    // result is true if this was the first ASTORE to var

	    dup(); // #1
	    if (isNewVar || true) {
		push((String)null); //#2
	    }
	    else {
		// PROBLEMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
		// --------------------------------------
		visitVarInsn(ALOAD,var); //#2
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
		insertThisOrStatic();
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
		insertThisOrStatic();
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
	    insertThisOrStatic();

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
	    insertThisOrStatic();

	    
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
@Override
public void visitLocalVariable(String name,
			       String desc,
			       String signature,
			       Label start,
			       Label end,
			       int index) {
    System.out.println("new var" + name + desc + signature);
    super.visitLocalVariable(name,desc,signature,start,end,index);
}



/******************************************************************************/
/* onMethodEnter onMethodExit visitMaxs                                       */
/******************************************************************************/




    private void loadListSize() {
	super.visitVarInsn(ALOAD,listIndex);
	super.visitMethodInsn(INVOKEVIRTUAL,
			      "Ljava/util/LinkedList;",
			      "size",
			      "()I");	
    }

    @Override protected void onMethodExit(int opcode) {
	
	//ref
	
	if (opcode == ATHROW)
	    return;

	if (opcode == ARETURN) {
	    dup();// #1
	}
	else {
	    push((String)null); // #1
	}
	//ref ref
	push(met); // #2 
	push(des); // #3
	// ref ref name desc
	insertThisOrStatic(); // #4,5


	// int counter = 0;
	// if (currentLocals != null) {
	//     for (int i = 0; i < currentLocals.length;i++) {
	// 	System.out.println(currentLocals[i]);
	// 	if 
	// 	counter++;
	//     }
	// }

	// PROBLEMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
	// --------------------------------------

	// Label elser = new Label();

	// Label startLabel = new Label();
	// Label endLabel = new Label();

	// loadListSize();
	// visitJumpInsn(IFLE,elser);

	// loadListSize();
	// mv.visitTypeInsn(ANEWARRAY,"Ljava/lang/Object;");

	// int ind = newLocal(Type.getType("I"));
	// push(0);
	// super.visitVarInsn(ISTORE,ind);



	// super.visitLabel(startLabel);
	// super.visitVarInsn(ILOAD,ind);
	
	// super.visitVarInsn(ALOAD,listIndex);
	// super.visitMethodInsn(INVOKEVIRTUAL,
	// 		      "Ljava/util/LinkedList;",
	// 		      "size",
	// 		      "()I");

	// super.visitJumpInsn(IF_ICMPGE,endLabel);
	// dup();
	// super.visitVarInsn(ILOAD,ind);
	// //elem to be stored in arr at ind
	// super.visitVarInsn(ALOAD,listIndex);
	// super.visitVarInsn(ILOAD,ind);
	// super.visitMethodInsn(INVOKEVIRTUAL,
	// 		      "Ljava/util/LinkedList;",
	// 		      "get",
	// 		      "(I)Ljava/lang/Object;");	
	// visitVarInsn(ALOAD,i.intValue());
	
	// mv.visitInsn(AASTORE);

	// super.visitIincInsn(ind,1);

	// super.visitJumpInsn(GOTO,startLabel);

	// super.visitLabel(endLabel);

	// super.visitLabel(elser);




	if (l.size() > 0 && opcode != ATHROW && false) {
	    mv.visitIntInsn(BIPUSH,l.size());
	    mv.visitTypeInsn(ANEWARRAY,"Ljava/lang/Object;"); // #6
	    int index = 0;
	    for (Integer i : l) {
	    	mv.visitInsn(DUP);
	    	mv.visitIntInsn(BIPUSH,index++);
	    	visitVarInsn(ALOAD,i.intValue());
	    	mv.visitInsn(AASTORE);
	    }
	}
	// //-----------------------------------


	else {
	    push((String)null); // #6
	}

	
	mv.visitMethodInsn(INVOKESTATIC,
			   "java/lang/Thread",
			   "currentThread",
			   "()Ljava/lang/Thread;"); // #7

	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","methodExit",
			   "(Ljava/lang/Object;" +    // returned obj    #1
			   "Ljava/lang/String;" +     // name            #2
			   "Ljava/lang/String;" +     // desc            #3
			   "Ljava/lang/String;" +     // staticcallee    #4
			   "Ljava/lang/Object;" +     // callee          #5
			   "[Ljava/lang/Object;" +    // out of scopes   #6
			   "Ljava/lang/Thread;)V");   // current thread  #7
    }


    public void visitMethodInsn(int opcode,
				String owner,
				String name,
				String desc) {
	if (name.equals("<init>") && owner.equals(currentNew)) {
	    dup(); // #2
	}
	super.visitMethodInsn(opcode,owner,name,desc);


	if (name.equals("<init>") && owner.equals(currentNew)) {
	    push(owner); // #1
	    swap();
	    insertThisOrStatic(); // #3-4
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

	// if (met.equals("<init>")) {
	//     push(klass);//#1
	//     loadThis();//#2
	//     visitInsn(ACONST_NULL);//#3
	//     visitInsn(ACONST_NULL);//#4
	//     mv.visitMethodInsn(INVOKESTATIC,
	//     		       "java/lang/Thread",
	//     		       "currentThread",
	//     		       "()Ljava/lang/Thread;"); //#5


	// mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","newObj",
	// 		   "(Ljava/lang/String;" +    // desc            #1
	// 		   "Ljava/lang/Object;" +     // stored obj      #2
	// 		   "Ljava/lang/String;" +     // caller static   #3
	// 		   "Ljava/lang/Object;" +     // caller object   #4
	// 		   "Ljava/lang/Thread;)V");   // current thread  #5
	// }


	// super.visitTypeInsn(NEW,"Ljava/util/LinkedList;");
	// listIndex = newLocal(Type.getType("Ljava/util/LinkedList;"));
	// dup();
	// super.visitMethodInsn(INVOKESPECIAL,
	// 		      "Ljava/util/LinkedList;",
	// 		      "<init>",
	// 		      "()V");
	// super.visitVarInsn(ASTORE,listIndex);


    	int parametersCounter = countParameters();
    	int[] parameters;

	mv.visitLdcInsn(met);          // #1
	mv.visitLdcInsn(des);          // #2
	insertThisOrStatic();
	//System.out.println("met enter: " + met + " " + des + " parCounter" + parametersCounter);
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
