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
      }
]

const router = new VueRouter({
  routes // short for `routes: routes`
})

var vue_app = new Vue(
  {
    router: router,
  }).$mount('#accountContainer');

//export default router;
