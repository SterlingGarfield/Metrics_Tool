# Measurement Workflow

## A. Code Metrics Workflow

1. Accept Java source input (text/file/files/folder).
2. Parse AST with Eclipse JDT.
3. Compute method metrics, class metrics, project summary.
4. Derive LK presentation view.
5. Produce risk findings and exportable outputs.

## B. Structured Diagram Workflow

1. Accept `.puml/.mmd` + `diagramType`.
2. Parse to normalized graph entities/relations.
3. Compute class/flow/usecase metrics.
4. Return confidence and issue metadata.

## C. Image Diagram Workflow

1. preprocess image
2. run OCR for mixed language labels
3. detect nodes/connectors
4. recover semantic relations
5. build metric payload
6. compute confidence and directly-measurable flag

## D. Estimation Workflow

1. Collect size/complexity inputs from code + diagram analysis.
2. Apply heuristic workload formula with diagram-type multiplier.
3. Derive cost, schedule, and staffing suggestion.
4. Return basis details for report explanation.

