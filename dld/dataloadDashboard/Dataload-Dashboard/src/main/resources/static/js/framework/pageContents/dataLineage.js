//graph DS
var graph = new Graph();

//layout preparation.
var entityMaxLevel = {};
entityMaxLevel["sourceSystem"]=0;
entityMaxLevel["staging"]=0;
entityMaxLevel["reception"]=0;
entityMaxLevel["datahub"]=0;
entityMaxLevel["mart"]=0;
entityMaxLevel["dataConsumer"]=0;
var distinctSourceLevels = new Set();
var distinctRecptionLevels = new Set();
var distinctStagingLevels = new Set();
var distinctDatahubLevels = new Set();
var distinctMartLevels = new Set();
var distinctDataConsumerLevels = new Set();
var appendHtml="";
var appendArray = [];
var counter=0;
//map of  sort order and type.
var typeAndSortMap = {}

//map contains for each	entity and its type - SS,dataRepo,mart,DC		  
var entityType = {};
 
//entity actual map in order to handle multiple marts.
var entityActualType = {};

//entity and owner map.
var entityDSMap = {};

//entity solution map
 var entitySolMap = {};
 
 //Task Status map
 var taskStatusMap={};
 
//distinct data source 
var distinctSourceName = new Set();

//distinct solutions
var distinctSolutionName = new Set();

//distinct marts.
var distinctMartsType = new Set();

//distinct staging.
var distinctStagingType = new Set();

//hexx values of entity names.
var hexor={};
var hexToName = {};

//SOURCE TARGET AND TASKnAME PAIR
var sourceTargetTaskMap = {};

//entity n desc map.
var entityDescMap = {};

//pending child array
var pendingHighlightArray = [];


//pending child array
var pendingParentHighlightArray = [];


var levelMap;
var edges;

//nodes to parent
var nameTypeObjArrayForParent = [];

//highlightState maintain
var clickedNodeId = [];

//status object

var entityStatusMap = {};

//source entity position and dimension property maps
var srcLeftPostionMap = {};
var srcTopPostionMap = {};
var srcHeightMap = {};
var srcWidthMap = {};

//destination entity position and dimension property maps
var tgtLeftPostionMap = {};
var tgtTopPostionMap = {};
var tgtHeightMap = {};
var tgtWidthMap = {};


