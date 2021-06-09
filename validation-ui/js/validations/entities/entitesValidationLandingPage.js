var gridFilter=[];
var editValidationArray = [];
var isEdit = "";
var defaultArrayVal = [];
var previlegeValue = "";
var gridPageNo = 1;
var effectiveDateObject={
	"effective-date-id":moment().format(momentDateFormat),
	"effective-endDate-exportDef":"",
	"effective-startDate-exportDef":moment().format(momentDateFormat),
};
$(document).ready(function () {
	//var displayDateFormat = 'dd-mm-yyyy';
	var ajaxcsrf = {};
	ajaxcsrf[csrfHeaderParam] = csrfTokenX;
	//console.log(ajaxcsrf);
    $.ajaxSetup({
        headers: ajaxcsrf
    });
	$("#effective-date-id,#effective-startDate-exportDef").datepicker({
        format: displayDateFormat,
        autoclose: true,
		forceParse:false,
    }).on('hide', function(e) {
		$("#effective-endDate-exportDef").next('p').remove();
		$("#effective-endDate-exportDef").removeClass("dropDown-redBorder");
		if(moment($(this).val(), momentDateFormat, true).isValid()==false){
			$("#"+$(this).attr("id")).datepicker('setDate', effectiveDateObject[$(this).attr("id")]);
		}
		else{
			effectiveDateObject[$(this).attr("id")]=$(this).val();
		}
		var momentA = moment($('#effective-startDate-exportDef').val(),momentDateFormat);
		var momentB = moment($("#effective-endDate-exportDef").val(),momentDateFormat);
		if(moment($("#effective-endDate-exportDef").val(), momentDateFormat, true).isValid()==true && moment(momentB).isAfter(momentA)==false && moment(momentB).isSame(momentA)==false){
			endDateLessThanStratDate(momentA,momentB,"effective-endDate-exportDef");
		}
		if($(this).attr("id")=="effective-startDate-exportDef")
			restrictExportEndDate();
	});
	$("#effective-endDate-exportDef").datepicker({
        format: displayDateFormat,
        autoclose: true,
		forceParse:false,
		clearBtn:true,
    }).on('hide', function(e) {
		$("#effective-endDate-exportDef").next('p').remove();
		$("#effective-endDate-exportDef").removeClass("dropDown-redBorder");
		var momentA = moment($('#effective-startDate-exportDef').val(),momentDateFormat);
		var momentB = moment($("#effective-endDate-exportDef").val(),momentDateFormat);
		if(moment($(this).val(), momentDateFormat, true).isValid()==false){
			if($(this).val().trim()=="" || $(this).val()==null){
				effectiveDateObject[$(this).attr("id")]="";
				$("#"+$(this).attr("id")).datepicker('setDate','');
			}
			else{
				$("#"+$(this).attr("id")).datepicker('setDate', effectiveDateObject[$(this).attr("id")]);
			}
		}
		else if(moment(momentB).isAfter(momentA)==false && moment(momentB).isSame(momentA)==false){
			endDateLessThanStratDate(momentA,momentB,"effective-endDate-exportDef");
		}
		else{
			effectiveDateObject[$(this).attr("id")]=$(this).val();
		}
	});
	$('#effective-date-id').datepicker('setDate', 'now');
    getMainFilterData();
    renderReturnValidationGrid();
    $("#uploadForm").submit(function(e) {
        e.preventDefault();
        $(".overlay").css("display", "block");
        $.ajax({
            url: 'masseditcheckcreationv2Ent.htm',
            data: new FormData($(this)[0]),
            cache: false,
            contentType: false,
            processData: false,
            type: 'POST',
            success: function(response) {
                var response = JSON.parse(response);
                $('#help-div').empty();
                $('#help-div').append("<strong>" + response.returnStatus + "!</strong> " + response.returnMessage);
                if (response.returnStatus == 'VALIDATION_FAILED') {
                    $('#hiddenDownloader').attr('src', 'masseditcheckcreationv2EntErrorFile.htm');
                    $("#upload-modal").modal("hide");
                    $("#help-div-parent").removeClass('success').addClass('error').show();
                } else if (response.returnStatus == 'SUCCESS') {
                	$("#help-div-parent").removeClass('error').addClass('success').show();
                    renderReturnValidationGrid();
                }
                $(".overlay").css("display", "none");
            },
            failure: function() {
                $('#help-div').empty();
                $('#help-div').append("<strong>Failure!</strong> Error Failed while uploading");
                $("#help-div-parent").show();
                $(".overlay").css("display", "none");
            }
        });
    });
    $('.k-i-filter').addClass('kendoFilterDeselected');
    $('.k-state-active .k-i-filter').addClass('kendoFilterSelected');
    $('#upload-modal').on('hidden.bs.modal', function() {
        $('#file').val("");
        $('#help-div-parent').hide();
    });
    $('.applyButtonEnableDisable').on('change', function() {
        $('#applyButtonId').removeAttr('disabled');
    });
    if (previlegeValue.toUpperCase() == "EDIT") {
        isView = "N";
    } else {
        isView = "Y";
    }
    if (isView == "Y") {
        $('#import-btn').attr('disabled', 'disabled');
        $('#new-validation-btn').attr('disabled', 'disabled');
    } else {
        $('#import-btn').removeAttr('disabled')
        $('#new-validation-btn').removeAttr('disabled');
    }
});

