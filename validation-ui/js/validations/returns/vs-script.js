/* Latest Version : 1.05.02 */
/* Author : AP|KP*/
/* Date : 05/07/2019 */

//Tab
function vsOpenTabContent(evt, tabContainerID) {
    var i, tabcontent, tablinks;

    //Make all button unselected
    tabcontent = document.getElementsByClassName("vs-tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    //Change all tabs display to None.
    tablinks = document.getElementsByClassName("vs-tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    //Set corresponding tabs display to block.
    document.getElementById(tabContainerID).style.display = "block";
    //Set corresponding button display to active.
    evt.currentTarget.className += " active";
}



function vsOpenTabTrigger(tab_container, tab_clicked) {
    var i, tabcontent, tablinks;

    var parentofSelectedButton = document.getElementById(tab_clicked).parentNode; // gives the parent DIV
    var childrenBtns = parentofSelectedButton.children;
    for (var i=0; i < childrenBtns.length; i++) {
        if (childrenBtns[i].tagName = "BUTTON") {
            childrenBtns[i].className = childrenBtns[i].className.replace(" active", "");
        }
    }

      document.getElementById(tab_clicked).classList.add("active");

	if(document.getElementById(tab_container)!=null){
      var parentofSelectedTab = document.getElementById(tab_container).parentNode; // gives the parent DIV
      var children = parentofSelectedTab.children;
      for (var i=1; i < children.length; i++) {
          if (children[i].tagName = "DIV") {
              children[i].style.display = "none";
              // break;
          }
      }

      document.getElementById(tab_container).style.display = "block";
    }

}

//Accordion
/*var vs_acc = document.getElementsByClassName("vs-accordion");
var i;
for (i = 0; i < vs_acc.length; i++) {
  vs_acc[i].addEventListener("click", function() {
    this.classList.toggle("vs-active");

    var parent = this.parentElement;
    var panel = parent.nextElementSibling;
    if (panel.style.maxHeight){
      panel.style.maxHeight = null;
    } else {
      panel.style.maxHeight = panel.scrollHeight + "px";
    }
  });
}*/
function vsTriggerAccordionHeader(contextElem,ind){
	contextElem.children[0].classList.toggle("vs-active");
  contextElem.classList.toggle("vs-header-active");
  if(ind=='text-overflow'){
    if(contextElem.children[0].classList.contains("vs-active")){
      contextElem.children[0].children[0].classList.remove("icon-add");
      contextElem.children[0].children[0].classList.add("icon-cancel-minus");
    }
    else{
      contextElem.children[0].children[0].classList.remove("icon-cancel-minus");
      contextElem.children[0].children[0].classList.add("icon-add");
    }
  }
	var panel =contextElem.nextElementSibling;
	if (panel.style.maxHeight){
      panel.style.maxHeight = null;
    } else {
      panel.style.maxHeight = panel.scrollHeight + "px";
    }
}

//Search Box
function vs_search_list(trigger_input,target_area) {
    var input, filter, ul, li, a, i, txtValue;
    input = document.getElementById(trigger_input);
    filter = input.value.toUpperCase();

    var my_no_data = target_area + "_no_data"
    var no_data_content = '<div class=\"vs-banner-message\" id=' + my_no_data + '><div><i class=\"icon-large icon-info-outline\"></i></div><div><h4 class=\"vs-h4-light-secondary vs-txt-aln-center\">No result found please try again</h4></div></div>';

    var list_occurence = 0;
    ul = document.getElementById(target_area);
    li = ul.getElementsByTagName("li");
    for (i = 0; i < li.length; i++) {
        a = li[i].getElementsByTagName("a")[0];
        txtValue = a.textContent || a.innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1)
        {
            li[i].style.display = "";
        } else {
          li[i].style.display = "none";
          list_occurence++;
        }
    }
    if(list_occurence == li.length){
      if(document.getElementById(my_no_data) == null){
        ul.insertAdjacentHTML("afterend",no_data_content);
      }
    }
    else{
      var elem = document.getElementById(my_no_data);
      if(elem!=null){
        elem.parentNode.removeChild(elem);
      }
    }
}

//Overlay Model
function vs_model_show(model_id) {
    var html = document.documentElement;
    console.log(html);
    html.style.overflow = "hidden";
    document.getElementById(model_id).style.display = "block";
}

function vs_model_hide(model_id) {
    var html = document.documentElement;
    html.style.overflow = "visible";
    document.getElementById(model_id).style.display = "none";
}

//Alert bar
var alrt_close_btn = document.getElementsByClassName("vs-alert-closebtn");
var alert_box_count;

for (alert_box_count = 0; alert_box_count < alrt_close_btn.length; alert_box_count++) {
    alrt_close_btn[alert_box_count].onclick = function(){
        var parent_div = this.parentElement;
        var grand_parent_div = parent_div.parentElement;
        // grand_parent_div.style.opacity = "0";
        setTimeout(function(){ grand_parent_div.style.display = "none"; }, 600);
    }
}

function close_alert(alert_id){
  if(alert_id){
    var alert_box = alert_id;
    var alert_box_parent = alert_box.parentElement;
    alert_box_parent.removeChild(alert_box);
  }
}

/*
Function: To call alert message of type success and info.
Paramenters:
1. parentid - Will be the id of parent where alert box needs to be prepened with exisiting textContent
2. alertType - 1 for Success message and 0 for Error message
3. message - Message for the alert box
*/

function generate_alert(parentId,alertType,message){
  // Get parent id
  var parent_div=document.getElementById(parentId);
  // Generate a random id to alert box with alrt_ prefixed
  var alertId=Math.floor((Math.random() * 100) + Math.random());
    if(alertType == 1){
        // Append success alert box from top to this parent
        parent_div.innerHTML = '<div class="vs-alert success vs-top-mgn-8 vs-left-mgn-8 vs-right-mgn-8" id="alert_'+alertId+'"><div><strong>Success!</strong>'+ message+'</div><div><span class="vs-alert-closebtn"><i class="icon-medium icon-close" onclick="close_alert(alert_' + alertId + ')"></i></span></div></div>' +parent_div.innerHTML;
        //Set Timer for this alert box
        setTimeout(function(){ document.getElementById("alert_"+alertId+"").style.display = "none"; },5000);
    }
    else if (alertType == 2) {
      parent_div.innerHTML = '<div class="vs-alert error vs-top-mgn-8 vs-left-mgn-8 vs-right-mgn-8" id="alert_'+alertId+'"><div><strong>Error!</strong>'+ message+'</div><div><span class="vs-alert-closebtn"><i class="icon-medium icon-close" onclick="close_alert(alert_' + alertId + ')"></i></span></div></div>' +parent_div.innerHTML;

    }
    else{
        parent_div.innerHTML = '<div class="vs-alert info vs-top-mgn-8 vs-left-mgn-8 vs-right-mgn-8" id="alert_'+alertId+'"><div><strong>Info!</strong>'+ message+'</div><div><span class="vs-alert-closebtn"><i class="icon-medium icon-close" onclick="close_alert(alert_' + alertId + ')"></i></span></div></div>' +parent_div.innerHTML;
        setTimeout(function(){ document.getElementById("alert_"+alertId+"").style.display = "none"; },5000);
    }
}


//Mini loader
function show_mini_loader(loader_container){

  //Storing loader_container's original position in session
  var session_variable = "mini_load_" + loader_container;
  sessionStorage.SessionName = session_variable;

  //Get original position
  // var current_position = document.getElementById(loader_container).position;\

  //Check if the position is static or any other position type

if(document.getElementById(loader_container)){
	if(typeof document.getElementById(loader_container).position == 'undefined'){
    sessionStorage.setItem(session_variable,"");
  }
  else{
    sessionStorage.setItem("mini_load_" + loader_container,document.getElementById(loader_container).position);
  }
//Change original position to relative
  document.getElementById(loader_container).style.position = "relative";

//Condition to check if this parent already has a child div named "vs-small-spinner"
  var parent = document.getElementById(loader_container);
  for (var i = 0; i < parent.childNodes.length; i++) {
      if (parent.childNodes[i].className == "vs-small-spinner") {
          return;
      }
  }

//Add a new div named "vs-small-spinner" if parent doesnt have any.
  var div = document.createElement('div');
  div.className = 'vs-small-spinner';
  div.innerHTML ='<span>Loading please wait &#8230;</span>';
  document.getElementById(loader_container).appendChild(div);
}








}

function remove_mini_loader(loader_container){

  //Fetch the assiociated session variable name with original position value.
  var session_variable = "mini_load_" + loader_container;

  //Get original position
  var previous_position = sessionStorage.getItem(session_variable);

  //Remove loader div from this parent div
  var parent_div=document.getElementById(loader_container);
  var child_item = parent_div.getElementsByClassName("vs-small-spinner");
  var has_imm_loader_child = false;

  if (parent_div.childNodes.length < 0){
    return;
  }
  else
  {
    for (var i = 0; i < parent_div.childNodes.length; i++)
    {
      // Check if parent has any immediate child with name vs-small-spinner
      if (parent_div.childNodes[i].className == "vs-small-spinner")
      {
        has_imm_loader_child = true; // Has a valid immediate child with spinner
		var child_of_this_parent=parent_div.childNodes[i];
		parent_div.removeChild(child_of_this_parent);
        //parent_div.childNodes[i].remove();
        break;
      }
    }

    if(has_imm_loader_child == false)
    {
      // Shows this parent does not have any immediate child with class as vs-small-spinner
      return;
    }
  }

  //Change position to original position
  document.getElementById(loader_container).style.position = previous_position;

  //Remove previous stored session variable.
  sessionStorage.removeItem(session_variable);

}

function vs_text_tooltip(txt_value,txt_id) {
  document.getElementById(txt_id).setAttribute('title',txt_value);
}


//Load below after DOM gets loaded
document.addEventListener("DOMContentLoaded", function(event) {

	const el = document.getElementById('vs-resizable');
	const handle = document.getElementById('vs-handle-horizontal');





	let startX, startWidth;
	if(el && handle ){
	handle.addEventListener('mousedown', setup, false)
}
	function setup(event){
	  startY = event.clientY;//Shows where was the click coordinate
	  startHeight = parseInt(window.getComputedStyle(el).height, 10); //Returns integer


	  document.documentElement.addEventListener('mousemove', drag, false);
		document.documentElement.addEventListener('mouseup', destroy, false);
	}

function drag(event) {
		let setHeight=$("#vs-resizable").height()-$("#stretchOut-grid-container .vs-gc-stretch-out").height()-$("#vs-section-dataSide").height()-100;
		$($(".mainGridForResize").find(".k-grid-content")[0]).height(setHeight);
		//$("#grid .k-grid-content").height(setHeight);
		$("#setProxyHeight").height(setHeight+70);
		if (event.clientY > 100 && event.clientY < window.innerHeight - 40){
			el.style.height = (startHeight - event.clientY + startY) + 'px';
		}

	}

	function destroy(e) {

		document.documentElement.removeEventListener('mousemove', drag, false);
		document.documentElement.removeEventListener('mouseup', destroy, false);
	}

});

function dragElement(elmnt) {
  var pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
  if (document.getElementById(elmnt.id + "header")) {
    /* if present, the header is where you move the DIV from:*/
    document.getElementById(elmnt.id + "header").onmousedown = dragMouseDown;
  } else {
    /* otherwise, move the DIV from anywhere inside the DIV:*/
    elmnt.onmousedown = dragMouseDown;
  }

  function dragMouseDown(e) {
    e = e || window.event;
    e.preventDefault();
    // get the mouse cursor position at startup:
    pos3 = e.clientX;
    pos4 = e.clientY;
    document.onmouseup = closeDragElement;
    // call a function whenever the cursor moves:
                document.onmousemove = elementDrag;
  }

  function elementDrag(e) {
	document.getElementById("context-idheader").style.cursor="grabbing";
    e = e || window.event;
    e.preventDefault();
    // calculate the new cursor position:
    pos1 = pos3 - e.clientX;
    pos2 = pos4 - e.clientY;
    pos3 = e.clientX;
    pos4 = e.clientY;
    // set the element's new position:

                if((elmnt.offsetTop >=0) &&(elmnt.offsetLeft >=  0) && ((elmnt.offsetLeft+elmnt.offsetWidth) <=  window.innerWidth) && ((elmnt.offsetTop+elmnt.offsetHeight) <=  window.innerHeight)){
                                elmnt.style.top = (elmnt.offsetTop - pos2) + "px";
                                elmnt.style.left = (elmnt.offsetLeft - pos1) + "px";
                }
                if((elmnt.offsetTop<0)){
                                elmnt.style.top = (0) + "px";
                }
                if((elmnt.offsetLeft <  0)){
                                elmnt.style.left = (0) + "px";
                }
                if((elmnt.offsetTop+elmnt.offsetHeight) >=  window.innerHeight){
                                elmnt.style.top = (window.innerHeight-elmnt.offsetHeight) + "px";
                }
                if((elmnt.offsetLeft+elmnt.offsetWidth) >=  window.innerWidth){
                                elmnt.style.left = (window.innerWidth-elmnt.offsetWidth) + "px";
                }


  }

  function closeDragElement() {

    /* stop moving when mouse button is released:*/
    document.getElementById("context-idheader").style.cursor="grab";
	document.onmouseup = null;
    document.onmousemove = null;
  }
}


/*Rafi code Starts here*/
/*function vs_trigger_button(button_id) {
  var element = document.getElementById(button_id);
  if (element.classList.contains("vs-primary-one-outline")) {
      element.classList.remove("vs-primary-one-outline");
      element.classList.add("vs-primary-one");
  } else if (element.classList.contains("vs-primary-one")) {
      element.classList.remove("vs-primary-one");
      element.classList.add("vs-primary-one-outline");
  }
}*/

function openTab(evt, tabName) {
  var i, tabcontent, tablinks;
  tabcontent = document.getElementsByClassName("vs-tab-content");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }
  tablinks = document.getElementsByClassName("vs-tab-links");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  document.getElementById(tabName).style.display = "block";
  evt.currentTarget.className += " active";
}

