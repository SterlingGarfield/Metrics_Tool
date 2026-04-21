<template>
  <section class="panel results-section results-section-summary">
    <div class="results-section-heading">
      <p class="results-section-kicker">概览</p>
      <h2>本次分析概览</h2>
      <p class="results-section-copy">
        汇总本轮分析的核心规模数据，帮助你快速判断本次输入覆盖范围与代码体量。
      </p>
    </div>

    <section class="card-grid">
      <article class="metric-card">
        <span>文件数</span>
        <strong>{{ summary.totalFiles }}</strong>
      </article>
      <article class="metric-card">
        <span>类数量</span>
        <strong>{{ summary.totalClasses }}</strong>
      </article>
      <article class="metric-card">
        <span>方法数</span>
        <strong>{{ summary.totalMethods }}</strong>
      </article>
      <article class="metric-card">
        <span>总 LOC</span>
        <strong>{{ summary.totalLoc }}</strong>
      </article>
    </section>
  </section>
</template>

<script setup>
defineProps({
  summary: {
    type: Object,
    required: true
  }
})
</script>
