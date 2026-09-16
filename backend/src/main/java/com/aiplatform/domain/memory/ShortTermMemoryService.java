package com.aiplatform.domain.memory;

import com.aiplatform.domain.rag.JavaParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Short-term Memory Service - Manages .ai/ directory with markdown files.
 * These files provide AI with quick project understanding without reading entire codebase.
 * 
 * Structure:
 * .ai/
 * ├── overview.md          - Project overview
 * ├── architecture.md      - Architecture description
 * ├── modules.md           - Module breakdown
 * ├── dependencies.md      - Dependency graph
 * ├── conventions.md       - Coding conventions
 * ├── stack.md             - Technology stack
 * ├── structure.md         - File structure
 * ├── classes.md           - Class inventory
 * └── runtime/             - Runtime memory
 *     ├── current-task.md
 *     ├── current-plan.md
 *     ├── discoveries.md
 *     ├── decisions.md
 *     ├── touched-files.md
 *     ├── test-results.md
 *     └── task-summary.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortTermMemoryService {

    private final JavaParserService javaParserService;

    public void initializeAiDirectory(String workspacePath, UUID projectId) {
        Path aiDir = Path.of(workspacePath, ".ai");
        try {
            Files.createDirectories(aiDir);
            Files.createDirectories(aiDir.resolve("runtime"));
            Files.createDirectories(aiDir.resolve("knowledge"));
            Files.createDirectories(aiDir.resolve("codebase"));
            log.info("Initialized .ai directory at: {}", aiDir);
        } catch (IOException e) {
            log.error("Failed to create .ai directory", e);
        }
    }

    public void generateProjectOverview(String workspacePath, UUID projectId) {
        Path aiDir = Path.of(workspacePath, ".ai");
        Path overviewFile = aiDir.resolve("overview.md");

        StringBuilder content = new StringBuilder();
        content.append("# Project Overview\n\n");
        content.append("**Generated:** ").append(Instant.now()).append("\n");
        content.append("**Project ID:** ").append(projectId).append("\n\n");

        // Count files
        long javaFiles = countFiles(workspacePath, "*.java");
        long xmlFiles = countFiles(workspacePath, "*.xml");
        long ymlFiles = countFiles(workspacePath, "*.yml") + countFiles(workspacePath, "*.yaml");
        long propertiesFiles = countFiles(workspacePath, "*.properties");

        content.append("## Statistics\n\n");
        content.append("- Java Files: ").append(javaFiles).append("\n");
        content.append("- XML Files: ").append(xmlFiles).append("\n");
        content.append("- YAML Files: ").append(ymlFiles).append("\n");
        content.append("- Properties Files: ").append(propertiesFiles).append("\n\n");

        // Detect project type
        content.append("## Project Type\n\n");
        if (Files.exists(Path.of(workspacePath, "pom.xml"))) {
            content.append("- **Build System:** Maven\n");
        } else if (Files.exists(Path.of(workspacePath, "build.gradle"))) {
            content.append("- **Build System:** Gradle\n");
        }

        if (Files.exists(Path.of(workspacePath, "src/main/java"))) {
            content.append("- **Framework:** Spring Boot (detected)\n");
        }

        writeFile(overviewFile, content.toString());
    }

    public void generateStackInfo(String workspacePath, UUID projectId) {
        Path aiDir = Path.of(workspacePath, ".ai");
        Path stackFile = aiDir.resolve("stack.md");

        StringBuilder content = new StringBuilder();
        content.append("# Technology Stack\n\n");
        content.append("**Generated:** ").append(Instant.now()).append("\n\n");

        // Parse pom.xml or build.gradle for dependencies
        Path pomFile = Path.of(workspacePath, "pom.xml");
        if (Files.exists(pomFile)) {
            try {
                String pomContent = Files.readString(pomFile);
                content.append("## Dependencies (from pom.xml)\n\n");

                // Extract key dependencies
                if (pomContent.contains("spring-boot")) {
                    content.append("- Spring Boot\n");
                }
                if (pomContent.contains("spring-data-jpa")) {
                    content.append("- Spring Data JPA\n");
                }
                if (pomContent.contains("postgresql")) {
                    content.append("- PostgreSQL\n");
                }
                if (pomContent.contains("lombok")) {
                    content.append("- Lombok\n");
                }
                if (pomContent.contains("mapstruct")) {
                    content.append("- MapStruct\n");
                }
                if (pomContent.contains("spring-security")) {
                    content.append("- Spring Security\n");
                }
            } catch (IOException e) {
                log.error("Failed to read pom.xml", e);
            }
        }

        writeFile(stackFile, content.toString());
    }

    public void generateStructureInfo(String workspacePath, UUID projectId) {
        Path aiDir = Path.of(workspacePath, ".ai");
        Path structureFile = aiDir.resolve("structure.md");

        StringBuilder content = new StringBuilder();
        content.append("# Project Structure\n\n");
        content.append("**Generated:** ").append(Instant.now()).append("\n\n");
        content.append("```\n");

        try (Stream<Path> paths = Files.walk(Path.of(workspacePath))) {
            paths.filter(Files::isDirectory)
                    .filter(p -> !p.toString().contains(".git"))
                    .filter(p -> !p.toString().contains("target"))
                    .filter(p -> !p.toString().contains("node_modules"))
                    .sorted()
                    .limit(100)
                    .forEach(p -> {
                        int depth = (int) Path.of(workspacePath).relativize(p).getNameCount();
                        String indent = "  ".repeat(depth);
                        content.append(indent).append(p.getFileName()).append("/\n");
                    });
        } catch (IOException e) {
            log.error("Failed to walk directory", e);
        }

        content.append("```\n");
        writeFile(structureFile, content.toString());
    }

    public void generateModulesInfo(String workspacePath, UUID projectId) {
        Path aiDir = Path.of(workspacePath, ".ai");
        Path modulesFile = aiDir.resolve("modules.md");

        StringBuilder content = new StringBuilder();
        content.append("# Modules\n\n");
        content.append("**Generated:** ").append(Instant.now()).append("\n\n");

        // Find all Java packages
        Path srcDir = Path.of(workspacePath, "src/main/java");
        if (Files.exists(srcDir)) {
            try (Stream<Path> paths = Files.walk(srcDir)) {
                Map<String, List<Path>> packages = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .collect(Collectors.groupingBy(
                                p -> srcDir.relativize(p.getParent()).toString().replace("/", ".")
                        ));

                packages.forEach((pkg, files) -> {
                    content.append("## ").append(pkg).append("\n\n");
                    content.append("**Files:** ").append(files.size()).append("\n\n");
                    files.forEach(f -> content.append("- ").append(f.getFileName()).append("\n"));
                    content.append("\n");
                });
            } catch (IOException e) {
                log.error("Failed to analyze modules", e);
            }
        }

        writeFile(modulesFile, content.toString());
    }

    public void generateClassesInfo(String workspacePath, UUID projectId) {
        Path aiDir = Path.of(workspacePath, ".ai");
        Path classesFile = aiDir.resolve("classes.md");

        StringBuilder content = new StringBuilder();
        content.append("# Classes Inventory\n\n");
        content.append("**Generated:** ").append(Instant.now()).append("\n\n");

        Path srcDir = Path.of(workspacePath, "src/main/java");
        if (Files.exists(srcDir)) {
            try (Stream<Path> paths = Files.walk(srcDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .sorted()
                        .forEach(p -> {
                            try {
                                String fileContent = Files.readString(p);
                                JavaParserService.ParsedJavaFile parsed =
                                        javaParserService.parse(fileContent, p.toString());

                                content.append("## ").append(parsed.packageName()).append("\n\n");
                                for (JavaParserService.JavaClass cls : parsed.classes()) {
                                    content.append("### ").append(cls.name()).append("\n");
                                    content.append("- **File:** ").append(p.getFileName()).append("\n");
                                    content.append("- **Methods:** ").append(cls.methods().size()).append("\n");
                                    content.append("- **Dependencies:** ")
                                            .append(String.join(", ", cls.dependencies())).append("\n\n");
                                }
                            } catch (IOException e) {
                                log.error("Failed to parse: {}", p, e);
                            }
                        });
            } catch (IOException e) {
                log.error("Failed to walk source directory", e);
            }
        }

        writeFile(classesFile, content.toString());
    }

    public void generateDependenciesInfo(String workspacePath, UUID projectId) {
        Path aiDir = Path.of(workspacePath, ".ai");
        Path depsFile = aiDir.resolve("dependencies.md");

        StringBuilder content = new StringBuilder();
        content.append("# Dependencies Graph\n\n");
        content.append("**Generated:** ").append(Instant.now()).append("\n\n");

        Path srcDir = Path.of(workspacePath, "src/main/java");
        if (Files.exists(srcDir)) {
            Map<String, Set<String>> dependencyGraph = new HashMap<>();

            try (Stream<Path> paths = Files.walk(srcDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> {
                            try {
                                String fileContent = Files.readString(p);
                                JavaParserService.ParsedJavaFile parsed =
                                        javaParserService.parse(fileContent, p.toString());

                                for (JavaParserService.JavaClass cls : parsed.classes()) {
                                    String className = parsed.packageName() + "." + cls.name();
                                    dependencyGraph.put(className, new HashSet<>(cls.dependencies()));
                                }
                            } catch (IOException e) {
                                log.error("Failed to parse: {}", p, e);
                            }
                        });
            } catch (IOException e) {
                log.error("Failed to walk source directory", e);
            }

            dependencyGraph.forEach((cls, deps) -> {
                content.append("### ").append(cls).append("\n");
                content.append("Depends on:\n");
                deps.forEach(d -> content.append("- ").append(d).append("\n"));
                content.append("\n");
            });
        }

        writeFile(depsFile, content.toString());
    }

    public void writeRuntimeMemory(String workspacePath, String fileName, String content) {
        Path runtimeDir = Path.of(workspacePath, ".ai", "runtime");
        try {
            Files.createDirectories(runtimeDir);
            writeFile(runtimeDir.resolve(fileName), content);
        } catch (IOException e) {
            log.error("Failed to write runtime memory: {}", fileName, e);
        }
    }

    public String readRuntimeMemory(String workspacePath, String fileName) {
        Path runtimeFile = Path.of(workspacePath, ".ai", "runtime", fileName);
        try {
            if (Files.exists(runtimeFile)) {
                return Files.readString(runtimeFile);
            }
        } catch (IOException e) {
            log.error("Failed to read runtime memory: {}", fileName, e);
        }
        return null;
    }

    public void writeCurrentTask(String workspacePath, UUID taskId, String taskDescription) {
        String content = String.format("""
                # Current Task
                
                **Task ID:** %s
                **Started:** %s
                
                ## Description
                %s
                """, taskId, Instant.now(), taskDescription);
        writeRuntimeMemory(workspacePath, "current-task.md", content);
    }

    public void writeCurrentPlan(String workspacePath, String plan) {
        String content = String.format("""
                # Current Plan
                
                **Updated:** %s
                
                %s
                """, Instant.now(), plan);
        writeRuntimeMemory(workspacePath, "current-plan.md", content);
    }

    public void appendDiscovery(String workspacePath, String discovery) {
        String existing = readRuntimeMemory(workspacePath, "discoveries.md");
        String content = (existing != null ? existing : "# Discoveries\n\n") +
                String.format("- [%s] %s\n", Instant.now(), discovery);
        writeRuntimeMemory(workspacePath, "discoveries.md", content);
    }

    public void appendDecision(String workspacePath, String decision) {
        String existing = readRuntimeMemory(workspacePath, "decisions.md");
        String content = (existing != null ? existing : "# Decisions\n\n") +
                String.format("- [%s] %s\n", Instant.now(), decision);
        writeRuntimeMemory(workspacePath, "decisions.md", content);
    }

    public void writeTaskSummary(String workspacePath, UUID taskId, String summary) {
        String content = String.format("""
                # Task Summary
                
                **Task ID:** %s
                **Completed:** %s
                
                ## Summary
                %s
                """, taskId, Instant.now(), summary);
        writeRuntimeMemory(workspacePath, "task-summary.md", content);
    }

    private void writeFile(Path path, String content) {
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            log.error("Failed to write file: {}", path, e);
        }
    }

    private long countFiles(String workspacePath, String pattern) {
        try (Stream<Path> paths = Files.walk(Path.of(workspacePath))) {
            return paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(
                            pattern.replace("*", "")))
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }
}