// Get the element with id="defaultOpen" and click on it
// document.getElementById("defaultOpen").click();

//switch on off
function vs_switch_state(context){
  if(context.classList.contains("switchOn")){
    context.classList.remove("switchOn");
    context.classList.add("switchOff");
  }
  else{
    context.classList.remove("switchOff");
    context.classList.add("switchOn");
  }
}

//close alert
function closeSideAlert(alert_id){
  if(alert_id!="" || alert_id!=undefined){
    var generic_ele=document.getElementById(alert_id);
    generic_ele.parentElement.removeChild(generic_ele);
  }
}

//Multi alert collapse/expose
function vsToggleAlertPanel(hideId,showId,indicator){
  if(indicator=="show"){
    document.getElementById(hideId).style.display = "none";
    document.getElementById(showId).style.display = "block";
    }
  else{
    document.getElementById(hideId).style.display = "none"
    document.getElementById(showId).style.display = "flex"
  }
}

//Collapse and expose for lay left side bars buttons.
function vs_show_aside(context,ind){
  var className="lay-left-sidebar-expose";
  for(var i=0;i<context.parentElement.children.length;i++){
    context.parentElement.children[i].style.background="#FAFAFA";
  }
  if(ind=="big"){
    className="lay-left-big-sidebar-expose";
  }
  var parentEle=context.parentElement.parentElement.parentElement;
  var unique_name=context.getAttribute("side_content_trigger");
  var child_aside=parentEle.childNodes[3];
  if(parentEle.classList.contains("lay-left-sidebar") && child_aside.getAttribute("side_content_name")==""){
    child_aside.setAttribute("side_content_name",unique_name);
    parentEle.classList.remove("lay-left-sidebar");
    parentEle.classList.add(className);
    context.style.background="#E6E9EB";
  }
  else if(parentEle.classList.contains(className) && child_aside.getAttribute("side_content_name")==unique_name){
    parentEle.classList.remove(className);
    parentEle.classList.add("lay-left-sidebar");
    child_aside.setAttribute("side_content_name","");
  }
  else{
    child_aside.setAttribute("side_content_name",unique_name);
	context.style.background="#E6E9EB";
  }
}

