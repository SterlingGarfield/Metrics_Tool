# Metrics Tool Aura Dark Redesign Design

## Context

The current `metrics-frontend` implementation is functionally complete enough for local analysis demos, but its interface still reflects an early utility-style layout:

- one long page with hero, input, status, charts, tables, and help stacked together
- a light warm theme that does not match the newly requested branding direction
- limited visual hierarchy between onboarding, analysis actions, and results
- little separation between brand expression and professional data reading

The redesign should keep the current backend API contract intact while significantly upgrading the frontend information architecture, visual system, and interaction flow.

The user has now locked the direction to:

- a major frontend redesign rather than a light skin update
- dark overall UI with color and depth inspired by Aura
- `简约LOGO.png` as the formal software logo
- the other pig-themed images used only as visual-style elements, mainly on the homepage and empty states
- a homepage that introduces the product before pushing the user into analysis
- Chinese-first UI copy with retained technical abbreviations such as `WMC` and `CBO`
- a balance between professional tool feel and distinct pig-themed brand personality

## Goal

Redesign the frontend into a brand-led dark experience that feels layered, modern, and presentation-ready while preserving the current Java metrics workflow and backend integration.

The redesigned frontend must:

- present the product as a branded software experience instead of a plain demo page
- guide the user from homepage to analysis workspace to results workspace through clear state transitions
- keep analysis inputs and result reading efficient after the initial branded entry experience
- use the new logo consistently and keep pig illustrations controlled and intentional
- improve the readability of metrics, risk findings, charts, and tables in a dark interface
- remain compatible with the existing frontend stack and current backend endpoints

## Non-Goals

- changing backend endpoints, payload shapes, or analysis logic
- redesigning metric formulas or risk thresholds
- turning pig illustrations into repeated decorative noise across all result surfaces
- building a multi-page routed application if the current single-page architecture can support the new flow cleanly
- introducing a design system that requires a full component-library migration

## Users

- course project team members who need a polished demo-ready UI
- instructors and reviewers who will judge both function and presentation quality
- local users who need to analyze Java code through a clearer and more guided workflow

## Success Criteria

- first-time visitors immediately understand what the product does from the homepage hero
- users can move from branded landing area into analysis with a single clear primary action
- the four existing analysis modes remain supported but become easier to discover and compare
- result reading follows a strong sequence: summary first, risk focus second, detailed metrics third
- empty and initial states feel branded, but result-heavy states stay professional and restrained
- the redesigned interface works on desktop and mobile widths without layout collapse
- the implementation does not require backend changes

## Design Principles

### Brand before workspace

The first screen should establish product identity, mood, and confidence before asking for input. This is important because the user explicitly wants the homepage to introduce the brand before the analysis workflow begins.

### Professional results, playful edges

Pig-themed assets should help the product feel memorable, but they should not reduce trust in the metrics output. As a result, pig visuals should concentrate on the homepage hero and empty states, while the results workspace becomes more restrained and data-focused.

### Dark, layered, calm

The interface should feel deep and modern rather than merely inverted. Contrast must come from layered panels, glows, borders, and spacing hierarchy instead of harsh neon accents or noisy textures.

### Guided reading path

Users should not land directly in dense tables. The interface should progressively reveal:

1. what this product is
2. how to start an analysis
3. what the analysis concluded
4. where the details and explanations live

### Preserve technical trust

Even with a stronger visual brand, the interface still represents a software metrics tool. Charts, metric abbreviations, status indicators, and risk findings must remain legible, structured, and academically credible.

## Recommended Direction

Adopt a hybrid “brand homepage plus professional workspace” direction.

This direction is preferred over:

- a homepage-heavy art direction, because the product would become slower to use repeatedly
- a purely dashboard-like control panel, because it would underuse the requested logo and pig-inspired identity

The recommended direction balances:

- homepage-led brand storytelling
- an efficient analysis workspace immediately below or after the hero
- a restrained results environment that feels like a real tool rather than a poster

## Information Architecture

The redesigned frontend should use four major zones within a single coherent experience.

### 1. Brand homepage

The first viewport introduces the product.

Required content:

- top navigation with logo and compact status area
- hero title in Chinese with concise product positioning
- supporting text describing Java metrics analysis and report-ready output
- one dominant primary action that moves the user into analysis
- one compact secondary summary area describing supported analysis capabilities

Visual role:

- strongest brand expression in the product
- dark Aura-inspired atmospheric background
- carefully placed pig illustration or pig-inspired art treatment

### 2. Analysis workspace

This becomes the practical action zone where users choose how to provide source input.

Required content:

