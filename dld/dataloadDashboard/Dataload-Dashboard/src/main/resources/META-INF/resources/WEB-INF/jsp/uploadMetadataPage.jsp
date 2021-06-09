<%@ include file="header.jsp"%>

<!--lib and css -->
<script src="js/lib/jquery-3.2.1.min.js"></script>
<script src="js/lib/jquery-ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/lib/font-awesome-4.6.3/css/font-awesome.min.css" />
<link rel="stylesheet" charset="utf-8" type="text/css" href="js/lib/highChart/grouped-categories.css"/>
<script type="text/javascript" src="js/lib/radial-progress-bar.js"></script>
<script type="text/javascript" src="js/lib/highChart/highstock.js"></script>
<script type="text/javascript" src="js/lib/highChart/highcharts-more.js"></script>
<link rel="stylesheet" type="text/css" href="js/lib/bootstrap-datepicker/css/datepicker.css" />
<script type="text/javascript" src="js/lib/bootstrap-datepicker/js/bootstrap-datepicker.js"></script>
<script src="js/lib/highChart/grouped-categories.js"></script>
<script src="js/lib/highChart/export-csv.js"></script>	
<script type="text/javascript" src="js/lib/moment-with-locales.min.js"></script>
<!-- <script src="js/framework/pageContents/landingPage.js" ></script> -->
<link rel="stylesheet" href="css/framework/pageContents/uploader.css" />
 <!-- The basic File Upload plugin -->
<script src="js/lib/jquery.fileupload.js"></script>
<!-- JQuery-18n plugin for internationalization -->
<script type="text/javascript" language="JavaScript" src="js/lib/jquery.i18n.properties-min.js"></script>


<!-- JS Files -->
<script src="js/framework/pageContents/uploader.js" type="text/javascript"></script>
<script type="text/javascript">
       // var currentBusinessDate ='<c:out value="${model.currentBusinessDate}"/>';
       var currentBusinessDate = "01-01-2016";
</script>

<script>
    //Moving text contents to the property file
     jQuery.i18n.properties({
         name:'uploaderProperties', 
         path:'./js/framework/pageContents/', 
         mode:'both',
         language:'en-US',
         callback: function() {
         }
     });
</script>



<body onLoad="">
	<div id="userSessionToken" style="display:none;"></div>
	<div style="height: 30px;">
		<div id="bulkUploader" class="bulkRulesUploader" style=""></div>
		<div id="uploaderFileName" class="rulesUploaderFilename" style=""></div>
	</div>
	<div id="uploaderParent" class="mainParent" style="">
    	<div id="ajaxMaskForLoad" class="ajaxMask">Loading...</div>
    	<div id="ajaxMaskForVal" class="ajaxMask">Validating...</div>
    	<div id="ajaxMaskForSave" class="ajaxMask">Saving...</div>
		<div class="buttonList" style="">
			<div id="downloadTemplate" class="downloadTemplateButton" style="">Download Metadata</div>
			<div style="float:left;margin: 10px; ">
				<form name="fileUploaderForm" id="fileUploaderForm" class="fileUploadForm" enctype="multipart/form-data" method="post" accept-charset="utf-8">
					<span class="uploadBtn btn-upload fileinput-button">
					<span id="uploadFile">Upload File</span>
					<input id="upload" type="file" name="fileName" multiple="">
					</span>
				</form>
			</div>
			<div id="validateFile" class="validateFileButton" style=""></div>
			<div id="downloadReport" class="downloadReportButton" style=""></div>
			<div id="saveFile" class="saveFileButton" style=""></div>
