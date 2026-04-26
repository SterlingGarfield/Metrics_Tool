<template>
  <section class="panel results-section results-section-summary">
    <div class="results-section-heading">
      <p class="results-section-kicker">Snapshot</p>
      <h2>分析摘要</h2>
      <p class="results-section-copy">
        先看规模，再判断这次分析需要把注意力放在哪里。
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
