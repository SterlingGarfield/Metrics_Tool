# Metric Definitions

## Method Metrics

- Cyclomatic Complexity: `1 + number of branch points`
- Method LOC: end line minus start line plus one
- Parameter Count: declared parameter total
- Maximum Nesting Depth: deepest conditional or loop nesting
- Branch Count: total detected decision branches

## Class Metrics

- WMC: sum of method cyclomatic complexity values in one class
- CBO: count of distinct referenced peer types
- RFC: declared methods plus distinct invoked methods
- LCOM: lack of cohesion estimate from method-field overlap
- DIT: inheritance depth from the analyzed source set
- NOC: direct child class count in the analyzed source set
- NOM: declared method count
- NOA: declared attribute count

## Project Metrics

- Total Files: number of analyzed Java inputs
- Total Classes: number of detected classes
- Total Methods: number of detected methods
- Comment Ratio: comment lines divided by total LOC
- High-Risk Counts: classes or methods over configured thresholds
