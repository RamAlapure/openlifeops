# AI Home-Repair Negotiator / Home OS

**Rank:** 9th. Practical, WhatsApp-native, slightly lower “platform wow” than Consumer Advocate but the same bill/quote muscle.

## Mission

Turn messy home-service quotes into a structured, negotiable, historically tracked **Home OS**.

## Problem

Plumber, AC, car, appliance, and construction quotes are opaque. Families overpay, lose warranties, and have no appliance history. Coordinators and “someone who knows a guy” are the expensive human layer.

## Product

User uploads:

- plumber quotation  
- AC repair quote  
- car repair estimate  
- appliance repair bill  
- construction quotation  

Example:

**Quote = ₹48,700**

| Category | Amount |
| --- | --- |
| Labour | ₹12,000 |
| Parts | ₹18,500 |
| Consumables | ₹4,000 |
| Service charges | ₹6,500 |
| Unknown | ₹7,700 |

Then: “Three items require clarification.”

Capabilities:

- compare market prices  
- identify duplicate charges  
- compare alternative quotes  
- negotiate  
- generate questions  
- generate WhatsApp message  
- maintain repair history  
- remind about warranty/service  
- store appliance information  

### Home OS tree

```
Home
 ├── AC
 ├── Refrigerator
 ├── Washing machine
 ├── Car
 ├── Plumbing
 ├── Electrical
 ├── Appliances
 └── Documents
```

Family digital maintenance manager, not a one-off quote chatbot.

## Relationship to Consumer Advocate

v1 of Consumer Advocate on any quotation already covers much of this. Home OS is the **persistent asset graph** (appliances, warranties, history) on top.

## Engineering story

Quote OCR, line-item taxonomy, price comparables, WhatsApp draft tools, asset registry, reminder jobs.

## Suggested URL

`alapureram.com/apps/home-os`

## Pack

**Family Pack** / Consumer Pack overlap.
