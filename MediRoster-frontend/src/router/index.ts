import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },
    { path: '/medir/teams', name: 'medir-teams', component: () => import('@/views/medir/TeamsView.vue') },
    { path: '/medir/posts', name: 'medir-posts', component: () => import('@/views/medir/PostsView.vue') },
    { path: '/medir/shift-types', name: 'medir-shift-types', component: () => import('@/views/medir/ShiftTypesView.vue') },
    { path: '/medir/staff', name: 'medir-staff', component: () => import('@/views/medir/StaffView.vue') },
    { path: '/medir/config', name: 'medir-config', component: () => import('@/views/medir/ConfigView.vue') },
    { path: '/medir/rule-meta', name: 'medir-rule-meta', component: () => import('@/views/medir/RuleMetaView.vue') },
    { path: '/medir/roster-weeks', name: 'medir-roster-weeks', component: () => import('@/views/medir/RosterWeeksView.vue') },
    { path: '/medir/calendar-days', name: 'medir-calendar-days', component: () => import('@/views/medir/CalendarDaysView.vue') },
  ],
})

export default router
