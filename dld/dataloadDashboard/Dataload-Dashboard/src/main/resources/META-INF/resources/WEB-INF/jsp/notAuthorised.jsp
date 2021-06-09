<link rel="stylesheet" type="text/css" href="css/framework/basicConfig/logout.css" />
<script type="text/javascript"> 
        function submitform() {   document.logoutForm.submit(); } 
		
</script> 

<div class=" bizscoreBackgrnd">
	<div class= "orgLogo" style="width:100%;height:125px"></div>
	<div id="errorMsgBox" style="width:100%;height:30px;padding-top:20px;">
		<div style = "width:470px !important;" class="login-errormsg-box">
			<div style=" float: left;">You are currently not authourized for this functionality. Please contact admin!! or  go back to &nbsp;</div>
			<form style=" float: left;" action="/dldwebapplication/login" method="GET" name="logoutForm">
				<a href="javascript: submitform()" > Login </a>
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
			</form> 
			<div>&nbsp; page.</div>
	</div>
</div>
</div>
<%@ include file="footer.jsp"%>