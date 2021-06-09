var  overdueDates = [];
var  failedDates = [];

//to have reference of clicked entity for lineage
var lineageForEntity = {};


//navigating variables fromm tiles
var statusToNavigate = "";
var dataConsumersTypeToNavigate = "";
var solutionToNavigate = "";
var sourceSystemTabClickFromTile = "N";

$(document).ajaxStart(function(){
	$(".overlay").css("display","block");
});
$(document).ajaxStop(function(){
		$(".overlay").css("display","none");
});

$(document).ready(function(){
if (navigator.userAgent.indexOf("Chrome")==-1) {
    
	$('head').append('<link rel="stylesheet" href="css/framework/pageContents/mozillaFixes.css" type="text/css" />');
}                              
	
setInterval(function(){$(".fa-magic").toggleClass("showTimer")}, 10000);

	
$("#arrowSwitch").change(function() {
			if(this.checked){
				$("#dataLineageIframe").contents().find(".arrowInput").show();
				
				//$(".connection").show();
				//updateContainers();
				$("#dataLineageIframe").contents().find("path").show();
				//drawConnectionsMethod();
				
			}else{
				$.each($("#dataLineageIframe").contents().find("path"),function (k,v){
					if($(v).attr("stroke-dasharray")!=""){
						$(v).hide();
					}
				});
				$("#dataLineageIframe").contents().find(".arrowInput").hide();
				$("#dataLineageIframe").contents().find(".hightLightSlab > .arrowInput").show();
				//$(".connection").remove();
			}
			
		});
		$("#descriptionSwitch").change(function() {
				
			
			if(this.checked){
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				$("#dataLineageIframe").contents().find(".sourceDescription").show();
				$("#dataLineageIframe").contents().find(".receptionDescription").show();
				$("#dataLineageIframe").contents().find(".datahubDescription").show();
				$("#dataLineageIframe").contents().find(".stagingDescription").show();
				$("#dataLineageIframe").contents().find(".martDescription").show();
				$("#dataLineageIframe").contents().find(".dataConsumerDescription").show();
				$("#dataLineageIframe").contents().find(".nodes").css("height","60px");
				$("#dataLineageIframe").contents().find(".stagingDescription").show(); 
							document.getElementById("dataLineageIframe").contentWindow.drawConnectionsMethod();

				},0);
			}else{
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				$("#dataLineageIframe").contents().find(".sourceDescription").hide();
				$("#dataLineageIframe").contents().find(".receptionDescription").hide();
				$("#dataLineageIframe").contents().find(".stagingDescription").hide();
				$("#dataLineageIframe").contents().find(".datahubDescription").hide();
				$("#dataLineageIframe").contents().find(".martDescription").hide();
				$("#dataLineageIframe").contents().find(".dataConsumerDescription").hide();
				$("#dataLineageIframe").contents().find(".nodes").css("height","30px");
				$("#dataLineageIframe").contents().find(".stagingDescription").hide(); 
							document.getElementById("dataLineageIframe").contentWindow.drawConnectionsMethod();

				},0)
			}
			
//			if(true){
			

			
		//}
		//$(".overlay").hide();
	});
		
	$("#statusSwitch").change(function (){
			
		if(this.checked){
			$("#dataLineageIframe").contents().find(".tooltip").removeClass("hideToolTip")
		}else{
			$("#dataLineageIframe").contents().find(".tooltip").addClass("hideToolTip")
		}
		
	});

	$("#showSwitch").change(function() {
			if(this.checked){
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				$("#dataLineageIframe").contents().find(".hidePaths").removeClass("hidePaths");
				$("#dataLineageIframe").contents().find(".parentHidden").removeClass("parentHidden");
				$("#dataLineageIframe").contents().find(".hideLightNodes").removeClass("hideLightNodes");
				document.getElementById("dataLineageIframe").contentWindow.highlightCommonsFromIframe();
				$("#dataLineageIframe").contents().find("path").css("display","");
				},0)
			}else{
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				$("#dataLineageIframe").contents().find("path").addClass("hidePaths");
				$("#dataLineageIframe").contents().find(".nodes").addClass("hideLightNodes");
				document.getElementById("dataLineageIframe").contentWindow.highlightCommonsFromIframe();
				$("#dataLineageIframe").contents().find("path").css("display","");
				},0)
			}
			
		});	
$('#sourceSystemSearch').focus(function() {
		$(this).addClass("highlightInput");
 
});
 
$('#sourceSystemSearch').blur(function() {
    $(this).removeClass("highlightInput");
});
$('#dataRepoSearch').focus(function() {
		$(this).addClass("highlightInput");
 
});
 
$('#dataRepoSearch').blur(function() {
    $(this).removeClass("highlightInput");
});
$('#dataConsumerSearch').focus(function() {
		$(this).addClass("highlightInput");
 
});
 
$('#dataConsumerSearch').blur(function() {
    $(this).removeClass("highlightInput");
});
		$("#sourceSystemCheckBox").change(function() {
			$("#dataLineageIframe").contents().find(".disabledFlow").removeClass("disabledFlow")
			if(this.checked){
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				selectOrRemoveAllSourceSystem(true);
				$("#sourceSystemResultContainer").find(".filterNodes").removeClass("lowShade")
				$("#sourceSystemResultContainer").find(".filterNodes").find(".nodeImage").removeClass("fa-plus-circle")
				$("#sourceSystemResultContainer").find(".filterNodes").find(".nodeImage").addClass("fa-minus-circle")
				},0)
			}else{
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				selectOrRemoveAllSourceSystem(false);
				$("#sourceSystemResultContainer").find(".filterNodes").addClass("lowShade")
				$("#sourceSystemResultContainer").find(".filterNodes").find(".nodeImage").addClass("fa-plus-circle")
				$("#sourceSystemResultContainer").find(".filterNodes").find(".nodeImage").removeClass("fa-minus-circle")
				},0);
			}
			
		});
		$("#dataRepoCheckBox").change(function() {
			
			$("#dataLineageIframe").contents().find(".disabledFlow").removeClass("disabledFlow")
			if(this.checked){
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				selectOrRemoveAllDataRepo(true);
				$("#dataRepositoryResultContainer").find(".filterNodes").removeClass("lowShade")
				$("#dataRepositoryResultContainer").find(".filterNodes").find(".nodeImage").removeClass("fa-plus-circle")
				$("#dataRepositoryResultContainer").find(".filterNodes").find(".nodeImage").addClass("fa-minus-circle")
				},0)
			}else{
				 window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				selectOrRemoveAllDataRepo(false);
				$("#dataRepositoryResultContainer").find(".filterNodes").addClass("lowShade")
				$("#dataRepositoryResultContainer").find(".filterNodes").find(".nodeImage").addClass("fa-plus-circle")
				$("#dataRepositoryResultContainer").find(".filterNodes").find(".nodeImage").removeClass("fa-minus-circle")
				},0)
			}
			
		});
		$("#dataConsumerCheckBox").change(function() {
			$("#dataLineageIframe").contents().find(".disabledFlow").removeClass("disabledFlow")
			if(this.checked){
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				selectOrRemoveAllDataConsumer(true);
				$("#dataConsumerResultContainer").find(".filterNodes").removeClass("lowShade")
				$("#dataConsumerResultContainer").find(".filterNodes").find(".nodeImage").removeClass("fa-plus-circle")
				$("#dataConsumerResultContainer").find(".filterNodes").find(".nodeImage").addClass("fa-minus-circle")
				},0)
			}else{
				window.parent.$(".overlay").css("display","block");
				setTimeout(function(){
				
				selectOrRemoveAllDataConsumer(false);
				$("#dataConsumerResultContainer").find(".filterNodes").addClass("lowShade")
				$("#dataConsumerResultContainer").find(".filterNodes").find(".nodeImage").addClass("fa-plus-circle")
				$("#dataConsumerResultContainer").find(".filterNodes").find(".nodeImage").removeClass("fa-minus-circle")
				},0)
			}
			
		});


	window.onscroll = function() {scrollFunction()};
	

	$(function() {
		$("#cbdDatePicker").datepicker({
		changeYear: true,
		changeMonth: true,
		format: displayDateFormat,
		autoclose: true,
		beforeShowDay: function(date){
			var formattedDate = dateFormatter(date);
			if ($.inArray(formattedDate, overdueDates)!= -1){
			   return {
				  classes:"overdue"
				  
			   };
		   }
		   if ($.inArray(formattedDate, failedDates)!= -1){
			   return {
				  classes:"failed"
			   };
		   }
		  return;
		}
		}).on('changeDate',cbdChanged);		
		fetchData("","/dldwebapplication/getcurrrentBusinessDateStatisticsForLoad").then(onloadGetMaxBusinessDate,errorHandler);

		var data=[
				{SourceSystem:"NCB",TaskSummary:"18/18",EntitySummary:"12/12",ContactDetails:"NCB Owner",SourceSystemStatus:"COMPLETED"},
				{SourceSystem:"NCB",TaskSummary:"18/18",EntitySummary:"12/12",ContactDetails:"NCB Owner",SourceSystemStatus:"COMPLETED"},
				{SourceSystem:"NCB",TaskSummary:"18/18",EntitySummary:"12/12",ContactDetails:"NCB Owner",SourceSystemStatus:"FAILED"}
			]
		
				
		
	});	
});

function cbdChanged(){
	
	var changedDate = new Date(dateFormatterForNewDate($("#cbdDatePicker").val()));
	//changing CBD.
	var new_date = moment($("#cbdDatePicker").val(), "DD-MM-YYYY")
	if(moment($("#cbdDatePicker").val(), "DD-MM-YYYY").isSame(moment("31-12-9999", "DD-MM-YYYY"),"days")){
		$("#cbdIncrement").hide();
	}
	else{
	$("#cbdIncrement").show();}
	$(".cbdDateDay").text(changedDate.getDate());
	$(".cbdDateMonth").text(monthNames[changedDate.getMonth()]);
	$(".cbdDateYear").text(changedDate.getFullYear());
	$(".cbdDateWeek").text(weekday[changedDate.getDay()]);
	
	onloadCbdChanges()
}


function incrementDate(){
	
	var new_date = moment($("#cbdDatePicker").val(), "DD-MM-YYYY").add(1, 'days');
	moment(maxCurBusinessDate, "DD-MM-YYYY")
	if(moment($("#cbdDatePicker").val(), "DD-MM-YYYY").isSame(moment("31-12-9999", "DD-MM-YYYY"),"days")){
		$("#cbdIncrement").hide();
	}
	var changedDate = new Date(dateFormatterForNewDate(new_date));
	//changing CBD.
	$("#cbdDatePicker").datepicker('setDates', changedDate);
	$(".cbdDateDay").text(changedDate.getDate());
	$(".cbdDateMonth").text(monthNames[changedDate.getMonth()]);
	$(".cbdDateYear").text(changedDate.getFullYear());
	$(".cbdDateWeek").text(weekday[changedDate.getDay()]);
	$("#cbdDatePicker").val(dateFormatter(new_date));
	//onloadCbdChanges()
}

function decrementDate(){
	
	$("#cbdIncrement").show();
	var new_date = moment($("#cbdDatePicker").val(), "DD-MM-YYYY").subtract(1, 'days');
	var changedDate = new Date(dateFormatterForNewDate(new_date));
	//changing CBD.
	$("#cbdDatePicker").datepicker('setDates', changedDate);
	$(".cbdDateDay").text(changedDate.getDate());
	$(".cbdDateMonth").text(monthNames[changedDate.getMonth()]);
	$(".cbdDateYear").text(changedDate.getFullYear());
	$(".cbdDateWeek").text(weekday[changedDate.getDay()]);
	$("#cbdDatePicker").val(dateFormatter(new_date));
	//onloadCbdChanges()
}

