import org.objectweb.asm.*;
import org.objectweb.asm.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;



public class Instrument {

    public static byte[] transform(byte[] b1) {
	// if (true)
	//     return b1;
	ClassReader cr = new ClassReader(b1);
	ClassWriter cw = new MyClassWriter(cr,ClassWriter.COMPUTE_MAXS);
	AddMethodAdapter ca = new AddMethodAdapter(cw);



	//ClassReader.SKIP_FRAMES;
	cr.accept(ca, ClassReader.EXPAND_FRAMES);
	byte[] b2 = cw.toByteArray();
	return b2;
    }

}






