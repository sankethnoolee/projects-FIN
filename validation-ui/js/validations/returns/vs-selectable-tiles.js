function vs_tile_select(ele) {
            if (ele.parentElement.classList.contains("vs-card-single")) {
                var previousSelectedElement = ele.parentNode.getElementsByClassName('tile-active');
                if (previousSelectedElement.length > 0) {

                    var unselectElement = previousSelectedElement[0].children[0].children[0];
                    unselectElement.removeAttribute('class', 'icon-accepted-round');
                    unselectElement.setAttribute('class', 'icon-chat-delivered');
                    previousSelectedElement[0].removeAttribute("style", "border-color:#069ABC");
                    var SelectedElement = previousSelectedElement[0] == ele;
                    if (SelectedElement == false) {
                        previousSelectedElement[0].classList.remove("tile-active");;
                    }
                }

            }

            if (!ele.classList.contains("tile-active")) {
                ele.setAttribute("style", "border-color:#069ABC");
                ele.getElementsByClassName('vs-card-icon')[0].getElementsByTagName('i')[0].setAttribute(
                    'class', 'icon-accepted-round');
            } else {
                ele.removeAttribute("style", "border-color:#069ABC");
                ele.getElementsByClassName('vs-card-icon')[0].getElementsByTagName('i')[0].setAttribute(
                    'class', 'icon-chat-delivered');
            }
            ele.classList.toggle("tile-active");

}

