import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '../stores/admin'

const Layout = () => import('../layout/index.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DataLine' },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Users.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('../views/Categories.vue'),
        meta: { title: '商品分类', icon: 'Menu', parent: '商品管理' },
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('../views/Products.vue'),
        meta: { title: '商品管理', icon: 'Goods', parent: '商品管理' },
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/Orders.vue'),
        meta: { title: '订单管理', icon: 'List' },
      },
      {
        path: 'banners',
        name: 'Banners',
        component: () => import('../views/Banners.vue'),
        meta: { title: '轮播图管理', icon: 'Picture', parent: '营销管理' },
      },
      {
        path: 'points/logs',
        name: 'PointsLogs',
        component: () => import('../views/PointsLogs.vue'),
        meta: { title: '积分流水', icon: 'Coin', parent: '积分管理' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人信息', icon: 'UserFilled', hidden: true },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局前置守卫：除登录页外都需登录
router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()
  document.title = to.meta.title
    ? `${to.meta.title} - 积分商城管理后台`
    : '积分商城管理后台'
  if (to.path === '/login') {
    if (adminStore.isLogin) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }
  if (!adminStore.isLogin) {
    next('/login')
    return
  }
  next()
})

export default router
