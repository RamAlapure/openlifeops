# OpenLifeOps — An open operating system for AI-powered life workflows

> **Plan. Act. Verify. Ask when it matters.**

Shared foundation for every vertical in this folder. One **OpenLifeOps Runtime**, many **OpenLifeOps Packs**. Not ten chatbots.

## Terminology

| Term | Meaning |
| --- | --- |
| **OpenLifeOps** | The project / ecosystem |
| **OpenLifeOps Runtime** | The operating framework (modular monolith) |
| **OpenLifeOps Packs** | Tax, Consumer, Finance, Family, … |
| **OpenLifeOps Agents** | Specialized agents inside a pack |

## Mission

Give an ordinary person 70–90% of the value of an expensive human service (analyst, researcher, negotiator, admin) at near-zero marginal cost.

A portfolio line that matters:

> Give the agent a goal; it researches, gathers documents, reasons, calls APIs/browser tools, produces an evidence-backed decision, asks for approval when necessary, and completes the task.

## Opportunity map

| Area | Current human service | Consumer pain | AI leverage |
| --- | --- | --- | --- |
| Tax | CA / tax consultant | High | Very high |
| Legal | Lawyer / paralegal | Very high | High |
| Personal finance | Advisor / research analyst | High | Very high |
| Consumer complaints | Support / lawyer | High | Very high |
| Education | Tutor | Very high | Very high |
| Home services | Coordinator / technician | High | Medium–high |
| Travel | Travel agent / concierge | Medium–high | Very high |
| SMB administration | Accountant / admin / ops | Very high | Very high |
| Career | Coach / recruiter | High | Very high |
| Healthcare navigation | Coordinator | Very high | High |
| Insurance | Agent / claims consultant | Very high | Very high |
| Real estate | Broker / agent | Very high | High |

## Architecture

Workflow + evidence + policy–centric. LLMs are reasoning components behind a gateway — not the spine of the system.

```
USER
  │  natural language
  ▼
AI Gateway                    ← Spring AI (Phase 5)
  ▼
OpenLifeOps Runtime
  Task → Execution → Step → Action
        │
   ┌────┴────┐
   ▼         ▼
Policy    Executor
   │         │
Approval   Tool / MCP Layer
   │         │
   └────┬────┘
        ▼
   Evidence (immutable, append-only)
        ▼
   Human Approval
```

### Domain spine (Phase 1)

| Concept | Role |
| --- | --- |
| **Task** | What the user asked for |
| **Execution** | One attempt/run of a Task (retry/resume later) |
| **Step** | Unit inside an Execution |
| **Action** | What governance evaluates before execution |
| **Evidence** | Immutable claim with provenance; append only |
| **Approval** | Human decision on a pending Action |

Public facade: `TaskExecution execute(TaskRequest request)`.

### Module layout (`openlifeops/`)

```text
openlifeops-core           # domain, events, DomainEventPublisher
openlifeops-runtime        # TaskManager, execution spine
openlifeops-orchestrator   # OpenLifeOps.execute facade
openlifeops-mcp            # ToolDescriptor, ToolRegistry
openlifeops-knowledge      # KnowledgeService stubs
openlifeops-governance     # PolicyEngine.evaluate(Action)
openlifeops-evidence       # append-only EvidenceStore
openlifeops-memory         # conversation/session stubs
openlifeops-api            # Spring Boot REST
openlifeops-packs/tax      # TaxPack implements OpenLifeOpsPack
```

Deploy as one **modular monolith** — prove domain boundaries first; extract services only when scaling demands it.

### Vertical packs

| Pack | Agents |
| --- | --- |
| Tax | Document analysis, reconciliation (**v0.1 reference**) |
| Consumer | Bill analysis, complaint, negotiation (second validation) |
| Finance | Research, portfolio intelligence |
| Family | School, travel, home |
| SMB | Sales, support, operations |

Each pack provides: agents, tasks, tools, knowledge, policies, workflows — not a separate application.

### Separation of concerns

```text
OpenLifeOps = how the system operates (tasks, actions, policy, evidence, approval)
Spring AI   = how the system reasons (via AI Gateway, not inside core runtime)
```

OpenLifeOps is personal work. **Eliza4J is BNY-internal** and is not part of this stack — use **Spring AI** directly for model abstraction, chat clients, embeddings, and structured output.

### Existing stack to reuse

| Piece | Role |
| --- | --- |
| Spring Boot 4.1 / Java 21–25 | API and services |
| Spring AI | Model abstraction, chat, embeddings, structured output (AI Gateway) |
| MCP | Tools |
| RAG (Spring AI + vector store) | Grounding |
| Playwright | Browser execution |
| MongoDB | Task / execution state |
| Vector DB (pgvector) | Long-term knowledge (later) |

