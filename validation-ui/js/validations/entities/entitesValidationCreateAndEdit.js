var expressionObj = {};
var sliderLimit;
var isChangeFlag = "N";
var lastClicked;
var selectedDateForEdit;
var returnArray = [];
var dEntity = {};
var ddExpressionObj={};
var effDateFormat = "YYYY-MM-DD";

$(document).ready(function() {
    //var displayDateFormat = 'dd-mm-yyyy';
    $("#basicInformationPanel").css({
        "max-height": "none",
    });
    $("#basicInformation").addClass("vs-active");
    $("#date-picker-id").datepicker({
        format: displayDateFormat,
        autoclose: true,
        forceParse:false,
    }).on('changeDate', function(e) {
    	if($("#date-picker-id").val()!=""){
    		$(this).removeClass('dropDown-redBorder');
    		$(this).siblings('.vs-alert-text').remove();
    		var isExist = false;
            returnArray = [];
            var selectedReturn = $("#returnNameId").val();
            if (isEdit == false) {
                $('#returnNameId').empty();
                $('#returnNameId').append("<option selected disabled>Select Option</option>");
                $('#returnNameSection').empty();
                $('#returnNameSection').append("<option selected>Select Option</option>");
            }
            createReturnFragment(selectedReturn);
            if (isEdit == true) {
                for (var i = 0; i < returnArray.length; i++) {
                    if (returnArray[i] == selectedReturn) {
                        isExist = true;
                    }
                }
                if (isExist == true) {
                	$("#returnNameId").multiselect('disable');
                    $('#returnNameId').append("<option>" + selectedReturn + "</option>");
                    $("#returnNameId").val(selectedReturn);
                    $("#returnNameId").siblings('.btn-group').children("button").children('.multiselect-selected-text').text(selectedReturn);
                    $("#returnNameId").siblings('.btn-group').children("button").attr('title',selectedReturn);
                } else {
                    if (($('#editValidationbtn').css('display') == 'block' && isEdit == "true") || ($('#editValidationbtn').css('display') == "none" && isEdit == "false")) {
                        $("#onChangeOfEffectiveDate").css("display", "flex");
                        $('#date-picker-id').datepicker('setDate', selectedDateForEdit);
                        $('#returnNameId').append("<option>" + selectedReturn + "</option>");
                        $("#returnNameId").val(selectedReturn);
                    }
                }
            } else {
                if (returnArray.length == 0) {
                    $("#onChangeOfEffectiveDate").css("display", "flex");
                }
            }
            if ($('#editValidationbtn').attr('disabled') == 'disabled' && previlegeValue.toUpperCase()!="VIEW") {
                var momentA = $('#date-picker-id').val();
                var momentB = moment(editValidationArray[0].data[editValidationArray[0].data.length - 1].effectiveDate).format(momentDateFormat);
                if (moment(momentA,momentDateFormat).isBefore(moment(momentB,momentDateFormat))) {
                    $('#onClickingEffectiveDateGreaterAlert').css('display', 'flex');
                }
            }
            clearInpBoxAndReloadData("returnData","","");
    	}
    }).on('hide', function(e) {
		if(moment($(this).val(), momentDateFormat, true).isValid()==false){
			$("#"+$(this).attr("id")).datepicker('setDate', selectedDateForEdit);
		}
		else{
			selectedDateForEdit=$(this).val();
		}
	});
    //('#date-picker-id').datepicker('setDate', 'now');
    $("#rightScroll").click(function() {
    	var view = $(".cotainerProgressBar");
		var move = "100px";
        var currentPosition = parseInt(view.css("left"));
        if (currentPosition >= sliderLimit) view.stop(false, true).animate({
            left: "-=" + move
        }, {
            duration: 400
        })
    });
    $("#leftScroll").click(function() {
    	var view = $(".cotainerProgressBar");
		var move = "100px";
        var currentPosition = parseInt(view.css("left"));
        if (currentPosition < 0) view.stop(false, true).animate({
            left: "+=" + move
        }, {
            duration: 400
        })
    });
    $("#returnNameId").change(function() {
        var changedVal = this.value;
        $('#returnNameSection').empty();
        $('#returnNameSection').append("<option selected>Select Option</option>");
        createReturnFragmentChange(changedVal);
    });
	$("#thresholdUnitId").change(function() {
        var changedVal = this.value;
        if(changedVal.toLowerCase()==="percentage"){
			$("#aThreshhold").attr("min","1");
			$("#aThreshhold").attr("max","100");
			
			$("#qThreshhold").attr("min","1");
			$("#qThreshhold").attr("max","100");
		}else if(changedVal.toLowerCase()=="absolute"){
			
			$("#aThreshhold").removeAttr("max");
			                            
			$("#qThreshhold").removeAttr("max");
		}else{
			$("#measuredOnId").val("");
			$("#aThreshhold").val("");
			$("#qThreshhold").val("");
		}
        
    });
	
    $('#editValidationbtn').click(function() {
        $('#editValidationbtn').attr('disabled', true);
        $('.editGroupBtns').css('display', 'block');
        $('.nonEditGroupBtns').css('display', 'none');
        contentEditableMethod();
    });
    $("#validationNameTextId").keyup(function(event) {
        var headingTextValue = $(this).val();
        $("#validationNameHeading").text($("#validationCodeTextId").val() + " : " + headingTextValue);
        if ($('#validationNameTextId').val() == "" && $('#validationCodeTextId').val() == "") {
            $("#validationNameHeading").text("New Validation");
        }
    });
    $("#validationCodeTextId").keyup(function(event) {
        var headingTextValue = $(this).val();
        $("#validationNameHeading").text(headingTextValue + " : " + $("#validationNameTextId").val());
        if ($('#validationNameTextId').val() == "" && $('#validationCodeTextId').val() == "") {
            $("#validationNameHeading").text("New Validation");
        }
    });
    createFunctionAndOperatorList();
    if (isEdit != true) {
        $('.editGroupBtns').css('display', 'block');
        $('.nonEditGroupBtns').css('display', 'none');
    }
    $("#validationNameTextId, #validationDescriptionTextId, #validationDescriptionTextId, #returnNameSection, #groupReturnId, #everyOccurenceDropDown, #statusId, #validationDescriptionTextArea, input[type=radio][name=categoryBtn],input[type=radio][name=typeBtn]").change(function() {
        if(isEdit==true){
        	isChangeFlag = "Y";
            var momentA = $('#date-picker-id').val();
            var momentB = moment(editValidationArray[0].data[editValidationArray[0].data.length - 1].effectiveDate).format(momentDateFormat);
            if (moment(momentA,momentDateFormat).isBefore(moment(momentB,momentDateFormat))) {
                $('#onClickingEffectiveDateGreaterAlert').css('display', 'flex');
            }		
        }
    });
    $('#cancelTheProcess').on('click', function() {
        $('#onClickingEffectiveDateAlert').css('display', 'none');
    });
    if (previlegeValue.toUpperCase() == "EDIT") {
        isView = "N";
    } else {
        isView = "Y";
    }
    $('#everyOccurenceDropDown').on('change', function() {
        if ($(this).val() == "Per Validation") {
            $('#lableToEnableComment1').css('display', 'none');
            $('#lableToEnableComment2').css('display', 'block');
        } else {
            $('#lableToEnableComment2').css('display', 'none');
            $('#lableToEnableComment1').css('display', 'block');
        }
    });
    $(".alertInlineValidation").focusout(function() {
        if ($(this).val().trim() != "" && $(this).val() != null && $(this).val() != undefined) {
            $(this).next("p").remove();
            $(this).removeClass("dropDown-redBorder");
        }
    });
    $(".alertInlineValidationDropDown").on('change', function() {
        if ($(this).val() != "" && $(this).val() != null && $(this).val() != undefined) {
        	//$(this).parent().next('p').remove();
        	$(this).parent().children('.vs-alert-text').remove();
            $(this).parent().siblings(".vs-alert-text").remove();
            $(this).removeClass("dropDown-redBorder");
        }
    });
    $('#return-Search').bind("cut copy paste", function(e) {
        e.preventDefault();
    });
    $("#button2, #button3").click(function(){
    	var elemId = $(this).attr('onclick').split("('")[1].split(",")[0].replace(/'/g, "");
    	var expressionFormat = $("#"+elemId+" .highLightClass").attr('expressionformat');
    	if(expressionFormat!=undefined){
    		var expressionformatstr = $("#"+elemId+" .highLightClass").attr('format-str');
    		$("#"+elemId+" .expression-format .example-div").text(expressionFormat).attr("format-str", expressionformatstr);
    	}else{
    		$("#"+elemId+" .expression-format .example-div").text("").attr("format-str", "");
    	}
    	if($('#'+elemId+' .search-class').val().trim()==""){
    		$("#"+elemId+' .clearInputClass').hide();
    		$("#"+elemId+' .search-class').removeClass("borderClass");
    	}else{
    		$("#"+elemId+' .clearInputClass').show();
    		$("#"+elemId+' .search-class').addClass("borderClass");
    	}
    	
    });
    //$("#date-picker-id").datepicker('setDate', 'now');
});

function createListOfElements(name, sectionType, entityName, entityVersion, ind, returnTye, searchInd) {
    ind = ind == undefined ? "" : ind;
    $('.parent-div').addClass('parent-div-hide');
    $('#expression-data .main-parent').remove();
    var effectiveDate = $("#date-picker-id").data('datepicker').getFormattedDate(displayDateFormat);
    if (ind.toLowerCase() == "level-1") {
        getReturnDetails(effectiveDate);
    } else if (ind.toLowerCase() == "level-2") {
        expressionObj = {};
        expressionObj["returnName"] = name;
        getSectionDetails(effectiveDate, expressionObj.returnName);
    } else if (ind.toLowerCase() == "level-3") {
        expressionObj["sectionName"] = name;
        var resultObj = jsonPath(sections, "$.[?(@.section=='" + name + "')]")[0];
        returnTye=resultObj.sectionType;
        if(returnTye.toUpperCase()=="LIST"){
        	 entityName = resultObj.entityName;
        	 entityVersion = resultObj.entityVersion;
        }
        getLineItemDetails(effectiveDate, expressionObj.returnName, expressionObj.sectionName, returnTye, entityName, entityVersion);
    }
}

function createListOfElementsForReftbl(e, ind) {
    ind = ind == undefined ? "" : ind;
    $('.parent-div').addClass('parent-div-hide');
    $('#expression-data .main-parent').remove();
    if (ind.toLowerCase() == "level-1") {
        getEntityViews();
    } else if (ind.toLowerCase() == "level-2") {
        if (e != undefined) {
            expressionObj = {};
            expressionObj["element"] = $(e).text();
            expressionObj["entityUuid"] = e.getAttribute("entityUuid");
            expressionObj["entityViewUuid"] = e.getAttribute("entityViewUuid");
            expressionObj["entityViewVersion"] = e.getAttribute("entityViewVersion");
        }
        getEntityColumnName(expressionObj.element, expressionObj.entityUuid, expressionObj.entityViewUuid, expressionObj.entityViewVersion);
    }
}

function getEntityColumnName(entityViews, entityUuid, entityViewUuid, entityViewVersion) {
    $.ajax({
        url: 'getentitycolumdetails.htm',
        type: 'POST',
        dataType: 'json',
        data: {
            entityViews: entityViews,
            entityUuid: entityUuid,
            entityViewUuid: entityViewUuid,
            entityViewVersion: entityViewVersion
        },
        success: function(response) {
            $('#expression-data .main-parent').remove();
            var columnName = response.success;
            var fragment = document.createDocumentFragment();
            columnName.forEach(function(val) {
                var parentElement = document.createElement('div');
                parentElement.setAttribute('class', 'main-parent');
                var mainElement = document.createElement('div');
                mainElement.setAttribute('class', 'child-div contentDiv');
                mainElement.setAttribute('ondblclick', 'addValueIntoInputBoxOfReftbl(this)');
                mainElement.setAttribute('level', 'level-3');
                mainElement.textContent = val.entityName;
                mainElement.value = val.entityName;
                var childElement = document.createElement('div');
                childElement.setAttribute('class', 'vs-tooltip icon-info');
                var iElement = document.createElement('i');
                iElement.setAttribute('class', 'icon-small icon-info-solid');
                var spanElement = document.createElement('span');
                spanElement.setAttribute('class', 'vs-tooltiptext-left');
                spanElement.setAttribute('style', 'width:max-content');
                var smallElement = document.createElement('small');
                smallElement.setAttribute('class', 'vs-baseline-regular-black');
                smallElement.textContent = "Column Name : ";
                var innerSpanElement = document.createElement('span');
                innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
                innerSpanElement.textContent = val.entityName;
                var br = document.createElement('br');
                smallElement.appendChild(innerSpanElement);
                spanElement.appendChild(smallElement).appendChild(br);

                var smallElement1 = document.createElement('small');
                smallElement1.setAttribute('class', 'vs-baseline-regular-black');
                smallElement1.textContent = "Data Type : ";
                var innerSpanElement1 = document.createElement('span');
                innerSpanElement1.setAttribute('class', 'vs-baseline-medium-primary');
                innerSpanElement1.textContent = val.dataType;
                var br = document.createElement('br');
                smallElement1.appendChild(innerSpanElement1);
                spanElement.appendChild(smallElement1).appendChild(br);

                childElement.appendChild(iElement);
                childElement.appendChild(spanElement);
                parentElement.appendChild(mainElement);
                parentElement.appendChild(childElement);
                fragment.appendChild(parentElement);
            });
            $("#expression-data").append(fragment);
        }
    }).done(function() {
    	$('#return-Search').keyup();
    });
}

function getEntityViews() {
    $.ajax({
        url: 'getentityviews.htm',
        type: 'POST',
        dataType: 'json',
        data: {},
        success: function(response) {
            $('#expression-data .main-parent').remove();
            var returns = response.success;
            var fragment = document.createDocumentFragment();
            returns.forEach(function(val) {
                var parentElement = document.createElement('div');
                parentElement.setAttribute('class', 'main-parent');
                var mainElement = document.createElement('div');
                mainElement.setAttribute('class', 'child-div contentDiv');
                mainElement.setAttribute('ondblclick', 'addValueIntoInputBoxOfReftbl(this,"level-2","grid")');
                mainElement.setAttribute('level', 'level-2');
                mainElement.setAttribute('entityUuid', val.entityUuid);
                mainElement.setAttribute('entityViewUuid', val.entityViewUuid);
                mainElement.setAttribute('entityViewVersion', val.entityViewVersion);
                mainElement.textContent = val.entityViewName;
                mainElement.value = val.entityViewName;
                var childElement = document.createElement('div');
                childElement.setAttribute('class', 'vs-tooltip icon-info');
                var iElement = document.createElement('i');
                iElement.setAttribute('class', 'icon-small icon-info-solid');
                var spanElement = document.createElement('span');
                spanElement.setAttribute('class', 'vs-tooltiptext-left');
                spanElement.setAttribute('style', 'width:max-content');
                var smallElement = document.createElement('small');
                smallElement.setAttribute('class', 'vs-baseline-regular-black');
                smallElement.textContent = "Entity  : ";
                var innerSpanElement = document.createElement('span');
                innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
                innerSpanElement.textContent = val.entityViewName;
                smallElement.appendChild(innerSpanElement)
                spanElement.appendChild(smallElement);
                childElement.appendChild(iElement);
                childElement.appendChild(spanElement);
                parentElement.appendChild(mainElement);
                parentElement.appendChild(childElement);
                fragment.appendChild(parentElement);
            });
            $("#expression-data").append(fragment);
        }
    }).done(function() {
    	$('#return-Search').keyup();
    });
}

function getReturnDetails(effectiveDate) {
    $.ajax({
        url: 'getreturns.htm',
        type: 'POST',
        dataType: 'json',
        async:false,
        data: {
            effectiveDate: effectiveDate,
            isExpr: 'Y'
        },
        success: function(response) {
            $('#expression-data .main-parent').remove();
            var returns = response.response;
            var fragment = document.createDocumentFragment();
            returns.forEach(function(val) {
                var parentElement = document.createElement('div');
                parentElement.setAttribute('class', 'main-parent');
                var mainElement = document.createElement('div');
                mainElement.setAttribute('class', 'child-div contentDiv');
                mainElement.setAttribute('ondblclick', 'addValueIntoInputBox(this,"level-2","grid")');
                mainElement.setAttribute('level', 'level-2');
                mainElement.setAttribute('sectionType', val.returnType);
                mainElement.textContent = val.returns;
                mainElement.value = val.returns;
                var childElement = document.createElement('div');
                childElement.setAttribute('class', 'vs-tooltip icon-info');
                var iElement = document.createElement('i');
                iElement.setAttribute('class', 'icon-small icon-info-solid');
                var spanElement = document.createElement('span');
                spanElement.setAttribute('class', 'vs-tooltiptext-left');
                spanElement.setAttribute('style', 'width:max-content');
                var smallElement = document.createElement('small');
                smallElement.setAttribute('class', 'vs-baseline-regular-black');
                smallElement.textContent = "Return Name : ";
                var innerSpanElement = document.createElement('span');
                innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
                innerSpanElement.textContent = val.returnName;
                smallElement.appendChild(innerSpanElement)
                spanElement.appendChild(smallElement);
                childElement.appendChild(iElement);
                childElement.appendChild(spanElement);
                parentElement.appendChild(mainElement);
                parentElement.appendChild(childElement);
                fragment.appendChild(parentElement);
            });
            $("#expression-data").append(fragment);
        }
    }).done(function() {
    	$('#return-Search').keyup();
    });
}

function getSectionDetails(effectiveDate, returnName) {
    $.ajax({
        url: 'getsections.htm',
        type: 'POST',
        dataType: 'json',
        async:false,
        data: {
            effectiveDate: effectiveDate,
            returns: returnName,
            isExpr: 'Y'
        },
        success: function(response) {
            $('#expression-data .main-parent').remove();
            sections = response.response;
            var fragment = document.createDocumentFragment();
            sections.forEach(function(val) {
                var parentElement = document.createElement('div');
                parentElement.setAttribute('class', 'main-parent');
                var mainElement = document.createElement('div');
                mainElement.setAttribute('class', 'child-div contentDiv');
                mainElement.setAttribute('ondblclick', 'addValueIntoInputBox(this,"level-3","grid")');
                mainElement.setAttribute('level', 'level-3');
                mainElement.setAttribute('sectionType', val.sectionType);
                mainElement.setAttribute('entityName', val.entityName);
                mainElement.setAttribute('entityVersion', val.entityVersion);
                mainElement.textContent = val.section;
                mainElement.value = val.section;
                var childElement = document.createElement('div');
                childElement.setAttribute('class', 'vs-tooltip icon-info');
                var iElement = document.createElement('i');
                iElement.setAttribute('class', 'icon-small icon-info-solid');
                var spanElement = document.createElement('span');
                spanElement.setAttribute('class', 'vs-tooltiptext-left');
                spanElement.setAttribute('style', 'width:max-content;overflow: hidden;max-width: 500px;text-overflow: ellipsis;max-height: 200px;');
                var smallElement = document.createElement('small');
                smallElement.setAttribute('class', 'vs-baseline-regular-black');
                smallElement.textContent = "Section Name : ";
                var innerSpanElement = document.createElement('span');
                innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
                innerSpanElement.textContent = val.sectionName;
                var br = document.createElement('br');
                smallElement.appendChild(innerSpanElement);
                spanElement.appendChild(smallElement).appendChild(br);

                var smallElement1 = document.createElement('small');
                smallElement1.setAttribute('class', 'vs-baseline-regular-black');
                smallElement1.textContent = "Form Name : ";
                var innerSpanElement1 = document.createElement('span');
                innerSpanElement1.setAttribute('class', 'vs-baseline-medium-primary');
                innerSpanElement1.textContent = val.formName;
                var br = document.createElement('br');
                smallElement1.appendChild(innerSpanElement1);
                spanElement.appendChild(smallElement1).appendChild(br);

                childElement.appendChild(iElement);
                childElement.appendChild(spanElement);
                parentElement.appendChild(mainElement);
                parentElement.appendChild(childElement);
                fragment.appendChild(parentElement);
            });
            $("#expression-data").append(fragment);
        }
    }).done(function() {
    	$('#return-Search').keyup();
    });
}

function getLineItemDetails(effectiveDate, returnName, section, sectionType, entityName, entityVersion) {
    $.ajax({
        url: 'getlineitems.htm',
        type: 'POST',
        dataType: 'json',
        async:false,
        data: {
            effectiveDate: effectiveDate,
            returns: returnName,
            sections: section,
            sectionType: sectionType,
            entityName: entityName,
            entityVersion: entityVersion
        },
        success: function(response) {
            $('#expression-data .main-parent').remove();
            var lineItems = response.response;
            var fragment = document.createDocumentFragment();
            lineItems.forEach(function(val) {
            	if(sectionType.toUpperCase()=="GRID"){
            		 var parentElement = document.createElement('div');
                     parentElement.setAttribute('class', 'main-parent');
                     var mainElement = document.createElement('div');
                     mainElement.setAttribute('class', 'contentDiv');
                     mainElement.setAttribute('ondblclick', 'addValueIntoInputBox(this)');
                     mainElement.setAttribute('level', 'level-4');
                     mainElement.setAttribute('sectionType', val.sectionType);
                     mainElement.setAttribute('entityName', val.entityName);
                     mainElement.setAttribute('entityVersion', val.entityVersion);
                     mainElement.textContent = val.name;
                     mainElement.value = val.name;
                     var childElement = document.createElement('div');
                     childElement.setAttribute('class', 'vs-tooltip icon-info');
                     var iElement = document.createElement('i');
                     iElement.setAttribute('class', 'icon-small icon-info-solid');
                     var spanElement = document.createElement('span');
                     spanElement.setAttribute('class', 'vs-tooltiptext-left');
                     spanElement.setAttribute('style', 'width:max-content');
                     var smallElement = document.createElement('small');
                     smallElement.setAttribute('class', 'vs-baseline-regular-black');
                     smallElement.textContent = "Column Name: ";
                     var innerSpanElement = document.createElement('span');
                     innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
                     innerSpanElement.textContent = val.columnName;
                     var br = document.createElement('br');
                     smallElement.appendChild(innerSpanElement);
                     spanElement.appendChild(smallElement).appendChild(br);

                     var smallElement1 = document.createElement('small');
                     smallElement1.setAttribute('class', 'vs-baseline-regular-black');
                     smallElement1.textContent = "Data Type : ";
                     var innerSpanElement1 = document.createElement('span');
                     innerSpanElement1.setAttribute('class', 'vs-baseline-medium-primary');
                     innerSpanElement1.textContent = val.DataType;
                     var br = document.createElement('br');
                     smallElement1.appendChild(innerSpanElement1);
                     spanElement.appendChild(smallElement1).appendChild(br);

                     var smallElement1 = document.createElement('small');
                     smallElement1.setAttribute('class', 'vs-baseline-regular-black');
                     smallElement1.textContent = "Attribute Type : ";
                     var innerSpanElement1 = document.createElement('span');
                     innerSpanElement1.setAttribute('class', 'vs-baseline-medium-primary');
                     innerSpanElement1.textContent = val.AttributeType;
                     var br = document.createElement('br');
                     smallElement1.appendChild(innerSpanElement1);
                     spanElement.appendChild(smallElement1).appendChild(br);
                     childElement.appendChild(iElement);
                     childElement.appendChild(spanElement);

                     var divElement = document.createElement('div');
                     var spanElement = document.createElement('span');
                     spanElement.setAttribute('class', 'icon-info-solid');
                     divElement.textContent = val;
                     divElement.setAttribute('ondblclick', 'addValueIntoInputBox(this)');
                     divElement.setAttribute('class', 'contentDiv');
                     divElement.appendChild(spanElement);
                     fragment.appendChild(mainElement);
                     parentElement.appendChild(mainElement);
                     parentElement.appendChild(childElement);
                     fragment.appendChild(parentElement);
            	}else{
            		 var parentElement = document.createElement('div');
                     parentElement.setAttribute('class', 'main-parent');
                     var mainElement = document.createElement('div');
                     mainElement.setAttribute('class', 'contentDiv');
                     mainElement.setAttribute('ondblclick', 'addValueIntoInputBox(this)');
                     mainElement.setAttribute('level', 'level-4');
                     mainElement.setAttribute('sectionType', sectionType);
                     mainElement.setAttribute('entityName', entityName);
                     mainElement.setAttribute('entityVersion', entityVersion);
                     mainElement.textContent = val.columnName;
                     mainElement.value = val.columnName;
                     var childElement = document.createElement('div');
                     childElement.setAttribute('class', 'vs-tooltip icon-info');
                     var iElement = document.createElement('i');
                     iElement.setAttribute('class', 'icon-small icon-info-solid');
                     var spanElement = document.createElement('span');
                     spanElement.setAttribute('class', 'vs-tooltiptext-left');
                     spanElement.setAttribute('style', 'width:max-content');
                     var smallElement = document.createElement('small');
                     smallElement.setAttribute('class', 'vs-baseline-regular-black');
                     smallElement.textContent = "Column Name: ";
                     var innerSpanElement = document.createElement('span');
                     innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
                     innerSpanElement.textContent = val.columnName;
                     var br = document.createElement('br');
                     smallElement.appendChild(innerSpanElement);
                     spanElement.appendChild(smallElement).appendChild(br);

                     var smallElement1 = document.createElement('small');
                     smallElement1.setAttribute('class', 'vs-baseline-regular-black');
                     smallElement1.textContent = "Data Type : ";
                     var innerSpanElement1 = document.createElement('span');
                     innerSpanElement1.setAttribute('class', 'vs-baseline-medium-primary');
                     innerSpanElement1.textContent = val.columnType;
                     var br = document.createElement('br');
                     smallElement1.appendChild(innerSpanElement1);
                     spanElement.appendChild(smallElement1).appendChild(br);

                     var smallElement1 = document.createElement('small');
                     smallElement1.setAttribute('class', 'vs-baseline-regular-black');
                     smallElement1.textContent = "Attribute Type : ";
                     var innerSpanElement1 = document.createElement('span');
                     innerSpanElement1.setAttribute('class', 'vs-baseline-medium-primary');
                     innerSpanElement1.textContent = val.AttributeType;
                     var br = document.createElement('br');
                     smallElement1.appendChild(innerSpanElement1);
                     spanElement.appendChild(smallElement1).appendChild(br);
                     childElement.appendChild(iElement);
                     childElement.appendChild(spanElement);

                     var divElement = document.createElement('div');
                     var spanElement = document.createElement('span');
                     spanElement.setAttribute('class', 'icon-info-solid');
                     divElement.textContent = val;
                     divElement.setAttribute('ondblclick', 'addValueIntoInputBox(this)');
                     divElement.setAttribute('class', 'contentDiv');
                     divElement.appendChild(spanElement);
                     fragment.appendChild(mainElement);
                     parentElement.appendChild(mainElement);
                     parentElement.appendChild(childElement);
                     fragment.appendChild(parentElement);
            	}
               
            });
            $("#expression-data").append(fragment);
        }
    }).done(function() {
    	$('#return-Search').keyup();
    });
}

function populateReturnValidationData(effectiveDateOnBar) {
	$('#date-picker-id').val(effectiveDateOnBar);
	$('#date-picker-id').datepicker('setDate', effectiveDateOnBar);
    var editValidationArrayLen = editValidationArray[0].data.length;
    for (var i = 0; i < editValidationArrayLen; i++) {
    	if (moment(editValidationArray[0].data[i]["effectiveDate"]).format(momentDateFormat) == effectiveDateOnBar) {
            var validationArray = editValidationArray[0].data[i];
            $('#validationCodeTextId').val(validationArray["validationCode"]);
            $('#validationNameTextId').val(validationArray["validationName"]);
            $('#validationNameHeading').text($('#validationCodeTextId').val() + " : " + $('#validationNameTextId').val());
            $('#validationDescriptionTextId').val(validationArray["validationDescription"]);
            $('#returnNameId').val(validationArray["validationEntities"].trim());
			$('#returnNameId').multiselect('refresh');
            $('#groupReturnId').val(validationArray["validationGroup"]);
            $('#statusId').val(validationArray["validationStatus"]);
            createReturnFragmentChange($('#returnNameId').val());
            $('#returnNameSection').val(validationArray["validationColumn"]);
			$('#returnNameSection').multiselect('refresh');
			
			//for thresholdUnit
			$('#aThreshhold').val(validationArray["acceptableThreshold"]);
			$('#qThreshhold').val(validationArray["questionableThreshold"]);
			
			if(validationArray["measuredOn"]!=null && validationArray["measuredOn"]!=""){
				var mValue = (validationArray["measuredOn"]).toUpperCase()=="PASSED"?"Valid Occurrences":"Invalid Occurrences";
				
				$('#measuredOnId').val(mValue);
				//$('#measuredOnId').multiselect('refresh');
			}else{
				$('#measuredOnId').val("");
				//$('#measuredOnId').multiselect('refresh');
			}
			
			if(validationArray["thresholdUnit"]!=null && validationArray["thresholdUnit"]!=""){
				var tValue = capitalizeFirstLetter(validationArray["thresholdUnit"]);
				$('#thresholdUnitId').val(tValue);
				var changedVal = tValue;
					if(changedVal.toLowerCase()==="percentage"){
						$("#aThreshhold").attr("min","1");
						$("#aThreshhold").attr("max","100");
						
						$("#qThreshhold").attr("min","1");
						$("#qThreshhold").attr("max","100");
					}else{
						
						$("#aThreshhold").removeAttr("max");
													
						$("#qThreshhold").removeAttr("max");
					}
				//$('#thresholdUnitId').multiselect('refresh');
			}else{
				$('#thresholdUnitId').val("");
				//$('#thresholdUnitId').multiselect('refresh');
			}
			
			if(validationArray["vaidationRuleType"]!=null && validationArray["vaidationRuleType"]!=""){
				var tValue = validationArray["vaidationRuleType"];
				$('#ruleTypeId').val(tValue);
				//$('#ruleTypeId').multiselect('refresh');
			}else{
				$('#ruleTypeId').val("");
				//$('#ruleTypeId').multiselect('refresh');
			}
			
				
			
			
            selectedDateForEdit = moment(validationArray["effectiveDate"]).format(momentDateFormat);
            $("#validationDescriptionTextArea").val(validationArray["validationExpression"].replace(/\\"/g, '"'));
            if (validationArray["validationCategory"] == "Regulatory") {
                $('#regulatoryId').prop('checked', true);
            } else {
                $('#customId').prop('checked', true);
            }
            if (validationArray["validationType"] == "Mandatory") {
                $('#mandatoryId').prop('checked', true);
                hideTypeOccurence();
            } else {
                showTypeOccurence();
                $('#optionalId').prop('checked', true);
                if (validationArray["isCommentAtValidationLevel"] == "N") {
                    $('#everyOccurenceDropDown').val("Every Occurence");
                    $('#lableToEnableComment1').css('display', 'block');
                    $('#lableToEnableComment2').css('display', 'none');
                } else {
                    $('#everyOccurenceDropDown').val("Per Validation");
                    $('#lableToEnableComment1').css('display', 'none');
                    $('#lableToEnableComment2').css('display', 'block');
                }
            }
        }
    }
    if($('#editValidationbtn').attr('disabled') != 'disabled'){
    	contentNonEditableMethod();
    }
    if (isView == "Y") {
        $('#editValidationbtn').attr('disabled', 'disabled');
        contentNonEditableMethod();
    }
}

function validationJsonCreation() {
    var validationObj = {};
    validationObj["validationCode"] = $("#validationCodeTextId").val();
    validationObj["validationName"] = $("#validationNameTextId").val();
    validationObj["validationDescription"] = $("#validationDescriptionTextId").val();
    validationObj["validationGroup"] = $("#groupReturnId").val()==null?-9999:$("#groupReturnId").val();
    validationObj["effectiveDate"] = $("#date-picker-id").data('datepicker').getFormattedDate(validationJsonDate);
    validationObj["validationType"] = $('input[name=typeBtn]:checked').parent().text().trim();
    validationObj["validationCategory"] = $('input[name=categoryBtn]:checked').parent().text().trim();
    validationObj["validationStatus"] = $("#statusId").val().trim();
    validationObj["validationExpression"] = $("#validationDescriptionTextArea").val();
	
	
	//new columns for entities
	validationObj["validationRuleType"] = $('#ruleTypeId').val()==null?"":$('#ruleTypeId').val().trim();
	validationObj["validationEntities"] = $("#returnNameId").val()==null?"":$("#returnNameId").val();
    validationObj["validationColumn"] = ($("#returnNameSection").val()==null || $("#returnNameSection").val()=="" || $("#returnNameSection").val()=="Select Option")?null:$("#returnNameSection").val();
	validationObj["thresholdUnit"] = $('#thresholdUnitId').val()==null?null:$('#thresholdUnitId').val().trim();
	validationObj["measuredOn"] = $('#measuredOnId').val()==null?null:$('#measuredOnId').val().trim();
	validationObj["acceptableThreshold"] = $('#aThreshhold').val()==null?null:$('#aThreshhold').val().trim();
	validationObj["questionableThreshold"] = $('#qThreshhold').val()==null?null:$('#qThreshhold').val().trim();
    return validationObj;
}
function checkDuplicateForValidation() {
    $.ajax({
        url: 'checkduplicateforvalidation.htm',
        type: 'POST',
        dataType: 'json',
        data: {
            returnName: "",
            validationCode: ""
        },
        success: function(response) {}
    }).done(function() {});
}



function validateExpression(){
	var valiDationInd=checkMandatoryFieldForValidation();
	if(valiDationInd[0]){
		closeAlertValidationPopup();
		var validateExpressionObj=validationJsonCreation();
		if($("#validationDescriptionTextArea").val().trim()!=""){
			$(".overlay").css("display","block");
			$.ajax({
			url: 'validatevalidationForEnt.htm',
			data: {
				validateJsonObj : JSON.stringify(validateExpressionObj)
			},
			success: function(response) {
						if(response.success!=""){
							$('#saveValidationbtn').attr('disabled',true);
							$("#validation-msg-div").empty();
							var arrayOfErrors=response.success.split("\n");
							arrayOfErrors.splice(arrayOfErrors.length-1,1);
							if(arrayOfErrors.length>1){
								$('#validation-msg').hide();
								createToggleErrorMsg(arrayOfErrors,"validation-multi-msg");
								$("#validation-multi-msg").show();
								$('#validationDescriptionTextArea').addClass('textarea-height');
							}
							else{
								$("#validation-multi-msg,#validation-multi-msg-2").hide();
								$("#validation-msg-div").append("<small class='vs-baseline-regular-black'>"+response.success+"</small>");
								$('#validationDescriptionTextArea').addClass('textarea-height');
								$('#validation-msg').addClass('error').show();
							}
							$(".overlay").css("display","none");
						}else{
							if(isEdit!=true){
								saveValidationCheck();
							}else{
								saveValidation();
							}
						}
				}
			}).done(function(){
			});
		}
		else{
			$("#validation-multi-msg,#validation-multi-msg-2").hide();
			$("#validation-msg-div").empty();
			$("#validation-msg-div").append("<small class='vs-baseline-regular-black'>Expression is Empty</small>");
			$('#validation-msg').show();
			$('#validationDescriptionTextArea').addClass('textarea-height');
			$(".overlay").css("display","none");
		}
	}
	else{
		if(valiDationInd[1]=="Y"){
			$("#validateValidationbtn-btn").attr("data-scroll-to",".class1");
			scrollAutoFocus("#validateValidationbtn-btn");
		}
		else if($("#expressionEditorPanel").css("max-height")!="0px"){
			$("#validateValidationbtn-btn").attr("data-scroll-to",".class2");
			scrollAutoFocus("#validateValidationbtn-btn");
		}
	}
}

function appendOptionsCreateAndEdit(data, id) {
	if(id=="statusId" || id=="everyOccurenceDropDown"){
		$("#"+id+" option").remove();
	}
	else{
		$("#"+id+" option:not(:first)").remove();
	}
	var dataLen = data.length;
	for (var i = 0; i < dataLen; i++) {
		if (id == "everyOccurenceDropDown" || id == "statusId" || id == "groupReturnId" || id == "ruleTypeId") {
			if (i == 0 ) { //this part of if condition removed since default value should be there in ent RBL && id != "groupReturnId"
				$('#' + id).append("<option value='" + data[0].trim() + "'selected>" + data[0].trim() + "</option>");
			} else {
				$('#' + id).append("<option value='" + data[i].trim() + "'>" + data[i].trim() + "</option>");
			}
		} else {
			$('#' + id).append("<option value='" + data[i].trim() + "'>" + data[i].trim() + "</option>");
		}
		if (id == "returnNameId") {
			returnArray.push(data[i].trim());
		}
	}
	if(id=="returnNameId" || id=="returnNameSection"){
		$('#' + id).multiselect({
			enableCaseInsensitiveFiltering: true
		});
		$('.multiselect-container').css('width','auto');
		$('.multiselect-container .radio input[type="radio"]').css('display','none');
	}
}

function saveValidationCheck() {
    $.ajax({
        url: 'checkduplicateforvalidationEnt.htm',
		type: 'POST',
		dataType: 'json',
        data: {
            entityName: $("#returnNameId").val(),
            validationCode: $("#validationCodeTextId").val()
        },
        success: function(response) {
            if (response.response == false) {
                saveValidation();
            } else {
                $("#alertMessageDisplay").text("Duplicate validation code");
                $("#alertMessageOnTop").css("display", "flex");
                $(".overlay").css("display", "none");
            }
        }
    }).done(function() {});
}

function clickOnEffectiveDate(e) {
    if (isChangeFlag == "Y") {
        $('#onClickingEffectiveDateAlert').css('display', 'flex');
        lastClicked = e;
    } else {
        var effectiveDateOnBar = e.textContent;
        $(e).addClass('nameProgressAddClass');
        $(e).siblings().removeClass('nameProgressAddClass');
        $($(e).prev()).addClass('clickedDateClass');
        $($(e).prev()).siblings().removeClass('clickedDateClass');
        populateReturnValidationData(moment(effectiveDateOnBar).format(momentDateFormat));
        selectedDateForEdit = moment(effectiveDateOnBar).format(momentDateFormat);
    }
}

function contentEditableMethod() {
    $("#validationCodeTextId").attr('disabled', 'disabled');
    $("#date-picker-id").removeAttr('disabled');
    $("#validationNameTextId").removeAttr('disabled');
    $("#validationDescriptionTextId").removeAttr('disabled');
   // $("#returnNameId").attr('disabled', 'disabled');
    //$('#returnNameSection').removeAttr('disabled');
	$("#returnNameId").multiselect('disable');
	$("#returnNameSection").multiselect('enable');
    $('#groupReturnId').removeAttr('disabled');
    
    $('#statusId').removeAttr('disabled');
    $("#validationDescriptionTextArea").removeAttr('disabled');
    $("input[name=categoryBtn]").removeAttr('disabled');
    $("input[name=typeBtn]").removeAttr('disabled');
    $('#validationExpressionSuggestion').css('display', 'block');
    $('#editGroupBtns').css('display', 'none');
    $('#nonEditGroupBtns').css('display', 'block');
    $('#everyOccurenceDropDown').removeAttr('disabled');
	
	//new columns for entities
	$('#ruleTypeId').removeAttr('disabled');
	$('#thresholdUnitId').removeAttr('disabled');
	$('#measuredOnId').removeAttr('disabled');
	$('#aThreshhold').removeAttr('disabled');
	$('#qThreshhold').removeAttr('disabled');
}

function contentNonEditableMethod() {
    $("#validationCodeTextId").attr('disabled', 'disabled');
    $("#date-picker-id").attr('disabled', 'disabled');
    $("#validationNameTextId").attr('disabled', 'disabled');
    $("#validationDescriptionTextId").attr('disabled', 'disabled');
  //  $('#returnNameId').attr('disabled', 'disabled');
   // $('#returnNameSection').attr('disabled', 'disabled');
	$("#returnNameId").multiselect('disable');
	$("#returnNameSection").multiselect('disable');
    $('#groupReturnId').attr('disabled', 'disabled');
    $('#statusId').attr('disabled', 'disabled');
    $("#validationDescriptionTextArea").attr('disabled', 'disabled');
    $("input[name=categoryBtn]").attr('disabled', 'disabled');
    $("input[name=typeBtn]").attr('disabled', 'disabled');
    $('#validationExpressionSuggestion').css('display', 'none');
    $('#everyOccurenceDropDown').attr('disabled', 'disabled');
	
	//new columns for entities
	$('#ruleTypeId').attr('disable', 'disabled');
	$('#thresholdUnitId').attr('disable', 'disabled');
	$('#measuredOnId').attr('disable', 'disabled');
	$('#aThreshhold').attr('disabled', 'disabled');
	$('#qThreshhold').attr('disabled', 'disabled');
}

function saveValidation() {
    var saveValidationObj = validationJsonCreation();
    $.ajax({
        url: 'savevalidationForEnt.htm',
        type: 'POST',
        dataType: 'json',
        data: {
            saveValidationObj: JSON.stringify(saveValidationObj),
            isEdit: isEdit
        },
        success: function(response) {
            if (response.success == true) {
                //alert('Save is successful'); 
                //window.location = 'loadreturnvalidationpage.htm';
            	renderReturnValidationGrid();
            	reloadBackToLandingPage();
            } else {
                $("#alertMessageDisplay").text("Save is unsuccessful");
                $("#alertMessageOnTop").css("display", "flex");
            }
            $(".overlay").css("display", "none");
        }
    }).done(function() {});
}

function checkMandatoryFieldForValidation() {
    var valid = true;
    var errorInValidationEle = "N";
    $('.vs-alert-text').remove();
    if ($('#validationCodeTextId').val().trim() == "" || $('#validationCodeTextId').val() == null || $('#validationCodeTextId').val() == undefined) {
        valid = false;
        errorInValidationEle = "Y";
        $('#validationCodeTextId').attr("required", "");
        $('#validationCodeTextId').addClass("dropDown-redBorder");
        $('#validationCodeTextId').after("<p class='vs-alert-text'>Validation code is mandatory</p>");
    }
    if ($('#date-picker-id').val() == "" || $('#date-picker-id').val() == null || $('#date-picker-id').val() == undefined) {
        valid = false;
        errorInValidationEle = "Y";
        $('#date-picker-id').attr("required", "");
        $('#date-picker-id').addClass("dropDown-redBorder");
        $('#date-picker-id').after("<p class='vs-alert-text'>Effective date is mandatory</p>");
    }
    if ($('#validationNameTextId').val().trim() == "" || $('#validationNameTextId').val() == null || $('#validationNameTextId').val() == undefined) {
        valid = false;
        errorInValidationEle = "Y";
        $('#validationNameTextId').attr("required", "");
        $('#validationNameTextId').addClass("dropDown-redBorder");
        $('#validationNameTextId').after("<p class='vs-alert-text'>Validation Name is necessary</p>");
    }
    if ($('#returnNameId').val() == "" || $('#returnNameId').val() == null || $('#returnNameId').val() == undefined) {
        valid = false;
        $('#returnNameId').addClass("dropDown-redBorder");
        $('#returnNameId').siblings('.btn-group').after("<p class='vs-alert-text'>Entity name is mandatory</p>");
    }
    if($('#statusId').val()!="INACTIVE"){
    	/*if ($('#returnNameSection').val() == "" || $('#returnNameSection').val() == null || $('#returnNameSection').val() == undefined || $('#returnNameSection').val() == "Select Option") {
            valid = false;
            $('#returnNameSection').addClass("dropDown-redBorder");
            $('#returnNameSection').siblings('.btn-group').after("<p class='vs-alert-text'>Section is mandatory</p>");
        }*/
        if ($('#groupReturnId').val() == "" || $('#groupReturnId').val() == null || $('#groupReturnId').val() == undefined) {
            valid = false;
            $('#groupReturnId').addClass("dropDown-redBorder");
            $('#groupReturnId').after("<p class='vs-alert-text'>Group is mandatory</p>");
        }
    }
    if ($('#statusId').val() == "" || $('#statusId').val() == null || $('#statusId').val() == undefined) {
        valid = false;
        $('#statusId').addClass("dropDown-redBorder");
        $('#statusId').after("<p class='vs-alert-text'>Status is mandatory</p>");
    }
	
	if ($('#ruleTypeId').val() == "" || $('#ruleTypeId').val() == null || $('#ruleTypeId').val() == undefined) {
        valid = false;
        $('#ruleTypeId').addClass("dropDown-redBorder");
        $('#ruleTypeId').after("<p class='vs-alert-text'>Rule type is mandatory</p>");
    }
	
	if($("#thresholdUnitId").val()!=null && $("#thresholdUnitId").val()!=undefined && $("#thresholdUnitId").val()!="" && 
		($("#thresholdUnitId").val().toLowerCase()=="percentage"|| $("#thresholdUnitId").val().toLowerCase()=="absolute")){
		if($("#measuredOnId").val()==null || $("#measuredOnId").val()==undefined || $("#measuredOnId").val()==""){
			valid = false;
			$('#measuredOnId').addClass("dropDown-redBorder");
			$('#measuredOnId').after("<p class='vs-alert-text'>Please select a value.</p>");
		}
		
		if($("#aThreshhold").val()=="" || $("#aThreshhold").val()=="0"){
			valid = false;
			$('#aThreshhold').addClass("dropDown-redBorder");
			$('#aThreshhold').parent().after("<p class='vs-alert-text'>Value should be greater than 0</p>");
		}
		if($("#qThreshhold").val()=="" || $("#qThreshhold").val()=="0"){
			valid = false;
			$('#qThreshhold').addClass("dropDown-redBorder");
			$('#qThreshhold').parent().after("<p class='vs-alert-text'>Value should be greater than 0</p>");
		}
		
		if($("#thresholdUnitId").val().toLowerCase()=="percentage"){
			if(Number($("#aThreshhold").val())>100){
				valid = false;
				$('#aThreshhold').addClass("dropDown-redBorder");
				$('#aThreshhold').parent().after("<p class='vs-alert-text'>Value should be less than 100</p>");
			}
			if(Number($("#qThreshhold").val())>100){
				valid = false;
				$('#qThreshhold').addClass("dropDown-redBorder");
				$('#qThreshhold').parent().after("<p class='vs-alert-text'>Value should be less than 100</p>");
			}
		}
		if($("#measuredOnId").val()=="Invalid Occurrences"){
			if(Number($("#aThreshhold").val())>Number($("#qThreshhold").val())){
				valid = false;
				$('#aThreshhold').addClass("dropDown-redBorder");
				$('#aThreshhold').parent().after("<p class='vs-alert-text'>Value should be less than/ equal to Questionable Threshold</p>");
			}
		}else if($("#measuredOnId").val()=="Valid Occurrences"){
			if(Number($("#aThreshhold").val())<Number($("#qThreshhold").val())){
				valid = false;
				$('#qThreshhold').addClass("dropDown-redBorder");
				$('#qThreshhold').parent().after("<p class='vs-alert-text'>Value should be less than/ equal to Acceptable Threshold</p>");
			}
		}
	}
    return [valid, errorInValidationEle];
}

function showTypeOccurence() {
    $('#typeOccurenceId').css('display', 'block');
}

function hideTypeOccurence() {
    $('#typeOccurenceId').css('display', 'none');
}

function createFunctionAndOperatorList() {
    var uniqueFuncParentElem = jsonPath(jsonPath(functionsAndOperatorArray, "$.[?(@.category=='Functions')]"), "$.[*].subCategory").unique();
    var uniqueOperParentElem = jsonPath(jsonPath(functionsAndOperatorArray, "$.[?(@.category=='Operator')]"), "$.[*].subCategory").unique();

    for (var i = 0; i < uniqueFuncParentElem.length; i++) {
        var functionsArray = jsonPath(jsonPath(functionsAndOperatorArray, "$.[?(@.category=='Functions')]"), "$.[?(@.subCategory=='" + uniqueFuncParentElem[i] + "')]");
        var functionLen = functionsArray.length;
        $('#function-tab .expression-scroll').append("<div class='parent-div-elem' onclick='toggleChildElements(this);'>" + uniqueFuncParentElem[i] + "</div>");
        for (var j = 0; j < functionLen; j++) {
            $('#function-tab .expression-scroll').append("<div class='child-div-elem hideShowClass' parent-div-elem='" + uniqueFuncParentElem[i] + "' expressionFormat='" + functionsArray[j].expressionFormat + "' format-str='" + functionsArray[j].internalValue + "' onclick='showFormat(this);'>" + functionsArray[j].name + "</div>");
        }
    }
    for (var i = 0; i < uniqueOperParentElem.length; i++) {
        var operatorArray = jsonPath(jsonPath(functionsAndOperatorArray, "$.[?(@.category=='Operator')]"), "$.[?(@.subCategory=='" + uniqueOperParentElem[i] + "')]");
        var operatorLen = operatorArray.length;
        $('#operator-tab .expression-scroll').append("<div class='parent-div-elem' onclick='toggleChildElements(this);'>" + uniqueOperParentElem[i] + "</div>");
        for (var j = 0; j < operatorLen; j++) {
            $('#operator-tab .expression-scroll').append("<div class='child-div-elem hideShowClass' parent-div-elem='" + uniqueOperParentElem[i] + "' expressionFormat='" + operatorArray[j].expressionFormat + "' format-str='" + operatorArray[j].internalValue + "' onclick='showFormat(this)'>" + operatorArray[j].name + "</div>");
        }
    }
}
Array.prototype.unique = function() {
    return this.filter(
        function(val, i, arr) {
            return (i <= arr.indexOf(val));
        }
    );
}

function filterValidationData(e, event) {
    $("#return-Search").siblings('.clearInputClass').show();
    filter = $('#return-Search').val().trim();
    if(filter==""){
    	$('#expression-data .main-parent').remove();
    	$('.parent-div').removeClass('parent-div-hide displayClass');
    }
    else if (event.keyCode == 190) {
    	var $txt = $(e);
        var caretPos = $txt[0].selectionStart;
        var textAreaTxt = $txt.val();
       // if(textAreaTxt[caretPos-2]=='"'){
        	$("#return-Search").val(function() {
                return this.value + '""';
            });
        	var element = $('#return-Search')[0];
            var elementLength = $('#return-Search').val().length - 1;
            element.focus();
            element.setSelectionRange(elementLength, elementLength);
            var inputValLen = $('#return-Search').val().split('.').length;
            var inputText = $('#return-Search').val().split('.');
            var inputVal = inputText.slice(inputValLen - 2, inputValLen - 1)[0];
            var levelInd = "level-".concat(inputValLen == 0 ? 1 : inputValLen - 1);
            console.log("levelInd",levelInd);
            var rtnType = "GRID";
            if (inputText[0].replace(/"/g, '').toUpperCase() == "RTN") {
                createListOfElements(inputVal.replace(/"/g, ''), "", "undefined", "undefined", levelInd, rtnType, "");
            } else if (inputText[0].replace(/"/g, '').toUpperCase() == "ENT") {
                createListOfElementsForEntity(inputVal.replace(/"/g, ''), levelInd );
            } else {
                //createListOfElementsForReftbl($('#expression-data .main-parent .contentDiv')[0], levelInd);
            }
        /*}else{
        	var input = $('#return-Search').val().split('.');
            var inputValLen = input.length;
            var levelInd = "level-".concat(inputValLen == 0 ? 1 : inputValLen - 1);
            var filter = input[input.length - 1];
            hideAndShowFactors(filter, input.length, levelInd);
        }*/
    } else if (event.keyCode == 8) {
        var inputValLen = $('#return-Search').val().split('.').length;
        var inputText = $('#return-Search').val().split('.');
        var inputVal = inputText.slice(inputValLen - 2, inputValLen - 1)[0];
        var levelNumber = inputValLen == 0 ? 1 : inputValLen - 1;
        var levelInd = "level-".concat(levelNumber);
        var currentLevelInd = "level-".concat(levelNumber + 1);
        var rtnType = "GRID";
        if (currentLevelInd.toLowerCase() != ($($('#expression-data .main-parent .contentDiv')[0]).attr('level') == undefined ? "" : $($('#expression-data .main-parent .contentDiv')[0]).attr('level')).toLowerCase()) {
            if (inputText[0].replace(/"/g, '').toUpperCase() == "RTN" && levelNumber > 0) {
                createListOfElements(inputVal.replace(/"/g, ''), "", "undefined", "undefined", levelInd, rtnType, "");
            }else if (inputText[0].replace(/"/g, '').toUpperCase() == "ENT" && levelNumber > 0) {
                createListOfElementsForEntity(inputVal.replace(/"/g, ''), levelInd );
            } else if (levelNumber == 0) {
                $('.parent-div').removeClass('parent-div-hide');
                $('#expression-data .main-parent').remove();
            } else {
                //createListOfElementsForReftbl($('#expression-data .main-parent .contentDiv')[0], levelInd);
            }
        }
        (function($) {
            jQuery.expr[':'].Contains = function(a, i, m) {
                return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
            };
            var input = $('#return-Search').val().split('.');
            var filter = input[input.length - 1];
            hideAndShowFactors(filter, input.length, levelInd);
        }(jQuery));
    } else {
        (function($) {
            jQuery.expr[':'].Contains = function(a, i, m) {
                return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
            };
            var input = $('#return-Search').val().split('.');
            var inputValLen = input.length;
            var levelInd = "level-".concat(inputValLen == 0 ? 1 : inputValLen - 1);
            var filter = input[input.length - 1];
            hideAndShowFactors(filter, input.length, levelInd);
        }(jQuery));
    }
}

function addValueIntoInputBox(e, levelInd, rtnType) {
    var previousValue = $('#return-Search').val().split('.').splice(0, $('#return-Search').val().split('.').length - 1).join("~||~");
    if (levelInd != "level-1" && levelInd != undefined) {
        $('#return-Search').val(previousValue.split("~||~").join('.') + '."' + $(e).val() + '"' + '.""');
    } else if (levelInd == undefined) {
        $('#return-Search').val(previousValue.split("~||~").join('.') + '."' + $(e).val() + '"');
    } else {
       // $('#return-Search').val(previousValue.split("~||~").join(".") + $(e).attr('value') + '.""');
    	$('#return-Search').val(previousValue.split("~||~").join('.') + $(e).attr('value') +'.""');
    }
    $("#return-Search").siblings('.clearInputClass').show();
    $("#return-Search").siblings('.search-class').addClass("borderClass");
    var element = $('#return-Search')[0];
    var elementLength = $('#return-Search').val().length - 1;
    element.focus();
    element.setSelectionRange(elementLength, elementLength);
    createListOfElements($(e).text(), e.getAttribute("sectionType"), e.getAttribute("entityName"), e.getAttribute("entityVersion"), levelInd, rtnType, "");
}

function addValueIntoInputBoxOfReftbl(e, levelInd, rtnType) {
    var previousValue = $('#return-Search').val().split('.').splice(0, $('#return-Search').val().split('.').length - 1).join("~||~");
	if (levelInd != "level-1" && levelInd != undefined) {
        $('#return-Search').val(previousValue.split("~||~").join('.') + '."' + $(e).val() + '"' + '.""');
    } else if (levelInd == undefined) {
        $('#return-Search').val(previousValue.split("~||~").join('.') + '."' + $(e).val() + '"');
    } else {
        //$('#return-Search').val(previousValue.split("~||~").join(".") + $(e).attr('value') + '.""');
    	$('#return-Search').val(previousValue.split("~||~").join('.') +$(e).attr('value')+'.""');
    }
	$("#return-Search").siblings('.clearInputClass').show();
	$("#return-Search").siblings('.search-class').addClass("borderClass");
    var element = $('#return-Search')[0];
    var elementLength = $('#return-Search').val().length - 1;
    element.focus();
    element.setSelectionRange(elementLength, elementLength);
    createListOfElementsForReftbl(e, levelInd);
}

function filterValidationFuncAndOper(elemId, parentId) {
	$("#"+elemId).siblings('.clearInputClass').show();
	$("#"+elemId).siblings('.search-class').addClass("borderClass");
    (function($) {
        jQuery.expr[':'].Contains = function(a, i, m) {
            return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
        };
                    var filter = $('#' + elemId).val();
                    if (filter) {
                        $($('#' + parentId)).find(".child-div-elem:not(:Contains(" + filter + "))").addClass('hideShowClass');
                        $($('#' + parentId)).find(".child-div-elem:Contains(" + filter + ")").removeClass('hideShowClass');
                    } else {
                    	//$("#"+elemId).siblings('.clearInputClass').hide();
                    	//$("#"+elemId).siblings('.search-class').removeClass("borderClass");
                        $('#' + parentId).find(".parent-div-elem").removeClass('hideShowClass');
                        $('#' + parentId).find(".child-div-elem").addClass('hideShowClass');
                    }
    }(jQuery));
}

function showFormat(e) {
	var parentElem = $(e).parent().attr("id");
    $('#'+parentElem+' .child-div-elem').removeClass('highLightClass');
    $(e).addClass('highLightClass');
    $("#"+parentElem).siblings(".expression-format").children(".example-div").text($(e).attr("expressionFormat")).attr("format-str", $(e).attr("format-str"));
}

function reloadBackToLandingPage() {
    //window.location = 'loadreturnvalidationpage.htm';
	isChangeFlag = "N";
	$("#suggestiondivID").css("display","none");
	$('#validationListContainer').css("display", "block");
	$('#validation-create-edit-con').css("display", "none");
	
	$('#editValidationbtn').attr('disabled', false);
    $('.editGroupBtns').css('display', 'none');
    $('.nonEditGroupBtns').css('display', 'block');
    
    $('.nameProgress').removeClass('nameProgressAddClass');
    $('.dateSliderValue').removeClass('clickedDateClass');
    
}

function addDataElementToTextArea(e, data) {
    var cursorPos = $('#validationDescriptionTextArea').prop('selectionStart');
    var v = $('#validationDescriptionTextArea').val();
    var textBefore = v.substring(0, cursorPos);
    var textAfter = v.substring(cursorPos, v.length);
    var newTextLen = "";
    var newText = "";
    $('.search-class').removeClass("borderClass");
    if (data != "rtn") {
        newText = $(e).siblings('.example-div').attr("format-str");
        clearInpBoxAndReloadData("", $(e).attr("id"),$(e).parent().siblings('.expression-scroll').attr('id'));
    } else {
        newText = $('#return-Search').val().trim();
        if (newText.slice(newText.length - 1, newText.length) == ".") {
            newText = newText.slice(0, newText.length - 1);
        }
    }
    if (newText != "") {
        var str = newText;
        var regex = new RegExp("[\(\)]");
        if (regex.test(str)) {
            $('#validationDescriptionTextArea').val(textBefore + str + textAfter);
            newTextLen = str.length - 1;
        } else {
            $('#validationDescriptionTextArea').val(textBefore + str + textAfter);
            newTextLen = str.length;
        }

    }
    var element = $('#validationDescriptionTextArea')[0];
    var elementLength = textBefore.length + newTextLen;
    element.focus();
    element.setSelectionRange(elementLength, elementLength);
}

function closeValidationMsgDailog(){
	$("#validation-multi-msg,#validation-multi-msg-2,#validation-msg").hide();
	$("#validationDescriptionTextArea").removeClass('validationTxtAreaClass');
}

function toggleChildElements(e) {
	var parentElem = $(e).parent().attr('id');
    $('#'+parentElem+' .child-div-elem[parent-div-elem="' + $(e).text() + '"]').toggleClass('hideShowClass');
    $('#'+parentElem+' .child-div-elem[parent-div-elem!="' + $(e).text() + '"]').addClass('hideShowClass');
}

function clearInpBoxAndReloadData(ind, elementId,parentId) {
	if(ind=="returnData"){
		 expressionObj = {};
		    $("#return-Search").siblings('.clearInputClass').hide();
		    $("#return-Search").siblings('.search-class').removeClass("borderClass");
		    $('#return-Search').val("");
		    $('#expression-data').find('*').remove();
		    $('#expression-data').find('.child-div').remove();
		    $('.parent-div').removeClass('parent-div-hide');
			
			
			// ENT module
			
			var fragment2 = document.createDocumentFragment();
		    var parentElement2 = document.createElement('div');
		    parentElement2.setAttribute('class', 'parent-div');
		    var mainElement2 = document.createElement('div');
		    mainElement2.setAttribute('class', 'contentDiv');
		    mainElement2.setAttribute('ondblclick', 'addValueIntoInputBoxOfEnt(this,"level-1")');
		    mainElement2.setAttribute('level', 'level-1');
		    mainElement2.textContent = "ENT";
		    mainElement2.setAttribute('value', "ENT");
		    var childElement2 = document.createElement('div');
		    childElement2.setAttribute('class', 'vs-tooltip icon-info');
		    var iElement2 = document.createElement('i');
		    iElement2.setAttribute('class', 'icon-small icon-info-solid');
		    var spanElement2 = document.createElement('span');
		    spanElement2.setAttribute('class', 'vs-tooltiptext-left');
		    spanElement2.setAttribute('style', 'width:max-content');
		    var innerSpanElement2 = document.createElement('span');
		    innerSpanElement2.setAttribute('class', 'vs-baseline-medium-primary');
		    innerSpanElement2.textContent = "Entities";
		    spanElement2.appendChild(innerSpanElement2);
		    childElement2.appendChild(iElement2);
		    childElement2.appendChild(spanElement2);


		    parentElement2.appendChild(mainElement2);
		    parentElement2.appendChild(childElement2);
		    fragment2.appendChild(parentElement2);
		    $("#expression-data").append(fragment2);
			//end ENT module
			
			
			
	}
	else{
		$('#'+parentId).siblings().find(".clearInputClass").hide();
		$('#'+parentId).siblings().find(".search-class").removeClass("borderClass");
		$('#'+parentId).siblings().find(".search-class").val("");
		$('#'+parentId).find(".parent-div-elem").removeClass('hideShowClass');
		$('#'+parentId).find(".child-div-elem").addClass('hideShowClass').removeClass('highLightClass');
		$("#"+elementId).parent().children(".expression-format .example-div").text("").attr("format-str", "");
	}
}

function clickToChnge() {
    isChangeFlag = "N";
    clickOnEffectiveDate(lastClicked);
    $('#onClickingEffectiveDateAlert').css('display', 'none');
}

function closeAlertMessage(e) {
    $("#alertMessageOnTop").css("display", "none");
}

function scrollAutoFocus(e) {
    var $this = $(e),
        $toElement = $this.attr('data-scroll-to'),
        $focusElement = $this.attr('data-scroll-focus'),
        $offset = $this.attr('data-scroll-offset') * 1 || 0,
        $speed = $this.attr('data-scroll-speed') * 1 || 500;
    $('html, body').animate({
        scrollTop: $($toElement).offset().top + $offset
    }, $speed);
    if ($focusElement) $($focusElement).focus();
}

function clickToCancelOverridden() {
    $('#date-picker-id').datepicker('setDate', selectedDateForEdit);
    $('#onClickingEffectiveDateGreaterAlert').css('display', 'none');
}

function clickToChngeOveridden() {
    $('#onClickingEffectiveDateGreaterAlert').css('display', 'none');
}

function closeAlertValidationPopup(){
	$("#validation-multi-msg,#validation-multi-msg-2,#validation-msg").hide();
	$('#validationDescriptionTextArea').removeClass('textarea-height');
	var position = getCaretPosition($('#validationDescriptionTextArea')[0]);
	cursorPosY=17+position.y-$('#validationDescriptionTextArea')[0].getBoundingClientRect().top;
	cursorPosX=position.x-$('#validationDescriptionTextArea')[0].getBoundingClientRect().left;
}

function getFactorDataOnClick(e) {
    var $txt = $(e);
    var caretPos = $txt[0].selectionStart;
    var textAreaTxt = $txt.val();
    
    if(caretPos<textAreaTxt.length){
    	var txtArray = textAreaTxt.split('.');
        var temp = 0;
    	for (var i = 0; i < txtArray.length; i++) {
            temp = temp + txtArray[i].length;
            if (temp + i >= caretPos) {
                var inputText = $('#return-Search').val().split('.');
                var element = $('#return-Search')[0];
                var levelNumber = i;
                var levelInd = "level-".concat(levelNumber);
                var currentLevelInd = "level-".concat(levelNumber + 1);
                var rtnType = "GRID";
                if(currentLevelInd=="level-3"){
                	
                }
                if(i==0){
                	$('#return-Search').val($('#return-Search').val().substring(0, temp+i - (txtArray[i].length)));
                }else{
                	$('#return-Search').val($('#return-Search').val().substring(0, temp+i - (txtArray[i].length))+'""');
                }
                
                element.focus();
                element.setSelectionRange($('#return-Search').val().length-1,$('#return-Search').val().length-1);
                if (currentLevelInd.toLowerCase() != ($($('#expression-data .main-parent .contentDiv')[0]).attr('level') == undefined ? "" : $($('#expression-data .main-parent .contentDiv')[0]).attr('level')).toLowerCase()) {
                    if (inputText[0].replace(/"/g, '').toUpperCase() == "RTN" && levelNumber > 0) {
                        createListOfElements(txtArray[i - 1].replace(/"/g, ''), "", "undefined", "undefined", levelInd, rtnType, "");
                    } else if (levelNumber == 0) {
                        $('.parent-div').removeClass('parent-div-hide displayClass');
                        $('#expression-data .main-parent').remove();
                        $('#return-Search').keyup();
                    } else {
                       // createListOfElementsForReftbl($('#expression-data .main-parent .contentDiv')[0], levelInd);
                    }
                }
                break;
            }
        }
    }
}

function hideAndShowFactors(filter, inputlen, levelInd) {
	var regPattern = new RegExp(/"(?:[^"]|.)*"/);
    if (filter != "" && inputlen > 1 && regPattern.test(filter)) {
        filter = filter.replace(/"/g, '');
        $($('#expression-data div')).find(".contentDiv:not(:Contains(" + filter + "))").parent().addClass('displayClass');
        $($('#expression-data div')).find(".contentDiv:Contains(" + filter + ")").parent().removeClass('displayClass');
    } else if (levelInd == "level-0") {
    	$('#expression-data .main-parent').remove();
        $('.parent-div').removeClass('parent-div-hide displayClass');
        $($('#expression-data div')).find(".contentDiv:not(:Contains(" + filter + "))").parent().addClass('displayClass');
        $($('#expression-data div')).find(".contentDiv:Contains(" + filter + ")").parent().removeClass('displayClass');
    }
}
function createToggleErrorMsg(arrayOfErrors,id1)
{
	$($("#"+id1).children()[0]).children().text("Total Errors: "+arrayOfErrors.length);
	$("#"+id1).next("div").find("ul").empty();
	for(var i=0;i<arrayOfErrors.length;i++){
		var j=i+1;
		$("#"+id1).next("div").find("ul").append("<li>"+j+") "+arrayOfErrors[i]+"</li><br>");
		//$("#"+id1).next("div").find("ul").append("<li>"+arrayOfErrors[i]+"</li>");		
	}
}
function toggleAlertPanel(hideId,showId,ind){
	$("#"+hideId).hide();
	$("#"+showId).show();
	if(ind=="show"){
		$("#validationDescriptionTextArea").css("height","61%");
	}
	else{
		$('#validationDescriptionTextArea').addClass('textarea-height');
	}
}
function onEditMode(isEdit){
	var arrayOfDates = [];
	resetMandatoryHighLight();
	$('.cotainerProgressBar').css('left',0);
	$('#progressBar').empty();
	$('#returnNameId').multiselect('destroy');
	$('#returnNameSection').multiselect('destroy');
	populateReturnValidationData(editValidationArray[0]["effectiveDate"]);
	$('#versionTimelineDiv').css('display', 'block');
	$('#versionTimeline').css('display', 'flex');
	$('#editValidationbtn').css('display', 'block');
	$('.editGroupBtns').css('display', 'none');
    $('.nonEditGroupBtns').css('display', 'block');
    $("#validationDescriptionTextArea").removeClass('textarea-height');
	$('#validation-msg').hide();
	var editValidationArrayLength = editValidationArray[0].data.length;
	for (var j = 0; j < editValidationArrayLength; j++) {
		arrayOfDates.push(moment(moment(editValidationArray[0].data[j]["effectiveDate"],effDateFormat)).format(editValidationJsonDate));
		
	}
	for (var i = 0; i < arrayOfDates.length; i++) {
		$('#progressBar').append("<li class='dateSliderValue'></li><span class='nameProgress' onclick='clickOnEffectiveDate(this);'>" + arrayOfDates[i] + "</span>");
		var view = $(".cotainerProgressBar");
		var move = "100px";
		if (arrayOfDates.length >= 6) {
			sliderLimit = -150 - (150 * (arrayOfDates.length - 6));
			$('.tabScrollValidationClass').css('display', 'block');
		} else {
			$('.tabScrollValidationClass').css('display', 'none');
			$('#versionTimeline').css('justify-content', 'center');
			sliderLimit = 0;
		}
	}
	for (var i = 0; i < editValidationArray[0].data.length; i++) {
		if (moment(editValidationArray[0].data[i]["effectiveDate"]).format(momentDateFormat) == editValidationArray[0]["effectiveDate"]) {
			for (var j = 0; j < $('#progressBar').children('span').length; j++) {
				if (moment($($('#progressBar').children('span')[j]).text()).format(momentDateFormat) == moment(editValidationArray[0].data[i]["effectiveDate"]).format(momentDateFormat)) {
					$($('#progressBar').children('li')[j]).addClass('clickedDateClass');
					$($('#progressBar').children('span')[j]).addClass('nameProgressAddClass');
				}
			}
		}
	}
}
function creatingNewValidation(){
	$('#returnNameId').multiselect('destroy');
	$('#returnNameSection').multiselect('destroy');
	$('#versionTimelineDiv').css('display', 'none');
    $('#versionTimeline').css('display', 'none');
    $("#validationNameHeading").text("New Validation");
    $("#validationCodeTextId").val("").removeAttr('disabled');
    $("#date-picker-id").datepicker('setDate', 'now').removeAttr('disabled');
    $("#validationNameTextId").val("").removeAttr('disabled');
    $("#validationDescriptionTextId").val("").removeAttr('disabled');
    $('#returnNameId').append("<option selected disabled>Select Option</option>");
    $('#returnNameId').multiselect('refresh');
    $('#returnNameSection').append("<option selected>Select Option</option>");
    $('#returnNameSection').multiselect('refresh');
	
    $('#groupReturnId').val("").removeAttr('disabled');
    $('#statusId').val($($("#statusId").children()[0]).val()).removeAttr('disabled');
    $("#validationDescriptionTextArea").val("").removeAttr('disabled');
    $("input[name=categoryBtn]").val("").removeAttr('disabled');
    $("input[name=typeBtn]").val("").removeAttr('disabled');
    $('#validationExpressionSuggestion').css('display', 'block');
    $('#everyOccurenceDropDown').val("").removeAttr('disabled');
    $('#mandatoryId').prop('checked', true);
    $('#regulatoryId').prop('checked', true);
    hideTypeOccurence();
    $('.editGroupBtns').css('display', 'block');
    $('.nonEditGroupBtns').css('display', 'none');
	
	$("#thresholdUnitId").val("");
	$("#measuredOnId").val("");
	$("#ruleTypeId").val("");
	$('#thresholdUnitId').val("").removeAttr('disabled');
	$('#measuredOnId').val("").removeAttr('disabled');
	$('#ruleTypeId').val("").removeAttr('disabled');
	$("#aThreshhold").val("");
    $("#qThreshhold").val("");
	
	
	
	
}

function resetMandatoryHighLight(){
	$('#validationCodeTextId').removeClass("dropDown-redBorder");
	$('#date-picker-id').removeClass("dropDown-redBorder");
	$('#validationNameTextId').removeClass("dropDown-redBorder");
	$('#returnNameId').removeClass("dropDown-redBorder");
	$('#returnNameSection').removeClass("dropDown-redBorder");
	$('#groupReturnId').removeClass("dropDown-redBorder");
	$('#statusId').removeClass("dropDown-redBorder");
	$('.vs-alert-text').remove();
}

















 // ENT module functions
	  
	  
	  function addValueIntoInputBoxOfEnt(e, levelInd) {
	
		var previousValue = $('#return-Search').val().split('.').splice(0, $('#return-Search').val().split('.').length - 1).join("~||~");
			if (levelInd != "level-1" && levelInd != undefined) {
				$('#return-Search').val(previousValue.split("~||~").join('.') + '."' + $(e).val() + '"' + '.""');
			} else if (levelInd == undefined) {
				$('#return-Search').val(previousValue.split("~||~").join('.') + '."' + $(e).val() + '"');
			} else {
			   // $('#return-Search').val(previousValue.split("~||~").join(".") + $(e).attr('value') + '.""');
				$('#return-Search').val(previousValue.split("~||~").join('.') + $(e).attr('value') +'.""');
			}
		$("#return-Search").siblings('.clearInputClass').show();
		$("#return-Search").siblings('.search-class').addClass("borderClass");
		var element = $('#return-Search')[0];
		var elementLength = $('#return-Search').val().length - 1;
		element.focus();
		element.setSelectionRange(elementLength, elementLength);
		
		
		createListOfElementsForEntity($(e).text(), levelInd);
}


     
	 
     function createListOfElementsForEntity(name, ind) {
		 ind = ind == undefined ? "" : ind;
		$('.parent-div').addClass('parent-div-hide');
		$('#expression-data .main-parent').remove();
		
			if (ind.toLowerCase() == "level-1") {
				getEntitySubjectArea();
			} else if (ind.toLowerCase() == "level-2") {
				
			   ddExpressionObj = jsonPath(dEntity["subjectArea"], "$.[?(@.subjectAreaName=='" + name + "')]")[0];
				getEntityTableName(ddExpressionObj.subjectAreaUuid);
				
				
			} else if (ind.toLowerCase() == "level-3") {
				ddExpressionObj = jsonPath(dEntity["entityName"], "$.[?(@.entityName=='" + name + "')]")[0];
				getEntityTableColumnName(ddExpressionObj.entityUuid);
				
				
			   
			}
}


       
     function getEntitySubjectArea() {
		$.ajax({
			url: 'getsubjectAreaForDQ.htm',
			type: 'GET',
			dataType: 'json',
		   
			data: { },
				
				
		   
			success: function(response) {
				$('#expression-data .main-parent').remove();
				var respJSON = JSON.parse(response.response);
				
				dEntity["subjectArea"] = respJSON.model.subjectAreas;
				var returns = respJSON.model.subjectAreas;
				var fragment = document.createDocumentFragment();
				returns.forEach(function(val) {
					var parentElement = document.createElement('div');
					parentElement.setAttribute('class', 'main-parent');
					var mainElement = document.createElement('div');
					mainElement.setAttribute('class', 'child-div contentDiv');
					mainElement.setAttribute('ondblclick', 'addValueIntoInputBoxOfEnt(this,"level-2")');
					mainElement.setAttribute('level', 'level-2');
					/* mainElement.setAttribute('sectionType', val.returnType); */
					mainElement.textContent = val.subjectAreaName;
					mainElement.value = val.subjectAreaName;
					var childElement = document.createElement('div');
					childElement.setAttribute('class', 'vs-tooltip icon-info');
					var iElement = document.createElement('i');
					iElement.setAttribute('class', 'icon-small icon-info-solid');
					var spanElement = document.createElement('span');
					spanElement.setAttribute('class', 'vs-tooltiptext-left');
					spanElement.setAttribute('style', 'width:max-content');
					var smallElement = document.createElement('small');
					smallElement.setAttribute('class', 'vs-baseline-regular-black');
					smallElement.textContent = "Subject Area Name : ";
					var innerSpanElement = document.createElement('span');
					innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
					innerSpanElement.textContent = val.subjectAreaName;
					smallElement.appendChild(innerSpanElement)
					spanElement.appendChild(smallElement);
					childElement.appendChild(iElement);
					childElement.appendChild(spanElement);
					parentElement.appendChild(mainElement);
					parentElement.appendChild(childElement);
					fragment.appendChild(parentElement);
				});
				$("#expression-data").append(fragment);
			}
		}).done(function() {
			$('#return-Search').keyup();
		});
}





       function getEntityTableName(subjectAreaUuid) {
		$.ajax({
			url: 'getentitytablenameforautosuggestion.htm',
			type: 'GET',
			dataType: 'json',
			async:false,
			data: {
			  
				subjectAreaUuid : subjectAreaUuid
				
			},
			success: function(response) {
				$('#expression-data .main-parent').remove();
				dEntity["entityName"] = response.success.model;
	/* 			response = response.success.model; */
				var returns = dEntity["entityName"];
				var fragment = document.createDocumentFragment();
				returns.forEach(function(val) {
					var parentElement = document.createElement('div');
					parentElement.setAttribute('class', 'main-parent');
					var mainElement = document.createElement('div');
					mainElement.setAttribute('class', 'child-div contentDiv');
					mainElement.setAttribute('ondblclick', 'addValueIntoInputBoxOfEnt(this,"level-3")');
					mainElement.setAttribute('level', 'level-3');
					
					mainElement.textContent = val.entityName;
					mainElement.value = val.entityName;
					var childElement = document.createElement('div');
					childElement.setAttribute('class', 'vs-tooltip icon-info');
					var iElement = document.createElement('i');
					iElement.setAttribute('class', 'icon-small icon-info-solid');
					var spanElement = document.createElement('span');
					spanElement.setAttribute('class', 'vs-tooltiptext-left');
					spanElement.setAttribute('style', 'width:max-content');
					var smallElement = document.createElement('small');
					smallElement.setAttribute('class', 'vs-baseline-regular-black');
					smallElement.textContent = "Entity Name : ";
					var innerSpanElement = document.createElement('span');
					innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
					innerSpanElement.textContent = val.entityName;
					smallElement.appendChild(innerSpanElement)
					spanElement.appendChild(smallElement);
					childElement.appendChild(iElement);
					childElement.appendChild(spanElement);
					parentElement.appendChild(mainElement);
					parentElement.appendChild(childElement);
					fragment.appendChild(parentElement);
				});
				$("#expression-data").append(fragment);
			}
		}).done(function() {
			$('#return-Search').keyup();
		});
}





     function getEntityTableColumnName(entityUuid) {
		$.ajax({
			url: 'getEntityColumnByUUIDForDQ.htm',
			type: 'POST',
			dataType: 'json',
			async:false,
			data: {
				entityUuid : entityUuid
			},
			success: function(response) {
				$('#expression-data .main-parent').remove();
				dEntity["entityColumnName"] = response.response;
				var returns = dEntity["entityColumnName"];
				var fragment = document.createDocumentFragment();
				returns.forEach(function(val) {
					var parentElement = document.createElement('div');
					parentElement.setAttribute('class', 'main-parent');
					var mainElement = document.createElement('div');
					mainElement.setAttribute('class', 'child-div contentDiv');
					mainElement.setAttribute('ondblclick', 'addValueIntoInputBoxOfEnt(this)');
					
					mainElement.textContent = val;
					mainElement.value = val;
					var childElement = document.createElement('div');
					childElement.setAttribute('class', 'vs-tooltip icon-info');
					var iElement = document.createElement('i');
					iElement.setAttribute('class', 'icon-small icon-info-solid');
					var spanElement = document.createElement('span');
					spanElement.setAttribute('class', 'vs-tooltiptext-left');
					spanElement.setAttribute('style', 'width:max-content');
					var smallElement = document.createElement('small');
					smallElement.setAttribute('class', 'vs-baseline-regular-black');
					smallElement.textContent = "Column Name : ";
					var innerSpanElement = document.createElement('span');
					innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
					innerSpanElement.textContent = val;
					smallElement.appendChild(innerSpanElement)
					spanElement.appendChild(smallElement);
					childElement.appendChild(iElement);
					childElement.appendChild(spanElement);
					parentElement.appendChild(mainElement);
					parentElement.appendChild(childElement);
					fragment.appendChild(parentElement);
				});
				$("#expression-data").append(fragment);
			}
		}).done(function() {
			$('#return-Search').keyup();
		});
}














//replace entities here.......

function createReturnFragment(selectedReturn) {
    if (isEdit != true) {
        $('#versionTimelineDiv').css('display', 'none');
        $('#versionTimeline').css('display', 'none');
    }
    $.ajax({
        url: 'getentitesForDQ.htm',
        type: 'POST',
        dataType: 'json',
        async: false,
        data: {
            effectiveDate: $("#date-picker-id").data('datepicker').getFormattedDate(displayDateFormat),
            isExpr: 'N'
        },
        success: function(response) {
        	$('#returnNameId').multiselect('destroy');
            appendOptionsCreateAndEdit(response.response, "returnNameId");
            var isExist = false;
            if (isEdit == true) {
                for (var i = 0; i < returnArray.length; i++) {
                    if (returnArray[i] == selectedReturn) {
                        isExist = true;
                    }
                }
                if (isExist == true) {
                    $('#returnNameId').append("<option>" + selectedReturn + "</option>");
                    $("#returnNameId").val(selectedReturn);
                } else {
                    if (($('#editValidationbtn').css('display') == 'block' && isEdit == "true") || ($('#editValidationbtn').css('display') == "none" && isEdit == "false")) {
                        $("#onChangeOfEffectiveDate").css("display", "flex");
                        $('#date-picker-id').datepicker('setDate', selectedDateForEdit);
                        $('#returnNameId').append("<option>" + selectedReturn + "</option>");
                        $("#returnNameId").val(selectedReturn);
                    }
                }
            } else {
                if (returnArray.length == 0) {
                    $("#onChangeOfEffectiveDate").css("display", "flex");
                }
            }
            $(".overlay").css("display", "none");
        }
    }).done(function() {
    });
}

function cancelPopup() {
    $("#onChangeOfEffectiveDate").css("display", "none");
}

function createReturnFragmentChange(changedVal) {
    $.ajax({
        url: 'getentityColumnForDQ.htm',
        type: 'POST',
        dataType: 'json',
        async: false,
        data: {
            effectiveDate:$("#date-picker-id").data('datepicker').getFormattedDate(displayDateFormat),
            entityName: changedVal,
            isExpr: 'N'
        },
        success: function(response) {
        	$('#returnNameSection').multiselect('destroy');
            appendOptionsCreateAndEdit(response.response, "returnNameSection");
        }
    }).done(function() {
    });
}

function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.toLowerCase().slice(1);
}