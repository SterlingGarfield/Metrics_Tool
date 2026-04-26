# Healing Illustration UI Redesign Design

## Summary

Re-skin the current Metrics Tool into a healing, illustration-led desktop application while preserving the existing analysis workflow, information architecture, and desktop packaging path. The new style should cover the entire product, including the homepage, input workspace, empty states, results workspace, charts, and data tables.

The new visual language will use soft warm palettes, flat UI components, background-only illustrations, and a branded pig mascot system derived from [简约LOGO.png](</D:/Photos/LOGO 设计/theme/简约LOGO.png>). The interface should feel emotionally warm and approachable without reducing the readability of metrics, tables, or charts.

## Goals

- Replace the current dark analytical visual language with a healing illustration style across the full product.
- Preserve the current four analysis modes and existing result modules.
- Introduce a branded mascot system centered on a cute, seated pig holding flowers and dozing off.
- Reflect multicultural warmth through background-only companion characters.
- Use soft gradients to transition between illustration zones and pure functional surfaces.
- Apply the provided pig logo as the app identity for in-app branding, desktop shortcut icons, and the packaged `.exe`.

## Non-Goals

- Do not change the underlying metrics-analysis behavior.
- Do not redesign the user flow into a new IA or multi-step wizard.
- Do not place illustration content on top of tables, chart plots, form text, or button labels.
- Do not turn data-heavy areas into decorative dashboards that reduce clarity.

## Product Context

The current application already has:

- a Vue frontend rooted at [App.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/App.vue)
- component-driven homepage, input workspace, and results workspace
- desktop packaging through Electron under [metrics-desktop](/D:/Projects/SQA/Metrics_Tool/metrics-desktop)
- existing brand illustrations referenced in [HeroSection.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/HeroSection.vue) and [EmptyStatePanel.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/EmptyStatePanel.vue)
- a single global theme stylesheet at [theme.css](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/styles/theme.css)

This redesign should reuse that structure rather than replace it.

## Visual Direction

### Core Tone

The interface should feel:

- warm
- soft
- emotionally supportive
- modern and clean
- illustration-led without becoming childish or noisy

The visual reference is a “healing workbench” rather than a “technical dashboard”.

### Color System

Use a soft, low-saturation palette:

- base background: cream, warm ivory, pale beige
- supporting surfaces: mist pink, cloud blue, light sage, soft apricot
- accents: muted peach-orange for primary actions
- semantic colors:
  - success: soft leaf green
  - warning: warm apricot
  - danger: gentle coral rather than aggressive red

Gradients should be airy and diffused. Avoid hard neon contrasts, over-saturated purple, and stark black surfaces.

### Typography

- Use a rounded, friendly sans-serif feeling for headings and general UI.
- Preserve high-legibility system-friendly Chinese text rendering.
- Keep code input and tabular numeric content in more neutral, highly readable fonts.

### Illustration Rules

- Illustrations live in the background layer only.
- Functional surfaces such as buttons, tables, chart canvases, and form controls stay flat and clean.
- Mascot and companion illustrations may appear in:
  - hero areas
  - empty states
  - section headers
  - outer chart containers
  - background corners of input and result panels
- Illustrations may not overlap chart data, table cells, or interactive text labels.

## Brand System

### Logo Source

The source identity is [简约LOGO.png](</D:/Photos/LOGO 设计/theme/简约LOGO.png>).

That source should be adapted into:

- an in-app brand logo asset
- a Windows `.ico` file
- an Electron app icon for the executable, taskbar, and shortcut

### Mascot Definition

The main mascot is:

- a cute pig
- seated
- holding flowers
- sleepy / dozing off
- flat and simplified
- emotionally soft rather than comic or exaggerated

This mascot becomes the emotional anchor of the interface.

### Companion Characters

The redesign should optionally include 2 to 3 simplified multicultural human figures in the background illustration set. These figures should communicate warmth and inclusion through clothing, skin tone, and posture diversity, but they must stay visually subordinate to the product’s functional content.

## Page-Level Design

### App Header

The header should:

- replace the current dark badge style with a soft flat top bar
- show the adapted pig logo in a compact app-brand lockup
- keep the backend/desktop health status visible
- feel lightweight and calm rather than technical

### Homepage Hero

The hero remains a two-column structure:

- left: title, support copy, start-analysis CTA
- right: the primary mascot illustration scene

The hero illustration should show the seated pig with flowers, layered with clouds, leaves, and soft abstract forms. Optional companion human figures can sit in the rear background with low contrast. The background should transition smoothly into the next workspace section via soft gradients.

### Input Workspace

The four existing modes remain:

- code input
- single file
- multiple files
- folder scan

The redesign changes only the presentation:

- cards become soft flat selection cards
- active states use peach/apricot emphasis
- the panel background can include subtle foliage or cloud shapes
- a small background mascot accent may appear in a corner
- file and folder actions should use simple icon-led buttons