function onloadCbdChanges(){
	
	//actual and progress call
	$("actualDataContainer").removeClass("expanded");
	$("actualDataContainer").addClass("collapsed");
	expandCollapseProgressActual($("actualDataContainer"));
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();	
	var changedDate = new Date(dateFormatterForNewDate($("#cbdDatePicker").val()));
	$("#cbdChart").empty();
	$("#previousDayChart").empty();
	$("#previousMonthChart").empty();
	var query={businessDate:dateFormatter(changedDate)}
	fetchData(query,"/dldwebapplication/getStatsOnChange").then(domChangesOnChangeOfBusinessDate,errorHandler);	
	$("#stagSummaryDetailContainer").show();
	$("#sourceSystemClicked").hide();
	$("#drClicked").hide();
	$("#consumersClicked").hide();
	$("#stageSummaryAccordianParent").removeClass("expanded");
	$("#stageSummaryAccordianParent").addClass("collapsed");
	$("#stageSummaryAccordianParent").find(".arrowStyle").addClass("fa-chevron-right");
	$("#stageSummaryAccordianParent").find(".arrowStyle").removeClass("fa-chevron-down");	
	$("#stageSummaryAccordianDetailsParent").hide();
	
	$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
	if(!$("#allTaskGrid").hasClass("DLHidden")){
		showHideDataLineage($("#allTaskGrid"));
		showHideDataLineage($("#allTaskGrid"));
	}
}


function getLoadActualProgressDetails(){
	$("#actualDataContainer").empty();
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV
			 }
	fetchData(query,"/dldwebapplication/getloadactualprogressdetails").then(domChangesForLoadActualProgressDetails,errorHandler);	
}

//handling the collapse and expand of actual vs progress slab 
function expandCollapseProgressActual(elm){
	if($(elm).hasClass("expanded")){
		$(elm).addClass("collapsed");
		$(elm).find(".arrowStyle").addClass("fa-chevron-right");
		$(elm).removeClass("expanded");
		$(elm).find(".arrowStyle").removeClass("fa-chevron-down");
		$("#actualProgressAccordianDetailsParent").slideUp("slow");;
		$("#actualDataContainer").empty();
	}else{
		getLoadActualProgressDetails();
		$(elm).removeClass("collapsed");
		$(elm).find(".arrowStyle").removeClass("fa-chevron-right");
		$(elm).addClass("expanded");
		$(elm).find(".arrowStyle").addClass("fa-chevron-down");
		$("#actualProgressAccordianDetailsParent").slideDown("slow");
		
	}
}

		
function filterBasedOnFrequency(elm){
	if($(elm).hasClass("freqFilterApplied")){
		$(elm).removeClass("freqFilterApplied");
	}else{
		$(elm).addClass("freqFilterApplied");
	}
	getLoadActualProgressDetails();
}		

function filterBasedOnFlowType(elm){
	if($(elm).hasClass("flowFilterApplied")){
		$(elm).removeClass("flowFilterApplied");
	}else{
		$(elm).addClass("flowFilterApplied");
	}
	getLoadActualProgressDetails();
	
	fetchDetailsForStaging();
	$("#drClicked").hide();
	$("#sourceSystemClicked").hide();
	$("#stageSummaryAccordianParent").show();
	$("#stagSummaryDetailContainer").show();
	$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
	$("#stageSummaryAccordianParent").removeClass("expanded");
	$("#stageSummaryAccordianParent").find(".arrowStyle").addClass("fa-chevron-right");
	$("#stageSummaryAccordianParent").find(".arrowStyle").removeClass("fa-chevron-down");
	
}


//stag summary functions
function expandCollapseStageSummary(elm){
	if($(elm).hasClass("expanded")){
		$(elm).addClass("collapsed");
		$(elm).find(".arrowStyle").addClass("fa-chevron-right");
		$(elm).removeClass("expanded");
		$(elm).find(".arrowStyle").removeClass("fa-chevron-down");
		$("#stageSummaryAccordianDetailsParent").slideUp("slow");
	}else{
		fetchDetailsForStaging();
		$(elm).removeClass("collapsed");
		$(elm).find(".arrowStyle").removeClass("fa-chevron-right");
		$(elm).addClass("expanded");
		$(elm).find(".arrowStyle").addClass("fa-chevron-down");
		$("#stageSummaryAccordianDetailsParent").slideDown("slow");
		
	}
}



function fetchDetailsForStaging(){
	//getching details for src systems and data repo.
	getDetailsForSourceSystems();
	getDetailsForDataRepository();
	getsdetailsfordataconsumers();
}

function getDetailsForSourceSystems(){

	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV
			 }
	fetchData(query,"/dldwebapplication/getstagingdetailsforsourcesystems").then(domChangesForDetailsForSourceSystems,errorHandler);	
}

function getDetailsForDataRepository(){
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV
			 }
	fetchData(query,"/dldwebapplication/getstagingdetailsfordatarepository").then(domChangesForDetailsForDataRepository,errorHandler);	
}

function getsdetailsfordataconsumers(){
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV
			 }
	fetchData(query,"/dldwebapplication/getsdetailsfordataconsumers").then(domChangesForDetailsForDataConsumers,errorHandler);	
}



function onloadGetMaxBusinessDate(data)
{
	maxCurBusinessDate=new Date(formatDateForNewDate(data.curBusinessDate));
	$("#cbdDatePicker").datepicker('update', new Date(formatDateForNewDate(data.curBusinessDate)));
	if(moment($("#cbdDatePicker").val(), "DD-MM-YYYY").isSame(moment("31-12-9999", "DD-MM-YYYY"),"days")){
		$("#cbdIncrement").hide();
	}
	
	$("#cbdDatePicker").datepicker('setEndDate', new Date(formatDateForNewDate("31-12-9999")));
	//cbdChanged();	
}

function domChangesOnChangeOfBusinessDate(data){
	if(!(data.prevBusDateStats.prevBusinessDate=="No Previous Business Date Found"))
	{	
		$("#previousDayDateContainer").show();
		$("#previousDayDateContainer").next().show();
		$("#noPreviousDay").hide();
		var prevDate = new Date(dateFormatterForNewDate(data.prevBusDateStats.prevBusinessDate));
		$("#previousDayCustomFormat").find(".previousDateDay").text(prevDate.getDate());
		$("#previousDayCustomFormat").find(".previousDateMonth").text(monthNames[prevDate.getMonth()]);
		$("#previousDayCustomFormat").find(".previousDateYear").text(prevDate.getFullYear());
		$("#previousDayCustomFormat").find(".previousDateWeek").text(weekday[prevDate.getDay()]);
	}
	else
	{
		$("#previousDayDateContainer").hide();
		$("#previousDayDateContainer").next().hide();
		$("#noPreviousDay").show();
	}
	if(!(data.prevPenDateStats.prevPendingDate=="No Previous Pending Business Date Found"))
	{	
		$("#previousMonthDateContainer").show();
		$("#previousMonthDateContainer").next().show();
		$("#noPreviousPending").hide();
		var prevMonthDate = new Date(dateFormatterForNewDate(data.prevPenDateStats.prevPendingDate));		
		$("#previousMonthCustomFormat").find(".previousDateDay").text(prevMonthDate.getDate());
		$("#previousMonthCustomFormat").find(".previousDateMonth").text(monthNames[prevMonthDate.getMonth()]);
		$("#previousMonthCustomFormat").find(".previousDateYear").text(prevMonthDate.getFullYear());
		$("#previousMonthCustomFormat").find(".previousDateWeek").text(weekday[prevMonthDate.getDay()]);
	}
	else
	{
		$("#previousMonthDateContainer").hide();
		$("#previousMonthDateContainer").next().hide();
		$("#noPreviousPending").show();
	}
		
	//generating flow and freq blocks.
	$("#applicableFlowResults").empty();
	$("#applicableFreqResults").empty();
	generateFlowTypes(data.chosenBusDateStats.applicableFlowTypes);
	generateFreqTypes(data.chosenBusDateStats.applicableFreqTypes);
	
	//cbd task summary value  update.
	var totalTasks = Number(data.chosenBusDateStats.totalTasks);
	var plannedCompletionTasks = Number(data.chosenBusDateStats.plannedCompletionTasks);
	var notDueYet = Number(data.chosenBusDateStats.notDueYet);
	var completedTask = Number(data.chosenBusDateStats.completedTasks);
	var completedPercCbd  = 0;
	var plannedPercCbd = 0;
	var failedTasks = Number(data.chosenBusDateStats.failedTasks);
	var overDueTasks = Number(data.chosenBusDateStats.overDueTasks);
	var pendingDueTasks = Number(data.chosenBusDateStats.pendingDueTasks);
	if (totalTasks!=0){
		completedPercCbd= (completedTask/totalTasks)*100 ;
		 plannedPercCbd=(plannedCompletionTasks/totalTasks)*100 ;
	
	}
	if(failedTasks>0)
		$("#failureAlertParent").show();
	else
		$("#failureAlertParent").hide();
	
	if(plannedCompletionTasks>0&&completedTask==0&&failedTasks==0&&overDueTasks==0)
		$("#ProgreeStatusText").text("Not Started").addClass("completed");
	else if(plannedCompletionTasks>0&&completedTask!=totalTasks&&failedTasks>=0&&overDueTasks>=0)
		$("#ProgreeStatusText").text("In Progress(Delayed)").removeClass("completed").addClass("overdue");
	else if(plannedCompletionTasks>0&&(completedTask>0&&completedTask<plannedCompletionTasks)&&(overDueTasks==0)&&(failedTasks==0))
		$("#ProgreeStatusText").text("In Progress").removeClass("completed").addClass("inProgress");
	else if(plannedCompletionTasks==completedTask &&plannedCompletionTasks==0 &&totalTasks>0 )
		$("#ProgreeStatusText").text("Not Started").removeClass("inProgress").removeClass("overdue").addClass("completed");
	else if(plannedCompletionTasks==completedTask)
		$("#ProgreeStatusText").text("Completed").removeClass("inProgress").removeClass("overdue").addClass("completed");
	else 
		$("#ProgreeStatusText").text("In Progress(Delayed)").removeClass("completed").addClass("overdue");
	$("#totalTask").text(totalTasks);
	$("#plannedCompletionTask").text(plannedCompletionTasks);
	$("#notYetTask").text(notDueYet);
	$("#cbdCompletedNum").text(completedTask);
	$("#cbdFailedNum").text(failedTasks);
	$("#cbdOverdueNum").text(overDueTasks);
	$("#cbdPendingNum").text(pendingDueTasks);

	$("#cbdChart").radialMultiProgress("init", {
	  'fill': 15,
	  'font-size': 15,
	  'data': [
		{'color': "#00b0f0" },
		{'color': "#92d050"},
	  ]
	}).radialMultiProgress("to", {
		  "index": 0, 'perc':plannedPercCbd 
		}).radialMultiProgress("to", {
		  "index": 1, 'perc': completedPercCbd
		});
		
	//cbd task summary ends here.

	
	var prevtotalTasks = Number(data.prevBusDateStats.totalTasks);
	var prevplannedCompletionTasks = Number(data.prevBusDateStats.plannedCompletionTasks);
	var prevnotDueYet = Number(data.prevBusDateStats.notDueYet);
	var prevcompletedTask = Number(data.prevBusDateStats.completedTasks);
	var prevcompletedPercCbd  = 0;
	var prevplannedPercCbd = 0;
	var prevfailedTasks = Number(data.prevBusDateStats.failedTasks);
	var prevoverDueTasks = Number(data.prevBusDateStats.overDueTasks);
	var prevpendingDueTasks = Number(data.prevBusDateStats.pendingDueTasks);

	if (prevtotalTasks!=0){
		prevcompletedPercCbd= (prevcompletedTask/prevtotalTasks)*100 ;
		 prevplannedPercCbd=(prevplannedCompletionTasks/prevtotalTasks)*100 ;
	
	}
	
	if(prevplannedCompletionTasks>0&&prevcompletedTask==0&&prevfailedTasks==0&&prevoverDueTasks==0)
		$("#previousDayStatus").text("Not Started").addClass("completed");
	else if(prevplannedCompletionTasks>0&&prevcompletedTask!=prevtotalTasks&&prevfailedTasks>=0&&prevoverDueTasks>=0)
		$("#previousDayStatus").text("In Progress(Delayed)").removeClass("completed").addClass("overdue");
	else if(prevplannedCompletionTasks>0&&(prevcompletedTask>0&&prevcompletedTask<prevplannedCompletionTasks)&&(prevoverDueTasks==0)&&(prevfailedTasks==0))
		$("#ProgreeStatusText").text("In Progress").removeClass("completed").addClass("inProgress");
	else if(prevplannedCompletionTasks==prevcompletedTask &&prevplannedCompletionTasks==0 &&prevtotalTasks>0 )
		$("#previousDayStatus").text("Not Started").removeClass("inProgress").removeClass("overdue").addClass("completed");
	else if(prevplannedCompletionTasks==prevcompletedTask)
		$("#previousDayStatus").text("Completed").removeClass("inProgress").removeClass("overdue").addClass("completed");
	else 
		$("#previousDayStatus").text("In Progress(Delayed)").removeClass("completed").addClass("overdue");
		$("#previousDayChart").radialMultiProgress("init", {
	  'fill': 10,
	  'font-size': 12,
	  'data': [
		{'color': "#00b0f0" },
		{'color': "#92d050"},
	  ]
	}).radialMultiProgress("to", {
		  "index": 0, 'perc':prevplannedPercCbd 
		}).radialMultiProgress("to", {
		  "index": 1, 'perc': prevcompletedPercCbd
		});
		
	var prevPendingTasks = Number(data.prevPenDateStats.totalTasks);
	var prevPendingplannedCompletionTasks1 = Number(data.prevPenDateStats.plannedCompletionTasks);
	var prevPendingnotDueYet1 = Number(data.prevPenDateStats.notDueYet);
	var prevPendingcompletedTask = Number(data.prevPenDateStats.completedTasks);
	var prevcompletedPercPending  = 0;
	var prevplannedPercPending = 0;
	var prevfailedTasksPend = Number(data.prevPenDateStats.failedTasks);
	var prevoverDueTasksPending = Number(data.prevPenDateStats.overDueTasks);
	var prevpendingDueTasks1 = Number(data.prevPenDateStats.pendingDueTasks);

	if (prevPendingTasks!=0){
		prevcompletedPercPending= (prevPendingcompletedTask/prevPendingTasks)*100 ;
		 prevplannedPercPending=(prevPendingplannedCompletionTasks1/prevPendingTasks)*100 ;
	
	}
	if(prevPendingplannedCompletionTasks1>0&&prevPendingcompletedTask==0&&prevfailedTasksPend==0&&prevoverDueTasksPending==0)
		$("#previousMonthStatus").text("Not Started");
	else if(prevPendingplannedCompletionTasks1>0&&prevPendingcompletedTask!=prevPendingTasks&&prevfailedTasksPend>=0&&prevoverDueTasksPending>=0)
		$("#previousMonthStatus").text("In Progress(Delayed)").removeClass("completed").addClass("inProgress");
	else if(prevPendingplannedCompletionTasks1>0&&(prevPendingcompletedTask>0&&prevPendingcompletedTask<prevPendingplannedCompletionTasks1)&&(prevoverDueTasksPending==0)&&(prevfailedTasksPend==0))
		$("#ProgreeStatusText").text("In Progress").removeClass("completed").addClass("inProgress");
	else if(prevPendingplannedCompletionTasks1==prevPendingcompletedTask &&prevPendingplannedCompletionTasks1==0 &&prevPendingTasks>0 )
		$("#previousMonthStatus").text("Not Started").removeClass("inProgress").removeClass("overdue").addClass("completed");
	else if(prevPendingplannedCompletionTasks1==prevPendingcompletedTask)
		$("#previousMonthStatus").text("Completed").removeClass("overdue").removeClass("inProgress").addClass("completed");		
	else 
		$("#previousMonthStatus").text("In Progress(Delayed)").removeClass("overdue").removeClass("completed").addClass("inProgress");
	

	$("#previousMonthChart").radialMultiProgress("init", {
	  'fill': 10,
	  'font-size': 12,
	  'data': [
		{'color': "#00b0f0" },
		{'color': "#92d050"},
	  ]
	}).radialMultiProgress("to", {
		  "index": 0, 'perc':prevplannedPercPending 
		}).radialMultiProgress("to", {
		  "index": 1, 'perc': prevcompletedPercPending
		});
		
			
		
		
}


