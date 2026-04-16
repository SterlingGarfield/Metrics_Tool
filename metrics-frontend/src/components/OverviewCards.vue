<template>
  <section class="card-grid">
    <article class="metric-card">
      <span>Files</span>
      <strong>{{ summary.totalFiles }}</strong>
    </article>
    <article class="metric-card">
      <span>Classes</span>
      <strong>{{ summary.totalClasses }}</strong>
    </article>
    <article class="metric-card">
      <span>Methods</span>
      <strong>{{ summary.totalMethods }}</strong>
    </article>
    <article class="metric-card">
      <span>LOC</span>
      <strong>{{ summary.totalLoc }}</strong>
    </article>
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
