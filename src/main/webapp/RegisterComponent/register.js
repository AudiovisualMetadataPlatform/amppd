
var registerForm = Vue.component('register-form',
  {
    template: '#register', // should match the ID of template tag
    name: 'RegisterComponent',
  	data: function() {
      return {
		errors: [],
		name: null,
		pswd: null,
		confirm_pswd: null
    }
  },
  methods:{
    validateRegisterForm: function(e) {

      this.errors = [];

      if (!this.name) {
        this.errors.push('Name required.');
      }
      if (!this.pswd) {
        this.errors.push('Password required.');
      }
	  if (!this.confirm_pswd) {
        this.errors.push('Confirm Password required.');
	  }
	  if (this.pswd && this.confirm_pswd && this.confirm_pswd != this.pswd) {
        this.errors.push('Passwords do not match.');
      }
      if (this.errors == [])
      {
        
      }
      this.$router.push("/welcome");
      console.log("checkform WORKS");
      e.preventDefault();
    }
  },
  mounted() {
            console.log("IT WORKS");
        }
  });