function domChangesForLoadActualProgressDetails(data){
	for(var i = 0;i<data.length;i++){
		//calcuations for details slab
		var changedDate = new Date((data[i].progressDate));
		var totalTaskForGiverBusinessDate = new Date((data[i].totalTaskCount));
		var date =(changedDate.getDate());
		var month = (monthNames[changedDate.getMonth()]);
		var week = (weekDayInShort[changedDate.getDay()]);
		
		var plannedValue = data[i].plannedCount;
		var plannedCount = data[i].plannedCount;
		if(plannedValue ==""){
			plannedValue = -1;
		}
		
		var completedValue = data[i].completedCount;
		var completedCount = data[i].completedCount;
		if(completedValue ==""){
			completedValue = -1;
		}
		var sigmaPlanned = plannedCount;
		var sigmaCompleted = completedCount;
		
		var previousSigmaPlanned = 0;
		var previousSigmaCompleted =0; 
		if(i!=0){
			var prevSigmaPRec = $("div").find("[progressActualBlockNumber='" + (i-1) + "']").find(".plannedNumR3").text().split("(")[0];
			if(prevSigmaPRec == '-'){
				prevSigmaPRec=''
			}
			previousSigmaPlanned = Number(prevSigmaPRec);
			sigmaPlanned = previousSigmaPlanned+sigmaPlanned;
			
			var prevSigmaCRec = $("div").find("[progressActualBlockNumber='" + (i-1) + "']").find(".completedNumR3").text().split("(")[0];
			if(prevSigmaCRec == '-'){
				prevSigmaCRec=''
			}
			previousSigmaCompleted = Number(prevSigmaCRec);
			sigmaCompleted = previousSigmaCompleted+sigmaCompleted;
			
		}
		
		var sigmaPlannedPerc = "("+((sigmaPlanned/totalTaskForGiverBusinessDate)*100).toFixed(2)+"%)";
		var sigmaCompletedPerc ="";
		var postCurrentDate = "N";
		//check for null data.
		if (completedCount==""){
			sigmaCompletedPerc = '';
			sigmaCompleted = '-';
			postCurrentDate = "Y"
		}else{
		 sigmaCompletedPerc = "("+((sigmaCompleted/totalTaskForGiverBusinessDate)*100).toFixed(2)+"%)";
		}
		//check for sysdate and plan match
		var isDateMatch = "N";
		if(moment(new Date()).isSame(moment(changedDate),"days")){
			isDateMatch = "Y";
		}
		createActualProgressSlabs(i,date,month,week,plannedValue,plannedCount,completedValue,completedCount,sigmaPlanned,sigmaPlannedPerc,sigmaCompleted,sigmaCompletedPerc,isDateMatch,postCurrentDate)
	}
	//fill out for empty space
	if(data.length<6){
		appendDummyDivForDetails(6-data.length);
	}

		
}


function domChangesForDetailsForSourceSystems(data){
		
	//src entity details 
	var totalSE = data.sourceEntityDetails.total;
	var notStartedSE = data.sourceEntityDetails.notStarted;
	var partiallyCompletedSE = data.sourceEntityDetails.partiallyCompleted;
	var completedSE = data.sourceEntityDetails.completed;
	
	$("#srcEntities").find(".completedForStage").text(completedSE);
	$("#srcEntities").find(".partiallyForStage").text(partiallyCompletedSE);
	$("#srcEntities").find(".pendingForStage").text(notStartedSE);
	$("#srcEntities").find(".srcEntitiesLegend").find("b").text(totalSE);
	
	$("#srcEntities2").find(".completedForStage").text(completedSE);
	$("#srcEntities2").find(".partiallyForStage").text(partiallyCompletedSE);
	$("#srcEntities2").find(".pendingForStage").text(notStartedSE);
	$("#srcEntities2").find(".srcEntitiesLegend").find("b").text(totalSE);
	
	//src task details
	var totalST = data.sourceTaskDetails.total;
	var overDueST = data.sourceTaskDetails.overDue;
	var notStartedST = data.sourceTaskDetails.notStarted;
	var completedST = data.sourceTaskDetails.completed;
	var failedST = data.sourceTaskDetails.failed;
	
	$("#srcDataLoadTasks").find(".completedForStage").text(completedST);
	$("#srcDataLoadTasks").find(".failedForStage").text(failedST);
	$("#srcDataLoadTasks").find(".pendingForStage").text(notStartedST);
	$("#srcDataLoadTasks").find(".overdueForStage").text(overDueST);
	$("#srcDataLoadTasks").find(".srcDataLoadTaskLegend").find("b").text(totalST);
	
	$("#srcDataLoadTasks2").find(".completedForStage").text(completedST);
	$("#srcDataLoadTasks2").find(".failedForStage").text(failedST);
	$("#srcDataLoadTasks2").find(".pendingForStage").text(notStartedST);
	$("#srcDataLoadTasks2").find(".overdueForStage").text(overDueST);
	$("#srcDataLoadTasks2").find(".srcDataLoadTaskLegend").find("b").text(totalST);
	
	//src systems
	var totalSS = data.sourceSystemDetails.total;
	var notStartedSS = data.sourceSystemDetails.notStarted;
	var partiallyCompletedSS = data.sourceSystemDetails.partiallyCompleted;
	var completedSS = data.sourceSystemDetails.completed;
	
	$("#srcSystems").find(".completedForStage").text(completedSS);
	$("#srcSystems").find(".partiallyForStage").text(partiallyCompletedSS);
	$("#srcSystems").find(".pendingForStage").text(notStartedSS);
	$("#srcSystems").find(".srcSystemsLegend").find("b").text(totalSS);
	$("#srcSystems2").find(".completedForStage").text(completedSS);
	$("#srcSystems2").find(".partiallyForStage").text(partiallyCompletedSS);
	$("#srcSystems2").find(".pendingForStage").text(notStartedSS);
	$("#srcSystems2").find(".srcSystemsLegend").find("b").text(totalSS);
	
}


