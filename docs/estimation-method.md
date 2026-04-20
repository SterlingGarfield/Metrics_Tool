# Estimation Method

## Inputs

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
- `costRatePerPersonMonth` (optional, default `15000`)
- `targetScheduleMonths` (optional)

## Workload Paths

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
- details: concrete input values, UCP path (`standard|simplified`), and UCP intermediate values (`UAW`, `UUCW`, `UUCP`, `TCF`, `EF`, `UCP`)
- structured UCP breakdown payload for report traceability
