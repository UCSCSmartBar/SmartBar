<?php
/*****************************************************************
* File name: findCust.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 5/5/15
* Description: Finds a customer in our database from the phone 
* number.
******************************************************************/
if (!empty($_POST)) {

require_once '../../../../usr/local/bin/vendor/braintree/braintree_php/lib/Braintree.php';

Braintree_Configuration::environment('sandbox');
Braintree_Configuration::merchantId('p8dmdzsc4hn4794k');
Braintree_Configuration::publicKey('thwvhysnh3jb7jzt');
Braintree_Configuration::privateKey('ae2e17373a23d9ad87524ca26250ef52');

$phone = $_POST["phone"];

require("../config.inc.php");
$sql = "SELECT braintree FROM users WHERE userPin = ".$phone;
$result = $db->query($sql);
$row = $result->fetch();

$brainID = $row["braintree"];


if ($brainID == 0) {
	$response["success"] = 0;
    $response["message"] = "Customer braintree ID or phone number not found in our database.";
    die(json_encode($response));
}


if (empty($row) == false){
	$customer = Braintree_Customer::find($brainID);	
	$response["success"] = 1;
	$response["message"] = $customer->id;
	die(json_encode($response));
} 

}  else {

?>

<form  action="findCust.php" method="post">
	<input name="phone" value=""><br />
	<input type="submit" id="submit" value="Submit">
</form>

<?php } ?>