function domChangesForDetailsForDataRepository(data){
	//data repository entity details 
	var totalRE = data.dataRepoEntityDetails.total;
	var notStartedRE = data.dataRepoEntityDetails.notStarted;
	var partiallyCompletedRE = data.dataRepoEntityDetails.partiallyCompleted;
	var completedSE = data.dataRepoEntityDetails.completed;
	
	$("#dataRepoEntities").find(".completedForStage").text(completedSE);
	$("#dataRepoEntities").find(".partiallyForStage").text(partiallyCompletedRE);
	$("#dataRepoEntities").find(".pendingForStage").text(notStartedRE);
	
	$("#dataRepoEntities2").find(".completedForStage").text(completedSE);
	$("#dataRepoEntities2").find(".partiallyForStage").text(partiallyCompletedRE);
	$("#dataRepoEntities2").find(".pendingForStage").text(notStartedRE);
	
	$("#dataRepoEntities").find(".dataRepoEntitiesLegend").find("b").text(totalRE);
	$("#dataRepoEntities2").find(".dataRepoEntitiesLegend").find("b").text(totalRE);
	
	//data repository task details
	var totalRT = data.dataRepoTaskDetails.total;
	var overDueRT = data.dataRepoTaskDetails.overDue;
	var notStartedRT = data.dataRepoTaskDetails.notStarted;
	var completedRT = data.dataRepoTaskDetails.completed;
	var failedRT = data.dataRepoTaskDetails.failed;
	var plannedCompletionRT = data.dataRepoTaskDetails.plannedCompletion;
	var pendingDueTodayRT = data.dataRepoTaskDetails.pendingDueToday;
	
	$("#dataRepoDataLoadTasks").find(".completedForStage").text(completedRT);
	$("#dataRepoDataLoadTasks").find(".plannedForStage").text(plannedCompletionRT);
	$("#dataRepoDataLoadTasks").find(".pendingForStage").text(pendingDueTodayRT);
	$("#dataRepoDataLoadTasks").find(".failedForStage").text(failedRT);
	$("#dataRepoDataLoadTasks").find(".overdueForStage").text(overDueRT);
	$("#dataRepoDataLoadTasks").find(".notYetForStage").text(notStartedRT);
	
	$("#dataRepoDataLoadTasks2").find(".completedForStage").text(completedRT);
	$("#dataRepoDataLoadTasks2").find(".plannedForStage").text(plannedCompletionRT);
	$("#dataRepoDataLoadTasks2").find(".pendingForStage").text(pendingDueTodayRT);
	$("#dataRepoDataLoadTasks2").find(".failedForStage").text(failedRT);
	$("#dataRepoDataLoadTasks2").find(".overdueForStage").text(overDueRT);
	$("#dataRepoDataLoadTasks2").find(".notYetForStage").text(notStartedRT);
	$("#dataRepoDataLoadTasks2").find(".dataRepoDataLoadTaskLegend").find("b").text(totalRT);
	$("#dataRepoDataLoadTasks").find(".dataRepoDataLoadTaskLegend").find("b").text(totalRT);
	
	
	//individual data repository systems
	$("#RepoResultsCont").empty();
	var repoDetailsInd = data.repoListDetails
	var totalDataRepos = repoDetailsInd.length
	$(".dataRepoSystemsLegend").find("b").text(totalDataRepos);
	for(var i = 0;i<totalDataRepos;i++){
		generateIndividualDataRepoSlab(repoDetailsInd[i]);
	}
		
}

function domChangesForDetailsForDataConsumers(data){
	$("#tgtCont").empty();
	for(var i = 0;i<data.length;i++){
		generateLayoutForDataConsumers(data[i]);
	}
}

function templateForFirstColumn(name,desc){
	return "<div title = '"+name+"' class = 'firstColumnName'>"+ name+"</div>"+
			"<div title = '"+desc+"' class = 'firstColumnDesc'>"+ desc+"</div>";
}
function templateForCompletedVsTotal(value1,value2){
	return "<div title = "+value1+"/"+value2+" style  ='font-weight:600;text-align: center;font-size:12px;' ><span style  = 'color:#92d050;'>"+ value1+"</span><span>/</span><span>"+value2+"</span></div>";
}

