<template>
  <section class="panel">
    <h2>Risk Findings</h2>
    <ul v-if="riskFindings.length">
      <li v-for="item in riskFindings" :key="item.scope + item.target">
        {{ item.scope }}: {{ item.target }} - {{ item.message }}
      </li>
    </ul>
    <p v-else>No critical findings in this run.</p>
  </section>
</template>

<script setup>
defineProps({
  riskFindings: {
    type: Array,
    required: true
  }
})
</script>
