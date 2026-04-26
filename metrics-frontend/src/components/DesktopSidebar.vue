<template>
  <nav class="desktop-sidebar panel" aria-label="桌面工作区导航">
    <div class="desktop-sidebar__section">
      <p class="desktop-sidebar__eyebrow">Workspaces</p>
      <button
        v-for="item in workspaces"
        :key="item.key"
        type="button"
        class="desktop-sidebar__item"
        :class="{ active: workspace === item.key }"
        :aria-current="workspace === item.key ? 'page' : undefined"
        :aria-label="item.label"
        :aria-pressed="workspace === item.key"
        @click="emit('update:workspace', item.key)"
      >
        <span class="desktop-sidebar__item-accent" aria-hidden="true"></span>
        <span class="desktop-sidebar__item-icon" aria-hidden="true">
          <component
            :is="item.icon"
            theme="outline"
            size="20"
            fill="currentColor"
            :stroke-width="3"
          />
        </span>
        <span class="desktop-sidebar__item-label">{{ item.label }}</span>
      </button>

      <div v-if="workspace === 'code'" class="desktop-sidebar__subitems">
        <button
          v-for="item in codeModes"
          :key="item.key"
          type="button"
          class="desktop-sidebar__subitem"
          :class="{ active: codeMode === item.key }"
          :aria-current="codeMode === item.key ? 'page' : undefined"
          :aria-label="item.label"
          :aria-pressed="codeMode === item.key"
          @click="emit('update:codeMode', item.key)"
        >
          <span class="desktop-sidebar__subitem-accent" aria-hidden="true"></span>
          <span class="desktop-sidebar__subitem-icon" aria-hidden="true">
            <component
              :is="item.icon"
              theme="outline"
              size="16"
              fill="currentColor"
              :stroke-width="3"
            />
          </span>
          <span class="desktop-sidebar__subitem-label">{{ item.label }}</span>
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup>
import {
  Calculator,
  CodeBrackets,
  CodeComputer,
  CollectionFiles,
  FileText,
  FolderCode,
  GraphicDesign
} from '@icon-park/vue-next'

defineProps({
  workspace: {
    type: String,
    default: 'code'
  },
  codeMode: {
    type: String,
    default: 'text'
  }
})

const emit = defineEmits(['update:workspace', 'update:codeMode'])

const workspaces = [
  { key: 'code', label: '代码度量', icon: CodeComputer },
  { key: 'design', label: '设计度量', icon: GraphicDesign },
  { key: 'estimation', label: '项目估算', icon: Calculator }
]

const codeModes = [
  { key: 'text', label: '代码输入', icon: CodeBrackets },
  { key: 'file', label: '单文件分析', icon: FileText },
  { key: 'files', label: '多文件分析', icon: CollectionFiles },
  { key: 'folder', label: '文件夹扫描', icon: FolderCode }
]
</script>