function templateForlineage(entityName,entityType,entityOwner,reportId){
	if(undefined==reportId||null==reportId){
		return "<div title = 'Show Lineage for This Entity' entityName = '"+entityName+"' entityType = '"+entityType+"' entityOwner = '"+entityOwner+"' onclick = 'showLineageForTheCurrentEntity(this)' class = 'lineageIcon'></div>"
	}else{
		return "<div title = 'Show Lineage for This Entity' entityName = '"+entityName+" ["+reportId+"]"+"' entityType = '"+entityType+"' entityOwner = '"+entityOwner+"' onclick = 'showLineageForTheCurrentEntity(this)' class = 'lineageIcon'></div>"
	}
	
	
}
function sourceSystemOnClick(elem){
	if($(elem).hasClass("arrowHeaderSSClicked"))
	{
		$(elem).removeClass("arrowHeaderSSClicked");
		$("#stagSummaryDetailContainer").show();
		$("#sourceSystemClicked").hide();
		$("#drClicked").hide();	
	}	
	else{
		$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
		
		$("#stagSummaryDetailContainer").hide();
		$(elem).addClass("arrowHeaderSSClicked");
		$("#drClicked").hide();
		$("#sourceSystemsSummary").click();
		
	}
	
}
function dataLoadTaskDetailsFunction(elem)
{
	$(".selectedTab").removeClass("selectedTab");
	$(elem).addClass("selectedTab");
	$("#srcSystems2").hide();
	$("#srcDataLoadTasks2").show();
	$("#srcEntities2").hide();
	if($("#grid1").data("kendoGrid")!=undefined){
		$("#grid1").data("kendoGrid").destroy(); // destroy the Grid
		$("#grid1").empty();
	}
	
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV,
			 tabIndicator:"SOURCETASK"
			 }
	fetchData(query,"/dldwebapplication/getdetailsforsourcesystemgrid").then(
	
	function(dataForGrid){
			 
	$(".overlay").css("display","block");
	setTimeout(
	function(){	
	$("#grid1").kendoGrid({
						toolbar: ["excel"], excel: { fileName: "DataLoadTaskDetails.xlsx"} ,
                        dataSource: {
                            type: "json",
                            data:dataForGrid,
                           
                            schema: {
								data:"data",
                       model: {
                           fields: {
                               sourceOwner: { type: "string" },
                               taskName: { type: "string" },
                               sourceEntity: { type: "string" },
                               flowType: { type: "string" },
                               dueDate: { type: "string" },
                               completionDate: { type: "string" },
							   runCount: { type: "number" },
							   status: { type: "string" },
							   taskDesc: { type: "string" },
                              }
                            }
                         },
                            // pageSize: 10
							aggregate: [ { field: "sourceOwner", aggregate: "count" },
                                          { field: "taskName", aggregate: "count" },
                                          { field: "status", aggregate: "count" },
                                          { field: "runCount", aggregate: "count" },
                                          { field: "sourceEntity", aggregate: "count" }]
                        },
                        filterable: true,
                        groupable: true,
                        sortable: true,
                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "taskName",
                            title: "Task Name",
							template: '#=templateForFirstColumn(taskName,taskDesc)#',
							groupHeaderTemplate: "Task Name : #=value# (#= count#)" ,
							width:300
                        }, {
                            field: "sourceEntity",
                            title: "Source Entities",
							aggregates: ["count"],
							template: '#=templateForHover(sourceEntity)#',
							groupHeaderTemplate: "Source Entities : #=value# (#= count#)" ,
							width:200
                        },{
                            field: "flowType",
							aggregates: ["count"],
							groupHeaderTemplate: "Flow Type : #=value# (#= count#)" ,
							template: '#=templateForHover(flowType)#',
                            title: "Flow Type",width:200
                        },{
                            field: "dueDate",
							aggregates: ["count"],
							groupHeaderTemplate: "Due Date : #=value# (#= count#)" ,
                            title: "Due Date",width:200,filterable: false,
								sortable: false,
							template: '#=templateForDateColumn(dueDate)#',
                        },{
                            field: "completionDate",
							aggregates: ["count"],
							groupHeaderTemplate: "Completion Date : #=value# (#= count#)" ,
                            title: "Completion Date",width:200,filterable: false,
								sortable: false,
							template: '#=templateForDateColumn(completionDate)#',
                        },{
                            field: "runCount",
							aggregates: ["count"],
							groupHeaderTemplate: "Run Count :#=value# (#= count#)" ,
                            title: "Run Count",width:200
                        },{
                            field: "sourceOwner",
                            title: "Source System",
							aggregates: ["count"],
							groupHeaderTemplate: "Source System : #=value# (#= count#)" ,
							template: '#=templateForHover(sourceOwner)#',
							width:200,filterable: { multi: true }
                        },{
                            field: "status",
							aggregates: ["count"],
							groupHeaderTemplate: "Task Status : #=value# (#= count#)" ,
							template: '#=templateForHover(status)#',
                            title: "Task Status",width:200,filterable: { multi: true }
                        }],dataBound: dataBound
                    });
					$(".k-grouping-header").append('<div class="k-group-indicator" data-field="sourceOwner" data-title="Source System" data-dir="desc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-s">(sorted descending)</span>Source System</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div><div class="k-group-indicator" data-field="status" data-title="Task Status" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Task Status</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div>');
					$(".k-link").click();
					if(sourceSystemTabClickFromTile=="Y"){
						$("#grid1").data("kendoGrid").dataSource.filter({
							field: "status",
							operator: "eq",
							value: statusToNavigate.toUpperCase()
						});
						sourceSystemTabClickFromTile="N";
					}
					$(".overlay").css("display","none");
	},0);	
					
					
	})
}
function templateForDateColumn(name){
	if(name!="")
		return kendo.toString(kendo.parseDate(name, 'yyyy-MM-dd'), 'dd MMM yyyy');
	else
		return "";
}
function sourceSystemTabClick(elem)
{
	if($("#grid1").data("kendoGrid")!=undefined)
	{	
		$("#grid1").data("kendoGrid").destroy(); // destroy the Grid
		$("#grid1").empty();
	}
	if($("#grid4").data("kendoGrid")!=undefined)
	{	
		$("#grid4").data("kendoGrid").destroy(); // destroy the Grid
		$("#grid4").empty();
	}
		
	$("#srcSystems2").show();
	$("#srcEntities2").hide();
	$("#srcDataLoadTasks2").hide();
	$(".selectedTab").removeClass("selectedTab");
	$(elem).addClass("selectedTab");
	$("#sourceSystemClicked").show();
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV,
			 tabIndicator:"SOURCESYSTEM"
			 };
			 
	fetchData(query,"/dldwebapplication/getdetailsforsourcesystemgrid").then(
	
	function(dataForGrid){		 
	$(".overlay").css("display","block");
	setTimeout(
	function(){
	$("#grid1").kendoGrid({
						toolbar: ["excel"], excel: { fileName: "Source System Details.xlsx"} ,
                        dataSource: {
                            type: "json",
                            data:dataForGrid,
                           
                            schema: {
								data:"data",
								
                        
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               sourceName: { type: "string" },
                               totalTaskCount: { type: "number" },
							   completedTaskCount:{type: "number"},
							   entityTotalCount: { type: "number" },
							   entityCompletedCount: { type: "number" },                               
                               ContactDetails: { type: "string" },
                               status: { type: "string" },
                               sourceDesc: { type: "string" }
                              }
                            }
                         },
						 aggregate: [ { field: "sourceName", aggregate: "count" },
                                          { field: "totalTaskCount", aggregate: "count" },
                                          
                                          { field: "entityTotalCount", aggregate: "count" },
                                          { field: "entityCompletedCount", aggregate: "count" },
										  { field: "ContactDetails", aggregate: "count" },
										  { field: "status", aggregate: "count" },
										  { field: "sourceDesc", aggregate: "count" }
									]
                            // pageSize: 10
                        },
                        filterable: true,
                        groupable: true,
                        sortable: true,
                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "sourceName",
                            title: "Source System",
							template: '#=templateForFirstColumn(sourceName,sourceDesc)#',
							aggregates: ["count"],
							groupHeaderTemplate: "Source System : #=value# (#= count#)", 
							width:300
                        }, {
                            field: "totalTaskCount",
                            title: "Task Summary",
							template: '#=templateForCompletedVsTotal(completedTaskCount,totalTaskCount)#',
							groupable: false,filterable:false,sortable:false,	
							width:100
                        }, {
                            field: "entityCompletedCount",
                            title: "Enitity Summary",
							template: '#=templateForCompletedVsTotal(entityCompletedCount,entityTotalCount)#',
							aggregates: ["count"],
							groupable: false,filterable:false,sortable:false,
							groupHeaderTemplate: "Contact Details : #=value# (#= count#)", 
							width:100
                        }, {
                            field: "ContactDetails",
                            title: "Contact Details",
							aggregates: ["count"],
							template: '#=templateForHover(ContactDetails)#',
							groupHeaderTemplate: "Contact Details : #=value# (#= count#)", 
							width:200
                        },{
                            field: "status",
                            title: "Status",width:200,filterable: { multi: true },
							aggregates: ["count"],
							template: '#=templateForHover(status)#',
							groupHeaderTemplate: "#=value# (#= count# )" 
                        }],dataBound: dataBound
                    });
					
					
					$(".k-grouping-header").append('<div class="k-group-indicator" data-field="status" data-title="Status" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Status</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div>');
					$(".k-link").click();
					if(sourceSystemTabClickFromTile=="Y"){
						$("#grid1").data("kendoGrid").dataSource.filter({
							field: "status",
							operator: "eq",
							value: statusToNavigate.toUpperCase()
						});
						sourceSystemTabClickFromTile="N";
					}
					$(".overlay").css("display","none");
	},0);
	});
	
	
}
function sourceEntityDetailsFunction(elem)
{
	$(".selectedTab").removeClass("selectedTab");
	$(elem).addClass("selectedTab");
	$("#srcSystems2").hide();
	$("#srcDataLoadTasks2").hide();
	$("#srcEntities2").show();
	if($("#grid1").data("kendoGrid")!=undefined)
		$("#grid1").data("kendoGrid").destroy(); // destroy the Grid
    $("#grid1").empty();
	
		if($("#grid4").data("kendoGrid")!=undefined)
		$("#grid4").data("kendoGrid").destroy(); // destroy the Grid
    
	$("#grid4").empty();
	
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV,
			 tabIndicator:"SOURCEENTITY"
			 };
			 
	fetchData(query,"/dldwebapplication/getdetailsforsourcesystemgrid").then(
	
	function(dataForGrid){		 
	$(".overlay").css("display","block");
	setTimeout(
	function(){
	$("#grid1").kendoGrid({
						toolbar: ["excel"], excel: { fileName: "Source Entity Details.xlsx"} ,
                        dataSource: {
                            type: "json",
                            data:dataForGrid,
                           
                            schema: {
								data:"data",
								total: function (result) {
                            result = result.data || result;
                            return result.length;
                        },
						
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               
                               totalTaskCount: { type: "number" },
							   completedTaskCount: { type: "number" },
							   sourceOwner:{ type: "string" },
                               entityName: { type: "string" },
                               entityType: { type: "string" },
                               ContactDetails: { type: "string" },
                               entityDesc: { type: "string" },
							   Status:{type: "string"}
                              }
                            }
                         },
						 aggregate: [ { field: "sourceOwner", aggregate: "count" },
                                          { field: "entityName", aggregate: "count" },
                                          { field: "entityType", aggregate: "count" },
                                          { field: "entityCompletedCount", aggregate: "count" },
										  { field: "ContactDetails", aggregate: "count" },
										  { field: "status", aggregate: "count" },
										  
									]
                            // pageSize: 10
                        },
                        filterable: true,
                        groupable: true,
                        sortable: true,
                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "entityName",
                            title: "Entity Name",
							template: '#=templateForFirstColumn(entityName,entityDesc)#',
							width:300
                        },{
                            field: "entityType",
							template: '#=templateForHover(entityType)#',
                            title: "Entity Type",width:200
                        }, {
                            field: "taskSummary",
                            title: "Task Summary",
							template: '#=templateForCompletedVsTotal(completedTaskCount,totalTaskCount)#',
							width:200
                        }, {
                            field: "ContactDetails",
                            title: "Contact Details",
							template: '#=templateForHover(ContactDetails)#',
							width:200
                        }, {
                            field: "sourceOwner",
                            title: "Source System",
							width:200,
							template: '#=templateForHover(sourceOwner)#',
							aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)",filterable: { multi: true }
                        },{
                            field: "status",
                            title: "Entity Status",width:200,filterable: { multi: true },
							aggregates: ["count"],
							template: '#=templateForHover(status)#',
							groupHeaderTemplate: "#=value# (#= count#)" 
                        },{
                            field: "",
                            title: "",width:40,
							template: '#=templateForlineage(entityName,"sourceSystem",sourceOwner)#'
                        }],dataBound: dataBound
                    });
					
					
					$(".k-grouping-header").append('<div class="k-group-indicator" data-field="sourceOwner" data-title="Source System" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Source System</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div><div class="k-group-indicator" data-field="status" data-title="Entity Status" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Entity Status</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div>');
					$(".k-link").click();
					if(sourceSystemTabClickFromTile=="Y"){
						$("#grid1").data("kendoGrid").dataSource.filter({
							field: "status",
							operator: "eq",
							value: statusToNavigate.toUpperCase()
						});
						sourceSystemTabClickFromTile="N";
					}
					$(".overlay").css("display","none");
				},0);	
	});
	
}
function drOnClick(elem)
{
	if($(elem).hasClass("arrowHeaderSSClicked"))
	{
		$(elem).removeClass("arrowHeaderSSClicked");
		$("#stagSummaryDetailContainer").show();
		$("#sourceSystemClicked").hide();
		$("#drClicked").hide();	
	}	
	else{
		$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
		$("#stagSummaryDetailContainer").hide();
		$("#sourceSystemClicked").hide();
		$(elem).addClass("arrowHeaderSSClicked");
		$("#drDataLoadTask").click();
		
	}
	
}
function drDataLoadTaskClick(elem){
	
	$(".selectedTab").removeClass("selectedTab");
	$(elem).addClass("selectedTab");
	$("#drClicked").show();
	if($("#grid2").data("kendoGrid")!=undefined)
		$("#grid2").data("kendoGrid").destroy(); // destroy the Grid
	
	if($("#grid4").data("kendoGrid")!=undefined)
		$("#grid4").data("kendoGrid").destroy(); // destroy the Grid
    
	$("#grid4").empty();
	$("#grid2").empty();
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV,
			 tabIndicator:"DATAREPOTASK"
			 };
	$("#dataRepoDataLoadTasks2").show();
	$("#dataRepoEntities2").hide();
	fetchData(query,"/dldwebapplication/getdetailsfordatarepositorygrid").then(
	
	function(dataforgrid){
	$(".overlay").css("display","block");
		
	$("#grid2").kendoGrid({
						toolbar: ["excel"], excel: { fileName: "Data Repository Task Details.xlsx"} ,
                        dataSource: {
                            type: "json",
                            data:dataforgrid,
                           
                            schema: {
								data:"data", 
								total: function (result) {
                            result = result.data || result;
                            return result.length;
                        },
						
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
							   source:{type: "string"},
                               sourceOwner: { type: "string" },
                               Status: { type: "string" },
                               taskName: { type: "string" },
                               taskType: { type: "string" },
                               targetEntity: { type: "string" },
                               sourceEntity: { type: "string" },
							   entityDataRepo:{ type: "string" },
							   flowType: { type: "string" },
							   frequencyType: { type: "string" },
							   dueDate: { type: "string" },
							   loadStartDate: { type: "string" },
							   loadEndDate: { type: "string" },
							   taskTechnicalName: { type: "string" },
							   RunDetails: { type: "string" },
							   runCount: { type: "number" },
                              }
                            }
                         },
						 aggregate: [ { field: "sourceOwner", aggregate: "count" },
                                          { field: "source", aggregate: "count" },
										  { field: "Status", aggregate: "count" }
										  
									]
                            // pageSize: 10
                        },
						detailTemplate: '<div style="width:1000px;" class="grid3"></div>',
  detailInit: function(e) {
	  
	  var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	  var query1={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 repoName:e.data.dataRepo,
			 taskName:e.data.taskName
			 }
	  
	  if(parseInt(e.data.runCount)==0)
		  $(".grid3").append("Task not run");
	  else
	  {
		  fetchData(query1,"/dldwebapplication/getsubgridtaskdetails").then(
	
	function(dataforgrid3){

  
    $(".grid3").kendoGrid({
                        dataSource: {
                            type: "json",
                            data:dataforgrid3,
                           
                            schema: {
								data:"data",
								total: function (result) {
                            result = result.data || result;
                            return result.length;
                        },
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               sequenceNumber: { type: "string" },
                               taskStartDateTime: { type: "string" },
							   taskEndDateTime: { type: "string" },
                               status: { type: "string" },
                               runDetails: { type: "string" }
                              }
                            }
                         },
                            // pageSize: 10
                        },

                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "sequenceNumber",
                            title: "Sequence",
							width:150	
							
                        },{
                            field: "taskStartDateTime",
                            title: "Task Start Date",
							width:150,
							template: '#=templateForDateColumnTime(taskStartDateTime)#'
                        },{
                            field: "taskEndDateTime",
                            title: "Task End Date",
							width:150,
							template: '#=templateForDateColumnTime(taskEndDateTime)#'
                        }, {
                            field: "status",
                            title: "Task Status",
							template: '#=templateForHover(status)#',
							width:150,filterable: { multi: true }
                        }, {
                            field: "runDetails",
                            title: "Run Details",
							template: '#=templateForHover(runDetails)#',
							width:300
                        }],dataBound: dataBound
                    });
	})
	  }
    }
  ,
                        filterable: true,
                        groupable: true,
                        sortable: true,
                        pageable: false,//turn true for pagination styling done..
                        columns: [ {
                            field: "taskName",
                            title: "Task Name",
							width:300,
							template: '#=templateForFirstColumn(taskName,taskDesc)#'
                        }, {
                            field: "taskType",
                            title: "Task Type",
							template: '#=templateForHover(taskType)#',
							width:200
                        },{
                            field: "targetEntity",
							template: '#=templateForHover(targetEntity)#',
                            title: "Target Entity",width:200
                        },{
                            field: "sourceEntity",
                            template: '#=templateForHover(sourceEntity)#',
							title: "Source Entity",width:200
                        },{
                            field: "source",
							template: '#=templateForHover(source)#',
                            title: "Source ",width:200
                        },
						
						{
                            field: "flowType",
							template: '#=templateForHover(flowType)#',
                            title: "Flow Type",width:200
                        },{
                            field: "frequencyType",
							template: '#=templateForHover(frequencyType)#',
                            title: "Frequency",width:200
                        },{
                            field: "dueDate",
                            title: "Due Date",width:200,
							template: '#=templateForDateColumn(dueDate)#',filterable: false,
								sortable: false,
                        },{
                            field: "loadStartDate",
                            title: "Load Start Date",width:200,
							template: '#=templateForDateColumnTime(loadStartDate)#',filterable: false,
								sortable: false,
                        },{
                            field: "loadEndDate",
                            title: "Load End Date",width:200,
							template: '#=templateForDateColumnTime(loadEndDate)#',filterable: false,
								sortable: false,
                        },{
                            field: "taskTechnicalName",
							template: '#=templateForHover(taskTechnicalName)#',
                            title: "Task Technical Details",width:200
                        },{
                            field: "runDetails",
							template: '#=templateForHover(runDetails)#',
                            title: "Run Details",width:200
                        },{
                            field: "runCount",
							template: '#=templateForHover(runCount)#',
                            title: "Run Count",width:200
                        },{
                            field: "sourceOwner",
                            title: "Data Repository",
							width:200,
							aggregates: ["count"],
							template: '#=templateForHover(sourceOwner)#',
							groupHeaderTemplate: "#=value# (#= count#)", 
                        }, {
                            field: "status",
                            title: "Task Status",
							template: '#=templateForHover(status)#',
							width:200,
							aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)",filterable: { multi: true }
                        }],dataBound: dataBound
                    });
					
					//$(".k-grouping-header").append('<div class="k-group-indicator" data-field="sourceOwner" data-title="Data Repository" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Data Repository</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div><div class="k-group-indicator" data-field="status" data-title="Task Status" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Task Status</a></div>');
						
					//$(".k-link").click();
					
					//$(".k-grouping-header").append('<div class="k-group-indicator" data-field="source" data-title="Source " data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Source </a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div>');
					
					//$(".k-link").click();
					if(sourceSystemTabClickFromTile=="Y"){
						$("#grid2").data("kendoGrid").dataSource.filter({
							field: "status",
							operator: "eq",
							value: statusToNavigate.toUpperCase()
						});
						sourceSystemTabClickFromTile="N";
					}
					$(".overlay").css("display","none");
				
	});	
					
}


