<template>
  <section class="result-surface">
    <header class="result-heading">
      <p class="result-kicker">代码结果</p>
      <h2>项目概览</h2>
    </header>
    <div class="card-grid">
      <article class="metric-card">
        <span>文件总数</span>
        <strong>{{ summary.totalFiles }}</strong>
      </article>
      <article class="metric-card">
        <span>类总数</span>
        <strong>{{ summary.totalClasses }}</strong>
      </article>
      <article class="metric-card">
        <span>方法总数</span>
        <strong>{{ summary.totalMethods }}</strong>
      </article>
      <article class="metric-card">
        <span>总代码行数</span>
        <strong>{{ summary.totalLoc }}</strong>
      </article>
    </div>
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
