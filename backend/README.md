# AI Platform Backend

## Overview

Spring Boot 4.1 backend for AI Software Engineering Platform with Java 21, PostgreSQL, and complete implementation of all APIs.

## Architecture

### Core Principles
- **Git / Files = Source of Truth**
- **Database = Metadata / Index**
- **RAG = Retrieval (not Source of Truth)**
- **Disk / Workspace = Active Runtime State**
- **Knowledge ≠ Memory**
- **Configuration-driven & Extensible**

### Technology Stack
- **Java 21**
- **Spring Boot 4.1**
- **Spring AI 2.0** (LLM, Embedding, Vector Store)
- **PostgreSQL 16+** with PgVector extension
- **JGit 7.0** (Git operations)
- **Flyway 10.x** (Database migrations)
- **LangChain4j 1.0** (Agent orchestration)

## Domain Modules

### 1. Project Management
- Project CRUD
- Git repository cloning
- Workspace initialization
- Automatic indexing on project creation

### 2. Task Management
- Task lifecycle (Created → Planning → Executing → Completed)
- Task branch creation
- Agent team execution
- Commit, push, and PR creation

### 3. Agent Management
- Agent definitions with capabilities
- Skill and tool assignments
- Model configuration
- Execution policies

### 4. Tool Management
- Tool definitions (HTTP, INTERNAL, MCP)
- Capability-based discovery
- Permission levels
- Connector integration

### 5. Connector Management
- External system connections (GitHub, GitLab, Jira, etc.)
- Adapter pattern (REST, SDK, MCP, Native)
- Credential references (not plaintext)
- Health checks

### 6. Knowledge Management
- Platform and project knowledge
- Hierarchical resolution (Platform → Project → Workflow → Task)
- Versioned entries
- Source of truth tracking

### 7. Memory Management
- **Short-term Memory**: `.ai/` directory with markdown files
  - `overview.md` - Project overview
  - `architecture.md` - Architecture description
  - `modules.md` - Module breakdown
  - `dependencies.md` - Dependency graph
  - `classes.md` - Class inventory
  - `structure.md` - File structure
  - `runtime/` - Task execution memory

- **Long-term Memory**: RAG with vector database
  - Indexed source code
  - Code graph
  - Documentation
  - Historical memory

### 8. Workflow Management
- Machine-readable workflow definitions
- Step dependencies
- Parallel execution
- Validation and approval

### 9. Role & Skill Management
- Role definitions with constraints
- Skill expertise levels
- Agent-role mapping

### 10. Team Management
- Project-level agent teams
- Agent enable/disable
- Versioned team configurations

### 11. Prompt Management
- Prompt templates
- Variable substitution
- Model configuration
- Composition rules

### 12. Git Management
- Repository cloning
- Branch management
- Commit and push
- Diff analysis
- Pull request creation

### 13. RAG (Retrieval Augmented Generation)
- **Java File Parsing**: AST-based extraction of classes, methods, dependencies
- **Chunking**: Intelligent content chunking with overlap
- **Embedding**: Vector embeddings using OpenAI/Spring AI
- **Vector Storage**: PgVector for similarity search
- **Code Graph**: Dependency and caller relationship tracking
- **Hybrid Search**: Semantic + lexical search

### 14. Codebase Intelligence
- Full and incremental analysis
- Code graph construction
- Module and symbol context
- Staleness detection

### 15. Context Engine
- Local-first retrieval strategy
- Layered context assembly
- Knowledge resolution
- Memory integration

### 16. Workspace Management
- Runtime working copy
- File operations
- Command execution
- Build and test results

### 17. Planner
- Task analysis
- Execution plan creation
- Agent selection from project team
- Re-planning on feedback

## API Endpoints

### Dashboard
- `GET /dashboard/stats` - Dashboard statistics
- `GET /dashboard/activities` - Recent activities

### Projects
- `GET /projects` - List all projects
- `GET /projects/{id}` - Get project details
- `POST /projects` - Create project (triggers clone + indexing)
- `PUT /projects/{id}` - Update project
- `DELETE /projects/{id}` - Delete project

### Tasks
- `GET /tasks` - List tasks (optional projectId filter)
- `GET /tasks/{id}` - Get task details
- `POST /tasks` - Create task
- `PATCH /tasks/{id}/status` - Update task status
- `POST /tasks/{id}/execute` - Execute task
- `POST /tasks/{id}/complete` - Complete task

### Agents
- `GET /agents` - List agents
- `GET /agents/{id}` - Get agent details
- `POST /agents` - Create agent
- `PUT /agents/{id}` - Update agent

