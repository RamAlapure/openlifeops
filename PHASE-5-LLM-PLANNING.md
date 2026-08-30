# OpenLifeOps Phase 5 — LLM Planning (Infrastructure Ready)

## Status: infrastructure prepared, implementation deferred

Phase 5 is designed to add intelligent planning using Spring AI ChatClient, but implementation has been deferred to focus on more immediate prototype enhancements.

## Infrastructure Prepared

The following infrastructure has been created to support future AI planning:

- **Planner Interface**: Existing `Planner` interface allows multiple implementations
- **RuleBasedPlanner**: Current deterministic planner serves as fallback
- **Dependency Structure**: Runtime module prepared for optional Spring AI ChatClient dependency
- **Configuration**: Application.yml ready for OpenAI API key and model configuration

## When to Implement

Consider implementing LLM planning when:

1. **Spring AI Dependencies Available**: The Spring AI OpenAI starter is available in your environment
2. **Model Access**: You have access to OpenAI API keys or other compatible models
3. **Complex Workflows**: You need dynamic plan generation beyond the current tax pack
4. **Natural Language Objectives**: Users want to describe tasks in natural language rather than structured packs

## Planned Implementation

When implemented, Phase 5 will include:

1. **IntelligentPlanner**: AI-powered planner using Spring AI ChatClient
2. **Structured Output**: Parse AI responses into valid Plan structures
3. **PlannerSelector**: Choose between rule-based and AI planning based on configuration
4. **Fallback Logic**: Graceful degradation to rule-based planner when AI unavailable
5. **Configuration**: Model selection, prompts, and API key management
6. **Observability**: Log planning decisions and AI reasoning
7. **Safety**: Validate AI-generated plans against pack constraints

## Current State

The system currently uses **deterministic rule-based planning** exclusively:

- ✅ Workflow steps are defined in pack configurations
- ✅ Plan generation is predictable and testable
- ✅ No external dependencies required
- ✅ Fast and reliable for current tax pack use case

## Documentation Placeholder

When Phase 5 is implemented, this document will be expanded with:

- Detailed implementation steps
- Configuration examples
- Prompt engineering guidelines
- Integration test examples
- Performance considerations
- Cost estimation for AI API usage

## Related Phases

- **Phase 2**: Real Runtime (complete) - provides the execution engine
- **Phase 3**: MCP Tool Runtime (complete) - provides tool execution
- **Phase 4**: Knowledge (enhanced) - provides document context for planning
- **Phase 6**: Tax Reconciliation (complete) - demonstrates current planning capabilities

The current rule-based planner successfully handles the tax pack's 3-step workflow, demonstrating that intelligent planning is not required for the prototype's immediate needs.