function drRepoEntityFunction(elem)
{
	$(".selectedTab").removeClass("selectedTab");
	$(elem).addClass("selectedTab");
	$("#dataRepoEntities2").show();
	$("#drClicked").show();
	$("#dataRepoDataLoadTasks2").hide();
	if($("#grid2").data("kendoGrid")!=undefined)
	$("#grid2").data("kendoGrid").destroy(); // destroy the Grid
		if($("#grid4").data("kendoGrid")!=undefined)
		$("#grid4").data("kendoGrid").destroy(); // destroy the Grid
    
	$("#grid4").empty();
    var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV,
			 tabIndicator:"DATAREPOENTITY"
			 };
	$("#grid2").empty();
	
	 fetchData(query,"/dldwebapplication/getdetailsfordatarepositorygrid").then(
	
	function(dataforgrid){
		
	$(".overlay").css("display","block");
	setTimeout(
	function(){	
	$("#grid2").kendoGrid({
						toolbar: ["excel"], excel: { fileName: "Data Repository Entity Details.xlsx"} ,
                        dataSource: {
                            type: "json",
                            data:dataforgrid,
                           
                            schema: {
								data:"data",
								total: function (result) {
                            result = result.data || result;
                            return result.length;
                        },
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               entityDataRepo: { type: "string" },
							   totalTaskCount: { type: "number" },
							   completedTaskCount: { type: "number" },
                               status: { type: "string" },
                               entityName: { type: "string" },
                               entityType: { type: "string" },
							   entityTechnicalName:{type: "string"},
							   entityDesc:{type: "string"}
                              }
                            }
                         },
						 
						 aggregate: [ 
                                          { field: "entityDataRepo", aggregate: "count" },
										  { field: "status", aggregate: "count" }
										  
									]
                            // pageSize: 10
                        },
                        filterable: true,
                        groupable: true,
                        sortable: true,
                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "entityName",
                            title: "Entity Name",
							template: '#=templateForFirstColumn(entityName,entityDesc)#',
							width:300
                        },{
                            field: "entityType",
							template: '#=templateForHover(entityType)#',
                            title: "Entity Type",width:200
                        }, {
                            field: "totalTaskCount",
                            title: "Task Summary",
							template: '#=templateForCompletedVsTotal(completedTaskCount,totalTaskCount)#',
							width:200
                        },{
                            field: "entityTechnicalName",
							template: '#=templateForHover(entityTechnicalName)#',
                            title: "Entity Techinical Name",width:200
                        },{
                            field: "entityDataRepo",
							template: '#=templateForHover(entityDataRepo)#',
                            title: "Data Repository",width:200,
							aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)", 
							
                        },{
                            field: "status",
							template: '#=templateForHover(status)#',
                            title: "Entity Status",width:200,
							aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)",filterable: { multi: true } 
							
                        },{
                            field: "",
                            title: "",width:40,
							template: '#=templateForlineage(entityName,"dataRepository",entityDataRepo)#'
                        }],dataBound: dataBound
                    });
					
					$(".k-grouping-header").append('<div class="k-group-indicator" data-field="entityDataRepo" data-title="Data Repository" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Data Repository</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div><div class="k-group-indicator" data-field="status" data-title="Entity Status" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Entity Status</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div>');
					$(".k-link").click();
					if(sourceSystemTabClickFromTile=="Y"){
						$("#grid2").data("kendoGrid").dataSource.filter({
							field: "status",
							operator: "eq",
							value: statusToNavigate.toUpperCase()
						});
						sourceSystemTabClickFromTile="N";
					}
					$(".overlay").css("display","none");
				},0);			
	});	
	
}
function drUnplannedTaskFunction(elem)
{
	$(".selectedTab").removeClass("selectedTab");
	$(elem).addClass("selectedTab");
	$("#dataRepoEntities2").show();
	$("#drClicked").show();
	$("#dataRepoDataLoadTasks2").hide();
	$("#dataRepoEntities2").hide();
	
	$("#grid2").data("kendoGrid").destroy(); // destroy the Grid
    	if($("#grid4").data("kendoGrid")!=undefined)
		$("#grid4").data("kendoGrid").destroy(); // destroy the Grid
    
	$("#grid4").empty();
	$("#grid2").empty();
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected
			 };
			 
	fetchData(query,"/dldwebapplication/getunplannedtaskdetails").then(
	
	function(dataforgrid){		
	$(".overlay").css("display","block");	
	setTimeout(
	function(){	
	$("#grid2").kendoGrid({
						toolbar: ["excel"], excel: { fileName: "Data Repository Unplanned Task Details.xlsx"} ,
                        dataSource: {
                            type: "json",
                            data:dataforgrid,
                           
                            schema: {
								data:"data",
								total: function (result) {
                            result = result.data || result;
                            return result.length;
                        },
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               taskName: { type: "string" },
                               flowType: { type: "string" },
                               taskStartDate: { type: "string" },
                               taskEndDate: { type: "string" },
                               taskTechnicalName: { type: "string" },
                               runDetails: { type: "string" },
							   runCount:{type: "Number"},
								repository:{ type: "string" },
								taskStatus:{ type: "string" }
                              }
                            }
                         },
						 aggregate: [ 
                                          { field: "repository", aggregate: "count" },
										  { field: "taskStatus", aggregate: "count" }
										  
									]
						 
						 
                            // pageSize: 10
                        },
						detailTemplate: '<div style="width:1000px;" class="grid3"></div>',
  detailInit: function(e) {
	  var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	  
	  
	  if(parseInt(e.data.runCount)==0)
		  $(".grid3").append("Task still not run")
	  else
	  {
	var query1={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 repoName:e.data.repository,
			 taskName:e.data.taskName
			 };
			 
			 fetchData(query1,"/dldwebapplication/getsubgridtaskdetails").then(
	
	function(dataforgrid2){
    $(".grid3").kendoGrid({
                        dataSource: {
                            type: "json",
                            data:dataforgrid2,
                           
                            schema: {
								data:"data",
								total: function (result) {
                            result = result.data || result;
                            return result.length;
                        },
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               sequenceNumber: { type: "string" },
                               taskStartDateTime: { type: "string" },
							   taskEndDateTime: { type: "string" },
                               status: { type: "string" },
                               runDetails: { type: "string" }
                              }
                            }
                         },
                            // pageSize: 10
                        },

                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "sequenceNumber",
                            title: "Sequence",
							width:150	
							
                        },{
                            field: "taskStartDateTime",
                            title: "Task Start Date",
							width:150,
							template: '#=templateForDateColumnTime(taskStartDateTime)#',filterable: false,
								sortable: false,
                        },{
                            field: "taskEndDateTime",
                            title: "Task End Date",
							width:150,
							template: '#=templateForDateColumnTime(taskEndDateTime)#',filterable: false,
								sortable: false,
                        }, {
                            field: "status",
                            title: "Task Status",
							template: '#=templateForHover(status)#',
							width:150	,filterable: { multi: true }
                        }, {
                            field: "runDetails",
                            title: "Run Details",
							template: '#=templateForHover(runDetails)#',
							width:300
                        }],dataBound: dataBound
                    });
					});
	  }
    }
  ,
                        filterable: true,
                        groupable: true,
                        sortable: true,
                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "taskName",
							template: '#=templateForHover(taskName)#',
                            title: "Task Name",
							
							width:300
                        },{
                            field: "flowType",
							template: '#=templateForHover(flowType)#',
                            title: "Flow Type",width:200
                        },{
                            field: "taskStartDate",
                            title: "Task Start Date",width:200,filterable: false,
								sortable: false,
							template: '#=templateForDateColumn(taskStartDate)#'
                        },{
                            field: "taskEndDate",
                            title: "Task End Date",width:200,
							template: '#=templateForDateColumn(taskEndDate)#',filterable: false,
								sortable: false,
							
                        },{
                            field: "taskTechnicalName",
							template: '#=templateForHover(taskTechnicalName)#',
                            title: "Task Technical Name",width:200,
							
                        },{
                            field: "runDetails",
							template: '#=templateForHover(runDetails)#',
                            title: "Run Deatils",width:200,
							
                        },{
                            field: "runCount",
                            title: "Run Count",width:200,
							
                        },{
                            field: "repository",
							template: '#=templateForHover(repository)#',
                            title: "Data Repository",width:200,
							aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)",filterable: { multi: true }
							
                        },{
                            field: "status",
							template: '#=templateForHover(status)#',
                            title: "Task Status",width:200,
							aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)",filterable: { multi: true }
							
                        }],dataBound: dataBound
                    });
					
		$(".k-grouping-header").append('<div class="k-group-indicator" data-field="repository" data-title="Data Repository" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Data Repository</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div><div class="k-group-indicator" data-field="status" data-title="Task Status" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Task Status</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div>');
		$(".k-link").click();
		$(".overlay").css("display","none");
		},0);			
	});
	
}