- a mode selector redesigned as clear cards rather than basic pills
- one active input surface at a time for the chosen mode
- short mode descriptions so users understand the difference between text, file, multi-file, and folder scan
- contextual action labels and input hints

Modes retained:

1. code input
2. single file
3. multiple files
4. folder scan

### 3. Results workspace

This is the main reading environment after analysis succeeds.

The layout should prioritize:

- overview and primary conclusions
- risk emphasis
- chart-based interpretation
- detailed metric tables
- metric reference help

The results workspace should feel more professional than the homepage and should noticeably reduce decorative imagery.

### 4. Empty and support states

When there is no result yet, the UI should use empty-state messaging and pig-themed visuals to soften the experience and explain next steps.

These states should include:

- first-use empty state
- no critical issues state
- backend unavailable state
- input validation or error state

## Content Hierarchy

### Primary homepage hierarchy

1. logo and product name
2. product promise
3. primary action
4. concise supporting capability cards
5. ambient brand visual

### Primary analysis hierarchy

1. mode choice
2. active input surface
3. action button
4. helper guidance

### Primary results hierarchy

1. key metrics and overall analysis summary
2. risk findings and top hotspots
3. complexity chart and analytical visuals
4. class and method details
5. metric explanation reference

## Visual System

### Brand assets

#### Primary logo

`简约LOGO.png` becomes the formal software logo used in:

- top navigation
- homepage hero lockup
- lightweight brand markers elsewhere in the UI

It should be treated as the cleanest and most stable identity element.

#### Secondary brand visuals

The remaining pig-themed images should not be used as utility icons. They should instead become:

- homepage visual atmosphere
- empty-state illustrations
- subtle framed visual cards or soft background overlays

This prevents the UI from looking childish or visually inconsistent in results-heavy sections.

### Color direction

The palette should follow an Aura-inspired dark direction adapted to the pig-themed source materials.

Recommended palette families:

- deep graphite and blue-black for main backgrounds
- muted indigo or teal-shadow tones for depth layers
- warm pink-peach and soft coral as brand highlights
- restrained off-white for text and chart contrast
- selective soft cyan or aqua for status accents when needed

The palette should avoid:

- default purple-heavy gradients
- bright white panels that break dark immersion
- oversaturated candy colors across the full interface

### Texture and atmosphere

The interface should use layered background treatment:

- dark gradient base
- soft glow fields
- subtle blurred color haze
- very low-contrast texture that hints at the painterly source material

This creates depth without reducing readability.

### Typography

Typography should distinguish brand voice from data presentation.

Recommended roles:

- expressive display typography for the homepage hero
- stable sans-serif system for operational UI and tabular data
- monospace retained only for code or file-related surfaces if needed

The visual tone should feel intentional and premium rather than default-Vite or default-system UI.

## Layout Strategy

### Desktop behavior

Desktop should use a wide, layered composition.

Recommended structure:

- homepage hero with text block on the left and branded visual cluster on the right
- analysis workspace below as a two-column area or horizontally balanced card grid
- results workspace using broad panels with clear separation between summary, chart, and tables

### Mobile behavior

Mobile should stack the experience while preserving section priority.

Requirements:

- hero text and logo remain readable without oversized visual overflow
- branded illustration moves below or behind the hero content
- mode selector cards collapse into a scrollable stack or compact segmented list
- tables remain horizontally scrollable within contained panels
- spacing and contrast still preserve a premium feel on small screens

## State Model

The redesigned UI should make state transitions more explicit.

### Initial state

User sees the homepage with no analysis result yet.

Focus:

- product meaning
- trust cues
- invitation to begin

### Ready-to-analyze state

The analysis workspace is visible and the user is choosing or preparing an input mode.

Focus:

- clarity
- simplicity
- confidence in what happens next

### Loading state

When analysis begins, the interface should communicate active work with more intention than a plain text banner.

Recommended behavior:

- disable duplicate submit actions
- show contextual loading copy
- use a restrained dark-state progress treatment
- preserve user orientation so the page does not feel like it reset

### Success state

After analysis completes, the results workspace should become dominant.

Focus:

- summary first
- hotspots second
- detail third

### Error state

Errors should be visually separated by type rather than merged into one generic red message.

At minimum, distinguish:

- backend unavailable
- invalid or empty input
- analysis failure returned from backend
- partial-result conditions with warnings

## Component Direction

### App shell

`App.vue` should evolve from a stacked page into a shell that coordinates:

- navigation and branding
- hero content
- analysis workspace
- state banners
- results sections

### Hero section

The hero should move beyond a plain heading block and include:

- logo lockup
- main title in Chinese
- one concise value statement
- supporting detail line
- primary CTA
- lightweight trust signal such as backend health

### Mode selector

