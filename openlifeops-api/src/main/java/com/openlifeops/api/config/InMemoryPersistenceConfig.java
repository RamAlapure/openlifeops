package com.openlifeops.api.config;

import com.openlifeops.core.event.DomainEventPublisher;
import com.openlifeops.core.event.InMemoryDomainEventPublisher;
import com.openlifeops.core.pack.OpenLifeOpsPack;
import com.openlifeops.evidence.EvidenceStore;
import com.openlifeops.evidence.InMemoryEvidenceStore;
import com.openlifeops.governance.DefaultPolicyEngine;
import com.openlifeops.governance.PolicyEngine;
import com.openlifeops.mcp.ToolRegistry;
import com.openlifeops.knowledge.InMemoryKnowledgeService;
import com.openlifeops.knowledge.KnowledgeService;
import com.openlifeops.orchestrator.OpenLifeOps;
import com.openlifeops.packs.tax.TaxPack;
import com.openlifeops.packs.tax.TaxReconciliationToolRegistry;
import com.openlifeops.runtime.ExecutionEngine;
import com.openlifeops.runtime.Executor;
import com.openlifeops.runtime.TaskManager;
import com.openlifeops.runtime.pack.PackRegistry;
import com.openlifeops.runtime.knowledge.KnowledgeEnhancingToolRegistry;
import com.openlifeops.runtime.planning.Planner;
import com.openlifeops.runtime.planning.RuleBasedPlanner;
import com.openlifeops.runtime.store.ApprovalStore;
import com.openlifeops.runtime.store.ExecutionStore;
import com.openlifeops.runtime.store.InMemoryApprovalStore;
import com.openlifeops.runtime.store.InMemoryExecutionStore;
import com.openlifeops.runtime.store.InMemoryObservationStore;
import com.openlifeops.runtime.store.InMemoryPlanStore;
import com.openlifeops.runtime.store.InMemoryTaskStore;
import com.openlifeops.runtime.store.ObservationStore;
import com.openlifeops.runtime.store.PlanStore;
import com.openlifeops.runtime.store.TaskStore;
import com.openlifeops.runtime.validation.DefaultValidator;
import com.openlifeops.runtime.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile({"in-memory", "mcp", "mongo"})
public class InMemoryPersistenceConfig {

    @Bean
    TaskStore taskStore() {
        return new InMemoryTaskStore();
    }

    @Bean
    ExecutionStore executionStore() {
        return new InMemoryExecutionStore();
    }

    @Bean
    PlanStore planStore() {
        return new InMemoryPlanStore();
    }

    @Bean
    ApprovalStore approvalStore() {
        return new InMemoryApprovalStore();
    }

    @Bean
    ObservationStore observationStore() {
        return new InMemoryObservationStore();
    }

    @Bean
    EvidenceStore evidenceStore() {
        return new InMemoryEvidenceStore();
    }

    @Bean
    DomainEventPublisher domainEventPublisher() {
        return new InMemoryDomainEventPublisher();
    }

    @Bean
    OpenLifeOpsPack taxPack() {
        return new TaxPack();
    }

    @Bean
    PackRegistry packRegistry(List<OpenLifeOpsPack> packs) {
        PackRegistry registry = new PackRegistry();
        for (OpenLifeOpsPack pack : packs) {
            registry.register(pack);
        }
        return registry;
    }

    @Bean
    PolicyEngine policyEngine(PackRegistry packRegistry) {
        List<com.openlifeops.core.descriptor.PolicyDefinition> policies = new ArrayList<>();
        for (OpenLifeOpsPack pack : packRegistry.all()) {
            policies.addAll(pack.policies());
        }
        return new DefaultPolicyEngine(policies);
    }

    @Bean
    Planner planner() {
        return new RuleBasedPlanner();
    }

    @Bean
    Validator validator() {
        return new DefaultValidator();
    }

    @Bean
    KnowledgeService knowledgeService() {
        return new InMemoryKnowledgeService();
    }

    @Bean
    @Primary
    ToolRegistry toolRegistry(
            @Qualifier("baseToolRegistry") ToolRegistry baseToolRegistry,
            KnowledgeService knowledgeService) {
        ToolRegistry knowledgeAwareRegistry = new KnowledgeEnhancingToolRegistry(baseToolRegistry, knowledgeService);
        return new TaxReconciliationToolRegistry(knowledgeAwareRegistry, knowledgeService);
    }

    @Bean
    Executor executor(ToolRegistry toolRegistry, ObservationStore observationStore, EvidenceStore evidenceStore) {
        return new Executor(toolRegistry, observationStore, evidenceStore);
    }

    @Bean
    ExecutionEngine executionEngine(
            PolicyEngine policyEngine,
            Executor executor,
            Validator validator,
            TaskStore taskStore,
            ExecutionStore executionStore,
            ApprovalStore approvalStore,
            DomainEventPublisher domainEventPublisher) {
        return new ExecutionEngine(
                policyEngine,
                executor,
                validator,
                taskStore,
                executionStore,
                approvalStore,
                domainEventPublisher);
    }

    @Bean
    TaskManager taskManager(
            TaskStore taskStore,
            ExecutionStore executionStore,
            PlanStore planStore,
            ApprovalStore approvalStore,
            PackRegistry packRegistry,
            Planner planner,
            ExecutionEngine executionEngine,
            DomainEventPublisher domainEventPublisher) {
        return new TaskManager(
                taskStore,
                executionStore,
                planStore,
                approvalStore,
                packRegistry,
                planner,
                executionEngine,
                domainEventPublisher);
    }

    @Bean
    OpenLifeOps openLifeOps(TaskManager taskManager) {
        return new OpenLifeOps(taskManager);
    }
}
