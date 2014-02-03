import org.objectweb.asm.*;
import org.objectweb.asm.util.*;


public class Instrument {

    public static byte[] transform(byte[] b1) {
	ClassReader cr = new ClassReader(b1);
	ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
	AddMethodAdapter ca = new AddMethodAdapter(cw);

	// AddMethodAdapter ca = new AddMethodAdapter(new CheckClassAdapter(cw));

	
	// StringWriter sw = new StringWriter();
	// PrintWriter pw = new PrintWriter(sw);
	// CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), false, pw);
	// assertTrue(sw.toString(), sw.toString().length()==0);


	cr.accept(ca, ClassReader.EXPAND_FRAMES);
	byte[] b2 = cw.toByteArray();
	return b2;
    }

}






