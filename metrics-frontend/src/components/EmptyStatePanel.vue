<template>
  <aside class="empty-state panel">
    <img class="empty-state-art" :src="pigEmptyState" alt="等待分析的提示插图" />
    <div class="empty-state-copy">
      <p class="empty-state-kicker">分析前指引</p>
      <h2>从左侧导入 Java 代码开始</h2>
      <p>先选择输入方式，再提交源码、文件或文件夹，即可获得现有度量、风险与导出能力。</p>
      <ul class="empty-state-list">
        <li>默认支持快速粘贴代码片段发起分析。</li>
        <li>保留现有导出与风险视图，后续任务再升级结果层级。</li>
        <li>后端连通状态会在顶部持续展示，方便确认环境是否就绪。</li>
      </ul>
    </div>
  </aside>
</template>

<script setup>
import pigEmptyState from '../assets/branding/pig-empty-state.png'
</script>
