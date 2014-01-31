import org.objectweb.asm.*;


class AddMethodAdapter extends ClassVisitor implements Opcodes {
    private String owner;
    private boolean isInterface;
    public AddMethodAdapter(ClassVisitor cv) {
    	super(ASM4, cv);
    }
    @Override public void visit(int version, int access, String name,
    				String signature, String superName, String[] interfaces) {
    	cv.visit(version, access, name, signature, superName, interfaces);
	//System.out.println(name); class name
    	owner = name;
    	isInterface = (access & ACC_INTERFACE) != 0;
    }
    @Override public MethodVisitor visitMethod(int access, String name,
    					       String desc, String signature, String[] exceptions) {
    	MethodVisitor mv = cv.visitMethod(access, name, desc, signature,exceptions);
	if (access >= ACC_DEPRECATED)
	    return mv;
    	if (!isInterface && mv != null ) {
    	    mv = new AddMethodEnterAdapter(access,name,desc,mv,owner);
    	}
    	return mv;
    }

}
