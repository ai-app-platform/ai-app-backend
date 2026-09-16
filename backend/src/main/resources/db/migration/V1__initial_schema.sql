-- V1: Initial Schema for AI Platform
-- All tables follow DDD principles with proper indexing

-- ============================================
-- PROJECTS
-- ============================================
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    repository_url VARCHAR(1024) NOT NULL,
    default_branch VARCHAR(255) DEFAULT 'main',
    workspace_path VARCHAR(1024),
    connector_id UUID,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    configuration JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_projects_status ON projects(status);

-- ============================================
-- AGENTS
-- ============================================
CREATE TABLE agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    agent_type VARCHAR(50) NOT NULL,
    capabilities TEXT,
    default_prompt_ref VARCHAR(255),
    model_configuration JSONB,
    runtime_configuration JSONB,
    execution_policies JSONB,
    version_semantic VARCHAR(50) DEFAULT '1.0.0',
    version_latest BOOLEAN DEFAULT TRUE,
    version_sequence INTEGER DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE agent_skill_refs (
    agent_id UUID NOT NULL REFERENCES agents(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL,
    PRIMARY KEY (agent_id, skill_id)
);

CREATE TABLE agent_tool_refs (
    agent_id UUID NOT NULL REFERENCES agents(id) ON DELETE CASCADE,
    tool_id UUID NOT NULL,
    PRIMARY KEY (agent_id, tool_id)
);

CREATE INDEX idx_agents_name ON agents(name);
CREATE INDEX idx_agents_type ON agents(agent_type);
CREATE INDEX idx_agents_status ON agents(status);

-- ============================================
-- ROLES
-- ============================================
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    responsibility TEXT,
    constraints TEXT,
    prompt_policy_ref VARCHAR(255),
    skill_policy_ref VARCHAR(255),
    tool_policy_ref VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_roles_name ON roles(name);

-- ============================================
-- SKILLS
-- ============================================
CREATE TABLE skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(255),
    expertise_level VARCHAR(50) DEFAULT 'INTERMEDIATE',
    configuration JSONB,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_skills_name ON skills(name);

-- ============================================
-- TOOLS
-- ============================================
CREATE TABLE tools (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    tool_type VARCHAR(50) NOT NULL,
    capability VARCHAR(255) NOT NULL,
    input_schema JSONB,
    output_schema JSONB,
    permission_level VARCHAR(50) DEFAULT 'STANDARD',
    connector_id UUID,
    execution_policy JSONB,
    testing_configuration JSONB,
    version_semantic VARCHAR(50) DEFAULT '1.0.0',
    version_latest BOOLEAN DEFAULT TRUE,
    version_sequence INTEGER DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_tools_name ON tools(name);
CREATE INDEX idx_tools_type ON tools(tool_type);
CREATE INDEX idx_tools_capability ON tools(capability);

-- ============================================
-- CONNECTORS
-- ============================================
CREATE TABLE connectors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    connector_type VARCHAR(50) NOT NULL,
    provider VARCHAR(255) NOT NULL,
    endpoint VARCHAR(1024),
    repository_ref VARCHAR(1024),
    project_id UUID REFERENCES projects(id),
    credential_ref VARCHAR(512) NOT NULL,
    adapter_type VARCHAR(50) DEFAULT 'REST',
    connection_configuration JSONB,
    runtime_configuration JSONB,
    permission VARCHAR(50) NOT NULL DEFAULT 'READ_ONLY',
    connection_state VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    health_check_result JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE connector_capabilities (
    connector_id UUID NOT NULL REFERENCES connectors(id) ON DELETE CASCADE,
    capability VARCHAR(255) NOT NULL,
    PRIMARY KEY (connector_id, capability)
);

CREATE INDEX idx_connectors_provider ON connectors(provider);
CREATE INDEX idx_connectors_type ON connectors(connector_type);
CREATE INDEX idx_connectors_project ON connectors(project_id);

-- ============================================
-- PROMPTS
-- ============================================
CREATE TABLE prompts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    scope VARCHAR(50) NOT NULL DEFAULT 'PLATFORM',
    project_id UUID REFERENCES projects(id),
    template TEXT NOT NULL,
    variables JSONB,
    model_configuration JSONB,
    composition_rules JSONB,
    version_semantic VARCHAR(50) DEFAULT '1.0.0',
    version_latest BOOLEAN DEFAULT TRUE,
    version_sequence INTEGER DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_prompts_name ON prompts(name);
CREATE INDEX idx_prompts_scope ON prompts(scope);

-- ============================================
-- KNOWLEDGE ENTRIES
-- ============================================
CREATE TABLE knowledge_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(512) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(255),
    scope VARCHAR(50) NOT NULL DEFAULT 'PLATFORM',
    project_id UUID REFERENCES projects(id),
    workflow_id UUID,
    source_ref VARCHAR(1024),
    source_commit_sha VARCHAR(64),
    file_path VARCHAR(1024),
    priority INTEGER DEFAULT 0,
    tags TEXT,
    version_semantic VARCHAR(50) DEFAULT '1.0.0',
    version_latest BOOLEAN DEFAULT TRUE,
    version_sequence INTEGER DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_knowledge_scope ON knowledge_entries(scope);
CREATE INDEX idx_knowledge_project ON knowledge_entries(project_id);
CREATE INDEX idx_knowledge_category ON knowledge_entries(category);

-- ============================================
-- MEMORIES
-- ============================================
CREATE TABLE memories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    task_id UUID,
    memory_type VARCHAR(50) NOT NULL,
    title VARCHAR(512),
    content TEXT NOT NULL,
    file_path VARCHAR(1024),
    promoted BOOLEAN NOT NULL DEFAULT FALSE,
    summary TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_memories_project ON memories(project_id);
CREATE INDEX idx_memories_task ON memories(task_id);
CREATE INDEX idx_memories_type ON memories(memory_type);

-- ============================================
-- WORKFLOWS
-- ============================================
CREATE TABLE workflows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    scope VARCHAR(50) NOT NULL DEFAULT 'PLATFORM',
    project_id UUID REFERENCES projects(id),
    definition TEXT NOT NULL,
    planning_enabled BOOLEAN DEFAULT TRUE,
    agent_selection_strategy VARCHAR(50) DEFAULT 'dynamic',
    allow_parallel BOOLEAN DEFAULT TRUE,
    validation_required BOOLEAN DEFAULT TRUE,
    approval_required BOOLEAN DEFAULT FALSE,
    steps JSONB,
    retry_policy JSONB,
    failure_handling JSONB,
    version_semantic VARCHAR(50) DEFAULT '1.0.0',
    version_latest BOOLEAN DEFAULT TRUE,
    version_sequence INTEGER DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_workflows_name ON workflows(name);
CREATE INDEX idx_workflows_scope ON workflows(scope);
CREATE INDEX idx_workflows_project ON workflows(project_id);

-- ============================================
-- TASKS
-- ============================================
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    title VARCHAR(512) NOT NULL,
    description TEXT,
    task_type VARCHAR(50) DEFAULT 'SOFTWARE_DEVELOPMENT',
    task_status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    workflow_id UUID REFERENCES workflows(id),
    plan JSONB,
    selected_agents JSONB,
    execution_graph JSONB,
    execution_mode VARCHAR(50) DEFAULT 'AUTOMATIC',
    task_branch VARCHAR(255),
    base_commit_sha VARCHAR(64),
    result JSONB,
    execution_trace JSONB,
    priority INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_status ON tasks(task_status);
CREATE INDEX idx_tasks_workflow ON tasks(workflow_id);

-- ============================================
-- PROJECT TEAMS
-- ============================================
CREATE TABLE project_teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL UNIQUE REFERENCES projects(id),
    name VARCHAR(255),
    version_semantic VARCHAR(50) DEFAULT '1.0.0',
    version_latest BOOLEAN DEFAULT TRUE,
    version_sequence INTEGER DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE project_team_agents (
    team_id UUID NOT NULL REFERENCES project_teams(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    PRIMARY KEY (team_id, agent_id)
);

CREATE TABLE project_team_roles (
    team_id UUID NOT NULL REFERENCES project_teams(id) ON DELETE CASCADE,
    role_id UUID NOT NULL,
    PRIMARY KEY (team_id, role_id)
);

CREATE TABLE project_team_disabled_agents (
    team_id UUID NOT NULL REFERENCES project_teams(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    PRIMARY KEY (team_id, agent_id)
);

-- ============================================
-- GIT REPOSITORIES
-- ============================================
CREATE TABLE git_repositories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL UNIQUE REFERENCES projects(id),
    connector_id UUID,
    remote_url VARCHAR(1024) NOT NULL,
    local_path VARCHAR(1024),
    default_branch VARCHAR(255) DEFAULT 'main',
    current_branch VARCHAR(255),
    last_commit_sha VARCHAR(64),
    last_fetch_at TIMESTAMP WITH TIME ZONE,
    clone_status VARCHAR(50) DEFAULT 'NOT_CLONED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_git_project ON git_repositories(project_id);
CREATE INDEX idx_git_connector ON git_repositories(connector_id);

-- ============================================
-- RAG DOCUMENTS
-- ============================================
CREATE TABLE rag_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    source_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(1024),
    symbol VARCHAR(255),
    line_range_start INTEGER,
    line_range_end INTEGER,
    content TEXT NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    commit_sha VARCHAR(64),
    branch VARCHAR(255),
    chunk_index INTEGER,
    metadata JSONB,
    embedding_model_version VARCHAR(50),
    parser_version VARCHAR(50),
    index_version VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_rag_project ON rag_documents(project_id);
CREATE INDEX idx_rag_source ON rag_documents(source_type);
CREATE INDEX idx_rag_commit ON rag_documents(commit_sha);

-- ============================================
-- CODEBASE PROJECTIONS
-- ============================================
CREATE TABLE codebase_projections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL UNIQUE REFERENCES projects(id),
    overview TEXT,
    architecture TEXT,
    modules TEXT,
    dependencies TEXT,
    conventions TEXT,
    code_graph JSONB,
    source_commit_sha VARCHAR(64),
    parser_version VARCHAR(50),
    generator_version VARCHAR(50),
    generated_at TIMESTAMP WITH TIME ZONE,
    full_index BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_codebase_project ON codebase_projections(project_id);

-- ============================================
-- WORKSPACES
-- ============================================
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL UNIQUE REFERENCES projects(id),
    base_path VARCHAR(1024) NOT NULL,
    current_task_branch VARCHAR(255),
    workspace_status VARCHAR(50) DEFAULT 'INITIALIZED',
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_workspaces_project ON workspaces(project_id);

-- ============================================
-- DOMAIN EVENTS (Outbox Pattern)
-- ============================================
CREATE TABLE domain_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    occurred_by VARCHAR(255),
    processed BOOLEAN DEFAULT FALSE,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_events_aggregate ON domain_events(aggregate_type, aggregate_id);
CREATE INDEX idx_events_processed ON domain_events(processed);

-- ============================================
-- OUTBOX EVENTS (Transactional Outbox Pattern)
-- ============================================
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    occurred_by VARCHAR(255),
    processed BOOLEAN DEFAULT FALSE,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_processed ON outbox_events(processed);
