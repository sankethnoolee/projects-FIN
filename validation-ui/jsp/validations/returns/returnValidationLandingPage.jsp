<%@ include	file="/WEB-INF/jsp/platformcore/common/utils/commonIncludeSwf.jsp"%>
<%@ include file="/WEB-INF/jsp/platformcore/platformconfig/csrfheader.jsp" %>

<head>
	<link rel="stylesheet" type="text/css" href="style/font-awesome-4.6.3/css/font-awesome.min.css" />
	<link rel="stylesheet" href="style/kendo/kendo.common-material.min.css" />
	<link rel="stylesheet" href="style/kendo/kendo.material.min.css" />
	<link rel="stylesheet" href="style/kendo/kendo.material.mobile.min.css" />
	<link rel="stylesheet" type="text/css" href="js/lib/bootstrap-datepicker/css/datepicker.css" />
	<!--<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/returnValidation.css" />	-->
	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/returnValidationLandingPage.css"/>
	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/returnValidationCreateAndEdit.css" />
	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/vs.css" />

	<!-- <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
 <link rel="stylesheet" type="text/css" href="http://code.jquery.com/ui/1.10.4/themes/ui-lightness/jquery-ui.css"/>
 <script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>-->
 	<script src="js/framework/validations/returns/jquery-1.11.0.min.js"></script>
 	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/jquery-u.1.10.4.css"/>
 	<script src="js/framework/validations/returns/jquery-ui-1.10.4.min.js"></script>
	
	
	<script src="js/lib/jqueryFileUploader/jquery.ui.widget.js"></script>
	<script src="js/lib/jqueryFileUploader/jquery.iframe-transport.js"></script>
	<script src="js/lib/jqueryFileUploader/jquery.fileupload.js"></script>
	<script src="js/lib/kendo/kendo.all.min.js"></script>
	<script type="text/javascript" src="js/lib/moment-with-locales.min.js"></script>
	<script type="text/javascript" src="js/platformcore/administration/utils/bootstrap-datepicker.js"></script>
	<script src="js/lib/jsonpath-0.8.0.js"></script>
	
	<script src="js/framework/reportcontainer/bootstrap-min/bootstrap-popper.js"></script>
	<script src="js/platformcore/administration/utils/bootstrap.min.js"></script>
	<script src="js/framework/reportcontainer/bootstrap-min/bootstrap-multiSelect.js"></script>
	<link rel="stylesheet" href="js/framework/editcheckv2/css/bootstrap.min.css">
	<link rel="stylesheet" href="style/advancedreportcontainer/bootstrap-multiSelect.css">
	<link rel="stylesheet" href="style/advancedreportcontainer/custom-bootstrap.css">
	
	
		<%
		String userDateFormatStr= com.fintellix.platformcore.utils.ApplicationProperties.getValue("app.returnValidation.dateFormat");
		String displayDateFormat= com.fintellix.platformcore.utils.ApplicationProperties.getValue("app.returnValidation.displayDateFormat");
		String momentDateFormat= com.fintellix.platformcore.utils.ApplicationProperties.getValue("app.returnValidation.momentDateFormat");
		String validationJsonDate= com.fintellix.platformcore.utils.ApplicationProperties.getValue("app.returnValidation.validationJsonDate");
		String editValidationJsonDate= com.fintellix.platformcore.utils.ApplicationProperties.getValue("app.returnValidation.editValidationJsonDate");
	%>
	<script>
		var userDateFormat= "<%=userDateFormatStr%>";
		var displayDateFormat= "<%=displayDateFormat%>";
		var momentDateFormat= "<%=momentDateFormat%>";
		var validationJsonDate= "<%=validationJsonDate%>";
		var editValidationJsonDate= "<%=editValidationJsonDate%>";
	</script>
	<!--<script src="js/framework/validations/returns/returnValidation.js"></script>-->
	<script src="js/framework/validations/returns/returnValidationCreateAndEdit.js"></script>
	<script src="js/framework/validations/returns/returnValidationLandingPage.js"></script>
	
	<script src="js/framework/validations/returns/returnValidationExpressionPage.js"></script>
	<script src="js/framework/validations/returns/expressionGrammar.js"></script>
		<script src="js/framework/validations/returns/antlrJavascript/require.js"></script>
	<script src="js/framework/validations/returns/antlrJavascript/antlrExpression.js"></script>
	<script src="js/framework/validations/returns/common.js"></script>
	<script src="js/framework/validations/returns/vs-script.js"></script>
		<script>var previlegeValue = '<c:out value="${previlegeType}" escapeXml="false"/>';</script>
		
	<!--<script>var editValidationArray = '<c:out value="${editResponse}" escapeXml="false"/>';</script>
	<script>var isEdit = '<c:out value="${isedit}" escapeXml="false"/>';</script>
	<script>var defaultArrayVal = '<c:out value="${newResponse}" escapeXml="false"/>';</script>
	<script>var previlegeValue = '<c:out value="${previlegeType}" escapeXml="false"/>';</script>-->
	
	<style>
	.vDropDownIE{
		display : block !important;
	}
	</style>
	
