$(document).ready(function(){
	
});
function getUserdetails(e) {
	 $.ajax({
		 url: "getuserdetails.htm",
		 type:"GET",
		 dataType: 'json',
		 data:{
			 userName:$("userName").val(),
			 password:$("password").val()
		 },
		 success: function(result){
			 $.ajax({
			        url: "getgetuserdetailsView",
			        type: 'GET',
			        dataType: 'json',
			        success: function(res) {
			            console.log(res);
			            alert(res);
			        }
			    });
		  }
	 });
}