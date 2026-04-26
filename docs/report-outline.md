# Report Outline

## 1. Requirement Analysis

- course requirement mapping table
- software metrics automation tool goals
- functional, data, and non-functional requirements
- why the desktop form fits the assignment better than a browser-only shell

## 2. System Design

- frontend and backend architecture
- AST parsing workflow
- metric calculation modules
- desktop transport: renderer -> preload IPC -> Electron main -> Java CLI
- domain response split: `codeMetrics / designMetrics / estimationMetrics`

## 3. Implementation

- key interfaces
- code stage: CK + traditional metrics + LK supplementation
- design stage: class diagram / use-case diagram / flow diagram inputs
- project stage: LoC / workload / cost / time / staff + use case points
- visualization and export modules

## 4. Result Analysis

- sample project metrics
- LK Metrics section
- Design Metrics section
- Project Estimation section
- Use Case Points section
- accuracy comparison against expected values
- usability and effectiveness discussion

## 5. Improvement Suggestions

- known limitations
- next-step metrics
- future tooling extensions
