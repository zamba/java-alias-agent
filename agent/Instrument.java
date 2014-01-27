import org.objectweb.asm.*;


public class Instrument {

    public static byte[] transform(byte[] b1) {
	ClassReader cr = new ClassReader(b1);
	ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
	AddMethodAdapter ca = new AddMethodAdapter(cw);
	cr.accept(ca, 8);
	byte[] b2 = cw.toByteArray();
	return b2;
    }

}