<!-- 			<div id="datePickerId"  class="datePickerClass" style=""> -->
<!-- 				<div id="effectiveFromId" class="effectiveFrom" style=""></div> -->
<!-- 				<input type="text" id="effectiveDate" class="datepickerComponent" Placeholder="dd-mm-yyyy" style=""/> -->
<!-- 			</div> -->

			<div id = "cbdDateComponentParent">
			<div id = "cbdHeading"> Business Date</div>
					<div id = "cbdDecrement" class =" fa fa-chevron-left" onclick = "decrementDate();"></div>
					<div id = "cbdCustomDisplayParent" >
						<div class = "cbdDateDay">24</div>
						<div class = "cbdDateMonth">May</div>
						<div class = "cbdDateYear">2017</div>
						<div class = "cbdDateWeek">Wednesday</div>
						<input id = "cbdDatePicker"></input>
					</div>
					<div id = "cbdIncrement" class =" fa fa-chevron-right" onclick = "incrementDate();"></div>
			</div>
			<div class ="errorMessage">
				<p id="errorMessageId" style="font-size: 12px;margin-top: -17px;"></p>
			</div>
			<div class ="warningMessage">
				<p id="warningMessageId" style="font-size: 13px;color: red;font-weight: bold;display:none;"></p>
			</div>
			<div class ="saveMessage">
				<p id="saveMessageId" style="font-size: 12px;"></p>
			</div>
			<div class ="finalMessage">
				<p id="finalMessageId" style="font-size: 12px;"></p>
			</div>
			<div class ="finalErrorMessage">
				<p id="finalErrorMessageId" style="font-size: 12px;color:red;"></p>
			</div>
		</div>
		<div class="fileCount" style="">
			<div id="entityMasterCount" class="blueCountBlock"></div>
			<div id="entityMasterId" class="labelStyleForCount"></div>
			<div id="entityOwnerCount" class="blueCountBlock"></div>
			<div id="entityOwnerId" class="labelStyleForCount"></div>
			<div id="flowTypeCount" class="blueCountBlock"></div>
			<div id="flowTypeId" class="labelStyleForCount"></div>
			<div id="taskRepositoryCount" class="blueCountBlock"></div>
			<div id="taskRepositoryId" class="labelStyleForCount"></div>
			<div id="taskMasterCount" class="blueCountBlock"></div>
			<div id="taskMasterId" class="labelStyleForCount"></div>
			<div id="entityDetailCount" class="blueCountBlock"></div>
			<div id="entityDetailId" class="labelStyleForCount"></div>
		</div>
		<div id="validationParent" class="valStatusMenu">
			<div id="validationStatusBar" class="validationStatusBar">
				<div id="valStatusLabelId" class="valStatusLabel"></div>
				<div id="hideForVal" class="hideOrShow" style="">Show</div>
			</div>
			<div class="valStatusDetails">
				<div id="validationSuccessView" class="validationStatusView" >
					<div id="entityMasterValStatus" class="greeenCountBlock"></div>
					<div id="labelStyleForStatus2" class="labelStyleForStatus"></div>
					<div id="entityOwnerValStatus" class="greeenCountBlock"></div>
					<div id="labelStyleForStatus3" class="labelStyleForStatus"></div>
					<div id="flowTypeValStatus" class="greeenCountBlock"></div>
					<div id="labelStyleForStatus4" class="labelStyleForStatus"></div>
					<div id="taskRepositoryValStatus" class="greeenCountBlock"></div>
					<div id="labelStyleForStatus11" class="labelStyleForStatus"></div> 
					<div id="taskMasterValStatus" class="greeenCountBlock"></div>
					<div id="labelStyleForStatus9" class="labelStyleForStatus"></div> 
					<div id="entityDetailValStatus" class="greeenCountBlock"></div>
					<div id="labelStyleForStatus13" class="labelStyleForStatus"></div> 
					
				</div>
				<div id="validationErrorView" class="validationStatusView">
					<div id="entityMasterValErrorStatus" class="redCountBlock"></div>
					<div id="labelStyleForStatus6" class="labelStyleForStatus"></div>
					<div id="entityOwnerValErrorStatus" class="redCountBlock"></div>
					<div id="labelStyleForStatus7" class="labelStyleForStatus"></div>
					<div id="flowTypeValErrorStatus"class="redCountBlock"></div>
					<div id="labelStyleForStatus8" class="labelStyleForStatus"></div>
					<div id="taskRepositoryValErrorStatus"class="redCountBlock"></div>
					<div id="labelStyleForStatus12" class="labelStyleForStatus"></div>
					<div id="taskMasterValErrorStatus"class="redCountBlock"></div>
					<div id="labelStyleForStatus10" class="labelStyleForStatus"></div>
					<div id="entityDetailValErrorStatus"class="redCountBlock"></div>
					<div id="labelStyleForStatus14" class="labelStyleForStatus"></div>
				</div>
			</div>
		</div>
		<div class="helpNoteIndex">
			<div style="margin-left:10px;">
				<p id="helpNotes" style="margin-bottom:15px;width: 110px;"></p>
				<p id="helpIndex1"></p>
				<p id="helpIndex2"></p>
				<p id="helpIndex3"></p>
				<p id="helpIndex4"></p>
				<p id="helpIndex5"></p>
				<p id="helpIndex6"></p>
				<p id="helpIndex7"></p>
				<p id="helpIndex8"></p>
			</div>
		</div>
	</div>
	<div class="sheetHeaderValidation" style="">
		<div id="fileFormatError" class="fileFormatError"></div>
		<div id="tagHeaderVal" class="sheetHeaderval"></div>
		<div id="ruleHeaderVal" class="sheetHeaderval"></div>
		<div id="ruleMetricHeaderVal" class="sheetHeaderval"></div>
		<div id="ruleFilterHeaderVal" class="sheetHeaderval"></div>
		<div id="derivedRuleHeaderVal" class="sheetHeaderval"></div>
		<div id="lineRuleHeaderVal" class="sheetHeaderval"></div>
		<div id="lineRuleFilterHeaderVal" class="sheetHeaderval"></div>
	</div>
	<div id="uploadedFileContainer" style="display:none">
		<iframe id="uploaderFileFrame" src="" frameborder="0" height="360" width="100%"></iframe>
	</div>
	<div id="hiddenDownloaderID">
		<iframe id="hiddenDownloader" style="display:none;"></iframe>
	</div>
</body>








<%@ include file="footer.jsp"%>