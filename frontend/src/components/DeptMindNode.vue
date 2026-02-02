<template>
  <div class="mind-node-wrapper">
    <!-- 节点本身 -->
    <div class="mind-node-content glass-limit" :class="{ 'is-root': isRoot }">
      <div class="node-body" @mouseenter="hover = true" @mouseleave="hover = false">
        <div class="node-title">{{ data.deptName }}</div>
        <div class="node-meta" v-if="data.leader">负责人: {{ data.leader }}</div>
        
        <!-- 悬浮操作栏 -->
        <transition name="fade">
          <div class="node-actions" v-if="hover">
            <n-button circle size="tiny" type="success" @click="$emit('view-emp', data)">
              <template #icon><n-icon :component="PeopleOutline" /></template>
            </n-button>
            <n-button circle size="tiny" type="primary" @click="$emit('add', data)">
              <template #icon><n-icon :component="AddOutline" /></template>
            </n-button>
            <n-button circle size="tiny" type="info" @click="$emit('edit', data)" v-if="!isRoot">
              <template #icon><n-icon :component="CreateOutline" /></template>
            </n-button>
            <n-button circle size="tiny" type="error" @click="$emit('delete', data)" v-if="!isRoot">
              <template #icon><n-icon :component="TrashOutline" /></template>
            </n-button>
          </div>
        </transition>
      </div>
    </div>

    <!-- 子节点容器 (递归) -->
    <div class="node-children" v-if="data.children && data.children.length > 0">
      <div class="child-connector" v-for="child in data.children" :key="child.id">
        <DeptMindNode 
          :data="child" 
          @add="(d) => $emit('add', d)" 
          @edit="(d) => $emit('edit', d)" 
          @delete="(d) => $emit('delete', d)" 
          @view-emp="(d) => $emit('view-emp', d)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NIcon } from 'naive-ui'
import { AddOutline, CreateOutline, TrashOutline, PeopleOutline } from '@vicons/ionicons5'

defineProps<{
  data: any
  isRoot?: boolean
}>()

const emit = defineEmits(['add', 'edit', 'delete', 'view-emp'])
const hover = ref(false)
</script>

<style scoped>
/* 核心布局: 横向排列 */
.mind-node-wrapper {
  display: flex;
  align-items: center;
  position: relative;
  padding: 16px 0; /* 增加垂直间距 */
}

/* 节点卡片样式 */
.mind-node-content {
  min-width: 160px;
  max-width: 220px;
  padding: 16px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.85); /* 提高不透明度 */
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04); /* 更细腻的阴影 */
  position: relative;
  z-index: 2;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-right: 60px; /* 增加连接线长度 */
  flex-shrink: 0;
  cursor: default;
}

.mind-node-content:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.18);
  border-color: var(--primary-color);
}

/* 根节点特效 */
/* 根节点特效 */
.mind-node-content.is-root {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  box-shadow: 0 10px 25px rgba(79, 70, 229, 0.4);
}

.mind-node-content.is-root .node-title {
  color: #ffffff;
  font-size: 18px;
  letter-spacing: 0.5px;
}

.mind-node-content.is-root .node-meta {
  color: rgba(255, 255, 255, 0.8);
}

.node-title {
  font-weight: 700;
  font-size: 15px;
  color: #1e293b; /* 深色文字 */
  margin-bottom: 4px;
}

.node-meta {
  font-size: 12px;
  color: #64748b; /* 次级文字颜色 */
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 悬浮操作栏 */
.node-actions {
  position: absolute;
  top: -12px;
  right: -12px;
  display: flex;
  gap: 6px;
  background: #fff;
  padding: 4px;
  border-radius: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  border: 1px solid #f1f5f9;
}

/* 子节点容器 layout */
.node-children {
  display: flex;
  flex-direction: column;
  position: relative;
  justify-content: center; /* 垂直居中对齐 */
}

/* 连线绘制 */
.child-connector {
  position: relative;
  display: flex;
  align-items: center;
}

/* 水平连线 (从父到子) */
.child-connector::before {
  content: '';
  position: absolute;
  left: -60px; /* 对应 父margin-right */
  top: 50%;
  width: 60px;
  height: 2px;
  background: #cbd5e1;
  transition: background 0.3s;
}

/* 当父节点hover时高亮连线 */
.mind-node-wrapper:hover > .node-children > .child-connector::before,
.mind-node-wrapper:hover > .node-children > .child-connector::after {
    background: #a5b4fc;
}

/* 垂直连线 */
.child-connector::after {
  content: '';
  position: absolute;
  left: -60px;
  top: 0; 
  bottom: 0;
  width: 2px;
  background: #cbd5e1;
  transition: background 0.3s;
}

.child-connector:first-child::after {
  top: 50%;
}

.child-connector:last-child::after {
  bottom: 50%;
}

.child-connector:only-child::after {
  display: none;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
