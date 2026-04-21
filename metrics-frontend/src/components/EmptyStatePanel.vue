<template>
  <aside class="empty-state panel">
    <img class="empty-state-art" :src="pigEmptyState" alt="等待分析的提示插图" />
    <div class="empty-state-copy">
      <p class="empty-state-kicker">分析前指引</p>
      <h2>从任意一种输入方式开始</h2>
      <p>选择源码粘贴、文件上传或文件夹扫描后，即可进入结果工作区。</p>
      <ul class="empty-state-list">
        <li>首页保留品牌氛围，进入结果后会切换到更专业的暗色数据视图。</li>
        <li>四种输入模式可直接切换，不需要刷新页面或重置当前工作区。</li>
        <li>顶部会持续显示后端连通状态，方便确认环境是否就绪。</li>
      </ul>
    </div>
  </aside>
</template>

<script setup>
import pigEmptyState from '../assets/branding/pig-empty-state.png'
</script>
