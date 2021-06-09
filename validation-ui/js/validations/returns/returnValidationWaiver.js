var vw;
var validationReturnWaiverResult;
var organzationList;
var sectionList;

$(document).ready(function() {
	//onload methods like get the list of waviers on load  goes here.
	$('#section-dropdown-validations').on('change', function(){
		if($('#section-dropdown-validations').val()!=="-1"){
			$(".val-card").hide();
			$(".val-card[section-id='"+$('#section-dropdown-validations').val()+"']").show()
		
		}else{
			$(".val-card").show();
		}
	     
	});
	enablingAndDisablingButtons();
	//getReturnsForWaiver();
	//getOrganizationForWaiver();
	onChangeOfReturn();
	var defaultDate = formatDate(new Date().getTime(),"x","DD/MM/YYYY")
	var element = document.getElementById("versionEffective");
	element.setDataOptions({"displayFormat":"DD/MM/YYYY","defaultDate":defaultDate});
	var element = document.getElementById("rangeFromDate");
	element.setDataOptions({"displayFormat":"DD/MM/YYYY","defaultDate":defaultDate});
	var element = document.getElementById("rangeToDate");
	element.setDataOptions({"displayFormat":"DD/MM/YYYY","defaultDate":defaultDate});

	$('#orgIdInDeopdown').on('change', function(){
		if($('#orgIdInDeopdown').val()!=="Select Option"){
			fetchVersionList();
		}
	});
	$("#filterDatePicker").find("input").val("")
	
	//init
	vw = function(){
		
		/*
			Note : fetchData parameter query,url ----> then---->success handler, errorHandler
		
		*/
		
		
		//const
		const PERIOD_ID_FORMAT=app.PERIOD_ID_FORMAT;
		const DD_MMM_YYYY=app.DD_MMM_YYYY;
		const LONG_DATE_FORMAT=app.LONG_DATE_FORMAT;
		const WAIVER_DISPLAY_FORMAT=app.WAIVER_DISPLAY_FORMAT;
		const PAGE_SIZE_FOR_WAVIER_LIST=app.PAGE_SIZE_FOR_WAVIER_LIST;
		
		//urls
		const SAVE_VALIDATION_WAIVER = "./addvalidationwaiver.htm";
		const GET_VALIDATION_WAIVER_LIST = "./searchvalidationwaiver.htm";
		const DELETE_VALIDATION_WAIVER = "./deletevalidationwaiver.htm";
		const DEACTIVATE_VALIDATION_WAIVER = "./deactivatevalidationwaiver.htm";
		const SEARCH_VALIDATION_WAIVER = "./searchvalidationwaiver.htm";
		const ORGLISTURL = './getOrganisationlistforvalidationwaiver.htm';
		const GET_RETURN_LIST = './getreturnsforValidationWaiver.htm';
		const ACTIVATE_VALIDATION_WAIVER='./activatevalidationwaiver.htm';
		const GET_ACTIVE_AND_INACTIVE_COUNT_OF_WAIVER='./getActiveAndDeactiveCount.htm';
		const FETCH_VALIDATIONS_FOR_RETURN = "./fetchvalidationsforreturn.htm";
		const FETCH_VERSIONS_FOR_RETURN = "./getversiondetailsforvalidationwaiver.htm";
		const UPDATE_VALIDATION_WAIVER = "./updatevalidationwaiver.htm";
		
		//template init
		var waiverListHb = Handlebars.compile($('#waiver-list-hb')[0].innerHTML);
		var validationListHb = Handlebars.compile($('#validation-list-hb')[0].innerHTML);
		var validationExprListHb = Handlebars.compile($('#validation-expr-list-hb')[0].innerHTML);
		
		
		//onload variables
		let ORG_DETAILS;
		let RETURN_DETAILS;
		
		//global variables
		let orgIdandDetailsMap = {};
		let reportIdandDetailsMap = {};
		let validationIdandDetailsMap = {};
		let waiverIdDetailsMap = {};
		let VIEW_MODE = "N";
		let CURRENT_WAIVER_ID = "";
		let IS_VIEW = false;
		//onload block
		
		{	
			let query = {};
			//fetching oganisation details on load 
			fetchDataGet(query,ORGLISTURL).then(function(data){
				ORG_DETAILS=JSON.parse(data.model.applicableOrgs);
			},errorHandler).then(function(){
				populateOrgDropDownValues();
				//fetching return details on load 
				query = {
						isExpr:"Y"
				}
				fetchDataGet(query,GET_RETURN_LIST).then(function(data){
					RETURN_DETAILS=data.response;
				},errorHandler).then(function (){
					populateReturnDropDownValues()
					getAllValidationWaiver();
				});
				
			});
		}
		
		function populateOrgDropDownValues(){
			//landing page drop down
			var optStr="";
			for(var i= 0;i<ORG_DETAILS.length;i++){
				orgIdandDetailsMap[ORG_DETAILS[i].orgId+""] = ORG_DETAILS[i]
				optStr = optStr + "<option value = '"+ORG_DETAILS[i].orgId+"' orgId='"+ORG_DETAILS[i].orgId+"'>"+ORG_DETAILS[i].orgCode+" - "+ORG_DETAILS[i].orgName+"</option>"
			}
			$("#serachOrgId").append(optStr);
			
			
			//create/ edit page drop down here.
			$("#orgIdInDeopdown").append(optStr);
			
		}
		function populateReturnDropDownValues(){
			//landing page drop down 
			let optStr= "";
			for(var i= 0;i<RETURN_DETAILS.length;i++){
				reportIdandDetailsMap[RETURN_DETAILS[i].regReportId+""] = RETURN_DETAILS[i];
				optStr = optStr + "<option value = '"+RETURN_DETAILS[i].regReportId+"'returnValue = '"+RETURN_DETAILS[i].returns+"' >"+RETURN_DETAILS[i].returnName+"</option>"
			}
			$("#serachReturnId").append(optStr);
			
			//create/ edit page drop down here.
			$("#returnIdInDeopdown").append(optStr);
		}
		
		function validateSaveAction(){
			if($('#titleId').val()==""){
				warningAlert("Title cannot be empty.")
				return false;
			}
			if($('#returnIdInDeopdown').val()==""){
				warningAlert("Return cannot be empty.")
				return false;
			}
			if($('#orgIdInDeopdown').val()==""){
				warningAlert("Organization cannot be empty.")
				return false;
			}
			if ($(".tile-active").length<1){
				warningAlert("Minimum one validation should be selected.")
				return false;
			}
			
			if($("#period").is(":visible")){
				if(Number(formatDate($("#rangeFromDate").find("input").val(),WAIVER_DISPLAY_FORMAT,PERIOD_ID_FORMAT))>Number(formatDate($("#rangeToDate").find("input").val(),WAIVER_DISPLAY_FORMAT,PERIOD_ID_FORMAT))){
					warningAlert("Invalid Range")
				return false;
				}
				
			}
			if($("#period").is(":visible")){
				if($("#rangeToDate").find("input").val()=="" || $("#rangeFromDate").find("input").val()==""){
					warningAlert("Start Date and End Date cannot be empty");
					return false;
				}
			}else{
				if($("#versionEffective").find("input").val()==""){
					warningAlert("Effective Date cannot be empty");
					return false;
				}	
			}
			return true;
		}
		
		function getAllValidationWaiver(a,b,c){
			var query = {
				
			}
			fetchDataGet(query,GET_VALIDATION_WAIVER_LIST).then(function(data){				
				if(data.response.status){
					var vwdList = data.response.vwdList;
					if(vwdList.length>0){
						//$("#waiver-list-container").html(waiverListHb(vwdList));
						genrateGrid(vwdList);
						//statusConunt(vwdList);
						waiverIdDetailsMap={}
						for(let i=0;i<vwdList.length;i++){
							waiverIdDetailsMap[""+vwdList[i].validationWaiverId.waiverId+""] = vwdList[i];
						}
					}
					populateWaiverActiveAndInactiveCounts(vwdList);
				}
			},errorHandler)
		}
		
		function deactivateValidationWaiver(elm,wId,ev){
			ev.stopPropagation();
			var URL="";
			if($(elm).find('.activateIcon').length>0){
				URL=DEACTIVATE_VALIDATION_WAIVER;
			}else{
				URL=ACTIVATE_VALIDATION_WAIVER;
			}
			var query = {
				waiverId : wId
			}
			fetchData(query,URL).then(function(data){
				if(data.response.status){
					successAlert(data.response.message);
					getAllValidationWaiver();
				}else{
					failAlert(data.response.message);
				}
				
				
			},errorHandler)
		}
		
		function deleteValidationWaiver(wId){
				var query = {
					waiverId : wId
				}
				fetchData(query,DELETE_VALIDATION_WAIVER).then(function(data){
					$('#alertMessageId').empty();
					if(data.response.status){
						successAlert(data.response.message);
					}else{
						failAlert(data.response.message);
					}
					//alert(data.response.message);
					if(VIEW_MODE!="N"){
						location.reload()
					}else{
						getAllValidationWaiver();
					}
					
				},errorHandler)
		}
				
		function createNewQuery(){
			$('#waiverView').css('display','none');
			$('#addNewWaiver').css('display','block');
		    vs_generic_hideshow(["Version"],"none") ;
		   // vs_createBreadcrumb('big',[{page:"Home",url:"hrefval"},{page:"Manage Validation Waivers",url:"hrefval"},{page:"New Waiver",url:"hrefval"}]);
			$("#vs-breadcrumb-holderVNE").text("Add new validation Waiver")
		}
	    function changeVersion(ele){
	         var validateViewer = ele.value;
	         if(validateViewer == "Version"){
	            vs_generic_hideshow(["Version"],"block"); 
	            vs_generic_hideshow(["period"],"none"); 
	         }else{
	            vs_generic_hideshow(["period"],"block"); 
	            vs_generic_hideshow(["Version"],"none"); 
	         }
	         
		}
	    function addNewWaiver(){
			if (validateSaveAction()){
				var waiverTitle=$('#titleId').val();
				var regReportId=$('#returnIdInDeopdown').val();
				var orgId=$('#orgIdInDeopdown').val();
				
				
				var query = {
					waiverTitle : waiverTitle,
					regReportId : regReportId,
					validationIdCSV : null,
					startDate : null,
					endDate : null,
					effectiveDate : null,
					orgId : orgId,
					versionNo:null
				}
				if($("#period").is(":visible")){
					query["startDate"] = formatDate($("#rangeFromDate").find("input").val(),WAIVER_DISPLAY_FORMAT,LONG_DATE_FORMAT)
					query["endDate"] = formatDate($("#rangeToDate").find("input").val(),WAIVER_DISPLAY_FORMAT,LONG_DATE_FORMAT)
				}else{
					query["effectiveDate"] = formatDate($("#versionEffective").find("input").val(),WAIVER_DISPLAY_FORMAT,LONG_DATE_FORMAT);
					query["versionNo"] = $('#versiondropDownId').val();
				}
				let valSected = $(".tile-active");
				valIds = ""
				for(let i = 0;i<valSected.length;i++){
					valIds+=$(valSected[i]).attr("v-id")+",";
				}
				query["validationIdCSV"] = valIds.substr(0,valIds.length-1);
				
				fetchData(query,SAVE_VALIDATION_WAIVER).then(function(data){
					if(data.response.status){
						successAlert(data.response.message);
						location.reload();
					}else{
						failAlert(data.response.message);
					}
					
					
				},errorHandler)
			}
	    	
		
	    }
	    function cancelAddingNewWaiver(){
	    	$('#waiverView').css('display','block');
	    	$('#addNewWaiver').css('display','none');
	    	//BREAD CRUMB CHANGES  -- TODO
			$("#vs-breadcrumb-holder").text("Manage Validation Waivers")
			resetAllFieldsInCreateEditPage();
	    }
	    function searchWaiver(elm,e){
	    	//if (e.keyCode == 13) {
	    		var searchVal = $('#searchWaiverTitleId').val();
	    		//if(searchVal.trim()!=""){
	    			serachValidationWaiver(searchVal.trim());
	    		//}
	    	//}
	    }
	    function serachValidationWaiver(serachValue){
				fetchDataGet(formQueryForFilter(),SEARCH_VALIDATION_WAIVER).then(function(data){
					if(data.response.status){
						var vwdList = data.response.vwdList;
						if(vwdList.length>0){
							//$("#waiver-list-container").html(waiverListHb(vwdList));
							genrateGrid(vwdList);
						}else{
							$("#validationListKendo").html("<p class='vs-caption-uppercase-text'>No validation waiver found for the criteria.</p>");
						}
						populateWaiverActiveAndInactiveCounts(vwdList);
					}
				},errorHandler)
	    }
		function clearFilterCriteria(){
			$('#serachReturnId').val("");
			$('#serachOrgId').val("");
			$('#serachSectionId').val("");
			$('#serachValidationId').val("");
			$('#serachStatusId').val("");
			$('#applyFilterId').prop('disabled',true);
			$("#filterDatePicker").find("input").val("");
			$('.sortIcon').attr('order',"desc");
			getAllValidationWaiver();
			$("#waiverResultTitle").text("All Waivers");
			 
		}
		function applyFilter(){
			$("#waiverResultTitle").text("Filtered Waivers");
			fetchDataGet(formQueryForFilter(),SEARCH_VALIDATION_WAIVER).then(function(data){
					console.log(data);
					if(data.response.status){

						var vwdList = data.response.vwdList;
						if(vwdList.length>0){
							//$("#waiver-list-container").html(waiverListHb(vwdList));
							genrateGrid(vwdList);
						}else{
							$("#validationListKendo").html("<p class='vs-caption-uppercase-text'>No validation waiver found for the criteria.</p>");
						}
						populateWaiverActiveAndInactiveCounts(vwdList);
					}
				},errorHandler)

		}
		
		function formQueryForFilter(){
			var q = {};
			if($('#searchWaiverTitleId').val()!=""){
				q["waiverTitle"] = $('#searchWaiverTitleId').val().trim();
			}
			if($('#serachReturnId').val()!="-1"){
				q["regReportId"] = $('#serachReturnId').val().trim();
			}
			if($('#serachOrgId').val()!="-1"){
				q["orgId"] = $('#serachOrgId').val().trim();
			}
			if($('#serachSectionId').val()!="-1"){
				q["sectionId"] = $('#serachSectionId').val().trim();
			}
			if($('#serachValidationId').val()!="-1"){
				q["validationIdCSV"] = $('#serachValidationId').val().trim();
			}
			if($('#serachStatusId').val()!="-1"){
				q["status"] = $('#serachStatusId').val().trim();
			}
			if($("#filterDatePicker").find("input").val()!=""){
				q["effectiveDate"] = formatDate($("#filterDatePicker").find("input").val(),WAIVER_DISPLAY_FORMAT,LONG_DATE_FORMAT);
			}
			q["orderBy"] = $('.sortIcon').attr('order');
			return q;
		}
		
		
		function isDelete(wid,ev){
			ev.stopPropagation();
			var div='<div class="vs-alert info customAlertClass">	<div class="customAlertInnerClass">	  <strong>Confirm </strong>Do you want to delete	</div>	<div> <button class="vs-button-small vs-primary-one vs-right-mgn-16" type="button" onclick="proceedToDelete(true,\''+wid+'\')">Yes</button> <button class="vs-button-small vs-primary-one-outline" type="button" onclick="proceedToDelete(false,\''+wid+'\')">No</button>	  <span class="vs-alert-closebtn" onclick="proceedToDelete(false,\''+wid+'\')proceedToDelete(true,\''+wid+'\')"><i class="icon-medium icon-close"></i></span>	</div>	</div>';
			$('#alertMessageId').append(div);			
		}
		function proceedToDelete(val,wid){
			if(val){
				deleteValidationWaiver(wid);
			}else{
				$('#alertMessageId').empty();
				$('#alertMessageIdVNE').empty();
			}
		}
		function statusConunt(vwdList){
			var query = {
				}
			fetchDataGet(query,GET_ACTIVE_AND_INACTIVE_COUNT_OF_WAIVER).then(function(data){
				console.log(data);
				//$('#activeCountId').text("pass the count"); 
				//$('#inActiveCountId').text("pass the count");
			},errorHandler)
		}
		
		function fetchValidationsForReturn(rId){
			let query = {
				regReportId : rId
			}
			fetchDataGet(query,FETCH_VALIDATIONS_FOR_RETURN).then(function(data){
				//processing data for section and validation map
				
				let vData = data.response.data;
				let secStr = "<option value = '-1'>All</option>"
				var secSet = {};
				if(VIEW_MODE=="Y" && CURRENT_WAIVER_ID!=""){
					var tempList = [];
					let vIds = JSON.parse(waiverIdDetailsMap[CURRENT_WAIVER_ID].waiverInfo).validationIds;
					for(let i = 0; i<vData.length; i++){
						if(vIds.indexOf(vData[i].validationId+"")!=-1){
							tempList.push(vData[i])
						}
					}
					vData = tempList;
				}
				for(let i= 0;i<vData.length;i++){
					validationIdandDetailsMap[vData[i].validationId+""] = vData[i];
					secSet[vData[i].sectionId+""] = {
						sectionId : vData[i].sectionId,
						sectionName : vData[i].sectionName,
					}
				}
				
				var secFinalSet = Object.values(secSet);
				for(let i =0 ;i <secFinalSet.length;i++){
					secStr = secStr+ "<option value = '"+secFinalSet[i].sectionId+"' >"+secFinalSet[i].sectionName+"</option>"
				}
				$("#section-dropdown-validations").html(secStr);
				$("#validation-list-container").html(validationListHb(vData));
				//console.log(data)
			},errorHandler).then(
			function(){
				if(VIEW_MODE=="Y" && CURRENT_WAIVER_ID!=""){
					let wDetails = JSON.parse(waiverIdDetailsMap[CURRENT_WAIVER_ID].waiverInfo);
					let vIds = wDetails.validationIds;
					for(let i = 0; i<vIds.length;i++){
						vs_tile_select($('.val-card[v-id = "'+vIds[i]+'"]')[0])
					}
				}
			});
		}
		
		function showValidationDetails(vid,el,ev){
			ev.stopPropagation();
			vs_model_show('waiverInfoModal');
			$("#validationCodeModal").text(validationIdandDetailsMap[vid].validationCode)
			$("#valDescModal").text(validationIdandDetailsMap[vid].validationDesc)
			$("#valSectionModal").text(validationIdandDetailsMap[vid].sectionName)
			$("#valTypeModal").text(validationIdandDetailsMap[vid].validationType)
			$("#expressionDetailsByTime").html(validationExprListHb(validationIdandDetailsMap[vid].versionDetails))
		}
		
		function fetchVersionList(){
			
			var query = {
				returnID : $('#returnIdInDeopdown').val(),
				orgID:$('#orgIdInDeopdown').val(),
				isActive : "Y",
				effectiveDate : formatDate($("#versionEffective").find("input").val(),WAIVER_DISPLAY_FORMAT,PERIOD_ID_FORMAT)
			}
			fetchDataGet(query,FETCH_VERSIONS_FOR_RETURN).then(function(data){
				var secFinalSet = data.response;
				let secStr = "<option value='' selected>Select Option</option>";
				for(let i =0 ;i <secFinalSet.length;i++){
					secStr = secStr+ "<option value = '"+secFinalSet[i]+"' >"+secFinalSet[i]+"</option>"
				}
				$("#versiondropDownId").html(secStr)
			},errorHandler).then(
			function(){
				if(VIEW_MODE=="Y" && CURRENT_WAIVER_ID!=""){
					let wDetails = JSON.parse(waiverIdDetailsMap[CURRENT_WAIVER_ID].waiverInfo);
					$('#versiondropDownId').val(wDetails.versionNo);
				}
			});
		}
		
		function viewValidationWaiver(wid){
			$("#vs-breadcrumb-holderVNE").text("View / Edit Validation Waiver ")
			CURRENT_WAIVER_ID = wid;
			VIEW_MODE="Y";
			IS_VIEW = true;
			makeAllElementsViewOnly();
			$("#VNEButtons").css("display","flex");
			$("#editWavierButton").show();
			$('#waiverView').css('display','none');
			$('#addNewWaiver').css('display','block');
		    let cWaiver = waiverIdDetailsMap[wid];
			let waiverInfo = JSON.parse(cWaiver.waiverInfo)
			$('#titleId').val(cWaiver.waiverTitle);
			$('#returnIdInDeopdown').val(cWaiver.regReportId);
			$('#orgIdInDeopdown').val(cWaiver.orgId);
			$('#returnIdInDeopdown').prop("disabled",true)
			$('#orgIdInDeopdown').prop("disabled",true)
			$('#versiondropDownId').val("");
			$('#versiondropDownId').prop("disabled",true);
			fetchValidationsForReturn(cWaiver.regReportId);
			
			if(cWaiver.isActive==1){
				$("#deactivateWaiverButton").text("DEACTIVATE")
			}else{
				$("#deactivateWaiverButton").text("ACTIVATE")
			}
			
			if(waiverInfo.startDate==null || waiverInfo.startDate==""){
				$("input[value='Version']").prop("checked",true)
				vs_generic_hideshow(["Version"],"block"); 
	            vs_generic_hideshow(["period"],"none"); 
				//$('#versionEffective').find("input").val(formatDate(waiverInfo.effectiveDate,LONG_DATE_FORMAT,WAIVER_DISPLAY_FORMAT));
	            var defaultDate = formatDate(waiverInfo.effectiveDate,LONG_DATE_FORMAT,WAIVER_DISPLAY_FORMAT)
	        	var element = document.getElementById("versionEffective");
	        	element.setDataOptions({"displayFormat":"DD/MM/YYYY","defaultDate":defaultDate});
				fetchVersionList();
			}else{
				$("input[value='Period']").prop("checked",true)
				vs_generic_hideshow(["period"],"block"); 
	            vs_generic_hideshow(["Version"],"none");
				//$('#rangeFromDate').find("input").val(formatDate(waiverInfo.startDate,LONG_DATE_FORMAT,WAIVER_DISPLAY_FORMAT));
	            var defaultDate = formatDate(waiverInfo.startDate,LONG_DATE_FORMAT,WAIVER_DISPLAY_FORMAT)
	        	var element = document.getElementById("rangeFromDate");
	        	element.setDataOptions({"displayFormat":"DD/MM/YYYY","defaultDate":defaultDate});
				//$('#rangeToDate').find("input").val(formatDate(waiverInfo.endDate,LONG_DATE_FORMAT,WAIVER_DISPLAY_FORMAT));
	        	var defaultDate = formatDate(waiverInfo.endDate,LONG_DATE_FORMAT,WAIVER_DISPLAY_FORMAT)
	        	var element = document.getElementById("rangeToDate");
	        	element.setDataOptions({"displayFormat":"DD/MM/YYYY","defaultDate":defaultDate});
			}
			$("#cancelWaiverButton").show();
			$("#addNewWaiverButton").hide();
			

			//console.log(waiverIdDetailsMap[wid])
		}
		
		function updateWaiver(){
			
			if (validateSaveAction()){
				var waiverTitle=$('#titleId').val();
				var regReportId=$('#returnIdInDeopdown').val();
				var orgId=$('#orgIdInDeopdown').val();
				
				
				var query = {
					waiverId : CURRENT_WAIVER_ID,
					waiverTitle : waiverTitle,
					regReportId : regReportId,
					validationIdCSV : null,
					startDate : null,
					endDate : null,
					effectiveDate : null,
					orgId : orgId,
					versionNo:null
				}
				if($("#period").is(":visible")){
					query["startDate"] = formatDate($("#rangeFromDate").find("input").val(),WAIVER_DISPLAY_FORMAT,LONG_DATE_FORMAT)
					query["endDate"] = formatDate($("#rangeToDate").find("input").val(),WAIVER_DISPLAY_FORMAT,LONG_DATE_FORMAT)
				}else{
					query["effectiveDate"] = formatDate($("#versionEffective").find("input").val(),WAIVER_DISPLAY_FORMAT,LONG_DATE_FORMAT);
					query["versionNo"] = $('#versiondropDownId').val();
				}
				let valSected = $(".tile-active");
				valIds = ""
				for(let i = 0;i<valSected.length;i++){
					valIds+=$(valSected[i]).attr("v-id")+",";
				}
				query["validationIdCSV"] = valIds.substr(0,valIds.length-1);
				
				fetchData(query,UPDATE_VALIDATION_WAIVER).then(function(data){
					if(data.response.status){
						successAlert(data.response.message);
						VIEW_MODE = "N";
						CURRENT_WAIVER_ID = "";
						location.reload();
					}else{
						failAlert(data.response.message);
					}
					
					//alert(data.response.message);
					
					
					
				},errorHandler)
			}
	    	
		
		}
		
		function makeAllElementsViewOnly(){
			$("#titleId").prop("disabled",true);
			$("#toggle-one").prop("disabled",true);
			$("#toggle-two").prop("disabled",true);
			$("#versionEffective").find("input").prop("disabled",true);
			$("#rangeFromDate").find("input").prop("disabled",true);
			$("#rangeToDate").find("input").prop("disabled",true);
		}
		
		function selectCurrentValidation(ele){
			if(!IS_VIEW){
				vs_tile_select(ele)
			}
		}
		
		function editWaiver(){
			$("#editWavierButton").hide();
			IS_VIEW = false;
			$("#titleId").prop("disabled",false);
			$("#toggle-one").prop("disabled",false);
			$("#toggle-two").prop("disabled",false);
			$("#versionEffective").find("input").prop("disabled",false);
			$("#rangeFromDate").find("input").prop("disabled",false);
			$("#rangeToDate").find("input").prop("disabled",false);
			$("#updateWavierButton").show();
			$('#versiondropDownId').prop("disabled",false);
		}
		
		function deleteWaiverFromVnE(e){
			isDeleteVnE(CURRENT_WAIVER_ID,e)
		}
		
		function deactivateWaiverFromVnE(){
			var URL="";
			if(waiverIdDetailsMap[CURRENT_WAIVER_ID].isActive==1){
				URL=DEACTIVATE_VALIDATION_WAIVER;
			}else{
				URL=ACTIVATE_VALIDATION_WAIVER;
			}
			var query = {
				waiverId : CURRENT_WAIVER_ID
			}
			fetchData(query,URL).then(function(data){
				if(data.response.status){
					successAlert(data.response.message);
					location.reload();
				}else{
					failAlert(data.response.message);
				}
				//alert(data.response.message);
				//location.reload();
			},errorHandler)
		}
		
		function isDeleteVnE(wid,ev){
			ev.stopPropagation();
			var div='<div class="vs-alert info customAlertClass">	<div class="customAlertInnerClass">	  <strong>Confirm </strong>Do you want to delete	</div>	<div> <button class="vs-button-small vs-primary-one vs-right-mgn-16" type="button" onclick="proceedToDelete(true,\''+wid+'\')">Yes</button> <button class="vs-button-small vs-primary-one-outline" type="button" onclick="proceedToDelete(false,\''+wid+'\')">No</button>	  <span class="vs-alert-closebtn" onclick="proceedToDelete(false,\''+wid+'\')"><i class="icon-medium icon-close"></i></span>	</div>	</div>';
			$('#alertMessageIdVNE').append(div);			
		}
		
		function resetAllFieldsInCreateEditPage(){
			$('#titleId').val("");
			$('#returnIdInDeopdown').val("");
			$('#orgIdInDeopdown').val("");
			$('#versiondropDownId').val("");
			$('#versiondropDownId').prop("disabled",true)
			$('#returnIdInDeopdown').prop("disabled",false)
			$('#orgIdInDeopdown').prop("disabled",false)
			$("input[value='Period']").prop("checked",true)
			vs_generic_hideshow(["period"],"block"); 
			vs_generic_hideshow(["Version"],"none");
			$("#cancelWaiverButton").show()
			$("#addNewWaiverButton").show();
			$("#updateWavierButton").hide();
			$("#VNEButtons").hide();
			IS_VIEW = false;
			$("#titleId").prop("disabled",false);
			$("#toggle-one").prop("disabled",false);
			$("#toggle-two").prop("disabled",false);
			$("#versionEffective").find("input").prop("disabled",false);
			$("#rangeFromDate").find("input").prop("disabled",false);
			$("#rangeToDate").find("input").prop("disabled",false);
			VIEW_MODE = "N";
			CURRENT_WAIVER_ID = "";
			$("#validation-list-container").html("<p class='vs-caption-uppercase-text'>CHOOSE RETURN TO SEE LIST OF VALIDATIONS</p>")
		}
		function getOrgDetails(oi){
			return orgIdandDetailsMap[oi].orgCode + " - " + orgIdandDetailsMap[oi].orgName;
		}
		function getReturnDetails(oi){
			return reportIdandDetailsMap[oi].returns;
		}
		
		function fetchValidationsForDropdown(rId){
			let query = {
				regReportId : rId
			}
			fetchDataGet(query,FETCH_VALIDATIONS_FOR_RETURN).then(function(data){
				//processing data for section and validation map
				
				let vData = data.response.data;
				var secSet = {};
				let valStr = "<option value = '-1'>Select Validation</option>";
				for(let i= 0;i<vData.length;i++){
					valStr = valStr + "<option value = '"+vData[i].validationId+"'>"+vData[i].validationCode+"</option>"
					secSet[vData[i].sectionId+""] = {
						sectionId : vData[i].sectionId,
						sectionName : vData[i].sectionName,
					}
				}
				$("#serachValidationId").html(valStr);
				$("#serachValidationId").prop("disabled",false);
				var sectionList = Object.values(secSet);
				
				var optStr="<option value = '-1'>Select Return Sections</option>";
				for(var i= 0;i<sectionList.length;i++){
					optStr = optStr + "<option value = '"+sectionList[i].sectionId+"' name='"+sectionList[i].sectionName+"'>"+sectionList[i].sectionName+"</option>"
				}
				
				$("#serachSectionId").html(optStr);
				$("#serachSectionId").prop("disabled",false);
				//console.log(data)
			},errorHandler)
		}
		
		
		function populateWaiverActiveAndInactiveCounts(vwdList){
			let activeCount =0;
			let inActiveCount = 0;
			if(vwdList.length>0){
				for(let i= 0;i<vwdList.length;i++){
					if(vwdList[i].isActive==1){
						activeCount++;
					}else{
						inActiveCount++;
					}
				}
			}
			$("#activeCountId").text(activeCount)
			$("#inActiveCountId").text(inActiveCount)
		}
		function orderByQueryResult(val){
			$('.sortIcon').attr('order',val);
			applyFilter();
		}
	function genrateGrid(vwdList){
		
		vwdList = processValidationWaiverDataForGrid(vwdList)
		
		$("#validationListKendo").html("");
	if ($("#validationWaiverListKendo").data("kendoGrid") != undefined){
	    $("#validationWaiverListKendo").data("kendoGrid").destroy();
	}
	$("#validationWaiverListKendo").empty();
					
			$("#validationListKendo").kendoGrid({
				dataSource: {
					data: vwdList,
					pageSize: PAGE_SIZE_FOR_WAVIER_LIST,
					schema: {
						model :{
							fields :{
								waiverId : {
									type :"string"
								},waiverTitle : {
									type :"string"
								},createdTime : {
									type :"number"
								},isActive : {
									type :"number"
								},isDeleted : {
									type :"number"
								},lastModifiedBy : {
									type :"number"
								},lastModifiedTime : {
									type :"number"
								},orgId : {
									type :"number"
								},regReportId : {
									type :"number"
								},solutionId : {
									type :"number"
								},waiverInfo : {
									type :"string"
								},createdBy : {
									type :"number"
								}
							}
						}
					}
				},
				sortable: true,
				pageable: {
					input: true,
					numeric: false
				},
				filterable: false,
				noRecords: true,
				
				columns: [{
						field: "waiverId",
						title: "waiverId",
						filterable: false,
						template: '#=generateWaiverSlab(createdBy,createdTime,isActive,isDeleted,lastModifiedBy,lastModifiedTime,orgId,regReportId,solutionId,waiverId,waiverInfo,waiverTitle)#'
					}
				],
				dataBound: function(e) {
					$(".overlay").css("display", "none");
				}
			});
		}
		function generateWaiverSlab(createdBy,createdTime,isActive,isDeleted,lastModifiedBy,lastModifiedTime,orgId,regReportId,solutionId,waiverId,waiverInfo,waiverTitle){
			let obj = {createdBy : createdBy
						,createdTime : createdTime
						,isActive : isActive
						,isDeleted : isDeleted
						,lastModifiedBy : lastModifiedBy
						,lastModifiedTime : lastModifiedTime
						,orgId : orgId
						,regReportId : regReportId
						,solutionId : solutionId
						,waiverId : waiverId
						,waiverInfo : waiverInfo
						,waiverTitle : waiverTitle
				}
			return waiverListHb(obj)
		}
		
		function processValidationWaiverDataForGrid(vl){
			for(let i = 0; i<vl.length ; i++){
				vl[i]["waiverId"] = vl[i].validationWaiverId.waiverId
			}
			return vl;
		}
	

		return {
			getAllValidationWaiver : function(name,id, elm){
				getAllValidationWaiver(name,id, elm);
			},createNewQuery:function(){
				createNewQuery();
			},changeVersion : function(ele){
				changeVersion(ele);
			},deactivateValidationWaiver : function(elm,wid,ev){
				deactivateValidationWaiver(elm,wid,ev)
			},deleteValidationWaiver : function(wid){
				deleteValidationWaiver(wid)
			},addNewWaiver : function(){
				addNewWaiver();
			},cancelAddingNewWaiver:function(){
				cancelAddingNewWaiver();
			},searchWaiver : function(elm,e){
				searchWaiver(elm,e);
			},serachValidationWaiver:function(serachValue){
				serachValidationWaiver(serachValue);
			},clearFilterCriteria : function(){
				clearFilterCriteria();
			},applyFilter : function(){
				applyFilter();
			},proceedToDelete : function(val, wid){
				proceedToDelete(val, wid);
			},isDelete : function(wid,ev){
				isDelete(wid,ev);
			},fetchValidationsForReturn : function(rId){
				fetchValidationsForReturn(rId);
			},showValidationDetails : function(vid,el,ev){
				showValidationDetails(vid,el,ev);
			},fetchVersionList : function(){
				fetchVersionList();
			},viewValidationWaiver : function(wid){
				viewValidationWaiver(wid);
			},updateWaiver : function(){
				updateWaiver();
			},selectCurrentValidation : function(ele){
				selectCurrentValidation(ele);
			},editWaiver : function(){
				editWaiver();
			},deleteWaiverFromVnE : function(e){
				deleteWaiverFromVnE(e);
			},deactivateWaiverFromVnE : function(){
				deactivateWaiverFromVnE();
			},getOrgDetails : function(oi){
				return getOrgDetails(oi);
			},getReturnDetails : function(ri){
				return getReturnDetails(ri);
			},fetchValidationsForDropdown : function(ri){
				return fetchValidationsForDropdown(ri);
			},orderByQueryResult : function(val){
				return orderByQueryResult(val);
			},generateWaiverSlab : function(createdBy,createdTime,isActive,isDeleted,lastModifiedBy,lastModifiedTime,orgId,regReportId,solutionId,waiverId,waiverInfo,waiverTitle){
				return generateWaiverSlab(createdBy,createdTime,isActive,isDeleted,lastModifiedBy,lastModifiedTime,orgId,regReportId,solutionId,waiverId,waiverInfo,waiverTitle);
			}
		}
	}();
	//getAllValidationWaiver();
});





