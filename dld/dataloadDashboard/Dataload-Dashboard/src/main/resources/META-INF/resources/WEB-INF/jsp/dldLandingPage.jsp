<%@ include file="header.jsp"%>

<!--lib and css -->
<script src="js/lib/jquery-3.2.1.min.js"></script>
<script src="js/lib/jquery-ui.js"></script>

<link rel="stylesheet" type="text/css" href="css/lib/font-awesome-4.6.3/css/font-awesome.min.css" />
<link rel="stylesheet" charset="utf-8" type="text/css" href="js/lib/highChart/grouped-categories.css"/>
<script type="text/javascript" src="js/lib/radial-progress-bar.js"></script>
<script type="text/javascript" src="js/lib/highChart/highstock.js"></script>
<script type="text/javascript" src="js/lib/highChart/highcharts-more.js"></script>
<link rel="stylesheet" type="text/css" href="css/lib/kendo/kendo.common.min.css" />
<link rel="stylesheet" type="text/css" href="css/lib/kendo/kendo.default.min.css" />
<link rel="stylesheet" type="text/css" href="css/lib/kendo/kendo.default.mobile.min.css" />
<link rel="stylesheet" type="text/css" href="js/lib/bootstrap-datepicker/css/datepicker.css" />
<link rel="stylesheet" type="text/css" href="css/framework/pageContents/landingPage.css" />
<script type="text/javascript" src="js/lib/bootstrap-datepicker/js/bootstrap-datepicker.js"></script>
<script src="js/lib/highChart/grouped-categories.js"></script>
<script src="js/lib/highChart/export-csv.js"></script>	
<script type="text/javascript" src="js/lib/moment-with-locales.min.js"></script>
<script type="text/javascript" src="js/lib/kendo/kendo.all.min.js"></script>
<script src="js/framework/pageContents/dldUtils.js" ></script>
<script src="js/framework/pageContents/layoutFunctions.js" ></script>
<script src="js/lib/jszip.js"></script> <script src="js/framework/pageContents/landingPage.js" ></script>

<!--lib and css -->

<!-- main container-->

<div id = "main-content" style = "font-family: segoe ui;min-width:1200px;width:100%;min-height:713px;margin-top:-7px;height:auto;">
	<!--loader-->
	<div  class="overlay">
		<div class="loader"></div>
	</div>
	<!--loader-->


	<!--scroll top button-->
	<button onclick="topFunction()" class = "fa fa-angle-double-up" id="toTop" title="Scroll to Top"></button>
	<button onclick="leftFunction()" class = "fa fa-angle-left" id="toLeftByNPix" title="Scroll 500 pixels Left"></button>
	<button onclick="bottomFunction()" class = "fa fa-angle-double-down" id="toBottom" title="Scroll to Bottom"></button>
	<button onclick="bottomByNFunction()" class = "fa fa-angle-down" id="toBottomByNPix" title="Scroll 500 pixels Bottom"></button>
	<button onclick="topByNFunction()" class = "fa fa-angle-up" id="toTopByNPix" title="Scroll 500 pixels Top"></button>
	<button onclick="rightFunction()" class = "fa fa-angle-right" id="toRightByNPix" title="Scroll 500 pixels Right"></button>
	
	<!--scroll top button-->
	
	<!-- filter entities -->
	<div id = "configEntity" class = "fa fa-filter" onclick = "showHideFilter();">Filter Entity</div>
	<div id = "configEntityParent" >
	<!--<div class = "switchContainer">
	<div class = "switchText">Arrows <span style = "float:right;line-height:1.3;" class  = "fa fa-eye-slash"></span></div>
	<label class="switch">
	  <input id = "arrowSwitch" type="checkbox" checked>
	  <span class="slider round"></span>
	</label>
	<div class = "switchTextIcon fa fa-eye"></div>

</div>-->
<div class = "switchContainer" style="float:right;">
	<div class = "switchText">Description <span  style = "line-height:1.3;" class  = "fa fa-eye-slash"></span></div>
	<label class="switch">
	  <input id = "descriptionSwitch" type="checkbox" checked>
	  <span class="slider round"></span>
	</label>
	<div class = "switchTextIcon fa fa-eye"></div>

