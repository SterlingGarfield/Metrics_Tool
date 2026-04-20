# Report Outline

## 1. Project Requirement Analysis

- Explain how metric models and tool interfaces are used to realize software metrics automation.
- Functional requirements:
  - code metrics
  - structured diagram metrics
  - image diagram recognition metrics
  - project estimation outputs
- Data requirements:
  - contract entities and metric payload structures
  - input/output data definitions
- Non-functional requirements:
  - local deployment
  - reproducibility
  - reliability and usability

## 2. System Design

- Overall architecture (frontend, Java backend, Python recognition service)
- Key module design and responsibility boundaries
- UML/data contract description of metric entities
- Automated measurement workflow and algorithm boundaries

## 3. Implementation

- Core interfaces and routing
- AST-based code analysis implementation
- Structured parser implementation
- Image recognition pipeline implementation
- Estimation method implementation
- Frontend integration and export functions

## 4. Result Analysis And Evaluation

- Demonstration on sample code + diagram inputs
- Comparison between predicted metrics/estimation and practical expectations
- Accuracy, usability, and effectiveness discussion
- Identified limitations and confidence caveats

## 5. Improvement Plan

- Runtime hardening and dependency reproducibility improvements
- Metric coverage expansion (optional additional methods)
- Recognition robustness upgrades and better calibration
- Future integration and report automation enhancements

