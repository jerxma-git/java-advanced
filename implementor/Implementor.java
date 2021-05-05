package info.kgeorgiy.ja.zheromskii.implementor;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Implementor implements Impler {
    public static void main(final String[] args) {
        if (args == null || args.length < 1) {
            System.out.println("Usage: Implementor fully_qualified_name");
        }
        // :NOTE: ??
    }

    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        final int mod = token.getModifiers();
        if (!Modifier.isInterface(mod)) {
            throw new ImplerException(token.getCanonicalName() + " is not an interface");
        }
        
        if (Modifier.isPrivate(mod)) {
            throw new ImplerException(token.getCanonicalName() + " is private");
        }
        
        // :NOTE: Упростить
       
        final Path outputFilePath = root
            .resolve(token.getPackageName().replace(".", File.separator))
            .resolve(token.getSimpleName() + "Impl.java");
        try {
            Path parent = outputFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (final IOException e) {
            System.err.println("Couldn't create directories");
            return;
        }

        // // :NOTE: String/join
        String code = String.format("package %s; class %s implements %s {%s}", 
            token.getPackageName(), 
            token.getSimpleName() + "Impl", 
            token.getCanonicalName(),
            Arrays.stream(token.getMethods()).map(Implementor::convertMethod).collect(Collectors.joining("")));

        // :NOTE: Упростить
        try (final BufferedWriter writer = Files.newBufferedWriter(outputFilePath)) {
            try {
                writer.write(code);
            } catch (final IOException e) {
                System.err.println("I/O error in writing to out file");
            }
        } catch (final IOException e) {
            System.err.println("I/O error in creating output file");
        }
    }

    // :NOTE: Модиифкаторы?
    private static String convertMethod(final Method m) {      
        final String parameters = Arrays.stream(m.getParameters())
                 .map(p -> p.getType().getCanonicalName() + " " + p.getName())
                 .collect(Collectors.joining(", "));
        return String.format(
                "%s %s %s(%s) { return %s;}",
                Modifier.toString(m.getModifiers() & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT),
                m.getReturnType().getCanonicalName(),
                m.getName(),
                parameters,
                defaultVal(m.getReturnType())
        );
    }

    private static String defaultVal(final Class<?> c) {
        if (c == boolean.class) {
            return "false";
        }
        if (c == void.class) {
            return "";
        }
        if (Object.class.isAssignableFrom(c)) {
            return "null";
        }
        return "0";
    }




}
