# Apple-Inspired Workbench Redesign Design

## Context

The current frontend already has the required functional surface for the course project:

- code metrics
- design diagram analysis
- project estimation
- export and course-alignment evidence

However, the current page still behaves visually like a single long engineering workbench:

- one page with repeated neutral panels
- limited visual hierarchy between product overview and tool execution
- weak distinction between “what this product is” and “how to use each mainline”
- mixed density across hero, forms, and results

This is functional, but it does not yet present the project as a polished product experience.

The new goal is not just to “decorate” the page. The frontend should be redesigned into a coherent product-style interface that:

- feels closer to an Apple-style product page in tone
- remains practical as a working tool
- uses Chinese as the primary interface language
- supports section-based navigation instead of one undifferentiated scrolling workbench

## Goal

Redesign the frontend into a two-layer experience:

1. a polished product-style overview layer with strong visual storytelling
2. an efficient section-switched workbench for the three course mainlines and report export

At the end of this phase:

- the page should feel like one unified product instead of a stack of utility panels
- the site should use Chinese as the primary language
- top-level navigation should support section switching across:
  - 产品概览
  - 代码度量
  - 设计图度量
  - 项目估算
  - 报告导出
- the product overview should be visually elevated
- the workbench sections should remain efficient and legible

## Non-Goals

- changing backend APIs
- changing metric formulas
- changing estimation formulas
- rewriting exporter behavior
- changing course-alignment semantics
- introducing dark mode as the primary presentation
- imitating Apple branding assets or copying Apple layouts literally

## Design Direction

This redesign should be Apple-inspired in interaction tone and visual discipline, not a direct copy.

### Design principles

- **Large-scale hierarchy:** fewer small headings, more meaningful large titles
- **Generous whitespace:** spacious sections, comfortable reading rhythm
- **Calm materials:** bright neutral surfaces, soft shadows, restrained translucency
- **Precision over decoration:** subtle gradients, clean lines, controlled accents
- **Chinese-first clarity:** copy should read like a refined Chinese product page, with English only as secondary terminology where helpful
- **Tool efficiency preserved:** once inside a work section, actions should still feel fast and direct

### Experience model

The site should feel like:

- first: a product showcase
- then: a professional workbench

That means the page needs a stronger transition from “what the product offers” into “use the product now.”

## Information Architecture

The redesign should move from a pure vertical workbench to a section-switched product page.

### Top navigation

Add a top navigation bar with these anchors:

- 产品概览
- 代码度量
- 设计图度量
- 项目估算
- 报告导出

Expected behavior:

- sticky or semi-sticky behavior while scrolling
- active section state
- smooth scroll or section jump behavior
- mobile-friendly wrapping or compact stacking

### Page structure

The page should be reorganized into these major sections:

#### 1. Product Hero

Purpose:

- introduce the product in one strong first impression
- communicate the project as an integrated system, not just a demo tool

Content direction:

- large Chinese headline
- concise supporting sentence
- current runtime status shown in a cleaner status strip
- one or two primary actions guiding users downward into the workbench

#### 2. Mainline Overview

Purpose:

- explain the three course mainlines as product capabilities

Content direction:

- three feature cards or tiles
- stronger visual identity than the current simple cards
- each card should emphasize:
  - capability meaning
  - supported artifact type
  - value to software measurement workflow

#### 3. Workbench Navigation Transition

Purpose:

- visually separate the “product overview” layer from the “do work” layer

Content direction:

- a compact intro band or segmented control
- explicit invitation to enter the analysis workspace

#### 4. Section-Based Workbench

Purpose:

- keep each operational area focused and efficient

Sections:

- 代码度量
- 设计图度量
- 项目估算
- 报告导出

Each section should feel like a premium tool module rather than a generic form block.

## Content Language

The interface should switch to Chinese as the primary user-facing language.

### Translation policy

- primary headings, helper text, section titles, and button labels should be Chinese
- technical abbreviations may remain in English where they are domain-standard:
  - LK
  - CK
  - UCP
  - Function Point
- English may appear as secondary labels or microcopy only when it helps recognition

### Tone

The copy should sound:

- product-grade
- concise
- calm
- professional

Avoid:

- machine-translated stiffness
- overly academic headings on the main UI
- long instructional paragraphs in the hero area

## Visual System

### Color direction

Replace the current warm paper-and-accent palette with a cooler, more modern neutral system.

Recommended palette direction:

- near-white base
- soft silver-gray surfaces
- graphite text
- blue-gray highlight tint
- one restrained accent color for emphasis

This should feel:

- precise
- premium
- clean
- modern

