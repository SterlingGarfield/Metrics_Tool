<template>
  <section class="overview-panel" aria-labelledby="mainline-overview-title">
    <div class="overview-header">
      <p class="overview-eyebrow">主线概览</p>
      <h2 id="mainline-overview-title">三条主线，一条本地流程</h2>
    </div>

    <div class="overview-grid">
      <article class="overview-card overview-card--code">
        <p class="card-kicker">主线一</p>
        <h3>代码度量</h3>
        <p>从 Java 源码提取项目、类与方法层面的指标，并保留风险判断结果。</p>
        <p class="card-status">状态：{{ codeStatus }}</p>
      </article>

      <article class="overview-card overview-card--diagram">
        <p class="card-kicker">主线二</p>
        <h3>设计图度量</h3>
        <p>结合结构化文件与图片识别，恢复关系并提取图纸指标。</p>
        <p class="card-status">状态：{{ diagramStatus }}</p>
      </article>

      <article class="overview-card overview-card--estimation">
        <p class="card-kicker">主线三</p>
        <h3>项目估算</h3>
        <p>支持 UCP 与 Function Point 两条路径，输出工作量与成本依据。</p>
        <p class="card-status">状态：{{ estimationStatus }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
defineProps({
  codeStatus: {
    type: String,
    default: 'idle'
  },
  diagramStatus: {
    type: String,
    default: 'idle'
  },
  estimationStatus: {
    type: String,
    default: 'idle'
  }
})
</script>
