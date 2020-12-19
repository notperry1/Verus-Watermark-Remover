package de.xbrowniecodez.verus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class WatermarkRemover {

    /**
     * @author brownie
     * @throws Exception
     * @time: 9 Nov 2020 23:17:09
     */
    public static String watermark = null;

    public static void main(String args[]) throws Throwable {
        Logger.info("");
        Logger.info("Verus Watermark Remover v0.1 by xBrownieCodez");
        Logger.info("");
        Logger.info("Loading file " + args[0]);
        Logger.info("");
        process(new File(args[0]));

    }

    public static void process(File input) throws Throwable {
        ZipFile zipFile = new ZipFile(input);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipOutputStream out = new ZipOutputStream(
                new FileOutputStream(input.toString().replace(".jar", "-Output.jar")));

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                try (InputStream in = zipFile.getInputStream(entry)) {
                    ClassReader cr = new ClassReader(in);
                    ClassNode classNode = new ClassNode();
                    cr.accept(classNode, 0);
                    if (classNode.name.equals("me/levansj01/verus/VerusPlugin")) {
                        for (MethodNode methodNode : classNode.methods) {
                            if (methodNode.signature != null && !methodNode.signature.contains("bukkit")
                                    && methodNode.signature.contains("@")) {
                                watermark = methodNode.signature;
                                
                            }

                        }
                        if(watermark == null) {
                           Logger.error("No watermark found, aborting...");
                           return;
                        }
                    }
                    
                    for (MethodNode methodNode : classNode.methods) {
                        if (methodNode.signature != null && methodNode.signature.equals(watermark)) {
                            Logger.info("Found watermark " + watermark);
                            Logger.info("Attempting to remove watermark in " + classNode.name + "." + methodNode.name
                                    + methodNode.desc);   
                            methodNode.signature = null;
                            Logger.info("Watermark successfully removed");
                            Logger.info("");
                        }
                    }
                    ClassWriter cw = new ClassWriter(0);
                    classNode.accept(cw);
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    newEntry.setTime(System.currentTimeMillis());
                    out.putNextEntry(newEntry);
                    writeToFile(out, new ByteArrayInputStream(cw.toByteArray()));
                    in.close();

                }
            } else {
                entry.setTime(System.currentTimeMillis());
                out.putNextEntry(entry);
                writeToFile(out, zipFile.getInputStream(entry));
            }


        }
        Logger.info("Done! Output: " + input.getAbsolutePath().toString().replace(".jar", "-Output.jar"));
        zipFile.close();
        out.close();

    }

    private static void writeToFile(ZipOutputStream outputStream, InputStream inputStream) throws Throwable {
        byte[] buffer = new byte[4096];
        try {
            while (inputStream.available() > 0) {
                int data = inputStream.read(buffer);
                outputStream.write(buffer, 0, data);
            }
        } finally {
            inputStream.close();
            outputStream.closeEntry();
        }
    }

}
