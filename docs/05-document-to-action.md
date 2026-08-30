# AI Document → Action Engine

**Rank:** 5th as a product; **shared substrate** for Consumer Advocate, Tax, Insurance, School, and Home.

## Mission

Current AI stops at “here’s a summary.” This product says: **what this document means, what you need to do, by when, and I can do the next step.**

## Problem

Households receive a constant stream of documents they do not fully read:

- bank letter  
- insurance policy  
- school circular  
- tax document  
- employment document  
- rental agreement  
- medical bill  
- warranty  
- government notice  
- invoice  
- legal notice  

Missing a deadline or consent form is the real cost, not lack of a summary.

## Product

### Example: school circular

Upload PDF.

> School picnic on September 14.

Then automatically:

| Extract | Action |
| --- | --- |
| Event | Calendar |
| ₹1,250 required | Task |
| Consent form | Extract fields |
| Deadline | Reminder |
| Items to carry | Checklist |
| WhatsApp to family | Draft |

Same pattern for legal notices (deadline + response draft), warranties (expiry + claim steps), policies (exclusions + what to keep).

## Why it is a platform play

Almost every vertical starts with a document. A strong document-to-action pipeline (OCR, classification, entity/date extraction, action graph, human approval) is the deepest technical layer and can sit under multiple apps.

## MVP

One document type done well (school circular **or** utility bill **or** insurance letter): classify → entities → calendar/task/draft → show evidence spans in the PDF.

## Engineering story

Multimodal ingest, layout-aware extraction, action schemas, calendar/email MCP tools, eval on extraction F1 and missed-deadline rate.

## Suggested URL

`alapureram.com/apps/document-action`

## Pack

Cross-cutting engine; used by Consumer, Tax, Family, and Insurance packs.