function getAllValidationWaiver(a,b,c){
	vw.getAllValidationWaiver(a,b,c)
}
function createNewQuery(){
	vw.createNewQuery();
}
function changeVersion(ele){
	vw.changeVersion(ele);
}
function deactivateValidationWaiver(elm,w,ev){
	vw.deactivateValidationWaiver(elm,w,ev);
}
function deleteValidationWaiver(ele){
	vw.deleteValidationWaiver(ele);
}
function addNewWaiver() {
	vw.addNewWaiver();
}
function cancelAddingNewWaiver(){
	vw.cancelAddingNewWaiver();
}
function searchWaiver(elm,e){
	vw.searchWaiver(elm,e);
}
function serachValidationWaiver(serachValue){
	vw.serachValidationWaiver(serachValue);
}
function clearFilterCriteria(){
	vw.clearFilterCriteria();
}
function applyFilter(){
	vw.applyFilter();
}
function enablingAndDisablingButtons(){
	 $('.filterCriteriaClass').on('change', function(){
	      $('#applyFilterId').prop('disabled',false);
	});
}
function proceedToDelete(val, wid){
	vw.proceedToDelete(val, wid);
}
function isDelete(wid,ev){
	vw.isDelete(wid,ev);
}
function showValidationDetails(vId,el,ev){
	vw.showValidationDetails(vId,el,ev);
}

