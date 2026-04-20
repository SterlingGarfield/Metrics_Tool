function quoteCsv(value) {
  if (value === null || value === undefined) {
    return ''
  }
  const text = String(value)
  if (text.includes(',') || text.includes('"') || text.includes('\n')) {
    return `"${text.replace(/"/g, '""')}"`
  }
  return text
}

function formatValue(value) {
  if (value === null || value === undefined) {
    return 'n/a'
  }
  if (typeof value === 'number') {
    return Number.isInteger(value) ? value : value.toFixed(2)
  }
  return String(value)
}

export function buildCsv(snapshot) {
  const codeResult = snapshot?.codeResult || null
  const diagramResult = snapshot?.diagramResult || null
  const estimationResult = snapshot?.estimationResult || null

  const rows = [['track', 'item', 'value']]

  const summary = codeResult?.projectSummary
  if (summary) {
    rows.push(['codeMetrics', 'files', formatValue(summary.totalFiles)])
    rows.push(['codeMetrics', 'classes', formatValue(summary.totalClasses)])
    rows.push(['codeMetrics', 'methods', formatValue(summary.totalMethods)])
    rows.push(['codeMetrics', 'loc', formatValue(summary.totalLoc)])
  }

  if (Array.isArray(codeResult?.classMetrics)) {
    codeResult.classMetrics.forEach((item) => {
      rows.push(['codeMetrics.class', `${item.className}.wmc`, formatValue(item.wmc)])
      rows.push(['codeMetrics.class', `${item.className}.cbo`, formatValue(item.cbo)])
      rows.push(['codeMetrics.class', `${item.className}.rfc`, formatValue(item.rfc)])
    })
  }

  if (diagramResult) {
    rows.push(['diagramMetrics', 'diagramType', formatValue(diagramResult.diagramType)])
    rows.push(['diagramMetrics', 'sourceType', formatValue(diagramResult.sourceType)])
    rows.push(['diagramMetrics', 'elementCount', formatValue(diagramResult.elements?.length || 0)])
    rows.push(['diagramMetrics', 'relationCount', formatValue(diagramResult.relations?.length || 0)])
    rows.push(['diagramMetrics', 'confidence', formatValue(diagramResult.confidence?.overall)])

    if (Array.isArray(diagramResult.metrics)) {
      diagramResult.metrics.forEach((metric) => {
        rows.push([
          'diagramMetrics.metric',
          metric.name,
          formatValue(metric.value)
        ])
      })
    }
  }

  if (estimationResult) {
    rows.push(['estimation', 'workloadPersonMonths', formatValue(estimationResult.workloadPersonMonths)])
    rows.push(['estimation', 'cost', formatValue(estimationResult.cost)])
    rows.push(['estimation', 'scheduleMonths', formatValue(estimationResult.scheduleMonths)])
    rows.push(['estimation', 'suggestedStaffing', formatValue(estimationResult.suggestedStaffing)])
    rows.push(['estimation', 'basisSummary', formatValue(estimationResult.basis?.summary)])

    const ucp = estimationResult.ucpBreakdown
    if (ucp) {
      rows.push(['estimation.ucp', 'standardInputUsed', formatValue(ucp.standardInputUsed)])
      rows.push(['estimation.ucp', 'uaw', formatValue(ucp.uaw)])
      rows.push(['estimation.ucp', 'uucw', formatValue(ucp.uucw)])
      rows.push(['estimation.ucp', 'uucp', formatValue(ucp.uucp)])
      rows.push(['estimation.ucp', 'ucp', formatValue(ucp.ucp)])
    }
  }

  return rows.map((row) => row.map(quoteCsv).join(',')).join('\n')
}

export function buildMarkdownReport(snapshot) {
  const codeResult = snapshot?.codeResult || null
  const diagramResult = snapshot?.diagramResult || null
  const estimationResult = snapshot?.estimationResult || null

  const lines = ['# Metrics Workbench Integrated Report', '']

  lines.push('## Mainline 1: Code Metrics')
  if (codeResult?.projectSummary) {
    const summary = codeResult.projectSummary
    lines.push(`- Files: ${summary.totalFiles}`)
    lines.push(`- Classes: ${summary.totalClasses}`)
    lines.push(`- Methods: ${summary.totalMethods}`)
    lines.push(`- LOC: ${summary.totalLoc}`)
    const riskLines = (codeResult.riskFindings || []).length === 0
      ? ['- No high-risk findings in this run']
      : codeResult.riskFindings.map((item) => `- ${item.scope}: ${item.target} - ${item.message}`)
    lines.push('')
    lines.push('### Risk Findings')
    lines.push(...riskLines)
  } else {
    lines.push('- No code metrics run in this report.')
  }

  lines.push('')
  lines.push('## Mainline 2: Design Diagram Metrics')
  if (diagramResult) {
    lines.push(`- Diagram Type: ${formatValue(diagramResult.diagramType)}`)
    lines.push(`- Source Type: ${formatValue(diagramResult.sourceType)}`)
    lines.push(`- Elements: ${formatValue(diagramResult.elements?.length || 0)}`)
    lines.push(`- Relations: ${formatValue(diagramResult.relations?.length || 0)}`)
    lines.push(`- Confidence: ${formatValue(diagramResult.confidence?.overall)}`)
    if (Array.isArray(diagramResult.metrics) && diagramResult.metrics.length > 0) {
      lines.push('')
      lines.push('### Diagram Metric Values')
      diagramResult.metrics.forEach((metric) => {
        lines.push(`- ${metric.name}: ${formatValue(metric.value)} ${metric.unit || ''}`.trim())
      })
    }
  } else {
    lines.push('- No diagram analysis run in this report.')
  }

  lines.push('')
  lines.push('## Mainline 3: Project Estimation')
  if (estimationResult) {
    lines.push(`- Workload (PM): ${formatValue(estimationResult.workloadPersonMonths)}`)
    lines.push(`- Cost: ${formatValue(estimationResult.cost)}`)
    lines.push(`- Schedule (months): ${formatValue(estimationResult.scheduleMonths)}`)
    lines.push(`- Suggested Staffing: ${formatValue(estimationResult.suggestedStaffing)}`)
    lines.push(`- Basis: ${formatValue(estimationResult.basis?.summary)}`)
    if (estimationResult.ucpBreakdown) {
      const ucp = estimationResult.ucpBreakdown
      lines.push('')
      lines.push('### UCP Breakdown')
      lines.push(`- Standard Input Used: ${formatValue(ucp.standardInputUsed)}`)
      lines.push(`- UAW: ${formatValue(ucp.uaw)}`)
      lines.push(`- UUCW: ${formatValue(ucp.uucw)}`)
      lines.push(`- UUCP: ${formatValue(ucp.uucp)}`)
      lines.push(`- UCP: ${formatValue(ucp.ucp)}`)
    }
  } else {
    lines.push('- No project estimation run in this report.')
  }

  return lines.join('\n')
}