function renderReturnValidationGrid() {
    $(".overlay").css("display", "block");
    gridFilter = createFilterJson("grid");
    var gridWidth = $('#validationListKendo').width();
    if ($("#validationListKendo").data("kendoGrid") != undefined){
	    $("#validationListKendo").data("kendoGrid").destroy();
	}
	$("#validationListKendo").empty();
    $("#validationListKendo").kendoGrid({
        dataSource: {
            serverPaging: true,
            serverSorting: true,
            serverFiltering: true,
            pageSize: 10,
            page: gridPageNo,
            transport: {
                read: function(options) {
                    gridFilter.gridDetails.pageNo = options.data.page;
                    gridFilter.gridDetails.pageSize = options.data.pageSize;
                    gridFilter.gridDetails.sortOn = "validationEntities~~asc";
                    gridFilter.gridDetails.validationCode = "";
                    gridFilter.gridDetails.validationName = "";
                    gridFilter.gridDetails.validationEntityColumn = "";
                    if (options.data.sort != undefined && options.data.sort.length > 0) {
                        gridFilter.gridDetails.sortOn = options.data.sort[0].field + "~~" + options.data.sort[0].dir;
                    }
                    if (options.data.filter != undefined) {
                        for (var i = 0; i < options.data.filter.filters.length; i++) {
                            if (options.data.filter.filters[i].field != undefined) {
                                if (options.data.filter.filters[i].field == "validationCode") {
                                    gridFilter.gridDetails.validationCode = options.data.filter.filters[i].value;
                                }
                                if (options.data.filter.filters[i].field == "validationName") {
                                    gridFilter.gridDetails.validationName = options.data.filter.filters[i].value;
                                }
                                if (options.data.filter.filters[i].field == "validationEntityColumn") {
                                    gridFilter.gridDetails.validationEntityColumn = options.data.filter.filters[i].value;
                                }
                            }
                        }
                    }
                    $.ajax({
                        type: 'POST',
                        dataType: 'json',
                        url: 'getdataforvalidationgridEnt.htm',
                        data: {
                            gridData: JSON.stringify(gridFilter)
                        },
                        success: function(response) {
                            options.success(response);
                        }

                    });
                }
            },
            schema: {
                data: function(response) {
                    if (response.response.length == 0) {
                        return [];
                    }
                    return response["response"][0].data;
                },
                total: function(response) {
                    if (response.response.length == 0) {
                        return [];
                    }
                    return response["response"][0].total;
                }
            }
        },
        groupable: true,
        sortable: true,
        pageable: {
            input: true,
            numeric: false
        },
        filterable: {
            multi: true,
            extra: false,
            operators: {
                string: {
                    Contains: "Contains"
                }
            }
        },
        selectable: true,
        noRecords: true,
        change: function(options) {
            var grid = $("#validationListKendo").data("kendoGrid");
            gridPageNo = grid.dataSource._page;
            var selectedItem = grid.dataItem(this.select());
          //  if ($(this.select().children()[5+$('.k-grouping-header').children().length]).text().toLowerCase() == "active") {
            	 $(".overlay").css("display", "block");
            	    $.ajax({
            	        url: 'loadreturnvalidationeditpageForEnt.htm',
            	        type: 'POST',
            	        dataType: 'json',
            	        data: {
            	        	validationEntities: selectedItem["validationEntities"],
            	        	validationCode:selectedItem["validationCode"],
            	        	validationEntityColumn:selectedItem["validationEntityColumn"],
            	        	effectiveDate:selectedItem["effectiveDate"],
            	        	validationStatus:selectedItem["validationStatus"]
            	        },
            	        success: function(response) {
            	        	editValidationArray = response.editResponse;
            	        	isEdit = response.isedit;
            	        	defaultArrayVal = response.newResponse;
            	        	previlegeValue = response.previlegeType;
            	        	var group = defaultArrayVal.length==0?[]:defaultArrayVal[0].validationGroup;
            	            var optionalDropDownValues = defaultArrayVal.length==0?[]:defaultArrayVal[0].optionalDropDownValues;
            	            var statusArr = defaultArrayVal.length==0?[]:defaultArrayVal[0].validationStatus;
							var validationRuleType = defaultArrayVal.length==0?[]:defaultArrayVal[0].validationRuleType;
            	            appendOptionsCreateAndEdit(group, "groupReturnId");
            	            appendOptionsCreateAndEdit(optionalDropDownValues, "everyOccurenceDropDown");
            	            appendOptionsCreateAndEdit(statusArr, "statusId");
							appendOptionsCreateAndEdit(validationRuleType, "ruleTypeId");
							$('#groupReturnId').prop('selectedIndex',0);
            	            onEditMode(isEdit);
            	        }
            	    }).done(function() {
            	    	$('#validationListContainer').css("display", "none");
        	        	$('#validation-create-edit-con').css("display", "block");
        	            $(".overlay").css("display", "none");
            	    });
            	
            	
               /* var $form = $(document.createElement('form')).css({
                    display: 'none'
                }).attr("method", "POST").attr("action", "loadreturnvalidationeditpage.htm");
                var $input1 = $(document.createElement('input')).attr('name', 'validationReturns').val(selectedItem["validationReturns"]);
                var $input2 = $(document.createElement('input')).attr('name', 'validationCode').val(selectedItem["validationCode"]);
                var $input3 = $(document.createElement('input')).attr('name', 'validationSection').val(selectedItem["validationSection"]);
                var $input4 = $(document.createElement('input')).attr('name', 'effectiveDate').val(selectedItem["effectiveDate"]);
                $form.append($input1).append($input2).append($input3).append($input4);
                $("body").append($form);
                $form.submit();*/
           // }
        },
        columns: [{
                field: "validationEntities",
                title: "Entity",
                filterable: false,
                width: gridWidth * 0.1
            },
            {
                field: "validationCode",
                title: "Validation Code",
                width: gridWidth * 0.1
            },
            {
                field: "validationName",
                title: "Validation Name",
                width: gridWidth * 0.2
            },
            {
                field: "validationEntityColumn",
                title: "Column",
                width: gridWidth * 0.1
            },
            {
                field: "effectiveDate",
                title: "Effective Start Date",
                filterable: false,
                width: gridWidth * 0.1
            },
            {
                field: "validationStatus",
                title: "Status",
                filterable: false,
                width: gridWidth * 0.07
            },
            {
                field: "validationType",
                title: "Type",
                filterable: false,
                width: gridWidth * 0.07
            },
			{
                field: "validationRuleType",
                title: "Rule Type",
                filterable: false,
                width: gridWidth * 0.07
            },
            {
                field: "validationGroup",
                title: "Group",
				filterable: false,
                width: gridWidth * 0.07
            }
        ],
        dataBound: function(e) {
            $(".overlay").css("display", "none");
        }
    });
    $('#applyButtonId').attr('disabled', 'disabled');
}

