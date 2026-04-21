<template>
  <section class="hero-section panel">
    <div class="hero-copy">
      <p class="hero-eyebrow">Software Quality Assurance Studio</p>
      <h1>Java 度量分析平台</h1>
      <p class="hero-lede">暗色可视化代码度量与风险洞察</p>
      <p class="hero-support">支持代码输入、单文件、多文件与文件夹扫描</p>
      <div class="hero-actions">
        <button type="button" class="hero-cta" @click="$emit('start')">开始分析</button>
      </div>
    </div>

    <div class="hero-art">
      <img class="hero-illustration" :src="pigHero" alt="品牌猪形象" />
    </div>
  </section>
</template>

<script setup>
import pigHero from '../assets/branding/pig-hero.png'

defineEmits(['start'])
</script>
