<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/lib/font-awesome-4.6.3/css/font-awesome.min.css" />
<script  src="js/lib/jquery-3.2.1.min.js"></script>
<script src="js/lib/xor128.min.js"></script>
<script type="text/javascript"  src="js/lib/d3.js"></script>
<script type="text/javascript" src="js/lib/seedrandom.min.js"></script>
<script type="text/javascript" src="js/lib/tinysort.min.js"></script>

<link rel="stylesheet" type="text/css" href="css/framework/pageContents/dataLineage.css" />
<script src="js/framework/pageContents/dldUtils.js" ></script>
<script type="text/javascript">
			var cbd="${model.cbd}";
			var clientCode="${model.clientCode}";
			
</script>
<script src="js/framework/pageContents/dataLineage.js" ></script>



</head>
<script type="text/javascript">
			var userSessionToken="${model.test}";
			window.onload = function(){
			 var selfurl = self.location.href;
			var testingURL = selfurl.substring(selfurl.lastIndexOf("/"),selfurl.indexOf("?"))
			if ( selfurl.indexOf("/lineage?")<0){
				top.location.href ="login";
			}
			};

</script>	
<!--loader-->
	<div  class="overlay">
		<div class="loader"></div>
	</div>
	<!--loader-->




<div id = "main" STYLE = "    display: inline-block;
    vertical-align: top;
    height: auto;
    width: max-content;">
	
	<div class= "sourceSystemParent parentS levelContainers">
	<div style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;    margin-bottom: 35px;" class = "levelHead">Data Source</div>
	</div>
	<div class= "dataRepoParent parentS">
	<div style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;" id = "dataRepoText" class = "levelHead">Data Repository</div>
	</div>
	<div class= "dataConsumerParent parentS levelContainers">
	<div style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    margin-bottom: 35px;" class = "levelHead">Data Consumers</div>
	</div>
		


</div>
<div class="absolute tooltip">
	<div id="sourceForInfo">Sources</div>
	<div id="taskForInfo">Tasks</div>
	<div id="statusForInfo">Status</div>

</div>
</html>