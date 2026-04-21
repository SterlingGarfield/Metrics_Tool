<template>
  <section class="panel input-panel workspace-panel">
    <div class="workspace-header">
      <div class="workspace-heading">
        <p class="card-kicker">代码主线</p>
        <h3>中文优先输入工作区</h3>
        <p class="workspace-lede">
          先选输入方式，再把 Java 源码、文件或文件夹交给分析流程。
        </p>
      </div>
      <div class="workspace-badge" aria-label="支持的输入类型">
        Java 源码 · 单文件 · 多文件 · 文件夹扫描
      </div>
    </div>

    <div class="mode-segment" role="group" aria-label="输入模式">
      <button
        v-for="item in modes"
        :key="item.key"
        type="button"
        class="mode-toggle"
        :class="{ active: mode === item.key }"
        :aria-pressed="mode === item.key"
        :aria-label="item.label"
        @click="mode = item.key"
      >
        <span class="mode-toggle__label">{{ item.label }}</span>
        <span class="mode-toggle__hint" aria-hidden="true">{{ item.hint }}</span>
      </button>
    </div>

    <div class="workspace-grid">
      <section v-if="mode === 'text'" class="workspace-card workspace-card--editor">
        <div class="workspace-card__header">
          <div>
            <p class="workspace-card__eyebrow">文本模式</p>
            <h4>代码输入</h4>
          </div>
          <p class="workspace-card__hint">适合粘贴类、方法片段或一段快速验证的源码。</p>
        </div>

        <label for="source-input">Java 源码</label>
        <textarea
          id="source-input"
          v-model="textSource"
          rows="14"
          placeholder="把 Java 代码粘贴到这里，例如一个类、一个方法，或一小段片段。"
        ></textarea>

        <div class="workspace-actions">
          <button type="button" class="primary-button workspace-submit" @click="emitText">
            开始分析
          </button>
        </div>
      </section>

      <section v-else-if="mode === 'file'" class="workspace-card workspace-card--upload">
        <div class="workspace-card__header">
          <div>
            <p class="workspace-card__eyebrow">文件模式</p>
            <h4>Java 源文件</h4>
          </div>
          <p class="workspace-card__hint">上传一个 `.java` 文件，进入单文件分析流程。</p>
        </div>

        <label for="single-file">Java 源文件</label>
        <input id="single-file" type="file" accept=".java" @change="emitSingleFile" />

        <p class="workspace-note">支持拖入或选择本地 Java 文件，分析结果会沿用现有提交流程。</p>
      </section>

      <section v-else-if="mode === 'files'" class="workspace-card workspace-card--upload">
        <div class="workspace-card__header">
          <div>
            <p class="workspace-card__eyebrow">批量模式</p>
            <h4>Java 文件集</h4>
          </div>
          <p class="workspace-card__hint">一次上传多个 `.java` 文件，便于批量分析。</p>
        </div>

        <label for="multi-file">Java 文件</label>
        <input id="multi-file" type="file" accept=".java" multiple @change="emitMultipleFiles" />

        <p class="workspace-note">适合小型代码集合，或只想挑选若干文件进行分析的场景。</p>
      </section>

      <section v-else class="workspace-card workspace-card--upload">
        <div class="workspace-card__header">
          <div>
            <p class="workspace-card__eyebrow">目录模式</p>
            <h4>Java 源码目录</h4>
          </div>
          <p class="workspace-card__hint">选择一个目录，系统会读取其中的 Java 文件。</p>
        </div>

        <label for="folder-file">Java 源码目录</label>
        <input
          id="folder-file"
          type="file"
          accept=".java"
          multiple
          webkitdirectory
          directory
          @change="emitFolder"
        />

        <p class="workspace-note">更适合项目级输入，保持文件夹结构并交给后端统一处理。</p>
      </section>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['submit-text', 'submit-file', 'submit-files', 'submit-folder'])
const mode = ref('text')
const textSource = ref('')
const modes = [
  { key: 'text', label: '代码输入', hint: '粘贴源码' },
  { key: 'file', label: '单文件上传', hint: '一个 .java' },
  { key: 'files', label: '多文件上传', hint: '批量提交' },
  { key: 'folder', label: '文件夹扫描', hint: '目录级分析' }
]

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
