import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import java.util.Arrays;
import java.util.LinkedList;


public class AddMethodEnterAdapter extends AdviceAdapter {
    
    private boolean debug = false;
    private boolean visitLocals = true;

    private String met;
    private String des;
    private String klass;
    private int acc;
    private int newObj = 0;

    private String[] currentArr = null;

    int listIndex;
    
    boolean isMetStatic = false;
    boolean problem = false;

    LinkedList<Integer> l = new LinkedList<Integer>();

    protected AddMethodEnterAdapter(int access, String name, String desc,
				 MethodVisitor mv, String owner) {
	super(ASM4, mv, access, name, desc);
	met = name;
	des = desc;
	acc = access;
	klass = owner;
	
	isMetStatic = isStatic(methodAccess);
	if (debug)
	    System.out.println("ASM Instrumenting Method: " + desc + " " + name);
	//System.out.println("ASM Instrumenting Method: " + desc + " " + name);
	problem = klass.equals("org/dacapo/parser/Config$Size") && met.equals("<init>") && false;
	// System.out.println(access + " " + methodAccess + " " + methodDesc);
	//System.out.println(owner + " " + name);
    }

    boolean isStatic(int access) {
	return (access >= 8);
    }

/******************************************************************************/
/* NEW NEWARRAY ANEWARRAY MULTIANEWARRAY                                      */
/******************************************************************************/

    public void visitEnd() {
	if (debug)
	    System.out.println("ASM done instrumenting Method: "+ des + " "  + met );
	if (problem)
	    System.out.println("visitEnd");
	//System.out.println("ASM done instrumenting Method: "+ des + " "  + met );
	mv.visitEnd();
    }

