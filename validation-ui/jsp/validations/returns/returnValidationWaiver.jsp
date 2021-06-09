<%@ include	file="/WEB-INF/jsp/platformcore/common/utils/commonIncludeSwf.jsp"%>

<head>
	<!--css below please-->
	<meta charset="utf-8" />
    <title>Validation Waiver</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta name="theme-color" content="#000000" />
    
    <!-- css -->   
	<link rel="stylesheet" href="style/kendo/kendo.common-material.min.css" />
	<link rel="stylesheet" href="style/kendo/kendo.material.min.css" />
	<link rel="stylesheet" href="style/kendo/kendo.material.mobile.min.css" />
	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/bundle.css" />
	<link rel="stylesheet" type="text/css" href="style/modules/validations/returns/vs.css" />
	<link rel="stylesheet" href="style/modules/validations/returns/jquery-ui.css" />
	<link rel="stylesheet" href="style/modules/validations/returns/returnValidationWaiver.css" />  
	
	<!--js below please-->
	<!--<script src="js/framework/validations/returns/jquery-1.11.0.min.js"></script>-->
	<script src="js/framework/validations/returns/jquery.min.js"></script>
	
	<script src="js/framework/validations/returns/jquery-ui.min.js"></script>
	<script src="js/lib/kendo/kendo.all.min.js"></script>
	<script src="js/framework/validations/returns/vs-script.js"></script>
	<script src="js/framework/validations/returns/vs-pagination.js"></script>
	<script src="js/framework/validations/returns/vs-selectable-tiles.js"></script>
	<script src="js/framework/validations/returns/vs-search-dropDown.js"></script>
	<script src="js/framework/validations/returns/validationProperties.js"></script>
	<script src="js/framework/validations/returns/returnValidationWaiver.js"></script>
	
	<!--moment for date changes-->
	<script type="text/javascript" src="js/lib/moment-with-locales.min.js"></script>
	
	<script type="text/javascript" src="js/lib/handlebars-v3.0.0.js"></script>
	
	<script type="text/javascript">
	try{
		if(typeof(csrfTokenX)!="undefined"){
			$.ajaxSetup({
				beforeSend: function(xhr, settings) {
					if(settings.type==='POST'){
						xhr.setRequestHeader('X-XToken', csrfTokenX);
					}
				}
			});
		} 
	}catch(e){

	}