</head>

<body>
	<div  class="overlay">
		<div class="loader"></div>
	</div>
	<div id="validationListContainer">
		<div class="vs-gc-header-comp" id="headerCompLandingPage">
			<div><h3 class="vs-h3-light-primary">Return Validation List</h3></div>
			<div>
				<button class="vs-button-small vs-primary-one-outline" id="import-btn" onclick="showImportModal()" type="button">IMPORT</button>
				<!--data-toggle="modal" data-target="#upload-modal"-->
				<div class="vs-button-popover">
					<button class="vs-button-small vs-primary-one-outline" type="button">EXPORT<i class="icon-arrow-down-solid"></i></button>
					<div>
						<ul>
						  <li onclick="exportTemplate();" class="pointer-enable">EXPORT TEMPLATE</li>
						  <li onclick="exportDefinitionPopup();" class="pointer-enable">EXPORT DEFINITION</li>
						<ul>
					</div>
				</div>
				<button class="vs-button-small vs-primary-one" type="button" id="new-validation-btn" onclick="redirectToValidationCreationPage();">New</button>
			</div>
		</div>
		<div class="vs-gc-flex-grid vs-top-mgn-16" id="flexGridDropDown">
			<div class="vs-flex-item-15" ><p class="vs-body-regular-primary">
				<div class="vs-gc-lbl-comp">
					<label class="vs-body-regular-primary vs-right-pad-8" for="textid">Return</label>
					<span class="vs-dropdown big">
						<select id="return-select-id" class="applyButtonEnableDisable">
							<option value="" selected>All</option>
						</select>
					</span>
				</div>
			</p></div>
			<div class="vs-flex-item-15" ><p class="vs-body-regular-primary">
				<div class="vs-gc-lbl-comp">
					<label class="vs-body-regular-primary vs-right-pad-8" for="textid">Status</label>
					<span class="vs-dropdown big">
						<select id="status-select-id" class="applyButtonEnableDisable">
							<option value="" selected>All</option>
						</select>
					</span>
				</div>
			</p></div>
			<div class="vs-flex-item-15" ><p class="vs-body-regular-primary">
				<div class="vs-gc-lbl-comp">
					<label class="vs-body-regular-primary vs-right-pad-8" for="textid">Type</label>
					<span class="vs-dropdown big">
						<select id="type-select-id" class="applyButtonEnableDisable">
							<option value="" selected>All</option>
						</select>
					</span>
				</div>
			</p></div>
			<div class="vs-flex-item-15"><p class="vs-body-regular-primary">
				<div class="vs-gc-lbl-comp">
					<label class="vs-body-regular-primary vs-right-pad-8" for="textid">Group</label>
					<span class="vs-dropdown big">
						<select id="group-select-id" class="applyButtonEnableDisable">
							<option value="" selected>All</option>
						</select>
					</span>
				</div>
			</p></div>
			<div class="vs-flex-item-20"><p class="vs-body-regular-primary">
				<div class="vs-gc-lbl-comp">
					<label class="vs-body-regular-primary vs-right-pad-8" for="textid">Effective date</label>
					<!--<input type="date" name="date-picker" id="effective-date-id" class="applyButtonEnableDisable applyButtonEnableDisable" style="width:11em;">-->
					<input placeholder="DD-MM-YYYY" class="vs-datePicker applyButtonEnableDisable datePickerClass" type="text" id="effective-date-id"
                    style="width:128px;background-image: url(./images/calender.png);background-repeat:no-repeat ;background-position:right; background-size: 18px 16px;" autocomplete="off"></input>

					<!--<div class = "vs-left-mgn-8">
						<input placeholder="Select" type="text" class="vs-datePicker applyButtonEnableDisable" id="effective-date-id" autocomplete='off'  onkeydown="return false" style="background: url(./images/calender.png) no-repeat right; background-size: 18px 16px;">
					</div>-->
				</div>
			</p></div>
			<div class="vs-flex-item-5" ><p class="vs-body-regular-primary">
				<div><button class="vs-button vs-primary-one vs-bottom-mgn-8" type="button" id="applyButtonId" onclick="renderReturnValidationGrid();" >Apply</button></div>
			</p></div>
		</div>
		<div id="validationListKendo" class="vs-top-mgn-16"></div>
		<div id="hiddenDownloaderID">
			<iframe id="hiddenDownloader" style="display: none;"></iframe>
		</div>
		<div class="vs-modal" id="importModal">
		  <main class="lay-right-aside">
			<aside>
				<div class="vs-section-default">
					<p class="vs-caption-uppercase-text">IMPORT RETURN</p>
					<span class="icon-large icon-close" onclick="vs_model_hide('importModal')"></span>
				</div>
				<div id="help-div-parent" class="vs-alert error">
				  <div>
					<small id="help-div" class="vs-baseline-regular-black"></small>
				  </div>
				</div>
				<div class="vs-section-side">
					<form name="uploadForm" id="uploadForm" action="javascript:;" enctype="multipart/form-data" method="post" accept-charset="utf-8">
						<input type="file" name="file" id="file"/>
						<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
					</form>
					<!--<div id="help-div" style="display:none;">Please select a file to upload</div>-->
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-32">
				  <div class="vs-flex-item-50 vs-left-pad-16"><p class="vs-body-regular-primary"></p></div>
				  <div class="vs-flex-item-50 vs-left-pad-8"><p class="vs-body-regular-primary vs-top-mgn-16">
					<button class="vs-button vs-primary-one-outline" type="button" onclick="vs_model_hide('importModal')">CLOSE</button>
					<button class="vs-button vs-primary-one vs-left-mgn-8" onclick="importFromExcel();" type="button" onclick="exportToExcel()" id="export-btn">IMPORT</button>
				  </p></div>
				</div>
			</aside>
		  </main>
		</div>
		<!--<div class="modal fade" id="upload-modal" role="dialog">
    <div class="modal-dialog">
    
      <!-- Modal content
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">&times;</button>
          <h4 class="modal-title">Import Excel</h4>
        </div>
        <div class="modal-body">
         	 <form name="uploadForm" id="uploadForm" action="javascript:;" enctype="multipart/form-data" method="post" accept-charset="utf-8">
   				<input type="file" name="file" id="file"/>
		 	 </form>
			<div id="help-div" style="display:none;">Please select a file to upload</div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
          <button type="button" class="btn btn-default" onclick="importFromExcel();">Upload</button>
        </div>
      </div>
      
    </div>
  </div>-->
	<div class="vs-modal" id="ExportDefPopup">
		<main class="lay-right-aside">
			<aside>
				<div class="vs-section-default">
					<p class="vs-caption-uppercase-text">EXPORT DEFINITION</p>
					<span class="icon-large icon-close" onclick="vs_model_hide('ExportDefPopup')"></span>
				</div>
				<div id="export-def-error-msg-parent" style="display:none" class="vs-alert warning">
				  <div>
					<small id="export-def-error-msg" class="vs-baseline-regular-black"></small>
				  </div>
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-16">
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<label class="vs-body-regular-primary" for="textid">Return</label>
				  </p></div>
				  <div class="vs-flex-item-55"><p class="vs-body-regular-primary vs-top-mgn-8">
						<select id="return-exportDef-dropdown" class="vs-multiSelect exportDef" multiple="multiple">
						</select>
				  </p></div>
				  <div class="vs-flex-item-5 vs-left-pad-8"><p class="vs-body-regular-primary"></p></div>
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-16">
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<label class="vs-body-regular-primary" for="textid">Type</label>
				  </p></div>
				  <div class="vs-flex-item-55"><p class="vs-body-regular-primary vs-top-mgn-8">
					<select id="type-exportDef-dropdown" class="vs-multiSelect exportDef" multiple="multiple">
					</select>
				  </p></div>
				  <div class="vs-flex-item-5 vs-left-pad-8"><p class="vs-body-regular-primary"></p></div>
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-16">
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<label class="vs-body-regular-primary" for="textid">Status</label>
				  </p></div>
				  <div class="vs-flex-item-55"><p class="vs-body-regular-primary vs-top-mgn-8">
						<select id="status-exportDef-dropdown" class="vs-multiSelect exportDef" multiple="multiple">
						</select>
				  </p></div>
				  <div class="vs-flex-item-5 vs-left-pad-8"><p class="vs-body-regular-primary"></p></div>
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-16">
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<label class="vs-body-regular-primary" for="textid">Group</label>
				  </p></div>
				  <div class="vs-flex-item-55"><p class="vs-body-regular-primary vs-top-mgn-8">
						<select id="group-exportDef-dropdown" class="vs-multiSelect exportDef" multiple="multiple">
						</select>
				  </p></div>
				  <div class="vs-flex-item-5 vs-left-pad-8"><p class="vs-body-regular-primary"></p></div>
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-16">
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<label class="vs-body-regular-primary" for="textid">Effective Start Date<span class="redStarMandatory">*</span></label>
				  </p></div>
				  <div class="vs-flex-item-55"><p class="vs-body-regular-primary vs-top-mgn-8">
					<!--<input type="date" name="date-picker" id="effective-startDate-exportDef" style="width:18em">-->
					<input placeholder="DD-MM-YYYY" class="vs-datePicker datePickerClass" type="text" id="effective-startDate-exportDef" 
                    style="width:248px;background-image: url(./images/calender.png);background-repeat:no-repeat ;background-position:right; background-size: 18px 16px;" autocomplete="off"></input>
				  </p></div>
				  <div class="vs-flex-item-5 vs-left-pad-8"><p class="vs-body-regular-primary"></p></div>
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-16">
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<label class="vs-body-regular-primary" for="textid">Effective End Date</label>
				  </p></div>
				  <div class="vs-flex-item-55"><p class="vs-body-regular-primary vs-top-mgn-8">
					<!--<input type="date" name="date-picker" id="effective-endDate-exportDef" style="width:18em">-->
					<input placeholder="DD-MM-YYYY" class="vs-datePicker datePickerClass" type="text" id="effective-endDate-exportDef" 
                    style="width:248px;background-image: url(./images/calender.png);background-repeat:no-repeat ;background-position:right; background-size: 18px 16px;" autocomplete="off"></input>
				  </p></div>
				  <div class="vs-flex-item-5 vs-left-pad-8"><p class="vs-body-regular-primary"></p></div>
				</div>
				<div class="vs-gc-flex-grid vs-top-mgn-32">
				  <div class="vs-flex-item-50 vs-left-pad-16"><p class="vs-body-regular-primary"></p></div>
				  <div class="vs-flex-item-50 vs-left-pad-8"><p class="vs-body-regular-primary vs-top-mgn-16">
					<button class="vs-button vs-primary-one-outline" type="button" onclick="vs_model_hide('ExportDefPopup')">CANCEL</button>
					<button class="vs-button vs-primary-one vs-left-mgn-8" type="button" onclick="exportToExcel()" id="export-btn">EXPORT</button>
				  </p></div>
				</div>
			</aside>
		</main>
	</div>