Avoid:

- strong orange dominance
- heavy skeuomorphic effects
- loud gradients
- dark-mode-first styling

### Typography direction

Typography should become more deliberate and product-like.

Recommended treatment:

- large, bold hero headline
- medium-weight section titles
- smaller supporting text with strong line-height
- numbers and metric values remain crisp and prominent

The design should avoid default “dashboard blandness.” Headings must carry the page.

### Surfaces and spacing

Use:

- larger corner radii
- softer shadows
- more open section spacing
- cleaner borders
- occasional translucent or frosted material cues where useful

The workbench should feel layered, not boxed-in.

## Component-Level Design

### `App.vue`

This file will remain the orchestration layer, but its visual structure should change significantly.

Required changes:

- reorganize top-level sections into the new information architecture
- add navigation and section anchors
- separate the overview layer from the workbench layer
- localize headings and support text to Chinese
- make the export area feel like a final deliverable module, not just two buttons inside a plain card

### `InputWorkspace.vue`

This component should evolve from a functional input switcher into a more premium code-entry module.

Required direction:

- segmented mode switch should feel more refined
- code input area should be better framed
- file upload states should feel intentional, not bare
- supporting copy should guide users without clutter

### `LkMetricsPanel.vue`

This panel should remain focused, but should better match the premium visual system.

Required direction:

- stronger typographic hierarchy
- more elegant metric cards
- inheritance summary area should feel like an evidence panel, not a plain note

### Diagram and estimation sections

The structured/image forms and the estimation form should be visually upgraded without sacrificing input density.

Recommended direction:

- separate “input” and “result” more clearly
- improve internal grouping of controls
- reduce the feeling of an unstructured vertical form

### Result areas

Current result blocks should feel more curated.

Recommended direction:

- better headings
- clearer spacing between summary and detail
- stronger emphasis on confidence, counts, and key outcomes
- less raw “debug output” feeling

## Interaction Design

### Section switching

The user selected “分段切换” rather than a single long workbench.

That means the UI should support clear movement between sections:

- top nav clicks jump to sections
- the current section should be visually highlighted
- section transitions should feel smooth

### Motion

Motion should be subtle and purposeful.

Recommended uses:

- soft entrance/fade for hero and cards
- section highlight transitions
- slight lift or glow changes on important cards/buttons

Avoid:

- heavy motion
- bouncing micro-interactions
- overly playful animation

## Responsive Behavior

The redesign must continue to work on both desktop and mobile.

### Desktop

- overview sections can use wider layouts
- hero and mainline overview can breathe
- workbench navigation can remain horizontal

### Mobile

- navigation may wrap or collapse visually while staying usable
- forms should remain readable without cramped side-by-side layouts
- large hero text should scale down gracefully

The redesign should not assume only a desktop presentation environment.

## Testing Strategy

This phase is primarily frontend presentation work, but it still needs regression coverage.

### Required coverage

- existing targeted frontend tests must keep passing
- any changed text assertions in UI tests must be updated carefully
- if navigation or section logic becomes stateful, add focused tests for:
  - active section rendering
  - preserved LK panel visibility
  - export button availability rules

### Verification targets

At minimum, preserve or update passing coverage for:

- `src/components/__tests__/InputWorkspace.test.js`
- `src/components/__tests__/DashboardFlow.test.js`
- `src/utils/__tests__/exporters.test.js`

## Risks And Mitigations

### Risk: Apple-inspired becomes Apple-imitation

Mitigation:

- borrow tone and discipline, not brand mimicry
- avoid copying Apple’s exact layout patterns or identity markers

### Risk: product-page polish makes the tool slower

Mitigation:

- keep workbench sections efficient
- keep forms scannable
- preserve direct action paths

### Risk: Chinese-first rewrite breaks tests

Mitigation:

- update text assertions intentionally
- keep identifiers and technical labels stable where needed

### Risk: section switching becomes over-engineered

Mitigation:

- use lightweight anchor-based navigation or minimal state
- avoid introducing complex routing unless truly necessary

## Acceptance Rule

Do not call this phase complete until:

1. the page has a clear product-overview layer and workbench layer
2. Chinese is the primary UI language
3. top-level navigation supports section switching
4. the visual system is consistently applied across hero, input, and result areas
5. existing critical workflows remain usable
6. targeted frontend tests pass

## Design Summary

This redesign turns the current utility-style page into a Chinese-first, Apple-inspired product workbench. The homepage becomes a polished capability narrative, while the operational sections remain efficient and tool-oriented through section-based navigation and a unified premium visual language.
