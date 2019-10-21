//import Vue from ''
//import Router from 'vue-router'
//import LoginComponent from "App"

/* Vue.use(Router) */


const routes = [
  
  {
        path: '/',
        component: loginForm,
        name: 'LOGIN'
      },
     {
        path: '/register', 
        component: registerForm,
        name: 'REGISTER'
      },
      {
        path: '/welcome', 
        component: welcomeForm,
        name: 'WELCOME'
      }
]

const router = new VueRouter({
  routes // short for `routes: routes`
})

var vue_app = new Vue(
  {
    router: router,
  }).$mount('#amp_app');

//export default router;