</div>
<div id="validation-create-edit-con" style="display:none;">
	<div class="vs-gc-header-comp validationBottomBorder">
		<div><h3 class="vs-h3-light-primary" id="validationNameHeading">New Validation</h3></div>
	</div>
	<div id="versionTimelineDiv" style="display:block;width:100%;">
		<div><h3 class="vs-h4-light-primary vs-top-mgn-8 vs-bottom-mgn-8 vs-left-pad-16" id="validationNameHeading">Version Timeline</h3></div>
		<div style="display:flex;">
			<div style="width:95%;">
				<div id="versionTimeline">
						<div class="cotainerProgressBar">
							<ul id="progressBar">	
							</ul>
						</div>
				</div>
				<div id="tabScrollValidation" style=" float:right;">
						<div class="tabScrollValidationClass fa fa-angle-left" id="leftScroll">
						</div>
						<div class="tabScrollValidationClass fa fa-angle-right" id="rightScroll">
						</div>
				</div>
			</div>
			<div>
				<button class="vs-button-small vs-primary-one" type="button" id="editValidationbtn" style="display:none">EDIT</button>
			</div>
		</div>
	</div>
	<div class="vs-accordion-header vs-top-mgn-16" onclick="vsTriggerAccordionHeader(this)"  id="basicInformation">
		<button class="vs-accordion vs-left-mgn-8 vs-active">Basic Information</button>
	</div>
	<div id="basicInformationPanel" class="vs-accordion-panel vs-top-mgn-16">
		<div class="vs-gc-flex-grid">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
			<div class="vs-gc-flex-grid">
				  <div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
						<div class="vs-gc-lbl-comp">
							<div><label class="vs-body-regular-primary" for="textid">Validation Code<span class="mandatoryFied">*</span></label></div>
							<div><input maxlength="128" class="vs-textbox alertInlineValidation" type="text" placeholder="Enter validation code here" id="validationCodeTextId" autocomplete="off"></div>
						</div>
				  </p></div>
				  <div class="vs-flex-item-40 vs-left-pad-16">
				  <p class="vs-body-regular-primary">
					<div class="vs-gc-lbl-comp">
						<div>
							<label class="vs-body-regular-primary" for="textid">Effective Date<span class="mandatoryFied">*</span></label>
						</div>
						<div>
							<input placeholder="DD-MM-YYYY" class="vs-datePicker datePickerClass" type="text" id="date-picker-id" style="width:315px;background-image: url(./images/calender.png);background-repeat:no-repeat ;background-position:right; background-size: 18px 16px;" autocomplete="off"></input>
						</div>
					</div>
				  </p>
				 </div>
			</div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16" ><p class="vs-body-regular-primary"></p></div>
		</div>
		<div class="vs-gc-flex-grid">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
				<div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
					<div class="vs-gc-lbl-comp">
						<div><label class="vs-body-regular-primary" for="textid">Validation Name<span class="mandatoryFied">*</span></label></div>
						<div><input maxlength="256" class="vs-textbox alertInlineValidation" type="text" placeholder="Enter validation name here" id="validationNameTextId" autocomplete="off"></div>
					</div>
				 </p></div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16"><p class="vs-body-regular-primary"></p></div>
		</div>
		<div class="vs-gc-flex-grid validationBottomBorder">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
				<div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
					<div class="vs-gc-lbl-comp">
						<div><label class="vs-body-regular-primary" for="textid">Validation Description</label></div>
						<div><input maxlength="2048" class="vs-textbox validationDecExt" type="text" placeholder="Enter validation description here" id="validationDescriptionTextId" autocomplete="off"></div>
					</div>
				 </p></div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16"><p class="vs-body-regular-primary"></p></div>
		</div>
		<div class="vs-gc-flex-grid vs-top-mgn-16 class2">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
			<div class="vs-gc-flex-grid">
				  <div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
						<div class="vs-gc-lbl-comp">
							<div class="vs-right-mgn-32"><label class="vs-body-regular-primary" for="textid">Return Name<span class="mandatoryFied">*</span></label></div>
						<span class="vs-dropdown big vs-left-mgn-32">
							<select id="returnNameId" class="alertInlineValidationDropDown">
								<option value="" selected disabled>Select Option</option>
							</select>
						</span>
						</div>
				  </p></div>
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<div class="vs-gc-lbl-comp">
						<div><label class="vs-body-regular-primary" for="textid">Section<span class="mandatoryFied">*</span></label></div>
						<span class="vs-dropdown big">
							<select id="returnNameSection" class="alertInlineValidationDropDown">
								<option value="" selected disabled>Select Option</option>
							</select>
						</span>
					</div>
				  </p></div>
			</div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16" ><p class="vs-body-regular-primary"></p></div>
		</div>
		<div class="vs-gc-flex-grid">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
			<div class="vs-gc-flex-grid">
				  <div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
						<div class="vs-gc-lbl-comp">
							<div class="vs-right-mgn-32"><label class="vs-body-regular-primary" for="textid">Group<span class="mandatoryFied">*</span></label></div>
						<span class="vs-dropdown big vs-left-mgn-32">
							<select id="groupReturnId" class="alertInlineValidationDropDown">
								<option value="" selected disabled>Select Option</option>
							</select>
						</span>
						</div>
				  </p></div>
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<!--eff-->
				  </p></div>
			</div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16" ><p class="vs-body-regular-primary"></p></div>
		</div>
		<div class="vs-gc-flex-grid validationBottomBorder">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
			<div class="vs-gc-flex-grid">
				  <div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
						<div class="vs-gc-lbl-comp">
							<div><label class="vs-body-regular-primary" for="textid">Category<span class="mandatoryFied">*</span></label></div>
							<label class="vs-radiobutton">Regulatory
							  <input name="categoryBtn" class="radiobutton-text" type="radio" checked="checked" id="regulatoryId">
							  <span class="vs-radio-dot"></span>
							</label>
							<label class="vs-radiobutton vs-left-mgn-16">Custom
							  <input name="categoryBtn" class="radiobutton-text" type="radio" id="customId">
							  <span class="vs-radio-dot"></span>
							</label>
						</div>
				  </p></div>
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
				  </p></div>
				  </p></div>
			</div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16" ><p class="vs-body-regular-primary"></p></div>
		</div>
		<div class="vs-gc-flex-grid vs-top-mgn-16 validationBottomBorder">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
			<div class="vs-gc-flex-grid">
				  <div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
						<div class="vs-gc-lbl-comp" id="typeBtnId">
							<div><label class="vs-body-regular-primary" for="textid">Type<span class="mandatoryFied">*</span></label></div>
							<label class="vs-radiobutton">Mandatory
							  <input name="typeBtn" type="radio" class="radiobutton-text" checked="checked" id="mandatoryId" onclick="hideTypeOccurence()";>
							  <span class="vs-radio-dot"></span>
							</label>
							<label class="vs-radiobutton vs-left-mgn-16">Optional
							  <input name="typeBtn" type="radio" class="radiobutton-text" id="optionalId" onclick="showTypeOccurence();">
							  <span class="vs-radio-dot"></span>
							</label>
						</div>
				  </p></div>
				  <div class="vs-flex-item-40 vs-left-pad-16" id="typeOccurenceId" style="display:none;"><p class="vs-body-regular-primary"></p>
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					</p><div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					</p><div class="vs-gc-lbl-comp">
						<span class="vs-dropdown big">
							<select id="everyOccurenceDropDown">
								<!--<option value="" selected="">Every Occurence</option>-->
							</select>
						</span>
						<div>
							<label id="lableToEnableComment1" class="vs-body-regular-primary" for="textid">Enables warning comments for every occurence</label>
							<label id="lableToEnableComment2" class="vs-body-regular-primary" for="textid" style="display:none;">Enables warning comments at validation level</label>
						</div>
					</div>
				  <p></p></div>
				  <p></p></div>
				  </div>
			</div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16" ><p class="vs-body-regular-primary"></p></div>
		</div>
		<div class="vs-gc-flex-grid vs-top-mgn-16">
			<div class="vs-flex-item-80 vs-left-pad-16" ><p class="vs-body-regular-primary">
			<div class="vs-gc-flex-grid">
				  <div class="vs-flex-item-40 vs-left-pad-16" ><p class="vs-body-regular-primary">
						<div class="vs-gc-lbl-comp">
							<div class="vs-right-mgn-32"><label class="vs-body-regular-primary" for="textid">Status<span class="mandatoryFied">*</span></label></div>
						<span class="vs-dropdown big vs-left-mgn-32">
							<select id="statusId" class="alertInlineValidationDropDown">
								<!--<option value="active" selected>Active</option>-->
							</select>
						</span>
						</div>
				  </p></div>
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary"></p></div>
			</div>
			</p></div>
			<div class="vs-flex-item-20 vs-left-pad-16" ><p class="vs-body-regular-primary"></p></div>
		</div>
	</div>
	<div class="vs-accordion-header vs-left-mgn-8" onclick="vsTriggerAccordionHeader(this)" id="expressionEditor">
		<button  class="vs-accordion">Expression Editor</button>
	</div>
	<div id="expressionEditorPanel" class="vs-accordion-panel vs-top-mgn-16">
		<div class="vs-gc-flex-grid">
			<div class="vs-flex-item-50 vs-left-pad-16" ><p class="vs-body-regular-primary">
				<div id="validationDescExpressionEditor" class="expressionEditorDiv">
					<div class="vs-gc-header-comp">
						<div><h1 class="vs-h4-regular-black vs-top-mgn-8">Validation Expression</h1></div>
					</div>
					<!--<div id="validation-msg" style="display:none;"></div>-->
					<!--<div id="validation-msg" style="display:none;">
    					<span id="span-msg"></span>
    					<!--<span id="span-btn" type="button" class="fa fa-times" onclick="closeValidationMsgDailog()"></span>-->
						<!--<span class="vs-alert-closebtn" id="span-btn"><i class="icon-medium icon-close" onclick="closeValidationMsgDailog();"></i></span>
					</div>-->
					<div id="validation-msg" class="vs-alert error" style="display:none;height:auto;align-items: baseline;">
					  <div id="validation-msg-div"></div>
					  <div>
						<span class="vs-alert-closebtn"><i class="icon-medium icon-close" onclick="closeAlertValidationPopup()"></i></span>
					  </div>
					</div>
					<div id="validation-multi-msg" class="vs-alert error" style="display:none">
						<div>
							<strong></strong>
						</div>
						<div>
							<button class="vs-button-small vs-primary-one-outline" type="button" onclick="toggleAlertPanel('validation-multi-msg','validation-multi-msg-2','show')">SHOW</button>
						</div>
					</div>
					<div id="validation-multi-msg-2" class="vs-alert-panel" style="display:none">
						<div style="height:43px">
							<strong style="color:#C41C22">Errors:</strong>
							<button class="vs-button-small vs-primary-one-outline" type="button" onclick="toggleAlertPanel('validation-multi-msg-2','validation-multi-msg','hide')" style="float:right">HIDE</button>
						</div>
						<div>
							<ul class="vs-ul" id="validationErrorList"></ul>
						</div>
					</div>
					<!--<div id="validation-msg-2" class="vs-alert error vs-multi-alert-panel">
						<div id="validation-msg-div">
						</div>
					</div>-->
					<textarea id="validationDescriptionTextArea" class="vs-textarea vs-top-mgn-8" rows="8" cols="50" name="" form="" placeholder="Enter text here..." style="resize:none;margin-top: 0px ! important;min-height:201px" spellcheck="false"></textarea>
				</div>
			</p></div>
			<div class="vs-flex-item-50 vs-left-pad-16" id="validationExpressionSuggestion"><p class="vs-body-regular-primary">
				<div class="vs-tab-container">
				  <div class="vs-tab">
					<button id="button1" class="vs-tablinks active" onclick="vsOpenTabTrigger('factors-tab','button1')">FACTORS</button>
					<button id="button2" class="vs-tablinks" onclick="vsOpenTabTrigger('function-tab','button2')">FUNCTIONS</button>
					<button id="button3" class="vs-tablinks" onclick="vsOpenTabTrigger('operator-tab','button3')">OPERATORS</button>
				  </div>
				  <div id="factors-tab" class="vs-tabcontent expressionEditorDiv">
				  	<div style="display:flex;">
				  		<input type="text" id="return-Search" autocomplete="off" placeholder="Search and double click to select" onkeyup="filterValidationData(this,event);" onclick="getFactorDataOnClick(this)"/>
				  		<span class="fa fa-times clearInputClass" style="display:none;" onclick="clearInpBoxAndReloadData('returnData');"></span>
					<button class="vs-button-small vs-primary-one-outline" id="add-val-data-btn" type="button" onclick="addDataElementToTextArea(this,'rtn');">ADD</button>
				  	</div>
					<div id="expression-data">
						<div value="REFTBL" class="parent-div contentDiv" ondblclick="addValueIntoInputBoxOfReftbl(this,'level-1');">REFTBL
							<div class="vs-tooltip icon-info vs-top-mgn-8">
								<i class="icon-small icon-info-solid"></i>
								<span class="vs-tooltiptext-left">Reference Data Entities</span>
							</div>
						</div>
						<div value="RTN" class="parent-div contentDiv" ondblclick="addValueIntoInputBox(this,'level-1');">RTN
							<div class="vs-tooltip icon-info vs-top-mgn-8">
								<i class="icon-small icon-info-solid"></i>
								<span class="vs-tooltiptext-left">Returns</span>
							</div>
						</div>
					</div>
				  </div>
				  <div id="function-tab" class="vs-tabcontent expressionEditorDiv">
				  	<div style="display:flex;">
				  		<input type="text" id="function-Search" autocomplete="off" class="search-class" placeholder="Search functions" onkeyup="filterValidationFuncAndOper(this.id,'expression-func-id');" onfocus="filterValidationFuncAndOper(this.id,'expression-func-id');"/>
				  		<span class="fa fa-times clearInputClass" style="display:none;" onclick="clearInpBoxAndReloadData('','add-val-func-btn','expression-func-id');"></span>
				  	</div>
				  	<div class="expression-scroll" id="expression-func-id"></div>
				  	<div class="expression-format">
				  		<div class="example-div"></div>
				  		<button class="vs-button-small vs-primary-one-outline format-str-div" id="add-val-func-btn" type="button" onclick="addDataElementToTextArea(this,'func');">ADD</button>
				  	</div>
				  </div>
				  <div id="operator-tab" class="vs-tabcontent expressionEditorDiv">
				  <div style="display:flex;">
				  		<input type="text" id="operator-Search" autocomplete="off" class="search-class" placeholder="Search operators"  onkeyup="filterValidationFuncAndOper(this.id,'expression-oper-id');" onfocus="filterValidationFuncAndOper(this.id,'expression-oper-id');"/>
				  		<span class="fa fa-times clearInputClass" style="display:none;" onclick="clearInpBoxAndReloadData('','add-val-oper-btn','expression-oper-id');"></span>
				  	</div>
				  	
				  	<div class="expression-scroll" id="expression-oper-id"></div>
				  	<div class="expression-format">
				  		<div class="example-div"></div>
				  		<button class="vs-button-small vs-primary-one-outline format-str-div" id="add-val-oper-btn" type="button" onclick="addDataElementToTextArea(this,'oper');">ADD</button>
				  	</div>
				  </div>
				</div>
			</p></div>
		</div>
	</div>
		<div style="float:right;margin-bottom:4px" class="editGroupBtns">
			<button class="vs-button-small vs-primary-one-outline " id="cancelValidationbtn-btn" type="button" onclick="reloadBackToLandingPage();">CANCEL</button>
			<!--<button class="vs-button-small vs-primary-one " type="button" id="validateValidationbtn-btn" onclick="validateExpression()">VALIDATE</button>
			<button class="vs-button-small vs-primary-one" type="button" id="saveValidationbtn" disabled>SAVE</button>-->
			<button class="vs-button-small vs-primary-one" type="button" id="validateValidationbtn-btn" onclick="validateExpression()">SAVE</button>
		</div>
		<div class="nonEditGroupBtns" style="float:right;margin-bottom:4px;">
			<button class="vs-button-small vs-primary-one-outline" id="closeValidationbtn-btn" type="button" onclick="reloadBackToLandingPage();">CLOSE</button>
		</div>	
