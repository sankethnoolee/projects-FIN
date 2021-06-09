<link rel="stylesheet" type="text/css" href="css/framework/basicConfig/header.css" />
<link rel="icon" href="images/favicon">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<!-- For Menu -->
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="css/lib/bootstrap.min.css">
  <script src="js/lib/jquery-3.2.1.min.js"></script>
  <script src="js/lib/bootstrap/js/bootstrap.min.js"></script>
<title>Fintellix Platform - Data Load Dashboard</title>
<script type="text/javascript"> 
        function submitform() {   document.logoutForm.submit(); } 
		
		function checkForLogOut(){
			if (confirm('Are you sure you want to logout?')) {
				submitform();
			}
		}
		
</script> 
<%
String username="";
String userRole="";
String solutionName="";

if (session.getAttribute("username")!=null)
	username=session.getAttribute("username").toString(); 
if (session.getAttribute("userRole")!=null)
	userRole=session.getAttribute("userRole").toString();
if (session.getAttribute("solutionName")!=null)
	solutionName=session.getAttribute("solutionName").toString(); 
  
%>
 <div id = "header-fintellix" style="width:99%;height:65px;">
	<div class="logo-box-header" style="float:left;height:30px;    margin-bottom: 8px;"></div>
	<div id="header-menu-user" style="" class = "userDetails">
		<div style = "float:right;margin-right:20px;">
			<form action="/dldwebapplication/logout" method="GET" name="logoutForm">
				<a href="javascript: checkForLogOut()" style = "color: black !important;text-decoration: none !important;">Logout</a>
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
			</form>
		</div>
		<div style = "float:right;margin-right:10px;margin-left:10px;">|</div>
		<div style = "float:right;" id="header-username">Solution Name : <b><%=solutionName%></b></div>
		<div style = "float:right;margin-right:10px;margin-left:10px;">|</div>
		<div style = "float:right;" id="header-username">Welcome : <b><%=username%></b></div>
	</div>
	<div class="dropdown" style="margin-left: .5%; clear:both;width:99%;height:50%;">
    <a  href="dldLandingPage" class="btn btn-default dropdown-toggle navbuttons" role="button" >Dashboard</a>
    <a  href="uploadMetadataPage" class="btn btn-default dropdown-toggle navbuttons" role="button" >Upload Metadata</a>
	</div>
</div>
<!-- <div class="container"> -->
  	