//alert timeout
function successTimeOut(alert_id,indicator){
    if(indicator=="remove"){
      setTimeout(function(){ closeSideAlert(alert_id,indicator); },5000);
    }
    else {
      setTimeout(function(){ var generic_ele=document.getElementById(alert_id);
      generic_ele.parentElement.removeChild(generic_ele); },5000);
    }
}

//change filter
function indicateIcon(idOfIcon,toggleState){
  if(toggleState=="Y"){
    var element=document.getElementById(idOfIcon);
    element.innerHTML="&#8226;";
    element.style.color="#FD462A";
  }
  else{
    var element=document.getElementById(idOfIcon);
    element.innerHTML="";
    element.style.color="#54585A";
  }
}

//breadcrumb

function vs_createBreadcrumb(value,arrayOfObjects,id){
  var className="";
  var iconFont="";
  var arrayForBreadCrumb=[value,arrayOfObjects];

  switch(arrayForBreadCrumb[0].toString().toLowerCase()){
    case 'big':
      className="vs-breadcrumb-big";
      iconFont="icon-small"
      break;
    default:
      className="vs-breadcrumb";
      iconFont="";
  }

  var unorderedList=document.createElement('ul');
  unorderedList.classList.add(className);

	if(id!=undefined && id!=""){
		document.getElementById(id).appendChild(unorderedList);
	}
	else{
		document.getElementById("vs-breadcrumb-holder").appendChild(unorderedList);
	}
  arrayForBreadCrumb[1].reverse();

  if(arrayForBreadCrumb[1].length==1){
    var list=vs_breadcrumbHelper(arrayForBreadCrumb[1][0],"one");
    unorderedList.appendChild(list);
    arrayForBreadCrumb[1]=[];
  }
  else if(arrayForBreadCrumb[1].length >= 2){
     if(arrayForBreadCrumb[1].length > 2){
      unorderedList.innerHTML = '<li><div class="vs-button-popover vs-breadcrumb-popup"><button class="vs-trans-button">'+
      '<i class="icon-more-alt '+iconFont+'"></i></button>'+
      '<div><ul class="buttonPopOver"></ul></div></div></li>'
    }
    var list=vs_breadcrumbHelper(arrayForBreadCrumb[1][1],"two");
    unorderedList.appendChild(list);

    var list=vs_breadcrumbHelper(arrayForBreadCrumb[1][0],"one");
    unorderedList.appendChild(list);

    arrayForBreadCrumb[1].splice(0,2);
  }
  if(arrayForBreadCrumb[1].length!=0){
    arrayForBreadCrumb[1].forEach(function(value,key){
      var list=document.createElement("Li");
      if(value.url!=undefined && value.url!=""){
        var anchor=document.createElement('a');
        anchor.setAttribute("href",value.url);
        anchor.appendChild(document.createTextNode(value.page));
        list.appendChild(anchor);
      }
      else{
        list.appendChild(document.createTextNode(value.page));
      }
      breadcrumbPropsHelper(list,value);
      document.getElementsByClassName("buttonPopOver")[0].appendChild(list);
    });
    document.getElementsByClassName("buttonPopOver")[0].classList.remove('buttonPopOver');
  }
}

