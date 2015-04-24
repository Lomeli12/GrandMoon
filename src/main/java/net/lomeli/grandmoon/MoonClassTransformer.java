package net.lomeli.grandmoon;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class MoonClassTransformer implements IClassTransformer {
    private final String[] renderGlobalNames = new String[]{"net.minecraft.client.renderer.RenderGlobal", "ckn"};
    private final String[] renderSkyNames = new String[]{"renderSky", "func_174976_a", "a"};
    private final String renderSkyDesc = "(FI)V";
    private final String[] moonPngNames = new String[]{"locationMoonPhasesPng", "field_110927_h", "c"};
    private final String[] tessDrawName = new String[]{"draw", "func_78381_a", "b"};
    private final String tessDrawDesc = "()I";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (matchesName(name, renderGlobalNames) || matchesName(transformedName, renderGlobalNames))
            return patchRenderGlobal(bytes);
        return bytes;
    }

    private byte[] patchRenderGlobal(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        Logger.logInfo("Found Render Global Class: " + classNode.name);
        Logger.logWarning("If the game crashes after this, it's probably because another mod is trying to edit " + classNode.name + " in a way that conflicts with this!");

        MethodNode renderSky = null;

        for (MethodNode node : classNode.methods) {
            if (matchesName(node.name, renderSkyNames) && node.desc.equals(renderSkyDesc)) {
                renderSky = node;
                break;
            }
        }

        if (renderSky != null) {
            Logger.logInfo("Found render sky!");
            boolean foundMoonPhase = false, exit = false;
            for (int i = 0; i < renderSky.instructions.size(); i++) {
                AbstractInsnNode abstractNode = renderSky.instructions.get(i);
                if (abstractNode instanceof FieldInsnNode) {
                    FieldInsnNode fieldNode = (FieldInsnNode) abstractNode;
                    if (matchesName(fieldNode.name, moonPngNames)) {
                        Logger.logInfo("Found beginning of moon rendering! Inserting size hook");
                        InsnList insertList = new InsnList();
                        insertList.add(new FieldInsnNode(GETSTATIC, "net/lomeli/grandmoon/MoonClientHooks", "INSTANCE", "Lnet/lomeli/grandmoon/MoonClientHooks;"));
                        insertList.add(new MethodInsnNode(INVOKEVIRTUAL, "net/lomeli/grandmoon/MoonClientHooks", "moonSizeHook", "()V", false));
                        renderSky.instructions.insertBefore(fieldNode, insertList);
                        foundMoonPhase = true;
                        i += 2;
                    }
                } else if (abstractNode instanceof MethodInsnNode) {
                    MethodInsnNode methodNode = (MethodInsnNode) abstractNode;
                    if (matchesName(methodNode.name, tessDrawName) && methodNode.desc.equals(tessDrawDesc) && foundMoonPhase) {
                        Logger.logInfo("Found tessellator.draw() for moon rendering! Inserting reset size hook!");
                        InsnList insertList = new InsnList();
                        insertList.add(new FieldInsnNode(GETSTATIC, "net/lomeli/grandmoon/MoonClientHooks", "INSTANCE", "Lnet/lomeli/grandmoon/MoonClientHooks;"));
                        insertList.add(new MethodInsnNode(INVOKEVIRTUAL, "net/lomeli/grandmoon/MoonClientHooks", "moonSizeReset", "()V", false));
                        renderSky.instructions.insert(methodNode, insertList);
                        i += 2;
                        exit = true;
                    }
                }

                if (exit)
                    break;
            }
        }

        Logger.logInfo("Writing changes to class!");
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private boolean matchesName(String name, String[] potential) {
        for (String mcp : potential) {
            if (mcp.equals(name))
                return true;
        }
        return false;
    }
}
