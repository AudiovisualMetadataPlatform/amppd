/**
 * for mapping the routes
 */

import Vue from 'vue';
import Router from 'vue-router';

import HomePage from '../home.html'
import LoginPage from '../index.html'
import RegisterPage from '../RegisterComponent/register.html'

Vue.use(Router);

export const router = new Router({
  mode: 'history',
  routes: [
    { path: '/', component: HomePage },
    { path: '/index', component: LoginPage },
    { path: '/register', component: RegisterPage },

    // otherwise redirect to home
    { path: '*', redirect: '/' }
  ]
});