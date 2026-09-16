import { useState } from 'react';

const apiEndpoints = [
  { category: 'Dashboard', endpoints: ['GET /dashboard/stats', 'GET /dashboard/activities'] },
  { category: 'Projects', endpoints: ['GET /projects', 'GET /projects/{id}', 'POST /projects', 'PUT /projects/{id}', 'DELETE /projects/{id}'] },
  { category: 'Tasks', endpoints: ['GET /tasks', 'GET /tasks/{id}', 'POST /tasks', 'PATCH /tasks/{id}/status', 'POST /tasks/{id}/execute', 'POST /tasks/{id}/complete'] },
  { category: 'Agents', endpoints: ['GET /agents', 'GET /agents/{id}', 'POST /agents', 'PUT /agents/{id}'] },
  { category: 'Tools', endpoints: ['GET /tools', 'GET /tools/{id}', 'POST /tools'] },
  { category: 'Connectors', endpoints: ['GET /connectors', 'GET /connectors/{id}', 'POST /connectors', 'POST /connectors/{id}/test'] },
  { category: 'Knowledge', endpoints: ['GET /knowledge', 'GET /knowledge/{id}', 'POST /knowledge'] },
  { category: 'Workflows', endpoints: ['GET /workflows', 'GET /workflows/{id}', 'POST /workflows'] },
  { category: 'Roles', endpoints: ['GET /roles', 'GET /roles/{id}', 'POST /roles'] },
  { category: 'Skills', endpoints: ['GET /skills', 'GET /skills/{id}', 'POST /skills'] },
  { category: 'Teams', endpoints: ['GET /teams', 'GET /projects/{id}/team', 'POST /teams', 'PUT /teams/{id}'] },
  { category: 'Prompts', endpoints: ['GET /prompts', 'GET /prompts/{id}', 'POST /prompts'] },
  { category: 'Memory', endpoints: ['GET /projects/{id}/memory', 'GET /memory/{id}'] },
  { category: 'RAG', endpoints: ['GET /projects/{id}/rag/status', 'POST /projects/{id}/rag/search'] },
  { category: 'Git', endpoints: ['GET /projects/{id}/git/branches', 'GET /projects/{id}/git/commits', 'POST /projects/{id}/git/branches', 'GET /projects/{id}/git/diff', 'GET /projects/{id}/git/pull-requests'] },
  { category: 'Codebase', endpoints: ['GET /projects/{id}/codebase/overview', 'GET /projects/{id}/codebase/modules', 'GET /projects/{id}/codebase/modules/{name}', 'POST /projects/{id}/codebase/search', 'GET /projects/{id}/codebase/symbols/{name}', 'GET /projects/{id}/codebase/files'] },
  { category: 'Context Engine', endpoints: ['GET /tasks/{id}/context'] },
  { category: 'Workspace', endpoints: ['GET /workspaces', 'GET /workspaces/{name}', 'GET /workspaces/{name}/files', 'POST /workspaces/{name}/execute'] },
  { category: 'Settings', endpoints: ['GET /settings/database', 'POST /settings/database/test', 'POST /settings/database/backup', 'GET /settings/api-keys', 'POST /settings/api-keys', 'DELETE /settings/api-keys/{id}', 'GET /settings/infrastructure'] },
];

const modules = [
  { name: 'Project Management', desc: 'Project CRUD, Git clone, workspace init, auto-indexing', icon: '📁' },
  { name: 'Task Orchestration', desc: 'Full task lifecycle, branch creation, agent execution, PR', icon: '📋' },
  { name: 'Agent Management', desc: 'Agent definitions, capabilities, model config', icon: '🤖' },
  { name: 'Tool Management', desc: 'Tool definitions, schemas, capabilities, permissions', icon: '🔧' },
  { name: 'Connector Management', desc: 'External connections, adapters, credentials', icon: '🔌' },
  { name: 'Knowledge Management', desc: 'Hierarchical resolution, versioned entries', icon: '📚' },
  { name: 'Memory (Short-term)', desc: '.ai/ directory, markdown files, runtime state', icon: '🧠' },
  { name: 'Memory (Long-term)', desc: 'RAG, vector DB, embeddings, semantic search', icon: '💾' },
  { name: 'Workflow Management', desc: 'Process definitions, steps, validation', icon: '⚡' },
  { name: 'Role & Skill', desc: 'Role constraints, skill expertise levels', icon: '🎭' },
  { name: 'Team Management', desc: 'Project teams, agent enable/disable', icon: '👥' },
  { name: 'Prompt Management', desc: 'Templates, variables, composition', icon: '📝' },
  { name: 'Git Management', desc: 'Clone, branch, commit, push, PR via JGit', icon: '🌿' },
  { name: 'RAG Engine', desc: 'Java parsing, chunking, embedding, PgVector', icon: '🔍' },
  { name: 'Codebase Intelligence', desc: 'AST analysis, code graph, projections', icon: '🗺️' },
  { name: 'Context Engine', desc: 'Local-first retrieval, layered assembly', icon: '🎯' },
  { name: 'Planner', desc: 'Task analysis, plan creation, agent selection', icon: '📐' },
  { name: 'Workspace', desc: 'Runtime working copy, file ops, command exec', icon: '🏗️' },
];

