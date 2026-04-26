<template>
  <div class="pane workspace-form estimation-workspace">
    <div class="mode-row" role="tablist" aria-label="估算模式切换">
      <button
        type="button"
        class="mode-pill"
        :class="{ active: mode === 'manual' }"
        :aria-pressed="mode === 'manual'"
        @click="mode = 'manual'"
      >
        人工估算
      </button>
      <button
        type="button"
        class="mode-pill"
        :class="{ active: mode === 'use-case-points' }"
        :aria-pressed="mode === 'use-case-points'"
        @click="mode = 'use-case-points'"
      >
        用例点估算
      </button>
    </div>

    <div v-if="mode === 'manual'" class="form-grid">
      <label>
        <span>LoC</span>
        <input v-model="loc" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>人员数量</span>
        <input v-model="staffCount" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>工期（月）</span>
        <input v-model="devMonths" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>成本</span>
        <input v-model="cost" type="number" min="0" step="0.01" inputmode="decimal" />
      </label>
    </div>

    <div v-else class="form-grid">
      <label>
        <span>简单参与者</span>
        <input v-model="simpleActors" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>一般参与者</span>
        <input v-model="averageActors" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>复杂参与者</span>
        <input v-model="complexActors" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>简单用例</span>
        <input v-model="simpleUseCases" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>一般用例</span>
        <input v-model="averageUseCases" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>复杂用例</span>
        <input v-model="complexUseCases" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>技术复杂度因子</span>
        <input v-model="technicalComplexityFactor" type="number" min="0" step="0.01" inputmode="decimal" />
      </label>
      <label>
        <span>环境复杂度因子</span>
        <input v-model="environmentalComplexityFactor" type="number" min="0" step="0.01" inputmode="decimal" />
      </label>
    </div>

    <AppActionButton v-if="mode === 'manual'" @click="submitEstimation">提交项目估算</AppActionButton>
    <AppActionButton v-else @click="submitUseCasePoints">提交用例点估算</AppActionButton>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import AppActionButton from './AppActionButton.vue'

const props = defineProps({
  useCaseDefaults: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['submit-estimation', 'submit-use-case-points'])

const mode = ref('manual')
const loc = ref('0')
const staffCount = ref('0')
const devMonths = ref('0')
const cost = ref('0')
const simpleActors = ref('0')
const averageActors = ref('0')
const complexActors = ref('0')
const simpleUseCases = ref('0')
const averageUseCases = ref('0')
const complexUseCases = ref('0')
const technicalComplexityFactor = ref('1.0')
const environmentalComplexityFactor = ref('1.0')

function parseInteger(value) {
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0
}

function parseDecimal(value, fallback = 0) {
  const parsed = Number.parseFloat(value)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback
}

function seedUseCaseDefaults(defaults) {
  averageActors.value = String(defaults?.actorCount ?? 0)
  averageUseCases.value = String(defaults?.useCaseCount ?? 0)
}

function submitEstimation() {
  emit('submit-estimation', {
    loc: parseInteger(loc.value),
    staffCount: parseInteger(staffCount.value),
    devMonths: parseInteger(devMonths.value),
    cost: parseDecimal(cost.value)
  })
}

function submitUseCasePoints() {
  emit('submit-use-case-points', {
    simpleActors: parseInteger(simpleActors.value),
    averageActors: parseInteger(averageActors.value),
    complexActors: parseInteger(complexActors.value),
    simpleUseCases: parseInteger(simpleUseCases.value),
    averageUseCases: parseInteger(averageUseCases.value),
    complexUseCases: parseInteger(complexUseCases.value),
    technicalComplexityFactor: parseDecimal(technicalComplexityFactor.value, 1),
    environmentalComplexityFactor: parseDecimal(environmentalComplexityFactor.value, 1)
  })
}

watch(
  () => props.useCaseDefaults,
  (defaults) => {
    seedUseCaseDefaults(defaults)
  },
  { immediate: true }
)
</script>
