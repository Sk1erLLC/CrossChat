package club.sk1er.crosschat.tweaker;

import club.sk1er.crosschat.CrossChat;
import club.sk1er.crosschat.asm.MinecraftTransformer;
import club.sk1er.crosschat.tweaker.transformer.Transformer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class ClassTransformer implements IClassTransformer {

    private final Logger LOGGER = LogManager.getLogger(CrossChat.class.getName());
    private final Multimap<String, Transformer> transformerMap = ArrayListMultimap.create();

    public ClassTransformer() {
        registerTransformer(new MinecraftTransformer());
    }

    private void registerTransformer(Transformer transformer) {
        Arrays.stream(transformer.getClassName()).forEach(cls -> transformerMap.put(cls, transformer));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;

        Collection<Transformer> transformers = transformerMap.get(transformedName);
        if (transformers.isEmpty()) return bytes;

        LOGGER.info("Found {} transformers for {}", transformers.size(), transformedName);

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);

        transformers.forEach(transformer -> {
            LOGGER.info("Applying transformer {} on {}...", transformer.getClass().getName(), transformedName);
            transformer.transform(node, transformedName);
        });

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        try {
            node.accept(writer);
        } catch (Throwable t) {
            LOGGER.error("Exception when transforming " + transformedName + " : " + t.getClass().getSimpleName());
            t.printStackTrace();
        }

        try {
            FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Jack\\Desktop\\projects\\Sk1erLLC\\CrossChat\\bytecode\\Bytecode.class");
            outputStream.write(writer.toByteArray());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toByteArray();
    }
}