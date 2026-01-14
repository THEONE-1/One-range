import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/views/Layout.vue'),
      redirect: '/home',
      children: [
        {
          path: '/home',
          name: 'Home',
          component: () => import('@/views/Home.vue')
        },
        {
          path: '/progress',
          name: 'Progress',
          component: () => import('@/views/Progress.vue')
        },
        {
          path: '/users',
          name: 'UserManagement',
          component: () => import('@/views/UserManagement.vue')
        },
        // XSS
        {
          path: '/xss/reflect',
          name: 'XSSReflect',
          component: () => import('@/views/vulns/XSSReflect.vue')
        },
        {
          path: '/xss/store',
          name: 'XSSStore',
          component: () => import('@/views/vulns/XSSStore.vue')
        },
        {
          path: '/xss/dom',
          name: 'XSSDOM',
          component: () => import('@/views/vulns/XSSDOM.vue')
        },
        // SQL注入
        {
          path: '/sqli/jdbc_int_based',
          name: 'SQLIntBased',
          component: () => import('@/views/vulns/SQLIntBased.vue')
        },
        {
          path: '/sqli/jdbc_error_based',
          name: 'SQLErrorBased',
          component: () => import('@/views/vulns/SQLErrorBased.vue')
        },
        {
          path: '/sqli/jdbc_blind_time_based',
          name: 'SQLTimeBased',
          component: () => import('@/views/vulns/SQLTimeBased.vue')
        },
        {
          path: '/sqli/jdbc_blind_bool_based',
          name: 'SQLBoolBased',
          component: () => import('@/views/vulns/SQLBoolBased.vue')
        },
        // RCE
        {
          path: '/rce/runtime',
          name: 'RCERuntime',
          component: () => import('@/views/vulns/RCERuntime.vue')
        },
        {
          path: '/rce/processbuilder',
          name: 'RCEProcessBuilder',
          component: () => import('@/views/vulns/RCEProcessBuilder.vue')
        },
        // 文件操作
        {
          path: '/file/upload',
          name: 'FileUpload',
          component: () => import('@/views/vulns/FileUpload.vue')
        },
        {
          path: '/file/download',
          name: 'FileDownload',
          component: () => import('@/views/vulns/FileDownload.vue')
        },
        // XXE
        {
          path: '/xxe/xmlreader',
          name: 'XXEXMLReader',
          component: () => import('@/views/vulns/XXEXMLReader.vue')
        },
        {
          path: '/xxe/saxreader',
          name: 'XXESAXReader',
          component: () => import('@/views/vulns/XXESAXReader.vue')
        },
        {
          path: '/xxe/saxbuilder',
          name: 'XXESAXBuilder',
          component: () => import('@/views/vulns/XXESAXBuilder.vue')
        },
        // 其他
        {
          path: '/ssrf',
          name: 'SSRF',
          component: () => import('@/views/vulns/SSRF.vue')
        },
        {
          path: '/xff',
          name: 'XFF',
          component: () => import('@/views/vulns/XFF.vue')
        },
        {
          path: '/cors',
          name: 'CORS',
          component: () => import('@/views/vulns/CORS.vue')
        },
        {
          path: '/redirect',
          name: 'Redirect',
          component: () => import('@/views/vulns/Redirect.vue')
        },
        {
          path: '/unauth/info',
          name: 'Unauth',
          component: () => import('@/views/vulns/Unauth.vue')
        },
        // 组件漏洞
        {
          path: '/fastjson',
          name: 'Fastjson',
          component: () => import('@/views/vulns/Fastjson.vue')
        },
        {
          path: '/jackson',
          name: 'Jackson',
          component: () => import('@/views/vulns/Jackson.vue')
        },
        {
          path: '/shiro',
          name: 'Shiro',
          component: () => import('@/views/vulns/Shiro.vue')
        },
        {
          path: '/log4j',
          name: 'Log4j',
          component: () => import('@/views/vulns/Log4j.vue')
        },
        {
          path: '/actuators/index',
          name: 'Actuators',
          component: () => import('@/views/vulns/Actuators.vue')
        },
        // 反序列化
        {
          path: '/deserialize/readobject',
          name: 'ReadObject',
          component: () => import('@/views/vulns/ReadObject.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('username')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/home')
  } else {
    next()
  }
})

export default router