function onChangeOfReturn(){
	$('#serachReturnId').on('change', function(){
		if($('#serachReturnId').val()!=="-1"){
			//getSectionDetails();
			//TODO populate validation list for drop down
			fetchValidationsForDropdown($('#serachReturnId').val());
		}else{
			$("#serachSectionId").prop("disabled",true);
			$("#serachValidationId").prop("disabled",true);
		}
	     
	});
	$('#returnIdInDeopdown').on('change', function(){
		if($('#returnIdInDeopdown').val()!=="Select Return"){
			vw.fetchValidationsForReturn($('#returnIdInDeopdown').val());
			$('#versiondropDownId').prop("disabled",false);
			fetchVersionList();
		}else{
			//$("#serachSectionId").prop("disabled",true);
			$('#versiondropDownId').prop("disabled",true)
		}
	     
	});
}
function getSectionDetails(){
	var query = {
			returns:$('#serachReturnId').find('option:selected').attr("returnvalue"),
			isExpr:"Y"
	}
	fetchDataGet(query,'./getsectionsforValidationWaiver.htm').then(function(data){
		sectionList=data.response;
		var optStr="";
		for(var i= 0;i<sectionList.length;i++){
			optStr = optStr + "<option value = '"+sectionList[i].section+"' name='"+sectionList[i].sectionName+"' sectionType='"+sectionList[i].sectionType+"'>"+sectionList[i].section+"</option>"
		}
		
		$("#serachSectionId").append(optStr);
		$("#serachSectionId").prop("disabled",false);
	},errorHandler)
}


