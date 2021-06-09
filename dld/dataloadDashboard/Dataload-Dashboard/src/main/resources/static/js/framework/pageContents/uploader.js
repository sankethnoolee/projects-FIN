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

$(document).ready(function(){
    //setcurrentBusinessDate();
	$("#cbdDatePicker").datepicker({
	    changeYear: true,
	    changeMonth: true,
		format: displayDateFormat,
		autoclose: true,
	}).on('changeDate',cbdChanged);	

	$.ajax({
		 method: "GET",
		url: "/dldwebapplication/getcurrrentBusinessDateStatisticsForLoad",
		success:function(data)
		{
			maxCurBusinessDate=new Date(formatDateForNewDate(data.curBusinessDate));
			$("#cbdIncrement").hide();
			$("#cbdDatePicker").datepicker('update', new Date(formatDateForNewDate(data.curBusinessDate)));
			$("#cbdDatePicker").datepicker('setEndDate', new Date(formatDateForNewDate(data.curBusinessDate)));
			//cbdChanged();	
		}
	})	



	//$("#cbdDatePicker").val("24-07-2017")
    addLabelsCreate();
    $(function(){
        $('#upload').change( function(){
            filepath=$(this).val().split("\\");
            $("#uploaderFileName").text(filepath[2]);
        });
    });
    $('#upload').click( function(){
		$("#effectiveDate").removeAttr('disabled');
        $(this).val("");
        $(".errorMessage").css("display","none");
        $(".saveMessage").css("display","none");
        $(".finalMessage").css("display","none");
		$(".warningMessage").css('display', 'none'); 
        $(".finalErrorMessage").css("display","none");
        $(".valStatusMenu").css("display","none");
        $(".validateFileButton").css("display","none");
        $(".saveFileButton").css("display","none");
        $(".downloadReportButton").css('display', 'none');
        if($("#hideForVal").text().toLowerCase()=="hide"){
            $("#hideForVal").text("Show");
            $(".valStatusDetails").slideToggle();
        }
    });
    var url = 'handleUploadMetadata.htm';
    $('#upload').fileupload({
        url: url,
        dataType: 'json',
        formData:{
            effectiveDate:$("#cbdDatePicker").val()
        },
        add: function (e, data) {
            $("#ajaxMaskForLoad").css("display","block");
            $(".fileUploadForm").css("display","none");
            data.submit();
        },
        success: function (response) {
            if(response.isFileFormatCorrect){
                if(response.isHeaderCorrect){
                    if(response.isFileNotEmpty){
                        if (response.success) {
                            $(".sheetHeaderValidation").css("display","none");
                            $("#uploaderFileFrame").attr('src', response.uploadedFilePath);
                            $("#userSessionToken").text("").text(response.userTokenSession);
                            $("#uploadedFileContainer").css('display', 'block');
                            $(".validateFileButton").css('display', 'block');
                            $(".helpNoteIndex").css('display', 'none');
							$(".warningMessage").css('display', 'none');
                            $("#uploaderFileName").text(response.fileName);
                            $(".fileCount").css("display","block");
                            //$("#tagCount").text(response.clientCount);
                            $("#entityMasterCount").text(response.entityMasterCount);
                            $("#entityOwnerCount").text(response.entityOwnerCount);
                            $("#flowTypeCount").text(response.flowTypesCount);
                            $("#taskRepositoryCount").text(response.taskRepositoryCount);
							$("#taskMasterCount").text(response.taskMasterCount);
							$("#entityDetailCount").text(response.taskEntityDetailCount);
							
							
                           
                        }else{
                            $("#uploaderFileName").text(" ");
                            alert("File uploading failed.Please contact administration");
                        }
                    }else{
                        $(".helpNoteIndex").css("display","block");
                        $(".sheetHeaderValidation").css("display","none");
                        $("#uploadedFileContainer").css('display', 'none');
                        $(".valStatusMenu").css('display', 'none');
                        $(".finalStatusMenu").css('display', 'none'); 
                        $(".fileCount").css("display","none");
                        $(".validateFileButton").css('display', 'none');
                        $(".downloadReportButton").css('display', 'none');
                        $(".saveFileButton").css('display', 'none'); 
						$(".warningMessage").css('display', 'none');
                        $("#uploaderFileName").text(" ");                        
                        alert("File is empty.");
                    }
                }else{
                    $(".helpNoteIndex").css("display","none");
                    $(".sheetHeaderValidation").css("display","block");
                    $("#fileFormatError").css("display","block");
                    $("#uploadedFileContainer").css('display', 'none');
                    $(".validateFileButton").css('display', 'none');
                    $(".downloadReportButton").css('display', 'none');
                    $(".fileCount").css("display","none");
                    $(".valStatusMenu").css('display', 'none');
                    $(".finalStatusMenu").css('display', 'none');
                    $(".validateFileButton").css('display', 'none');
					$(".warningMessage").css('display', 'none');
                    $(".saveFileButton").css('display', 'none'); 
                    $(".downloadReportButton").css('display', 'none');
                    
                }
            }else{
                $(".helpNoteIndex").css("display","block");
                $(".sheetHeaderValidation").css("display","none");
                $("#uploadedFileContainer").css('display', 'none');
                $(".valStatusMenu").css('display', 'none');
                $(".finalStatusMenu").css('display', 'none'); 
				$(".warningMessage").css('display', 'none');
                $(".fileCount").css("display","none");
                $(".validateFileButton").css('display', 'none');
                $(".downloadReportButton").css('display', 'none');
                $(".saveFileButton").css('display', 'none');  
                $("#uploaderFileName").text(" ");                
                alert("File format is invalid.");
            }
        },
        done: function (e, data) {
            $("#ajaxMaskForLoad").css("display","none");
            $(".fileUploadForm").css("display","block");
        }
    });
    
    $("#downloadTemplate").click(function(){
        downLoadTemplate();
    });
	$("#saveFile").click(function(){
        $("#ajaxMaskForSave").css("display","block");
        $(".fileUploadForm").css("display","none");
		$.ajax({
			 url: 'handleSaveMetadata.htm',
			 data:{
				effectiveDate:$("#cbdDatePicker").val(),
				userSessionToken:$("#userSessionToken").text(),
				fileName:$("#uploaderFileName").text()
			 },
			type:'POST',
			success:function(res){
                var response=JSON.parse(res);
                if(response.success){
                    $(".finalMessage").css("display","block");
                    $(".saveMessage").css('display', 'none'); 
                    $(".finalErrorMessage").css("display","none");                                        
                    alert("Process completed successfully.");
                    location.reload();
                }else{
                    $(".finalMessage").css("display","none");  
                    $(".saveMessage").css("display","none");  
                    $(".finalErrorMessage").css("display","block");                    
                    $(".fileUploadForm").css("display","block");                    
                }
            }
		}).done(function(){
            $("#ajaxMaskForSave").css("display","none");
        });
	});
    $("#validateFile").click(function(){
       var fileName= $("#uploaderFileName").text();
       getUploaderValidation(fileName);
       $(".fileUploadForm").css("display","none");
    });
    $("#hideForVal").click(function(){
        if($("#hideForVal").text().toLowerCase()=="show"){
            $("#hideForVal").text("Hide");
        }else{
             $("#hideForVal").text("Show");
        }
      $(".valStatusDetails").slideToggle(); 
    });
    $("#downloadReport").click(function(){
        var fileName= $("#uploaderFileName").text();
        downLoadFile(fileName);
    });
});
function setcurrentBusinessDate(){
	$("#effectiveDate").datepicker({
		dateFormat: 'dd-mm-yy',
		changeMonth: true,
		changeYear: true,
		inline: true,
		constrainInput:true,
		maxDate: $.datepicker.parseDate('dd-mm-yy', currentBusinessDate)
			});
    $("#effectiveDate").datepicker("setDate", currentBusinessDate);
}
function getUploaderValidation(fileName){
    $("#ajaxMaskForVal").css("display","block");
     $.ajax({
        url: './handleMetadataValidation.htm',
        type:"POST",
        data:{
				userSessionToken:$("#userSessionToken").text(),
                effectiveDate:$("#cbdDatePicker").val(),
                fileName:fileName
			},
			dataType: 'json',
	        success: function (response) { 
	            $("#uploaderFileFrame").attr('src', response.uploadedFilePath);
	            $(".valStatusMenu").css('display', 'block');
					if (response.success){
						$(".downloadReportButton").css('display', 'block');
						setValidationStatus(response.valStatus) 
						if(response.hasError){
								$(".errorMessage").css('display', 'block');  
								$(".warningMessage").css('display', 'none'); 
								$(".saveMessage").css('display', 'none');   
								$(".saveFileButton").css('display', 'none'); 
								$(".validateFileButton").css('display', 'none');
						}
						else if(response.hasWarning)
						{
								$(".warningMessage").css('display', 'block'); 
								$("#warningMessageId").css('display', 'block');								
								$(".saveFileButton").css('display', 'block'); 
								$(".errorMessage").css('display', 'none'); 
								$(".validateFileButton").css('display', 'none');			
						}
						
						else{
							$('#effectiveDate').attr('disabled', true);
							$(".saveFileButton").css('display', 'block'); 
							$(".errorMessage").css('display', 'none');  
							$(".saveMessage").css('display', 'block');
							$(".warningMessage").css('display', 'none'); 
							$(".validateFileButton").css('display', 'none');
						}
					}else{
						alert("validation failed.Contact Administration");
						$(".validateFileButton").css('display', 'block');
					}
	        }
	    }).done(function(){
            $("#ajaxMaskForVal").css("display","none");
            $(".fileUploadForm").css("display","block");
    });
}
function setHTMLLables(key,selector){
	var getValue = jQuery.i18n.prop(key);
	$(selector).text(getValue);
}
function addLabelsCreate(){
	setHTMLLables("bulkUploader","#bulkUploader");
	setHTMLLables("addFiles","#addFiles");
	setHTMLLables("downloadTemplate","#downloadTemplate");
	setHTMLLables("loading","#ajaxMaskForLoad");
	setHTMLLables("validating","#ajaxMaskForVal");
	setHTMLLables("saving","#ajaxMaskForSave");
	setHTMLLables("chooseFile",".chooseFile");
	setHTMLLables("uploadFile","#uploadFile");
	setHTMLLables("validate","#validateFile");
	setHTMLLables("downloadReport","#downloadReport");
	setHTMLLables("save","#saveFile");
	setHTMLLables("effectiveFrom","#effectiveFromId");
	setHTMLLables("selectTheFileFirst","#valMsg");
	setHTMLLables("client","#tagsId");
	setHTMLLables("entitymstr","#entityMasterId");
	setHTMLLables("entityownr","#entityOwnerId");
	setHTMLLables("flowtype","#flowTypeId");
	setHTMLLables("errorMsg","#errorMessageId");
	setHTMLLables("warningMsg","#warningMessageId");
	setHTMLLables("saveMsg","#saveMessageId");
    setHTMLLables("finalMsg","#finalMessageId");
    setHTMLLables("finalErrorMessage","#finalErrorMessageId");
	setHTMLLables("validationStatus","#valStatusLabelId");
	setHTMLLables("validateSuccessfully","#labelStyleForStatus1");
	setHTMLLables("validateSuccessfully","#labelStyleForStatus2");
	setHTMLLables("validateSuccessfully","#labelStyleForStatus3");
	setHTMLLables("validateSuccessfully","#labelStyleForStatus4");
	setHTMLLables("validateWithErrors","#labelStyleForStatus5");
	setHTMLLables("validateWithErrors","#labelStyleForStatus6");
	setHTMLLables("validateWithErrors","#labelStyleForStatus7");
	setHTMLLables("validateWithErrors","#labelStyleForStatus8");
	setHTMLLables("validateSuccessfully","#labelStyleForStatus9");
	setHTMLLables("validateSuccessfully","#labelStyleForStatus9");
	setHTMLLables("validateWithErrors","#labelStyleForStatus10");
	setHTMLLables("validateSuccessfully","#labelStyleForStatus11");
	setHTMLLables("validateWithErrors","#labelStyleForStatus12");
	setHTMLLables("helpNotes","#helpNotes");
	setHTMLLables("fileFormatError","#fileFormatError");
	setHTMLLables("taskmaster","#taskMasterId");
	setHTMLLables("entitydetail","#entityDetailId");
	
	setHTMLLables("taskRepository","#taskRepositoryId");
}	
function downLoadFile(fileName){
    effectiveDate=$("#cbdDatePicker").val();
    var downloadUrl = './handleDownloadValidationReport.htm?userSessionToken='+userSessionToken+"&effectiveDate="+effectiveDate+"&fileName="+fileName;
    $('#hiddenDownloader').attr('src', downloadUrl);
}
function downLoadTemplate(){
	 effectiveDate=$("#cbdDatePicker").val();
    var downloadUrl = './handleMetadataDownload.htm?effectiveDate='+effectiveDate;
    $('#hiddenDownloader').attr('src', downloadUrl);
}
function setValidationStatus(valStatus){
	$("#entityMasterValStatus,#entityMasterValErrorStatus,#entityOwnerValErrorStatus,#flowTypeValStatus,#entityOwnerValStatus").text("");
	$("#flowTypeValErrorStatus,#taskMasterValErrorStatus ,#taskRepositoryValErrorStatus,#taskRepositoryValStatus,#taskMasterValStatus,entityDetailValStatus,entityDetailValErrorStatus").text("");
    $.each(valStatus,function(key,val){
       if(val.entityName.toLowerCase()=="entitymaster"){
            $("#entityMasterValStatus").text(val.noOfSuccess);
            $("#entityMasterValErrorStatus").text(val.noOfError);
        }if(val.entityName.toLowerCase()=="entityowner"){
            $("#entityOwnerValStatus").text(val.noOfSuccess);
            $("#entityOwnerValErrorStatus").text(val.noOfError);
        }if(val.entityName.toLowerCase()=="flow types"){
            $("#flowTypeValStatus").text(val.noOfSuccess);
            $("#flowTypeValErrorStatus").text(val.noOfError);
        }
		if(val.entityName.toLowerCase()=="task master"){
            $("#taskMasterValStatus").text(val.noOfSuccess);
            $("#taskMasterValErrorStatus").text(val.noOfError);
        }
		if(val.entityName.toLowerCase()=="task repositories"){
            $("#taskRepositoryValStatus").text(val.noOfSuccess);
            $("#taskRepositoryValErrorStatus").text(val.noOfError);
        }
		if(val.entityName.toLowerCase()=="task entity detail"){
            $("#entityDetailValStatus").text(val.noOfSuccess);
            $("#entityDetailValErrorStatus").text(val.noOfError);
        }
    });
}


