<template>
  <div id="app">
   
    <InstructorApp />
    
  </div>
</template>

<script>
import InstructorApp from './components/login.vue'
export default {
  name: 'In28Minutes',
  components: {
  InstructorApp

}}
</script>

<style>
#app {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