function exportToExcel() {
if($("#effective-endDate-exportDef").hasClass("dropDown-redBorder")==false){
    $(".overlay").css("display", "block");
    $.ajax({
        url: 'exporttoexcelForDataQuality.htm',
        type: 'POST',
        dataType: 'json',
        data: {
            gridData: JSON.stringify(createFilterJson("export"))
        },
        success: function(response) {
            if (response.success == true) {
                $('#hiddenDownloader').attr('src', 'exporttoexcelhiddenForDataQuality.htm');
                vs_model_hide('ExportDefPopup');
                $('.exportDef').empty()
                $('.exportDef').multiselect('rebuild');
            } else {
                $("#export-def-error-msg").empty();
                $("#export-def-error-msg").append("<strong>Warning! </strong>Data not Available For Selected Values.");
                $("#export-def-error-msg-parent").show();
            }
            $(".overlay").css("display", "none");
        }
    }).done(function() {});
}
}

function importFromExcel() {
    $("#help-div-parent").hide();
    if ($("#file").val()) {
        $("#uploadForm").submit();
    } else {
        $('#help-div').empty();
        $('#help-div').append("Please select a file to upload");
        $("#help-div-parent").show();
    }
}

function createFilterJson(ind) {
    var filterJsonObj = {};
    var gridFilter = {};
    var mainFilter = {};
    if (ind == "export") {
        var exportDefDetails = createExportJsonArray();
        mainFilter["validationEntities"] = exportDefDetails["return-exportDef-dropdown"];
        mainFilter["validationType"] = exportDefDetails["type-exportDef-dropdown"];
        mainFilter["validationRuleType"] = exportDefDetails["rule-type-exportDef-dropdown"];
        mainFilter["validationStatus"] = exportDefDetails["status-exportDef-dropdown"];
        mainFilter["validationGroup"] = exportDefDetails["group-exportDef-dropdown"];
        mainFilter["effectiveDate"] = $('#effective-startDate-exportDef').val() == "" ? moment(new Date()).format(momentDateFormat) : $('#effective-startDate-exportDef').val();
        mainFilter["effectiveEndDate"] = $('#effective-endDate-exportDef').val();
    } else {
        mainFilter["validationEntities"] = $('#return-select-id option:selected').text() == "All" ? "" : $('#return-select-id option:selected').text();
        mainFilter["validationType"] = $('#type-select-id option:selected').text() == "All" ? "" : $('#type-select-id option:selected').text();
        mainFilter["validationRuleType"] = $('#rule-type-select-id option:selected').text() == "All" ? "" : $('#rule-type-select-id option:selected').text();
        mainFilter["validationStatus"] = $('#status-select-id option:selected').text() == "All" ? "" : $('#status-select-id option:selected').text();
        mainFilter["validationGroup"] = $('#group-select-id option:selected').text() == "All" ? "" : $('#group-select-id option:selected').text();
        mainFilter["effectiveDate"] = $('#effective-date-id').val() == "" ? moment(new Date()).format(momentDateFormat) : $('#effective-date-id').val();
    }

    filterJsonObj["mainFilter"] = mainFilter;

    gridFilter["pageNo"] = "1";
    gridFilter["pageSize"] = "10";
    gridFilter["validationCode"] = "";
    gridFilter["validationName"] = "";
    gridFilter["validationSection"] = "";
    gridFilter["sortOn"] = "validationEntities~~asc";
    filterJsonObj["gridDetails"] = gridFilter;
    if (ind == "export") {
        filterJsonObj["exportInd"] = "Y";
    } else {
        filterJsonObj["exportInd"] = "N";
    }
    return filterJsonObj;
}

