import org.objectweb.asm.*;


public class AddMethodAdapter extends ClassVisitor implements Opcodes {
    private String owner;
    private boolean isInterface;
    private boolean isAnonymous;
    private boolean isSynthetic;


    public AddMethodAdapter(ClassVisitor cv) {
    	super(ASM4, cv);
    }

    private boolean isAnonymous(String name) {
	int loc = name.lastIndexOf('$');

	if (loc == -1) {
	    return false;
	}
	String temp = name.substring(loc + 1);

	try {
	    int num = Integer.parseInt(temp);

	    return true;
	}
	catch (NumberFormatException e) {
	    return false;
	}	
    }


    // private boolean isSynthetic(String superName) {
    // 	//return (superName.indexOf('$') != -1);
    // 	return (superName.indexOf("Enum") != -1);
    // }

    @Override public void visit(int version, int access, String name,
    				String signature, String superName, String[] interfaces) {
    	cv.visit(version, access, name, signature, superName, interfaces);

    	owner = name;
    	isInterface = (access & ACC_INTERFACE) != 0;
	isAnonymous = isAnonymous(name);
	// isSynthetic = isSynthetic(superName);

    }





    @Override public MethodVisitor visitMethod(int access, String name,
    					       String desc, String signature, String[] exceptions) {
    	MethodVisitor mv = cv.visitMethod(access, name, desc, signature,exceptions);

    	if ((access & ACC_DEPRECATED) != 0)
    	    return mv;
    	if ((access & ACC_SYNTHETIC) != 0)
    	    return mv;
    	if ((access & ACC_BRIDGE) != 0)
    	    return mv;
    	if ((access & ACC_ABSTRACT) != 0)
    	    return mv;
    	if ((access & ACC_STRICT) != 0)
    	    return mv;
	

	if (isAnonymous && name.equals("<init>")) {
	    return mv;
	}
	if (isSynthetic) {
	    return mv;
	}


	

    	else if (!isInterface && mv != null ) {
    	    mv = new AddMethodEnterAdapter(access,name,desc,mv,owner);
    	}
    	return mv;
    }

}
