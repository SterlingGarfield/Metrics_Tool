export function buildCsv(result) {
  const codeMetrics = result.codeMetrics || { classMetrics: [], projectSummary: {} }
  const lkSummary = codeMetrics.lkSummary || {}
  const designMetrics = result.designMetrics || {}
  const estimationMetrics = result.estimationMetrics || {}
  const useCasePoints = estimationMetrics.useCasePoints || {}

  const summaryRows = [
    ['code.totalFiles', codeMetrics.projectSummary?.totalFiles ?? 0],
    ['code.totalClasses', codeMetrics.projectSummary?.totalClasses ?? 0],
    ['code.totalMethods', codeMetrics.projectSummary?.totalMethods ?? 0],
    ['lk.averageAddedMethodCount', lkSummary.averageAddedMethodCount ?? 0],
    ['lk.averageOverriddenMethodCount', lkSummary.averageOverriddenMethodCount ?? 0],
    ['lk.maxSpecializationIndex', lkSummary.maxSpecializationIndex ?? 0],
    ['lk.inheritanceClassCount', lkSummary.inheritanceClassCount ?? 0],
    ['design.diagramType', designMetrics.diagramType ?? ''],
    ['design.classCount', designMetrics.classCount ?? 0],
    ['design.relationshipCount', designMetrics.relationshipCount ?? 0],
    ['design.useCaseCount', designMetrics.useCaseCount ?? 0],
    ['design.actorCount', designMetrics.actorCount ?? 0],
    ['design.flowNodeCount', designMetrics.flowNodeCount ?? 0],
    ['design.relationshipDensity', designMetrics.relationshipDensity ?? 0],
    ['design.useCasesPerActor', designMetrics.useCasesPerActor ?? 0],
    ['design.imageProvided', designMetrics.imageProvided ?? false],
    ['estimation.loc', estimationMetrics.loc ?? 0],
    ['estimation.staffCount', estimationMetrics.staffCount ?? 0],
    ['estimation.devMonths', estimationMetrics.devMonths ?? 0],
    ['estimation.cost', estimationMetrics.cost ?? 0],
    ['estimation.workloadPersonMonths', estimationMetrics.workloadPersonMonths ?? 0],
    ['estimation.productivityPerPersonMonth', estimationMetrics.productivityPerPersonMonth ?? 0],
    ['estimation.costPerLoc', estimationMetrics.costPerLoc ?? 0],
    ['estimation.useCasePoints.uaw', useCasePoints.uaw ?? 0],
    ['estimation.useCasePoints.uucw', useCasePoints.uucw ?? 0],
    ['estimation.useCasePoints.uucp', useCasePoints.uucp ?? 0],
    ['estimation.useCasePoints.ucp', useCasePoints.ucp ?? 0]
  ].map(([metric, value]) => `${metric},${value}`)

  if (!codeMetrics.classMetrics?.length) {
    return ['metric,value', ...summaryRows].join('\n')
  }

  const detailHeader = 'className,wmc,cbo,rfc,lcom,dit,noc,nom,noa,addedMethodCount,overriddenMethodCount,specializationIndex'
  const detailRows = codeMetrics.classMetrics.map((item) => [
    item.className,
    item.wmc,
    item.cbo,
    item.rfc,
    item.lcom,
    item.dit,
    item.noc,
    item.nom,
    item.noa,
    item.addedMethodCount,
    item.overriddenMethodCount,
    item.specializationIndex
  ].join(','))

  return ['metric,value', ...summaryRows, '', detailHeader, ...detailRows].join('\n')
}

export function buildMarkdownReport(result) {
  const codeMetrics = result.codeMetrics || {}
  const summary = codeMetrics.projectSummary || {}
  const lkSummary = codeMetrics.lkSummary || {}
  const designMetrics = result.designMetrics || {}
  const estimationMetrics = result.estimationMetrics || {}
  const useCasePoints = estimationMetrics.useCasePoints || {}
  const riskFindings = result.riskFindings || []
  const riskLines = riskFindings.length === 0
    ? ['- No high-risk findings in this run']
    : riskFindings.map((item) => `- ${item.scope}: ${item.target} - ${item.message}`)

  const report = [
    '# Java Metrics Analysis Report',
    ''
  ]

  if (codeMetrics.available) {
    report.push(
      '## Project Summary',
      `- Files: ${summary.totalFiles ?? 0}`,
      `- Classes: ${summary.totalClasses ?? 0}`,
      `- Methods: ${summary.totalMethods ?? 0}`,
      `- LOC: ${summary.totalLoc ?? 0}`,
      ''
    )
  }

  if (codeMetrics.available || lkSummary.available) {
    report.push(
      '## LK Metrics',
      `- Average Added Method Count: ${lkSummary.averageAddedMethodCount ?? 0}`,
      `- Average Overridden Method Count: ${lkSummary.averageOverriddenMethodCount ?? 0}`,
      `- Max Specialization Index: ${lkSummary.maxSpecializationIndex ?? 0}`,
      `- Inheritance Class Count: ${lkSummary.inheritanceClassCount ?? 0}`,
      ''
    )
  }

  if (designMetrics.available) {
    report.push(
      '## Design Metrics',
      `- Diagram Type: ${designMetrics.diagramType ?? ''}`,
      `- Class Count: ${designMetrics.classCount ?? 0}`,
      `- Relationship Count: ${designMetrics.relationshipCount ?? 0}`,
      `- Use Case Count: ${designMetrics.useCaseCount ?? 0}`,
      `- Actor Count: ${designMetrics.actorCount ?? 0}`,
      `- Flow Node Count: ${designMetrics.flowNodeCount ?? 0}`,
      `- Relationship Density: ${designMetrics.relationshipDensity ?? 0}`,
      `- Use Cases Per Actor: ${designMetrics.useCasesPerActor ?? 0}`,
      `- OCR Participated: ${designMetrics.imageProvided ? 'Yes' : 'No'}`,
      ''
    )
  }

  if (estimationMetrics.available) {
    report.push(
      '## Project Estimation',
      `- LOC: ${estimationMetrics.loc ?? 0}`,
      `- Staff Count: ${estimationMetrics.staffCount ?? 0}`,
      `- Development Months: ${estimationMetrics.devMonths ?? 0}`,
      `- Cost: ${estimationMetrics.cost ?? 0}`,
      `- Workload Person-Months: ${estimationMetrics.workloadPersonMonths ?? 0}`,
      `- Productivity Per Person-Month: ${estimationMetrics.productivityPerPersonMonth ?? 0}`,
      `- Cost Per LOC: ${estimationMetrics.costPerLoc ?? 0}`,
      ''
    )
  }

  if (estimationMetrics.mode === 'use-case-points' || (useCasePoints.ucp ?? 0) > 0) {
    report.push(
      '## Use Case Points',
      `- UAW: ${useCasePoints.uaw ?? 0}`,
      `- UUCW: ${useCasePoints.uucw ?? 0}`,
      `- UUCP: ${useCasePoints.uucp ?? 0}`,
      `- UCP: ${useCasePoints.ucp ?? 0}`,
      ''
    )
  }

  report.push('## Risk Findings', ...riskLines)
  return report.join('\n')
}
