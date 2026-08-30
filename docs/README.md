# AI Projects

Practical agentic products for everyday problems. These notes come from the [ChatGPT analysis](https://chatgpt.com/share/6a831f2b-8418-83e8-ad4c-07f2733ea1ce) of what to build as a Java/AI engineer: turn expensive, repetitive human services into AI that delivers most of the value at near-zero marginal cost.

**Do not ship ten separate apps.** Build one platform — **OpenLifeOps: An open operating system for AI-powered life workflows** — and add vertical packs on top.

> **Plan. Act. Verify. Ask when it matters.**

Lab home: [alapureram.com](https://alapureram.com) (`/lab`, `/projects`, `/apps/...`).

Code: [`openlifeops/`](openlifeops/) — OpenLifeOps Runtime (Java 21 / Spring Boot modular monolith).

## How to read this folder

| File | What it is |
| --- | --- |
| [linkedin-post.md](linkedin-post.md) | LinkedIn series posts 1–17 (text, Mermaid, diagrams in `assets/`) |
| [00-openlifeops-platform.md](00-openlifeops-platform.md) | Shared platform, architecture, cost, hardware, build order |
| [03-personal-tax-agent.md](03-personal-tax-agent.md) | **Start here on the platform** — v0.1 Tax reference pack |
| [01-consumer-advocate.md](01-consumer-advocate.md) | Second validation pack — bill analysis, complaints |
| [02-smb-ai-employee.md](02-smb-ai-employee.md) | Small-business back-office agent |
| [04-financial-research-analyst.md](04-financial-research-analyst.md) | Evidence-backed investment research (not advice) |
| [05-document-to-action.md](05-document-to-action.md) | Shared document engine under several products |
| [06-insurance-claims-assistant.md](06-insurance-claims-assistant.md) | Policy + claim navigation |
| [07-career-agent.md](07-career-agent.md) | Agentic job-search operating system |
| [08-school-tutor.md](08-school-tutor.md) | Tutor + parent dashboard with learning memory |
| [09-home-os.md](09-home-os.md) | Home repair negotiator and appliance OS |
| [10-travel-concierge.md](10-travel-concierge.md) | Family travel agent, not itinerary generator |

## Ranking (profile + solo MVP)

| Rank | Product | Impact | Technical depth | Business | Solo MVP | Portfolio |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Consumer Advocate | 10 | 9 | 9 | 8 | 10 |
| 2 | SMB AI Employee | 9 | 10 | 10 | 6 | 10 |
| 3 | Tax Agent | 10 | 9 | 9 | 8 | 10 |
| 4 | Financial Researcher | 9 | 9 | 9 | 7 | 10 |
| 5 | Document → Action | 9 | 10 | 10 | 8 | 10 |
| 6 | Insurance Agent | 10 | 9 | 9 | 6 | 9 |
| 7 | Career Agent | 9 | 9 | 9 | 8 | 9 |
| 8 | School Tutor | 10 | 8 | 9 | 8 | 9 |
| 9 | Home OS | 9 | 8 | 9 | 7 | 9 |
| 10 | Travel Agent | 7 | 8 | 8 | 9 | 8 |

## Thesis

Old world: person searches, compares, calls, fills forms, follows up.

New world: person states a problem; an agent understands, researches, decides, asks permission, executes, and tracks.

Positioning for regulated domains: **AI handles ~80% of preparation; a human handles the 20% that needs professional judgement.**

## First slice

1. Upgrade ThinkPad RAM to 32 GB DDR4-3200 SO-DIMM.
2. WSL2 + Docker + **OpenLifeOps Runtime** in `openlifeops/`.
3. Ship **Tax Pack v0.1** on the platform: document reconciliation stub → policy → approval → evidence → completion.
4. Validate the runtime with **Consumer Advocate** as the second pack (no runtime rewrite).
