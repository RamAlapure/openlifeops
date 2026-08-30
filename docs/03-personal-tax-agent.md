# Personal Tax Agent for India

**OpenLifeOps v0.1 reference pack.** Implements `OpenLifeOpsPack` in `openlifeops/openlifeops-packs/tax` with workflow `tax.reconcile.documents`.

**Rank:** 3rd in product ranking; **1st on the platform** because reconciliation exercises Task → Action → Policy → Evidence → Approval without dangerous autonomous transactions.

Treat as **year-round monitoring**, not “AI files your ITR.”

## Mission

Continuously reconcile a person’s tax year so mismatches surface before filing, then produce a prepared package a CA (or the user) can review.

## Problem

ClearTax-style expert-assisted filing costs thousands of rupees, more for capital gains, foreign income, ESOP/RSU, business income, and year-round advisory. DIY filers in Indian personal-finance communities routinely ask whether a complex return is worth a CA.

The painful work is **reconciliation**, not the last click on the ITR form.

## Product

Connect (or upload):

- Form 16 / Form 26AS (conversation also mentioned Form 130 — treat as AIS-related / verify official names when implementing)
- AIS, TIS
- capital-gains statements
- broker statements
- mutual-fund statements
- bank interest
- foreign assets
- ESOP/RSU transactions
- home loan
- rent
- donations
- previous ITR

Build a **Personal Tax Ledger**, then surface:

> You have a ₹1.83L capital-gain mismatch.

> Your broker statement contains 17 transactions that don’t reconcile with the Schedule 112A CSV.

### Killer feature: tax sanity checker

Before filing:

> Don’t just calculate my tax. Try to find everything that could be wrong.

## Positioning (regulated)

Do **not** market as unregistered tax practice or “we file for you” without a licensed partner. Default: preparation, reconciliation, explanations, checklists. Human CA for judgement and filing if needed (80/20 split).

## Why it is a good second or third agent

Documents, OCR, RAG, and reconciliation are the same muscles as Consumer Advocate. India-specific rules make the demo impressive. Compliance and data sensitivity are higher than a bill dispute.

## Engineering story

Multi-document RAG, tabular extraction, deterministic reconcilers + LLM explanation, audit trail of every mismatch, eval on known mismatch fixtures.

## Suggested URL

`alapureram.com/apps/tax-agent` or `tax.alapureram.com`

## Pack

**Tax Pack** on OpenLifeOps Runtime: tax document analysis + reconciliation.

Framework modules used: `openlifeops-core`, `openlifeops-runtime`, `openlifeops-governance`, `openlifeops-evidence`, `openlifeops-mcp`, `openlifeops-packs/tax`.
