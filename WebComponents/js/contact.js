class ContactList extends HTMLElement{
	
	constructor(data){
		super();
		const _data = data;
		this.innerHTML = `<div>${_data.name}</div>`;
		
		/*const input = document.createElement('input');
		input.setAttribute('class','vfs-input');*/
	}
	
	
}

customElements.define('contact-list',ContactList);