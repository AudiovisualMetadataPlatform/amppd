<template id = "app">
	<!-- @submit handles any form of submission. -->
    <!-- .prevent keeps the event from bubbling around and doing anything else. -->
    <!-- <form @submit.prevent="login"> -->
	<form id="app" v-on:submit="checkForm" action="/RegisterComponent/register.html" method="post">
	<div class="form">
		<div class="imgcontainer">
			<img src="Images/amp_header.png" >
		</div>
		<!-- <p v-if="errors.length">
			<b>Please correct the following error(s):</b>
			<ul>
				<li v-for="error in errors">{{ error }}</li>
			</ul>
		</p> -->
		<div class="container" id="login">
				<div class="row"><input id="name" v-model="name" type="text" placeholder="Enter Username" name="name"> </div>
				<div class="row"><input id="pswd" v-model="pswd" type="password" placeholder="Enter Password" name="pswd"></div> 
				<div class="row"><label><input type="checkbox" checked="checked" name="remember"> Remember me</label></div>
				<div class="row"><span><a href="#">Forgot Password?</a></span></div>
				<div class="row"><button type = "submit">Sign In</button></div>
				<div class="row"><label>Or</label></div>
				<!-- <div class="row"><button type="submit">Sign Up</button></div> -->
		
		</div>
	</div>
	<script src="LoginComponent/login.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.21.1/babel.min.js"></script>
			
	</form>
</template>


<!--Javascript -->
<script>
export default {
	name: 'LoginComponent',
  	data: {
		errors: [],
		name: null,
		pswd: null
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
}
</script>


<!-- CSS -->
<style scoped>
	form {
	/* border: 3px solid #f1f1f1; */
	height:auto;
	width:auto;
	
	}

	/* Full-width inputs */
	input[type=text], input[type=password] {
	border-radius: 5px;
	padding: 15px 20px;
	margin: 20px 20px;
	display: inline-block;
	border: 1px solid #ccc;
	box-sizing: border-box;
	align-content: center;
	}

	/* Set a style for all buttons */
	button {
	background-color: #2C5B7F;
	color: #E9972D;
	font:bold;
	padding: 14px 20px;
	margin-left: 20px;
	margin-right: 20px;
	margin-top: 20px;
	margin-bottom: 20px;
	border: none;
	cursor: pointer;
	border-radius: 15px;
	}

	/* Add a hover effect for buttons */
	button:hover {
	opacity: 0.8;
	}

	/* Center the avatar image inside this container */
	.imgcontainer {
	width: 100%;

	}

</style>