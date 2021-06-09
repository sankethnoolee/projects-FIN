<%@ include	file="/WEB-INF/jsp/platformcore/common/utils/commonIncludeSwf.jsp"%>
<%@ include file="/WEB-INF/jsp/platformcore/platformconfig/csrfheader.jsp" %>

<head>
	<link rel="stylesheet" href="js/framework/editcheckv2/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="style/font-awesome-4.6.3/css/font-awesome.min.css" />
	<link rel="stylesheet" type="text/css" href="js/lib/bootstrap-datepicker/css/datepicker.css" />
	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/returnValidationCreateAndEdit.css" />
	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/vs.css" />
	
	<script src="js/lib/jquery-3.2.1.min.js"></script>
	<script src="js/lib/jqueryFileUploader/jquery.ui.widget.js"></script>
	<script src="js/lib/jqueryFileUploader/jquery.iframe-transport.js"></script>
	<script src="js/lib/jqueryFileUploader/jquery.fileupload.js"></script>
	<script type="text/javascript" src="js/lib/moment-with-locales.min.js"></script>
	<script type="text/javascript" src="js/platformcore/administration/utils/bootstrap-datepicker.js"></script>
	<script src="js/platformcore/administration/utils/bootstrap.min.js"></script>
	<script src="js/lib/jsonpath-0.8.0.js"></script>
	<script src="js/framework/reportcontainer/bootstrap-min/bootstrap-multiSelect.js"></script>
	<link rel="stylesheet" href="style/advancedreportcontainer/custom-bootstrap.css">
	
	<link rel="stylesheet" href="style/advancedreportcontainer/bootstrap-multiSelect.css">
	
	<script src="js/framework/validations/returns/common.js"></script>	
	<script src="js/framework/validations/returns/returnValidationCreateAndEdit.js"></script>
	<script src="js/framework/validations/returns/vs-script.js"></script>
	
	<script>var editValidationArray = '<c:out value="${editResponse}" escapeXml="false"/>';</script>
	<script>var isEdit = '<c:out value="${isedit}" escapeXml="false"/>';</script>
	<script>var defaultArrayVal = '<c:out value="${newResponse}" escapeXml="false"/>';</script>
	<script>var previlegeValue = '<c:out value="${previlegeType}" escapeXml="false"/>';</script>
</head>
<body>
<div  class="overlay">
	<div class="loader"></div>
</div>
<div>
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
	<div class="vs-accordion-header vs-top-mgn-16">
		<button id="basicInformation" class="vs-accordion vs-left-mgn-8 class1">Basic Information</button>
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
				  <div class="vs-flex-item-40 vs-left-pad-16"><p class="vs-body-regular-primary">
					<div class="vs-gc-lbl-comp">
						<div>
							<label class="vs-body-regular-primary" for="textid">Effective Date<span class="mandatoryFied">*</span></label></div>
							<!--<input type="date" name="date-picker" id="date-picker-id" class="vs-hoz-len-80 vs-right-mgn-8" style="width:11em;">-->
							<input placeholder="DD-MM-YYYY" class="vs-datePicker datePickerClass" type="text" id="date-picker-id" style="width:315px;background-image: url(./images/calender.png);background-repeat:no-repeat ;background-position:right; background-size: 18px 16px;"></input>
						<!--<div><input class="vs-textbox" type="text" placeholder="Enter Effective Date here" id="effectiveDateTextId"></div>-->
						<!--<input type="date" name="date-picker" id="date-picker-id">-->
					</div>
				  </p></div>
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
							<p class="vs-radiobutton">Regulatory
							  <input name="categoryBtn" class="radiobutton-text" type="radio" checked="checked" id="regulatoryId">
							  <span class="vs-radio-dot"></span>
							</p>
							<p class="vs-radiobutton vs-left-mgn-16">Custom
							  <input name="categoryBtn" class="radiobutton-text" type="radio" id="customId">
							  <span class="vs-radio-dot"></span>
							</p>
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
							<p class="vs-radiobutton">Mandatory
							  <input name="typeBtn" type="radio" class="radiobutton-text" checked="checked" id="mandatoryId" onclick="hideTypeOccurence()";>
							  <span class="vs-radio-dot"></span>
							</p>
							<p class="vs-radiobutton vs-left-mgn-16">Optional
							  <input name="typeBtn" type="radio" class="radiobutton-text" id="optionalId" onclick="showTypeOccurence();">
							  <span class="vs-radio-dot"></span>
							</p>
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
	<div class="vs-accordion-header vs-left-mgn-8">
		<button id="expressionEditor" class="vs-accordion">Expression Editor</button>
	</div>
	<div id="expressionEditorPanel" class="vs-accordion-panel vs-top-mgn-16">
		<div class="vs-gc-flex-grid">
			<div class="vs-flex-item-50 vs-left-pad-16" ><p class="vs-body-regular-primary">
				<div id="validationDescExpressionEditor" class="expressionEditorDiv">
					<div class="vs-gc-header-comp">
						<div><h1 class="vs-h4-regular-black vs-top-mgn-8 vs-left-pad-16">Validation Expression</h1></div>
					</div>
					<!--<div id="validation-msg" style="display:none;"></div>-->
					<!--<div id="validation-msg" style="display:none;">
    					<span id="span-msg"></span>
    					<!--<span id="span-btn" type="button" class="fa fa-times" onclick="closeValidationMsgDailog()"></span>-->
						<!--<span class="vs-alert-closebtn" id="span-btn"><i class="icon-medium icon-close" onclick="closeValidationMsgDailog();"></i></span>
					</div>-->
					<div id="validation-msg" class="vs-alert error" style="display:none;height:auto;align-items: baseline;">
					  <div id="validation-msg-div">
					  </div>
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
					<textarea id="validationDescriptionTextArea" class="vs-textarea vs-left-mgn-16 vs-top-mgn-8" rows="8" cols="50" name="" form="" placeholder="Enter text here..." style="resize:none;margin-top: 0px ! important;min-height:201px"></textarea>
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
</body>
