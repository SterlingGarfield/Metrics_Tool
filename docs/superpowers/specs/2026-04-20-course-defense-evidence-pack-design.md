# Course Defense Evidence Pack Design

## Context

The project already has the core course-facing capabilities in place:

- code metrics through the Java backend and Vue frontend
- structured diagram analysis and image-based diagram recognition
- project estimation with both `ucp` and `function_point`
- report/export output and course-alignment documentation

The remaining gap is no longer feature completeness. The current risk is that the defense flow still depends too much on:

- developer knowledge of where evidence lives
- raw JSON or implicit UI knowledge
- scattered sample inputs and scripts

This creates unnecessary friction during course defense, report writing, and final demonstration.

## Goal

Package the existing project into a stable, easy-to-follow course-defense evidence pack that makes the demonstration path explicit and repeatable.

At the end of this phase:

- the repository must contain a defense-oriented walkthrough for the recommended live demo path
- each course requirement must have a direct evidence index to pages, APIs, docs, samples, scripts, and tests
- the existing sample inputs must be annotated with what they are for and what result the presenter should highlight
- top-level documentation must expose these materials as first-class entry points

## Non-Goals

- adding a new metric algorithm
- changing backend contracts or frontend behavior
- introducing a new orchestration script unless the current scripts are insufficient
- replacing the existing report outline
- generating static screenshots or one-off presentation assets

## Problem Statement

The current repository has the right ingredients, but they are not yet assembled into one defense-ready narrative.

### 1. The demo story is implicit

The repository contains samples under:

- `samples/ui-demo-inputs`
- `samples/diagram-inputs`
- `samples/demo-projects`

It also contains useful scripts under:

- `scripts/build-demo.ps1`
- `scripts/run-demo.ps1`
- `scripts/smoke-demo.ps1`

However, a presenter still has to infer:

- which path to show first
- which sample is best for each input mode
- which output should be emphasized to satisfy course requirements

### 2. Evidence is distributed across multiple docs

The project now has strong course-alignment artifacts:

- `docs/course-requirement-matrix.zh-CN.md`
- `docs/lk-course-alignment.zh-CN.md`
- `docs/metric-definitions.md`
- `docs/estimation-method.md`

These are useful, but there is no single evidence index that says:

- requirement -> where to show it in the product
- requirement -> which file/doc/test proves it

### 3. Demo readiness is under-explained

The current README files include setup and API information, but they do not yet foreground:

- the recommended defense path
- which scripts to use before the defense
- which documents the presenter should open during Q&A

## Success Criteria

This phase is complete only when all of the following are true:

### Defense walkthrough

- a dedicated Chinese walkthrough exists for the course defense flow
- it covers the recommended order:
  - code metrics
  - structured diagram metrics
  - image diagram recognition
  - project estimation with `ucp` and `function_point`
  - export/report evidence
- each step calls out the input, the expected visible result, and the talking points to emphasize

### Evidence indexing

- a dedicated Chinese evidence index exists
- it maps each course requirement to:
  - UI area
  - backend API
  - sample input
  - supporting documentation
  - relevant tests

### Sample guidance

- `samples/ui-demo-inputs/README.md` explicitly explains how each sample supports the demo
- the sample guide points to the recommended order and expected observations

### Entry points

- `README.zh-CN.md` links to the new defense materials
- `docs/report-outline.md` links to the new defense materials as evidence sources

## Recommended Approach

### 1. Add a dedicated course-defense walkthrough

Create `docs/course-defense-demo.zh-CN.md` as the main presenter script.

It should include:

- pre-demo checklist
- startup order
- recommended live walkthrough
- fallback path if image recognition is unavailable
- defense Q&A references

The walkthrough should favor operational clarity over theory. It should tell the presenter:

- what to open
- what to paste/upload
- what to point at on screen
- which course requirement that moment supports

### 2. Add a course evidence index

Create `docs/course-evidence-index.zh-CN.md` as the cross-reference table for the defense and report.

Its purpose is different from the requirement matrix:

- the requirement matrix explains alignment
- the evidence index explains where to prove the alignment live

Each entry should connect one course item to:

- UI or workflow entry
- API or contract
- sample asset
- supporting doc
- test evidence

### 3. Upgrade the sample input guide

Extend `samples/ui-demo-inputs/README.md` so it becomes a demo operator guide instead of a bare file list.

For each input mode, the guide should state:

- which file or folder to use
- what the presenter should expect to see
- which talking point it supports

This keeps the live walkthrough stable even if the presenter has limited time to rehearse.

### 4. Expose the evidence pack from existing entry points

Update:

- `README.zh-CN.md`
- `docs/report-outline.md`

so the new defense materials are easy to find.

The README should answer:

- where to start for defense prep
- where to find evidence for course requirements

The report outline should answer:

- which appendix or supporting doc to cite while writing each section

## Data And Navigation Flow

The design keeps the existing repository structure and adds a clearer navigation layer:

1. `README.zh-CN.md` points to the defense pack
2. `docs/course-defense-demo.zh-CN.md` points to samples, scripts, and detailed evidence docs
3. `docs/course-evidence-index.zh-CN.md` points back to UI areas, APIs, tests, and alignment docs
4. `samples/ui-demo-inputs/README.md` supports the live operator flow during the demo

This creates one stable chain from setup to defense to report writing without changing the underlying product.

## Error Handling And Fallbacks

The defense walkthrough should include two explicit fallback rules.

### 1. If image recognition is unavailable

The walkthrough should direct the presenter to:

- show structured diagram analysis first
- show recognition status endpoints or UI status if needed
- explain that the image pipeline is integrated but runtime-dependent

This preserves the defense narrative without hiding the system boundary.

### 2. If time is limited

The walkthrough should provide a shorter “minimum passing” route that still demonstrates:

- code metrics
- one structured diagram path
- one estimation path
- one export/report output

## Risks And Mitigations

### Risk: duplicated wording across documents drifts

Mitigation:

- keep the new docs as navigation and evidence layers
- link to existing metric and alignment docs instead of duplicating formulas

### Risk: the walkthrough becomes too long

Mitigation:

- keep the main path concise
- move detailed explanations into the evidence index and metric docs

### Risk: live image recognition depends on runtime state

Mitigation:

- document a fallback to structured samples
- reference the existing smoke/build/runtime scripts in the pre-demo checklist

## Acceptance Rule

Do not call this phase complete until:

1. the defense walkthrough doc exists
2. the evidence index doc exists
3. the sample input guide has been upgraded for live demo use
4. the README and report outline link to the new materials

## Design Summary

This phase does not add new product behavior. It converts the current repository from “feature-complete but presenter-dependent” into a defense-ready package with a repeatable narrative, explicit evidence paths, and stable documentation entry points.
