<?php 

if(!isset($_GET['token'])) {
	require_once '../../../usr/local/bin/vendor/braintree/braintree_php/lib/Braintree.php';

	Braintree_Configuration::environment('sandbox');
	Braintree_Configuration::merchantId('p8dmdzsc4hn4794k');
	Braintree_Configuration::publicKey('thwvhysnh3jb7jzt');
	Braintree_Configuration::privateKey('ae2e17373a23d9ad87524ca26250ef52');

	$clientToken = Braintree_ClientToken::generate(array("customerId" => $aCustomerId));
	$new_location = "http://smartbarproject.com/paytest.php?token=".$clientToken;

	header('Location: ' . $new_location);
}
?>