### Tools
- `GET /tools` - List tools
- `GET /tools/{id}` - Get tool details
- `POST /tools` - Create tool

### Connectors
- `GET /connectors` - List connectors
- `GET /connectors/{id}` - Get connector details
- `POST /connectors` - Create connector
- `POST /connectors/{id}/test` - Test connection

### Knowledge
- `GET /knowledge` - List knowledge (optional scope filter)
- `GET /knowledge/{id}` - Get knowledge details
- `POST /knowledge` - Create knowledge

### Workflows
- `GET /workflows` - List workflows
- `GET /workflows/{id}` - Get workflow details
- `POST /workflows` - Create workflow

### Roles
- `GET /roles` - List roles
- `GET /roles/{id}` - Get role details
- `POST /roles` - Create role

### Skills
- `GET /skills` - List skills
- `GET /skills/{id}` - Get skill details
- `POST /skills` - Create skill

### Teams
- `GET /teams` - List teams
- `GET /projects/{projectId}/team` - Get project team
- `POST /teams` - Create team
- `PUT /teams/{id}` - Update team

### Prompts
- `GET /prompts` - List prompts
- `GET /prompts/{id}` - Get prompt details
- `POST /prompts` - Create prompt

### Memory
- `GET /projects/{projectId}/memory` - Get project memory
- `GET /memory/{id}` - Get memory details

### RAG
- `GET /projects/{projectId}/rag/status` - Get RAG status
- `POST /projects/{projectId}/rag/search` - Search RAG

### Git Management
- `GET /projects/{projectId}/git/branches` - List branches
- `GET /projects/{projectId}/git/commits` - List commits
- `POST /projects/{projectId}/git/branches` - Create branch
- `GET /projects/{projectId}/git/diff` - Get diff
- `GET /projects/{projectId}/git/pull-requests` - List PRs

### Codebase Intelligence
- `GET /projects/{projectId}/codebase/overview` - Get overview
- `GET /projects/{projectId}/codebase/modules` - List modules
- `GET /projects/{projectId}/codebase/modules/{name}` - Get module details
- `POST /projects/{projectId}/codebase/search` - Search codebase
- `GET /projects/{projectId}/codebase/symbols/{name}` - Get symbol details
- `GET /projects/{projectId}/codebase/files` - Get file content

### Context Engine
- `GET /tasks/{taskId}/context` - Get assembled context

### Workspace
- `GET /workspaces` - List workspaces
- `GET /workspaces/{name}` - Get workspace details
- `GET /workspaces/{name}/files` - List files
- `POST /workspaces/{name}/execute` - Execute command

### Settings
- `GET /settings/database` - Database settings
- `POST /settings/database/test` - Test database
- `POST /settings/database/backup` - Backup database
- `GET /settings/api-keys` - List API keys
- `POST /settings/api-keys` - Create API key
- `DELETE /settings/api-keys/{id}` - Delete API key
- `GET /settings/infrastructure` - Infrastructure status

## Task Lifecycle

```
1. Create Task
   ↓
2. Create Task Branch (from main)
   ↓
3. Initialize Runtime Memory (.ai/runtime/)
   ↓
4. Planning Phase
   - Analyze task requirements
   - Create execution plan
   ↓
5. Agent Selection
   - Select agents from project team
   - Map capabilities to agents
   ↓
6. Execution
   - Context Engine assembles context
   - Agents execute with LLM
   - Tool calls through Tool Executor
   - Changes applied to workspace
   ↓
7. Validation
   - Build verification
   - Test execution
   - Security scan
   ↓
8. Review
   - Code review by reviewer agent
   - Approval if needed
   ↓
9. Commit & Push
   - Commit changes to task branch
   - Push to remote
   ↓
10. Create Pull Request
    - PR to main branch
    ↓
11. Complete Task
    - Update runtime memory
    - Promote relevant discoveries to long-term memory
```

## RAG Implementation

### Java File Parsing
- Package extraction
- Import analysis
- Class/interface/enum detection
- Method signature extraction
- Dependency tracking
- Caller relationship mapping

### Indexing Process
1. **File Structure Index**: Package, imports, classes
2. **Class Index**: Class name, methods, dependencies
3. **Method Index**: Method signature, parameters, callers
4. **Content Chunking**: Intelligent chunking with overlap
5. **Code Graph**: Dependency and caller relationships

### Vector Storage
- PgVector extension for PostgreSQL
- Embedding model: text-embedding-3-small
- Similarity search with threshold filtering
- Metadata-based filtering

