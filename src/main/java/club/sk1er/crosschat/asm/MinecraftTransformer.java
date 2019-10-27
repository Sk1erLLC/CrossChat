package club.sk1er.crosschat.asm;

import club.sk1er.crosschat.tweaker.transformer.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.ListIterator;

public class MinecraftTransformer implements Transformer {

    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        classNode.methods.forEach(methodNode -> {
            String methodName = mapMethodName(classNode, methodNode);
            if (methodName.equals("displayGuiScreen") || methodName.equals("func_147108_a")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node instanceof FieldInsnNode) {
                        if (node.getOpcode() == Opcodes.GETFIELD && node.getNext().getOpcode() == Opcodes.INVOKEVIRTUAL && node.getNext().getNext().getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.remove(node.getNext());
                            methodNode.instructions.remove(node.getNext());
                            methodNode.instructions.remove(node);
                        }
                    }
                }
            }
        });
    }
}
