import { createRouter, createWebHistory } from 'vue-router'

console.log('Router index.ts loaded - timestamp:', Date.now())

const routes: any[] = [
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'employee-list',
        name: 'EmployeeList',
        component: () => import('../views/EmployeeList.vue')
      },
      {
        path: 'employee/profile/:id',
        name: 'EmployeeProfile',
        component: () => import('../views/EmployeeProfile.vue')
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('../views/DeptView.vue')
      },
      {
        path: 'salary',
        name: 'Salary',
        component: () => import('../views/SalaryView.vue')
      },
      {
        path: 'system/user',
        name: 'UserList',
        component: () => import('../views/system/UserList.vue')
      },
      {
        path: 'system/role',
        name: 'RoleList',
        component: () => import('../views/system/RoleList.vue')
      },
      {
        path: 'system/announcement',
        name: 'AnnouncementList',
        component: () => import('../views/system/AnnouncementList.vue')
      },
      {
        path: 'system/job',
        name: 'JobManagement',
        component: () => import('../views/system/JobManagement.vue')
      },
      {
        path: 'onboarding',
        name: 'Onboarding',
        component: () => import('../views/OnboardingView.vue')
      },
      {
        path: 'payroll',
        name: 'Payroll',
        component: () => import('../views/PayrollView.vue')
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('../views/AttendanceView.vue')
      },
      {
        path: 'performance',
        name: 'Performance',
        component: () => import('../views/PerformanceManagement.vue')
      },
      {
        path: 'document',
        name: 'Document',
        component: () => import('../views/DocumentView.vue')
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('../views/AnalyticsView.vue')
      },
      {
        path: 'attendance-management',
        name: 'AttendanceManagement',
        component: () => import('../views/attendance/AttendanceManagement.vue')
      },
      {
        path: 'approvals',
        name: 'Approvals',
        component: () => import('../views/ApprovalView.vue')
      },
      {
        path: 'audit',
        name: 'AuditLog',
        component: () => import('../views/AuditLogView.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/SettingsView.vue')
      },
      {
        path: 'recruitment',
        name: 'Recruitment',
        component: () => import('../views/RecruitmentATS.vue')
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/ChatView.vue')
      },
      {
        path: 'resource-booking',
        name: 'ResourceBooking',
        component: () => import('../views/ResourceBookingView.vue')
      },
      {
        path: 'asset-management',
        name: 'AssetManagement',
        component: () => import('../views/AssetManagementView.vue')
      },
      {
        path: 'resource-analytics',
        name: 'ResourceAnalytics',
        component: () => import('../views/ResourceAnalyticsView.vue')
      },
      {
        path: 'workflow',
        name: 'Workflow',
        component: () => import('../views/WorkflowView.vue')
      },
      {
        path: 'contract',
        name: 'Contract',
        component: () => import('../views/ContractView.vue')
      },
      {
        path: 'recruitment-pool',
        name: 'RecruitmentPool',
        component: () => import('../views/RecruitmentPoolView.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
