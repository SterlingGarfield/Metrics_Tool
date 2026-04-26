<template>
  <section class="panel input-panel workspace-panel">
    <div class="workspace-intro">
      <div class="workspace-intro-copy">
        <h2 class="workspace-slogan">Measure. Analyze. Evolve.</h2>
        <p class="workspace-kicker">分析工作区</p>
        <p class="workspace-summary">请在左侧选择您要使用的模式</p>
      </div>

      <aside class="workspace-status" aria-live="polite">
        <span class="workspace-status-label">当前模式</span>
        <p class="workspace-status-line">当前模式：{{ activeStatus.label }}</p>
        <p>{{ activeStatus.summary }}</p>
      </aside>
    </div>

    <div
      v-if="!hideWorkspaceSwitcher"
      class="mode-row workspace-scope-row"
      role="tablist"
      aria-label="分析工作区切换"
    >
      <button
        v-for="item in workspaces"
        :key="item.key"
        type="button"
        class="mode-pill"
        :class="{ active: workspace === item.key }"
        :aria-pressed="workspace === item.key"
        :aria-label="item.label"
        @click="emit('update:workspace', item.key)"
      >
        <span class="mode-toggle__label">{{ item.label }}</span>
        <span class="mode-toggle__hint" aria-hidden="true">{{ item.hint }}</span>
      </button>
    </div>

    <div
      v-if="workspace === 'code' && !hideWorkspaceSwitcher"
      class="mode-grid"
      role="list"
      aria-label="分析模式选择"
    >
      <button
        v-for="item in modes"
        :key="item.key"
        type="button"
        class="mode-card"
        :class="{ active: codeMode === item.key }"
        :aria-pressed="codeMode === item.key"
        :aria-label="item.label"
        @click="emit('update:codeMode', item.key)"
      >
        <span class="mode-card-title">{{ item.label }}</span>
        <span class="mode-card-summary">{{ item.summary }}</span>
      </button>
    </div>

    <div class="workspace-body">
      <div class="workspace-body-copy">
        <p class="workspace-kicker">模式详情</p>
        <h3>{{ activeStatus.label }}</h3>
        <p>{{ activeStatus.detail }}</p>
      </div>

      <div v-if="workspace === 'code' && codeMode === 'text'" class="pane workspace-form">
        <label for="source-input">Java 源码输入区</label>
        <textarea
          id="source-input"
          ref="sourceInput"
          v-model="textSource"
          class="app-textarea app-textarea--autosize"
          rows="14"
          @input="syncSourceInputHeight"
        ></textarea>
        <AppActionButton @click="emitText">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M5 12H19" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
              <path d="M13.5 6.5L19 12L13.5 17.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </template>
          开始分析任务
        </AppActionButton>
      </div>

      <div v-else-if="workspace === 'code' && codeMode === 'file'" class="pane workspace-form">
        <p class="workspace-summary">通过系统文件对话框选择待分析的 Java 文件。</p>
        <AppActionButton @click="emit('submit-file')">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M7 6.5H13L16.5 10V17.5H7V6.5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" />
              <path d="M13 6.5V10H16.5" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" />
            </svg>
          </template>
          从系统中选择 Java 文件
        </AppActionButton>
      </div>

      <div v-else-if="workspace === 'code' && codeMode === 'files'" class="pane workspace-form">
        <p class="workspace-summary">通过系统文件对话框一次选择多个 Java 文件。</p>
        <AppActionButton @click="emit('submit-files')">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none">
              <rect x="4.75" y="7.75" width="7.5" height="9.5" rx="1.5" stroke="currentColor" stroke-width="1.8" />
              <rect x="11.75" y="5.75" width="7.5" height="11.5" rx="1.5" stroke="currentColor" stroke-width="1.8" />
            </svg>
          </template>
          从系统中选择多个 Java 文件
        </AppActionButton>
      </div>

      <div v-else-if="workspace === 'code'" class="pane workspace-form">
        <p class="workspace-summary">通过系统目录选择器扫描包含 Java 源码的文件夹。</p>
        <AppActionButton @click="emit('submit-folder')">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M4.5 8.5H9L10.6 10.5H19.5V17.5H4.5V8.5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" />
              <path d="M4.5 8.5V6.5H8.4L10 8.5" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" />
            </svg>
          </template>
          从系统中选择源码文件夹
        </AppActionButton>
      </div>

      <DesignInputPanel
        v-else-if="workspace === 'design'"
        @submit-design="emit('submit-design', $event)"
      />

      <EstimationInputPanel
        v-else
        :use-case-defaults="resolvedUseCaseDefaults"
        @submit-estimation="emit('submit-estimation', $event)"
        @submit-use-case-points="emit('submit-use-case-points', $event)"
      />
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useAutosizeTextarea } from '../composables/useAutosizeTextarea'
import AppActionButton from './AppActionButton.vue'
import DesignInputPanel from './DesignInputPanel.vue'
import EstimationInputPanel from './EstimationInputPanel.vue'

