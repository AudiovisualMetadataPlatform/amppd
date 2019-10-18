
var loginForm = Vue.component('login-form',
  {
    template: '#app', // should match the ID of template tag
    name: 'LoginComponent',
  	data: function() {
      return {
		errors: [],
		name: null,
    pswd: null
    }
  },
  methods:{
    checkForm: function(e) {
      /* if (this.name && this.pswd) {
        return true;
      } */

      this.errors = [];

      if (!this.name) {
        this.errors.push('Name required.');
      }
      if (!this.pswd) {
        this.errors.push('Password required.');
      }

      if (this.errors == [])
      {
        
      }
      console.log("checkform WORKS");
      e.preventDefault();
    }
  },
  mounted() {
            console.log("IT WORKS");
        }
  });





