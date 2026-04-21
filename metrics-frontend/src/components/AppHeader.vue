<template>
  <header class="app-header panel">
    <div class="brand-lockup">
      <img class="brand-logo" :src="logoMinimal" alt="平台标志" />
      <div>
        <p class="brand-kicker">BRO Metrics</p>
        <strong class="brand-title">Java 度量分析平台</strong>
      </div>
    </div>

    <span class="status-badge" :class="statusClass">
      后端状态：{{ healthStatus }}
    </span>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import logoMinimal from '../assets/branding/logo-minimal.png'

const props = defineProps({
  healthStatus: {
    type: String,
    default: 'checking'
  }
})

const statusClass = computed(() => {
  if (props.healthStatus === 'UP') {
    return 'is-up'
  }

  if (props.healthStatus === 'checking') {
    return 'is-checking'
  }

  return 'is-down'
})
</script>
