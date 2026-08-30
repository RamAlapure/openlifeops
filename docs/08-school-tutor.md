# AI School Tutor (with parent dashboard)

**Rank:** 8th. High social impact. Differentiator is **learning memory + parent mode**, not a ChatGPT clone for kids.

## Mission

Tutor the child on *their* school material, remember what stuck, and tell the parent exactly what to practice tomorrow.

## Problem

Generic homework chatbots restart every session. Parents cannot see gaps. Tutors are expensive. Indian families already pay for coaching; a cheap agent that follows the actual school PDF is more useful than a global curriculum bot.

## Product

Child uploads school material. Pipeline:

```
Curriculum → topics → concepts → difficulty → learning gaps → personalized practice
```

Generate:

- questions  
- worksheets  
- oral quizzes  
- reading exercises  
- revision  
- handwriting sheets  
- tests  

### Learning memory

Not “What is photosynthesis?” with a blank slate.

The system knows, for example:

| Skill | Mastery |
| --- | --- |
| Fractions | 82% |
| Multiplication | 94% |
| Reading | 76% |
| Grammar | 61% |

Tomorrow’s lesson is generated from that state.

### Parent mode

> Your child is struggling with converting fractions to decimals. Spend 15 minutes on this tomorrow.

## Safety

Child accounts, no open web by default, parent visibility, age-appropriate content, no storing of other children’s data from photos of classmates.

## Relationship to Document → Action

School circulars (picnic, fees, consent) are a natural Family Pack overlap.

## Engineering story

Persistent student model, item generation with difficulty control, mastery tracking, parent reports, eval on learning-gap prediction vs later test items.

## Suggested URL

`alapureram.com/apps/tutor`

## Pack

**Family Pack** (with travel and home).