</div>
<!--<div class = "switchContainer">
	<div class = "switchText">Status <span  style = "float:right;line-height:1.3;" class  = "fa fa-eye-slash"></span></div>
	<label class="switch">
	  <input id = "statusSwitch" type="checkbox" checked>
	  <span class="slider round"></span>
	</label>
	<div class = "switchTextIcon fa fa-eye"></div>

</div>-->
<div class = "switchContainer" style="float:right;">
	<div class = "switchText">Show/Hide<span  style = "float:right;line-height:1.3;" class  = "fa fa-eye-slash"></span></div>
	<label class="switch">
	  <input id = "showSwitch" type="checkbox" checked>
	  <span class="slider round"></span>
	</label>
	<div class = "switchTextIcon fa fa-eye"></div>

</div>

<div id  = "dropDownContainers" style  = "height: 64px;    clear: both;">
<div style="
    font-size: 12px;
    margin-bottom: 5px;
">Lineage Level</div>
	<select id  = "allOrLineItemSwitch" onchange="showOrHideLineItemDetails(this);">
	  <option value="all" selected>All</option>
	  <option value="lineItem">lineItem</option>
	</select>
	<select id  = "reportWithLineItem" onchange="showLineageForThisReport(this);">
	  <option value="" selected> </option>
	  
	</select>


