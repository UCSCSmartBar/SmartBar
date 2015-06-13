
<?php
if (!empty($_POST)) {
	// checking for minimum PHP version
	if (version_compare(PHP_VERSION, '5.3.7', '<')) {
		exit("Sorry, Simple PHP Login does not run on a PHP version smaller than 5.3.7 !");
	} else if (version_compare(PHP_VERSION, '5.5.0', '<')) {
		// if you are using PHP 5.3 or PHP 5.4 you have to include the password_api_compatibility_library.php
		// (this library adds the PHP 5.5 password hashing functions to older versions of PHP)
		require_once("classes/password_compatibility_library.php");
	}
	// include the configs / constants for the database connection
	require_once("db.php");
	// load the login class
	require_once("classes/islogin.php");


	// create a login object. when this object is created, it will do all login/logout stuff automatically
	// so this single line handles the entire login process. in consequence, you can simply ...
	//session_start();
	$login = new Login();
	// ... ask if we are logged in here:



	if($login->isUserLoggedIn() == true){
		$response["success"] = 1;
		$response["message"] = "Login Successful for " . $_SESSION['user_name'];
		echo json_encode($response);
	} else {
	    $response["success"] = 0;
		$response["message"] = "Login Failed.";
		die(json_encode($response));	
	}

} else {
?>
<form method="post" action="isLogged.php" name="loginform">

    <label for="login_input_username">Username</label>
    <input id="login_input_username" class="login_input" style="width:200px;border:1px solid #ccc" type="text" name="user_name" />
	<br /><br />
    <label for="login_input_password">Password</label>
    <input id="login_input_password" class="login_input" style="width:200px;border:1px solid #ccc" type="password" name="user_password" />
	<br /><br />
    <input type="submit"  name="login" value="Log in" />

</form>

<?php 
	}
?>
