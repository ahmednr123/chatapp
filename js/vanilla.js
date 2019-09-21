function $ (_element, _all){
	if(!_element)
        return document
        
    if (_all)
        return document.querySelectorAll(_element)
    else
        return document.querySelector(_element)
}

function $forEach (_element, _func){
	Array.prototype.forEach.call(document.querySelectorAll(_element), _func);
}

function $xhrRequest (_url, _onReady, _onProgress, _xhr){
	var xhr = _xhr || new XMLHttpRequest();
    xhr.addEventListener('progress', _onProgress)
    xhr.onreadystatechange = function(){
		if(xhr.readyState == 4 && xhr.status == 200){
			_onReady(xhr.responseText, _xhr);
		}
    }

	xhr.open("GET", _url, true);
	xhr.send();
}

function $xhrPost (_url, _data, _onReady, _xhr) {
    const xhr = _xhr || new XMLHttpRequest();

    xhr.addEventListener( 'load', function () {console.log('request succesful!')});
    xhr.open( 'POST', _url );
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhr.onreadystatechange = function () {
        if(xhr.readyState == 4){
			_onReady(xhr.responseText, _xhr);
		}
    }

    let params = jsonToParams(_data);
    console.log(`data sent: ${params}`);
    xhr.send( params );
}

function jsonToParams (json) {
    let str = ""
    
    for (let val in json) {
        str += val + "=" + json[val] + "&";
    }

    return str.slice(0, -1);
}