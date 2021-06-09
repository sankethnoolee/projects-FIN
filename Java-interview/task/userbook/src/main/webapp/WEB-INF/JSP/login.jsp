<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/userbook.js"></script>
<script>
	var userData = '<c:out value="${model.data}"/>';
</script>
</head>
<body>
	<div id="loginId">
		<span id=loginMsg style='display: none'>user not found</span></br>
		<lable>User Name</lable>
		<input id=userName type="text" name="userName" />
		<lable>Password</lable>
		<input id=password type="password" name="password" />
		<input type="submit" value="submit" onclick="getUserdetails(this)" />
	</div>
	</form>
</body>
</html>