const props = defineProps({
  workspace: {
    type: String,
    default: 'code'
  },
  codeMode: {
    type: String,
    default: 'text'
  },
  useCaseDefaults: {
    type: Object,
    default: null
  },
  estimationDefaults: {
    type: Object,
    default: null
  },
  hideWorkspaceSwitcher: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:workspace',
  'update:codeMode',
  'submit-text',
  'submit-file',
  'submit-files',
  'submit-folder',
  'submit-design',
  'submit-estimation',
  'submit-use-case-points'
])
const textSource = ref('')
const sourceInput = ref(null)
const workspaces = [
  {
    key: 'code',
    label: '代码度量',
    summary: '从源码片段、文件或文件夹开始分析 Java 代码度量。',
    detail: '覆盖四种 Java 输入方式，适合从结构、复杂度和风险开始。'
  },
  {
    key: 'design',
    label: '设计度量',
    summary: '整理类图、用例图或流程图指标，并可用 OCR 建议值辅助录入。',
    detail: '先确认课程要求的设计实体，再用 OCR 建议缩短录入时间。'
  },
  {
    key: 'estimation',
    label: '项目估算',
    summary: '录入规模、人员、周期与成本，快速得到课程展示所需估算值。',
    detail: '把 LoC、人员、工期和成本统一到一张可展示的估算视图。'
  }
]
const modes = [
  {
    key: 'text',
    label: '代码输入',
    summary: '粘贴源码片段，直接开始分析。',
    detail: '适合快速验证某段 Java 代码的复杂度与结构指标。'
  },
  {
    key: 'file',
    label: '单文件分析',
    summary: '上传一个 Java 文件，查看单点结果。',
    detail: '用于聚焦某个类或单个源码文件的风险与指标。'
  },
  {
    key: 'files',
    label: '多文件分析',
    summary: '一次选择多个文件，批量分析。',
    detail: '适合比较多个 Java 文件，快速得到批量结果。'
  },
  {
    key: 'folder',
    label: '文件夹扫描',
    summary: '扫描源码目录，覆盖整个模块。',
    detail: '用于从目录级别整理整个 Java 模块的总体度量。'
  }
]

const activeWorkspace = computed(() => workspaces.find((item) => item.key === props.workspace) || workspaces[0])
const activeMode = computed(() => modes.find((item) => item.key === props.codeMode) || modes[0])
const activeStatus = computed(() => (props.workspace === 'code' ? activeMode.value : activeWorkspace.value))
const resolvedUseCaseDefaults = computed(() => props.useCaseDefaults ?? props.estimationDefaults ?? null)
const { syncHeight: syncSourceInputHeight } = useAutosizeTextarea(sourceInput, [
  textSource,
  () => props.workspace,
  () => props.codeMode
])

function emitText() {
  emit('submit-text', {
    fileName: 'Snippet.java',
    sourceCode: textSource.value
  })
}
</script>
