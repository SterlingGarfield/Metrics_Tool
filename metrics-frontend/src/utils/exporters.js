export function buildCsv(result) {
  const header = 'className,wmc,cbo,rfc,lcom,dit,noc,nom,noa'
  const rows = result.classMetrics.map((item) => [
    item.className,
    item.wmc,
    item.cbo,
    item.rfc,
    item.lcom,
    item.dit,
    item.noc,
    item.nom,
    item.noa
  ].join(','))
  return [header, ...rows].join('\n')
}

export function buildMarkdownReport(result) {
  const summary = result.projectSummary
  const riskLines = result.riskFindings.length === 0
    ? ['- No high-risk findings in this run']
    : result.riskFindings.map((item) => `- ${item.scope}: ${item.target} - ${item.message}`)

  return [
    '# Java Metrics Analysis Report',
    '',
    '## Project Summary',
    `- Files: ${summary.totalFiles}`,
    `- Classes: ${summary.totalClasses}`,
    `- Methods: ${summary.totalMethods}`,
    `- LOC: ${summary.totalLoc}`,
    '',
    '## Risk Findings',
    ...riskLines
  ].join('\n')
}
