<!DOCTYPE html>
<html>
<head>
<script src="js/lib/jquery-1.12.4.min.js"></script>
<link type="text/css" href="style/font-awesome-4.6.3/css/font-awesome.min.css" rel="stylesheet"></link>
<script type="text/javascript" src="js/framework/validations/api/validationApi.js"></script>
<script src="js/lib/jqueryFileUploader/jquery.ui.widget.js"></script>
<script src="js/lib/jqueryFileUploader/jquery.iframe-transport.js"></script>
<script src="js/lib/jqueryFileUploader/jquery.fileupload.js"></script>
<script>
			$(document).ready(function(){
				try{
					if(csrfTokenX!=''){
				}
				$.ajaxSetup({
					beforeSend: function(xhr, settings) {
						if(settings.type==='POST'){
							xhr.setRequestHeader('X-XToken', csrfTokenX);
						}
					}
				}); 
				}catch(e){

				}
			});
	</script>
<style>
/*loader*/
.overlay {
    height: 100%;
    width: 100%;
    position: fixed;
    z-index: 100000;
    top: 0;
    left: 0;
    background-color: rgb(0,0,0);
    background-color: rgba(0,0,0, 0.5);
    overflow-x: hidden;
    transition: 0.5s;
	display:none;
}
.loader {
  border: 7px solid #f3f3f3;
    border-radius: 50%;
    border-top: 7px solid #00b0f0;
    width: 50px;
    height: 50px;
    -webkit-animation: spin 2s linear infinite;
    animation: spin 2s linear infinite;
    position: absolute;
    top: 48%;
    left: 50%;
}

@-webkit-keyframes spin {
  0% { -webkit-transform: rotate(0deg); }
  100% { -webkit-transform: rotate(360deg); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
/*loader*/
.container {
    display: flex;
}

.column {
   flex: 1;
    height: calc(100vh - 175px);
    padding: 8px;
    background: #fcfeff;
    border: 1px solid #e6e6e6;
    box-sizing: border-box;
    border: 4px solid #E3F2FD;
}

#column-one {
    order: 1;
	min-width : 250px;
	max-width : 300px;
}
#column-two {
    order: 2;
}
#column-three {
    order: 3;
	min-width : 250px;
	max-width : 300px;
}

.headings{
	 font-weight: bold;
}
.api-slab{
	height: 50px;
    border-bottom: 1px solid #E0E0E0;
	cursor : pointer;
	padding: 0 8px 0 8px;
}

.heading-description{
	font-size: 12px;
    border-bottom: 1px solid #757575;
	min-height : 16px;
}
.api-desc{
    width: 85%;
    height: 18px;
    padding: 16px 0 16px 0;
    font-size: 14px;
	float: left;
}

.rq-type-get{
	background:#43A047;
	font-size: 10px;
    padding: 4px 1px 4px 1px;
    text-align: center;
    border-radius: 50px;
}


.rq-type-post{
	background:#FDD835;
	font-size: 10px;
    padding: 4px 1px 4px 1px;
    text-align: center;
    border-radius: 50px;
}

.rq-type{
	width: 15%;
    height: 18px;
    padding: 16px 0 16px 0;
    font-size: 14px;
	float: left;
	color:white;
}

.api-container{
	overflow: auto;
    height: 300px;
}

#key-value-table {
  margin-top: 8px;
  font-size: 12px;
  border-collapse: collapse;
  width: 100%;
}

#key-value-table td, #key-value-table th {
  border: 1px solid #ddd;
  padding: 4px 8px 4px 8px;
}

#key-value-table tr:nth-child(even){background-color: #f2f2f2;}

#key-value-table tr:hover {background-color: #ddd;}

#key-value-table th {
  padding-top: 8px;
  padding-bottom: 8px;
  text-align: left;
  background-color: #0D47A1;
  color: white;
}

.kv-input{
	width : 98%;
}
.button {
  border: none;
    color: white;
    padding: 8px 16px 8px 24px;
    text-align: center;
    text-decoration: none;
    display: inline-block;
    font-size: 12px;
    margin: 4px 2px;
    cursor: pointer;
    float: right;
    font-weight: bold;
    border-radius: 8px;
}
.button2 {background-color: #E64A19;}
.selected{
	background: #f0f0ec;
}

.button i{
	font-size: 16px;
    padding-left: 16px;
}
</style>

</head>
<body style="font-family:Segoe UI;">
<div style="
    font-size: 16px;
    padding-left: 8px;
    font-weight: bold;
">
Validation APIs
</div>
<div class="container">
   <div class = "column" id="column-one">
		<div class = "headings"> API
		</div>
		<div class = "heading-description"> List of APIs
		</div>
		<div class = "api-container"> 
			<div class="api-slab" onclick = "downloadValidationTemplateLayout(this)">
                <div class="api-desc">Download Validation Template</div>
                <div class="rq-type"><div class="rq-type-get">GET</div></div>
            </div>
			<div class="api-slab" onclick = "uploadValidationLayout(this)">
                <div class="api-desc">Upload Validations</div>
                <div class="rq-type"><div class="rq-type-post">POST</div></div>
            </div>	
			<div class="api-slab" onclick = "exportValidationLayout(this)">
                <div class="api-desc">Export Validations Data</div>
                <div class="rq-type"><div class="rq-type-get">GET</div></div>
            </div>
		</div>
		
   </div>
   <div class = "column" id="column-two">
		<div class = "headings"> Details
		</div>
		<div class = "heading-description"> Enter the required values
		</div>
		<div id = "kvContainer">
			<table id="key-value-table">
			  <tr>
				<th>Key</th>
				<th>Value</th>
			  </tr>
			  <tr>
				<td><input class = "kv-input"/></td>
				<td><input class = "kv-input"/></td>
			  </tr>
			</table>
		</div>
		
	</div>
   <div class = "column" id="column-three">
		<div class = "headings"> Response
		</div>
		<div class = "heading-description">
		</div>
		<div id = "respContent">
		</div>
   </div>
</div>
<div id="hiddenDownloaderID">
	<iframe id="hiddenDownloader" style="display: none;"></iframe>
</div>
<div class="overlay" style="display: none;">
		<div class="loader"></div>
</div>
</body>
</html>