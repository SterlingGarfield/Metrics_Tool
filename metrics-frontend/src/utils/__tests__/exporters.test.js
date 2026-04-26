import { buildCsv, buildMarkdownReport } from '../exporters'

describe('exporters', () => {
  test('includes LK metrics, design derived metrics, workload, and use case points in CSV output', () => {
    const csv = buildCsv({
      codeMetrics: {
        available: true,
        projectSummary: {
          totalFiles: 2,
          totalClasses: 3,
          totalMethods: 4
        },
        classMetrics: [],
        methodMetrics: [],
        lkSummary: {
          averageAddedMethodCount: 1.5,
          averageOverriddenMethodCount: 0.5,
          maxSpecializationIndex: 0.75,
          inheritanceClassCount: 2
        }
      },
      designMetrics: {
        available: true,
        diagramType: 'use-case',
        classCount: 0,
        relationshipCount: 5,
        useCaseCount: 6,
        actorCount: 4,
        flowNodeCount: 0,
        relationshipDensity: 0,
        useCasesPerActor: 1.5
      },
      estimationMetrics: {
        available: true,
        mode: 'manual',
        loc: 1200,
        staffCount: 6,
        devMonths: 2,
        cost: 12000,
        workloadPersonMonths: 12,
        productivityPerPersonMonth: 100,
        costPerLoc: 10,
        useCasePoints: {
          uaw: 8,
          uucw: 35,
          uucp: 43,
          ucp: 42.57
        }
      }
    })

    expect(csv).toContain('lk.averageAddedMethodCount,1.5')
    expect(csv).toContain('design.useCasesPerActor,1.5')
    expect(csv).toContain('estimation.workloadPersonMonths,12')
    expect(csv).toContain('estimation.useCasePoints.ucp,42.57')
  })

  test('includes dedicated markdown sections for LK metrics and use case points', () => {
    const markdown = buildMarkdownReport({
      codeMetrics: {
        available: true,
        projectSummary: {
          totalFiles: 2,
          totalClasses: 3,
          totalMethods: 4,
          totalLoc: 120
        },
        classMetrics: [],
        methodMetrics: [],
        lkSummary: {
          averageAddedMethodCount: 1.5,
          averageOverriddenMethodCount: 0.5,
          maxSpecializationIndex: 0.75,
          inheritanceClassCount: 2
        }
      },
      designMetrics: {
        available: true,
        diagramType: 'use-case',
        classCount: 0,
        relationshipCount: 5,
        useCaseCount: 6,
        actorCount: 4,
        flowNodeCount: 0,
        relationshipDensity: 0,
        useCasesPerActor: 1.5
      },
      estimationMetrics: {
        available: true,
        mode: 'use-case-points',
        loc: 0,
        staffCount: 0,
        devMonths: 0,
        cost: 0,
        workloadPersonMonths: 0,
        productivityPerPersonMonth: 0,
        costPerLoc: 0,
        useCasePoints: {
          uaw: 8,
          uucw: 35,
          uucp: 43,
          ucp: 42.57
        }
      },
      riskFindings: []
    })

    expect(markdown).toContain('## LK Metrics')
    expect(markdown).toContain('## Design Metrics')
    expect(markdown).toContain('## Project Estimation')
    expect(markdown).toContain('## Use Case Points')
    expect(markdown).toContain('- Workload Person-Months:')
    expect(markdown).toContain('- UCP: 42.57')
  })
})
