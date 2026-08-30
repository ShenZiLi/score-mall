import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/', redirect: '/home' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/home',
    name: 'HomePage',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页', tab: 'home' },
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('../views/Category.vue'),
    meta: { title: '分类', tab: 'category' },
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('../views/Products.vue'),
    meta: { title: '商品列表' },
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
    meta: { title: '商品详情' },
  },
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: () => import('../views/OrderConfirm.vue'),
    meta: { title: '确认订单', requiresAuth: true },
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { title: '我的订单', requiresAuth: true },
  },
  {
    path: '/sign',
    name: 'Sign',
    component: () => import('../views/Sign.vue'),
    meta: { title: '签到', tab: 'sign', requiresAuth: true },
  },
  {
    path: '/points/logs',
    name: 'PointsLogs',
    component: () => import('../views/PointsLogs.vue'),
    meta: { title: '积分明细', requiresAuth: true },
  },
  {
    path: '/addresses',
    name: 'Addresses',
    component: () => import('../views/Addresses.vue'),
    meta: { title: '收货地址', requiresAuth: true },
  },
  {
    path: '/address/edit',
    name: 'AddressEdit',
    component: () => import('../views/AddressEdit.vue'),
    meta: { title: '编辑地址', requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: '我的', tab: 'profile', requiresAuth: true },
  },
  {
    path: '/profile/edit',
    name: 'ProfileEdit',
    component: () => import('../views/ProfileEdit.vue'),
    meta: { title: '修改资料', requiresAuth: true },
  },
  {
    path: '/profile/password',
    name: 'ProfilePassword',
    component: () => import('../views/ProfilePassword.vue'),
    meta: { title: '修改密码', requiresAuth: true },
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 积分商城` : '积分商城'
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }
  next()
})

export default router
