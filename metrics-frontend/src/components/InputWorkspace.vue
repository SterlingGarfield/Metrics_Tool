<template>
  <section class="panel input-panel">
    <div class="mode-row">
      <button
        v-for="item in modes"
        :key="item.key"
        type="button"
        class="mode-pill"
        :class="{ active: mode === item.key }"
        @click="mode = item.key"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-if="mode === 'text'" class="pane">
      <label for="source-input">Java Source</label>
      <textarea id="source-input" v-model="textSource" rows="14"></textarea>
      <button type="button" class="primary-button" @click="emitText">Analyze Text</button>
    </div>

    <div v-else-if="mode === 'file'" class="pane">
      <label for="single-file">Java File</label>
      <input id="single-file" type="file" accept=".java" @change="emitSingleFile" />
    </div>

    <div v-else-if="mode === 'files'" class="pane">
      <label for="multi-file">Java Files</label>
      <input id="multi-file" type="file" accept=".java" multiple @change="emitMultipleFiles" />
    </div>

    <div v-else class="pane">
      <label for="folder-file">Java Source Folder</label>
      <input id="folder-file" type="file" accept=".java" multiple webkitdirectory directory @change="emitFolder" />
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['submit-text', 'submit-file', 'submit-files', 'submit-folder'])
const mode = ref('text')
const textSource = ref('')
const modes = [
  { key: 'text', label: 'Code Input' },
  { key: 'file', label: 'Single File' },
  { key: 'files', label: 'Multiple Files' },
  { key: 'folder', label: 'Folder Scan' }
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
