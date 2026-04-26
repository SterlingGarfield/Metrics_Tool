<template>
  <button type="button" class="app-action-button" :class="variantClass">
    <span v-if="$slots.icon" class="app-action-button__icon" aria-hidden="true">
      <slot name="icon" />
    </span>
    <span class="app-action-button__label"><slot /></span>
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary'
  }
})

const variantClass = computed(() => `is-${props.variant}`)
</script>