function addElementToExpressionBox(e) {
    alert('hi');
}

function saveReturnValidation() {
    $.ajax({
        url: 'savereturnvalidation.htm',
        type: 'POST',
        dataType: 'json',
        data: {
            returnValidation: JSON.stringify(validationJsonCreation())
        },
        success: function(response) {}
    }).done(function() {});
}

function getMainFilterData(ind) {
    var effectiveDate = "";
    if (ind == "export") {
        effectiveDate = $('#effective-startDate-exportDef').val();
    } else {
        effectiveDate = $('#effective-date-id').val();
    }
    $.ajax({
        url: 'getlistformainfilterForEntities.htm',
        type: 'POST',
        dataType: 'json',
        data: {
            effectiveDate: effectiveDate
        },
        success: function(response) {
            if (ind == "export") {
                appendOptions(response.response.validationEntity, "return-exportDef-dropdown", 'export');
                appendOptions(response.response.validationType, "type-exportDef-dropdown", 'export');
                appendOptions(response.response.validationRuleType, "rule-type-exportDef-dropdown", 'export');
                appendOptions(response.response.validationStatus, "status-exportDef-dropdown", 'export');
                appendOptions(response.response.validationGroup, "group-exportDef-dropdown", 'export');
                $('.exportDef').multiselect({
                    includeSelectAllOption: true,
                    selectAllValue: 'select-all-value',
                    maxHeight: 450,
                    maxWidth: 100,
                    buttonText: function(options) {
                        if (options.length == 0) {
                            return 'Select your option';
                        } else if (options.length == 1) {
                            return $(options[0]).text();
                        } else {
                            var selected = 0;
                            options.each(function() {
                                selected += 1;
                            });
                            return selected + ' selected';
                        }
                    },
                    buttonWidth: '250px',
                    templates: {
                        button: '<button type="button" class="multiselect dropdown-toggle" data-toggle="dropdown"><span class="multiselect-selected-text"></span> <b class="caret exportDef-returnValidation-Caret"></b></button>'
                    },
					enableCaseInsensitiveFiltering: true
                });
                $('.exportDef').multiselect('rebuild');
                vs_model_show('ExportDefPopup');
            } else {
                appendOptions(response.response.validationEntity, "return-select-id");
                appendOptions(response.response.validationType, "type-select-id");
                appendOptions(response.response.validationRuleType, "rule-type-select-id");
                appendOptions(response.response.validationStatus, "status-select-id");
                appendOptions(response.response.validationGroup, "group-select-id");
            }
        }
    }).done(function() {});
}