(function () {
	'use strict';

	function ToInteger(value) {
		var number = Number(value);
		return sign(number) * Math.floor(Math.abs(Math.min(Math.max(number || 0, 0), 9007199254740991)));
	}

	var has = Object.prototype.hasOwnProperty;
	var strValue = String.prototype.valueOf;

	var tryStringObject = function tryStringObject(value) {
		try {
			strValue.call(value);
			return true;
		} catch (e) {
			return false;
		}
	};

	function sign(number) {
		return number >= 0 ? 1 : -1;
	}

	var toStr = Object.prototype.toString;
	var strClass = '[object String]';
	var hasSymbols = typeof Symbol === 'function';
	var hasToStringTag = hasSymbols && 'toStringTag' in Symbol;

	function isString(value) {
		if (typeof value === 'string') {
			return true;
		}
		if (typeof value !== 'object') {
			return false;
		}
		return hasToStringTag ? tryStringObject(value) : toStr.call(value) === strClass;
	}

	var fnToStr = Function.prototype.toString;

	var constructorRegex = /^\s*class /;
	var isES6ClassFn = function isES6ClassFn(value) {
		try {
			var fnStr = fnToStr.call(value);
			var singleStripped = fnStr.replace(/\/\/.*\n/g, '');
			var multiStripped = singleStripped.replace(/\/\*[.\s\S]*\*\//g, '');
			var spaceStripped = multiStripped.replace(/\n/mg, ' ').replace(/ {2}/g, ' ');
			return constructorRegex.test(spaceStripped);
		} catch (e) {
			return false; // not a function
		}
	};

	var tryFunctionObject = function tryFunctionObject(value) {
		try {
			if (isES6ClassFn(value)) {
				return false;
			}
			fnToStr.call(value);
			return true;
		} catch (e) {
			return false;
		}
	};
	var fnClass = '[object Function]';
	var genClass = '[object GeneratorFunction]';

	function isCallable(value) {
		if (!value) {
			return false;
		}
		if (typeof value !== 'function' && typeof value !== 'object') {
			return false;
		}
		if (hasToStringTag) {
			return tryFunctionObject(value);
		}
		if (isES6ClassFn(value)) {
			return false;
		}
		var strClass = toStr.call(value);
		return strClass === fnClass || strClass === genClass;
	};
	var isArray = Array.isArray;

	var parseIterable = function (iterator) {
		var done = false;
		var iterableResponse;
		var tempArray = [];

		if (iterator && typeof iterator.next === 'function') {
			while (!done) {
				iterableResponse = iterator.next();
				if (
					has.call(iterableResponse, 'value') &&
					has.call(iterableResponse, 'done')
				) {
					if (iterableResponse.done === true) {
						done = true;
						break; // eslint-disable-line no-restricted-syntax

					} else if (iterableResponse.done !== false) {
						break; // eslint-disable-line no-restricted-syntax
					}

					tempArray.push(iterableResponse.value);
				} else if (iterableResponse.done === true) {
					done = true;
					break; // eslint-disable-line no-restricted-syntax
				} else {
					break; // eslint-disable-line no-restricted-syntax
				}
			}
		}

		return done ? tempArray : false;
	};

	var iteratorSymbol;
	var forOf;
	var hasSet = typeof Set === 'function';
	var hasMap = typeof Map === 'function';

	if (hasSymbols) {
		iteratorSymbol = Symbol.iterator;
	} else {
		var iterate;
		try {
			iterate = Function('iterable', 'var arr = []; for (var value of iterable) arr.push(value); return arr;'); // eslint-disable-line no-new-func
		} catch (e) {}
		var supportsStrIterator = (function () {
			try {
				var supported = false;
				var obj = { // eslint-disable-line no-unused-vars
					'@@iterator': function () {
						return {
							'next': function () {
								supported = true;
								return {
									'done': true,
									'value': undefined
								};
							}
						};
					}
				};

				iterate(obj);
				return supported;
			} catch (e) {
				return false;
			}
		}());

		if (supportsStrIterator) {
			iteratorSymbol = '@@iterator';
		} else if (typeof Set === 'function') {
			var s = new Set();
			s.add(0);
			try {
				if (iterate(s).length === 1) {
					forOf = iterate;
				}
			} catch (e) {}
		}
	}

	var isSet;
	if (hasSet) {
		var setSize = Object.getOwnPropertyDescriptor(Set.prototype, 'size').get;
		isSet = function (set) {
			try {
				setSize.call(set);
				return true;
			} catch (e) {
				return false;
			}
		};
	}

	var isMap;
	if (hasMap) {
		var mapSize = Object.getOwnPropertyDescriptor(Map.prototype, 'size').get;
		isMap = function (m) {
			try {
				mapSize.call(m);
				return true;
			} catch (e) {
				return false;
			}
		};
	}

	var setForEach = hasSet && Set.prototype.forEach;
	var mapForEach = hasMap && Map.prototype.forEach;
	var usingIterator = function (items) {
		var tempArray = [];
		if (has.call(items, iteratorSymbol)) {
			return items[iteratorSymbol]();
		} else if (setForEach && isSet(items)) {
			setForEach.call(items, function (val) {
				tempArray.push(val);
			});
			return {
				next: function () {
					return tempArray.length === 0
						? {
							done: true
						}
						: {
							value: tempArray.splice(0, 1)[0],
							done: false
						};
				}
			};
		} else if (mapForEach && isMap(items)) {
			mapForEach.call(items, function (val, key) {
				tempArray.push([key, val]);
			});
			return {
				next: function () {
					return tempArray.length === 0
						? {
							done: true
						}
						: {
							value: tempArray.splice(0, 1)[0],
							done: false
						};
				}
			};
		}
		return items;
	};

	var strMatch = String.prototype.match;

	var parseIterableLike = function (items) {
		var arr = parseIterable(usingIterator(items));

		if (!arr) {
			if (isString(items)) {
				arr = strMatch.call(items, /[\uD800-\uDBFF][\uDC00-\uDFFF]?|[^\uD800-\uDFFF]|./g) || [];
			} else if (forOf && !isArray(items)) {
				// Safari 8's native Map or Set can't be iterated except with for..of
				try {
					arr = forOf(items);
				} catch (e) {}
			}
		}
		return arr || items;
	};

	/*! https://mths.be/array-from v0.2.0 by @mathias */
	Object.defineProperty(Array, 'from', {
		configurable: true,
		value: function from(items) {
			var C = this;
			if (items === null || typeof items === 'undefined') {
				throw new TypeError('`Array.from` requires an array-like object, not `null` or `undefined`');
			}
			var mapFn, T;
			if (typeof arguments[1] !== 'undefined') {
				mapFn = arguments[1];
				if (!isCallable(mapFn)) {
					throw new TypeError('When provided, the second argument to `Array.from` must be a function');
				}
				if (arguments.length > 2) {
					T = arguments[2];
				}
			}

			var arrayLike = Object(parseIterableLike(items));
			var len = ToInteger(arrayLike.length);
			var A = isCallable(C) ? Object(new C(len)) : new Array(len);
			var k = 0;
			var kValue, mappedValue;

			while (k < len) {
				kValue = arrayLike[k];
				if (mapFn) {
					mappedValue = typeof T === 'undefined' ? mapFn(kValue, k) : mapFn.apply(T, [kValue, k]);
				} else {
					mappedValue = kValue;
				}
				Object.defineProperty(A, k, {
					'configurable': true,
					'enumerable': true,
					'value': mappedValue,
					'writable': true
				});
				k += 1;
			}
			A.length = len;
			return A;
		},
		writable: true
	});
}());

//ajax loader.
$(document).ajaxStart(function(){
	window.parent.$(".overlay").css("display","block");
});
$(document).ajaxStop(function(){
	
	if(!(window.parent.$(".overlay").hasClass("fakeLoader"))){
		window.parent.$(".overlay").css("display","none");
	}
		
});


$(document).ready(function(){
		 
});


$.ajax({
    url: "/dldwebapplication/getDataForlineage?cbd="+cbd+"&clientCode="+clientCode+"",
    type: 'GET',
    success: function(data){ 
        for(var i=0;i<data.entities.length;i++){
			var hexCode = (new Math.seedrandom(data.entities[i].entityName+data.entities[i].entityOwner)).int32();
			var entityName = data.entities[i].entityName;
			var entityOwner = data.entities[i].entityOwner;
			hexor[entityName+entityOwner]=hexCode;
			hexToName[hexCode] = entityName;
			graph.addVertex(hexCode);
			entityDescMap[hexCode] = data.entities[i].entityDesc;
		}
		
		for(var i=0;i<data.displayOrders.length;i++){
			var ownerNameRec = (data.displayOrders[i].ownerName).toUpperCase();
			if(ownerNameRec == "DATAHUB" || ownerNameRec =="DATA HUB"){
				ownerNameRec = "DATAHUB"
			}
			typeAndSortMap[ownerNameRec] = Number(data.displayOrders[i].displayOrder);
		}
		prepareDataForLayout(data);
		checkForButtonDisplay();
		window.parent.$(".overlay").addClass("fakeLoader");
    },
    error: function(data) {
        alert('Something went wrong'); //or whatever
    }
});
	
function prepareDataForLayout(data){
	
	//preparing maps for all the processing.
	
	for(var i=0;i<data.entities.length;i++){
		var nameTypeObj = {};
		var entityName = data.entities[i].entityName;
		var entityOwner = data.entities[i].entityOwner;
		if((data.entities[i].isSourceSystem).toUpperCase()=='Y'){
			entityType[hexor[entityName+entityOwner]] = "sourceSystem";
			entityActualType[hexor[entityName+entityOwner]] = "sourceSystem";
			nameTypeObj.entityName = entityName;
			nameTypeObj.entityOwner = entityOwner;
			nameTypeObj.entityType = "sourceSystem";
			entityDSMap[hexor[entityName+entityOwner]] = data.entities[i].entityOwner;
			distinctSourceName.add(data.entities[i].entityOwner);
		}else if((data.entities[i].isStageArea).toUpperCase()=='Y' ){
			entityType[hexor[entityName+entityOwner]] = "staging";
			entityActualType[hexor[entityName+entityOwner]] = data.entities[i].entityOwner;
			distinctStagingType.add(data.entities[i].entityOwner);
			nameTypeObj.entityOwner = entityOwner;
			nameTypeObj.entityName = entityName;
			nameTypeObj.entityType = "dataRepository";
		}else if((data.entities[i].isDataRepo).toUpperCase()=='Y' && (data.entities[i].entityOwner).toUpperCase()=='RECEPTION'){
			entityType[hexor[entityName+entityOwner]] = "reception";
			entityActualType[hexor[entityName+entityOwner]] = "reception";
			nameTypeObj.entityType =  "dataRepository";
			nameTypeObj.entityName = entityName;
			nameTypeObj.entityOwner = entityOwner;
		}else if((data.entities[i].isDataRepo).toUpperCase()=='Y' && ((data.entities[i].entityOwner).toUpperCase()=='DATAHUB')||((data.entities[i].entityOwner).toUpperCase()=='DATA HUB')){
			entityType[hexor[entityName+entityOwner]] = "datahub";
			entityActualType[hexor[entityName+entityOwner]] = "datahub";
			nameTypeObj.entityType = "dataRepository";
			nameTypeObj.entityName = entityName;
			nameTypeObj.entityOwner = entityOwner;
		}else if((data.entities[i].isDataRepo).toUpperCase()=='Y' && (data.entities[i].entityOwner).toUpperCase()!='RECEPTION' && ((data.entities[i].entityOwner).toUpperCase()!='DATAHUB'||(data.entities[i].entityOwner).toUpperCase()!='DATA HUB')){
			entityType[hexor[entityName+entityOwner]] = "mart";
			nameTypeObj.entityType = "dataRepository";
			entityActualType[hexor[entityName+entityOwner]] = data.entities[i].entityOwner;
			distinctMartsType.add(data.entities[i].entityOwner);
			nameTypeObj.entityOwner = entityOwner;
			nameTypeObj.entityName = entityName;
		}else if((data.entities[i].isDataConsumer).toUpperCase()=='Y'){
			entityType[hexor[entityName+entityOwner]] = "dataConsumer";
			entityActualType[hexor[entityName+entityOwner]] = "dataConsumer";
			entitySolMap[hexor[entityName+entityOwner]] = data.entities[i].entityOwner;
			distinctSolutionName.add(data.entities[i].entityOwner);
			nameTypeObj.entityType = "dataConsumer";
			nameTypeObj.entityName = entityName;
			nameTypeObj.entityOwner = entityOwner;
			if(data.entities[i].isLineItemDataRequired=='Y'){
				var hexIdOfOpt = (new Math.seedrandom(entityName+'('+entityOwner+')'+'opt')).int32();
				if(window.parent.$("#"+hexIdOfOpt).length==0){
					window.parent.$("#reportWithLineItem").append('<option id = "'+hexIdOfOpt+'" solutionName = "'+entityOwner+'" reportId = "'+data.entities[i].reportId+'" value="'+entityName+'('+entityOwner+')'+'"> '+entityName+'('+entityOwner+')'+'</option>');
				}
				
			}
			
		}
		nameTypeObjArrayForParent.push(nameTypeObj);
	}
	
	//preparing connections.
	for(var i=0;i<data.task.length;i++){
		var sourceName = data.task[i].SOURCE;
		
		var sourceOwner = data.task[i].SOURCEOWNER;
		var targetName = data.task[i].TARGET;
		var targetOwner = data.task[i].TARGETOWNER;
		var taskName = data.task[i].taskName;
		var taskStatus =data.task[i].STATUS;
		taskStatusMap[taskName]=taskStatus
		var sourceHexCode = hexor[sourceName+sourceOwner];
		var targetHexCode = hexor[targetName+targetOwner];
		if(sourceHexCode!=undefined && targetHexCode!=undefined){
			sourceTargetTaskMap[sourceName+"~~||~~"+targetName] = taskName;
			graph.addEdge(sourceHexCode,targetHexCode);
		}
		if(entityStatusMap[targetHexCode]===undefined){
			entityStatusMap[targetHexCode] = taskStatus;
		}else {
			
			checkStatusPriority(targetHexCode,taskStatus==undefined?"NOT APLLICABLE":taskStatus);
		}
	}
	
	//loop find max level in each type.
	var a=graph.vertexLevelMap;
	a.forEach(function (v,k){
	   if(entityType[k]=="sourceSystem"){
				if(a.get(k)>entityMaxLevel["sourceSystem"])
					  entityMaxLevel["sourceSystem"]=a.get(k);
	   }else if(entityType[k]=="staging"){
					  if(a.get(k)>entityMaxLevel["staging"])
						entityMaxLevel["staging"]=a.get(k);
	   }else if(entityType[k]=="reception"){
					  if(a.get(k)>entityMaxLevel["reception"])
						entityMaxLevel["reception"]=a.get(k);
	   }else if(entityType[k]=="datahub"){
					  if(a.get(k)>entityMaxLevel["datahub"])
						entityMaxLevel["datahub"]=a.get(k);
	   }else if(entityType[k]=="mart"){
					   if(a.get(k)>entityMaxLevel["mart"])
						entityMaxLevel["mart"]=a.get(k);
	   }else if(entityType[k]=="dataConsumer"){
					  if(a.get(k)>entityMaxLevel["dataConsumer"])
						entityMaxLevel["dataConsumer"]=a.get(k);
	   }
	});
					
	//processing to finalize all levels
	
	levelMap=finalizeLevels(a);
	if(!graph.validateLevels()){
		graph.calcLevelsForNodes();
	}
	
	edges = graph.edges;
	generateBaseLayout();
	passNodesToParent();
	//iframe size to the layout size.
	parent.document.getElementsByTagName("IFRAME")[0].style.height =$("#main").height()+200;
}

function finalizeLevels(levelMap1){
		levelMap1.forEach(function (v,k){
			if(entityType[k]=="staging"){
				validateAndUpdateLevels("sourceSystem","staging",k,v,levelMap1);
			}else if(entityType[k]=="reception"){
				validateAndUpdateLevels("sourceSystem","reception",k,v,levelMap1);
				validateAndUpdateLevels("staging","reception",k,v,levelMap1);
			}else if(entityType[k]=="datahub"){
				validateAndUpdateLevels("reception","datahub",k,v,levelMap1);
			}else if(entityType[k]=="mart"){
				validateAndUpdateLevels("datahub","mart",k,v,levelMap1);
			}
		});
		
		//processParent();
		return levelMap1;
}

function validateAndUpdateLevels(prevType,currentType,objectName,curVal,levelMap){
	if(levelMap.get(objectName)<=entityMaxLevel[prevType]){
		if(curVal>1){
			levelMap.set(objectName,entityMaxLevel[prevType]+curVal);
		}else{
			levelMap.set(objectName,entityMaxLevel[prevType]+1);
			
		}
		
	}
}


function generateBaseLayout(){
	processParent();
	processMarts();
	processStaging();
	sortTheDivsBasedOnSortOrder();
	if($(".levelContainers").length==2){
	$("#main").css("width",$(".levelContainers").length*$(".levelContainers").width()+300)
	}else{
	$("#main").css("width",$(".levelContainers").length*$(".levelContainers").width()+150);
	}
	setTimeout(function(){
	drawConnectionsMethod();
	checkForEntityClick();
	},0);
	
	
}				

function processParent(){
	levelMap.forEach(function (v,k){
	   if(entityType[k]=="sourceSystem"){   
					  distinctSourceLevels.add(v);
	   }else if(entityType[k]=="staging"){
					  distinctStagingLevels.add(v);
	   }else if(entityType[k]=="reception"){
					  distinctRecptionLevels.add(v);
	   }else if(entityType[k]=="datahub"){
					  distinctDatahubLevels.add(v);
	   }else if(entityType[k]=="mart"){
					  distinctMartLevels.add(v);
	   }else if(entityType[k]=="dataConsumer"){
					  distinctDataConsumerLevels.add(v);
	   }
	});
	 
	prepareLayOut(distinctSourceLevels);
}					
				
function processMarts(){
	var sortedArrayMart  = Array.from(distinctMartsType).sort(function(a, b){return a - b});
	for(var m = 0;m<sortedArrayMart.length;m++){
		 var sortedArray =  Array.from(distinctMartLevels).sort(function(a, b){return a - b});
		 var hexOwner =  new Math.seedrandom(sortedArrayMart[m]).int32();
		if(sortedArray.length>0){
			//appending containeer only entities prewsent.
				$(".dataRepoParent").append('<div sortOrder = '+typeAndSortMap[sortedArrayMart[m].toUpperCase()]+' class= "martParent '+hexOwner+'Parent expanded" id = "'+hexOwner+'" > <div onclick = "collapseExpand(this);" class= "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;"><span class = "headingTypeTextSpan" title = "'+sortedArrayMart[m]+'">'+sortedArrayMart[m]+'</span><span class= "fa fa-arrow-circle-left expandCollapseImg"></span></div></div>')
		}
		for(var  i= 0;i<sortedArray.length;i++){
			$("."+hexOwner+"Parent").append("<div class='martLevelStyle levelContainers' sublevel="+sortedArray[i]+" id='martLevel"+hexOwner+sortedArray[i]+"'></div>");
		}
	}
	placeInRespectiveMarts();
	  
	updateContainers();
}
function processStaging(){
	var sortedArrayStaging  = Array.from(distinctStagingType).sort(function(a, b){return a - b});
	for(var m = 0;m<sortedArrayStaging.length;m++){
		 var sortedArray =  Array.from(distinctStagingLevels).sort(function(a, b){return a - b});
		 var hexOwner =  new Math.seedrandom(sortedArrayStaging[m]).int32();
		if(sortedArray.length>0){
			//appending containeer only entities prewsent.
				$('<div sortOrder = '+typeAndSortMap[sortedArrayStaging[m].toUpperCase()]+' class= "stagingParent '+hexOwner+'Parent expanded" id = "'+hexOwner+'" > <div onclick = "collapseExpand(this);" class= "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;"><span class = "headingTypeTextSpan" title = "'+sortedArrayStaging[m]+'">'+sortedArrayStaging[m]+'</span><span class= "fa fa-arrow-circle-left expandCollapseImg"></span></div></div>').insertAfter($("#dataRepoText"));
		}
		for(var  i= 0;i<sortedArray.length;i++){
			$("."+hexOwner+"Parent").append("<div class='stagingLevelStyle levelContainers' sublevel="+sortedArray[i]+" id='stagingLevel"+hexOwner+sortedArray[i]+"'></div>");
		}
	}
	placeInRespectiveStaging();
	  
	updateContainers();
}					

function prepareLayOut(distinctSourceLevels){
	var sortedArray  = Array.from(distinctSourceName).sort(function(a, b){return a - b}); 
	for(var i = 0;i<sortedArray.length;i++){
		var hexOwner =  new Math.seedrandom(sortedArray[i]).int32();
		$(".sourceSystemParent").append('<div class= "sourceNameParent expanded" id ="'+hexOwner+'Parent" > <div onclick = "collapseExpandForSourceSystem(this);" class= "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;"><span class = "headingTypeTextSpan" title = "'+sortedArray[i]+'">'+sortedArray[i]+'</span><span class= "fa fa-arrow-circle-up expandCollapseImg"></span></div><div class="sourceSystemLevelStyle" id="sourceSystem'+hexOwner+'"></div></div>')
	}
	/*for(var  i= 0;i<sortedArray.length;i++){
	$(".sourceSystemParent").append("<div class='sourceSystemLevelStyle levelContainers' id='sourceSystemLevel"+sortedArray[i]+"'></div>");
	}*/


	sortedArray  = Array.from(distinctStagingLevels).sort(function(a, b){return a - b});
	if(sortedArray.length>0){
	//appending containeer only entities prewsent.
		$(".dataRepoParent").append('<div class= "stagingParent defaultStaging expanded" > <div onclick = "collapseExpand(this);" class= "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;">Staging<span class= "fa fa-arrow-circle-left expandCollapseImg"></span></div></div>')
	}

	for(var  i= 0;i<sortedArray.length;i++){
		$(".stagingParent").append("<div class='receptionLevelStyle levelContainers' id='stagingLevel"+sortedArray[i]+"'></div>");
	}
	sortedArray  = Array.from(distinctRecptionLevels).sort(function(a, b){return a - b});
	if(sortedArray.length>0){
	//appending containeer only entities prewsent.
		$(".dataRepoParent").append('<div sortOrder = '+typeAndSortMap["RECEPTION"]+' class= "receptionParent expanded" > <div onclick = "collapseExpand(this);" class= "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;">Reception<span class= "fa fa-arrow-circle-left expandCollapseImg"></span></div></div>')
	}

	for(var  i= 0;i<sortedArray.length;i++){
		$(".receptionParent").append("<div class='receptionLevelStyle levelContainers' sublevel="+sortedArray[i]+" id='receptionLevel"+sortedArray[i]+"'></div>");
	}
	sortedArray  = Array.from(distinctDatahubLevels).sort(function(a, b){return a - b}); 
	if(sortedArray.length>0){
	//appending containeer only entities prewsent.
		$(".dataRepoParent").append('<div sortOrder = '+typeAndSortMap["DATAHUB"]+' class= "datahubParent expanded" > <div onclick = "collapseExpand(this);" class = "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;">Datahub<span class= "fa fa-arrow-circle-left expandCollapseImg"></span></div></div>')
	}
	for(var  i= 0;i<sortedArray.length;i++){
		$(".datahubParent").append("<div class='datahubLevelStyle levelContainers' sublevel="+sortedArray[i]+" id='datahubLevel"+sortedArray[i]+"'></div>");
	}
	sortedArray  = Array.from(distinctMartLevels).sort(function(a, b){return a - b}); 
	if(sortedArray.length>0){
	//appending containeer only entities prewsent.
		$(".dataRepoParent").append('<div class= "martParent defaultMart expanded" > <div onclick = "collapseExpand(this);" class= "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;">Marts<span class= "fa fa-arrow-circle-left expandCollapseImg"></span></div></div>')
	}
	for(var  i= 0;i<sortedArray.length;i++){
		$(".martParent").append("<div class='martLevelStyle levelContainers' sublevel="+sortedArray[i]+" id='martLevel"+sortedArray[i]+"'></div>");
	}

	//data consumers parent logic.
	var sortedArray  = Array.from(distinctSolutionName).sort(function(a, b){return a - b}); 
	for(var i = 0;i<sortedArray.length;i++){
		var hexOwner =  new Math.seedrandom(sortedArray[i]).int32();
		$(".dataConsumerParent").append('<div class= "solutionNameParent expanded" id ="'+hexOwner+'Parent" > <div onclick = "collapseExpandForSolution(this);" class= "typeText" style="width:100%;height:35px;text-align: center;font-weight:bold;font-family:Segoe UI;    line-height: 2;"><span class="fa fa-arrow-right arrowInputDC" style="float: left;margin-top: 10px;display:none;"></span><span  title = "'+sortedArray[i]+'" class = "headingTypeTextSpan">'+sortedArray[i]+'</span><span class= "fa fa-arrow-circle-up expandCollapseImg"></span></div><div class="solutionLevelStyle" id="dataConsumer'+hexOwner+'"></div></div>')
	}

	//$(".dataConsumerParent").append("<div class='dataConsumerLevelStyle levelContainers' id='dataConsumerLevel"+0+"'></div>");

	appendBlocks();

}				
              
function appendBlocks(){
	levelMap.forEach(function (v,k){
		var actualName = hexToName[k];
		var entityStat = "NOT APPLICABLE";
		if(entityStatusMap[k]!=undefined){
			entityStat = entityStatusMap[k].toUpperCase();
		}
		
		if(entityType[k]=="dataConsumer"){
			
			$("#"+entityType[k]+""+(new Math.seedrandom(entitySolMap[k]).int32())).append('<div entityCatagory = "'+entityType[k]+'" id = "node_'+k+'" class = "'+entityType[k]+'Slab nodes shownNode"><span class = "fa fa-arrow-right arrowInput toolTip_'+k+'"></span><div style = "width: 100%;height: 30px;" class ="headingSlab" onclick = "hightlightTheLinkedItems(this,\''+k+'\',\''+entityType[k]+'\');"><div id = "'+k+'Start" class = ></div><div class ="fa fa-newspaper-o-s elementImageStyle"></div><div   title ="'+actualName+'" class = "elementName">'+actualName+'</div><div onClick="infoClick(this,event);" class = "infoImage fa fa-info-circle '+entityStat.split(" ").join("")+'"></div><div id = "'+k+'End" class = "connDiv fa"></div>'
			+'</div><div class = "dataConsumerDescription descriptionParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title = "'+entityDescMap[k]+'" onmouseover = "showHideDataFlow(this,\'consumer\')"> '+entityDescMap[k]+'</div><div class = "flowShowParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" onmouseleave = "showHideDescription(this)"> <div class = "fa fa-long-arrow-left lienageFlowBack" onclick = "showBackwardFlow(this)" title = "Show Backward flow"></div></div></div>'
			+'</div>');
		}else if(entityType[k]=="sourceSystem"){
			$("#"+entityType[k]+""+(new Math.seedrandom(entityDSMap[k]).int32())).append('<div entityCatagory = "'+entityType[k]+'" id = "node_'+k+'" class = "'+entityType[k]+'Slab nodes shownNode"><div style = "width: 100%;height: 30px;" class ="headingSlab" onclick = "hightlightTheLinkedItems(this,\''+k+'\',\''+entityType[k]+'\');"><div id = "'+k+'Start" class = ></div><div class ="fa fa-database-s elementImageStyle"></div><div   title ="'+actualName+'" class = "elementName">'+actualName+'</div><div  class = "infoImage fa fa-info-circle '+entityStat.split(" ").join("")+'"></div><div id = "'+k+'End" class = "connDiv fa"></div>'
			+'</div><div class = "sourceDescription descriptionParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title = "'+entityDescMap[k]+'" onmouseover = "showHideDataFlow(this,\'source\')">'+entityDescMap[k]+'</div>'+'<div class = "flowShowParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"  onmouseleave = "showHideDescription(this)"> <div class = "fa fa-long-arrow-right lienageFlowFWD" onclick = "showForwardFlow(this)" title = "Show Forward flow"></div></div></div>'
			+'</div>');

		}else{ 
			$("#"+entityType[k]+"Level"+v).append('<div entityCatagory = "'+entityType[k]+'" id = "node_'+k+'" class = "'+entityType[k]+'Slab nodes shownNode"><span class = "fa fa-arrow-right arrowInput toolTip_'+k+'"></span><div style = "width: 100%;height: 30px;" class ="headingSlab" onclick = "hightlightTheLinkedItems(this,\''+k+'\',\''+entityType[k]+'\');"><div id = "'+k+'Start" class = ></div><div class ="fa fa-database elementImageStyle"></div><div   title ="'+actualName+'" class = "elementName">'+actualName+'</div><div onClick="infoClick(this,event)";  class = "infoImage fa fa-info-circle '+entityStat.split(" ").join("")+'"></div><div id = "'+k+'End" class = "connDiv fa"></div>'
			+'</div><div class = "'+entityType[k]+'Description descriptionParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title = "'+entityDescMap[k]+'" onmouseover = "showHideDataFlow(this,\'other\')">'+entityDescMap[k]+'</div>'+'<div class = "flowShowParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" onmouseleave = "showHideDescription(this)"> <div class = "fa fa-long-arrow-left lienageFlowBack" onclick = "showBackwardFlow(this)" title = "Show Backward flow"></div><div class = "fa fa-long-arrow-right lienageFlowFWD" onclick = "showForwardFlow(this)" title = "Show Forward flow"></div></div></div>'
			+'</div>');

		}
	});
}
			  
			  
function placeInRespectiveMarts (){
	levelMap.forEach(function (v,k){
		var entityStat = "NOT APPLICABLE";
		if(entityStatusMap[k]!=undefined){
			entityStat = entityStatusMap[k].toUpperCase();
		}
				  if(entityType[k]=="mart"){
					  var actualName = hexToName[k];
					 var  entityActual = new Math.seedrandom(entityActualType[k]).int32();
						$("#"+entityType[k]+"Level"+entityActual+v).append('<div entityCatagory = "'+entityType[k]+'" id = "node_'+k+'" class = "'+entityType[k]+'Slab nodes"><span class = "fa fa-arrow-right arrowInput toolTip_'+k+'"></span><div style = "width: 100%;height: 30px;" class ="headingSlab" onclick = "hightlightTheLinkedItems(this,\''+k+'\',\''+entityType[k]+'\');"><div id = "'+k+'Start" class = ></div><div class ="fa fa-database elementImageStyle"></div><div   title ="'+actualName+'" class = "elementName">'+actualName+'</div><div onClick="infoClick(this,event)";  class = "infoImage fa fa-info-circle '+entityStat.split(" ").join("")+'"></div><div id = "'+k+'End" class = "connDiv fa"></div>'
						+'</div><div class = "'+entityType[k]+'Description descriptionParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title = "'+entityDescMap[k]+'" onmouseover = "showHideDataFlow(this,\'other\')"> '+entityDescMap[k]+'</div>'+'<div class = "flowShowParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"   onmouseleave = "showHideDescription(this)"> <div class = "fa fa-long-arrow-left lienageFlowBack" onclick = "showBackwardFlow(this)" title = "Show Backward flow"></div><div class = "fa fa-long-arrow-right lienageFlowFWD" onclick = "showForwardFlow(this)" title = "Show Forward flow"></div></div></div>'
						+'</div>');
					}                                                          
				 });
				 
	cleanUpMarts();
}

function placeInRespectiveStaging (){
	levelMap.forEach(function (v,k){
		var entityStat = "NOT APPLICABLE";
		if(entityStatusMap[k]!=undefined){
			entityStat = entityStatusMap[k].toUpperCase();
		}
				  if(entityType[k]=="staging"){
					  var actualName = hexToName[k];
					 var entityActual = new Math.seedrandom(entityActualType[k]).int32();
						$("#"+entityType[k]+"Level"+entityActual+v).append('<div entityCatagory = "'+entityType[k]+'" id = "node_'+k+'" class = "'+entityType[k]+'Slab nodes"><span class = "fa fa-arrow-right arrowInput toolTip_'+k+'"></span><div style = "width: 100%;height: 30px;" class ="headingSlab" onclick = "hightlightTheLinkedItems(this,\''+k+'\',\''+entityType[k]+'\');"><div id = "'+k+'Start" class = ></div><div class ="fa fa-database elementImageStyle"></div><div   title ="'+actualName+'" class = "elementName">'+actualName+'</div><div onClick="infoClick(this,event)";  class = "infoImage fa fa-info-circle '+entityStat.split(" ").join("")+'"></div><div id = "'+k+'End" class = "connDiv fa"></div>'
						+'</div><div class = "'+entityType[k]+'Description descriptionParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title = "'+entityDescMap[k]+'" onmouseover = "showHideDataFlow(this,\'other\')"> '+entityDescMap[k]+'</div>'+'<div class = "flowShowParent" style  = "text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"   onmouseleave = "showHideDescription(this)"> <div class = "fa fa-long-arrow-left lienageFlowBack" onclick = "showBackwardFlow(this)" title = "Show Backward flow"></div><div class = "fa fa-long-arrow-right lienageFlowFWD" onclick = "showForwardFlow(this)" title = "Show Forward flow"></div></div></div>'
						+'</div>');
					}                                                          
				 });
				 
	cleanUpStaging();
}

function cleanUpMarts(){
	var classNameForClean = [];
	$(".defaultMart").remove();
	$.each($('.levelContainers'),function (k,v){
		if($(v).children().length==0){
			classNameForClean.push($(v).attr("id"));
		}
	});

	$.each(classNameForClean,function (k,v){
		$("#"+v).remove();
	});

	$.each($('.martParent'),function (k,v){
		if($(v).children().length==1){
			$(v).remove();
		}
	});
}
function cleanUpStaging(){
	var classNameForClean = [];
	$(".defaultStaging").remove();
	$.each($('.levelContainers'),function (k,v){
		if($(v).children().length==0){
			classNameForClean.push($(v).attr("id"));
		}
	});

	$.each(classNameForClean,function (k,v){
		$("#"+v).remove();
	});

	$.each($('.stagingParent'),function (k,v){
		if($(v).children().length==1){
			$(v).remove();
		}
	});
}
			  
function updateContainers(){
	var heights = [];
	$.each($("#main").children().not($(".connection")),function(k,v){
		if($(v).hasClass("sourceSystemParent")){
			heights.push($(v).height()-70);
		}else if ($(v).hasClass("dataConsumerParent")){
			heights.push($(v).height()-60);
		}else {
			heights.push($(v).height()-70);
		}
	});
	var max = heights.reduce(function(a, b) {
		return Math.max(a, b);
	});
	$(".levelContainers").css("min-height",max+"px");
	$(".dataConsumerParent").css("min-height",max+60);	
	$(".sourceSystemParent").css("min-height",max+70);	
	$(".levelContainers").css("height",max+"px");
	$(".dataConsumerParent").css("height",max+60);	
	$(".sourceSystemParent").css("height",max+70);	
	$(".levelContainers").css("height","auto");
	$(".dataConsumerParent").css("height","auto");	
	$(".sourceSystemParent").css("height","auto");	
	

}
function drawConnectionsMethod(){
	//reset maps
	srcLeftPostionMap = {};
	srcTopPostionMap = {};
	srcHeightMap = {};
	srcWidthMap = {};
    
	tgtLeftPostionMap = {};
	tgtTopPostionMap = {};
	tgtHeightMap = {};
	tgtWidthMap = {};
	
	
	
	
	appendHtml="";
	counter=0;
	appendArray=[];
	//window.parent.$(".overlay").css("display","block");
	var a=window.parent.document.getElementById("arrowSwitch");
	if(true){
		$(".connection").remove();
		
		for(var i= 0;i<edges.length;i++){
				var sourceName = edges[i][0];
				var destnName = edges[i][1];
				drawConnections(sourceName,destnName);
		}
	}
	for(var i =0;i<appendArray.length;i++){
	
			$("#main").append(appendArray[i]);
			var test = appendArray[i];
			//setTimeout(createTimerCallback(test), 0);
			
			
	}
	
	//$("#main")[0].innerHTML=$("#main")[0].innerHTML+appendHtml;
	$("#main").append(appendHtml);
			window.parent.$(".overlay").css("display","none");
			window.parent.$(".overlay").removeClass("fakeLoader");

	//document.getElementById("main").innerHTML=document.getElementById("main").innerHTML+appendHtml;
			
		
	
}

function createTimerCallback(im) {
    return function(){  
         $("#main").append(im);
		 maintainHighLightState();
    };
}
	
function drawConnections(srcName,destName){
	counter++;

var className = 'connection connection' + srcName+destName;
var idForPath = 'connection' + srcName+destName;
var idName= 'connection' + srcName;
var srcId = "#node_"+srcName		;
var destId = "#node_"+destName		;
var srcElm = $(srcId+"> .headingSlab");
var destElm = $(destId+"> .headingSlab");
var sourceActualName = hexToName[srcName];
var targetActualName = hexToName[destName];
var taskName = sourceTargetTaskMap[sourceActualName+"~~||~~"+targetActualName];
	if($(srcId).css("display")!="none" && $(destId).css("display")!="none"){
		
if($(srcId).hasClass("collapsedSlabForDSDC")){
	srcId = "#"+$(srcId).parent().parent().attr("id");
	srcElm = $(srcId);
}

if($(destId).hasClass("collapsedSlabForDSDC")){
	destId = "#"+$(destId).parent().parent().attr("id");
	destElm = $(destId);
}
//src variable
var srcLeftFromMap;
var srcTopFromMap;
var srcWidthFromMap;
var srcHeightFromMap;
//src left
if(srcLeftPostionMap[srcId]==undefined){
	srcLeftPostionMap[srcId] = srcElm.offset().left;
	srcLeftFromMap=srcLeftPostionMap[srcId];
}else{
	srcLeftFromMap=srcLeftPostionMap[srcId];
}
//src top
if(srcTopPostionMap[srcId]==undefined){
	srcTopPostionMap[srcId] = srcElm.offset().top;
	srcTopFromMap=srcTopPostionMap[srcId];
}else{
	srcTopFromMap=srcTopPostionMap[srcId];
}

//src height
if(srcHeightMap[srcId]==undefined){
	srcHeightMap[srcId] = srcElm.height();
	srcHeightFromMap=srcHeightMap[srcId];
}else{
	srcHeightFromMap=srcHeightMap[srcId];
}
//src width
if(srcWidthMap[srcId]==undefined){
	srcWidthMap[srcId] = srcElm.width();
	srcWidthFromMap=srcWidthMap[srcId];
}else{
	srcWidthFromMap=srcWidthMap[srcId];
}
//tgt variable
var tgtLeftFromMap;
var tgtTopFromMap;
var tgtWidthFromMap;
var tgtHeightFromMap;
//tgt left
if(tgtLeftPostionMap[destId]==undefined){
	tgtLeftPostionMap[destId] = destElm.offset().left;
	tgtLeftFromMap=tgtLeftPostionMap[destId];
}else{
	tgtLeftFromMap=tgtLeftPostionMap[destId];
}
//tgt top
if(tgtTopPostionMap[destId]==undefined){
	tgtTopPostionMap[destId] = destElm.offset().top;
	tgtTopFromMap=tgtTopPostionMap[destId];
}else{
	tgtTopFromMap=tgtTopPostionMap[destId];
}

//tgt height
if(tgtHeightMap[destId]==undefined){
	tgtHeightMap[destId] = destElm.height();
	tgtHeightFromMap=tgtHeightMap[destId];
}else{
	tgtHeightFromMap=tgtHeightMap[destId];
}
//tgt width
if(tgtWidthMap[destId]==undefined){
	tgtWidthMap[destId] = destElm.width();
	tgtWidthFromMap=tgtWidthMap[destId];
}else{
	tgtWidthFromMap=tgtWidthMap[destId];
}






if((tgtLeftFromMap - srcLeftFromMap)>0){
	//if source behind target --> fwd flow

if((srcTopFromMap - tgtTopFromMap)==0){
		//parent child equal height
		var svgHeight = 3
		var svgWidth = Math.abs((srcLeftFromMap+srcWidthFromMap) - tgtLeftFromMap);  // vertical distance
		var srcCenter = Math.abs(srcHeightFromMap/2);
		var destCenter = Math.abs(tgtHeightFromMap/2);
		var srcXmov = 20;
		var destXmov = 20;
		var vr_line_length = svgHeight;
		var hr_line_length = svgWidth-srcXmov-destXmov;
		var svgPosTop = srcTopFromMap+srcCenter+2;
		var svgPosLeft = srcLeftFromMap+srcWidthFromMap;
		var destStartPointX = svgWidth-20;
		var destStartPointY = svgHeight;
		//$("#main").append('<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+';left:'+svgPosLeft+';" height="'+svgHeight+'" width="'+svgWidth+'"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M 0 0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>');
		appendHtml+='<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+'px;left:'+svgPosLeft+'px;" height="'+svgHeight+'px" width="'+svgWidth+'px"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M 0 0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>';

	}else if((srcTopFromMap - tgtTopFromMap)>0){
		//parent below child
		var svgHeight = Math.abs(srcTopFromMap - tgtTopFromMap);  // horizontal distance
		var svgWidth = Math.abs((srcLeftFromMap+srcWidthFromMap) - tgtLeftFromMap);  // vertical distance
		var srcCenter = Math.abs(srcHeightFromMap/2);
		var destCenter = Math.abs(tgtHeightFromMap/2);
		var srcXmov = 20;
		var destXmov = 20;
		var vr_line_length = svgHeight;
		var hr_line_length = svgWidth-srcXmov-destXmov;
		var svgPosTop = tgtTopFromMap+srcCenter+2;
		var svgPosLeft = srcLeftFromMap+srcWidthFromMap;
		var destStartPointX = svgWidth-20;
		var destStartPointY = svgHeight;
		//$("#main").append('<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+';left:'+svgPosLeft+';" height="'+svgHeight+'" width="'+svgWidth+'"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M 0 '+destStartPointY+' l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartPointX+' 0 l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLine" d= "M '+srcXmov+' 0 l 0 '+vr_line_length+'"  stroke="#2d2d2d" stroke-width="1" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horizontalLine" d= "M '+srcXmov+' 0 l '+hr_line_length+' 0 " stroke="#2d2d2d" stroke-width="3" fill="none"/></svg>');
		appendHtml+='<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+'px;left:'+svgPosLeft+'px;" height="'+svgHeight+'px" width="'+svgWidth+'px"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M 0 '+destStartPointY+' l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartPointX+' 0 l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLine" d= "M '+srcXmov+' 0 l 0 '+vr_line_length+'"  stroke="#2d2d2d" stroke-width="1" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horizontalLine" d= "M '+srcXmov+' 0 l '+hr_line_length+' 0 " stroke="#2d2d2d" stroke-width="3" fill="none"/></svg>'
		
	}else if((srcTopFromMap - tgtTopFromMap)<0){
		//parent above child
		var svgHeight = Math.abs(srcTopFromMap - tgtTopFromMap)+1;  // horizontal distance
		var svgWidth = Math.abs((srcLeftFromMap+srcWidthFromMap) - tgtLeftFromMap);  // vertical distance
		var srcCenter = Math.abs(srcHeightFromMap/2);
		var destCenter = Math.abs(tgtHeightFromMap/2);
		var srcXmov = 20;
		var destXmov = 20;
		var vr_line_length = svgHeight;
		var hr_line_length = svgWidth-srcXmov-destXmov;
		var svgPosTop = srcTopFromMap+srcCenter+2;
		var svgPosLeft = srcLeftFromMap+srcWidthFromMap;
		var destStartPointX = svgWidth-20;
		var destStartPointY = svgHeight;
		//$("#main").append('<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+';left:'+svgPosLeft+';" height="'+svgHeight+'" width="'+svgWidth+'"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M 0 0 l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartPointX+' '+destStartPointY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horizontalLine" d= "M '+srcXmov+' '+destStartPointY+' l '+hr_line_length+' 0"  stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLine" d= "M '+srcXmov+' 0 l 0 '+svgHeight+'" stroke="#2d2d2d" stroke-width="1" fill="none"/></svg>');
		appendHtml+='<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+'px;left:'+svgPosLeft+'px;" height="'+svgHeight+'px" width="'+svgWidth+'px"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M 0 0 l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartPointX+' '+destStartPointY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horizontalLine" d= "M '+srcXmov+' '+destStartPointY+' l '+hr_line_length+' 0"  stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLine" d= "M '+srcXmov+' 0 l 0 '+svgHeight+'" stroke="#2d2d2d" stroke-width="1" fill="none"/></svg>';
	
	}
	
	
	
	
	//reverse connections.
}else{
	//if source after target --> backward flow
	
		if((srcTopFromMap - tgtTopFromMap)>0){
			//parent above child
			var svgHeight = Math.abs((srcTopFromMap +20) -(tgtTopFromMap-20))+1;  // vvetical distance
			var svgWidth = Math.abs((srcLeftFromMap+srcWidthFromMap+20) - (tgtLeftFromMap-20));  // horz distance
			var srcCenter = Math.abs(srcHeightFromMap/2);
			var destCenter = Math.abs(tgtHeightFromMap/2);
			var srcXmov = 20;
			var destXmov = 20;
			var svgPosTop = tgtTopFromMap-tgtHeightFromMap+2;
			var svgPosLeft = tgtLeftFromMap-destXmov;
			var vr_line_from_src  = 0;
			var vr_line_from_tgt  = 0;
			var svgPosTop = 0;
			var svgPosLeft =0;
			var srcStartX = svgWidth-20-2;
			var srcStartY = svgHeight;
			
			var destStartX = 0;
			var destStartY = 0;
			if((srcTopFromMap - tgtTopFromMap)>0){
			//src below
				vr_line_from_src = svgHeight;
				vr_line_from_tgt  = 20;
				hr_final_Connector_width = svgWidth;
				svgPosTop = tgtTopFromMap-tgtHeightFromMap+2;
				svgPosLeft = tgtLeftFromMap-destXmov;
				
			}else if((srcTopFromMap - tgtTopFromMap)<0){
			
			
			}else {
			//equal height
			vr_line_from_src = 20;
			hr_final_Connector_width = svgWidth;
			vr_line_from_tgt  = 20;
			svgPosTop = tgtTopFromMap-tgtHeightFromMap+2;
			svgPosLeft = tgtLeftFromMap-destXmov;
			
			}
			destStartY = Math.abs(svgPosTop-(tgtTopFromMap+20))-7;
			
			var vr_line_length = svgHeight-(+srcHeightFromMap+tgtHeightFromMap);
			var hr_line_length = svgWidth-srcXmov-destXmov;
			
			var destStartPointX = svgWidth-20;
			var destStartPointY = svgHeight+tgtHeightFromMap;
			//$("#main").append('<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+';left:'+svgPosLeft+';" height="'+svgHeight+'" width="'+svgWidth+'"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M '+srcStartX+' '+srcStartY+' l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartX+' '+destStartY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="2" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromSrc" d= "M '+(srcStartX+20)+' '+0+' l 0 '+srcStartY+' "  stroke="#2d2d2d" stroke-width="2" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromTgt" d= "M '+(destXmov-20)+' 0 l 0 '+destStartY+'" stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horzontalLine" d="M '+0+' '+0+'0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>');
			appendHtml+='<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+'px;left:'+svgPosLeft+'px;" height="'+svgHeight+'px" width="'+svgWidth+'px"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M '+srcStartX+' '+srcStartY+' l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartX+' '+destStartY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="2" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromSrc" d= "M '+(srcStartX+20)+' '+0+' l 0 '+srcStartY+' "  stroke="#2d2d2d" stroke-width="2" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromTgt" d= "M '+(destXmov-20)+' 0 l 0 '+destStartY+'" stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horzontalLine" d="M '+0+' '+0+'0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>';
	
	
	}else if((srcTopFromMap - tgtTopFromMap)<0 ){
	//parent above child
			var svgHeight = Math.abs((tgtTopFromMap +20) -(srcTopFromMap-20))+1;  // vvetical distance
			var svgWidth = Math.abs((srcLeftFromMap+srcWidthFromMap+20) - (tgtLeftFromMap-20));  // horz distance
			var srcCenter = Math.abs(srcHeightFromMap/2);
			var destCenter = Math.abs(tgtHeightFromMap/2);
			var srcXmov = 20;
			var destXmov = 20;
			var svgPosTop = srcTopFromMap-srcHeightFromMap+2;
			var svgPosLeft = tgtLeftFromMap-destXmov;
			var vr_line_from_src  = 0;
			var vr_line_from_tgt  = 0;
			
			var svgPosLeft =0;
			var srcStartX = svgWidth-20-2;
			var srcStartY = Math.abs(svgPosTop-(srcTopFromMap))+srcCenter;//svgHeight;
			
			var destStartX = 0;
			var destStartY = svgHeight;
			//src above
			vr_line_from_src = 20;
			hr_final_Connector_width = svgWidth;
			vr_line_from_tgt  = svgHeight;
			//svgPosTop = 
			svgPosLeft = tgtLeftFromMap-destXmov;
			//destStartY = Math.abs(svgPosTop-(tgtTopFromMap+20))
			
			var vr_line_length = svgHeight-(+srcHeightFromMap+tgtHeightFromMap);
			var hr_line_length = svgWidth-srcXmov-destXmov;
			
			var destStartPointX = svgWidth-20;
			var destStartPointY = svgHeight+tgtHeightFromMap;
			//$("#main").append('<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+';left:'+svgPosLeft+';" height="'+svgHeight+'" width="'+svgWidth+'"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M '+srcStartX+' '+srcStartY+'  l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartX+' '+destStartY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="2" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromSrc" d= "M '+(srcStartX+20)+' '+0+' l 0 '+srcStartY+' "  stroke="#2d2d2d" stroke-width="2" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromTgt" d= "M '+(destXmov-20)+' 0 l 0 '+destStartY+'" stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horzontalLine" d="M '+0+' '+0+'0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>');
			appendHtml+='<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+'px;left:'+svgPosLeft+'px;" height="'+svgHeight+'px" width="'+svgWidth+'px"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M '+srcStartX+' '+srcStartY+'  l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartX+' '+destStartY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="2" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromSrc" d= "M '+(srcStartX+20)+' '+0+' l 0 '+srcStartY+' "  stroke="#2d2d2d" stroke-width="2" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromTgt" d= "M '+(destXmov-20)+' 0 l 0 '+destStartY+'" stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horzontalLine" d="M '+0+' '+0+'0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>';
	}else{
	
	//parent above child
			var svgHeight = Math.abs((srcTopFromMap +20) -(tgtTopFromMap-20))+1;  // vvetical distance
			var svgWidth = Math.abs((srcLeftFromMap+srcWidthFromMap+20) - (tgtLeftFromMap-20));  // horz distance
			var srcCenter = Math.abs(srcHeightFromMap/2);
			var destCenter = Math.abs(tgtHeightFromMap/2);
			var srcXmov = 20;
			var destXmov = 20;
			var svgPosTop = tgtTopFromMap-tgtHeightFromMap+2;
			var svgPosLeft = tgtLeftFromMap-destXmov;
			var vr_line_from_src  = 0;
			var vr_line_from_tgt  = 0;
			var svgPosTop = 0;
			var svgPosLeft =0;
			var srcStartX = svgWidth-20-2;
			var srcStartY = svgHeight;
			
			var destStartX = 0;
			var destStartY = 0;
			if((srcTopFromMap - tgtTopFromMap)>0){
			//src below
				vr_line_from_src = svgHeight;
				vr_line_from_tgt  = 20;
				hr_final_Connector_width = svgWidth;
				svgPosTop = tgtTopFromMap-tgtHeightFromMap+2;
				svgPosLeft = tgtLeftFromMap-destXmov;
				
			}else if((srcTopFromMap - tgtTopFromMap)<0){
			
			
			}else {
			//equal height
			vr_line_from_src = 20;
			hr_final_Connector_width = svgWidth;
			vr_line_from_tgt  = 20;
			svgPosTop = tgtTopFromMap-tgtHeightFromMap+2;
			svgPosLeft = tgtLeftFromMap-destXmov;
			
			}
			destStartY = svgHeight;
			
			var vr_line_length = svgHeight-(+srcHeightFromMap+tgtHeightFromMap);
			var hr_line_length = svgWidth-srcXmov-destXmov;
			
			var destStartPointX = svgWidth-20;
			var destStartPointY = svgHeight+tgtHeightFromMap;
			//$("#main").append('<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+';left:'+svgPosLeft+';" height="'+svgHeight+'" width="'+svgWidth+'"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M '+srcStartX+' '+srcStartY+' l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartX+' '+destStartY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="2" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromSrc" d= "M '+(srcStartX+20)+' '+0+' l 0 '+srcStartY+' "  stroke="#2d2d2d" stroke-width="2" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromTgt" d= "M '+(destXmov-20)+' 0 l 0 '+destStartY+'" stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horzontalLine" d="M '+0+' '+0+'0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>');
			appendHtml+='<svg class = "'+className+'" id = "'+idName+'" style = "position:absolute;top:'+svgPosTop+'px;left:'+svgPosLeft+'px;" height="'+svgHeight+'px" width="'+svgWidth+'px"><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "sourceEnd" d="M '+srcStartX+' '+srcStartY+' l '+srcXmov+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" />	<path class="'+idForPath+'" stroke-dasharray="2, 2" class = "destStart" d="M '+destStartX+' '+destStartY+' l '+destXmov+' 0" stroke="#2d2d2d" stroke-width="2" fill="none" /><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromSrc" d= "M '+(srcStartX+20)+' '+0+' l 0 '+srcStartY+' "  stroke="#2d2d2d" stroke-width="2" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "verticalLineFromTgt" d= "M '+(destXmov-20)+' 0 l 0 '+destStartY+'" stroke="#2d2d2d" stroke-width="3" fill="none"/><path class="'+idForPath+'" stroke-dasharray="2, 2" class = "horzontalLine" d="M '+0+' '+0+'0 l '+svgWidth+' 0" stroke="#2d2d2d" stroke-width="3" fill="none" /></svg>';
	}
	
}
	
	
	
	
	}
	
	if(counter==10)
	{
		counter=0;
		/* setTimeout(function(){ 
			var test = appendHtml;
			$("#main").append(test);
			
		}, 1);
		 */
		 appendArray.push(appendHtml);
		 appendHtml="";	
	}
	
}


function hightlightTheLinkedItems(elm,curId,curCat){
	if(showHideSwitchStatus()){
		
	
	var ClickedNode = $(elm).parent().attr("id");
	
	$(".hightlightLink").removeClass("hightlightLink");
	$(".borderBottomStyle").removeClass("borderBottomStyle");
	$(".borderTopStyle").removeClass("borderTopStyle");
	$(".borderLeftStyle").removeClass("borderLeftStyle");
	$(".borderRightStyle").removeClass("borderRightStyle");
	$(".borderBottomForConn").removeClass("borderBottomForConn");
	$(".connection").show();
	$("path").attr("stroke-dasharray","2, 2");
	$("path").addClass("lightPath");
		$("path").removeClass("darkPath");
		$(".nodes").addClass("lightNodes");
	if(!(window.parent.$("#showSwitch").prop('checked'))){
		$("path").addClass("hidePaths");	
		
	}	
	
	$(".hightLightSlab:not(#"+ClickedNode+")").removeClass("hightLightSlab");
	$(".darkNodes:not(#"+ClickedNode+")").removeClass("darkNodes");
	if(!($("#node_"+curId).hasClass("hightLightSlab"))){
		clickedNodeId.push(ClickedNode);
		$("#"+ClickedNode).addClass("hightLightSlab")
		$("#"+ClickedNode).addClass("darkNodes")
		pendingHighlightArray = [];
		pendingParentHighlightArray = [];
		pendingHighlightArray.push(Number(curId));
		pendingParentHighlightArray.push(Number(curId));
		//$(".connection").hide();
		getSourceConnection(curId,curCat);
		getTargetConnections(curId,curCat);
	}else{
	clickedNodeId = [];
	$(".hightLightSlab").removeClass("hightLightSlab");
	$(".darkNodes").removeClass("darkNodes");
	$(".lightNodes").removeClass("lightNodes");
	$(".darkPath").removeClass("darkPath");
	$(".lightPath").removeClass("lightPath");
	//$(".hidePaths").removeClass("hidePaths");
	//$(".hideLightNodes").removeClass("hideLightNodes");
	
		
	}
	arrowCheck();
	$("path").css("display","");
	//updateContainersForShowHide();
	setParentFrame();
	}
}

function getSourceConnection(currentElmId,currentElmCat){
	for(var i = 0;i<graph.edges.length;i++){
				if(graph.edges[i][0] == currentElmId ){
					pendingHighlightArray.push(graph.edges[i][1]);
					hightlightTheRelation(graph.edges[i][0]+"~~||~~"+graph.edges[i][1]);
					if(pendingHighlightArray.indexOf(graph.edges[i][0])!=-1){
					pendingHighlightArray.splice(pendingHighlightArray.indexOf(graph.edges[i][0]),1);
					
					}
					
					//getSourceConnection(connArray[i].split(">")[0]);
				}
	}
	if(currentElmId==pendingHighlightArray[0]){
		pendingHighlightArray.splice(pendingHighlightArray.indexOf(currentElmId),1);
	}
	
	if(pendingHighlightArray.length>0){
	pendingHighlightArray=	Array.from(new Set(pendingHighlightArray));
		if(entityType[pendingHighlightArray[0]]!="dataConsumer"){
			getSourceConnection(pendingHighlightArray[0],"");
		
		}else{
			pendingHighlightArray.splice(pendingHighlightArray.indexOf(pendingHighlightArray[0]),1);
			if(pendingHighlightArray.length>0){
				getSourceConnection(pendingHighlightArray[0],"");
			}		
		}
	}
	
}
function getTargetConnections(currentElmId){
	for(var i = 0;i<graph.edges.length;i++){
				if(graph.edges[i][1] == currentElmId ){
					pendingParentHighlightArray.push(graph.edges[i][0]);
					hightlightTheRelation(graph.edges[i][0]+"~~||~~"+graph.edges[i][1]);
					if(pendingParentHighlightArray.indexOf(graph.edges[i][1])!=-1){
					pendingParentHighlightArray.splice(pendingParentHighlightArray.indexOf(graph.edges[i][1]),1);
					
					}
					
					//getSourceConnection(connArray[i].split(">")[0]);
				}
	}
	if(currentElmId==pendingParentHighlightArray[0]){
		pendingParentHighlightArray.splice(pendingParentHighlightArray.indexOf(currentElmId),1);
	}
	
	if(pendingParentHighlightArray.length>0){
	pendingParentHighlightArray=	Array.from(new Set(pendingParentHighlightArray));
		if(entityType[pendingParentHighlightArray[0]]!="sourceSystem"){
			getTargetConnections(pendingParentHighlightArray[0],"");
		
		}else{
			pendingParentHighlightArray.splice(pendingParentHighlightArray.indexOf(pendingParentHighlightArray[0]),1);
			if(pendingParentHighlightArray.length>0){
				getTargetConnections(pendingParentHighlightArray[0],"");
			}		
		}
	}
}

/*function getTargetConnections(currentElmId){
	for(var i = 0;i<graph.edges.length;i++){
				if(graph.edges[i][1] == currentElmId ){
					hightlightTheRelation(graph.edges[i][0]+"~~||~~"+graph.edges[i][1]);
					//getTargetConnections(connArray[i].split(">")[1]);
				}
	}	

}*/
function hightlightTheRelation(elementClass){
		$.each($(".connection"+elementClass.split("~~||~~")[0]+elementClass.split("~~||~~")[1]),function(key,val){
			$(val).show();
			$(val).find("path").attr("stroke-dasharray","");
			$(val).find("path").addClass("darkPath");
			if(!($(val).css("border-bottom-width")=="0px")){
				$(val).addClass("borderBottomStyle");
			}
			if(!($(val).css("border-top-width")=="0px")){
				$(val).addClass("borderTopStyle");
			}
			if(!($(val).css("border-left-width")=="0px")){
				$(val).addClass("borderLeftStyle");
			}
			if(!($(val).css("border-right-width")=="0px")){
				$(val).addClass("borderRightStyle");
			}
			$("#node_"+elementClass.split("~~||~~")[0]).addClass("hightLightSlab");
			$("#node_"+elementClass.split("~~||~~")[0]).addClass("darkNodes");
			$("#node_"+elementClass.split("~~||~~")[1]).addClass("hightLightSlab");
			$("#node_"+elementClass.split("~~||~~")[1]).addClass("darkNodes");
			
			
		
		
	});
	
	


}

function collapseExpand(elmRec){
	var elm = $(elmRec).parent();
	if($(elm).hasClass("expanded")){
		$(elm).addClass("collapsed");
		$(elm).removeClass("expanded");
		$(elm).find(".elementImageStyle").hide();
		$(elm).find(".elementName").hide();
		$(elm).find(".levelContainers").css("min-width","0px");
		$(elm).find(".infoImage").hide();
		$(elm).find(".nodes").addClass("collapsed");
		$(elm).find(".nodes").addClass("collapsedSlab");
		$(elm).find(".levelContainers").addClass("collapsedSlab");
		$(elm).find(".typeText").addClass("collapsedText");
		$(elm).find(".nodes").removeClass("expanded");
		$(elm).find(".expandCollapseImg").removeClass("fa-arrow-circle-left");
		$(elm).find(".expandCollapseImg").addClass("fa-arrow-circle-down");
		//bindClickForCollapsed();
	}else{
		$(elm).addClass("expanded");
		$(elm).removeClass("collapsed");
		$(elm).find(".elementImageStyle").show();
		$(elm).find(".elementName").show();
		$(elm).find(".infoImage").show();
		$(elm).find(".levelContainers").css("min-width","200px");
		$(elm).find(".nodes").addClass("expanded");
		$(elm).find(".nodes").removeClass("collapsed");
		$(elm).find(".nodes").removeClass("collapsedSlab");
		$(elm).find(".levelContainers").removeClass("collapsedSlab");
		$(elm).find(".typeText").removeClass("collapsedText");
		$(elm).find(".expandCollapseImg").addClass("fa-arrow-circle-left");
		$(elm).find(".expandCollapseImg").removeClass("fa-arrow-circle-down");

	}
	//updateContainers();
	window.parent.$(".overlay").css("display","block");
	setTimeout(function(){
	drawConnectionsMethod();
	},0);
}

function collapseExpandForSourceSystem(elmRec){
	var elm = $(elmRec).parent();
	if($(elm).hasClass("expanded")){
		$(elm).addClass("collapsed");
		$(elm).removeClass("expanded");
		$(elm).find(".elementImageStyle").hide();
		$(elm).find(".elementName").hide();
		$(elm).find(".sourceSystemLevelStyle").css("height","0px");
		$(elm).find(".sourceSystemLevelStyle").css("padding-bottom","0px");
		$(elm).find(".infoImage").hide();
		$(elm).find(".nodes").addClass("collapsed");
		$(elm).find(".nodes").addClass("collapsedSlabForDSDC");
		$(elm).find(".nodes").children().hide();
		$(elm).find(".nodes").find(".sourceDescription").css("height","0px");
		$(elm).find(".nodes").removeClass("expanded");
		$(elm).find(".expandCollapseImg").removeClass("fa-arrow-circle-up");
		$(elm).find(".expandCollapseImg").addClass("fa-arrow-circle-down");
		//bindClickForCollapsed();
	}else{
		$(elm).addClass("expanded");
		$(elm).removeClass("collapsed");
		$(elm).find(".elementImageStyle").show();
		$(elm).find(".elementName").show();
		$(elm).find(".infoImage").show();
		$(elm).find(".sourceSystemLevelStyle").css("height","");
		$(elm).find(".sourceSystemLevelStyle").css("padding-bottom","15px");
		$(elm).find(".nodes").addClass("expanded");
		$(elm).find(".nodes").removeClass("collapsed");
		$(elm).find(".nodes").removeClass("collapsedSlabForDSDC");
		$(elm).find(".nodes").children().show();
		$(elm).find(".nodes").find(".sourceDescription").css("height","30px");
		$(elm).find(".expandCollapseImg").addClass("fa-arrow-circle-up");
		$(elm).find(".expandCollapseImg").removeClass("fa-arrow-circle-down");
		

	}
	//updateContainers();
	window.parent.$(".overlay").css("display","block");
	setTimeout(function(){
	drawConnectionsMethod();
	},0);
}

function collapseExpandForSolution(elmRec){
	
	var elm = $(elmRec).parent();
	if($(elm).hasClass("expanded")){
		$(elm).addClass("collapsed");
		$(elmRec).find(".arrowInputDC").show();
		$(elm).removeClass("expanded");
		$(elm).find(".elementImageStyle").hide();
		$(elm).find(".elementName").hide();
		$(elm).find(".solutionLevelStyle").css("height","0px");
		$(elm).find(".solutionLevelStyle").css("padding-bottom","0px");
		$(elm).find(".infoImage").hide();
		$(elm).find(".nodes").addClass("collapsed");
		$(elm).find(".nodes").addClass("collapsedSlabForDSDC");
		$(elm).find(".nodes").children().hide();
		$(elm).find(".nodes").removeClass("expanded");
		$(elm).find(".expandCollapseImg").removeClass("fa-arrow-circle-up");
		$(elm).find(".expandCollapseImg").addClass("fa-arrow-circle-down");
		//bindClickForCollapsed();
	}else{
		$(elm).addClass("expanded");
		$(elmRec).find(".arrowInputDC").hide();
		$(elm).removeClass("collapsed");
		$(elm).find(".elementImageStyle").show();
		$(elm).find(".elementName").show();
		$(elm).find(".infoImage").show();
		$(elm).find(".solutionLevelStyle").css("height","");
		$(elm).find(".solutionLevelStyle").css("padding-bottom","15px");
		$(elm).find(".nodes").addClass("expanded");
		$(elm).find(".nodes").removeClass("collapsed");
		$(elm).find(".nodes").removeClass("collapsedSlabForDSDC");
		$(elm).find(".nodes").children().show();
		$(elm).find(".expandCollapseImg").addClass("fa-arrow-circle-up");
		$(elm).find(".expandCollapseImg").removeClass("fa-arrow-circle-down");
		

	}
	//updateContainers();
	window.parent.$(".overlay").css("display","block");
	setTimeout(function(){
	drawConnectionsMethod();
	},0);
}



function bindClickForCollapsed(){
	$(".collapsed").click(function(){
		  // Holds the product ID of the clicked element
		  collapseExpand($(this));
		});

}	

function sortTheDivsBasedOnSortOrder() {

    tinysort('.dataRepoParent > div:not(#dataRepoText)', {
        attr: 'sortOrder'
    });

}
function passNodesToParent(){
	window.parent.getNodesFromIframe(nameTypeObjArrayForParent);
}

function showOrHideNodes(nodeName,owner){
	if(showHideSwitchStatus()){
		
	
	var nodeId ="#node_"+hexor[nodeName+owner];
	if($(nodeId).hasClass("shownNode")){
		$(nodeId).addClass("hideNode");
		$(nodeId).removeClass("shownNode");
		//updateContainersForRemove(type);
	}else{
		$(nodeId).removeClass("hideNode");
		$(nodeId).addClass("shownNode");
		updateContainers();
		scrollToNodePosition(nodeId);
	}
	window.parent.$(".overlay").css("display","block");
	setTimeout(function(){
	drawConnectionsMethod();
	},0);
	setParentFrame();
}
}

function setParentFrame(){
	
	parent.document.getElementsByTagName("IFRAME")[0].style.height =$("#main").height()+200;
}


function showOrHideAllNodes(type,statusRec){
	if(statusRec){
		$("."+type+"Parent").find(".nodes").addClass("shownNode");
		$("."+type+"Parent").find(".nodes").removeClass("hideNode");
		//updateContainers();
		updateContainersForRemove(type);
	}else{
		$("."+type+"Parent").find(".nodes").removeClass("shownNode");
		$("."+type+"Parent").find(".nodes").addClass("hideNode");
		updateContainersForRemove(type);
		//updateContainers();
	}
	
	
	setParentFrame();
	
	drawConnectionsMethod();
	
}

function updateContainersForRemove(type){
	if(type=="dataRepo"){
		$(".levelContainers").css("min-height","");	
		$(".levelContainers").css("height","auto");	
		//updateContainers();
	}else if(type=="dataConsumer" ||type=="sourceSystem"){
		$("."+type+"Parent").css("min-height","");	
		$("."+type+"Parent").css("height","auto");	
	}
	
	var heights = [];
	$.each($("#main").children().not($(".connection")),function(k,v){
		if($(v).hasClass("sourceSystemParent")){
			heights.push($(v).height()-70);
		}else if ($(v).hasClass("dataConsumerParent")){
			heights.push($(v).height()-60);
		}else {
			heights.push($(v).height()-70);
		}
	});
	var max = heights.reduce(function(a, b) {
		return Math.max(a, b);
	});
	$(".levelContainers").css("min-height",max);	
	$(".dataConsumerParent").css("min-height",max+60);	
	$(".sourceSystemParent").css("min-height",max+70);	
	$(".levelContainers").css("height",max+"px");
	$(".dataConsumerParent").css("height",max+60);	
	$(".sourceSystemParent").css("height",max+70);	
	
}
function arrowCheck(){
	if(true){
				$(".arrowInput").show();
				//$(".connection").show();
				//updateContainers();
				$("path").show();
				//drawConnectionsMethod();
				
			}else{
				$.each($("path"),function (k,v){
					if($(v).attr("stroke-dasharray")!=""){
						$(v).hide();
					}
				});
				$(".arrowInput").hide();
				$(".hightLightSlab > .arrowInput").show();
				//$(".connection").remove();
			}
}

function checkForEntityClick(){
	var clickObjDetails = parent.window.lineageForEntity;
	if(!(Object.keys(parent.window.lineageForEntity).length === 0 && $.isEmptyObject(parent.window.lineageForEntity))){
		var entityName = clickObjDetails.entityName;
		var entityType = clickObjDetails.entityType;
		var entityOwner = clickObjDetails.entityOwner;
		var entityOwnerDiv = "";
		if(entityOwner.toLowerCase()=="data hub" || entityOwner.toLowerCase()=="datahub"){
			entityOwnerDiv = "DATAHUB";
		}else{
			entityOwnerDiv = entityOwner;
		}
		var nodeId ="#node_"+hexor[entityName+entityOwner];
		$(nodeId).find(".headingSlab").click();
		var nodePosition = $(nodeId).position();
		var parentElement = parent.document.body;
		//parent.document.body.scrollTop = nodePosition.top;
		//parent.document.body.scrollLeft = nodePosition.left;
		$(parentElement).animate({scrollTop:nodePosition.top}, 'slow');
		window.parent.$('#dataLineageIframe').contents().children().animate({scrollLeft:nodePosition.left},"slow")
		//$(parentElement).animate({scrollTop:nodePosition.top,scrollLeft:nodePosition.left}, 'slow');
		parent.window.lineageForEntity = {};	
	}
}

function checkForButtonDisplay(){
	var a   = parent.document.body;
	if($(a).width()<=$("#main").width()){
		$(a).find("#toRightByNPix").show();
		$(a).find("#toLeftByNPix").show();
	}else{
		$(a).find("#toRightByNPix").hide();
		$(a).find("#toLeftByNPix").hide();
	
	}
}

function showHideSwitchStatus(){
	return window.parent.$("#showSwitch").prop('checked');
}

function updateContainersForShowHide(type){

		$(".levelContainers").css("min-height","");	
		$(".levelContainers").css("height","auto");	
	
	var heights = [];
	$.each($(".levelContainers"),function(k,v){
		heights.push($(v).height());
	});
	var max = heights.reduce(function(a, b) {
		return Math.max(a, b);
	});
	$(".levelContainers").css("min-height",max);	
	$(".dataConsumerParent").css("min-height",max+60);	
	$(".sourceSystemParent").css("min-height",max+70);	
	$(".levelContainers").css("height",max+"px");
	$(".dataConsumerParent").css("height",max+60);	
	$(".sourceSystemParent").css("height",max+70);	
	

}

function highlightCommonsFromIframe(){
	arrowCheck();
	updateContainers();
	hideShowBasedOnChildren();
	updateContainersForShowHide();
	//window.parent.$(".overlay").css("display","block");
	//setTimeout(function(){
	drawConnectionsMethod();
	//},0);
	//setTimeout(maintainHighLightState(), 0);;
	maintainHighLightState();
	setParentFrame();
}


function maintainHighLightState(){
	if(!(clickedNodeId.length==0)){
		var clickedElmId = clickedNodeId[0];
		$("#"+clickedElmId).find(".headingSlab").click();
		$("#"+clickedElmId).find(".headingSlab").click();
		
	}
}

function hideShowBasedOnChildren(){

	$.each($(".sourceSystemLevelStyle"),function (k,v){
		if($(v).find(".hideLightNodes").length==$(v).children().length && $(v).find(".darkNodes").length==0){
			$(v).parent().addClass("parentHidden");
		}
	});
	$.each($(".solutionLevelStyle"),function (k,v){
		if($(v).find(".hideLightNodes").length==$(v).children().length && $(v).find(".darkNodes").length==0){
			$(v).parent().addClass("parentHidden");
		}
	});

}  
 

 function scrollToNodePosition(nodeId){
		//$(nodeId).find(".headingSlab").click();
		var nodePosition = $(nodeId).position();
		var parentElement = parent.document.body;
		//parent.document.body.scrollTop = nodePosition.top;
		//parent.document.body.scrollLeft = nodePosition.left;
		$(parentElement).animate({scrollTop:nodePosition.top}, 'slow');
		window.parent.$('#dataLineageIframe').contents().children().animate({scrollLeft:nodePosition.left},"slow")
	 
 }
 function infoClick(elm,evt)
 {
	 var name=$(elm).prev().text();
	 var sources=[];
	 var tasks=[];
	 evt.preventDefault();
	 evt.stopPropagation();
	 $(".souceinClass").remove();
	 $(".taskinClass").remove();
	 $(".statusinClass").remove();
	 $.each(sourceTargetTaskMap,function(k,v){
		
		if(k.split("~~||~~")[1]==name)
		{
			tasks.push(sourceTargetTaskMap[k]);
			sources.push(k.split("~~||~~")[0]);
			$("#sourceForInfo").append("<div class='souceinClass' style='text-overflow:ellipsis;overflow:hidden;white-space:nowrap' title='"+k.split("~~||~~")[0]+"'>"+k.split("~~||~~")[0]+"</div>");
			$("#taskForInfo").append("<div class='taskinClass' style='text-overflow:ellipsis;overflow:hidden;white-space:nowrap' title='"+sourceTargetTaskMap[k]+"'>"+sourceTargetTaskMap[k]+"</div>");
			$("#statusForInfo").append("<div class='statusinClass' style='text-overflow:ellipsis;overflow:hidden;white-space:nowrap' title='"+taskStatusMap[sourceTargetTaskMap[k]]+"'>"+taskStatusMap[sourceTargetTaskMap[k]]+"</div>");
		}
	 
	 });
	 
	 $(".tooltip").toggle();
	 $(".tooltip").css("top",$(elm).offset().top+10);
	 $(".tooltip").css("left",$(elm).offset().left);
 }
 
 
 function showHideDataFlow(elm,type){
	 if((!window.parent.$("#sourceSystemCheckBox").prop("checked"))&& (!window.parent.$("#dataRepoCheckBox").prop("checked")) && (!window.parent.$("#dataConsumerCheckBox").prop("checked"))){
		 $(elm).parent().find(".flowShowParent").css("display","inline-block");
		 $(elm).css("display","none");
		 
	 }
 }
 
 
 function showHideDescription(elm){
	 $(elm).css("display","none")
	 $(elm).parent().find(".descriptionParent").css("display","inline-block");
 }
 
 function showBackwardFlow(elm){
	 $(elm).addClass("disabledFlow");
	 var currentElmId = $(elm).parent().parent().attr("id").replace("node_","");
	 for(var i = 0;i<graph.edges.length;i++){
				if(graph.edges[i][1] == currentElmId ){
					//window.parent.$("#filterSlabLeaf_"+graph.edges[i][0]).find(".nodeImage");
					highlight1StepBCK(graph.edges[i][0]);
				}
	}
	updateContainers();
	window.parent.$(".overlay").css("display","block");
	setTimeout(function(){
	drawConnectionsMethod();
	},0);
	setParentFrame();
 }
 
 function showForwardFlow(elm){
	 $(elm).addClass("disabledFlow");
	 var currentElmId = $(elm).parent().parent().attr("id").replace("node_","");
	for(var i = 0;i<graph.edges.length;i++){
				if(graph.edges[i][0] == currentElmId ){
					
					highlight1StepFWD(graph.edges[i][1]);
				}
	}
	updateContainers();
	window.parent.$(".overlay").css("display","block");
	setTimeout(function(){
	drawConnectionsMethod();
	},0);
	setParentFrame();
 }
 
 
 function highlight1StepFWD(nodeId){
	 var elm =window.parent.$("#filterSlabLeaf_"+nodeId).find(".nodeImage"); 
	 nodeId ="#node_"+nodeId;
	 if($(elm).hasClass("fa-minus-circle")){
		//$(elm).addClass("fa-plus-circle");
		//$(elm).parent().addClass("lowShade");
		//$(elm).removeClass("fa-minus-circle");
	}else{
		$(elm).removeClass("fa-plus-circle");
		$(elm).addClass("fa-minus-circle");
		$(elm).parent().removeClass("lowShade");
	}
	
	if($(nodeId).hasClass("shownNode")){
		//$(nodeId).addClass("hideNode");
		//$(nodeId).removeClass("shownNode");
		//updateContainersForRemove(type);
	}else{
		$(nodeId).removeClass("hideNode");
		$(nodeId).addClass("shownNode");
		//updateContainers();
		//scrollToNodePosition(nodeId);
	}

	
 }
 
 function highlight1StepBCK(nodeId){
	 var elm =window.parent.$("#filterSlabLeaf_"+nodeId).find(".nodeImage"); 
	 nodeId ="#node_"+nodeId;
	 if($(elm).hasClass("fa-minus-circle")){
		//$(elm).addClass("fa-plus-circle");
		//$(elm).parent().addClass("lowShade");
		//$(elm).removeClass("fa-minus-circle");
	}else{
		$(elm).removeClass("fa-plus-circle");
		$(elm).addClass("fa-minus-circle");
		$(elm).parent().removeClass("lowShade");
	}
	
	if($(nodeId).hasClass("shownNode")){
		//$(nodeId).addClass("hideNode");
		//$(nodeId).removeClass("shownNode");
		//updateContainersForRemove(type);
	}else{
		$(nodeId).removeClass("hideNode");
		$(nodeId).addClass("shownNode");
		//scrollToNodePosition(nodeId);
	}

 }
 
function checkStatusPriority(targetHexCode,taskStatus){
	var currentStatus = entityStatusMap[targetHexCode];
	if(taskStatus!=currentStatus && currentStatus.toUpperCase()!="FAILED" ){
		if(taskStatus.toUpperCase()=="PENDING" ){
			entityStatusMap[targetHexCode] = "PENDING";
		}else if(taskStatus.toUpperCase()=="NOT APLLICABLE"  ){
			entityStatusMap[targetHexCode] = "NOT APLLICABLE";
		}else if((taskStatus.toUpperCase()=="COMPLETED"  )){
			entityStatusMap[targetHexCode] = "COMPLETED";
		}
		
			
		}
}
function loadForAReport(reportId,solutionName){
	cleanUpFullLayout();
	
		$.get("/dldwebapplication/getDataForlineageForLineItem?cbd="+cbd+"&clientCode="+clientCode+"&reportId="+reportId+"&solutionName="+solutionName+"").done(function(data){
		
		for(var i=0;i<data.entities.length;i++){
			var hexCode = (new Math.seedrandom(data.entities[i].entityName+data.entities[i].entityOwner)).int32();
			var entityName = data.entities[i].entityName;
			var entityOwner = data.entities[i].entityOwner;
			hexor[entityName+entityOwner]=hexCode;
			hexToName[hexCode] = entityName;
			graph.addVertex(hexCode);
			entityDescMap[hexCode] = data.entities[i].entityDesc;
		}
		
		for(var i=0;i<data.displayOrders.length;i++){
			var ownerNameRec = (data.displayOrders[i].ownerName).toUpperCase();
			if(ownerNameRec == "DATAHUB" || ownerNameRec =="DATA HUB"){
				ownerNameRec = "DATAHUB"
			}
			typeAndSortMap[ownerNameRec] = Number(data.displayOrders[i].displayOrder);
		}
		prepareDataForLayout(data);
		checkForButtonDisplay();
		updateReportName(window.parent.$("#reportWithLineItem").val());
		
	});
}

function loadFullLineage(){
	cleanUpFullLayout();
	$.get("/dldwebapplication/getDataForlineage?cbd="+cbd+"&clientCode="+clientCode+"").done(function(data){
		
		for(var i=0;i<data.entities.length;i++){
			var hexCode = (new Math.seedrandom(data.entities[i].entityName+data.entities[i].entityOwner)).int32();
			var entityName = data.entities[i].entityName;
			var entityOwner = data.entities[i].entityOwner;
			hexor[entityName+entityOwner]=hexCode;
			hexToName[hexCode] = entityName;
			graph.addVertex(hexCode);
			entityDescMap[hexCode] = data.entities[i].entityDesc;
		}
		
		for(var i=0;i<data.displayOrders.length;i++){
			var ownerNameRec = (data.displayOrders[i].ownerName).toUpperCase();
			if(ownerNameRec == "DATAHUB" || ownerNameRec =="DATA HUB"){
				ownerNameRec = "DATAHUB"
			}
			typeAndSortMap[ownerNameRec] = Number(data.displayOrders[i].displayOrder);
		}
		prepareDataForLayout(data);
		checkForButtonDisplay();
		
	});
}


function cleanUpFullLayout(){
	graph = new Graph();
	$(".nodes").remove();
	var max = 300;
	$(".levelContainers").children().not(".levelHead").remove();
	$(".dataRepoParent").children().not(".levelHead").remove();
	$(".levelContainers").css("min-height",max+"px");
	$(".dataConsumerParent").css("min-height",max+60);	
	$(".sourceSystemParent").css("min-height",max+70);	
	$(".levelContainers").css("height",max+"px");
	$(".dataConsumerParent").css("height",max+60);	
	$(".sourceSystemParent").css("height",max+70);	
	$(".levelContainers").css("height","auto");
	$(".dataConsumerParent").css("height","auto");	
	$(".sourceSystemParent").css("height","auto");	
	
	//re intialize all global variable 
	//layout preparation.
entityMaxLevel = {};
entityMaxLevel["sourceSystem"]=0;
entityMaxLevel["staging"]=0;
entityMaxLevel["reception"]=0;
entityMaxLevel["datahub"]=0;
entityMaxLevel["mart"]=0;
entityMaxLevel["dataConsumer"]=0;
distinctSourceLevels = new Set();
distinctRecptionLevels = new Set();
distinctStagingLevels = new Set();
distinctDatahubLevels = new Set();
distinctMartLevels = new Set();
distinctDataConsumerLevels = new Set();
appendHtml="";
appendArray = [];
counter=0;
//map of  sort order and type.
typeAndSortMap = {}

//map contains for each	entity and its type - SS,dataRepo,mart,DC		  
entityType = {};
 
//entity actual map in order to handle multiple marts.
entityActualType = {};

//entity and owner map.
entityDSMap = {};

//entity solution map
entitySolMap = {};
 
 //Task Status map
taskStatusMap={};
 
//distinct data source 
distinctSourceName = new Set();

//distinct solutions
distinctSolutionName = new Set();

//distinct marts.
distinctMartsType = new Set();

//distinct staging.
distinctStagingType = new Set();

//hexx values of entity names.
hexor={};
hexToName = {};

//SOURCE TARGET AND TASKnAME PAIR
sourceTargetTaskMap = {};

//entity n desc map.
entityDescMap = {};

//pending child array
pendingHighlightArray = [];


//pending child array
pendingParentHighlightArray = [];


//nodes to parent
nameTypeObjArrayForParent = [];

//highlightState maintain
clickedNodeId = [];

//status object

entityStatusMap = {};
	
}


function updateReportName (reportDetails){
	$(".solutionNameParent").find(".headingTypeTextSpan").text(reportDetails);
	$(".solutionNameParent").find(".headingTypeTextSpan").attr("title",reportDetails);
}