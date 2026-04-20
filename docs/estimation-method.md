# Estimation Method

## Inputs

- `diagramType`: `class|flow|usecase`
- `totalLoc`
- `classCount`
- `relationshipCount`
- `useCaseCount`
- `decisionNodeCount`
- `costRatePerPersonMonth` (optional, default `15000`)
- `targetScheduleMonths` (optional)

## Workload Heuristic

Workload person-months is estimated from:

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
- details: concrete input values and multiplier used in this run

