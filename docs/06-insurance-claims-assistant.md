# AI Insurance Claims Assistant

**Rank:** 6th. High impact; more integration and document burden than Consumer Advocate v1.

## Mission

Help the consumer **understand and navigate** insurance. Do not sell policies.

## Problem

Claims fail on missing documents, misunderstood exclusions, and process opacity. Consultants and agents are expensive. People upload a hospital bill and a policy PDF and cannot map one to the other.

## Product

User uploads:

- policy  
- hospital invoice  
- discharge summary  
- receipts  
- previous correspondence  

Agent determines:

```
Coverage
  → eligibility
  → required documents
  → missing information
  → claim amount
  → exclusions
  → potential rejection reasons
```

Then generates a **claim submission package** and tracks status.

## Positioning

Not an insurer. Not a TPA replacement. A navigator: what is covered, what is missing, what to submit, what to expect. Human approval before any submission to an insurer portal.

## Relationship to other projects

Overlaps Consumer Advocate (hospital bills) and Document → Action (policy PDFs). Can start as a **Consumer Pack** vertical after bill disputes work.

## Engineering story

Multi-document reasoning, policy RAG, checklist completeness, browser automation for portals (fragile — isolate behind MCP), eval on missing-doc detection.

## Suggested URL

`alapureram.com/apps/insurance-claims`
