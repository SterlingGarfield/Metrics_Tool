<template>
  <header class="desktop-titlebar" role="banner">
    <div class="desktop-titlebar__left">
      <div class="desktop-titlebar__history" aria-label="导航控制">
        <button
          type="button"
          class="desktop-titlebar__history-button"
          aria-label="后退"
          disabled
        >
          <span aria-hidden="true">&lt;</span>
        </button>
        <button
          type="button"
          class="desktop-titlebar__history-button"
          aria-label="前进"
          disabled
        >
          <span aria-hidden="true">&gt;</span>
        </button>
      </div>

      <nav class="desktop-titlebar__menus" aria-label="应用菜单">
        <button
          v-for="item in menuItems"
          :key="item"
          type="button"
          class="desktop-titlebar__menu-item"
          :aria-label="item"
          @click="emit('show-app-menu')"
        >
          {{ item }}
        </button>
      </nav>
    </div>

    <strong class="desktop-titlebar__center-title desktop-titlebar__center-title--bold">
      Piggy Metrics
    </strong>

    <div class="desktop-titlebar__drag-region" aria-hidden="true"></div>

    <div class="desktop-titlebar__window-controls" aria-label="窗口控制">
      <button
        type="button"
        class="desktop-titlebar__window-button"
        aria-label="最小化窗口"
        @click="$emit('minimize-window')"
      >
        <span aria-hidden="true">−</span>
      </button>
      <button
        type="button"
        class="desktop-titlebar__window-button"
        :aria-label="maximizeLabel"
        @click="$emit('toggle-maximize-window')"
      >
        <span aria-hidden="true">{{ isMaximized ? '❐' : '□' }}</span>
      </button>
      <button
        type="button"
        class="desktop-titlebar__window-button desktop-titlebar__window-button--close"
        aria-label="关闭窗口"
        @click="$emit('close-window')"
      >
        <span aria-hidden="true">×</span>
      </button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  isMaximized: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'show-app-menu',
  'close-window',
  'minimize-window',
  'toggle-maximize-window'
])

const menuItems = ['文件', '编辑', '查看', '窗口', '帮助']

const maximizeLabel = computed(() => (
  props.isMaximized ? '还原窗口' : '最大化窗口'
))
</script>