function dcOnClick(elem)
{
	if($(elem).hasClass("arrowHeaderSSClicked"))
	{
		$(elem).removeClass("arrowHeaderSSClicked");
		$("#stagSummaryDetailContainer").show();
		$("#sourceSystemClicked").hide	();
		$("#drClicked").hide();	
		$("#consumersClicked").hide();
		
	}	
	else{
		$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
		$("#stagSummaryDetailContainer").hide();
		$("#drClicked").hide();
		$("#consumersClicked").show();
		$("#sourceSystemClicked").hide();
		$(elem).addClass("arrowHeaderSSClicked");
		dataconsumerGrid();
		
		
	}
	
}
function dataconsumerGrid()
{
	if($("#grid4").data("kendoGrid")!=undefined)
		$("#grid4").data("kendoGrid").destroy(); // destroy the Grid
    
	$("#grid4").empty();
	var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	var query={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV
			 };
			 
	fetchData(query,"/dldwebapplication/getdataconsumersummary").then(
	
	function(dataforgrid){		 
	$(".overlay").css("display","block");		 
	setTimeout(
	function(){	
	$("#grid4").kendoGrid({
						toolbar: ["excel"], excel: { fileName: "Data Consumers Details.xlsx"} ,
                        dataSource: {
                            type: "json",
                            data:dataforgrid,
                           
                            schema: {
								data:"data",
								total: function (result) {
                            result = result.data || result;
                            return result.length;
                        },
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               reportName: { type: "string" },
                               status: { type: "string" },
                               dataAvailDueDate: { type: "string" },
                               dataAvailCompletedDate: { type: "string" },
                               dataProcessingDueDate: { type: "string" },
                               lastDataProcessingDate: { type: "string" },
							   solutionName:{type: "string"},
								dataConsumer:{ type: "string" },
								lineItemFlag:{ type: "string" },
								reportId:{ type: "string" },
								solutionId:{ type: "string" },
								dataProcessedstatus:{ type: "string" },
                              }
                            }
                         },
						 aggregate: [ 
                                          { field: "dataConsumer", aggregate: "count" },
										  { field: "solutionName", aggregate: "count" }
										  
									]
						
						 
                            // pageSize: 10
                        },
						detailTemplate: '<div style="width:100%;" class="grid5"></div>',
  detailInit: function(e) {
	  var businessDateAndFilterDetails = getBusinessDateAndFilterDetails();
	  
	  
	  
	var query1={
			 businessDate:businessDateAndFilterDetails.businessDateSelected,
			 isFlowFilterApplied:businessDateAndFilterDetails.isFlowTypeFilterAppliedFlag,
			 isFrequencyFilterApplied:businessDateAndFilterDetails.isFrequencyFilterAppliedFlag,
			 flowFilterCSV:businessDateAndFilterDetails.flowTypeFilterCSV,
			 frequencyFilterCSV:businessDateAndFilterDetails.frequencyFilterCSV,
			 reportId:e.data.reportId,
			 solutionId:e.data.solutionId,
			 solutionName:e.data.solutionName
			 };
    $(".grid5").kendoGrid({
                        dataSource: {
                            type: "json",
                            transport: {
                                 read: function (options) {
									 
									query1.pageNo=options.data.page;
									query1.pageSize=options.data.pageSize;
									if(options.data.filter!=undefined)
									{
										for(var i=0;i<options.data.filter.filters.length;i++)
										{
											if(options.data.filter.filters[i].field=="lineItemId")
											{
													query1.lineItemIdSearch=options.data.filter.filters[i].value;
													query1.lineItemDescSearch="";
											}
											else
											{
													query1.lineItemDescSearch=options.data.filter.filters[i].value;
													query1.lineItemIdSearch="";
											}			

										}											
									}
									else
									{
										query1.lineItemIdSearch="";
										query1.lineItemDescSearch="";
										
									}
									 
                                $.ajax({ 
									  url: '/dldwebapplication/getlineitemsummary',
									  data:query1,
									  error: function(data){ }, 
									  success:function(data){
										  options.success(data);
										  
									  } 

									});
								 }
                            },
                           
                            schema: {
								data:"data",
								pageSize: 2,
								total:"totalCount",
						// pageSize: 2,
                       type: 'json',
                       model: {
                           fields: {
                               lineItemId: { type: "string" },
							   dataAvailCompletedDate: { type: "string" },
                               status: { type: "string" },
                               dataProcessingDueDate: { type: "string" },
							   lastDataProcessingDate: { type: "string" },
							   lineItemName: { type: "string" },
							   lineItemProcessedStatus :{ type: "string" },
                              }
                            }
                         },
						 pageSize: 10,
                          serverPaging: true,  // pageSize: 10
						  serverFiltering: true,
                        },
						filterable: true,
						sortable: true,
                        pageable: true,//turn true for pagination styling done..
						filterMenuInit: function(e) {
    
      var firstValueDropDown = e.container.find("select:eq(0)").data("kendoDropDownList");
      firstValueDropDown.wrapper.hide();
	  var secondValueDropDown = e.container.find("select:eq(1)").data("kendoDropDownList");
      secondValueDropDown.wrapper.hide();
	  var thirdValueDropDown = e.container.find("select:eq(2)").data("kendoDropDownList");
      thirdValueDropDown.wrapper.hide();
	  e.container.find(".k-widget").eq(2).next().hide();
	  e.container.find(".k-filter-help-text").text("Show items with value like:")
    
  },
                        columns: [
						{
                            field: "lineItemId",
                            title: "ID",
							width:150	
							
                        },
						{
                            field: "lineItemName",
                            title: "LineItem Name",
							template: '#=templateForHover(lineItemName)#',
							width:150	
							
                        },{
                            title: "Date Availability",
							columns: [{
                                field: "status",
                                title: "Status",
								template: '#=templateForHover(status)#',
                                width: 50,
								filterable: false,
								sortable: false,filterable: { multi: true }
                            },{
                                field: "dataAvailDueDate",
                                title: " Due Date",
                                width: 50,
								filterable: false,
								sortable: false,
								template: '#=templateForDateColumnPeriodId(dataAvailDueDate)#'
                            },{
                                field: "dataAvailCompletedDate",
                                title: "Completion Date",
                                width: 100,
								filterable: false,
								sortable: false,
								template: '#=templateForDateColumnPeriodId(dataAvailCompletedDate)#'
                            }],
							width:200
                        },{
                            title: " Date Processing",
							columns: [{
                                field: "lineItemProcessedStatus ",
                                title: "Status",
                                width: 100,
								filterable: false,
								sortable: false,
								template: '#=templateForDataCinsumerStatus(lineItemProcessedStatus)#'
                            },{
                                field: "lastDataProcessingDate",
                                title: "Last Processing Date",
                                width: 100,
								filterable: false,
								sortable: false,
								template: '#=templateForDateColumnPeriodId(lastDataProcessingDate)#'
                            }],
							width:200
                        }],dataBound: dataBound
                    });
	  }
    
  ,
                        filterable: true,
                        groupable: true,
                        sortable: true,
                        pageable: false,//turn true for pagination styling done..
                        columns: [{
                            field: "reportName",
                            title: "Name",
							template: '#=templateForHover(reportName)#',
							width:150	
							
                        },{
                            title: "Date Availability",
							columns: [{
                                field: "status",
                                title: "Status",
                                width: 100,
								template: '#=templateForDataCinsumerStatus(status)#',filterable: { multi: true }
                            },{
                                field: "dataAvailDueDate",
                                title: " Due Date",
                                width: 100,
								template: '#=templateForDateColumnPeriodId(dataAvailDueDate)#'
                            },{
                                field: "dataAvailCompletedDate",
                                title: "Completion Date",
                                width: 150,
								template: '#=templateForDateColumnPeriodId(dataAvailCompletedDate)#'
                            }],
							width:350
                        },{
                            title: " Date Processing",
							columns: [{
                                field: "dataProcessedstatus",
                                title: "Status",
                                width: 100,
								template: '#=templateForDataCinsumerStatus(dataProcessedstatus)#'
                            },{
                                field: "lastDataProcessingDate",
                                title: "Last Processing Date",
                                width: 180,
								template: '#=templateForDateColumnPeriodId(lastDataProcessingDate)#'
                            }],
							width:280
                        },
						{
                                field: "solutionName",
                                title: "Solution",
                                width: 100,
								template: '#=templateForHover(solutionName)#',
								aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)",
                            },
							{
                                field: "dataConsumer",
                                template: '#=templateForHover(dataConsumer)#',
								title: "Data Consumer",
                                width: 130,
								aggregates: ["count"],
							groupHeaderTemplate: "#=value# (#= count#)",
                            },{
                            field: "",
                            title: "",width:40,
							template: '#=templateForlineage(reportName,"dataConsumer",solutionName,reportId)#'
                        }],dataBound: dataBound
                    });
					
				$(".k-grouping-header").append('<div class="k-group-indicator" data-field="solutionName" data-title="Solution" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Solution</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div><div class="k-group-indicator" data-field="dataConsumer" data-title="Data Consumer" data-dir="asc"><a href="#" class="k-link"><span class="k-icon k-i-sarrow-n">(sorted ascending)</span>Data Consumer</a><a class="k-button k-button-icon k-button-bare"><span class="k-icon k-i-group-delete"></span></a></div>');
				$(".k-link").click();
				if(sourceSystemTabClickFromTile=="Y"){
						$("#grid4").data("kendoGrid").dataSource.filter({
							filters : [
								{
									field: "status",
									operator: "eq",
									value: statusToNavigate.toUpperCase()
								},
								{
									field: "solutionName",
									operator: "eq",
									value: solutionToNavigate
								},
								{
									field: "dataConsumer",
									operator: "eq",
									value: dataConsumersTypeToNavigate.toUpperCase()=="REPORTS"?"Report":dataConsumersTypeToNavigate
								}
							],
							logic:"and"
							
						});
						
						
						sourceSystemTabClickFromTile="N";
				}
				$(".overlay").css("display","none");
			},0);		
	});
	
}
function templateForDateColumnPeriodId(name){
	if(name=="NA")
		return name;
	if(name!=null&&name!="null"&&name!="")
		return kendo.toString(kendo.parseDate(name+"", 'yyyyMMdd'), 'dd MMM yyyy');
	else
		return "";
}
function templateForDataCinsumerStatus(status1)
{
	if(status1.toLowerCase()=="completed")
		return "<div style='color:#92d050' >"+status1+"</div>";
	else if(status1.toUpperCase()=="OVERDUE")
		return "<div style='color:#ffc000' >"+status1+"</div>";
	else if(status1.toLowerCase()=="partially completed")
		return "<div style='color:#00b0f0' >"+status1+"</div>";
	else if(status1.toUpperCase()=="NOT DUE YET")
		return "<div style='color:grey' >"+status1+"</div>";
	else 
		return "<div style='color:black' >"+status1+"</div>";
}
function templateForDateColumnTime(name){
	if(name!=null&&name!="null"&&name!="")
		return '<div>'+kendo.toString(kendo.parseDate(name+"", 'yyyy-MM-dd HH:mm:ss'), 'dd MMM yyyy')+'</div><div>'+kendo.toString(kendo.parseDate(name+"", 'yyyy-MM-dd HH:mm:ss'), 'hh:mm tt')+'</div>';
	else
		return "";
}

function showHideDataLineage(elm){
	if($(elm).hasClass("DLHidden")){
		var businessDateAndFilterDetails=getBusinessDateAndFilterDetails();
		$(elm).removeClass("DLHidden");
		$("#allDetailsParent").css("display","none");
		$("#dataLineageContainer").css("display","inline-block");
		document.getElementById("toRightByNPix").style.display = "block";
		document.getElementById("toLeftByNPix").style.display = "block";
		$("#dataLineageContainer").append("<iframe id = 'dataLineageIframe' src='/dldwebapplication/lineage?cbd="+ businessDateAndFilterDetails.businessDateSelected+"' style ='width:100%' onload = 'resizeIframe(this);'></iframe>")
		
		//generateBaseLayout();
	}else{
		$(elm).addClass("DLHidden")
		$("#allDetailsParent").css("display","inline-block");
		$("#dataLineageContainer").css("display","none");
		$("#dataLineageContainer").empty();
		$("#configEntity").hide();
		$("#configEntityParent").hide();
		$("#configEntityParent").addClass("hidden");
		document.getElementById("toRightByNPix").style.display = "none";
		document.getElementById("toLeftByNPix").style.display = "none";
	}
	$('#allOrLineItemSwitch').val("all");
	$('#reportWithLineItem').hide();
	$("#configEntity").css("left","-38px")
	
}