### Capabilities that make it senior-level

1. **Agent state** — goal, plan, current step, evidence, actions, failures, retries, result.
2. **Human gateway** — e.g. “I found a ₹14,500 refund. Send this complaint?”
3. **Evaluation** — task completion %, tool-call accuracy, hallucination rate, evidence coverage, human intervention rate, execution time, cost per completed task.
4. **Observability** — traces: goal → plan → search → document → reasoning → tool → result → validation → next action.
5. **Self-healing** — tool failed → diagnose → alternative tool → retry → validate → continue.

### Hybrid model router

| Lane | Model | Jobs |
| --- | --- | --- |
| Local (Ollama) | Small LLM | Classification, routing |
| Cheap API | Mid-tier | Extraction, summarization |
| Frontier API | Best reasoning | Planning, negotiation, hard judgement |

Cursor (₹649/month India Start: Composer + Grok) is the **development** budget. It is **not** an API key for Spring Boot. Product inference is Ollama and/or OpenAI / Anthropic / Google billed separately.

## Phase roadmap

| Phase | Focus |
| --- | --- |
| 1 | Foundation spine — task → action → policy → approval → evidence | **Complete** |
| 2 | Real runtime: Planner + ExecutionEngine + Validator + checkpoint/resume/retry | **Complete** |
| 3 | Tool runtime: MCP + discovery + registry |
| 4 | Knowledge and Evidence: ingest, RAG, provenance, citations |
| 5 | Intelligence Gateway: Spring AI + model routing + structured output |
| 6 | Tax reference pack: real document reconciliation |
| 7 | UI + DX: task console, approval, traces, evidence viewer |

## Local / hybrid deploy

```
Internet → Cloudflare Tunnel (or Tailscale for private)
  → home router
  → ThinkPad (WSL2 + Docker)
       Spring Boot, Agent Engine, MCP, PostgreSQL + pgvector,
       Redis, MinIO, Playwright, Ollama
  → model router → local LLM | OpenAI | Anthropic
```

Keep Windows 11. Use **WSL2 + Ubuntu + Docker** for the Linux stack.

## Cost stages

| Stage | Users | Infrastructure | Expected monthly cost |
| --- | --- | --- | --- |
| Private prototype | You | PC | ₹1,000–₹5,000 |
| Beta | 10–100 | PC + small cloud | ₹3,000–₹15,000 |
| Early product | 100–1,000 | Cloud + hybrid AI | ₹15,000–₹75,000+ |

Do not buy a ₹2–5 lakh server on day one. Agent architecture matters more than local GPU inference.

## Hardware (current machine)

**ThinkPad E15 Gen 2 Intel** (`20TDCTO1WW`)

| Component | Actual | Verdict |
| --- | --- | --- |
| CPU | i5-1135G7, 4C/8T | Good for development |
| RAM | 16 GB DDR4-3200, 1 of 1 slot, ~92% used | **Upgrade first: 32 GB SO-DIMM (replace, do not add)** |
| Storage | 512 GB M.2 2242 NVMe (~107 GB free) | Second 1 TB 2280 later if needed |
| GPU | MX350 2 GB / Iris Xe | Do not run large local LLMs |
| OS | Windows 11 Pro | Keep |

Do not buy a new laptop, GPU, or cloud box until the first MVP is measured.

## Public surface

Use [alapureram.com](https://alapureram.com) as the lab:

- `/` profile
- `/projects`, `/agents`, `/lab`, `/articles`, `/open-source`
- `/apps/consumer-advocate`, `/apps/tax-agent`, `/apps/finance-agent`, …

Optional subdomains later: `lab.`, `api.`, `agents.`, `consumer.`, `tax.`, `finance.`

## Build order

1. WSL2 + Ubuntu  
2. Docker  
3. PostgreSQL + pgvector  
4. Redis  
5. MinIO  
6. **OpenLifeOps Runtime** (Spring Boot modular monolith in `openlifeops/`)  
7. MCP  
8. Local model via Ollama  
9. External model API (only when local is not enough)  
10. Cloudflare / Tailscale  
11. `alapureram.com`  
12. First reference pack: **Tax document reconciliation**  
13. Second validation pack: **Consumer Advocate**

## Story to publish

GitHub (open agent framework) → LinkedIn architecture posts → blog (“Building reliable tax reconciliation agents”) → live demo on real documents → benchmark on known mismatch fixtures → expand Consumer → insurance → telecom → home → subscriptions.
