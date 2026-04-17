<script setup lang="ts">
import { RouterLink } from 'vue-router'

const nav = [
  { to: '/', label: '首页' },
  { to: '/medir/teams', label: '班组' },
  { to: '/medir/posts', label: '岗位' },
  { to: '/medir/shift-types', label: '班次类型' },
  { to: '/medir/staff', label: '人员' },
  { to: '/medir/config', label: '规则配置' },
  { to: '/medir/rule-meta', label: '规则元数据' },
  { to: '/medir/roster-weeks', label: '排班周' },
  { to: '/medir/calendar-days', label: '日历日' },
]
</script>

<template>
  <div class="min-h-screen bg-base-200">
    <div class="navbar bg-base-100 shadow-sm">
      <div class="flex-1">
        <RouterLink to="/" class="btn btn-ghost text-xl">MediRoster</RouterLink>
        <span class="text-sm opacity-70 hidden sm:inline">检验科排班</span>
      </div>
    </div>

    <div class="flex">
      <aside class="hidden md:block w-52 min-h-[calc(100vh-4rem)] bg-base-100 border-r border-base-300 p-3">
        <ul class="menu rounded-box">
          <li v-for="item in nav" :key="item.to">
            <RouterLink :to="item.to" active-class="active">{{ item.label }}</RouterLink>
          </li>
        </ul>
      </aside>

      <main class="flex-1 p-4 max-w-[1600px]">
        <div class="md:hidden mb-4 flex flex-wrap gap-1">
          <RouterLink v-for="item in nav" :key="item.to" :to="item.to" class="btn btn-xs btn-outline">
            {{ item.label }}
          </RouterLink>
        </div>
        <slot />
      </main>
    </div>
  </div>
</template>
