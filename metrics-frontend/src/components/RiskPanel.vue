<template>
  <section class="panel results-section risk-panel">
    <div class="results-section-heading">
      <p class="results-section-kicker">Risks</p>
      <h2>风险焦点</h2>
      <p class="results-section-copy">
        把高风险项先摆到前面，方便决定下一步该修哪一段代码。
      </p>
    </div>

    <ul v-if="riskFindings.length" class="risk-list">
      <li v-for="item in riskFindings" :key="item.scope + item.target" class="risk-item">
        <strong>{{ localizeScope(item.scope) }}</strong>
        <span>{{ item.target }}</span>
        <p>{{ localizeMessage(item.message) }}</p>
      </li>
    </ul>
    <p v-else class="results-empty-state">本次分析未发现需要立即关注的高风险项。</p>
  </section>
</template>

<script setup>
const scopeLabels = {
  CLASS: '类级风险',
  METHOD: '方法级风险'
}

const exactMessageLabels = {
  'Cyclomatic complexity is high': '圈复杂度偏高',
  'Class complexity or coupling is high': '类复杂度或耦合度偏高'
}

const messageReplacements = [
  ['Cyclomatic complexity', '圈复杂度'],
  ['complexity or coupling', '复杂度或耦合度'],
  ['exceeds threshold', '超过阈值'],
  ['threshold', '阈值'],
  ['is very high', '非常高'],
  ['is high', '偏高'],
  ['High coupling', '高耦合'],
  ['Deep nesting', '嵌套过深'],
  ['Long method', '方法过长'],
  ['Class', '类'],
  ['Method', '方法']
]

function localizeScope(scope) {
  return scopeLabels[scope] ?? '风险项'
}

function localizeMessage(message) {
  if (!message) {
    return '请结合详细指标进一步确认该风险项。'
  }

  const normalized = message.trim()

  if (exactMessageLabels[normalized]) {
    return exactMessageLabels[normalized]
  }

  let localized = normalized

  for (const [source, target] of messageReplacements) {
    localized = localized.replaceAll(source, target)
  }

  return localized
    .replaceAll('(', '（')
    .replaceAll(')', '）')
    .replace(/\s+/g, ' ')
    .replace(/\s+（/g, '（')
    .trim()
}

defineProps({
  riskFindings: {
    type: Array,
    required: true
  }
})

function formatScope(scope) {
  const scopeMap = {
    class: '类',
    method: '方法',
    file: '文件',
    project: '项目'
  }
  return scopeMap[scope] || scope
}
</script>
