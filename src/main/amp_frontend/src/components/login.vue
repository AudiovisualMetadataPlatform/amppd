<template>
    <div>
        This is login
    </div>
</template>

<script>
import dataService from '../service/dataService';
export default {
	name: "InstructorApp",
	data() {
		return {
			INSTRUCTOR: "in28minutes"
		};
	},
	methods: {
		getCreds(){
			dataService.getUser()
			.then(response => {
				console.log(response.data);

			});
		}
	},
	created(){
		this.getCreds();
	}
};
</script>

<style scoped>

</style>