<?php
require_once '../../../usr/local/bin/vendor/braintree/braintree_php/lib/Braintree.php';

Braintree_Configuration::environment('sandbox');
Braintree_Configuration::merchantId('p8dmdzsc4hn4794k');
Braintree_Configuration::publicKey('thwvhysnh3jb7jzt');
Braintree_Configuration::privateKey('ae2e17373a23d9ad87524ca26250ef52');

$clientToken = Braintree_ClientToken::generate(array("customerId" => $_SESSION['user_braintree']));

session_start();
if(isset($_GET['logout'])) {
    $_SESSION = array();
	session_destroy();
	header('Location: index.php');
	exit();
}
?>
<!DOCTYPE html>
<!-- Login from library: https://github.com/panique/php-login-minimal -->
<!--[if lt IE 7]> <html class="no-js lt-ie9 lt-ie8 lt-ie7" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js lt-ie9 lt-ie8" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js lt-ie9" lang="en"> <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js" lang="en">
<!--<![endif]-->
<head>
<meta charset="utf-8"/>
<!-- Set the viewport width to device width for mobile -->
<meta name="viewport" content="width=device-width"/>
<title>SmartBar Project</title>
<!-- CSS Files-->
<link rel="stylesheet" href="./stylesheets/style.css">
<link rel="stylesheet" href="./stylesheets/homepage.css"><!-- homepage stylesheet -->
<link rel="stylesheet" href="./stylesheets/skins/lilac.css"><!-- skin color -->
<link rel="stylesheet" href="./stylesheets/responsive.css">
<link rel="shortcut icon" href="favicon.ico">
<!-- IE Fix for HTML5 Tags -->
<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
  <![endif]--> 
<script src="https://js.braintreegateway.com/v2/braintree.js"></script> 
<script>
  braintree.setup("<?php echo $clientToken; ?>", "custom", {id: "checkout"});
</script>
</head>
<body>
<!-- HIDDEN PANEL 
================================================== -->
<div id="panel">
	<div class="row">
		<div class="twelve columns">
			<img src="images/info.png" class="pics" alt="info">
			<div class="infotext">
				Welcome to Smartbar, solutions for your autonomous drinking needs.
			</div>
		</div>
	</div>
</div>
<p class="slide">
	<a href="#" class="btn-slide"></a>
</p>
<!-- HEADER
================================================== -->
<div class="row">	
		<div class="four columns">
			<div class="logo">
				<img style="margin-left:-70px;margin-top:-25px" src="images/logo.png" width="60px" height="120px" />
				<a href="index.php"><h4><span style="color:#233A63">SmartBar</span></h4></a>
				
			</div>
		</div>
		<div class="eight columns noleftmarg">		
			<nav id="nav-wrap">
				<ul class="nav-bar sf-menu">				
									
					
					<?php
					if (isset($_SESSION['user_login_status']) AND $_SESSION['user_login_status'] == 1){
						include("includes/loginMenu.php");
					} else {
						include("includes/noLoginMenu.php");
					}
					?>
				</ul>
			</nav>
		</div>	
</div>
<div class="clear">
</div>
