import { buildCsv, buildMarkdownReport } from '../exporters'

describe('exporters', () => {
  test('includes LK course-aligned view in markdown and csv exports when lk presentation is available', () => {
    const snapshot = {
      codeResult: {
        projectSummary: {
          totalFiles: 1,
          totalClasses: 2,
          totalMethods: 5,
          totalLoc: 80
        },
        riskFindings: [],
        codeMetrics: {
          lkPresentation: {
            classCount: 2,
            methodCount: 5,
            attributeCount: 4,
            relationshipCount: 3,
            averageMethodsPerClass: 2.5,
            averageAttributesPerClass: 2.0,
            relationDensity: 1.5,
            inheritanceDepthDistribution: [0, 1]
          }
        }
      }
    }

    const markdown = buildMarkdownReport(snapshot)
    const csv = buildCsv(snapshot)

    expect(markdown).toContain('### LK Course-Aligned View')
    expect(markdown).toContain('- Class Count: 2')
    expect(markdown).toContain('- Relationship Density: 1.50')
    expect(markdown).toContain('- Inheritance Depth Distribution: [0,1]')

    expect(csv).toContain('codeMetrics.lk,relationshipCount,3')
    expect(csv).toContain('codeMetrics.lk,relationDensity,1.50')
    expect(csv).toContain('codeMetrics.lk,inheritanceDepthDistribution,"[0,1]"')
  })

  test('includes function point evidence in markdown and csv exports when fp breakdown is available', () => {
    const snapshot = {
      estimationResult: {
        workloadPersonMonths: 14.85,
        cost: 222750,
        scheduleMonths: 3.1,
        suggestedStaffing: 5,
        basis: {
          summary: 'Function Point estimation using direct transactional and data-function counts.'
        },
        functionPointBreakdown: {
          directInputUsed: true,
          externalInputCount: 12,
          externalOutputCount: 8,
          externalInquiryCount: 5,
          internalLogicalFileCount: 4,
          externalInterfaceFileCount: 2,
          unadjustedFunctionPoints: 162,
          valueAdjustmentFactor: 1.1,
          adjustedFunctionPoints: 178.2,
          workloadPersonMonths: 14.85
        }
      }
    }

    const markdown = buildMarkdownReport(snapshot)
    const csv = buildCsv(snapshot)

    expect(markdown).toContain('### Function Point Breakdown')
    expect(markdown).toContain('- Direct Input Used: true')
    expect(markdown).toContain('- Adjusted Function Points: 178.20')

    expect(csv).toContain('estimation.fp,directInputUsed,true')
    expect(csv).toContain('estimation.fp,adjustedFunctionPoints,178.20')
  })

  test('falls back to lk presentation when lk metrics is absent', () => {
    const snapshot = {
      codeResult: {
        projectSummary: { totalFiles: 1, totalClasses: 1, totalMethods: 1, totalLoc: 12 },
        riskFindings: [],
        codeMetrics: {
          lkPresentation: {
            classCount: 1,
            methodCount: 1,
            attributeCount: 0,
            relationshipCount: 0,
            averageMethodsPerClass: 1,
            averageAttributesPerClass: 0,
            relationDensity: 0,
            inheritanceDepthDistribution: [0]
          }
        }
      }
    }

    expect(buildMarkdownReport(snapshot)).toContain('### LK Course-Aligned View')
    expect(buildCsv(snapshot)).toContain('codeMetrics.lk,classCount,1')
  })

  test('prefers lk metrics over lk presentation when both are present', () => {
    const snapshot = {
      codeResult: {
        projectSummary: { totalFiles: 1, totalClasses: 3, totalMethods: 5, totalLoc: 42 },
        riskFindings: [],
        codeMetrics: {
          lkMetrics: {
            classCount: 7,
            methodCount: 8,
            attributeCount: 5,
            relationshipCount: 4,
            averageMethodsPerClass: 3.5,
            averageAttributesPerClass: 2.5,
            relationDensity: 9.99,
            inheritanceDepthDistribution: [9, 8]
          },
          lkPresentation: {
            classCount: 2,
            methodCount: 2,
            attributeCount: 1,
            relationshipCount: 1,
            averageMethodsPerClass: 1,
            averageAttributesPerClass: 0.5,
            relationDensity: 0.5,
            inheritanceDepthDistribution: [0, 1]
          }
        }
      }
    }

    const markdown = buildMarkdownReport(snapshot)
    const csv = buildCsv(snapshot)

    expect(markdown).toContain('- Class Count: 7')
    expect(markdown).toContain('- Relationship Density: 9.99')
    expect(markdown).toContain('- Inheritance Depth Distribution: [9,8]')
    expect(markdown).not.toContain('- Class Count: 2')

    expect(csv).toContain('codeMetrics.lk,classCount,7')
    expect(csv).toContain('codeMetrics.lk,relationshipCount,4')
    expect(csv).toContain('codeMetrics.lk,relationDensity,9.99')
    expect(csv).toContain('codeMetrics.lk,inheritanceDepthDistribution,"[9,8]"')
    expect(csv).not.toContain('codeMetrics.lk,classCount,2')
  })
})