function incrementDate(){
	
	var new_date = moment($("#cbdDatePicker").val(), "DD-MM-YYYY").add(1, 'days');
	var changedDate = new Date(dateFormatterForNewDate(new_date));
	
	moment(maxCurBusinessDate, "DD-MM-YYYY")
	if(new_date.isSame(moment(maxCurBusinessDate, "DD-MM-YYYY"),"days"))
		$("#cbdIncrement").hide();
	//changing CBD.
	$(".cbdDateDay").text(changedDate.getDate());
	$(".cbdDateMonth").text(monthNames[changedDate.getMonth()]);
	$(".cbdDateYear").text(changedDate.getFullYear());
	$(".cbdDateWeek").text(weekday[changedDate.getDay()]);
	$("#cbdDatePicker").val(dateFormatter(new_date));

		//previousDay
	var new_date = moment(changedDate, "DD-MM-YYYY").subtract(1, 'days');
	var prevDate = new Date(dateFormatterForNewDate(new_date));
	$("#previousDayCustomFormat").find(".previousDateDay").text(prevDate.getDate());
	$("#previousDayCustomFormat").find(".previousDateMonth").text(monthNames[prevDate.getMonth()]);
	$("#previousDayCustomFormat").find(".previousDateYear").text(prevDate.getFullYear());
	$("#previousDayCustomFormat").find(".previousDateWeek").text(weekday[prevDate.getDay()]);
	
	 var prevMonNew = moment(changedDate,"DD-MM-YYYY").subtract(1,'months').endOf('month').format("DD-MM-YYYY");
		var prevMonthDate = new Date(dateFormatterForNewDate(prevMonNew));
	//previousMonthEnd
	$("#previousMonthCustomFormat").find(".previousDateDay").text(prevMonthDate.getDate());
	$("#previousMonthCustomFormat").find(".previousDateMonth").text(monthNames[prevMonthDate.getMonth()]);
	$("#previousMonthCustomFormat").find(".previousDateYear").text(prevMonthDate.getFullYear());
	$("#previousMonthCustomFormat").find(".previousDateWeek").text(weekday[prevMonthDate.getDay()]);


}

