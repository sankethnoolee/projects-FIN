//method to create the slab in actual vs progress
function createActualProgressSlabs(i,date,month,week,plannedValue,plannedCount,completedValue,completedCount,sigmaPlanned,sigmaPlannedPerc,sigmaCompleted,sigmaCompletedPerc,isDateMatch,postCurrentDate){
	$("#actualDataContainer").append(
				'<div  class ="verParentBlockForDates" progressActualBlockNumber = '+i+'>'+
					'<div  class = "row1ForDetails " style = "padding-top: 7%;">'+
						'<div class = "dateAndMonthDetails"> '+date+' '+month+'</div>'+
						'<div class = "WeekDetails" >'+week+'</div>'+
					'</div>'+
					'<div id = "parentRow2" class = "parentblock">'+
						'<div value = '+plannedValue+' class = "plannedNumR2">'+plannedCount+'</div>'+
						'<div value = '+completedValue+' class = "completedNumR2">'+completedCount+'</div>'+
					'</div>'+
					'<div id = "parentRow3" class = "parentblock">'+
						'<div  class = "plannedNumR3">'+sigmaPlanned+'<span class = "subReplace">'+" "+sigmaPlannedPerc+'</span></div>'+
						'<div  class = "completedNumR3">'+sigmaCompleted+'<span class = "subReplace">'+" "+sigmaCompletedPerc+'</span></div>'+
					'</div>'+
				'</div>');
	if(isDateMatch=="Y"){
		$("div").find("[progressActualBlockNumber='" + (i) + "']").find(".row1ForDetails").addClass("dateHighLightForDetails");
	}
	if(postCurrentDate=="Y"){
		$("div").find("[progressActualBlockNumber='" + (i) + "']").find(".completedNumR3").addClass("noDataHere");
	}
}

//fill out for empty space layout generation.
function appendDummyDivForDetails(count){
	for(var i = 0;i<count;i++){
		$("#actualDataContainer").append(
				'<div  class ="verParentBlockForDates" >'+
					'<div  class = "row1ForDetails" style = "padding-top: 7%;">'+
						'<div class = "dateAndMonthDetails"> </div>'+
						'<div class = "WeekDetails" > </div>'+
					'</div>'+
					'<div id = "parentRow2" class = "parentblock">'+
						'<div  class = "plannedNumR2"></div>'+
						'<div  class = "completedNumR2"></div>'+
					'</div>'+
					'<div id = "parentRow3" class = "parentblock">'+
						'<div  class = "plannedNumR3 noDataHere" >-<sub></sub></div>'+
						'<div  class = "completedNumR3 noDataHere" >-<sub></sub></div>'+
					'</div>'+
				'</div>'
				);
	}
}


//populating flowType on date change
function generateFlowTypes(flowArray){
	$.each(flowArray,function(k,v){
		$("#applicableFlowResults").append('<div title = "'+v+'" onclick = "filterBasedOnFlowType(this)" class = "flowBlocks">'+v+'</div>')
	});
}

//populating freq on date change
function generateFreqTypes(freqArray){
	$.each(freqArray,function(k,v){
		$("#applicableFreqResults").append('<div title = "'+v+'" onclick = "filterBasedOnFrequency(this)" class = "frequencyBlocks">'+v+'</div>')
	});	
}

//for individual data repo slabs
function generateIndividualDataRepoSlab(repoSlabDetails){
	
	$("#RepoResultsCont").append('<div class = "repoSlabParent">'+
										'<div class ="solutionNameRepo">'+repoSlabDetails.repositoryName+'</div>'+
										'<div class = "solutionStatsParent">'+
											'<div class = "taskCompStatRepo"><span class ="completed">'+repoSlabDetails.taskCompleted+'</span><span class = "pending">/'+repoSlabDetails.taskTotal+'</span></div>'+
											'<div class = "entityCompStatRepo"><span  class = "completed">'+repoSlabDetails.entityCompleted+'</span><span class = "pending">/'+repoSlabDetails.entityTotal+'</span>'+
											'</div>'+
										'</div>'+
									'</div>');
}


//for data consumers layout
function generateLayoutForDataConsumers(dataForPerSolution){
	
	$("#tgtCont").append('<fieldset class = "tgtSolutionParent"><legend class = "tgtSolutionLegend">'+dataForPerSolution.solutionName+'</legend><fieldset class = "tgtTypeParent"><legend class = "tgtTypeLengend"><b style  = "font-size:17px !important;margin-right:2%;">'+dataForPerSolution.totalReports+'</b> REPORTS<div class = " customLegendTgtType"></div></legend><div class = "cbdStatusFor4Block" onclick="navigateToDataConsumersDetails(this)"><div class = "completedForStage" id = "failedNum">'+dataForPerSolution.completed+'</div><div class = "statusTextForStage">Completed</div></div><div class = "cbdStatusFor4Block" onclick="navigateToDataConsumersDetails(this)"><div class = "pendingForStage" id = "pendingNum">'+dataForPerSolution.notDueYet+'</div><div class = "statusTextForStage">Not Started</div></div><div class = "cbdStatusFor4Block" onclick="navigateToDataConsumersDetails(this)"><div class = "partiallyForStage" id = "partialNum">'+dataForPerSolution.partiallyCompleted+'</div><div class = "statusTextForStage">Partially&#10;Completed</div></div><div class = "cbdStatusFor4Block" onclick="navigateToDataConsumersDetails(this)"><div class = "overdueForStage" id = "failedNum">'+dataForPerSolution.overDue+'</div><div class = "statusTextForStage">Overdue</div></div></fieldset></fieldset>');
}
