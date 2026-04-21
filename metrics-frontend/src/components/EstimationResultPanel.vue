<template>
  <section class="result-surface result-surface--estimation">
    <header class="result-heading">
      <p class="result-kicker">估算结果</p>
      <h2>项目估算结果</h2>
      <p class="result-lede">
        先展示工作量、成本、工期和建议人数，再展开估算依据、UCP / Function Point 细分和原始数据。
      </p>
    </header>

    <div class="card-grid result-summary-grid">
      <article class="metric-card">
        <span>工作量</span>
        <strong>{{ formatWorkload(estimationResult.workloadPersonMonths) }}</strong>
      </article>
      <article class="metric-card">
        <span>成本</span>
        <strong>{{ formatMoney(estimationResult.cost) }}</strong>
      </article>
      <article class="metric-card">
        <span>工期</span>
        <strong>{{ formatSchedule(estimationResult.scheduleMonths) }}</strong>
      </article>
      <article class="metric-card">
        <span>建议人数</span>
        <strong>{{ formatPeople(estimationResult.suggestedStaffing) }}</strong>
      </article>
    </div>

    <section class="result-block">
      <header class="result-block__header">
        <h3>估算依据</h3>
        <p>{{ estimationResult.basis?.summary || '未提供估算依据摘要。' }}</p>
      </header>
      <p class="result-basis-details">{{ estimationResult.basis?.details || '未提供估算依据明细。' }}</p>
    </section>

    <section v-if="estimationResult.ucpBreakdown" class="result-block">
      <header class="result-block__header">
        <h3>UCP 细分</h3>
        <p>展示标准用例点路径的关键输入和中间结果。</p>
      </header>
      <div class="result-inline-grid">
        <article class="result-inline-card">
          <span>标准输入</span>
          <strong>{{ estimationResult.ucpBreakdown.standardInputUsed ? '是' : '否' }}</strong>
        </article>
        <article class="result-inline-card">
          <span>UAW</span>
          <strong>{{ formatDecimal(estimationResult.ucpBreakdown.uaw) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>UUCW</span>
          <strong>{{ formatDecimal(estimationResult.ucpBreakdown.uucw) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>UUCP</span>
          <strong>{{ formatDecimal(estimationResult.ucpBreakdown.uucp) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>TCF</span>
          <strong>{{ formatDecimal(estimationResult.ucpBreakdown.technicalComplexityFactor) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>EF</span>
          <strong>{{ formatDecimal(estimationResult.ucpBreakdown.environmentalFactor) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>UCP</span>
          <strong>{{ formatDecimal(estimationResult.ucpBreakdown.ucp) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>工作量</span>
          <strong>{{ formatWorkload(estimationResult.ucpBreakdown.workloadPersonMonths) }}</strong>
        </article>
      </div>
    </section>

    <section v-if="estimationResult.functionPointBreakdown" class="result-block">
      <header class="result-block__header">
        <h3>Function Point 细分</h3>
        <p>展示 FP 路径的证据，便于核对是否使用了直接输入还是简化回退。</p>
      </header>
      <div class="result-inline-grid">
        <article class="result-inline-card">
          <span>直接输入</span>
          <strong>{{ estimationResult.functionPointBreakdown.directInputUsed ? '是' : '否' }}</strong>
        </article>
        <article class="result-inline-card">
          <span>外部输入</span>
          <strong>{{ formatCount(estimationResult.functionPointBreakdown.externalInputCount) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>外部输出</span>
          <strong>{{ formatCount(estimationResult.functionPointBreakdown.externalOutputCount) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>外部查询</span>
          <strong>{{ formatCount(estimationResult.functionPointBreakdown.externalInquiryCount) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>内部逻辑文件</span>
          <strong>{{ formatCount(estimationResult.functionPointBreakdown.internalLogicalFileCount) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>外部接口文件</span>
          <strong>{{ formatCount(estimationResult.functionPointBreakdown.externalInterfaceFileCount) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>未调整 FP</span>
          <strong>{{ formatDecimal(estimationResult.functionPointBreakdown.unadjustedFunctionPoints) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>VAF</span>
          <strong>{{ formatDecimal(estimationResult.functionPointBreakdown.valueAdjustmentFactor) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>调整后 FP</span>
          <strong>{{ formatDecimal(estimationResult.functionPointBreakdown.adjustedFunctionPoints) }}</strong>
        </article>
        <article class="result-inline-card">
          <span>工作量</span>
          <strong>{{ formatWorkload(estimationResult.functionPointBreakdown.workloadPersonMonths) }}</strong>
        </article>
      </div>
    </section>

    <details class="result-raw">
      <summary>原始结果</summary>
      <div class="result-raw__body" role="region" aria-label="原始结果">
        <pre>{{ rawResult }}</pre>
      </div>
    </details>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  estimationResult: {
    type: Object,
    required: true
  }
})

const rawResult = computed(() => JSON.stringify(props.estimationResult, null, 2))

function formatWorkload(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '暂无'
  }
  return `${formatDecimal(value)} 人月`
}

function formatMoney(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '暂无'
  }
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value))
}

function formatSchedule(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '暂无'
  }
  return `${formatDecimal(value)} 个月`
}

function formatPeople(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '暂无'
  }
  return `${Number(value)} 人`
}

function formatDecimal(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '暂无'
  }
  return Number(value).toFixed(2)
}

function formatCount(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '暂无'
  }
  return String(value)
}
</script>