const ragFeatures = [
  { title: 'Java File Parsing', desc: 'Extracts packages, imports, classes, methods, dependencies, callers' },
  { title: 'Code Graph', desc: 'Dependency graph + caller relationships stored in vector DB' },
  { title: 'Intelligent Chunking', desc: 'Line-boundary-aware chunking with configurable overlap' },
  { title: 'Vector Embeddings', desc: 'OpenAI text-embedding-3-small via Spring AI' },
  { title: 'PgVector Storage', desc: 'HNSW index, cosine distance, metadata filtering' },
  { title: 'Hybrid Search', desc: 'Semantic + lexical search with reranking' },
  { title: 'Incremental Indexing', desc: 'Only re-index changed files via git diff' },
  { title: 'Symbol Search', desc: 'Find classes, methods, fields by name with context' },
];

const shortTermMemory = [
  { file: 'overview.md', desc: 'Project overview with statistics' },
  { file: 'architecture.md', desc: 'Architecture description' },
  { file: 'modules.md', desc: 'Module breakdown with packages' },
  { file: 'dependencies.md', desc: 'Dependency graph between classes' },
  { file: 'conventions.md', desc: 'Coding conventions and standards' },
  { file: 'stack.md', desc: 'Technology stack from pom.xml' },
  { file: 'structure.md', desc: 'File/directory structure tree' },
  { file: 'classes.md', desc: 'Complete class inventory with methods' },
  { file: 'runtime/current-task.md', desc: 'Current task being executed' },
  { file: 'runtime/current-plan.md', desc: 'Execution plan steps' },
  { file: 'runtime/discoveries.md', desc: 'Discoveries during execution' },
  { file: 'runtime/decisions.md', desc: 'Decisions made by agents' },
  { file: 'runtime/task-summary.md', desc: 'Task completion summary' },
];

