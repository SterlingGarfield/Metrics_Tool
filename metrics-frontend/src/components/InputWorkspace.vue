<template>
  <section class="panel input-panel workspace-panel">
    <header class="workspace-intro">
      <div class="workspace-intro-copy">
        <p class="workspace-kicker">分析工作区</p>
        <h2>选择你的分析方式</h2>
        <p class="workspace-summary">
          用一组清晰的模式卡片切换输入来源，快速进入 Java 度量分析流程。
        </p>
      </div>

      <aside class="workspace-status" aria-live="polite">
        <span class="workspace-status-label">当前模式</span>
        <p class="workspace-status-line">当前模式：{{ activeMode.label }}</p>
        <p>{{ activeMode.summary }}</p>
      </aside>
    </header>

    <div class="mode-grid" role="list" aria-label="分析模式选择">
      <button
        v-for="item in modes"
        :key="item.key"
        type="button"
        class="mode-card"
        :class="{ active: mode === item.key }"
        :aria-pressed="mode === item.key"
        :aria-label="item.label"
        @click="mode = item.key"
      >
        <span class="mode-card-title">{{ item.label }}</span>
        <span class="mode-card-summary">{{ item.summary }}</span>
      </button>
    </div>

    <div class="workspace-body">
      <div class="workspace-body-copy">
        <p class="workspace-kicker">模式详情</p>
        <h3>{{ activeMode.label }}</h3>
        <p>{{ activeMode.detail }}</p>
      </div>

      <div v-if="mode === 'text'" class="pane workspace-form">
        <label for="source-input">Java 源码输入区</label>
        <textarea id="source-input" v-model="textSource" rows="14"></textarea>
        <button type="button" class="primary-button" @click="emitText">开始分析任务</button>
      </div>

      <div v-else-if="mode === 'file'" class="pane workspace-form">
        <label for="single-file">选择一个 Java 文件</label>
        <input id="single-file" type="file" accept=".java" @change="emitSingleFile" />
      </div>

      <div v-else-if="mode === 'files'" class="pane workspace-form">
        <label for="multi-file">选择多个 Java 文件</label>
        <input id="multi-file" type="file" accept=".java" multiple @change="emitMultipleFiles" />
      </div>

      <div v-else class="pane workspace-form">
        <label for="folder-file">扫描 Java 源码文件夹</label>
        <input
          id="folder-file"
          type="file"
          accept=".java"
          multiple
          webkitdirectory
          directory
          @change="emitFolder"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'

const emit = defineEmits(['submit-text', 'submit-file', 'submit-files', 'submit-folder'])
const mode = ref('text')
const textSource = ref('')
const modes = [
  {
    key: 'text',
    label: '代码输入',
    summary: '粘贴源码片段，立即启动分析。',
    detail: '适合快速粘贴 Java 片段并立即启动度量分析。'
  },
  {
    key: 'file',
    label: '单文件分析',
    summary: '上传一个 Java 文件，查看单点结果。',
    detail: '用于对单个 Java 源文件进行快速检查与风险洞察。'
  },
  {
    key: 'files',
    label: '多文件分析',
    summary: '一次选择多个文件，批量分析。',
    detail: '适合把多个 Java 文件一起送入同一次分析任务。'
  },
  {
    key: 'folder',
    label: '文件夹扫描',
    summary: '扫描源码目录，覆盖整个模块。',
    detail: '用于按目录扫描 Java 项目源码并收集整体度量结果。'
  }
]

const activeMode = computed(() => modes.find((item) => item.key === mode.value) || modes[0])

function emitText() {
  emit('submit-text', {
    fileName: 'Snippet.java',
    sourceCode: textSource.value
  })
}

function emitSingleFile(event) {
  emit('submit-file', Array.from(event.target.files || []))
}

function emitMultipleFiles(event) {
  emit('submit-files', Array.from(event.target.files || []))
}

function emitFolder(event) {
  emit('submit-folder', Array.from(event.target.files || []))
}
</script>