The current mode pills should become richer choice cards with:

- mode name
- short description
- active styling
- clearer distinction between modes

### Input panels

Each mode should keep its current functional behavior but feel more productized through:

- larger drop zones or entry areas
- contextual guidance
- stronger action affordance
- dark-surface styling that still preserves readability

### Overview summary

The top summary should become more than raw counters.

It should combine:

- key totals
- at least one synthesized conclusion or summary statement
- visual grouping that immediately tells the user what the run covered

### Risk panel

Risk findings should be surfaced earlier and styled as a focal insight area instead of a plain unordered list.

Recommended improvements:

- stronger severity hierarchy
- clearer targeting by class or method
- cleaner separation between “no critical issues” and actual findings

### Charts

Charts should be visually aligned with the dark theme.

Requirements:

- tuned axis and text colors
- highlighted accent bars that still fit the restrained palette
- panel styling that matches summary and table surfaces

### Tables

Tables should remain straightforward and readable.

Improvements should focus on:

- stronger row separation
- sticky or visually anchored headers where feasible
- improved spacing and numeric legibility
- restrained dark styling without losing scan speed

### Metric guide

The metric guide should become a quieter supporting panel or collapsible reference block so it stays available without competing with primary results.

## Interaction Flow

The user journey should be:

1. land on the homepage
2. understand the product quickly
3. choose to start analysis
4. select an input mode
5. submit source input
6. see analysis loading feedback
7. review headline results
8. inspect risks and charts
9. drill into details or export outputs
10. optionally return to re-run analysis

The redesign should support this journey without requiring route changes if a single-page transition model is sufficient.

## Copy Direction

The UI should use Chinese-first wording, while preserving recognized metric abbreviations such as:

- `WMC`
- `CBO`
- `RFC`
- `LCOM`
- `DIT`
- `NOC`

Copy tone should be:

- concise
- confident
- professional
- slightly more polished than classroom demo boilerplate

The UI should avoid:

- fully English interface labels as the default
- overexplaining metric theory inside the main task flow
- playful copy that weakens technical credibility

## Accessibility and Readability Constraints

- dark surfaces must still preserve sufficient text contrast
- accent colors should not be the only signal for active state or error state
- charts must remain legible without relying on faint grid lines
- long tables must preserve clear row tracking
- focus and hover states should remain visible on dark backgrounds

## Technical Direction

### Keep current stack

Retain:

- Vue 3
- Vite
- Axios
- ECharts

The redesign should mainly change component structure, CSS architecture, and presentation logic.

### Frontend architecture changes

Expected frontend changes may include:

- rewriting `App.vue` into a stronger shell and section coordinator
- expanding `InputWorkspace.vue` into a richer analysis-workspace component
- restyling or restructuring overview, risk, chart, table, and metric guide components
- centralizing new theme tokens in `src/styles/theme.css`
- adding local asset references for the logo and decorative imagery

### Backend contract preservation

The redesign must continue to work with:

- `GET /api/metrics/health`
- `POST /api/metrics/analyze/text`
- `POST /api/metrics/analyze/files`
- `POST /api/metrics/analyze/folder`

No response-shape dependency should be introduced that requires backend updates.

## Testing Strategy

### Functional frontend testing

Verify that the redesign still supports:

- homepage render
- mode switching across all four input modes
- text analysis submission
- single-file and multi-file submissions
- folder-scan submission
- loading and error state rendering
- results rendering after successful analysis

### UI regression focus

Because this is a large UI rework, tests should prioritize:

- main CTA visibility
- mode-selection behavior
- analysis action availability
- result-summary presence
- risk panel rendering

### Manual validation

Manual validation should confirm:

- dark theme visual consistency
- logo usage correctness
- decorative pig assets only appear in approved areas
- mobile layout remains usable
- result-heavy screens stay professional and readable

## Implementation Boundaries

To keep the redesign achievable, protect scope in this order:

1. information architecture and shell restructuring
2. theme system and dark visual language
3. redesigned analysis workspace
4. results hierarchy improvements
5. decorative polish and atmospheric artwork treatment

If tradeoffs are required, preserve workflow clarity and result readability before adding extra visual motion or ornament.

## Design Summary

The chosen design transforms the current metrics frontend from a single long utility page into a brand-led dark experience with a clearer journey: homepage first, analysis second, results third. `简约LOGO.png` becomes the formal identity anchor, while the other pig images are limited to controlled homepage and empty-state roles. The UI takes visual cues from Aura through dark layered depth, soft glow, and restrained highlight colors, but keeps the results workspace professional and academically credible. The redesign stays within the existing frontend and backend contract, making it a strong presentation upgrade without requiring service-side changes.