function App() {
  const [activeTab, setActiveTab] = useState('overview');
  const [expandedCategory, setExpandedCategory] = useState<string | null>(null);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white" dir="rtl">
      {/* Header */}
      <header className="border-b border-slate-700/50 backdrop-blur-sm bg-slate-900/80 sticky top-0 z-50">
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
                <p className="text-xs text-slate-400">Spring Boot 4.1 • Java 21 • PostgreSQL • PgVector</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="px-3 py-1 bg-green-500/20 text-green-400 text-xs font-medium rounded-full border border-green-500/30">
                ● Complete
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
            {[
              { id: 'overview', label: 'نمای کلی' },
              { id: 'modules', label: 'ماژول‌ها' },
              { id: 'apis', label: 'APIها' },
              { id: 'rag', label: 'RAG Engine' },
              { id: 'memory', label: 'حافظه' },
              { id: 'flow', label: 'جریان کار' },
            ].map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-4 py-3 text-sm font-medium whitespace-nowrap transition-all ${
                  activeTab === tab.id
                    ? 'text-blue-400 border-b-2 border-blue-400'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {tab.label}
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
                { label: 'ماژول دامنه', value: '18', icon: '📦' },
                { label: 'API Endpoint', value: '60+', icon: '🔌' },
                { label: 'جدول دیتابیس', value: '22', icon: '🗄️' },
                { label: 'سرویس', value: '20+', icon: '⚙️' }
              ].map(stat => (
                <div key={stat.label} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-4">
                  <div className="text-2xl mb-1">{stat.icon}</div>
                  <div className="text-2xl font-bold text-white">{stat.value}</div>
                  <div className="text-xs text-slate-400">{stat.label}</div>
                </div>
              ))}
            </div>

            {/* Key Features */}
            <div className="grid md:grid-cols-2 gap-4">
              <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
                <h2 className="text-lg font-semibold text-white mb-4">✅ ویژگی‌های پیاده‌سازی شده</h2>
                <ul className="space-y-2 text-sm text-slate-300">
                  <li>✓ Clone خودکار Git هنگام ایجاد پروژه</li>
                  <li>✓ ایندکس کامل پروژه در RAG (حافظه بلندمدت)</li>
                  <li>✓ تولید فایل‌های .ai/ (حافظه کوتاه‌مدت)</li>
                  <li>✓ Parse فایل‌های جاوا (AST-based)</li>
                  <li>✓ Code Graph با وابستگی‌ها و callerها</li>
                  <li>✓ Chunking و Embedding با Spring AI</li>
                  <li>✓ ذخیره در PgVector</li>
                  <li>✓ ایجاد branch برای هر task</li>
                  <li>✓ Commit و Push تغییرات</li>
                  <li>✓ ایجاد Pull Request</li>
                  <li>✓ Task lifecycle کامل</li>
                  <li>✓ Context Engine با local-first retrieval</li>
                </ul>
              </div>
              <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
                <h2 className="text-lg font-semibold text-white mb-4">🏗️ اصول معماری</h2>
                <ul className="space-y-2 text-sm text-slate-300">
                  <li>◆ Git/Files = Source of Truth</li>
                  <li>◆ Database = Metadata/Index</li>
                  <li>◆ RAG = Retrieval (نه Source of Truth)</li>
                  <li>◆ Knowledge ≠ Memory</li>
                  <li>◆ Agent ≠ Tool ≠ Connector</li>
                  <li>◆ Team ≠ Task (تیم در سطح پروژه)</li>
                  <li>◆ Planner فقط از Team انتخاب می‌کند</li>
                  <li>◆ Configuration-driven و Extensible</li>
                  <li>◆ SOLID و DDD</li>
                  <li>◆ ACID Compliance</li>
                  <li>◆ Versioned و Reproducible</li>
                  <li>◆ No Code Smell, DRY</li>
                </ul>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'modules' && (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {modules.map((mod, i) => (
              <div key={mod.name} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-5 hover:border-blue-500/50 transition-all">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-2xl">{mod.icon}</span>
                  <h3 className="font-semibold text-white text-sm">{mod.name}</h3>
                </div>
                <p className="text-xs text-slate-400">{mod.desc}</p>
              </div>
            ))}
          </div>
        )}

        {activeTab === 'apis' && (
          <div className="space-y-3">
            {apiEndpoints.map(api => (
              <div key={api.category} className="bg-slate-800/50 border border-slate-700/50 rounded-xl overflow-hidden">
                <button
                  onClick={() => setExpandedCategory(expandedCategory === api.category ? null : api.category)}
                  className="w-full px-5 py-3 flex items-center justify-between text-right hover:bg-slate-700/30 transition-all"
                >
                  <span className="font-semibold text-white text-sm">{api.category}</span>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-slate-400">{api.endpoints.length} endpoint</span>
                    <span className={`text-slate-400 transition-transform ${expandedCategory === api.category ? 'rotate-180' : ''}`}>▼</span>
                  </div>
                </button>
                {expandedCategory === api.category && (
                  <div className="px-5 pb-3 border-t border-slate-700/50">
                    <div className="grid gap-1 mt-2">
                      {api.endpoints.map(ep => (
                        <div key={ep} className="font-mono text-xs text-slate-300 bg-slate-900/50 rounded px-3 py-1.5">
                          {ep}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {activeTab === 'rag' && (
          <div className="space-y-6">
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">🔍 RAG Engine Features</h2>
              <div className="grid md:grid-cols-2 gap-4">
                {ragFeatures.map(f => (
                  <div key={f.title} className="bg-slate-700/30 rounded-lg p-4">
                    <h3 className="text-sm font-semibold text-blue-300 mb-1">{f.title}</h3>
                    <p className="text-xs text-slate-400">{f.desc}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">📊 Indexing Flow</h2>
              <div className="font-mono text-xs text-slate-300 leading-relaxed bg-slate-900/50 rounded-lg p-4 overflow-x-auto" dir="ltr">
                <pre>{`
Java Files → Parse (AST) → Extract:
  ├── Package
  ├── Imports
  ├── Classes (name, methods, dependencies)
  ├── Methods (signature, callers, line range)
  └── Fields (type, name)
       ↓
  Index File Structure → Vector Store
  Index Classes → Vector Store
  Index Methods → Vector Store
  Chunk Content → Vector Store
  Build Code Graph → Vector Store
       ↓
  Search: Query → Embedding → Similarity → Results
                `}</pre>
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">🗃️ Vector Storage</h2>
              <div className="grid md:grid-cols-3 gap-4">
                <div className="bg-slate-700/30 rounded-lg p-4">
                  <h3 className="text-sm font-semibold text-green-300 mb-2">PgVector</h3>
                  <p className="text-xs text-slate-400">PostgreSQL extension for vector similarity search</p>
                </div>
                <div className="bg-slate-700/30 rounded-lg p-4">
                  <h3 className="text-sm font-semibold text-purple-300 mb-2">HNSW Index</h3>
                  <p className="text-xs text-slate-400">Hierarchical Navigable Small World for fast retrieval</p>
                </div>
                <div className="bg-slate-700/30 rounded-lg p-4">
                  <h3 className="text-sm font-semibold text-blue-300 mb-2">Cosine Distance</h3>
                  <p className="text-xs text-slate-400">Semantic similarity measurement</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'memory' && (
          <div className="space-y-6">
            <div className="grid md:grid-cols-2 gap-6">
              <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
                <h2 className="text-lg font-semibold text-white mb-4">🧠 Short-term Memory (.ai/)</h2>
                <p className="text-xs text-slate-400 mb-4">
                  فایل‌های markdown در پوشه .ai/ که AI بدون خواندن کل پروژه، آن را درک می‌کند
                </p>
                <div className="space-y-2">
                  {shortTermMemory.map(m => (
                    <div key={m.file} className="flex items-start gap-2 text-xs">
                      <code className="text-green-300 bg-slate-900/50 px-2 py-0.5 rounded font-mono shrink-0">{m.file}</code>
                      <span className="text-slate-400">{m.desc}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
                <h2 className="text-lg font-semibold text-white mb-4">💾 Long-term Memory (RAG)</h2>
                <p className="text-xs text-slate-400 mb-4">
                  حافظه بلندمدت در vector database برای جستجوی معنایی
                </p>
                <div className="space-y-3">
                  <div className="bg-slate-700/30 rounded-lg p-3">
                    <h3 className="text-xs font-semibold text-blue-300 mb-1">Indexed Content</h3>
                    <ul className="text-xs text-slate-400 space-y-1">
                      <li>• Source code (Java files)</li>
                      <li>• Class definitions</li>
                      <li>• Method signatures</li>
                      <li>• Code graph (dependencies)</li>
                      <li>• Documentation</li>
                      <li>• Knowledge entries</li>
                    </ul>
                  </div>
                  <div className="bg-slate-700/30 rounded-lg p-3">
                    <h3 className="text-xs font-semibold text-purple-300 mb-1">Search Capabilities</h3>
                    <ul className="text-xs text-slate-400 space-y-1">
                      <li>• Semantic similarity search</li>
                      <li>• Metadata filtering</li>
                      <li>• Symbol lookup</li>
                      <li>• Code graph traversal</li>
                      <li>• Hybrid retrieval</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'flow' && (
          <div className="space-y-6">
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">🔄 Task Lifecycle</h2>
              <div className="font-mono text-xs text-slate-300 leading-relaxed bg-slate-900/50 rounded-lg p-4 overflow-x-auto" dir="ltr">
                <pre>{`
1. Create Project
   ├── Clone Git Repository (JGit)
   ├── Initialize Workspace
   ├── Generate .ai/ files (short-term memory)
   └── Index in RAG (long-term memory)
        ↓
2. Create Task
   ├── Create task branch from main
   ├── Initialize runtime memory
   └── Set task status to PLANNING
        ↓
3. Planning Phase
   ├── Analyze task requirements
   ├── Create execution plan
   └── Identify required capabilities
        ↓
4. Agent Selection
   ├── Select from Project Team (not outside!)
   ├── Map capabilities to agents
   └── Create execution graph
        ↓
5. Execution
   ├── Context Engine assembles context
   ├── Agents execute with LLM
   ├── Tool calls via Tool Executor
   └── Changes applied to workspace
        ↓
6. Validation
   ├── Build verification
   ├── Test execution
   └── Security scan
        ↓
7. Review
   ├── Code review by reviewer agent
   └── Approval if needed
        ↓
8. Commit & Push
   ├── Commit changes to task branch
   └── Push to remote
        ↓
9. Create Pull Request
   └── PR from task branch to main
        ↓
10. Complete Task
    ├── Update runtime memory
    ├── Promote discoveries to long-term
    └── Mark task as COMPLETED
                `}</pre>
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6">
              <h2 className="text-lg font-semibold text-white mb-4">🔗 Data Flow</h2>
              <div className="font-mono text-xs text-slate-300 leading-relaxed bg-slate-900/50 rounded-lg p-4 overflow-x-auto" dir="ltr">
                <pre>{`
Agent → Tool Definition → Tool Executor → Connector → Adapter → External System
  ↑                                                         ↓
  └── Context Engine ← Knowledge + Memory + Codebase + RAG ←┘

Task → Planner → Agent Selection → LangGraph → Agent Runtime → LLM
  ↑                                                         ↓
  └── Git Management ← Workspace ← Tool Execution ←────────┘

Project → Git Clone → Workspace → .ai/ (short-term) → RAG (long-term)
                `}</pre>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-700/50 mt-12">
        <div className="max-w-7xl mx-auto px-6 py-6">
          <div className="flex items-center justify-between text-xs text-slate-500">
            <span>AI Platform Backend • Spring Boot 4.1 • Java 21 • PostgreSQL</span>
            <span>SOLID • ACID • DDD • Clean Architecture • DRY</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