### Search Strategy
- Semantic search (vector similarity)
- Metadata filtering (project, type, symbol)
- Hybrid retrieval (semantic + lexical)

## Short-term Memory (.ai/ directory)

### Structure
```
.ai/
├── overview.md          # Project overview
├── architecture.md      # Architecture description
├── modules.md           # Module breakdown
├── dependencies.md      # Dependency graph
├── conventions.md       # Coding conventions
├── stack.md             # Technology stack
├── structure.md         # File structure
├── classes.md           # Class inventory
└── runtime/
    ├── current-task.md      # Current task info
    ├── current-plan.md      # Execution plan
    ├── discoveries.md       # Discoveries during execution
    ├── decisions.md         # Decisions made
    ├── touched-files.md     # Modified files
    ├── test-results.md      # Test results
    └── task-summary.md      # Task completion summary
```

### Purpose
- Quick project understanding without reading entire codebase
- AI can understand project structure by reading these files
- Only refer to RAG when additional information is needed
- Runtime memory tracks task execution state

## Database Schema

### Core Tables
- `projects` - Project metadata
- `agents` - Agent definitions
- `roles` - Role definitions
- `skills` - Skill definitions
- `tools` - Tool definitions
- `connectors` - Connector configurations
- `prompts` - Prompt templates
- `knowledge_entries` - Knowledge base
- `memories` - Memory entries
- `workflows` - Workflow definitions
- `tasks` - Task instances
- `project_teams` - Project agent teams
- `git_repositories` - Git repository metadata
- `rag_documents` - RAG document metadata
- `codebase_projections` - Codebase intelligence
- `workspaces` - Workspace metadata
- `domain_events` - Event sourcing
- `outbox_events` - Transactional outbox

### Features
- UUID primary keys
- Optimistic locking (@Version)
- Audit fields (created_at, updated_at, created_by, updated_by)
- JSONB for flexible metadata
- Proper indexing for performance

## Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_platform
    username: postgres
    password: postgres
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
    vectorstore:
      pgvector:
        dimensions: 1536
        index-type: HNSW
        distance-type: COSINE_DISTANCE

platform:
  workspace:
    base-path: /tmp/ai-platform/workspaces
  rag:
    chunk-size: 1000
    chunk-overlap: 200
    max-results: 10
    similarity-threshold: 0.75
```

## Setup

### Prerequisites
- Java 21+
- PostgreSQL 16+ with PgVector extension
- Maven 3.9+

### Database Setup
```bash
# Create database
createdb ai_platform

# Enable PgVector extension
psql -d ai_platform -c "CREATE EXTENSION IF NOT EXISTS vector;"

# Run migrations (automatic on startup)
```

### Build & Run
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Environment Variables
```bash
export OPENAI_API_KEY=your-api-key
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=ai_platform
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## Design Patterns

- **Aggregate Root**: All domain entities
- **Repository**: Spring Data JPA
- **Command Pattern**: Create/Update commands
- **State Machine**: Task status transitions
- **Adapter Pattern**: Connector adapters
- **Strategy Pattern**: Agent selection, retrieval policies
- **Template Method**: Base service operations
- **Outbox Pattern**: Domain events
- **Builder**: Complex entity construction

## SOLID Principles

- **Single Responsibility**: Each service has one responsibility
- **Open/Closed**: Configuration-driven extensibility
- **Liskov Substitution**: Connector adapters interchangeable
- **Interface Segregation**: Focused interfaces
- **Dependency Inversion**: Depend on abstractions

## ACID Compliance

- **Atomicity**: @Transactional on all service methods
- **Consistency**: Business rule validation, state machine
- **Isolation**: Optimistic locking with @Version
- **Durability**: PostgreSQL WAL, Flyway migrations

## Project Flow

1. **Create Project**
   - Clone Git repository
   - Initialize workspace
   - Generate .ai/ directory
   - Index in RAG (long-term memory)

2. **Create Task**
   - Create task branch from main
   - Initialize runtime memory
   - Execute agent team
   - Apply changes
   - Commit and push
   - Create pull request

3. **Task Completion**
   - Update runtime memory
   - Promote discoveries to long-term memory
   - Mark task as completed

## Notes

- All timestamps in ISO 8601 format
- All IDs in UUID format
- Credentials stored as references (not plaintext)
- Git operations through JGit
- Vector embeddings through Spring AI
- Java parsing through regex-based AST extraction
- Short-term memory in markdown files for AI readability
- Long-term memory in vector database for semantic search

## Version

**Backend Version:** 1.0.0  
**Last Updated:** 2024  
**Spring Boot:** 4.1  
**Java:** 21