function redirectToValidationCreationPage() {
	$(".overlay").css("display", "block");
	$("#validationDescriptionTextArea").removeClass('textarea-height');
	$('#validation-msg').hide();
    $.ajax({
        url: 'loadreturnvalidationcreatepageEnt.htm',
        type: 'POST',
        dataType: 'json',
        data: {
        },
        success: function(response) {
        	isEdit = response.isedit;
        	defaultArrayVal = response.newResponse;
        	var group = defaultArrayVal.length==0?[]:defaultArrayVal[0].validationGroup;
            var optionalDropDownValues = defaultArrayVal.length==0?[]:defaultArrayVal[0].optionalDropDownValues;
            var statusArr = defaultArrayVal.length==0?[]:defaultArrayVal[0].validationStatus;
            var validationRuleType = defaultArrayVal.length==0?[]:defaultArrayVal[0].validationRuleType;
            appendOptionsCreateAndEdit(group, "groupReturnId");
            appendOptionsCreateAndEdit(optionalDropDownValues, "everyOccurenceDropDown");
            appendOptionsCreateAndEdit(statusArr, "statusId");
            appendOptionsCreateAndEdit(validationRuleType, "ruleTypeId");
			
            creatingNewValidation();
			$('#groupReturnId').prop('selectedIndex',0);
            $('#validationListContainer').css("display", "none");
        	$('#validation-create-edit-con').css("display", "block");
            $(".overlay").css("display", "none");
        }
    }).done(function() {});
	
    //window.location = 'loadreturnvalidationcreatepage.htm';
}

function appendOptions(data, id, ind) {
    $('#' + id).empty();
    if (ind != "export")
        $('#' + id).append("<option>" + "All" + "</option>");
    if (data != undefined) {
        var dataLen = data.length;
        for (var i = 0; i < dataLen; i++) {
            $('#' + id).append("<option>" + data[i].trim() + "</option>");
        }
    }
	if(id=="return-select-id"){
		$('#' + id).multiselect({
			enableCaseInsensitiveFiltering: true
		});
		$('.multiselect-container').css('width','auto');
		$('.multiselect-container .radio input[type="radio"]').css('display','none');
	}
}

function exportDefinitionPopup() {
    $('#effective-startDate-exportDef').datepicker('setDate', 'now');
    getMainFilterData("export");
    $('#effective-endDate-exportDef').val("");
    restrictExportEndDate();
    $("#export-def-error-msg-parent").hide();
	$("#effective-endDate-exportDef").next('p').remove();
	$("#effective-endDate-exportDef").removeClass("dropDown-redBorder");
}

function createExportJsonArray() {
    var exportDefDetails = {};
    $(".exportDef").each(function() {
        var idOfExportDef = $(this).attr("id");
        exportDefDetails[idOfExportDef] = [];
        $("#" + idOfExportDef).next("div").children("ul").find("input[type=checkbox]").each(
            function() {
                if ($(this).is(":checked")) {
                    if ($(this).val() == "select-all-value") {
                        exportDefDetails[idOfExportDef] = [];
                        return false;
                    } else {
                        exportDefDetails[idOfExportDef].push($(this).val());
                    }
                }
            });
    });
    return exportDefDetails;
}

function exportTemplate() {
    $(".overlay").css("display", "block");
    $('#hiddenDownloader').attr('src', 'downloadtemplatefordataqualityrule.htm');
    $(".overlay").css("display", "none");
}

function showImportModal() {
    $("#uploadForm input[type=file]").val("");
    $("#help-div-parent").hide();
    vs_model_show('importModal')
}

function restrictExportEndDate() {
    var fromDateVal = $("#effective-startDate-exportDef").val();
	$("#effective-endDate-exportDef").datepicker("setStartDate",fromDateVal);
    var displayDateFormat = displayDateFormat;
}

function endDateLessThanStratDate(momentA,momentB,endDateId){
	endDateId="#"+endDateId;
	$(endDateId).next('p').remove();
	effectiveDateObject[endDateId]="";
	//$(endDateId).datepicker('setDate','');
	$(endDateId).addClass("dropDown-redBorder");
	$(endDateId).after("<p class='vs-alert-text'>Effective end Date can not be less than effective start date.</p>");
}
