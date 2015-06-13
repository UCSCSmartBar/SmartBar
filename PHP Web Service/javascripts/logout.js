function logout() {
	var x = new XMLHttpRequest();
    x.open("GET","./logout.php",true);
    x.send();
    return false; 
}