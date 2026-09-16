import { useState } from 'react';

const modules = [
  {
    name: 'Project Management',
    description: 'Project identity, configuration, and lifecycle',
    entities: ['Project'],
    services: ['ProjectService'],
    patterns: ['Aggregate Root', 'Repository', 'Command Pattern']
  },
  {
    name: 'Agent Management',
    description: 'Agent definitions, capabilities, and runtime configuration',
    entities: ['Agent', 'AgentType'],
    services: ['AgentService'],
    patterns: ['Versioned Entity', 'Aggregate Root', 'Set-based References']
  },
  {
    name: 'Role Management',
    description: 'Role definitions with responsibility and constraints',
    entities: ['Role'],
    services: ['RoleService'],
    patterns: ['Policy Reference', 'Aggregate Root']
  },
  {
    name: 'Skill Management',
    description: 'Skill/expertise definitions for agents',
    entities: ['Skill', 'ExpertiseLevel'],
    services: ['SkillService'],
    patterns: ['Category-based', 'Aggregate Root']
  },
  {
    name: 'Tool Management',
    description: 'Tool definitions, schemas, capabilities, and permissions',
    entities: ['Tool', 'ToolType', 'PermissionLevel'],
    services: ['ToolService', 'ToolExecutorService'],
    patterns: ['Definition ≠ Implementation', 'Capability-based', 'Versioned']
  },
  {
    name: 'Connector Management',
    description: 'External system connections with adapters and credentials',
    entities: ['Connector', 'ConnectorType', 'AdapterType', 'ConnectionState'],
    services: ['ConnectorService'],
    patterns: ['Adapter Pattern', 'State Machine', 'Credential Reference']
  },
  {
    name: 'Prompt Management',
    description: 'Prompt templates, variables, and composition',
    entities: ['Prompt', 'PromptScope'],
    services: ['PromptService'],
    patterns: ['Template Rendering', 'Scoped Resolution', 'Versioned']
  },
  {
    name: 'Knowledge Management',
    description: 'Permanent knowledge with hierarchical resolution',
    entities: ['KnowledgeEntry', 'KnowledgeScope'],
    services: ['KnowledgeService'],
    patterns: ['Resolution Hierarchy', 'Source of Truth', 'Versioned']
  },
  {
    name: 'Memory',
    description: 'Runtime state, history, and long-term memory',
    entities: ['Memory', 'MemoryType'],
    services: ['MemoryService'],
    patterns: ['Lifecycle Management', 'Promotion Pattern']
  },
  {
    name: 'Workflow',
    description: 'Machine-readable process definitions and execution rules',
    entities: ['Workflow', 'WorkflowScope'],
    services: ['WorkflowService'],
    patterns: ['Definition ≠ Engine', 'Versioned', 'Scoped']
  },
  {
    name: 'Task',
    description: 'Task lifecycle with state machine transitions',
    entities: ['Task', 'TaskStatus', 'TaskType', 'ExecutionMode'],
    services: ['TaskService'],
    patterns: ['State Machine', 'Immutable After Start', 'Trace']
  },
  {
    name: 'Project Team',
    description: 'Available agent pool per project with versioning',
    entities: ['ProjectTeam'],
    services: ['ProjectTeamService'],
    patterns: ['Versioned Team', 'Enable/Disable', 'Set Operations']
  },
  {
    name: 'Git Management',
    description: 'Repository lifecycle, branches, commits, and PRs',
    entities: ['GitRepository', 'CloneStatus'],
    services: ['GitManagementService'],
    patterns: ['Lifecycle Management', 'Connector Integration']
  },
  {
    name: 'RAG',
    description: 'Indexing, embedding, and retrieval layer',
    entities: ['RagDocument', 'RagSourceType'],
    services: ['RagService'],
    patterns: ['Retrieval ≠ Source of Truth', 'Incremental Indexing']
  },
  {
    name: 'Codebase Intelligence',
    description: 'AST analysis, code graph, and derived projections',
    entities: ['CodebaseProjection'],
    services: ['CodebaseIntelligenceService'],
    patterns: ['Derived Projection', 'Incremental Analysis', 'Staleness Detection']
  },
  {
    name: 'Context Engine',
    description: 'Context collection, retrieval policy, and assembly',
    entities: ['AgentContext', 'ContextRequest'],
    services: ['ContextEngineService'],
    patterns: ['Local-first Retrieval', 'Layered Assembly', 'No Ownership']
  },
  {
    name: 'Planner',
    description: 'Task analysis, plan creation, and agent selection',
    entities: ['ExecutionPlan', 'PlanStep'],
    services: ['PlannerService'],
    patterns: ['Team Constraint', 'Re-planning', 'Capability Matching']
  }
];

