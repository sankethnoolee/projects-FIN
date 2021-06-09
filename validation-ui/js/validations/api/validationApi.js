$(document).ready(function() {
	console.log("sanketh");
	downloadValidationTemplateLayout($(".api-slab")[0]);
	
});

//ajax loader.
$(document).ajaxStart(function(){
	$(".overlay").css("display","block");
});
$(document).ajaxStop(function(){
	$(".overlay").css("display","none");
});

function downloadValidationTemplateLayout(elm){
	$("#respContent").empty();
	$(".selected").removeClass("selected");
	$(elm).addClass("selected");
	$("#kvContainer").empty();
	$("#kvContainer").append(""
		+'<button class="button button2" onclick= "downloadValidationTemplate()">Send <i class="fa fa-location-arrow"></i></button>'
	+"");
	
}

function uploadValidationLayout(elm){
	$("#respContent").empty();
	$("#kvContainer").empty();
	$(".selected").removeClass("selected");
	$(elm).addClass("selected");
	
	$("#kvContainer").append(""
		+'<button class="button button2" onclick="uploadValidation()">Send <i class="fa fa-location-arrow"></i></button>'
		+'<table id="key-value-table">'
		  +'<tr>'
			+'<th>Key</th>'
			+'<th>Value</th>'
		  +'</tr>'
		  +'<tr>'
			+'<td>File</td>'
			+'<td><form name="uploadForm" id="uploadForm" action="javascript:;" enctype="multipart/form-data" method="post" accept-charset="utf-8">'
						+'<input type="file" name="file" id="file"/>'
				+'</form>'
			+'</td>'
		  +'</tr>'
		+'</table>'
	+"");
	
}

function exportValidationLayout(elm){
	$("#respContent").empty();
	$("#kvContainer").empty();
	$(".selected").removeClass("selected");
	$(elm).addClass("selected");
	
	$("#kvContainer").append(""
		+'<button class="button button2" onclick= "exportValidation()">Send <i class="fa fa-location-arrow"></i></button>'
		+'<table id="key-value-table">'
		  +'<tr>'
			+'<th>Key</th>'
			+'<th>Value</th>'
		  +'</tr>'
		  +'<tr>'
			+'<td>Effective Date</td>'
			+'<td><input id="kvDate" placeholder = "Mandatory - (Format : dd-MM-yyyy)" class = "kv-input"/></td>'
		  +'</tr>'
		  +'<tr>'
			+'<td>Return CSV</td>'
			+'<td><input placeholder = "Optional" class = "kv-input"/></td>'
		  +'</tr>'
		  +'<tr>'
			+'<td>Validation Status CSV</td>'
			+'<td><input placeholder = "Optional" class = "kv-input"/></td>'
		  +'</tr>'
		+'<tr>'
			+'<td>Validation Type CSV</td>'
			+'<td><input placeholder = "Optional" class = "kv-input"/></td>'
		  +'</tr>'
		+'<tr>'
			+'<td>Validation Group CSV</td>'
			+'<td><input placeholder = "Optional" class = "kv-input"/></td>'
		  +'</tr>'
		  +'<tr>'
			+'<td>Entity Type CSV</td>'
			+'<td><input placeholder = "Optional" class = "kv-input"/></td>'
		  +'</tr>'
		  +'<tr>'
			+'<td>Validation Entity Name CSV</td>'
			+'<td><input placeholder = "Optional" class = "kv-input"/></td>'
		  +'</tr>'
		+'</table>'
	+"");
	
}


function downloadValidationTemplate(){
	var url = encodeURI("downloadtemplateforentity.htm");
	var newTab = window.open (url.split("###amp###").join("%26").split("###hash###").join("%23"), "_blank");
		
}

function exportValidation(){
	if($("#kvDate").val().trim()==""){
		alert("Please fill mandatory field(s).");
	}else{
		var urlFinal="exportvalidationdata.htm?effectiveDate="+$("#kvDate").val().trim();
		if($($(".kv-input")[1]).val().trim()!=""){
			urlFinal = urlFinal+"&validationReturnsCSV="+$($(".kv-input")[1]).val().trim();
		}
		if($($(".kv-input")[2]).val().trim()!=""){
			urlFinal = urlFinal+"&validationStatusCSV="+$($(".kv-input")[2]).val().trim();
		}
		if($($(".kv-input")[3]).val().trim()!=""){
			urlFinal = urlFinal+"&validationTypeCSV="+$($(".kv-input")[3]).val().trim();
		}
		if($($(".kv-input")[4]).val().trim()!=""){
			urlFinal = urlFinal+"&validationGroupCSV="+$($(".kv-input")[4]).val().trim();
		}
		if($($(".kv-input")[5]).val().trim()!=""){
			urlFinal = urlFinal+"&entityTypeCSV="+$($(".kv-input")[5]).val().trim();
		}
		if($($(".kv-input")[6]).val().trim()!=""){
			urlFinal = urlFinal+"&validationEntityNameCSV="+$($(".kv-input")[6]).val().trim();
		}
		 $.ajax({
			url: urlFinal,
			type: 'GET',
			dataType: 'json',
			success: function(response) {
				if (response.success == true) {
					$('#hiddenDownloader').attr('src', 'exporttoexcelhiddenresult.htm');
					
				} else {

					$("#respContent").empty();
					$("#respContent").text("Data not Available For Selected Values.")
				}
				$(".overlay").css("display", "none");
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) { 
				$("#respContent").empty();
				$("#respContent").text("Status: " + textStatus +" | Message : Something went wrong!")
				 
			}
		}).done(function() {});
	}
}

function uploadValidation(){
	if($("#file").val()){
		var data = new FormData();
		data.append('file', $('#file').prop('files')[0]);
		$.ajax({
			type: 'POST',               
			processData: false, // important
			contentType: false, // important
			data: data,
			url: "validationsforentityupload.htm",
			dataType : 'json',  
			success: function(jd){
				//closeuplodFileModal();
				if(jd.success)
				{
					$("#respContent").empty();
					$("#respContent").text(jd.returnMessage);
				}
				else{
					if(jd.returnStatus=="VALIDATION_FAILED"){
						$('#hiddenDownloader').attr('src', 'validationsforentityuploaderrorfile.htm');
						$("#respContent").empty();
						$("#respContent").text("Please refer downloaded error sheet.")
					}
					$("#respContent").empty();
					$("#respContent").text(jd.returnMessage);
				}
			},
			error:function(xhr,status,error){
				$("#respContent").empty();
				$("#respContent").text("Invalid file format.");
				//evt.stopPropagation();
			}
			
		}); 
	}else{
		alert("Please select a file to upload");
	}
}