</script>
	  <!--<script>
      $(document).ready(function(){         
          vs_createBreadcrumb('big',[{page:"Home",url:"hrefval"},{page:"Manage Validation Waivers",url:"hrefval"}]);
      })     
  </script>-->
  
  
  <!--handle bar templates-->
	<script>
		Handlebars.registerHelper('activeInactiveClass', function(isActive) {
			if(isActive==1){
				return "activate";
			}else{
				return "deactivate";
			}
		});
		Handlebars.registerHelper('activeInactiveText', function(isActive) {
			if(isActive==1){
				return "Deactivate Waiver ";
			}else{
				return "Activate Waiver";
			}
		});
		Handlebars.registerHelper('validationDateHelper', function(d) {
			return formatDate(d.time,"x","DD-MMM-YYYY");
		});
		Handlebars.registerHelper('getOrgDetails', function(orgId) {
			return getOrgDetails(orgId);
		});
		Handlebars.registerHelper('getReturnDetails', function(orgId) {
			return getReturnDetails(orgId);
		});
		Handlebars.registerHelper('updatedDetailsHelper', function(cr,up) {
			if(up == 0 || up == null){
				return formatDate(cr,"x","DD/MM/YYYY")
			}else{
				return formatDate(up,"x","DD/MM/YYYY")
			}
		});
		Handlebars.registerHelper('reportingDateHelper', function(wDetails) {
			let det = JSON.parse(wDetails);
			if(det.effectiveDate==null || det.effectiveDate==""){
				return formatDate(det.startDate,"x","DD/MM/YYYY") +" - "+ formatDate(det.endDate,"x","DD/MM/YYYY");
			}else{
				return formatDate(det.effectiveDate,"x","DD/MM/YYYY")
			}
		});
		
	</script>
	<script id="waiver-list-hb" type="text/html">
			
			<div waiver-id = "{{waiverId}}"class="vs-description-tile vs-border-strip-left vs-border-strip-warning vs-bottom-mgn-8 {{activeInactiveClass isActive}}Slab" onclick = "viewValidationWaiver('{{waiverId}}')">
                  <div>
                     <div><i class="icon-document"></i></div>
                  </div>
                  <div>
                     <div>
                        <div class="vs-tile-header-icon">
                           <h4 class="vs-h4-regular-primary vs-tile-author-date-icon vs-text-ellipsis vs-vw-50" title = "{{waiverTitle}}">{{waiverTitle}}</h4>
                        </div>
                        <div class="vs-tile-icons">
                           <div title  = "{{activeInactiveText isActive}}" onclick = "deactivateValidationWaiver(this,'{{waiverId}}',event)">
                              <i class="icon-cancel icon-medium {{activeInactiveClass isActive}}Icon"></i>
                           </div>
                           <div title  = "Delete Waiver" onclick = "isDelete('{{waiverId}}',event)">
                              <i class="icon-trash-solid icon-medium"></i>
                           </div>
                        </div>
                     </div>
                     <div>
                        <div class="vs-description-tile-icon">
                           <p class="vs-baseline-regular-tertiary" title = "{{getOrgDetails orgId}} | {{getReturnDetails regReportId}} | Reporting Date : {{reportingDateHelper waiverInfo}}">
							{{getOrgDetails orgId}} | {{getReturnDetails regReportId}} | {{reportingDateHelper waiverInfo}}
                           </p>
                        </div>
                        <div class="vs-description-tile-footer">
                           <div class="vs-baseline-regular-tertiary  " title="Updated : 03/03/2020"> Updated : {{updatedDetailsHelper createdTime lastModifiedTime}}</div>
                           <!-- <div class="vs-baseline-regular-tertiary  " title="Date:12/12/2119">
                              Date:12/12/2119</div> -->
                        </div>
                     </div>
                  </div>
               </div>
			   

	</script>
	
	<script id="validation-list-hb" type="text/html">
	{{#each this}}
		<div class="vs-card vs-card-selectable cursor-pointer val-card" onclick="selectCurrentValidation(this)" v-id = "{{validationId}}" section-id = "{{sectionId}}">
		  <div class="vs-gc-stretch-out">
			 <div class = "validation-name">
				{{validationCode}} - {{validationName}}
			 </div>
			 <div class = "info-margin">
				<span title = "Info" onclick  = "showValidationDetails({{validationId}},this,event)"><i class="icon-info-outline vs-right-mgn-8 validation-icons"></i></span>
				<span class="vs-card-icon validation-icons"><i class="icon-chat-delivered"></i></span>
			 </div>
		  </div>
	   </div>
	   {{/each}}
  </script>
  <script id="validation-expr-list-hb" type="text/html">
	{{#each this}}
		<fieldset class= "fieldset-style">
			<legend>
			{{ validationDateHelper startDate}} - {{validationDateHelper endDate}}
			</legend>
				<div><label class="vs-label tertiary" for="textid">Expression :</label></div>
				<div>
					<p class="vs-body-regular-primary">{{validationExpression}}</p>
				</div>
			
		</fieldset>
	   {{/each}}
  </script>
  
  <!--handle bar templates-->
  
  
 <body>
	<!--loader-->
	<div class="overlay" style="display: none;">
		<div class="loader"></div>
	</div>
	<!--alerts-->
	<div id="alertsPanel"></div>
   <div id="waiverView">
      <div class="vs-gc-stretch-out">
         <div class="vs-left-mgn-16">
            <div id="vs-breadcrumb-holder" class="vs-bottom-mgn-0 vs-top-pad-8 vs-caption-uppercase-text">Manage Validation Waivers</div>
         </div>
         <div class="vs-right-mgn-8">
            <button class="vs-button vs-primary-one " type="button" onclick="createNewQuery()">ADD NEW WAIVER</button>
         </div>
      </div>
      <hr class="vs-separator-ruler vs-top-mgn-8">
      <main class="lay-left-aside-thin">
         <aside class="vs-vh-90">
            <div class="vs-section-default">
               <p class="vs-caption-uppercase-text">FILTER WAIVERS</p>
            </div>
            <div class="vs-left-mgn-8 vs-right-mgn-8">
               <h5 class="vs-h5-regular-secondary vs-bottom-pad-8 vs-top-mgn-8">Return</h5>
               <span class="vs-dropdown">
                  <select id="serachReturnId" class="filterCriteriaClass">
                     <option value = "-1">Select Return</option>
                  </select>
               </span>
               <h5 class="vs-h5-regular-secondary vs-bottom-pad-8 vs-top-mgn-8">Organization</h5>
               <span class="vs-dropdown">
                  <select id="serachOrgId" class="filterCriteriaClass">
                     <option value = "-1">Select Organization</option>
                  </select>
               </span>
               <h5 class="vs-h5-regular-secondary vs-bottom-pad-8 vs-top-mgn-8" style = "display:none !important">Sections</h5>
               <span class="vs-dropdown" style = "display:none !important">
                  <select id="serachSectionId" class="filterCriteriaClass" disabled>
                     <option value = "-1">Select Return Sections</option>
                  </select>
               </span>
               <h5 class="vs-h5-regular-secondary vs-bottom-pad-8 vs-top-mgn-8">Validation</h5>
               <span class="vs-dropdown">
                  <select id="serachValidationId" class="filterCriteriaClass" disabled>
                     <option value = "-1">Select Validation</option>
                  </select>
               </span>
               <h5 class="vs-h5-regular-secondary vs-bottom-pad-8 vs-top-mgn-8">Status</h5>
               <span class="vs-dropdown">
                  <select id="serachStatusId" class="filterCriteriaClass">
                     <option value = "-1">Select Status</option>
                     <option value="1">Active</option>
                     <option value="0">Inactive</option>
                  </select>
               </span>
               <h5 class="vs-h5-regular-secondary vs-bottom-pad-8 vs-top-mgn-8">Reporting Date</h5>
               <div>
                  <date-picker name="dob" id="filterDatePicker" data-options='{"displayFormat": "DD/MM/YYYY", "iconAlignment":"left", "showErrorMessage": false, "dateStringAlignment": "left", "lowerLimit": "01/01/1900", "upperLimit": "31/12/9999", "validationMessages": [{"inValidFormat": "Invalid DOB"}, { "outsideRange": ""}] , "isDisabled": false, "showButtons": false, "dateButtonPrimary": "Ok", "showClearIcon": true, "manualEntry": false, "disabledList": ["08/07/2017", "09/07/2017", "01/11/2020", "20/11/2019"], "indicatorList": [{ "dates": ["01/10/2019","02/11/2019"], "color": "#333" }, { "dates": ["02/09/2019","01/08/2019"], "color": "#ff0000" }]}' onChange="enableFilterButton()" ></date-picker>
               </div>
               <div class="vs-gc-push-right vs-top-mgn-8">
                  <div><button class="vs-button vs-primary-one-outline vs-right-mgn-8 type="button" onClick="clearFilterCriteria()">CLEAR</button></div>
                  <div><button class="vs-button vs-primary-one" type="button" disabled id="applyFilterId" onClick="applyFilter()">APPLY</button></div>
               </div>
            </div>
         </aside>
         <article>
            <div  class="vs-gc-flex-grid vs-left-pad-16 vs-top-pad-8">
               <div class="vs-flex-item-50 vs-gc-push-left">
                  <p class="vs-body-regular-tertiary" id = "waiverResultTitle">All Waivers</p>
               </div>
               <div class="vs-flex-item-50 vs-gc-push-right vs-right-mgn-8">
                  <p class="vs-baseline-regular-secondary">Active Waivers</p>
                  <a href="#" class="vs-badge-small vs-color-success vs-left-mgn-8 vs-right-mgn-8" id="activeCountId" onclick = "fetchAllActiveWaivers()">0</a>
                  <p class="vs-baseline-regular-secondary vs-right-mgn-8">InActive Waivers</p>
                  <a href="#" class="vs-badge-small vs-color-warning" id="inActiveCountId" onclick = "fetchAllInActiveWaivers()">0</a>
                  <div class="vs-vl vs-right-mgn-8 vs-top-pad-8 vs-bottom-pad-8 vs-left-mgn-8"></div>
                  <div >
                     <div class="vs-textbox-small-icon">
                        <i class="icon-search icon-medium " id = "searchByTtitleIcon"></i>
                        <input class="vs-textbox-small" type="search" placeholder="Search Waiver..." id="searchWaiverTitleId" onsearch="searchWaiver(this,event)">
                        <div class="vs-vl vs-top-pad-8 vs-bottom-pad-8 vs-left-mgn-8"></div>
                     </div>
                  </div>
                  <div class="vs-button-popover-right vs-left-pad-8 sortIcon" order="desc">
                     <button class="vs-trans-button" type="button"><i class="icon-medium icon-data"></i></button>
                     <div>
                        <ul>
                        <li onClick="orderByQueryResult('desc')">Newest</li>
                        <li onClick="orderByQueryResult('asc')">Oldest</li>
                        <ul>
                     </div>
                  </div>
               </div>
            </div>
            <hr class="vs-separator-ruler vs-top-mgn-8 vs-bottom-mgn-8">
            <div id="alertMessageId"></div>
			<div id="validationListKendo" class="vs-top-mgn-16"></div>
            <div id = "waiver-list-container" class="vs-top-mgn-8 vs-left-mgn-16 vs-right-mgn-16">
               
            </div>
         </article>
   </div>
<div id="addNewWaiver" style="display:none">
   <div id="vs-breadcrumb-holderVNE" class="vs-bottom-mgn-0 vs-top-pad-8 vs-caption-uppercase-text"></div>
    <div class="vs-gc-buttons vs-right-mgn-8" id = "VNEButtons">
      <button class="vs-button vs-primary-one" type="button"  id="editWavierButton" onclick="editWaiver()">EDIT</button>     
	  <button class="vs-button vs-primary-one-outline" type="button"  id="deactivateWaiverButton" onclick="deactivateWaiverFromVnE()">DEACTIVATE</button>                     
	  <button class="vs-button vs-primary-one-outline" type="button" id = "deleteWaiverButton" onClick="deleteWaiverFromVnE(event)">DELETE</button>
   </div>         
   <hr class="vs-separator-ruler vs-top-mgn-8 vs-bottom-mgn-8">
   <div>
      <main class="lay-left-aside">
         <aside class="vs-min-vh-55">
            <div class="vs-section-default">
               <p class="vs-caption-uppercase-text">CHOOSE WAIVER TYPES</p>
            </div>
            <div class="vs-mgn-8">
               <div class="vs-gc-lbl-comp">
                  <div><label class="vs-body-regular-primary" for="textid">Waiver Title</label></div>
                  <div><input class="vs-textbox textbox-prop" type="text" placeholder="Waiver Title" id="titleId" maxlength="256"></div>
               </div>
               <div class="vs-gc-lbl-comp">
                  <div><label class="vs-body-regular-primary" for="textid">Organization</label></div>
                  <div>
                     <span class="vs-dropdown-small">
                        <select id="orgIdInDeopdown">
                           <option value="" disabled selected>Select Option</option>
                        </select>
                     </span>
                  </div>
               </div>
               <div class="vs-gc-lbl-comp">
                  <div><label class="vs-body-regular-primary" for="textid">Return</label></div>
                  <div>
                     <span class="vs-dropdown-small">
                        <select  id="returnIdInDeopdown">
                           <option value="" disabled selected>Select Return</option>
                        </select>
                     </span>
                  </div>
               </div>
               <div class="vs-gc-lbl-comp">
                  <div><label class="vs-body-regular-primary" for="textid">Waiver Applicable</label></div>
                  <div><input id="toggle-one" class="toggle toggle-left" name="toggle" value="Period" type="radio" onclick="changeVersion(this)" checked>
                     <label for="toggle-one" class="vs-slide-btn-small">Period</label>
                     <input id="toggle-two" class="toggle toggle-right" name="toggle" value="Version" type="radio" onclick="changeVersion(this)">
                     <label for="toggle-two" class="vs-slide-btn-small">Reporting Date</label>
                  </div>
               </div>
               <div id="Version">
                  <div class="vs-gc-lbl-comp">
                     <div><label class="vs-body-regular-primary" for="textid">Effective Date</label></div>
                     <div>
                        <date-picker name="dob" id="versionEffective" data-options='{"displayFormat": "DD/MM/YYYY", "iconAlignment":"left", "showErrorMessage": true, "dateStringAlignment": "left", "lowerLimit": "01/01/1900", "upperLimit": "31/12/9999", "validationMessages": [{"inValidFormat": "Invalid DOB"}, { "outsideRange": ""}] , "isDisabled": false, "showButtons": false, "dateButtonPrimary": "Ok", "showClearIcon": false, "manualEntry": true, "disabledList": ["08/07/2017", "09/07/2017", "01/11/2020", "20/11/2019"], "indicatorList": [{ "dates": ["01/10/2019","02/11/2019"], "color": "#333" }, { "dates": ["02/09/2019","01/08/2019"], "color": "#ff0000" }]}' onChange="fetchVersionList()" onInput="onInputHandler()"></date-picker>
                     </div>
                  </div>
                  <div class="vs-gc-lbl-comp">
                     <div><label class="vs-body-regular-primary" for="textid">Version</label></div>
                     <div>
                        <span class="vs-dropdown-small">
                           <select id="versiondropDownId" disabled>
                              <option value="" disabled selected>Select Option</option>
                           </select>
                        </span>
                     </div>
                  </div>
               </div>
               <div id="period">
                  <div class="vs-gc-lbl-comp">
                     <div><label class="vs-body-regular-primary" for="textid">Start Date</label></div>
                     <div>
                        <date-picker name="dob" id="rangeFromDate" data-options='{"displayFormat": "DD/MM/YYYY", "iconAlignment":"left", "showErrorMessage": true, "dateStringAlignment": "left", "lowerLimit": "01/01/1900", "upperLimit": "31/12/9999", "validationMessages": [{"inValidFormat": "Invalid DOB"}, { "outsideRange": ""}] , "isDisabled": false, "showButtons": false, "dateButtonPrimary": "Ok", "showClearIcon": false, "manualEntry": true, "disabledList": ["08/07/2017", "09/07/2017", "01/11/2020", "20/11/2019"], "indicatorList": [{ "dates": ["01/10/2019","02/11/2019"], "color": "#333" }, { "dates": ["02/09/2019","01/08/2019"], "color": "#ff0000" }]}' onChange="displayValue('datepicker1', 'display1')" onInput="onInputHandler()"></date-picker>
                     </div>
                  </div>
                  <div class="vs-gc-lbl-comp">
                     <div><label class="vs-body-regular-primary" for="textid">End Date</label></div>
                     <div>
                        <date-picker name="dob" id="rangeToDate" data-options='{"displayFormat": "DD/MM/YYYY", "iconAlignment":"left", "showErrorMessage": true, "dateStringAlignment": "left", "lowerLimit": "01/01/1900", "upperLimit": "31/12/9999", "validationMessages": [{"inValidFormat": "Invalid DOB"}, { "outsideRange": ""}] , "isDisabled": false, "showButtons": false, "dateButtonPrimary": "Ok", "showClearIcon": false, "manualEntry": true, "disabledList": ["08/07/2017", "09/07/2017", "01/11/2020", "20/11/2019"], "indicatorList": [{ "dates": ["01/10/2019","02/11/2019"], "color": "#333" }, { "dates": ["02/09/2019","01/08/2019"], "color": "#ff0000" }]}' onChange="displayValue('datepicker1', 'display1')" onInput="onInputHandler()"></date-picker>
                     </div>
                  </div>
               </div>
            </div>
         </aside>
         <article>
		 <div id="alertMessageIdVNE"></div>
            <div class="vs-section-side vs-left-mgn-8 vs-right-mgn-8">
               <p class="vs-caption-uppercase-text">VALIDATION TO WAIVE</p>
               <div class="vs-gc-lbl-comp vs-hoz-len-30 sectionContainerCreate">
                  <div><label class="vs-body-regular-primary" for="textid">Sections</label></div>
                  <div>
                     <span class="vs-dropdown-small vs-hoz-len-20">
                        <select id = "section-dropdown-validations">
                           <option value = "-1" selected>All</option>
                        </select>
                     </span>
                  </div>
               </div>
            </div>
            <div id = "validation-list-container" class="vs-card-multiple vs-left-mgn-16 vs-right-mgn-16">
               <p class="vs-baseline-regular-tertiary">CHOOSE RETURN TO SEE LIST OF VALIDATIONS</p>
            </div>
         </article>
      </main>
   </div>
   <div class="vs-gc-buttons vs-right-mgn-8 vs-top-mgn-24">
      <button class="vs-button vs-primary-one-outline" type="button" id = "cancelWaiverButton" onClick="cancelAddingNewWaiver()">CANCEL</button>
      <button class="vs-button vs-primary-one" type="button"  id="addNewWaiverButton" onclick="addNewWaiver()">ADD</button>                     
      <button class="vs-button vs-primary-one" type="button"  id="updateWavierButton" onclick="updateWaiver()">UPDATE</button>                     
   </div>
   <div class="vs-modal" id="waiverInfoModal">
      <main class="lay-right-aside">
         <aside>
            <div class="vs-section-side">
               <p class="vs-caption-uppercase-text" id = "validationCodeModal">V10039</p>
               <div class="icon-close icon-medium"  onclick="vs_model_hide('waiverInfoModal')"></div>
            </div>
            <div class="vs-left-mgn-8 vs-right-mgn-8  vs-top-mgn-16">
               <div class="vs-gc-lbl-comp ">
                  <div><label class="vs-label tertiary" for="textid">Description :</label></div>
                  <div>
                     <p class="vs-body-regular-primary" id = "valDescModal">Is expected to equal 0</p>
                  </div>
               </div>
               <div class="vs-gc-lbl-comp">
                  <div><label class="vs-label tertiary" for="textid">Section : </label></div>
                  <div>
                     <p class="vs-body-regular-primary" id = "valSectionModal">100</p>
                  </div>
               </div>
               <div class="vs-gc-lbl-comp">
                  <div><label class="vs-label tertiary" for="textid">Type : </label></div>
                  <div>
                     <p class="vs-body-regular-primary" id = "valTypeModal" >Mandatory</p>
                  </div>
               </div>
              
               <div class="vs-gc-lbl-comp" id = "expressionDetailsByTime">
                  
               </div>
               <div class="vs-gc-push-right vs-top-mgn-16">
                  <button class="vs-button vs-primary-one" type="button"  id="nextToView" onclick="vs_model_hide('waiverInfoModal')">Close</button>               
               </div>
            </div>
         </aside>
         <article>
         </article>
      </main>
   </div>
</div>
</main>
<script src="js/framework/validations/returns/bundle.js"></script>
</body>