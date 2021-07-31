package xyz.znix.graphicstest;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class QuestcraftTransformer implements IClassTransformer {
    private final Map<String, Consumer<ClassNode>> mappings = new HashMap<>();

    public QuestcraftTransformer() {
        mappings.put("com.sun.jna.NativeLibrary", this::tweakNativeLibrary);
        mappings.put("org.vivecraft.utils.InputInjector", this::tweakVivecraftInputInjector);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        Consumer<ClassNode> transform = mappings.get(name);
        if (transform == null)
            return basicClass;

        ClassReader cr = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        cr.accept(node, 0);

        transform.accept(node);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // ClassWriter cw = new ClassWriter(0); // Just for debugging
        node.accept(cw);
        return cw.toByteArray();
    }

    private void tweakNativeLibrary(ClassNode node) {
        MethodNode target = null;
        for (MethodNode method : node.methods) {
            if (method.name.equals("getInstance") && method.desc.equals("(Ljava/lang/String;Ljava/util/Map;)Lcom/sun/jna/NativeLibrary;"))
                tweakGetInstance(method);

            // if (method.name.equals("getSymbolAddress"))
            //     tweakGetSymAddr(method);
        }

        // TESTING
        // for (int i = node.methods.size() - 1; i >= 0; i--) {
        //     if (!node.methods.get(i).name.equals("getSymbolAddress"))
        //         node.methods.remove(i);
        // }
    }

    private void tweakGetInstance(MethodNode target) {
        InsnList list = new InsnList();
        String tgt = "xyz/znix/graphicstest/QuestcraftShim";

        // Get the first argument and the NativeClass class
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new LdcInsnNode(Type.getObjectType("com/sun/jna/NativeLibrary")));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, tgt, "hookGetInstance",
                "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false));
        list.add(new TypeInsnNode(Opcodes.CHECKCAST, "com/sun/jna/NativeLibrary"));
        list.add(new VarInsnNode(Opcodes.ASTORE, 2));

        LabelNode continueCode = new LabelNode();
        list.add(new VarInsnNode(Opcodes.ALOAD, 2));
        list.add(new JumpInsnNode(Opcodes.IFNULL, continueCode));
        list.add(new VarInsnNode(Opcodes.ALOAD, 2));
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(continueCode);

        target.instructions.insertBefore(target.instructions.getFirst(), list);
    }

    /*
    // NO LONGER NEEDED:
    // We don't try loading via the default handle anymore
    private void tweakGetSymAddr(MethodNode target) {
        InsnList list = new InsnList();

        // Check if the first argument is <openvr-process> and if so skip the handle check
        // Note that a handle of zero means a regular symbol lookup, so it should work fine
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "com/sun/jna/NativeLibrary", "libraryName", "Ljava/lang/String;"));
        list.add(new LdcInsnNode("<openvr-process>"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false));

        // If it's not equal, jump over all this
        LabelNode continueCode = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, continueCode)); // Pop int and jump if value == 0

        // Call Native.findSymbol without checking the handle
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "com/sun/jna/NativeLibrary", "handle", "J"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sun/jna/Native", "findSymbol", "(JLjava/lang/String;)J", false));
        list.add(new InsnNode(Opcodes.LRETURN));

        // list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, tgt, "hookGetInstance",
        //         "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false));
        // list.add(new TypeInsnNode(Opcodes.CHECKCAST, "com/sun/jna/NativeLibrary"));
        // list.add(new VarInsnNode(Opcodes.ASTORE, 2));

        list.add(continueCode); // End our 'if' from before

        target.instructions.insertBefore(target.instructions.getFirst(), list);
    }
    */

    private void tweakVivecraftInputInjector(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (method.name.equals("checkSupported"))
                tweakCheckSupported(method);
        }

        // TESTING
        // for (int i = node.methods.size() - 1; i >= 0; i--) {
        //     if (!node.methods.get(i).name.equals("checkSupported"))
        //         node.methods.remove(i);
        // }
    }

    private void tweakCheckSupported(MethodNode method) {
        InsnList il = new InsnList();
        il.add(new InsnNode(Opcodes.ICONST_0)); // false
        il.add(new FieldInsnNode(Opcodes.PUTSTATIC, "org/vivecraft/utils/InputInjector", "supported", "Z"));
        il.add(new InsnNode(Opcodes.RETURN));

        method.instructions.clear();
        method.instructions.add(il);
    }
}
