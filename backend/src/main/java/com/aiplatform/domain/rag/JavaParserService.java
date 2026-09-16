package com.aiplatform.domain.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Java Parser Service - Parses Java source files to extract:
 * - Package declarations
 * - Import statements
 * - Class/Interface definitions
 * - Method signatures
 * - Dependencies
 * - Caller relationships
 * 
 * Uses regex-based parsing for simplicity. In production, consider using
 * JavaParser or Eclipse JDT for more accurate AST parsing.
 */
@Slf4j
@Service
public class JavaParserService {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.*]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "(?:public|protected|private)?\\s*(?:abstract|final)?\\s*(?:class|interface|enum|record)\\s+(\\w+)",
            Pattern.MULTILINE);
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "(?:public|protected|private)\\s+(?:static\\s+)?(?:final\\s+)?(?:synchronized\\s+)?(?:abstract\\s+)?" +
            "(?:<[^>]+>\\s+)?(\\w+(?:<[^>]+>)?(?:\\[\\])?)\\s+(\\w+)\\s*\\(([^)]*)\\)",
            Pattern.MULTILINE);
    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "(?:private|protected|public)\\s+(?:static\\s+)?(?:final\\s+)?(\\w+(?:<[^>]+>)?(?:\\[\\])?)\\s+(\\w+)\\s*[;=]",
            Pattern.MULTILINE);
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile(
            "(\\w+)\\.(\\w+)\\s*\\(");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile(
            "@(\\w+)(?:\\(([^)]*)\\))?");

    public ParsedJavaFile parse(String content, String filePath) {
        String packageName = extractPackage(content);
        List<String> imports = extractImports(content);
        List<JavaClass> classes = extractClasses(content, imports);

        return new ParsedJavaFile(
                filePath,
                packageName,
                imports,
                classes,
                content
        );
    }

    private String extractPackage(String content) {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private List<String> extractImports(String content) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            imports.add(matcher.group(1));
        }
        return imports;
    }

    private List<JavaClass> extractClasses(String content, List<String> imports) {
        List<JavaClass> classes = new ArrayList<>();
        Matcher classMatcher = CLASS_PATTERN.matcher(content);

        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            int classStart = classMatcher.start();
            int classEnd = findBlockEnd(content, classStart);

            String classContent = content.substring(classStart, Math.min(classEnd, content.length()));

            List<JavaMethod> methods = extractMethods(classContent, className);
            List<String> fields = extractFields(classContent);
            List<String> dependencies = extractDependencies(classContent, imports);
            List<String> annotations = extractAnnotations(classContent);

            int lineStart = countLines(content, 0, classStart);
            int lineEnd = countLines(content, 0, classEnd);

            classes.add(new JavaClass(
                    className,
                    methods,
                    fields,
                    dependencies,
                    annotations,
                    lineStart,
                    lineEnd
            ));
        }

        return classes;
    }

    private List<JavaMethod> extractMethods(String classContent, String className) {
        List<JavaMethod> methods = new ArrayList<>();
        Matcher methodMatcher = METHOD_PATTERN.matcher(classContent);

        while (methodMatcher.find()) {
            String returnType = methodMatcher.group(1);
            String methodName = methodMatcher.group(2);
            String parameters = methodMatcher.group(3);

            // Skip constructors (no return type match or same as class name)
            if (returnType.equals(className)) continue;

            int methodStart = methodMatcher.start();
            int methodEnd = findMethodEnd(classContent, methodStart);

            String methodBody = classContent.substring(methodStart, Math.min(methodEnd, classContent.length()));
            List<String> callers = extractCallers(methodBody);

            int lineStart = countLines(classContent, 0, methodStart);
            int lineEnd = countLines(classContent, 0, methodEnd);

            String signature = returnType + " " + methodName + "(" + parameters + ")";

            methods.add(new JavaMethod(
                    methodName,
                    signature,
                    returnType,
                    parameters,
                    callers,
                    lineStart,
                    lineEnd
            ));
        }

        return methods;
    }

    private List<String> extractFields(String classContent) {
        List<String> fields = new ArrayList<>();
        Matcher fieldMatcher = FIELD_PATTERN.matcher(classContent);
        while (fieldMatcher.find()) {
            fields.add(fieldMatcher.group(1) + " " + fieldMatcher.group(2));
        }
        return fields;
    }

    private List<String> extractDependencies(String classContent, List<String> imports) {
        Set<String> dependencies = new HashSet<>();

        // Extract from field types
        Matcher fieldMatcher = FIELD_PATTERN.matcher(classContent);
        while (fieldMatcher.find()) {
            String type = fieldMatcher.group(1);
            String baseType = extractBaseType(type);
            if (!isPrimitive(baseType) && !baseType.equals("String")) {
                dependencies.add(baseType);
            }
        }

        // Extract from method parameters and return types
        Matcher methodMatcher = METHOD_PATTERN.matcher(classContent);
        while (methodMatcher.find()) {
            String returnType = extractBaseType(methodMatcher.group(1));
            if (!isPrimitive(returnType) && !returnType.equals("String") && !returnType.equals("void")) {
                dependencies.add(returnType);
            }

            String params = methodMatcher.group(3);
            for (String param : params.split(",")) {
                param = param.trim();
                if (!param.isEmpty()) {
                    String[] parts = param.split("\\s+");
                    if (parts.length >= 2) {
                        String type = extractBaseType(parts[0]);
                        if (!isPrimitive(type) && !type.equals("String")) {
                            dependencies.add(type);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(dependencies);
    }

    private List<String> extractCallers(String methodBody) {
        Set<String> callers = new HashSet<>();
        Matcher callMatcher = METHOD_CALL_PATTERN.matcher(methodBody);
        while (callMatcher.find()) {
            String caller = callMatcher.group(2);
            if (!caller.isEmpty() && !caller.equals("this") && !caller.equals("super")) {
                callers.add(caller);
            }
        }
        return new ArrayList<>(callers);
    }

    private List<String> extractAnnotations(String content) {
        List<String> annotations = new ArrayList<>();
        Matcher annotationMatcher = ANNOTATION_PATTERN.matcher(content);
        while (annotationMatcher.find()) {
            annotations.add(annotationMatcher.group(1));
        }
        return annotations;
    }

    private int findBlockEnd(String content, int start) {
        int braceCount = 0;
        boolean inBlock = false;

        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceCount++;
                inBlock = true;
            } else if (c == '}') {
                braceCount--;
                if (inBlock && braceCount == 0) {
                    return i + 1;
                }
            }
        }

        return content.length();
    }

    private int findMethodEnd(String content, int start) {
        int braceCount = 0;
        boolean inBlock = false;

        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceCount++;
                inBlock = true;
            } else if (c == '}') {
                braceCount--;
                if (inBlock && braceCount == 0) {
                    return i + 1;
                }
            } else if (c == ';' && !inBlock) {
                // Abstract method or interface method
                return i + 1;
            }
        }

        return content.length();
    }

    private int countLines(String content, int from, int to) {
        int lines = 0;
        for (int i = from; i < Math.min(to, content.length()); i++) {
            if (content.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private String extractBaseType(String type) {
        // Remove generics
        int genericStart = type.indexOf('<');
        if (genericStart > 0) {
            type = type.substring(0, genericStart);
        }
        // Remove array brackets
        type = type.replace("[]", "");
        // Get simple name
        int lastDot = type.lastIndexOf('.');
        if (lastDot > 0) {
            type = type.substring(lastDot + 1);
        }
        return type.trim();
    }

    private boolean isPrimitive(String type) {
        return Set.of("int", "long", "short", "byte", "float", "double", "boolean", "char", "void")
                .contains(type);
    }

    // Record types for parsed data
    public record ParsedJavaFile(
            String filePath,
            String packageName,
            List<String> imports,
            List<JavaClass> classes,
            String content
    ) {}

    public record JavaClass(
            String name,
            List<JavaMethod> methods,
            List<String> fields,
            List<String> dependencies,
            List<String> annotations,
            int lineStart,
            int lineEnd
    ) {}

    public record JavaMethod(
            String name,
            String signature,
            String returnType,
            String parameters,
            List<String> callers,
            int lineStart,
            int lineEnd
    ) {}
}