function fetchVersionList(){
	if($('#returnIdInDeopdown').val()!="" && $('#orgIdInDeopdown').val()!="" && $('#orgIdInDeopdown').val()!=null && $('#returnIdInDeopdown').val()!=null){
		vw.fetchVersionList();
	}
	
}

function onInputHandler(){
	
}

function viewValidationWaiver(wid){
	vw.viewValidationWaiver(wid);
}

function updateWaiver(){
	vw.updateWaiver()
}

function editWaiver(){
	vw.editWaiver();
}

function selectCurrentValidation(ele){
	vw.selectCurrentValidation(ele);
}

function deactivateWaiverFromVnE(){
	vw.deactivateWaiverFromVnE()
}
function deleteWaiverFromVnE(e){
	vw.deleteWaiverFromVnE(e)
}

function getOrgDetails(orgId){
	return vw.getOrgDetails(orgId)
}
function getReturnDetails(rId){
	return vw.getReturnDetails(rId)
}
function fetchValidationsForDropdown(rId){
	return vw.fetchValidationsForDropdown(rId)
}
function orderByQueryResult(val){
	return vw.orderByQueryResult(val)
}

function enableFilterButton(){
	$('#applyFilterId').prop('disabled',false);
}

function fetchAllActiveWaivers(){
	$("#serachStatusId").val("1");
	applyFilter();
}
function fetchAllInActiveWaivers(){
	$("#serachStatusId").val("0");
	applyFilter();
}

