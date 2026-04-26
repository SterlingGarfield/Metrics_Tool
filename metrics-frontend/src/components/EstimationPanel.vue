<template>
  <section class="panel results-section">
    <div class="results-section-heading">
      <p class="results-section-kicker">Estimation</p>
      <h2>估算结果</h2>
      <p class="results-section-copy">
        把人工估算与用例点估算拆开展示，补齐工作量与额外度量族的课程表达。
      </p>
    </div>

    <template v-if="summary.mode === 'use-case-points'">
      <h3 class="results-subheading">用例点估算结果</h3>
      <section class="card-grid">
        <article class="metric-card">
          <span>UAW</span>
          <strong>{{ summary.useCasePoints?.uaw ?? 0 }}</strong>
        </article>
        <article class="metric-card">
          <span>UUCW</span>
          <strong>{{ summary.useCasePoints?.uucw ?? 0 }}</strong>
        </article>
        <article class="metric-card">
          <span>UUCP</span>
          <strong>{{ summary.useCasePoints?.uucp ?? 0 }}</strong>
        </article>
        <article class="metric-card">
          <span>UCP</span>
          <strong>{{ formatNumber(summary.useCasePoints?.ucp) }}</strong>
        </article>
      </section>
    </template>

    <template v-else>
      <h3 class="results-subheading">人工估算结果</h3>
      <section class="card-grid">
        <article class="metric-card">
          <span>代码规模（LoC）</span>
          <strong>{{ summary.loc }}</strong>
        </article>
        <article class="metric-card">
          <span>人员数量</span>
          <strong>{{ summary.staffCount }}</strong>
        </article>
        <article class="metric-card">
          <span>开发时长（月）</span>
          <strong>{{ summary.devMonths }}</strong>
        </article>
        <article class="metric-card">
          <span>项目成本</span>
          <strong>{{ formatNumber(summary.cost) }}</strong>
        </article>
        <article class="metric-card">
          <span>工作量（人月）</span>
          <strong>{{ formatNumber(summary.workloadPersonMonths) }}</strong>
        </article>
        <article class="metric-card">
          <span>人月生产率</span>
          <strong>{{ formatNumber(summary.productivityPerPersonMonth) }}</strong>
        </article>
        <article class="metric-card">
          <span>单位 LoC 成本</span>
          <strong>{{ formatNumber(summary.costPerLoc) }}</strong>
        </article>
      </section>
    </template>
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
