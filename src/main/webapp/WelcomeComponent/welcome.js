
var welcomeForm = Vue.component('welcome-form',
  {
    template: '#welcome', // should match the ID of template tag
    name: 'WelcomeComponent',
  	data: function() {
      return {
		errors: [],
		name: null,
    pswd: null
    }
  },
  methods:{
    signout: function(e) {
      
      this.$router.push("/");
    }
  },
  mounted() {
            console.log("IT WORKS");
        }
  });





