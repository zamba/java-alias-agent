import org.objectweb.asm.*;


public class AddMethodAdapter extends ClassVisitor implements Opcodes {
    private String owner;
    private boolean isInterface;
    private boolean isAnonymous;
    private int ver;
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


    private boolean isSynthetic(String superName) {
	//return (superName.indexOf('$') != -1);
	return (superName.indexOf("Enum") != -1);
    }

    @Override public void visit(int version, int access, String name,
    				String signature, String superName, String[] interfaces) {
    	cv.visit(version, access, name, signature, superName, interfaces);
	//System.out.println(name); class name
	this.ver = version;
	// System.out.println(version);
    	owner = name;
    	isInterface = (access & ACC_INTERFACE) != 0;
	// if (name.equals("org/eclipse/equinox/internal/provisional/configurator/Configurator"))
	// System.out.println("java " + name + " " + Integer.toBinaryString(access));
	isAnonymous = isAnonymous(name);
	isSynthetic = isSynthetic(superName);
	// System.out.println("\n\nASM Instrumenting Class: " + signature + " " + name);
    }

    int getVersion() {
	return ver;
    }




    @Override public MethodVisitor visitMethod(int access, String name,
    					       String desc, String signature, String[] exceptions) {
    	MethodVisitor mv = cv.visitMethod(access, name, desc, signature,exceptions);


	
	// if (owner.equals("org/eclipse/equinox/internal/simpleconfigurator/SimpleConfiguratorImpl")) {
	// 	if (name.equals("<init>") ||
	// 	    name.equals("<clinit>") ||
	// 	    name.equals("getConfigurationURL") ||
	// 	    name.equals("applyConfiguration") ||
	// 	    name.equals("isExclusiveInstallation") ||
	// 	    name.equals("getUrlInUse") 
		    
	// 	    ) {
	// 	    return mv;
	// 	}

	// }


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