    public void insertThisOrStatic() {
	if (met.equals("<init>")) {
	    push((String)null);
	    push((String)null);
	    return;
	}
	if (debug)
	    System.out.println("ASM insertThisOrStatic: "+ des + " "  + met );
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
	if (debug)
	    System.out.println("ASM insertThisOrStatic: "+ des + " "  + met );
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
	if (problem)
	    System.out.println("visitTypeInsn opcode:" + opcode + " " + type);




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
	    insertNewCode("[L"+type+";");
	    // System.out.println("anewarray " + type);
	}
    }


    
    public void visitMethodInsn(int opcode,
				String owner,
				String name,
				String desc) {
	// if (name.equals("invoke")) {
	//     System.out.println(opcode + " || " +  klass + "." + met + "()" + " calling " + owner + "." + name + desc);
	// }
	if (problem)
	    System.out.println("visitMethodInsn opcode:" + opcode + " "+ owner + " " + name + " " + desc);
	if (debug)
	    System.out.println("ASM visitMet: "+ des + " "  + met );


	    // System.out.println("visitMethodInsn opcode:" + opcode + " "+ owner + " " + name + " " + desc);
	
	// String xy = name;
	// int len = xy.length();
	// if (len == 0) {
	//     System.out.println("xy = 0");
	// }
	// else {
	//     if (xy.charAt(0) == 'L' && xy.charAt(len-1) == ';') {
	// 	System.out.println(xy);	    
	//     }
	// }





	// if (met.equals("<init>") && des.equals("()V")) {
	//     System.out.println("ALIVEeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
	// }
	// if (met.equals("<init>") && des.equals("()V")) {
	//     System.out.println("ALIVEeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
	// }

	// SUPER!?!?
	// System.out.println("visitMethodInsn opcode:" + opcode + " "+ owner + " " + name + " " + desc);
	super.visitMethodInsn(opcode,owner,name,desc);


       // 	if (name.equals("<init>") && newObj > 0) {
       // 	    System.out.println("INIT : " + owner + " " + name + " " + desc);
       // 	}
       // System.out.println("Inside : " + met + " " + owner + " " + name + " " + desc);
	// if (name.equals("<init>") && met.equals("<init>")) {
	//     System.out.println("initfrominit");
	// }
 
	if (name.equals("<init>")  &&
	    currentArr != null &&
	    newObj > 0 &&
	    owner.equals(currentArr[newObj-1])) {

	    // System.out.println(owner + " " + met  +  " " + newObj + " " + currentArr.length);

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
	if (debug)
	    System.out.println("ASM visitMet: "+ des + " "  + met );

    }


    public void visitIntInsn(int opcode,
			     int operand) {
	if (problem)
	    System.out.println("visit int insn:" + opcode + " " + operand);
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
	if (problem)
	    System.out.println("visitmulti opcode:" + desc);
	super.visitMultiANewArrayInsn(desc,dims);
	if (!problem)
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
	//boolean isNewVar = addASTORE(var);

	if (opcode == ASTORE) {

	    boolean isNewVar = addASTORE(var);
	    // result is true if this was the first ASTORE to var

	    dup(); // #1
	    if (isNewVar || true) {
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

      TODO(low prio):
         high prio. if the desc[0] is != L | [, then dont get oldvalue nor value
      TODO(low prio):
         rewrite this method to make it more readable
    */
    public void visitFieldInsn(int opcode,
    			       String owner,
    			       String name,
    			       String desc) {

    	char fc = desc.charAt(0);
	if (name.equals("this$0")) {
	    mv.visitFieldInsn(opcode,owner,name,desc);
	    return;
	}
    	if (opcode == PUTFIELD || opcode == GETFIELD || opcode == PUTSTATIC) {
    	    if (opcode == PUTFIELD) {
    		//stack objref value

    		// value is object or array
    		if (fc == 'L' || fc == '[') {
    		    dup2(); // #1,2 callee value
    		}
    		// value is long or double
    		else if (fc == 'J' || fc == 'D') {
    		    dup2X1();
    		    pop2();
		    dupX2();
    		    push((String)null);
    		}
    		// value is primitive
    		else {
    		    dup2();
    		    pop();
    		    push((String)null);		  
    		}

    		//stack objref value objref (obj/arr/null)

    		if(fc == 'L' || fc == '[') {
    		    dup2();
    		    //stack objref value objref (obj/arr/null) objref (obj/arr/null)
    		    pop();
    		    //stack objref value objref (obj/arr/null) objref
    		    mv.visitFieldInsn(GETFIELD,owner,name,desc);
    		    //stack objref value objref (obj/arr/null) oldvalue
    		}
    		else {
    		    push((String)null);
    		}
    		//stack objref value objref (obj/arr/null) (oldvalue/null)
    		push(owner);                         // #4
    		push(name);                          // #5
    		push(desc);                          // #6
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

    		push(owner);                         // #4
    		push(name);                          // #5
    		push(desc);                          // #6
    		insertThisOrStatic();                // #7-8
    		addThreadAndField(1);                // # 9
    	    }
    	    else if (opcode == GETFIELD) {
    		//objref
    		dup();
    		//objref objref
    	    }
    	}
    	super.visitFieldInsn(opcode,owner,name,desc);
    	//getfield objref -> value
    	if (opcode == GETFIELD) {
    	    //objref value
    	    if (fc == 'L' || fc == '[') {
    		dupX1();
    		//value objref value
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
    	else if (opcode == GETSTATIC) {
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
	if(problem)
	    System.out.println("methodexit" + des);
	if (debug)
	    System.out.println("ASM onMetExit: "+ des + " "  + met );
	if (opcode == ATHROW) {
	    
	    return;
	}


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


	if (l.size() > 0 && false) {
	    mv.visitIntInsn(BIPUSH,l.size());
	    mv.visitTypeInsn(ANEWARRAY,"java/lang/Object"); // #6
	    int index = 0;
	    for (Integer i : l) {
	    	mv.visitInsn(DUP);
	    	mv.visitIntInsn(BIPUSH,index++);
	    	mv.visitVarInsn(ALOAD,i.intValue());
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
	if (debug)
	    System.out.println("ASM onMetExit: "+ des + " "  + met );
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
	if(problem) {
	    System.out.println("methodenter" + des);
	    
	    // Type[] apa = Type.getArgumentTypes(des);
	    // // System.out.println("apa=" + apa);
	    // for (int i = 0; i < apa.length;i++) {
	    // 	System.out.println(apa[i].getInternalName());
	    // }
	    }
	if (debug)
	    System.out.println("ASM onMetEnter: "+ des + " "  + met );

	if (met.equals("<clinit>")) {
	    
	}



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
	insertThisOrStatic();          // #3-4

	// System.out.println("met enter: ("+ acc + ") " + 
	// 		   klass + " " + met + " " + des +
	// 		   " parCounter" + parametersCounter);

	if (debug)
	    System.out.println("ASM onMetEnter: "+ des + " "  + met );

	if (parametersCounter > 0) {
	    parameters = new int[parametersCounter];
	    fillParameters(parameters);

	    // System.out.println(met + " " + des + parametersCounter + " " + Arrays.toString(parameters));

	    // System.out.println(Arrays.toString(parameters));
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

	mv.visitMethodInsn(INVOKESTATIC,"NativeInterface","methodEnter",
			   "(Ljava/lang/String;" +    // method name    #1
			   "Ljava/lang/String;" +     // desc           #2
			   "Ljava/lang/String;" +     // callee static  #3
			   "Ljava/lang/Object;" +     // callee object  #4
			   "[Ljava/lang/Object;" +    // args           #5
			   "Ljava/lang/Thread;)V");   // current thread #6
	if (debug)
	    System.out.println("ASM onMetEnter: "+ des + " "  + met );

    }



    @Override public void visitMaxs(int maxStack, int maxLocals) {
	// Using ClassWriter.COMPUTE_FRAMES, so arguments to visitMaxs
	// is calculated automaticly for the cost of some performance
        //System.out.println("maxs: " + maxStack + " " + maxLocals);

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
