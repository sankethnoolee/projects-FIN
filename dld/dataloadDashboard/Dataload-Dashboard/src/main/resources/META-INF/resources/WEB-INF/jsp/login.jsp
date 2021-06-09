<!--<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>-->
<!DOCTYPE html>

<head>




<link href="css/app.css" rel="stylesheet" type="text/css"/> 
<link rel="icon" href="images/favicon">
<link rel="stylesheet" type="text/css" href="css/framework/basicConfig/login.css" />
	<title>Fintellix Platform - Data Load Dashboard</title>
</head>



<body class = "security-app bizscoreBackgrnd1">
<div class=" bizscoreBackgrnd">
	<div class= "orgLogo" style="width:100%;height:125px">
			</div>
	<div class="details">
		<div class = "headingStyle" style = "font-size:25px;color: #696868;">Data Load Dashboard Login</div>
		<c:if test="${param.error ne null}">
		<div id="errorMsgBox" style="width:100%;height:30px;padding-top:20px;">
			<div class="login-errormsg-box">Could not authenticate user.Contact admin!!</div>
		</div>
		</c:if>
		<c:if test="${param.logout ne null}">
		<div id="errorMsgBox" style="width:100%;height:30px;padding-top:20px;">
			<div class="login-errormsg-box">You have been logged out.</div>
		</div>
		</c:if>
	
	</div>
	
	<form action="/dldwebapplication/login" method="post">

		<div class="lc-block">
			<div class = "helperText">Please enter your Login details below</div>
			<div>
				<input type="text" class="style-4" name="username"
					placeholder="User Name" />
			</div>
			<div>
				<input type="password" class="style-4" name="password"
					placeholder="Password" />
			</div>
			<div>
				<select class = "solutionNameIp" name="solutionName" style = "width: 100%;margin-bottom:0px !important;">
				    
				    
				 </select>
			</div>
			<div>
				<input style = "float: right;margin-right: 0px !important;" type="submit" value="Login" class="button red small" />
			</div>
			
			
			
		</div>
		<input type="hidden" name="${_csrf.parameterName}"
			value="${_csrf.token}" />
	</form>

</div>
</body>
<script type="text/javascript">
			var sols='${model.solutions}';
			var solArray=JSON.parse(sols);
			var elm = document.getElementsByClassName("solutionNameIp")[0];
			for(var i = 0;i< solArray.length;i++){
				
				elm.innerHTML += '<option value="'+solArray[i]+'">'+solArray[i]+'</option>';
				    
			}
			
</script>
<%@ include file="footer.jsp"%>