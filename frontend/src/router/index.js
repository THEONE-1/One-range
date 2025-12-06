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
        // {
        //   path: '/sqli/mybatis_like',
        //   name: 'MyBatisLike',
        //   component: () => import('@/views/vulns/MyBatisLike.vue')
        // },
        {
          path: '/sqli/jdbc_blind_bool_based',
          name: 'SQLBoolBased',
          component: () => import('@/views/vulns/SQLBoolBased.vue')
        },
        {
          path: '/sqli/mybatis_orderby',
          name: 'MyBatisOrderBy',
          component: () => import('@/views/vulns/MyBatisOrderBy.vue')
        },
        {
          path: '/sqli/mybatis_in',
          name: 'MyBatisIn',
          component: () => import('@/views/vulns/MyBatisIn.vue')
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
        {
          path: '/rce/processimpl',
          name: 'RCEProcessImpl',
          component: () => import('@/views/vulns/RCEProcessImpl.vue')
        },
        {
          path: '/rce/loadjs',
          name: 'RCELoadJs',
          component: () => import('@/views/vulns/RCELoadJs.vue')
        },
        {
          path: '/rce/groovy',
          name: 'RCEGroovy',
          component: () => import('@/views/vulns/RCEGroovy.vue')
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
        {
          path: '/xxe/documentbuilder',
          name: 'XXEDocumentBuilder',
          component: () => import('@/views/vulns/XXEDocumentBuilder.vue')
        },
        {
          path: '/xxe/unmarshaller',
          name: 'XXEUnmarshaller',
          component: () => import('@/views/vulns/XXEUnmarshaller.vue')
        },
        // SSTI - 已禁用
        // {
        //   path: '/ssti/thymeleaf',
        //   name: 'SSTIThymeleaf',
        //   component: () => import('@/views/vulns/SSTIThymeleaf.vue')
        // },
        // {
        //   path: '/ssti/noreturn',
        //   name: 'SSTINoReturn',
        //   component: () => import('@/views/vulns/SSTINoReturn.vue')
        // },
        // 其他
        // SpEL - 已禁用
        // {
        //   path: '/spel',
        //   name: 'SpEL',
        //   component: () => import('@/views/vulns/SpEL.vue')
        // },
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
          path: '/xstream',
          name: 'XStream',
          component: () => import('@/views/vulns/XStream.vue')
        },
        {
          path: '/actuators/index',
          name: 'Actuators',
          component: () => import('@/views/vulns/Actuators.vue')
        },
        {
          path: '/actuators/jolokialogback',
          name: 'Jolokia',
          component: () => import('@/views/vulns/Jolokia.vue')
        },
        // 反序列化
        {
          path: '/deserialize/readobject',
          name: 'ReadObject',
          component: () => import('@/views/vulns/ReadObject.vue')
        },
        {
          path: '/deserialize/rmi',
          name: 'RMI',
          component: () => import('@/views/vulns/RMI.vue')
        },
        {
          path: '/deserialize/xmldecoder',
          name: 'XMLDecoder',
          component: () => import('@/views/vulns/XMLDecoder.vue')
        },
        {
          path: '/deserialize/yaml',
          name: 'YAML',
          component: () => import('@/views/vulns/YAML.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('username')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/home')
  } else {
    next()
  }
})

export default router