function decrementDate(){
	$("#cbdIncrement").show();
	var new_date = moment($("#cbdDatePicker").val(), "DD-MM-YYYY").subtract(1, 'days');
	var changedDate = new Date(dateFormatterForNewDate(new_date));
	//changing CBD.
	$(".cbdDateDay").text(changedDate.getDate());
	$(".cbdDateMonth").text(monthNames[changedDate.getMonth()]);
	$(".cbdDateYear").text(changedDate.getFullYear());
	$(".cbdDateWeek").text(weekday[changedDate.getDay()]);
	$("#cbdDatePicker").val(dateFormatter(new_date));

}

function dateFormatterForNewDate(displayFormat){
	
	var ipDate = moment(displayFormat, "DD-MM-YYYY"); 

	//format that date into a different format
	return moment(ipDate).format("MM-DD-YYYY");
}


function dateFormatter(displayFormat){
	
	var ipDate = moment(displayFormat, "DD-MM-YYYY"); 

	//format that date into a different format
	return moment(ipDate).format(processingDateFormat);
}

function formatDateForNewDate(dateRec){
	var ipDate = moment(dateRec, "DD-MM-YYYY"); 
	//format that date into a different format
	return moment(ipDate).format("YYYY-MM-DD");
}

function cbdChanged(){
	
	var changedDate = new Date(dateFormatterForNewDate($("#cbdDatePicker").val()));
	moment(maxCurBusinessDate, "DD-MM-YYYY")
	var new_date = moment($("#cbdDatePicker").val(), "DD-MM-YYYY")
	if(new_date.isSame(moment(maxCurBusinessDate, "DD-MM-YYYY"),"days"))
		$("#cbdIncrement").hide();
	else
		$("#cbdIncrement").show();
	
	//changing CBD.
	$(".cbdDateDay").text(changedDate.getDate());
	$(".cbdDateMonth").text(monthNames[changedDate.getMonth()]);
	$(".cbdDateYear").text(changedDate.getFullYear());
	$(".cbdDateWeek").text(weekday[changedDate.getDay()]);
	
}