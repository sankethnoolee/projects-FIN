'use strict';
class InputLabel extends HTMLElement{
	
	constructor(){
		super();
			
		
		
		const shadow = this.attachShadow({mode:'closed'});
		const div = document.createElement('div');
		div.setAttribute('class','vfs-input-label');
		
		const labelName = this.getAttribute('label');
		div.textContent = labelName;
		
		const vfsInputLabelStyle = document.createElement('style');
		vfsInputLabelStyle.textContent = `
			.vfs-input-label{
				font-size:12px;
				color:rgb(119, 119, 119);
				width:100%;
				
			}
		
		`;
		
		shadow.appendChild(vfsInputLabelStyle);
		shadow.appendChild(div);
		this.labelContainer = div;
	}
	
	set setLabel(newVal){
		this.labelContainer.textContent = newVal;
	}
}

class VfsInput extends HTMLElement{
	
	constructor(){
		super();
		const shadow = this.attachShadow({mode:'closed'});
		const inputLabel = document.createElement('input-label');
		inputLabel.setAttribute('label',this.getAttribute('label'));
		inputLabel.setLabel = this.getAttribute('label');
		
		
		
		const input = document.createElement('input');
		input.setAttribute('class','vfs-input');
		input.setAttribute('placeholder',this.getAttribute('placeholder'));
		
		if(this.getAttribute('label')){
			input.setAttribute("type",this.getAttribute('type'));
		}
		
		const vfsInputLabelStyle = document.createElement('style');
		vfsInputLabelStyle.textContent = `
			.vfs-input{
				font-size:12px;
				color:black;
				width:100%;
				border : none;
				border-bottom:1px solid rgb(119, 119, 119);
				padding:8px;
			}
		
		`;
		
		shadow.appendChild(vfsInputLabelStyle);
		shadow.appendChild(inputLabel);
		shadow.appendChild(input);
		
		// Cache the value of the inputNode
		this.inputNode = input;
	}
	
	get inputValue () {
		return this.inputNode.value
	}

	set inputValue (newValue) {
		this.inputNode.value = newValue
	}
}



customElements.define('input-label',InputLabel);
customElements.define('vfs-input',VfsInput);


function MyCtor(element, data) {
    this.data = data;
    this.element = element;
    element.value = data;
    element.addEventListener("change", this, false);
}

MyCtor.prototype.handleEvent = function (event) {
    switch (event.type) {
        case "change":
            this.change(this.element.value);
    }
};


MyCtor.prototype.change = function (value){
    this.data = value;
    this.element.value = value;
};

function enterVal(value) {
    this.data = value;
    this.element.value = value;
};


var obj = new MyCtor(document.getElementById("test"), "initial");

