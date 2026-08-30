# AI Financial Research Analyst

**Rank:** 4th. High technical and portfolio value. **Research and education only** — not unregistered personalized investment advice.

## Mission

Become an ordinary investor’s research analyst: evidence-backed memos, not “should I buy Tata Motors?”

## Problem

Retail investors cannot afford a full-time analyst. Stock chatbots give opinions without a paper trail. What people need is structured research: filings, what management promised vs what happened, red flags, and invalidation criteria.

## Product

User: “Analyze Bajaj Finserv.”

Agents gather sources (filings, transcripts, news, financials — as available via APIs/browser), then produce an **investment memo**:

- Bull case  
- Bear case  
- What changed  
- What management promised  
- What actually happened  
- Financial red flags  
- Valuation  
- Key triggers  
- Things that would invalidate the thesis  

Second surface:

> Why did my portfolio move today?

Holdings in → personalized explanation of the day’s move (still framed as analysis, not a buy/sell instruction).

## Positioning (SEBI)

In India, personalized investment advice is a regulated activity. SEBI distinguishes registered Investment Advisers. **v1 must be research, analysis, and education**, with citations, not “you should buy X.”

## Why it is strong for the profile

Shows multi-source research agents, citations, structured writing, and eval against hallucination (every claim tied to a source). Complements BNY / markets background without pretending to be an RIA product.

## Engineering story

Research agent + analysis agent, RAG over filings, tool use for data, memo templates, source-level citations, human review before publish.

## Suggested URL

`alapureram.com/apps/finance-agent` or `finance.alapureram.com`

## Pack

**Finance Pack** on OpenLifeOps: research + portfolio intelligence.
