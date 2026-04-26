<template>
  <section class="workbench-intro">
    <div class="workbench-header">
      <p class="workbench-eyebrow">工作区过渡</p>
      <h2>从产品概览进入分析工作区</h2>
      <p class="workbench-subtitle">
        先从代码度量建立项目画像，再把设计图度量和项目估算串成同一条本地分析链路，最后统一导出报告。
      </p>
    </div>

    <div class="action-row" aria-label="快速入口">
      <button type="button" class="primary-button" @click="emit('select', 'code-metrics')">直达代码度量</button>
      <button type="button" class="primary-button" @click="emit('select', 'diagram-metrics')">直达设计图度量</button>
      <button type="button" class="primary-button" @click="emit('select', 'estimation')">直达项目估算</button>
      <button type="button" class="primary-button" @click="emit('select', 'export')">直达报告导出</button>
    </div>

    <div class="steps">
      <article class="step-card step-card--code">
        <span class="step-index">01</span>
        <h3>代码度量</h3>
        <p>直接输入源码、单文件、多个文件或目录，先把项目主数据跑出来。</p>
      </article>
      <article class="step-card step-card--diagram">
        <span class="step-index">02</span>
        <h3>设计图度量</h3>
        <p>在结构化文件和图片识别之间切换，把设计图关系补齐。</p>
      </article>
      <article class="step-card step-card--export">
        <span class="step-index">03</span>
        <h3>报告导出</h3>
        <p>当任一主线完成后，就可以把结果汇总成 CSV 或 Markdown。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
const emit = defineEmits(['select'])
</script>