function resizeIframe(obj){
	obj.style.height = obj.contentWindow.document.body.scrollHeight + 'px';
	if(document.getElementById("dataLineageIframe").contentWindow.location.href.indexOf("/lineage?")<0){
		window.location.href ="login"
	}
}

function showHideFilter(){
	if($("#configEntityParent").hasClass("shown")){
		$("#configEntityParent").hide();
		$("#configEntityParent").addClass("hidden");
		$("#configEntityParent").removeClass("shown");
		$("#configEntity").css("left","-38px")
	}else{
		$("#configEntityParent").show();
		$("#configEntityParent").removeClass("hidden");
		$("#configEntityParent").addClass("shown");
		$("#configEntity").css("left","372px")
	};
}


function getNodesFromIframe(nodeArray){
	$("#configEntity").show();
	$(".resultContainer").empty();
	for(var i = 0;i<nodeArray.length;i++){
		var nodeId = document.getElementById('dataLineageIframe').contentWindow.hexor[nodeArray[i].entityName+nodeArray[i].entityOwner];
		$("#"+nodeArray[i].entityType+"ResultContainer").append('<div id ="filterSlabLeaf_'+nodeId+'"  entityName = "'+nodeArray[i].entityName+'" ownerName = "'+nodeArray[i].entityOwner+'" class="filterNodes '+nodeArray[i].entityType+'NodeColor" ><div class="nodeName" title = "'+nodeArray[i].entityName +' ('+nodeArray[i].entityOwner+')'+'" >'+nodeArray[i].entityName +' ('+nodeArray[i].entityOwner+')' +'</div><div class="fa fa-minus-circle nodeImage" onclick = "showOrHideNodes(this)"></div></div>')
	}
	//fa-plus-circle
}

function showOrHideNodes(elm){
	if($("#showSwitch").prop('checked')){
		if($(elm).hasClass("fa-minus-circle")){
		$(elm).addClass("fa-plus-circle");
		$(elm).parent().addClass("lowShade");
		$(elm).removeClass("fa-minus-circle");
	}else{
		$(elm).removeClass("fa-plus-circle");
		$(elm).addClass("fa-minus-circle");
		$(elm).parent().removeClass("lowShade");
	}
	document.getElementById('dataLineageIframe').contentWindow.showOrHideNodes($(elm).parent().attr("entityName"),$(elm).parent().attr("ownerName"));
	}
	
}


function selectOrRemoveAllSourceSystem(statusRec){
	document.getElementById('dataLineageIframe').contentWindow.showOrHideAllNodes("sourceSystem",statusRec);
}
function selectOrRemoveAllDataRepo(statusRec){
	document.getElementById('dataLineageIframe').contentWindow.showOrHideAllNodes("dataRepo",statusRec);
}
function selectOrRemoveAllDataConsumer(statusRec){
	document.getElementById('dataLineageIframe').contentWindow.showOrHideAllNodes("dataConsumer",statusRec);
}

function searchSourceSystem(elm,evt){
	if ($(elm).val() != "" && $(elm).val() != null) {
        $('.sourceSystemNodeColor').hide();
        var searchValue = $(elm).val();
        $('.sourceSystemNodeColor').each(function(i, obj) {

            if ($(obj).find(".nodeName").text().toUpperCase().indexOf(searchValue.toUpperCase()) > -1) {
                $(obj).show();
            }
        });


    } else {
        $('.sourceSystemNodeColor').show();
    }
}
function searchDataRepo(elm,evt){
	if ($(elm).val() != "" && $(elm).val() != null) {
        $('.dataRepositoryNodeColor').hide();
        var searchValue = $(elm).val();
        $('.dataRepositoryNodeColor').each(function(i, obj) {

            if ($(obj).find(".nodeName").text().toUpperCase().indexOf(searchValue.toUpperCase()) > -1) {
                $(obj).show();
            }
        });


    } else {
        $('.dataRepositoryNodeColor').show();
    }
}
function searchDataConsumer(elm,evt){
	if ($(elm).val() != "" && $(elm).val() != null) {
        $('.dataConsumerNodeColor').hide();
        var searchValue = $(elm).val();
        $('.dataConsumerNodeColor').each(function(i, obj) {

            if ($(obj).find(".nodeName").text().toUpperCase().indexOf(searchValue.toUpperCase()) > -1) {
                $(obj).show();
            }
        });


    } else {
        $('.dataConsumerNodeColor').show();
    }
}

function showFilterForSource(elm){
	$(".filterNodeSelect").removeClass("darkSlabSelect")
	$(".entityTypeSlabs").hide();
	$("#SSFilterSlab").show();
	$(elm).addClass("darkSlabSelect");
}

function showFilterForDataRepo(elm){
	$(".filterNodeSelect").removeClass("darkSlabSelect")
	$(".entityTypeSlabs").hide();
	$("#DRFilterSlab").show();
	$(elm).addClass("darkSlabSelect");
}

function showFilterForDataConsumer(elm){
	$(".filterNodeSelect").removeClass("darkSlabSelect")
	$(".entityTypeSlabs").hide();
	$("#DCFilterSlab").show();
	$(elm).addClass("darkSlabSelect");
}
function templateForHover(name){
	
	return "<div title = '"+name+"' class = 'firstColumnName'>"+ name+"</div>";
}

function showLineageForTheCurrentEntity(elm){
	lineageForEntity.entityName = $(elm).attr("entityName");
	lineageForEntity.entityType = $(elm).attr("entityType");
	lineageForEntity.entityOwner = $(elm).attr("entityOwner");
	showHideDataLineage($("#allTaskGrid"));
}
function downloadDataLineage(){
	effectiveDate=formatDateToPeriodId($("#cbdDatePicker").val());
    var downloadUrl = './handleLineageDownload?effectiveDate='+effectiveDate;
    $('#hiddenDownloader').attr('src', downloadUrl);
}

function showLineageForThisReport(sel1){
	if(sel1.value!=""){
		var sel = sel1.value;
		var reportId = $(sel1).find('option:selected').attr('reportid');//$(sel1).attr("reportid");
		var solutionName =$(sel1).find('option:selected').attr('solutionname');//$(sel1).attr("solutionname");
		cleanUpFullLayout();
		document.getElementById('dataLineageIframe').contentWindow.loadForAReport(reportId,solutionName);
	}
}

function showOrHideLineItemDetails(sel){
	if(sel.value.toUpperCase()=='ALL'){
		$("#reportWithLineItem").hide();
		cleanUpFullLayout();
		document.getElementById('dataLineageIframe').contentWindow.loadFullLineage();
	}else if(sel.value.toUpperCase()=='LINEITEM'){
		$("#reportWithLineItem").css("display","inline-block");
	}
}


function cleanUpFullLayout(){
	$(".resultContainer").empty();
	$(".inputStyle").val("");
	$('#showSwitch').prop('checked',true);
	$('#switchText').prop('checked',true);
	$('#allOrLineItemSwitch').val("all");
	$('#reportWithLineItem').hide();
}


function dataBound(e) {	
if(undefined!=$("#grid").data("kendoGrid") || e.sender.dataSource.view().length != 0){
	if (e.sender.dataSource.view().length === 0) {
	var colspan = $("#grid thead").find("th").length;
	$("[data-role='grid'] tbody").html("<tr><td colspan='" + colspan + "'></td></tr>");
    var grid = $("#grid").data("kendoGrid");
    grid.thead.closest(".k-grid-header-wrap").scrollLeft(0);
    grid.table.width(2000);          
    $(".k-grid-content").height(2 * kendo.support.scrollbar());
 }
}
  
}





function navigateToSourceSystemDetails(elm,sourceEntity){
	var statusChosen = $($(elm).find("div")[1]).text();
	if(Number($($(elm).find("div")[0]).text())<=0){
		alert("The current filter event has no results.")
	}else{
		if($(".arrowHeaderSS").hasClass("arrowHeaderSSClicked"))
		{
			$(".arrowHeaderSS").removeClass("arrowHeaderSSClicked");
			$("#stagSummaryDetailContainer").show();
			$("#sourceSystemClicked").hide();
			$("#drClicked").hide();	
			$("#consumersClicked").hide();	
		}	
		else{
			sourceSystemTabClickFromTile = "Y";
			statusToNavigate = statusChosen;
			$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
			$("#sourceSystemClicked").show();
			$("#consumersClicked").hide();	
			$("#stagSummaryDetailContainer").hide();
			$(".arrowHeaderSS").addClass("arrowHeaderSSClicked");
			$("#drClicked").hide();
			$("#consumersClicked").hide();	
			if(sourceEntity=="SOURCEENTITIES"){
				$("#sourceEntityDetails").click();
			}else if(sourceEntity=="DATALOADTASKS"){
				$("#dataLoadTaskDetails").click();
			}else if(sourceEntity=="SOURCESYSTEMS"){
				$("#sourceSystemsSummary").click();
			}
			
			
		}
	}
}




function navigateToDataRepositoryDetails(elm,sourceEntity){
	var statusChosen = $($(elm).find("div")[1]).text();
	if(Number($($(elm).find("div")[0]).text())<=0){
		alert("The current filter event has no results.")
	}else{
		if($(".arrowHeaderDR").hasClass("arrowHeaderSSClicked"))
		{
			$(".arrowHeaderDR").removeClass("arrowHeaderSSClicked");
			$("#stagSummaryDetailContainer").show();
			$("#sourceSystemClicked").hide();
			$("#drClicked").hide();	
			$("#consumersClicked").hide();			
		}else{
			$("#drClicked").show();
			sourceSystemTabClickFromTile = "Y";
			statusToNavigate = statusChosen;
			$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
			$("#stagSummaryDetailContainer").hide();
			$("#consumersClicked").hide();	
			$("#sourceSystemClicked").hide();
			$(".arrowHeaderDR").addClass("arrowHeaderSSClicked");
			if(sourceEntity=="DATAREPOSITORYTASKS"){
				$("#drDataLoadTask").click();
			}else if(sourceEntity=="DATAREPOSITORYENTITIES"){
				$("#drRepoEntity").click();
			}
			
			
		}
	}
	
}



function navigateToDataConsumersDetails(elm){
	var statusChosen = $($(elm).find("div")[1]).text();
	if(Number($($(elm).find("div")[0]).text())<=0){
		alert("The current filter event has no results.")
	}else{
		if($(".arrowHeaderTg").hasClass("arrowHeaderSSClicked"))
		{
			$(".arrowHeaderTg").removeClass("arrowHeaderSSClicked");
			$("#stagSummaryDetailContainer").show();
			$("#sourceSystemClicked").hide();
			$("#drClicked").hide();	
			$("#consumersClicked").hide();
			
		}	
		else{
			sourceSystemTabClickFromTile = "Y";
			statusToNavigate = statusChosen;
			dataConsumersTypeToNavigate = $(elm).parent().find(".tgtTypeLengend").clone().children().remove().end().text().trim();
			solutionToNavigate = $(elm).parent().parent().find(".tgtSolutionLegend").clone().children().remove().end().text().trim();
			$(".arrowHeaderSSClicked").removeClass("arrowHeaderSSClicked");
			$("#stagSummaryDetailContainer").hide();
			$("#drClicked").hide();
			$("#consumersClicked").show();
			$("#sourceSystemClicked").hide();
			$(".arrowHeaderTg").addClass("arrowHeaderSSClicked");
			dataconsumerGrid();
			
			
		}
	}	
}