<?php 
/*****************************************************************
* File name: liquidLevels.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 5/4/15
* Description: Displays the client token for braintree in json format
******************************************************************/

	require_once '../../../usr/local/bin/vendor/braintree/braintree_php/lib/Braintree.php';

	Braintree_Configuration::environment('sandbox');
	Braintree_Configuration::merchantId('p8dmdzsc4hn4794k');
	Braintree_Configuration::publicKey('thwvhysnh3jb7jzt');
	Braintree_Configuration::privateKey('ae2e17373a23d9ad87524ca26250ef52');

	$clientToken = Braintree_ClientToken::generate(array("customerId" => $aCustomerId));

	$response["success"] = 1;
    $response["message"] = $clientToken;
    die(json_encode($response));
	

?>
