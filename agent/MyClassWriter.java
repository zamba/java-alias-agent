import org.objectweb.asm.*;

public class MyClassWriter extends ClassWriter {
    public MyClassWriter(ClassReader classReader, int flags) {
	super(classReader,flags);
    }


    protected String getCommonSuperClass(String type1,
					 String type2) {
	// ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	if (true) 
	    return "java/lang/Object";

	// Below is a copy of ClassWriter.getCommonSuperClass()
        Class<?> c, d;
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            c = Class.forName(type1.replace('/', '.'), false, classLoader);
            d = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }


}
