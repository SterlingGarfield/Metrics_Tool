<template>
  <section class="panel">
    <header class="result-heading">
      <p class="result-kicker">风险提示</p>
      <h2>风险提示</h2>
      <p class="result-lede">
        仅保留当前运行中识别到的高风险项，方便先看最需要关注的部分。
      </p>
    </header>
    <ul v-if="riskFindings.length">
      <li v-for="item in riskFindings" :key="item.scope + item.target">
        {{ formatScope(item.scope) }}：{{ item.target }} - {{ item.message }}
      </li>
    </ul>
    <p v-else>本次分析未发现关键风险。</p>
  </section>
</template>

<script setup>
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
