# Estimation Method

## Inputs

- `estimationMethod`: `ucp|function_point` (optional, default `ucp`)
- `diagramType`: `class|flow|usecase`
- `totalLoc`
- `classCount`
- `relationshipCount`
- `useCaseCount`
- `decisionNodeCount`
- optional standard UCP inputs:
  - `simpleActorCount`, `averageActorCount`, `complexActorCount`
  - `simpleUseCaseCount`, `averageUseCaseCount`, `complexUseCaseCount`
  - `technicalComplexityFactor`, `environmentalFactor`
- optional Function Point inputs:
  - `externalInputCount`
  - `externalOutputCount`
  - `externalInquiryCount`
  - `internalLogicalFileCount`
  - `externalInterfaceFileCount`
  - `valueAdjustmentFactor`
- `costRatePerPersonMonth` (optional, default `15000`)
- `targetScheduleMonths` (optional)

## Workload Paths

### Path 0: Function Point

- supported when `estimationMethod=function_point`
- direct-input path:
  - `UFP = EI * 4 + EO * 5 + EQ * 4 + ILF * 10 + EIF * 7`
  - `AFP = UFP * VAF`
  - workload person-months = `AFP / 12.0`
- simplified fallback path:
  - when FP counts are not supplied, derive a simplified FP profile from current project indicators (`totalLoc`, `classCount`, `relationshipCount`, `useCaseCount`, `decisionNodeCount`)
  - use the same average FP weights above
  - default `VAF = 1.0` when not supplied

### Path A: standard UCP (when standard UCP fields are provided)

- `UAW = simpleActor * 1 + averageActor * 2 + complexActor * 3`
- `UUCW = simpleUseCase * 5 + averageUseCase * 10 + complexUseCase * 15`
- `UUCP = UAW + UUCW`
- `UCP = UUCP * TCF * EF`
- workload person-months = `UCP / 8.0`

### Path B: simplified UCP fallback (when standard UCP fields are not provided)

- derive actor/use-case complexity buckets from existing project inputs (`classCount`, `relationshipCount`, `useCaseCount`, `decisionNodeCount`)
- compute UAW/UUCW/UUCP using the same UCP weights
- derive fallback TCF from coupling + control-flow complexity + diagram type multiplier
- fallback UCP workload = `UCP / 10.0`

### Heuristic blend

In simplified fallback mode, final workload uses a blend:

- `0.65 * heuristicWorkload + 0.35 * fallbackUcpWorkload`

where heuristic workload is the original code/diagram complexity model:

- size factor from LoC
- OO structure factor from class count
- coupling factor from relationship count
- diagram complexity factor from relationship/use case/decision nodes
- diagram type multiplier:
  - `class`: `1.00`
  - `flow`: `1.08`
  - `usecase`: `1.12`

## Derived Outputs

- workload person-months
- cost = workload × cost rate
- schedule months
  - use provided target schedule if present
  - otherwise infer from a heuristic schedule curve
- suggested staffing = ceil(workload / schedule)

## Reporting Basis

The service returns a basis block with:

- summary: short explanation of estimation strategy
- details: concrete input values, method path, and intermediate values
- structured UCP breakdown payload for report traceability
- structured Function Point breakdown payload for report traceability
