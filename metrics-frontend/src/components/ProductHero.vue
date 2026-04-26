<template>
  <section class="product-hero">
    <div class="hero-copy">
      <p class="hero-eyebrow">产品概览</p>
      <h1 class="hero-title">面向课程项目的软件度量工作台</h1>
      <p class="hero-subtitle">三条主线，一套本地化分析流程。</p>

      <div class="hero-rail">
        <dl class="status-grid" aria-label="系统状态">
          <div class="status-card">
            <dt class="status-label">代码后端</dt>
            <dd>{{ backendStatus }}</dd>
          </div>
          <div class="status-card">
            <dt class="status-label">识别服务</dt>
            <dd>{{ recognitionStatus }}</dd>
          </div>
        </dl>

        <div class="cta-row">
          <a class="cta cta-primary" href="#code-metrics">进入代码度量</a>
          <a class="cta cta-secondary" href="#workbench">进入分析工作区</a>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  backendStatus: {
    type: String,
    default: 'checking'
  },
  recognitionStatus: {
    type: String,
    default: 'checking'
  }
})
</script>