</div>
	<!--<div class="modal fade" id="onClickingEffectiveDateAlert" role="dialog">
    <div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title">By choosing other changes you will loose current changes.</h4>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				<button type="button" class="btn btn-default" onclick="clickToChnge();">Ok</button>
			</div>
			</div>
	    </div>
  </div>-->
    <div class="vs-notification-bar error" id="onClickingEffectiveDateAlert" style="top:0;display:none;">
      <div>
       <p class="vs-body-regular-black">By choosing other changes you will loose current changes.</p>
      </div>
      <div>
        <button class="vs-button-small vs-primary-one-outline" type="button" onclick="clickToChnge();">Proceed</button>
        <span class="vs-alert-closebtn" id="cancelTheProcess"><i class="icon-medium icon-close" onclick="clickToCancel();"></i></span>
      </div>
    </div>
	<div class="vs-notification-bar error" id="alertMessageOnTop" style="top:0;display:none;">
      <div>
       <p class="vs-body-regular-black" id="alertMessageDisplay"></p>
      </div>
      <div>
        <span class="vs-alert-closebtn"><i class="icon-medium icon-close" onclick="closeAlertMessage();"></i></span>
      </div>
    </div>
    <div class="vs-notification-bar error" id="onChangeOfEffectiveDate" style="top:0;display:none;">
      <div>
       <p class="vs-body-regular-black">There is no returns for selected date.</p>
      </div>
      <div>
        <span class="vs-alert-closebtn" id="cancelTheProcess"><i class="icon-medium icon-close" onclick="cancelPopup();"></i></span>
      </div>
    </div>
    <div class="vs-notification-bar error" id="onClickingEffectiveDateGreaterAlert" style="top:0;display:none;">
      <div>
       <p class="vs-body-regular-black">Future validations will get overridden.</p>
      </div>
      <div>
        <button class="vs-button-small vs-primary-one-outline" type="button" onclick="clickToChngeOveridden();">Proceed</button>
        <span class="vs-alert-closebtn" id="cancelTheProcess"><i class="icon-medium icon-close" onclick="clickToCancelOverridden();"></i></span>
      </div>
    </div>
	<div id="suggestiondivID"></div>
	<div id="detailDivId">
		<div id="detailDiv"></div>
		<div id="detailExample"></div>
		<div id="line-item-detail" style="display:none;"></div>
	</div>
</body>