function generateWaiverSlab(createdBy,createdTime,isActive,isDeleted,lastModifiedBy,lastModifiedTime,orgId,regReportId,solutionId,waiverId,waiverInfo,waiverTitle){
	return vw.generateWaiverSlab(createdBy,createdTime,isActive,isDeleted,lastModifiedBy,lastModifiedTime,orgId,regReportId,solutionId,waiverId,waiverInfo,waiverTitle);
}


//-----------------Util functions-------------------------
//ajax loader.
$(document).ajaxStart(function(){
	$(".overlay").css("display","block");
});
$(document).ajaxStop(function(){
	$(".overlay").css("display","none");
});

//post
var fetchData = function(query,dataURL) {
	// Return the $.ajax promise
	return $.ajax({
		data: query,
		dataType: 'json',
		url: dataURL,
		type: "POST"
	});
}

//get
//variable for ajax.

var fetchDataGet = function(query,dataURL) {
	// Return the $.ajax promise
	return $.ajax({
		data: query,
		dataType: 'json',
		url: dataURL,
	});
}

//error handler.
function errorHandler(xhr,status,error){
	$("#noValidationsFound").show()
	$("#validationBySectionGridContainer").hide();
	$("#downloadSummary").hide();
	failAlert(status +" : "+ xhr.status+" :" +error.toString());
	
}