const principles = [
  'Git / Files = Source of Truth',
  'Database = Metadata / Index',
  'RAG = Retrieval (not Source of Truth)',
  'Disk / Workspace = Active Runtime State',
  'Knowledge ≠ Memory',
  'Knowledge ≠ Prompt',
  'Memory ≠ RAG',
  'Prompt ≠ Workflow',
  'Workflow ≠ Agent',
  'Agent ≠ Tool',
  'Connector ≠ MCP',
  'Connector ≠ Tool',
  'Git ≠ Workspace',
  'Tool Definition ≠ Tool Implementation',
  'Codebase Intelligence ≠ Knowledge',
  'Codebase Intelligence ≠ RAG',
  'Team is persistent at Project level, not Task level',
  'Planner cannot select outside Project Team',
  'Execution must be Versioned and Reproducible',
  'Configuration-driven and Extensible'
];

const techStack = [
  { name: 'Java', version: '21' },
  { name: 'Spring Boot', version: '4.1' },
  { name: 'Spring AI', version: '2.0' },
  { name: 'LangChain4j', version: '1.0' },
  { name: 'PostgreSQL', version: '16+' },
  { name: 'PgVector', version: 'Extension' },
  { name: 'Flyway', version: '10.x' },
  { name: 'JGit', version: '7.0' },
  { name: 'MapStruct', version: '1.6' },
  { name: 'Lombok', version: 'Latest' }
];

const designPatterns = [
  { pattern: 'Aggregate Root', usage: 'All domain entities extend AggregateRoot with versioning' },
  { pattern: 'Repository', usage: 'Spring Data JPA repositories with custom queries' },
  { pattern: 'Command Pattern', usage: 'Create/Update commands as immutable records' },
  { pattern: 'State Machine', usage: 'Task status transitions with validation' },
  { pattern: 'Adapter Pattern', usage: 'Connector adapters (REST, SDK, MCP, Native)' },
  { pattern: 'Strategy Pattern', usage: 'Agent selection strategies, retrieval policies' },
  { pattern: 'Template Method', usage: 'Base service with common CRUD operations' },
  { pattern: 'Outbox Pattern', usage: 'Domain events for eventual consistency' },
  { pattern: 'Specification', usage: 'Knowledge resolution hierarchy' },
  { pattern: 'Builder', usage: 'Complex entity construction' },
  { pattern: 'Factory Method', usage: 'Tool executor adapter selection' },
  { pattern: 'Observer', usage: 'Domain events propagation' }
];

