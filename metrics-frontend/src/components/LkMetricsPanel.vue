<template>
  <section class="panel results-section">
    <div class="results-section-heading">
      <p class="results-section-kicker">LK</p>
      <h2>LK 指标视图</h2>
      <p class="results-section-copy">
        单独整理继承层次下的新增、覆写与特化程度，方便课程答辩时解释 LK 补齐项。
      </p>
    </div>

    <section class="card-grid">
      <article class="metric-card">
        <span>项目平均新增方法数</span>
        <strong>{{ formatNumber(summary.averageAddedMethodCount) }}</strong>
      </article>
      <article class="metric-card">
        <span>项目平均覆写方法数</span>
        <strong>{{ formatNumber(summary.averageOverriddenMethodCount) }}</strong>
      </article>
      <article class="metric-card">
        <span>最大特化指数</span>
        <strong>{{ formatNumber(summary.maxSpecializationIndex) }}</strong>
      </article>
      <article class="metric-card">
        <span>有继承关系的类数量</span>
        <strong>{{ summary.inheritanceClassCount ?? 0 }}</strong>
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

function formatNumber(value) {
  return Number(value ?? 0).toFixed(2)
}
</script>
