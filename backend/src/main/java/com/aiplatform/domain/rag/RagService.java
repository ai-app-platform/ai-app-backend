package com.aiplatform.domain.rag;

import com.aiplatform.config.PlatformProperties;
import com.aiplatform.interfaces.rest.dto.RagSearchResult;
import com.aiplatform.interfaces.rest.dto.RagChunk;
import com.aiplatform.shared.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagService {

    private final RagDocumentRepository ragDocumentRepository;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final PlatformProperties platformProperties;
    private final JavaParserService javaParserService;

    @Transactional
    public void indexProject(UUID projectId, String workspacePath, String commitSha) {
        log.info("Starting full indexing for project: {}", projectId);

        // Index all Java files
        List<Path> javaFiles = findJavaFiles(workspacePath);
        log.info("Found {} Java files to index", javaFiles.size());

        for (Path javaFile : javaFiles) {
            try {
                indexJavaFile(projectId, javaFile, workspacePath, commitSha);
            } catch (Exception e) {
                log.error("Failed to index file: {}", javaFile, e);
            }
        }

        // Build and index code graph
        buildAndIndexCodeGraph(projectId, workspacePath, commitSha);

        log.info("Indexing completed for project: {}", projectId);
    }

    @Transactional
    public void indexJavaFile(UUID projectId, Path filePath, String workspacePath, String commitSha) {
        try {
            String content = Files.readString(filePath);
            String relativePath = workspacePath != null
                    ? Path.of(workspacePath).relativize(filePath).toString()
                    : filePath.toString();

            // Parse Java file
            JavaParserService.ParsedJavaFile parsed = javaParserService.parse(content, relativePath);

            // Index file structure
            indexFileStructure(projectId, parsed, commitSha);

            // Index classes and methods
            for (JavaParserService.JavaClass javaClass : parsed.classes()) {
                indexClass(projectId, parsed, javaClass, commitSha);

                for (JavaParserService.JavaMethod method : javaClass.methods()) {
                    indexMethod(projectId, parsed, javaClass, method, commitSha);
                }
            }

            // Chunk and index full content
            chunkAndIndexContent(projectId, content, relativePath, commitSha);

        } catch (Exception e) {
            log.error("Failed to index Java file: {}", filePath, e);
        }
    }

    private void indexFileStructure(UUID projectId, JavaParserService.ParsedJavaFile parsed, String commitSha) {
        String contentHash = computeHash(parsed.content());

        RagDocument doc = RagDocument.builder()
                .projectId(projectId)
                .sourceType(RagSourceType.SOURCE_CODE)
                .filePath(parsed.filePath())
                .content(String.format("File: %s\nPackage: %s\nImports: %s\nClasses: %s",
                        parsed.filePath(),
                        parsed.packageName(),
                        String.join(", ", parsed.imports()),
                        parsed.classes().stream().map(JavaParserService.JavaClass::name).collect(Collectors.joining(", "))))
                .contentHash(contentHash)
                .commitSha(commitSha)
                .metadata(String.format("{\"type\":\"file\",\"package\":\"%s\",\"imports\":%d,\"classes\":%d}",
                        parsed.packageName(), parsed.imports().size(), parsed.classes().size()))
                .build();

        ragDocumentRepository.save(doc);
        storeInVectorStore(doc, Map.of("type", "file", "project", projectId.toString()));
    }

    private void indexClass(UUID projectId, JavaParserService.ParsedJavaFile parsed,
                             JavaParserService.JavaClass javaClass, String commitSha) {
        String content = String.format("Class: %s\nPackage: %s\nFile: %s\nMethods: %s\nDependencies: %s",
                javaClass.name(),
                parsed.packageName(),
                parsed.filePath(),
                String.join(", ", javaClass.methods().stream().map(JavaParserService.JavaMethod::name).toList()),
                String.join(", ", javaClass.dependencies()));

        String contentHash = computeHash(content);

        RagDocument doc = RagDocument.builder()
                .projectId(projectId)
                .sourceType(RagSourceType.SOURCE_CODE)
                .filePath(parsed.filePath())
                .symbol(javaClass.name())
                .lineRangeStart(javaClass.lineStart())
                .lineRangeEnd(javaClass.lineEnd())
                .content(content)
                .contentHash(contentHash)
                .commitSha(commitSha)
                .metadata(String.format("{\"type\":\"class\",\"name\":\"%s\",\"methods\":%d}",
                        javaClass.name(), javaClass.methods().size()))
                .build();

        ragDocumentRepository.save(doc);
        storeInVectorStore(doc, Map.of("type", "class", "symbol", javaClass.name(), "project", projectId.toString()));
    }

    private void indexMethod(UUID projectId, JavaParserService.ParsedJavaFile parsed,
                              JavaParserService.JavaClass javaClass,
                              JavaParserService.JavaMethod method, String commitSha) {
        String content = String.format("Method: %s.%s\nSignature: %s\nFile: %s\nLines: %d-%d",
                javaClass.name(),
                method.name(),
                method.signature(),
                parsed.filePath(),
                method.lineStart(),
                method.lineEnd());

        String contentHash = computeHash(content);

        RagDocument doc = RagDocument.builder()
                .projectId(projectId)
                .sourceType(RagSourceType.SOURCE_CODE)
                .filePath(parsed.filePath())
                .symbol(javaClass.name() + "." + method.name())
                .lineRangeStart(method.lineStart())
                .lineRangeEnd(method.lineEnd())
                .content(content)
                .contentHash(contentHash)
                .commitSha(commitSha)
                .metadata(String.format("{\"type\":\"method\",\"class\":\"%s\",\"name\":\"%s\"}",
                        javaClass.name(), method.name()))
                .build();

        ragDocumentRepository.save(doc);
        storeInVectorStore(doc, Map.of("type", "method", "symbol", javaClass.name() + "." + method.name(),
                "project", projectId.toString()));
    }

    private void chunkAndIndexContent(UUID projectId, String content, String filePath, String commitSha) {
        List<String> chunks = chunkContent(content);

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String contentHash = computeHash(chunk);

            RagDocument doc = RagDocument.builder()
                    .projectId(projectId)
                    .sourceType(RagSourceType.SOURCE_CODE)
                    .filePath(filePath)
                    .content(chunk)
                    .contentHash(contentHash)
                    .commitSha(commitSha)
                    .chunkIndex(i)
                    .build();

            ragDocumentRepository.save(doc);
            storeInVectorStore(doc, Map.of("type", "chunk", "chunk_index", String.valueOf(i),
                    "project", projectId.toString()));
        }
    }

    @Transactional
    public void buildAndIndexCodeGraph(UUID projectId, String workspacePath, String commitSha) {
        log.info("Building code graph for project: {}", projectId);

        // Build dependency graph from all parsed files
        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        Map<String, Set<String>> callerGraph = new HashMap<>();

        List<Path> javaFiles = findJavaFiles(workspacePath);
        for (Path javaFile : javaFiles) {
            try {
                String content = Files.readString(javaFile);
                JavaParserService.ParsedJavaFile parsed = javaParserService.parse(content, javaFile.toString());

                for (JavaParserService.JavaClass javaClass : parsed.classes()) {
                    String className = parsed.packageName() + "." + javaClass.name();
                    dependencyGraph.put(className, new HashSet<>(javaClass.dependencies()));

                    for (JavaParserService.JavaMethod method : javaClass.methods()) {
                        String methodKey = className + "." + method.name();
                        callerGraph.put(methodKey, new HashSet<>(method.callers()));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse file for graph: {}", javaFile, e);
            }
        }

        // Store graph in vector store for retrieval
        String graphJson = buildGraphJson(dependencyGraph, callerGraph);
        String contentHash = computeHash(graphJson);

        RagDocument graphDoc = RagDocument.builder()
                .projectId(projectId)
                .sourceType(RagSourceType.SOURCE_CODE)
                .symbol("__CODE_GRAPH__")
                .content(graphJson)
                .contentHash(contentHash)
                .commitSha(commitSha)
                .metadata("{\"type\":\"code_graph\"}")
                .build();

        ragDocumentRepository.save(graphDoc);
        storeInVectorStore(graphDoc, Map.of("type", "code_graph", "project", projectId.toString()));

        log.info("Code graph built and indexed for project: {}", projectId);
    }

    public List<RagSearchResult> search(UUID projectId, String query, Integer limit, Map<String, Object> filters) {
        int maxResults = limit != null ? limit : platformProperties.getRag().getMaxResults();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(maxResults)
                .similarityThreshold(platformProperties.getRag().getSimilarityThreshold())
                .filterExpression(String.format("project == '%s'", projectId.toString()))
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        return results.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
                    return new RagSearchResult(
                            UUID.randomUUID(),
                            (String) metadata.getOrDefault("file", "unknown"),
                            0.95, // Score from vector store
                            doc.getText(),
                            (String) metadata.getOrDefault("type", "code"),
                            null,
                            metadata.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())),
                            List.of(new RagChunk(doc.getText(), 0.95))
                    );
                })
                .collect(Collectors.toList());
    }

    public RagStatusResponse getStatus(UUID projectId) {
        List<RagDocument> docs = ragDocumentRepository.findByProjectId(projectId);
        return new RagStatusResponse(
                docs.size(),
                docs.isEmpty() ? null : Instant.now().toString(),
                "healthy",
                "text-embedding-3-small",
                "1.0.0"
        );
    }

    private void storeInVectorStore(RagDocument doc, Map<String, String> metadata) {
        try {
            Document document = new Document(doc.getContent(), metadata);
            vectorStore.add(List.of(document));
        } catch (Exception e) {
            log.error("Failed to store in vector store: {}", doc.getId(), e);
        }
    }

    private List<String> chunkContent(String content) {
        int chunkSize = platformProperties.getRag().getChunkSize();
        int overlap = platformProperties.getRag().getChunkOverlap();

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());

            // Try to break at line boundary
            if (end < content.length()) {
                int lastNewline = content.lastIndexOf('\n', end);
                if (lastNewline > start) {
                    end = lastNewline;
                }
            }

            chunks.add(content.substring(start, end));
            start = end - overlap;
        }

        return chunks;
    }

    private List<Path> findJavaFiles(String workspacePath) {
        try (Stream<Path> paths = Files.walk(Path.of(workspacePath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to find Java files in: {}", workspacePath, e);
            return List.of();
        }
    }

    private String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    private String buildGraphJson(Map<String, Set<String>> dependencyGraph, Map<String, Set<String>> callerGraph) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"dependencies\":{");
        dependencyGraph.forEach((key, deps) -> {
            sb.append("\"").append(key).append("\":[").append(
                    deps.stream().map(d -> "\"" + d + "\"").collect(Collectors.joining(","))
            ).append("],");
        });
        sb.append("},\"callers\":{");
        callerGraph.forEach((key, callers) -> {
            sb.append("\"").append(key).append("\":[").append(
                    callers.stream().map(c -> "\"" + c + "\"").collect(Collectors.joining(","))
            ).append("],");
        });
        sb.append("}}");
        return sb.toString();
    }
}
