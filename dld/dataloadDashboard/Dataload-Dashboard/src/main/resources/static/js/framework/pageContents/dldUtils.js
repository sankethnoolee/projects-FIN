/*global and constants*/
var displayDateFormat = 'dd-mm-yyyy';
var processingDateFormat = 'DD-MM-YYYY';


var monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun","Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
var weekday=new Array(7);
weekday[0]="Sunday";
weekday[1]="Monday";
weekday[2]="Tuesday";
weekday[3]="Wednesday";
weekday[4]="Thursday";
weekday[5]="Friday";
weekday[6]="Saturday";

var weekDayInShort = new Array(7);
weekDayInShort[0]="Sun";
weekDayInShort[1]="Mon";
weekDayInShort[2]="Tue";
weekDayInShort[3]="Wed";
weekDayInShort[4]="Thu";
weekDayInShort[5]="Fri";
weekDayInShort[6]="Sat";
var prevDate="";
var prevPendingDate="";
var maxCurBusinessDate=new Date();
var fetchData = function(query, dataURL) {
    // Return the $.ajax promise
    return $.ajax({
        data: query,
        dataType: 'json',
        url: dataURL
    });
}

/*global and constants*/




//scroll top button view function settings
function scrollFunction() {

	if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
			document.getElementById("toTop").style.display = "block";
			document.getElementById("toTopByNPix").style.display = "block";
			
	} else {
			document.getElementById("toTop").style.display = "none";
			document.getElementById("toTopByNPix").style.display = "none";
			
	}
	
	if (document.body.scrollTop >= 0) {
		document.getElementById("toBottom").style.display = "block";
		document.getElementById("toBottomByNPix").style.display = "block";
	}else if($(document).height()==$(window).scrollTop()){
		document.getElementById("toBottom").style.display = "none";
		document.getElementById("toBottomByNPix").style.display = "none";
	}
}

//scroll top function 
function topFunction() {
    $('html, body').animate({scrollTop:0}, 'slow');
}

//scroll top by n pix function 
function topByNFunction() {
    $('html, body').animate({scrollTop:$(window).scrollTop()-500}, 'slow');
}

//scroll bottom function 
function bottomFunction() {
    $('html, body').animate({scrollTop:$(document).height()}, 'slow');
}

//scroll bottom by n function 
function bottomByNFunction() {
    $('html, body').animate({scrollTop:$(window).scrollTop()+500}, 'slow');
}

//scroll right by n function 
function rightFunction() {
    $('#dataLineageIframe').contents().children().animate({scrollLeft:$('#dataLineageIframe').contents().scrollLeft()+500}, 'slow');
}

//scroll left by n function 
function leftFunction() {
    $('#dataLineageIframe').contents().children().animate({scrollLeft:$('#dataLineageIframe').contents().scrollLeft()-500}, 'slow');
}

//error handler.
function errorHandler(xhr,status,error){
	alert(error.toString());
}


//csv geneartion for frequency where clause.
function generateFrequencyFilterCsv(){
	var csv = "";
	$.each($(".freqFilterApplied"),function(k,v){
		csv = csv+"'"+$(v).text()+"',";
	});	
	if(csv.length>1){
		csv = csv.substring(csv,csv.length-1);
	}else{
		csv = "";
	}
	return csv;
}

//csv geneartion for flowtype where clause.
function generateFlowTypeFilterCsv(){
	var csv = "";
	$.each($(".flowFilterApplied"),function(k,v){
		csv = csv+"'"+$(v).text()+"',";
	});	
	if(csv.length>1){
		csv = csv.substring(csv,csv.length-1);
	}else{
		csv = "";
	}
	return csv;
}

//function for ajax parameters for lower grids.
function getBusinessDateAndFilterDetails(){
	var finalObj = {};
	finalObj.businessDateSelected = formatDateToPeriodId($("#cbdDatePicker").val());
	finalObj.isFrequencyFilterAppliedFlag = "N";
	finalObj.frequencyFilterCSV = "";
	finalObj.isFlowTypeFilterAppliedFlag = "N";
	finalObj.flowTypeFilterCSV = "";
	
	if($(".freqFilterApplied").length>0){
		finalObj.isFrequencyFilterAppliedFlag = "Y";
		finalObj.frequencyFilterCSV = generateFrequencyFilterCsv();
	}
	if($(".flowFilterApplied").length>0){
		finalObj.isFlowTypeFilterAppliedFlag = "Y";
		finalObj.flowTypeFilterCSV = generateFlowTypeFilterCsv();
	}
	return finalObj;
}