//date format changes
function formatDate(currentlyDisplayedDate , currentlyDisplayedDateFormat, outputDateFormat){
	var ipDate = moment(currentlyDisplayedDate, currentlyDisplayedDateFormat); 
	//format that date into a different format
	return moment(ipDate).format(outputDateFormat);
}

function successAlert(msg){
	var div='<div id="successAlert" class="vs-alert success customAlertClass">'+
			  '	<div class="customAlertInnerClass">'+
			  '	  <strong>Success! </strong>'+msg+
			  '	</div>'+
			  '	<div>'+
			  '	  <span class="vs-alert-closebtn" onclick="closeAlert()"><i class="icon-medium icon-close"></i></span>'+
			  '	</div>'+
			  '	</div>';
	$('#alertsPanel').empty();
	$('#alertsPanel').append(div);
	successTimeOut("successAlert") ;
}
function closeAlert(){
	$('.customAlertClass').detach();
}

function failAlert(msg){
	var div='<div class="vs-alert error customAlertClass">'+
			'  <div class="customAlertInnerClass">'+
			'    <strong>Error! </strong>'+msg+
			'  </div>'+
			'  <div>'+
			'    <span class="vs-alert-closebtn"  onclick="closeAlert()" ><i class="icon-medium icon-close"></i></span>'+
			'  </div>'+
			'</div>';
		$('#alertsPanel').empty();
		$('#alertsPanel').append(div);	
}

function warningAlert(msg){
	var div='<div class="vs-alert warning customAlertClass">'+
			  '	<div class="customAlertInnerClass">'+
			  '	  <strong>Warning! </strong>'+msg+
			  '	</div>'+
			  '	<div>'+
			  '	  <span class="vs-alert-closebtn" onclick="closeAlert()"><i class="icon-medium icon-close"></i></span>'+
			  '	</div>'+
			  '	</div>';

		$('#alertsPanel').empty();
		$('#alertsPanel').append(div);
	
}