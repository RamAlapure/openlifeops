# AI Consumer Advocate

**Rank:** 1st for this profile. **First product to ship on OpenLifeOps.**

## Mission

Give everyone the power of a personal analyst, researcher, negotiator, and administrative assistant for bills, invoices, and consumer disputes.

## Problem

Consumers drown in decisions and fine print. In India, dark patterns (hidden charges, forced add-ons, drip pricing, subscription traps) are a large, expensive problem. The National Consumer Helpline has handled on the order of **108,000 grievance dockets per month** (Apr–Jun 2024) across banking, telecom, e-commerce, electronics, and digital payments.

Human help (lawyer, CA, “someone who knows how to complain”) is slow and costly. Chatbots that only summarize a PDF do not complete the job.

## Product

User uploads:

- electricity / telecom bill
- hospital invoice
- appliance repair estimate
- insurance renewal
- subscription renewal
- any quotation

The system answers, in order:

1. What am I actually paying for?
2. Is anything unusual?
3. Can I challenge it?
4. What evidence do I need?
5. Draft the complaint.
6. Send it (with approval).
7. Track the response.
8. Escalate if there is no response.

### Hospital-bill example (₹37,000)

1. OCR the invoice  
2. Categorize charges  
3. Detect duplicates  
4. Compare line items to policy/coverage if available  
5. Flag questionable charges  
6. Search relevant regulations/policies  
7. Calculate amount worth disputing  
8. Generate an evidence package  
9. Draft a complaint  
10. Submit through the right channel  
11. Track the case  
12. Escalate on silence  

## MVP (v1)

Upload **any bill or quotation**. Return:

| Field | Example |
| --- | --- |
| Total | ₹23,450 |
| Potential savings | ₹4,200 |
| Suspicious charges | 3 |
| Missing information | 2 |
| Comparable market price | ₹18,900 |
| Confidence | 87% |
| Recommended action | Ask vendor for a revised quotation |

Then: generate negotiation message → compare another quote → track resolution → escalate complaint.

That is enough for a demo: real bill in, structured findings + draft out.

## Why this one first

- Socially useful and easy to demonstrate  
- Multimodal (PDF, photo, OCR)  
- Agentic (research + write + submit + track)  
- India-focused and multilingual later  
- Open-source friendly and commercially viable  
- Expands into telecom, insurance, hospitals, subscriptions, home services  

## Engineering story

Multimodal ingest → RAG on consumer rules/policies → MCP tools (browser, email) → human approval → evaluation metrics (₹ flagged, cases closed). Not “an LLM chatbot for bills.”

## Suggested URL

`alapureram.com/apps/consumer-advocate` or `consumer.alapureram.com`

## Pack

**Consumer Pack** on OpenLifeOps: bill analysis + complaint + negotiation.
