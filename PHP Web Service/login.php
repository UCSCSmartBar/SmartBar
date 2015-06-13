<?php include('header.php'); 
?>

<!-- SUBHEADER 
================================================== -->
<div id="subheader">
	<div class="row">
		<div class="twelve columns">
			<p class="left">
				<span style="font-size:19pt;font-weight:bold"> Login </span>
			</p>
			<p class="right">
				 <span style="font-size:20pt;font-weight:bold">  </span>
			</p>
		</div>
	</div>
</div>
<div class="hr">
</div>
<div align="center">
<?php

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
require_once("classes/login.php");


// create a login object. when this object is created, it will do all login/logout stuff automatically
// so this single line handles the entire login process. in consequence, you can simply ...
$login = new Login();
// ... ask if we are logged in here:
if ($login->isUserLoggedIn() == true) {
    // the user is logged in. you can do whatever you want here.
    // for demonstration purposes, we simply show the "you are logged in" view.
	echo 'You are now logged in, redirecting...';
    echo '<meta http-equiv="refresh" content="0; URL=https://smartbarproject.com/">';
	exit();
} else {
	if(isset($_GET['newuser'])) {
		echo "Your account has been created successfully. You can now log in.";
	}
    // the user is not logged in. you can do whatever you want here.
    // for demonstration purposes, we simply show the "you are not logged in" view.
    include("includes/not_logged_in.php");
} ?>
</div>
<?php include('footer.php');
?>