</div>
	<div onclick = "showFilterForSource(this);" class = "filterNodeSelect filterSSSlab darkSlabSelect">Source System</div>
	<div onclick = "showFilterForDataRepo(this);" class = "filterNodeSelect filterDRSlab">Data Repository</div>
	<div onclick = "showFilterForDataConsumer(this);" class = "filterNodeSelect filterSDClab">Data Consumer</div>
	<div class = "entityTypeSlabs" id  = "SSFilterSlab">
    	<div class="headingForEfilter dataSourceEfilter">Source System</div>
		<div style = "color: #2f1b14;height: 30px;width: 90%;margin-left:5%;font-size: 12px;"><input type="checkbox" name="source" value="source" checked id = "sourceSystemCheckBox">All / None</div>
		<input class = "inputStyle" id="sourceSystemSearch" placeholder="Search Source System entity" onkeyup='searchSourceSystem(this,event);' />
		<div class = "resultContainer dataSourceEfilter" id = "sourceSystemResultContainer"></div>

    </div>
	<div class = "entityTypeSlabs" id  = "DRFilterSlab" style ="display:none;">
    	<div class="headingForEfilter dataRepoEfilter">Data Repository</div>
		<div  style = "color: #2f1b14;height: 30px;width: 90%;margin-left:5%;font-size: 12px;"><input type="checkbox" name="dataRepo" value="dataRepo" checked id = "dataRepoCheckBox">All / None</div>
		<input class = "inputStyle" id="dataRepoSearch" placeholder="Search Data Repository entity" onkeyup='searchDataRepo(this,event);'/>
		<div class = "resultContainer dataRepoEfilter" id = "dataRepositoryResultContainer"></div>
    </div>
	<div class = "entityTypeSlabs" id  = "DCFilterSlab" style ="display:none;">
    	<div class="headingForEfilter dataConsumerEfilter">Data Consumer</div>
		<div style = "color: #2f1b14;height: 30px;width: 90%;font-size: 12px;margin-left:5%;"><input type="checkbox" name="dataConsumer" value="dataConsumer" checked id = "dataConsumerCheckBox">All / None</div>
		<input class = "inputStyle" id="dataConsumerSearch" placeholder="Search Data Consumer" onkeyup='searchDataConsumer(this,event);'/>
		<div class = "resultContainer dataConsumerEfilter" id = "dataConsumerResultContainer"></div>
    </div>

	
	
	</div>
	
	<!-- filter entities -->
	
	
	<!--heading -->
	<div id  = "dldHeading" class = "dldHeading">Data Load Dashboard</div>
	
	<!-- summary Slab-->
	<div id = "summaryContainer" class ="summaryContainer">
		<!-- cbd view chnage cont-->
		
	<div id = "viewSwitchParent">
		<div id  = "onlyOneCbd" class = " cbdIcons fa fa-futbol-o"></div>
		<div id  = "allCbd" class = "cbdIcons fa fa-plane"></div>
	
	</div>
	
	<!-- cbd view chnage cont-->
	

	<!-- cbd Details-->
	<div id = "contForDates">
		<div id = "newDivForStatus">
		<div id = "currentBusinessDateParent">
			<!--Date and progress details-->
			<div id = "cbdDateAndProgressParent">
				<div id = "cbdDateComponentParent">
					<div id = "cbdHeading"> Business Date</div>
					<div id = "cbdDecrement" class =" fa fa-chevron-left" onclick = "decrementDate();"></div>
					<div id = "cbdCustomDisplayParent" >
						<div class = "cbdDateDay">24</div>
						<div class = "cbdDateMonth">May</div>
						<div class = "cbdDateYear">2017</div>
						<div class = "cbdDateWeek">Wednesday</div>
						<input id = "cbdDatePicker" readonly></input>
					</div>
					<div id = "cbdIncrement" class =" fa fa-chevron-right" onclick = "incrementDate();"></div>
				</div>
				<div id = "ProgreeStatusText" class = "inProgress">In Progress</div>
				<div id = "failureAlertParent">
					<div class = "fa fa-exclamation-triangle failure attentionIcon"></div>
					<div id = "cbdFailureText" class = "failure">Has Failures</div>
				</div>
			</div>
			<!--Date and progress details-->
			<!--cbd chart Component-->
			<div id = "cbdChartContainer">
				<div id = "cbdTaskCompletionGraph">
					<div id = "cbdTaskCompletionText">Task Completion %</div>
					<div id = "cbdChart" ></div>
					<div id = "cbdLegendsContainer">
						<div id = "plannedCompletionParent">
							<div class = "fa fa-square legendsForCbd" style  = "color:#00b0f0"></div>
							<div id = "plannedCompletionText">Planned Completion %</div>
						</div>
						<div id = "actualCompletionParent">
							<div class = "fa fa-square legendsForCbd" style  = "color:#92d050"></div>
							<div id = "actualCompletionText">Actual Completion %</div>
						</div>
					</div>
				</div>
			</div>
			<!--cbd chart Component-->
			<!-- task status summary cbd-->
			<div id = "cbdTaskSummaryParent">
				<div id = "cbdTaskSummaryText">Task Status Summary</div>
				<div id= "cbdTaskSummarySlab" >
					<div style = "height: 50%;width: 100%;" id  = "topSlabParent">
							<div id = "totalCountCbdParent">
							<div id = "totalTask">0</div>
							<div id = "totalTaskRext">Total Tasks</div>
						</div>
						<div id = "plannedCompletionCountCbdParent">
							<div id = "plannedCompletionTask">0</div>
							<div id = "plannedCompletionTaskRext" style="white-space:pre-wrap;">Planned&#10;Completion</div>
						</div>
						<div id = "notYetCountCbdParent">
							<div id = "notYetTask">0</div>
							<div id = "notYetTaskRext">Not Yet Due</div>
						</div>
						
					</div>
					
					<div id = "currentExecDetailsParent" >
						<div id = "cbdAllStatusParent">
							<div class = "cbdStatusText">
								<div class = "completed" id = "cbdCompletedNum">0</div>
								<div class = "statusText">Completed</div>
							</div>
							<div class = "cbdStatusText">
								<div class = "failed" id = "cbdFailedNum">0</div>
								<div class = "statusText">Failed</div>
							</div>
							<div class = "cbdStatusText">
								<div class = "overdue" id = "cbdOverdueNum">0</div>
								<div class = "statusText">Overdue</div>
							</div>
							<div class = "cbdStatusText">
								<div class = "pending" id = "cbdPendingNum">0</div>
								<div class = "statusText" id ="pendingToday">Pending Due Today</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!-- task status summary cbd-->
		</div>
		<!-- cbd Details-->
		<!--dummy border-->
		<div class= "dummyBorder"></div>
		<!--dummy border-->
		<!-- previous details-->
		
		<div id = "previousContainer">
			<!--previous day parent-->
			<div id = "previousDayParent" class ="previousDetailsParent">
				<!-- no data-->
				<div id="noPreviousDay" style="display:none;text-align: center;">--No previous business date found--</div>

				<!-- no data-->
				<div id = "previousDayDateContainer" class = "previousDateContainer">
					<div class = "previousDateText">Previous Business Date</div>
					<div class = "previousCustomDateFormat" id = "previousDayCustomFormat">
						<div class = "previousDateDay">23</div>
						<div class = "previousDateMonth">May</div>
						<div class = "previousDateYear">2017</div>
						<div class = "previousDateWeek">Tuesday</div>
					
					</div>
					<div class = "previousStatus overdue" id = "previousDayStatus">Completed</div>
				</div>
				
				<div class= "previousChartConatiner">
					<div class = "previousChart" id= "previousDayChart"></div>
					<!--<div id="previousDayChartPercent" >100%</div>
					<div class = "previousExpected" id= "previousDayExpected">Expected : <b>100%</b></div>-->
				</div>
			</div>
			<!--previous day parent-->
			<!--dummy border-->
			<div class= "dummyBorderHorizontal"></div>
			<!--dummy border-->
		
			
			<!--previous month-->
			<div id = "previousMonthParent" class ="previousDetailsParent">
			<!-- no data-->
				<div id="noPreviousPending" style="display:none;text-align: center;">--No previous pending date found--</div>

			<!-- no data-->
				<div id = "previousMonthDateContainer" class = "previousDateContainer">
					<div class = "previousDateText">Previous Pending Business Date</div>
					<div class = "previousCustomDateFormat" id = "previousMonthCustomFormat">
						<div class = "previousDateDay">30</div>
						<div class = "previousDateMonth">Apr</div>
						<div class = "previousDateYear">2017</div>
						<div class = "previousDateWeek">Sunday</div>
					
					</div>
					<div class = "previousStatus overdue" id = "previousMonthStatus">Delayed</div>
				</div>
				
				<div class= "previousChartConatiner">
					<div class = "previousChart" id= "previousMonthChart"></div>
					<!--<div id="previousMonthChartPercent" >90%</div>
					<div class = "previousExpected" id= "previousMonthExpected">Expected : <b>91%</b></div>-->
				</div>
				
			</div>
			</div>
			<!--previous month-->
			
	
		</div>
		<!-- Filter container-->
			<div id = "filterContainerParent">
				
				<div id = "applicableFlowParent">
					<div id  = "applicableFlowText">Applicable Data Flows</div>
					<div id  = "applicableFlowResults">
					</div>
				</div>
				<!--dummy border-->
				<div class= "dummyBorderForFilter" ></div>
				<!--dummy border-->
				
				
				<div id = "applicableFreqParent">
					<div id  = "applicableFreqText">Applicable Frequencies</div>
					<div id  = "applicableFreqResults">
					</div>
				</div>
				
			</div>
			<!-- Filter container-->	
	</div>
		
		

		<!-- previous details-->
		
	</div>

	
	<!-- summary Slab-->
	
	
	<!-- actual progress dld details-->
		<div id = "dldDetailsParentContainer">
			<div id = "viewSwitchParentForDetails">
				<div id  = "allTaskGrid" title = "Show Full Lineage" class = " cbdIcons fa fa-magic-s DLHidden" onclick= "showHideDataLineage(this);"></div>
				<div id  = "allTaskGrid2" title = "Download Lineage" class = " cbdIcons fa fa-download DLHidden" style="margin-top:15px;" onclick= "downloadDataLineage();"></div>
			</div>
			<div id = "allDetailsParent">
			<!--main container for details-->
				<div onclick = "expandCollapseProgressActual(this)" id = "actualProgressAccordianParent" class = "expanded">
					<div id = "actualProgressText">Load Plan v/s Actual Progress Details</div>
					<div class = "fa fa-chevron-down arrowStyle" ></div>
				</div>
				<div id = "actualProgressAccordianDetailsParent">
				<!-- basic desc block for actual and completed-->
					<!--default block 1-->
					<div id = "sigmaDeltaParent" class ="verParentBlock" style  = "padding-top:7%;">
						<div id = "deltaParent" class = "parentblock">
							<div id = "deltaIcon" class = "deltaSigmaIcons">&#x394;</div>
							<div id  = "deltaText" class = "deltaSigmaText">No. of Tasks per Day</div>
						</div>
						<div id = "sigmaParent" class = "parentblock">
							<div id = "sigmaIcon" class = "fa fa-sum deltaSigmaIcons"></div>
							<div id  = "sigmaText" class = "deltaSigmaText">Total No. of Tasks </div>
						</div>
					</div>
					<!--default block 1-->
					<!-- default bloack 2-->
					<div  class ="verParentBlock" >
						<div id = "parentRow1" class = "parentblock" style = "padding-top: 19%;">
							<div id = "dataLoadDateAct"> Data Load Date</div>
							<div id = "dldRightArrow" class = "fa fa-arrow-right"></div>
						</div>
						<div id = "parentRow2" class = "parentblock">
							<div id = "plannedR2" class = "plannedBlockDesc">Planned</div>
							<div id  = "completedR2" class = "completedBlockDesc">Completed</div>
						</div>
						<div id = "parentRow3" class = "parentblock">
							<div id = "plannedR3" class = "plannedBlockDesc">Planned</div>
							<div id  = "completedR3" class = "completedBlockDesc">Completed</div>
						</div>
					</div>
					<!-- default bloack 2-->
					<!-- default bloack 3-->
					<div id= "actualDataContainer">
					</div>
					<!-- default bloack 3-->
				<!-- basic desc block for actual and completed-->
				</div>


				<!--Data Load Stages : Status Summary-->
				<div onclick = "expandCollapseStageSummary(this)" id = "stageSummaryAccordianParent" class = "collapsed">
					<div id = "stageSummaryText">Data Load Stages : Status Summary</div>
					<div class = "fa fa-chevron-right arrowStyle" ></div>
				</div>
				<!--stage summary container-->
				<div id="stageSummaryAccordianDetailsParent" style = "display:none;">
					<!--buttons-->
					<div id = "headingButtonContainer">
						<div class = "arrowHeaderSS" onclick="sourceSystemOnClick(this)">SOURCE SYSTEMS</div>
						<div class = "arrowHeaderDR" onclick="drOnClick(this)" >DATA REPOSITORY</div>
						<div class = "arrowHeaderTg" onclick="dcOnClick(this)">DATA CONSUMERS</div>
					
					</div>
					<!--buttons-->
					
					<!--details parent-->
					<div id = "stagSummaryDetailContainer">
						<div id = "srcCont">
						<!--src details-->
							<fieldset id = "srcDataLoadTasks">
								<legend class = "srcDataLoadTaskLegend"><b style  = "font-size:17px !important;margin-right:2%;">165</b> DATA LOAD TASKS
									<div class = "customLegendGear"></div>
								</legend>
								<div id = "srcDataLoadTaskStats">
									<div class = "cbdStatusFor4Block" onclick = "navigateToSourceSystemDetails(this,'DATALOADTASKS')">
										<div class = "completedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Completed</div>
									</div>
									<div class = "cbdStatusFor4Block" onclick = "navigateToSourceSystemDetails(this,'DATALOADTASKS')">
										<div class = "pendingForStage" id = "pendingNum">0</div>
										<div class = "statusTextForStage">Not Started</div>
									</div>
									<div class = "cbdStatusFor4Block" onclick = "navigateToSourceSystemDetails(this,'DATALOADTASKS')">
										<div class = "failedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Failed</div>
									</div>
									<div class = "cbdStatusFor4Block" onclick = "navigateToSourceSystemDetails(this,'DATALOADTASKS')">
										<div class = "overdueForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Overdue</div>
									</div>
								</div>
							</fieldset>
							
							<fieldset id = "srcEntities">
								<legend class = "srcEntitiesLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> SOURCE ENTITIES
									<div class = "  customLegendEntites"></div>
								</legend>
								<div class = "cbdStatusFor3Block" onclick = "navigateToSourceSystemDetails(this,'SOURCEENTITIES')">
									<div class = "completedForStage" id = "failedNum">0</div>
									<div class = "statusTextForStage">Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" onclick = "navigateToSourceSystemDetails(this,'SOURCEENTITIES')">
									<div class = "partiallyForStage" id = "partialNum">0</div>
									<div class = "statusTextForStage">Partially&#10;Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" onclick = "navigateToSourceSystemDetails(this,'SOURCEENTITIES')">
									<div class = "pendingForStage" id = "pendingNum">0</div>
									<div class = "statusTextForStage">Pending</div>
								</div>
							</fieldset>
							
							<fieldset id = "srcSystems">
								<legend class = "srcSystemsLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> SOURCE SYSTEMS
									<div class = "customLegendDb"></div>
								</legend>
								<div class = "cbdStatusFor3Block" onclick = "navigateToSourceSystemDetails(this,'SOURCESYSTEMS')">
									<div class = "completedForStage" id = "failedNum">0</div>
									<div class = "statusTextForStage">Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" onclick = "navigateToSourceSystemDetails(this,'SOURCESYSTEMS')">
									<div class = "partiallyForStage" id = "partialNum">0</div>
									<div class = "statusTextForStage">Partially&#10;Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" onclick = "navigateToSourceSystemDetails(this,'SOURCESYSTEMS')">
									<div class = "pendingForStage" id = "pendingNum">0</div>
									<div class = "statusTextForStage">Pending</div>
								</div>
							</fieldset>
							
							
						<!--src details-->
						
						</div>
						<div id = "dataRepoCont">
						<!--datarepo details-->
							<fieldset id = "dataRepoDataLoadTasks">
								<legend class = "dataRepoDataLoadTaskLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> DATA LOAD TASKS
									<div class = "customLegendGear2"></div>
								</legend>
									<div class = "cbdStatusFor6Block" style  = "pointer-events:none;">
										<div class = "plannedForStage" style = "width: 50%;height: 100%;float: left;padding-top: 6px;color:#2e75b6;" id = "failedNum">0</div>
										<div class = "statusTextForStage" style = "width: 50%;height: 100%;float: left;padding-top: 7px;">Planned&#10;Completion</div>
									</div>
									<div class = "cbdStatusFor6Block" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYTASKS')">
										<div class = "completedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Completed</div>
									</div>
									<div class = "cbdStatusFor6Block" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYTASKS')">
										<div class = "pendingForStage" id = "pendingNum">0</div>
										<div class = "statusTextForStage">Pending&#10;Due Today</div>
									</div>
									<div class = "cbdStatusFor6Block" style =" background: #f7f7f7;" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYTASKS')">
										<div class = "notYetForStage" style = "width: 50%;height: 100%;float: left;padding-top: 6px;" id = "notDueNum">0</div>
										<div class = "statusTextForStage" style = "width: 50%;height: 100%;float: left;padding-top: 7px;">Not Due&#10;Yet</div>
									</div>
									<div class = "cbdStatusFor6Block" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYTASKS')">
										<div class = "failedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Failed</div>
									</div>
									<div class = "cbdStatusFor6Block" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYTASKS')">
										<div class = "overdueForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Overdue</div>
									</div>
							</fieldset>
							
							<fieldset id = "dataRepoEntities">
								<legend class = "dataRepoEntitiesLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> REPOSITORY ENTITIES
									<div class = "  customLegendEntites2"></div>
								</legend>
								<div class = "cbdStatusFor3Block" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYENTITIES')">
									<div class = "completedForStage" id = "failedNum">0</div>
									<div class = "statusTextForStage">Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYENTITIES')">
									<div class = "partiallyForStage" id = "partialNum">0</div>
									<div class = "statusTextForStage">Partially&#10;Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" onclick = "navigateToDataRepositoryDetails(this,'DATAREPOSITORYENTITIES')">
									<div class = "pendingForStage" id = "pendingNum">0</div>
									<div class = "statusTextForStage">Pending</div>
								</div>
							</fieldset>
							
							<fieldset id = "dataRepoSystems">
								<legend class = "dataRepoSystemsLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> DATA REPOSITORY
									<div class = "customLegendDbRepo"></div>
								</legend>
								<div class = "headingRepoParent">
									<div class = "headingRepo">Entities Load&#10;Completed</div>
									<div class = "headingRepo">Data Load Tasks&#10;Completed</div>
								</div>
								<div id = "RepoResultsCont">
									
								</div>
							
							</fieldset>
							
						
						
						
						<!--datarepo details-->
						
						</div>
						
						<div id = "tgtCont">
						<!--tgt details-->
						<!--<fieldset class = "tgtSolutionParent">
							<legend class = "tgtSolutionLegend">ADF</legend>
							<fieldset class = "tgtTypeParent">
								<legend class = "tgtTypeLengend"><b style  = "font-size:17px !important;margin-right:2%;">28</b> REPORTS
									<div class = "fa fa-newspaper-o customLegendTgtType"></div>
								</legend>
									<div class = "cbdStatusFor4Block">
										<div class = "completedForStage" id = "failedNum">18</div>
										<div class = "statusTextForStage">Completed</div>
									</div>
									<div class = "cbdStatusFor4Block">
										<div class = "pendingForStage" id = "pendingNum">5</div>
										<div class = "statusTextForStage">Not Started</div>
									</div>
									<div class = "cbdStatusFor4Block">
										<div class = "partiallyForStage" id = "partialNum">5</div>
										<div class = "statusTextForStage">Partially&#10;Completed</div>
									</div>
									<div class = "cbdStatusFor4Block">
										<div class = "overdueForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Overdue</div>
									</div>
							</fieldset>
							<fieldset class = "tgtTypeParent">
								<legend class = "tgtTypeLengend"><b style  = "font-size:17px !important;margin-right:2%;">1</b>ASSOCIATED REPORTS
									<div class = "fa fa-paperclip customLegendTgtType"></div>
								</legend>
								<div class = "cbdStatusFor4Block">
										<div class = "completedForStage" id = "failedNum">112</div>
										<div class = "statusTextForStage">Completed</div>
									</div>
									<div class = "cbdStatusFor4Block">
										<div class = "pendingForStage" id = "pendingNum">52</div>
										<div class = "statusTextForStage">Not Started</div>
									</div>
									<div class = "cbdStatusFor4Block">
										<div class = "partiallyForStage" id = "partialNum">1</div>
										<div class = "statusTextForStage">Partially&#10;Completed</div>
									</div>
									<div class = "cbdStatusFor4Block">
										<div class = "overdueForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Overdue</div>
									</div>
							</fieldset>
						
						</fieldset>-->
						
						
						<!--tgt details-->
						</div>
					</div>
					<!--details parent-->
				<div id="sourceSystemClicked" style="margin-top: 22px;display:none">	
					<div id="sourceSystemsTabs">
						<div class="sourceSystemTab selectedTab" id="sourceSystemsSummary" onclick="sourceSystemTabClick(this)">Source System Summary</div>
						<div class="sourceSystemTab" id="dataLoadTaskDetails" onclick="dataLoadTaskDetailsFunction(this)">Data Load Task Details</div>
						<div class="sourceSystemTab" id="sourceEntityDetails" onclick="sourceEntityDetailsFunction(this)">Source Entity Details</div>
					</div>
					<fieldset  id = "srcSystems2" style="width: 25%;float: right;height: 85px;margin-top: 0px;margin-right: 2%;margin-bottom: 16px;">
								<legend style  = "margin-bottom:0px !important;font-size:17px !important;margin-right:2%;" class = "srcSystemsLegend"><b>0</b> SOURCE SYSTEMS
									<div class = "customLegendDb"></div>
								</legend>
								<div class = "cbdStatusFor3Block" >
									<div class = "completedForStage" id = "failedNum">0</div>
									<div class = "statusTextForStage">Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" >
									<div class = "partiallyForStage" id = "partialNum">0</div>
									<div class = "statusTextForStage">Partially Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" >
									<div class = "pendingForStage" id = "pendingNum">0</div>
									<div class = "statusTextForStage">Pending</div>
								</div>
					</fieldset>
					<fieldset id = "srcDataLoadTasks2" style="display:none;">
								<legend style="margin-bottom: 0px;" class = "srcDataLoadTaskLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> DATA LOAD TASKS
									<div class = "customLegendGear"></div>
								</legend>
								<div id = "srcDataLoadTaskStats">
									<div class = "cbdStatusFor4Block" >
										<div class = "completedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Completed</div>
									</div>
									<div class = "cbdStatusFor4Block" >
										<div class = "pendingForStage" id = "pendingNum">0</div>
										<div class = "statusTextForStage">Not Started</div>
									</div>
									<div class = "cbdStatusFor4Block" >
										<div class = "failedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Failed</div>
									</div>
									<div class = "cbdStatusFor4Block" >
										<div class = "overdueForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Overdue</div>
									</div>
								</div>
					</fieldset>
					<fieldset id = "srcEntities2" style="display:none; height:90px; position:relative; float:right;  width: 25%;float: right;margin-bottom: 15px;margin-top:0px;margin-right:2%;">
								<legend style="margin-bottom:0px !important;" class = "srcEntitiesLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> SOURCE ENTITIES
									<div class = "  customLegendEntites"></div>
								</legend>
								<div class = "cbdStatusFor3Block" >
									<div class = "completedForStage" id = "failedNum">0</div>
									<div class = "statusTextForStage">Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" >
									<div class = "partiallyForStage" id = "partialNum">0</div>
									<div class = "statusTextForStage">Partially&#10;Completed</div>
								</div>
								<div class = "cbdStatusFor3Block" >
									<div class = "pendingForStage" id = "pendingNum">0</div>
									<div class = "statusTextForStage">Pending</div>
								</div>
					</fieldset>
					
				
					<div style = "width:98%; clear:both;  font-family:segoe ui;border:none !important" id="grid1"></div>	
				</div>
				<!--stage  clicked end-->

				<!--dr  clicked start-->
				<div id="drClicked" style="margin-top: 22px;display:none">	
					<div id="drTabs">
						<div class="drTab selectedTab" id="drDataLoadTask" onclick="drDataLoadTaskClick(this)">Data Load Task Details</div>
						<div class="drTab" id="drRepoEntity" onclick="drRepoEntityFunction(this)">Repository Entity Details</div>
						<div class="drTab" id="drUnplannedTask" onclick="drUnplannedTaskFunction(this)">Unplanned Task Execution</div>
					</div>
					<fieldset style=" position:relative; float:right;width:40%;display:none;border: 1px solid #eee;margin-left:4%;margin-bottom: 10px;margin-right:2%;box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.25);height: 85px;" id = "dataRepoDataLoadTasks2">
								<legend style="margin-bottom:0px;" class = "dataRepoDataLoadTaskLegend"><b style  = " font-size:17px !important;margin-right:2%;">0</b> DATA LOAD TASKS
									<div class = "customLegendGear2"></div>
								</legend>
									<div class = "cbdStatusFor6Block" style="width: 13%;height: 65px;">
										<div class = "plannedForStage" style = "color:#2e75b6;" id = "failedNum">0</div>
										<div class = "statusTextForStage" style = "white-space: normal;text-align: center;">Planned&#10;Completion</div>
									</div>
									<div class = "cbdStatusFor6Block" style="width: 12%;height: 65px;">
										<div class = "completedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Completed</div>
									</div>
									<div class = "cbdStatusFor6Block" style="width: 12%;height: 65px;">
										<div class = "pendingForStage" id = "pendingNum">0</div>
										<div class = "statusTextForStage">Pending&#10;Due Today</div>
									</div>
									<div class = "cbdStatusFor6Block" style ="width: 12%;height: 65px; background: #f7f7f7;">
										<div class = "notYetForStage"  id = "notDueNum">0</div>
										<div class = "statusTextForStage" >Not Due&#10;Yet</div>
									</div>
									<div class = "cbdStatusFor6Block" style ="width: 12%;height: 65px;">
										<div class = "failedForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Failed</div>
									</div>
									<div class = "cbdStatusFor6Block" style ="width: 12%;height: 65px;">
										<div class = "overdueForStage" id = "failedNum">0</div>
										<div class = "statusTextForStage">Overdue</div>
									</div>
					</fieldset>
					<fieldset style="margin-right:2%;position:relative; float:right;width:20%;margin-bottom:15px;display:none; height:85px;" id = "dataRepoEntities2">
								<legend style="margin-bottom:0px;" class = "dataRepoEntitiesLegend"><b style  = "font-size:17px !important;margin-right:2%;">0</b> REPOSITORY ENTITIES
									<div class = "  customLegendEntites2"></div>
								</legend>
								<div class = "cbdStatusFor3Block">
									<div class = "completedForStage" id = "failedNum">0</div>
									<div class = "statusTextForStage">Completed</div>
								</div>
								<div class = "cbdStatusFor3Block">
									<div class = "partiallyForStage" id = "partialNum">0</div>
									<div class = "statusTextForStage">Partially&#10;Completed</div>
								</div>
								<div class = "cbdStatusFor3Block">
									<div class = "pendingForStage" id = "pendingNum">0</div>
									<div class = "statusTextForStage">Pending</div>
								</div>
					</fieldset>
					
				
					<div style = "width:98%; clear:both;  font-family:segoe ui;border:none !important" id="grid2"></div>	
				</div>
				<div  id="consumersClicked" >
					
					<div style = "width:98%; clear:both;  font-family:segoe ui;border:none !important;" id="grid4"></div>	
					
				</div>
				
					
			</div>
				<!--stage summary container-->	
					
				<!--Data Load Stages : Status Summary-->
				<!-- grid try out-->
				
				<!-- grid try out-->
				
			<!--main container for details-->
			</div>
			
			
	<!--data lineage contaiiner-->
		<div id = "dataLineageContainer">
		
		
				
		</div>
	<!--data lineage contaiiner-->
		</div>
	
	<!-- actual progress dld details-->
	
	
	
</div>
<iframe id="hiddenDownloader" style="display:none;"></iframe>  


<!-- main container-->





<%@ include file="footer.jsp"%>