function App() {
  const [activeTab, setActiveTab] = useState('overview');
  const [selectedModule, setSelectedModule] = useState<string | null>(null);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white">
      {/* Header */}
      <header className="border-b border-slate-700/50 backdrop-blur-sm bg-slate-900/50 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
                <span className="text-xl">🤖</span>
              </div>
              <div>
                <h1 className="text-xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
                  AI Platform Backend
                </h1>
                <p className="text-xs text-slate-400">Spring Boot 4.1 • Java 21 • PostgreSQL</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="px-3 py-1 bg-green-500/20 text-green-400 text-xs font-medium rounded-full border border-green-500/30">
                ● Backend Ready
              </span>
              <span className="px-3 py-1 bg-blue-500/20 text-blue-400 text-xs font-medium rounded-full border border-blue-500/30">
                v1.0.0
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="border-b border-slate-700/50 bg-slate-800/30">
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex gap-1 overflow-x-auto">
            {['overview', 'modules', 'architecture', 'patterns', 'database'].map(tab => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-4 py-3 text-sm font-medium capitalize whitespace-nowrap transition-all ${
                  activeTab === tab
                    ? 'text-blue-400 border-b-2 border-blue-400'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {tab}
              </button>
            ))}
          </div>
        </div>
      </nav>

      {/* Content */}
      <main className="max-w-7xl mx-auto px-6 py-8">
        {activeTab === 'overview' && (
          <div className="space-y-8">
            {/* Stats */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {[
                { label: 'Domain Modules', value: '17', icon: '📦' },
                { label: 'Entities', value: '25+', icon: '🗂️' },
                { label: 'Services', value: '17', icon: '⚙️' },
                { label: 'DB Tables', value: '22', icon: '🗄️' }
              ].map(stat => (
                <div key={stat.label} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-4">
                  <div className="text-2xl mb-1">{stat.icon}</div>
                  <div className="text-2xl font-bold text-white">{stat.value}</div>
                  <div className="text-xs text-slate-400">{stat.label}</div>
                </div>
              ))}
            </div>

            {/* Core Principles */}
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                <span className="text-blue-400">◆</span> Core Architectural Principles
              </h2>
              <div className="grid md:grid-cols-2 gap-2">
                {principles.map((p, i) => (
                  <div key={i} className="flex items-center gap-2 text-sm text-slate-300">
                    <span className="text-green-400 text-xs">✓</span>
                    {p}
                  </div>
                ))}
              </div>
            </div>

            {/* Tech Stack */}
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                <span className="text-purple-400">◆</span> Technology Stack
              </h2>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
                {techStack.map(tech => (
                  <div key={tech.name} className="bg-slate-700/30 rounded-lg p-3 text-center">
                    <div className="text-sm font-medium text-white">{tech.name}</div>
                    <div className="text-xs text-slate-400">{tech.version}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Architecture Statement */}
            <div className="bg-gradient-to-r from-blue-900/30 to-purple-900/30 border border-blue-700/30 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-3">Canonical Architecture Statement</h2>
              <div className="text-sm text-slate-300 leading-relaxed space-y-2">
                <p><strong className="text-blue-300">Source of Truth:</strong> Git / Files = Source of Truth. Database = Metadata / Index. RAG = Retrieval. Disk / Workspace = Active Runtime State.</p>
                <p><strong className="text-purple-300">Separation:</strong> Knowledge ≠ Memory. Knowledge ≠ Prompt. Memory ≠ RAG. Prompt ≠ Workflow. Workflow ≠ Agent. Agent ≠ Tool. Connector ≠ MCP.</p>
                <p><strong className="text-green-300">Design Goal:</strong> Configuration-driven, versioned, dynamic and extensible AI software-engineering platform with explicit domain separation.</p>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'modules' && (
          <div className="space-y-4">
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
              {modules.map((mod, i) => (
                <div
                  key={mod.name}
                  onClick={() => setSelectedModule(selectedModule === mod.name ? null : mod.name)}
                  className={`bg-slate-800/50 border rounded-xl p-5 cursor-pointer transition-all hover:border-blue-500/50 ${
                    selectedModule === mod.name ? 'border-blue-500/50 ring-1 ring-blue-500/20' : 'border-slate-700/50'
                  }`}
                >
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-6 h-6 bg-blue-500/20 text-blue-400 rounded text-xs flex items-center justify-center font-bold">
                      {i + 1}
                    </span>
                    <h3 className="font-semibold text-white text-sm">{mod.name}</h3>
                  </div>
                  <p className="text-xs text-slate-400 mb-3">{mod.description}</p>
                  {selectedModule === mod.name && (
                    <div className="space-y-2 mt-3 pt-3 border-t border-slate-700/50">
                      <div>
                        <span className="text-xs text-slate-500 uppercase">Entities:</span>
                        <div className="flex flex-wrap gap-1 mt-1">
                          {mod.entities.map(e => (
                            <span key={e} className="px-2 py-0.5 bg-blue-500/10 text-blue-300 text-xs rounded">{e}</span>
                          ))}
                        </div>
                      </div>
                      <div>
                        <span className="text-xs text-slate-500 uppercase">Services:</span>
                        <div className="flex flex-wrap gap-1 mt-1">
                          {mod.services.map(s => (
                            <span key={s} className="px-2 py-0.5 bg-green-500/10 text-green-300 text-xs rounded">{s}</span>
                          ))}
                        </div>
                      </div>
                      <div>
                        <span className="text-xs text-slate-500 uppercase">Patterns:</span>
                        <div className="flex flex-wrap gap-1 mt-1">
                          {mod.patterns.map(p => (
                            <span key={p} className="px-2 py-0.5 bg-purple-500/10 text-purple-300 text-xs rounded">{p}</span>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'architecture' && (
          <div className="space-y-6">
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">High-Level Architecture Flow</h2>
              <div className="font-mono text-xs text-slate-300 leading-relaxed bg-slate-900/50 rounded-lg p-4 overflow-x-auto">
                <pre>{`
Platform
 ├── Project Management
 ├── Prompt Management
 ├── Tool Management
 ├── Skill Management
 ├── Role Management
 ├── Agent Management
 ├── Connector Management
 ├── Git Management
 ├── Workspace
 ├── Knowledge Management
 ├── Memory
 ├── RAG
 ├── Project Codebase Intelligence
 ├── Context Engine
 └── Workflow

                    ↓
              Context Engine
                    ↓
              Workflow / LangGraph
                    ↓
               Agent Team
                    ↓
               Agent Runtime
                    ↓
                  LLM
                    ↓
              Tool Execution
                    ↓
                Workspace
                    ↓
          Build / Test / Review
                    ↓
              Git Commit / Push
                    ↓
                   PR
                `}</pre>
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">Task Lifecycle</h2>
              <div className="font-mono text-xs text-slate-300 leading-relaxed bg-slate-900/50 rounded-lg p-4 overflow-x-auto">
                <pre>{`
Create Task → Resolve Project → Prepare Workspace → Create Task Branch
    ↓
Resolve Knowledge → Resolve Agent Team → Resolve Workflow
    ↓
Load Context → Load Codebase Intelligence → Retrieve RAG Context
    ↓
Assemble Context → Plan Task → Dynamic Agent Selection
    ↓
Create Execution Graph → Execute Workflow → Agent/Tool Execution
    ↓
Code Changes → Build/Test/Validation → Review
    ↓
Commit → Push → Pull Request → Update Memory
                `}</pre>
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">Module Boundaries</h2>
              <div className="overflow-x-auto">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="text-left text-slate-400 border-b border-slate-700">
                      <th className="pb-2 pr-4">Module</th>
                      <th className="pb-2">Responsibility</th>
                    </tr>
                  </thead>
                  <tbody className="text-slate-300">
                    {modules.slice(0, 10).map(m => (
                      <tr key={m.name} className="border-b border-slate-800">
                        <td className="py-2 pr-4 font-medium text-blue-300">{m.name}</td>
                        <td className="py-2">{m.description}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'patterns' && (
          <div className="space-y-6">
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">Design Patterns Used</h2>
              <div className="grid md:grid-cols-2 gap-4">
                {designPatterns.map(dp => (
                  <div key={dp.pattern} className="bg-slate-700/30 rounded-lg p-4">
                    <h3 className="text-sm font-semibold text-blue-300 mb-1">{dp.pattern}</h3>
                    <p className="text-xs text-slate-400">{dp.usage}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">SOLID Principles</h2>
              <div className="space-y-3">
                {[
                  { letter: 'S', name: 'Single Responsibility', desc: 'Each service has one reason to change. KnowledgeService manages knowledge, MemoryService manages memory.' },
                  { letter: 'O', name: 'Open/Closed', desc: 'New tools, connectors, and agents can be added via configuration without modifying core code.' },
                  { letter: 'L', name: 'Liskov Substitution', desc: 'All connectors implement the same interface. Any adapter can replace another.' },
                  { letter: 'I', name: 'Interface Segregation', desc: 'CrudService, BaseRepository - clients depend only on methods they use.' },
                  { letter: 'D', name: 'Dependency Inversion', desc: 'Services depend on repository interfaces, not implementations. Spring DI manages wiring.' }
                ].map(solid => (
                  <div key={solid.letter} className="flex gap-3">
                    <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-500 rounded-lg flex items-center justify-center text-white font-bold text-sm shrink-0">
                      {solid.letter}
                    </div>
                    <div>
                      <h3 className="text-sm font-semibold text-white">{solid.name}</h3>
                      <p className="text-xs text-slate-400">{solid.desc}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">ACID Compliance</h2>
              <div className="grid md:grid-cols-2 gap-4">
                {[
                  { prop: 'Atomicity', desc: 'All service methods annotated with @Transactional ensure all-or-nothing execution' },
                  { prop: 'Consistency', desc: 'JPA validation, business rule checks, and state machine transitions ensure data consistency' },
                  { prop: 'Isolation', desc: 'Optimistic locking with @Version prevents concurrent modification conflicts' },
                  { prop: 'Durability', desc: 'PostgreSQL WAL and Flyway migrations ensure data persistence and schema evolution' }
                ].map(acid => (
                  <div key={acid.prop} className="bg-slate-700/30 rounded-lg p-4">
                    <h3 className="text-sm font-semibold text-green-300 mb-1">{acid.prop}</h3>
                    <p className="text-xs text-slate-400">{acid.desc}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'database' && (
          <div className="space-y-6">
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">Database Schema Overview</h2>
              <p className="text-sm text-slate-400 mb-4">
                PostgreSQL with Flyway migrations. All tables include audit fields (created_at, updated_at, created_by, updated_by) and optimistic locking (version).
              </p>
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-3">
                {[
                  'projects', 'agents', 'roles', 'skills', 'tools',
                  'connectors', 'prompts', 'knowledge_entries', 'memories',
                  'workflows', 'tasks', 'project_teams', 'git_repositories',
                  'rag_documents', 'codebase_projections', 'domain_events'
                ].map(table => (
                  <div key={table} className="bg-slate-700/30 rounded-lg px-3 py-2 flex items-center gap-2">
                    <span className="text-yellow-400 text-xs">⬡</span>
                    <span className="text-xs text-slate-300 font-mono">{table}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">Key Relationships</h2>
              <div className="font-mono text-xs text-slate-300 leading-relaxed bg-slate-900/50 rounded-lg p-4 overflow-x-auto">
                <pre>{`
Project 1──1 GitRepository
Project 1──1 ProjectTeam
Project 1──* Connector
Project 1──* Task
Project 1──* Memory
Project 1──1 CodebaseProjection
Project 1──* RagDocument

ProjectTeam *──* Agent (via project_team_agents)
ProjectTeam *──* Role (via project_team_roles)

Agent *──* Skill (via agent_skill_refs)
Agent *──* Tool  (via agent_tool_refs)

Connector *──* Capability (via connector_capabilities)

Task → Workflow (optional reference)
Tool → Connector (optional reference)
                `}</pre>
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">Infrastructure Databases</h2>
              <div className="grid md:grid-cols-3 gap-4">
                <div className="bg-slate-700/30 rounded-lg p-4">
                  <h3 className="text-sm font-semibold text-blue-300 mb-2">PostgreSQL</h3>
                  <p className="text-xs text-slate-400">Primary operational database for metadata, indexes, and configuration</p>
                </div>
                <div className="bg-slate-700/30 rounded-lg p-4">
                  <h3 className="text-sm font-semibold text-purple-300 mb-2">PgVector</h3>
                  <p className="text-xs text-slate-400">Vector embeddings for RAG similarity search (extension of PostgreSQL)</p>
                </div>
                <div className="bg-slate-700/30 rounded-lg p-4">
                  <h3 className="text-sm font-semibold text-green-300 mb-2">File System</h3>
                  <p className="text-xs text-slate-400">Workspace, .ai/ directory structure, runtime memory files</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-700/50 mt-12">
        <div className="max-w-7xl mx-auto px-6 py-6">
          <div className="flex items-center justify-between text-xs text-slate-500">
            <span>AI Platform Backend • Spring Boot 4.1 • Java 21</span>
            <span>SOLID • ACID • DDD • Clean Architecture</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