function vs_breadcrumbHelper(value,ind){
  switch(ind){
    case "one":
      var list=document.createElement("Li");
      breadcrumbPropsHelper(list,value);
      list.appendChild(document.createTextNode(value.page));
      return list;
    case "two":
      var list=document.createElement("Li");
      if(value.url!=undefined && value.url!=""){
        var anchor=document.createElement('a');
        anchor.setAttribute("href",value.url);
        anchor.appendChild(document.createTextNode(value.page));
        list.appendChild(anchor);
      }
      else{
        list.appendChild(document.createTextNode(value.page));
      }
      breadcrumbPropsHelper(list,value);
      list.classList.add("vs-noContentLi");
      return list;
  }
}

function breadcrumbPropsHelper(list,value){
  if(value["props"] != undefined && value["props"] != null ){
    var tempValue = value["props"];
    Object.keys(tempValue).forEach(function(key){
      list.setAttribute(key,tempValue[key]);
    });
  }
}

//display or hideId
function vs_generic_hideshow(array_of_id,displayProperty){
  for(var i=0;i<array_of_id.length;i++){
    document.getElementById(array_of_id[i]).style.display = displayProperty;
  }
}

//Collapse fade left panel
function vs_collapse(parentId){
  var element = document.getElementById(parentId);
  if(element.getAttribute("isFilterShown") == "N"){
      element.classList.add("vs-collapse-leftPanel");
      element.setAttribute("isFilterShown","Y");
      setTimeout(function(){
        element.classList.remove("vs-collapse-leftPanel");
        element.classList.add("vs-collapse-panel-left-pos");
      },600);
  }
  else{
    element.classList.add("vs-show-leftPanel");
    element.setAttribute("isFilterShown","N");
    setTimeout(function(){
      element.classList.remove("vs-show-leftPanel");
      element.classList.remove("vs-collapse-panel-left-pos");
    },600);
  }
}

//disable third party components
function diableThirdPartyComp(id, value) {
  if (value == "tag-selector") {
    document.getElementById(id).querySelector(".VS-Tag-Input-Border").classList.add("vs-disable-tag-selector");
    document.getElementById(id).querySelector("input").classList.add("vs-disable-tag-selector");
    if(document.getElementById(id).querySelector(".VS-AutoCompleteItem-New")!=null)
      document.getElementById(id).querySelector(".VS-AutoCompleteItem-New").classList.add("vs-disable-tag-selector");
  }
}

/*function addTagSelectorDataOptions(id) {
  let divfortagselector4 = document.getElementById(id);
  let tagSelector = document.createElement("tag-selector");
  let dataOptions = {"placeholder": "Type to add artifacts","canRemoveAll": false,"maxItemCounter": 1};
  tagSelector.setAttribute('data-options', JSON.stringify(dataOptions));
  tagSelector.setAttribute('id', "responderOrgTagSelector1");
  divfortagselector4.appendChild(tagSelector);
  addTagSelector(tagSelector);
} */
