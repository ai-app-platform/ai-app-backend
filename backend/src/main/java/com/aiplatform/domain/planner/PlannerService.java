package com.aiplatform.domain.planner;

import com.aiplatform.domain.task.Task;
import com.aiplatform.domain.team.ProjectTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Planner analyzes a Task and creates an execution Plan.
 * Plan includes: Steps, Required Capabilities, Required Agents, Dependencies,
 * Parallelizable Steps, Validation, Expected Outputs, Handoffs.
 * 
 * Key constraint: Planner can only select agents from the Project Team.
 */
@Service
@RequiredArgsConstructor
public class PlannerService {

    private final ProjectTeamService projectTeamService;

    /**
     * Creates an execution plan for the given task.
     * The plan maps steps to agents available in the Project Team.
     */
    public ExecutionPlan createPlan(UUID projectId, Task task) {
        // Get available agents from the project team
        Set<UUID> availableAgents = projectTeamService.getEnabledAgents(projectId);

        // In a real implementation, this would:
        // 1. Analyze the task description and requirements
        // 2. Use LLM to break down the task into steps
        // 3. Identify required capabilities for each step
        // 4. Map capabilities to available agents
        // 5. Determine dependencies and parallelizable steps
        // 6. Define validation criteria
        // 7. Define handoffs between agents

        ExecutionPlan plan = new ExecutionPlan();
        plan.setTaskId(task.getId());
        plan.setProjectId(projectId);
        plan.setAvailableAgents(availableAgents);
        plan.setStatus(PlanStatus.CREATED);

        return plan;
    }

    /**
     * Selects agents for a specific step based on required capabilities.
     * Only selects from the Project Team - never outside.
     */
    public Set<UUID> selectAgentsForStep(UUID projectId, Set<String> requiredCapabilities) {
        Set<UUID> availableAgents = projectTeamService.getEnabledAgents(projectId);

        // In a real implementation, this would:
        // 1. Look up agent capabilities
        // 2. Match required capabilities to agent capabilities
        // 3. Return the minimum set of agents needed

        return availableAgents;
    }

    /**
     * Re-plans based on execution feedback (e.g., review rejection).
     */
    public ExecutionPlan replan(ExecutionPlan originalPlan, String feedback) {
        ExecutionPlan newPlan = new ExecutionPlan();
        newPlan.setTaskId(originalPlan.getTaskId());
        newPlan.setProjectId(originalPlan.getProjectId());
        newPlan.setAvailableAgents(originalPlan.getAvailableAgents());
        newPlan.setStatus(PlanStatus.REPLANNED);
        newPlan.setFeedback(feedback);
        return newPlan;
    }

    public enum PlanStatus {
        CREATED,
        APPROVED,
        EXECUTING,
        COMPLETED,
        FAILED,
        REPLANNED
    }

    @lombok.Getter
    @lombok.Setter
    public static class ExecutionPlan {
        private UUID taskId;
        private UUID projectId;
        private Set<UUID> availableAgents;
        private List<PlanStep> steps = new ArrayList<>();
        private PlanStatus status;
        private String feedback;
        private Map<String, Object> metadata = new HashMap<>();
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.Builder
    public static class PlanStep {
        private String stepId;
        private String description;
        private Set<UUID> assignedAgents;
        private Set<String> requiredCapabilities;
        private List<String> dependencies;
        private boolean parallelizable;
        private String validationCriteria;
        private String expectedOutput;
        private int order;
    }
}