/*data functions and formats*/
function dateFormatter(displayFormat){
	var ipDate = moment(displayFormat, "DD-MM-YYYY"); 
	//format that date into a different format
	return moment(ipDate).format(processingDateFormat);
}
function dateFormatterForNewDate(displayFormat){
	
	var ipDate = moment(displayFormat, "DD-MM-YYYY"); 

	//format that date into a different format
	//return moment(ipDate).format("MM-DD-YYYY");
	return moment(ipDate).valueOf();
}
function formatDateToPeriodId(displayFormat){
	var ipDate = moment(displayFormat, "DD-MM-YYYY"); 

	//format that date into a different format
	return moment(ipDate).format("YYYYMMDD");
}
function formatDateForNewDate(dateRec){
	var ipDate = moment(dateRec, "DD-MM-YYYY"); 
	//format that date into a different format
	return moment(ipDate).format("YYYY-MM-DD");
}
/*data functions and formats*/

/*graph DS*/


function Graph() {
  this.vertices = new Array();
  this.edges = new Array();
  this.numberOfEdges = 0;
  this.vertexLevelMap = new Map();
}

Graph.prototype.calcLevelsForNodes = function(){
	var currGraph = this;
	currGraph.edges.forEach(function(edge) {
		if (currGraph.vertexLevelMap.get(edge[0]) >= currGraph.vertexLevelMap.get(edge[1])){
				currGraph.vertexLevelMap.set(edge[1],currGraph.vertexLevelMap.get(edge[0])+1);
			}
		});
		if(!currGraph.validateLevels())
        		currGraph.calcLevelsForNodes();
};

Graph.prototype.validateLevels = function(){
	var ret =true;
	var currGraph = this;
	currGraph.edges.forEach(function(edge) {
		if (currGraph.vertexLevelMap.get(edge[0])>=currGraph.vertexLevelMap.get(edge[1])){
				ret = false
			}
		});
		return ret;
};

Graph.prototype.isEmpty = function(){
	if(this.vertices.length==0)
				return true;
			return false;
};


Graph.prototype.containsNode = function(vertex){
	for(var i=0;i<this.vertices.length;i++)
				if(this.vertices[i]==vertex)
					return true;
			return false;
};

Graph.prototype.containsEdge = function(from,to){
	for(var i=0;i<this.edges.length;i++)
				if(this.edges[i][0]==from&&this.edges[i][1]==to)
					return true;
			return false;
};

Graph.prototype.reset = function(){
	this.vertices = new Array();
	this.edges = new Array();
	this.vertexLevelMap = new Map();
};


Graph.prototype.addVertex = function(vertex) {
	if(!this.containsNode(vertex)){
			this.vertices.push(vertex);
			this.vertexLevelMap.set(vertex,0);
			return true;
	}
	return false;
};
Graph.prototype.removeVertex = function(vertex) {
  var ret = false;
  var index = this.vertices.indexOf(vertex);
  if(~index) {
    this.vertices.splice(index, 1);
	this.vertexLevelMap.delete(vertex);
	ret = true;
  }
  
  while(this.edgesFrom(vertex).length>0) {
    var adjacentVertex = this.edgesFrom(vertex).pop();
    this.removeEdge(adjacentVertex, vertex);
  }
  
  while(this.edgesTo(vertex).length>0) {
    var adjacentVertex = this.edgesTo(vertex).pop();
    this.removeEdge(adjacentVertex, vertex);
  }
  return ret;
  this.calcLevelsForNodes();
};

Graph.prototype.addEdge = function(from, to) {
  var ret = false;
  if(!this.containsEdge(from,to)){
				this.addVertex(from);
				this.addVertex(to);
				this.edges.push([from,to]);
				this.numberOfEdges++;
				ret = true;
    			this.calcLevelsForNodes();
			}
			return ret;
};
Graph.prototype.removeEdge = function(vertex1, vertex2) {
	var removedEdges = this.edges.splice([vertex1,vertex2], 1).length;
    removedEdges = removedEdges  + this.edges.splice([vertex2,vertex1], 1).length;
	this.numberOfEdges = this.numberOfEdges - removedEdges;
	if(removedEdges>0)
		this.calcLevelsForNodes();
};

