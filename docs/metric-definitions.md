# Metric Definitions

## Method Metrics

- Cyclomatic Complexity: `1 + number of branch points`
- Method LoC: end line minus start line plus one
- Parameter Count: declared parameter total
- Maximum Nesting Depth: deepest conditional or loop nesting
- Branch Count: total detected decision branches

## Class Metrics

- WMC: sum of method cyclomatic complexity values in one class
- CBO: count of distinct referenced peer types
- RFC: declared methods plus distinct invoked methods
- LCOM: lack of cohesion estimate from method-field overlap
- DIT: inheritance depth from analyzed source set
- NOC: direct child class count in analyzed source set
- NOM: declared method count
- NOA: declared attribute count

## LK Presentation Metrics

- classCount
- methodCount
- attributeCount
- relationshipCount (aggregated coupling links)
- averageMethodsPerClass
- averageAttributesPerClass
- relationDensity
- inheritanceDepthDistribution

## Diagram Metrics

### Class Diagram

- classCount
- attributeCount
- methodCount
- inheritanceCount
- associationCount
- dependencyCount
- aggregationCount
- compositionCount
- relationshipDensity

### Flow Diagram

- nodeCount
- decisionNodeCount
- terminalNodeCount
- pathCountEstimate
- controlFlowComplexity

### Use Case Diagram

- actorCount
- useCaseCount
- associationCount
- includeCount
- extendCount
- generalizationCount

## Estimation Metrics

- workloadPersonMonths
- cost
- scheduleMonths
- suggestedStaffing
- basis.summary / basis.details
- ucpBreakdown.standardInputUsed
- ucpBreakdown.simpleActorCount / averageActorCount / complexActorCount
- ucpBreakdown.simpleUseCaseCount / averageUseCaseCount / complexUseCaseCount
- ucpBreakdown.uaw / uucw / uucp
- ucpBreakdown.technicalComplexityFactor / environmentalFactor
- ucpBreakdown.ucp
- ucpBreakdown.workloadPersonMonths