### Empty State

The empty state is one of the strongest emotional surfaces. It should use a fuller mascot scene and warmer supportive copy. The content should feel calm and encouraging, not like an error screen.

### Results Workspace

The results area should use a medium-illustration density:

- section headers get soft gradient bands and subtle illustration motifs
- overview cards gain warm flat surfaces with gentle depth
- risk sections keep semantic color but lose harsh warning aesthetics
- chart containers can have illustration-led outer backgrounds
- chart plotting areas remain clean
- metric tables stay crisp and data-first

### Tables

Tables are the strictest clarity zone:

- clean background
- high-contrast text
- subtle row separators
- gentle header tinting
- optional illustration only in the surrounding container, never inside cell content

### Charts

Charts keep clean plotting surfaces. Only the outer shell, title band, and background container can absorb decorative color or illustration treatment.

## Icons

Button and action icons should follow IconPark-style visual rhythm:

- thin-to-medium line weight
- rounded corners
- simplified geometry
- limited two-tone or mono-tone usage

Icons should be added to key actions such as:

- start analysis
- select file
- select folder
- export CSV
- export Markdown
- overview
- risk
- trends
- details

## Asset Strategy

Create a small but structured branding set:

- `app-logo` for header and brand lockup
- `hero-mascot` for homepage
- `empty-state-mascot`
- `results-background-accent`
- Windows icon source suitable for `.ico` export

All assets should be flat, clean, and consistent in line quality.

## Implementation Boundaries

### In Scope

- [theme.css](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/styles/theme.css) visual-system rewrite
- page-level component restyling
- replacement illustration assets in the frontend branding area
- icon integration for key actions
- Electron app icon and installer icon configuration via [electron-builder.json](/D:/Projects/SQA/Metrics_Tool/metrics-desktop/electron-builder.json)

### Out of Scope

- metrics logic changes
- API contract redesign
- result schema changes
- analysis workflow rewrites
- desktop runtime/process-flow changes unrelated to branding and icon packaging

## File-Level Design Intent

### Frontend

- [theme.css](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/styles/theme.css)
  - replace the dark palette with a healing color-token system
  - define soft surface variables, gradients, border tones, and semantic colors
- [App.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/App.vue)
  - keep structure; re-balance spacing and section rhythm for the new visual hierarchy
- [AppHeader.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/AppHeader.vue)
  - swap in the new logo and softer status presentation
- [HeroSection.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/HeroSection.vue)
  - rework homepage hero around the new mascot scene
- [EmptyStatePanel.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/EmptyStatePanel.vue)
  - rebuild as a healing illustration empty state
- [InputWorkspace.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/InputWorkspace.vue)
  - keep modes; restyle cards, controls, and section mood
- [OverviewCards.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/OverviewCards.vue)
  - restyle cards with soft flat warmth
- [RiskPanel.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/RiskPanel.vue)
  - soften warning aesthetics while keeping semantic clarity
- [MetricsCharts.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/MetricsCharts.vue)
  - preserve chart readability with illustration only in outer shells
- [MetricsTables.vue](/D:/Projects/SQA/Metrics_Tool/metrics-frontend/src/components/MetricsTables.vue)
  - keep tables crisp while harmonizing their outer containers with the new style

### Desktop Packaging

- [electron-builder.json](/D:/Projects/SQA/Metrics_Tool/metrics-desktop/electron-builder.json)
  - add Windows icon configuration
- packaging resources under [metrics-desktop/installer](/D:/Projects/SQA/Metrics_Tool/metrics-desktop/installer)
  - add the generated `.ico` asset and any required references

## Validation Criteria

The redesign is complete when:

- the homepage, input workspace, empty state, results area, charts, and tables all visibly share the same healing illustration visual system
- no critical data surface loses readability
- all four analysis entry modes still work
- CSV and Markdown export still work
- the app window, packaged `.exe`, installer, and shortcut use the new pig-based icon
- there are no residual dark-tech theme sections left in the main user flow

## Risks

### Risk 1: Over-decoration

If too much illustration reaches the data layer, the product will stop feeling like a usable metrics tool.

**Mitigation:** enforce a strict “background-only illustration” rule for data-heavy regions.

### Risk 2: Asset inconsistency

If the logo, mascot, and supporting art are produced in different styles, the redesign will feel patchworked.

**Mitigation:** derive all assets from one unified flat illustration language before wiring them into components.

### Risk 3: Small-icon legibility

The pig logo may look good in-page but fail at `.ico` sizes.

**Mitigation:** create an icon-specific simplified version rather than exporting the full illustration directly.

## Testing Strategy

- visually inspect desktop and browser builds at both homepage and results states
- run the existing frontend test suite
- run the desktop packaging path and inspect the generated executable icon
- manually verify:
  - start analysis
  - file select
  - folder select
  - export actions
  - results readability