Graph.prototype.edgesFrom= function(vertex) {
	var arr = new Array();
			for(var i=0;i<this.edges.length;i++)
				if(this.edges[i][0]==vertex){
					if(this.edges[i][2])
						arr.push([this.edges[i][1],this.edges[i][2]]);
					else
						arr.push(this.edges[i][1]);
				}
			if(arr.length==0)
				return [];
			return arr;
}

Graph.prototype.edgesTo= function(vertex) {
	var arr = new Array();
			for(var i=0;i<this.edges.length;i++)
				if(this.edges[i][1]==vertex){
					if(this.edges[i][2])
						arr.push([this.edges[i][0],this.edges[i][2]]);
					else
						arr.push(this.edges[i][0]);
				}
			if(arr.length==0)
				return [];
			return arr;
}

Graph.prototype.size = function() {
  return this.vertices.length;
};
Graph.prototype.relations = function() {
  return this.numberOfEdges;
};
Graph.prototype.traverseDFS = function(vertex, fn) {
  if(!~this.vertices.indexOf(vertex)) {
    return console.log('Vertex not found');
  }
  var visited = [];
  this._traverseDFS(vertex, visited, fn);
};
Graph.prototype._traverseDFS = function(vertex, visited, fn) {
  visited[vertex] = true;
  if(this.edgesFrom(vertex).length>0) {
    fn(vertex);
  }
  if(this.edgesFrom(vertex).length>0)
	  for(var i = 0; i < this.edgesFrom(vertex).length; i++) {
		if(!visited[this.edgesFrom(vertex)[i]]) {
		  this._traverseDFS(this.edgesFrom(vertex)[i], visited, fn);
		}
	  }
};
Graph.prototype.traverseBFS = function(vertex, fn) {
  if(!~this.vertices.indexOf(vertex)) {
    return console.log('Vertex not found');
  }
  var queue = [];
  queue.push(vertex);
  var visited = [];
  visited[vertex] = true;

  while(queue.length) {
    vertex = queue.shift();
    fn(vertex);
	if(this.edgesFrom(vertex).length>0)
		for(var i = 0; i < this.edgesFrom(vertex).length; i++) {
		  if(!visited[this.edgesFrom(vertex)[i]]) {
			visited[this.edgesFrom(vertex)[i]] = true;
			queue.push(this.edgesFrom(vertex)[i]);
		  }
		}
  }
};

Graph.prototype.traverseBFSTesting = function(vertex, fn) {
  var curGraph = this;
  if(!~curGraph.vertices.indexOf(vertex)) {
    return console.log('Vertex not found');
  }
	var queue = [];
	var visited = {};
    var distance = 0;
	var array = [vertex];
    queue.push(array);
	visited[vertex] = true;
	
	while (queue.length > 0) {
      var level = queue.shift();
      var adjs = [];
      _.each(level, function (element) {
        _.each(curGraph.edgesFrom(element), function (node) {
			
          if (!visited[node]) {
            adjs.push(node);
            visited[node] = true;
          }
        });
        queue.push(adjs);
      });
      if (adjs.length) {
        console.log(adjs);
      }
    }
	
};



Graph.prototype.pathFromTo = function(vertexSource, vertexDestination) {
  if(!~this.vertices.indexOf(vertexSource)) {
    return console.log('Vertex not found');
  }
  var queue = [];
  queue.push(vertexSource);
  var visited = [];
  visited[vertexSource] = true;
  var paths = [];

  while(queue.length) {
    var vertex = queue.shift();
	if(this.edgesFrom(vertex).length>0)
		for(var i = 0; i < this.edgesFrom(vertex).length; i++) {
		  if(!visited[this.edgesFrom(vertex)[i]]) {
			visited[this.edgesFrom(vertex)[i]] = true;
			queue.push(this.edgesFrom(vertex)[i]);
			// save paths between vertices
			paths[this.edgesFrom(vertex)[i]] = vertex;
		  }
		}
  }
  if(!visited[vertexDestination]) {
    return undefined;
  }

  var path = [];
  for(var j = vertexDestination; j != vertexSource; j = paths[j]) {
    path.push(j);
  }
  path.push(j);
  return path.reverse().join('-');
};
Graph.prototype.print = function() {
  
  console.log(this.vertices.map(function(vertex) {
    return (vertex + ' -> ' + this.edgesFrom(vertex).join(', ')).trim();
  }, this).join(' | '));
};

/*graph DS*/
