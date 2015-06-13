<?php
/*****************************************************************
* File name: postBrainID.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 5/4/15
* Description: Grabs the nonce from the mobile app and processes the
* payment
******************************************************************/
if (!empty($_POST)) {

require_once '../../../usr/local/bin/vendor/braintree/braintree_php/lib/Braintree.php';

Braintree_Configuration::environment('sandbox');
Braintree_Configuration::merchantId('p8dmdzsc4hn4794k');
Braintree_Configuration::publicKey('thwvhysnh3jb7jzt');
Braintree_Configuration::privateKey('ae2e17373a23d9ad87524ca26250ef52');

$id = $_POST["customerID"];
$amount = $_POST["amount"];

$clientToken = Braintree_ClientToken::generate(array("customerId" => $id));

$result = Braintree_Transaction::sale(array(
  'amount' => $amount,
  'customerId' => $id
));


if ($result->success){
	$response["success"] = 1;
    $response["message"] = "Payment Successful!";
    die(json_encode($response));
} else {
	$response["success"] = 0;
    $response["message"] = "Payment Failed, please try again.";
    die(json_encode($response));
}

}  else {

?>

<form  action="postBrainID.php" method="post">
	<input name="customerID" value=""><br />
	<input name="amount" value=""><br />
	
	<input type="submit" id="submit" value="Submit">
</form>

<?php } ?>