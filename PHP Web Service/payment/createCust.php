<?php
/*****************************************************************
* File name: createCust.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 5/5/15
* Description: Creates a customer on braintree based on the phone
* number in our database.
******************************************************************/
if (!empty($_POST)) {

require_once '../../../../usr/local/bin/vendor/braintree/braintree_php/lib/Braintree.php';


Braintree_Configuration::environment('sandbox');
Braintree_Configuration::merchantId('p8dmdzsc4hn4794k');
Braintree_Configuration::publicKey('thwvhysnh3jb7jzt');
Braintree_Configuration::privateKey('ae2e17373a23d9ad87524ca26250ef52');

$phone = $_POST["phone"];
$nonce = $_POST["nonce"];

$result = Braintree_Customer::create(array(
    'phone' => $phone,
	'paymentMethodNonce' => $nonce
));


if ($result->success){
	require("../config.inc.php");
	
	
	$sql = "SELECT userPin FROM users WHERE userPin = '$phone'";
    $result1 = $db->query($sql);
    $row = $result1->fetch();
    if (empty($row) == false) {
		
		$query = "UPDATE users SET braintree = :id WHERE userPin = :phone";

		//updatetokens with the data to avoid SQL injection:
		$query_params = array(
			':phone' => $phone,
			':id' => $result->customer->id
	
		);

		$stmt   = $db->prepare($query);
		$result2 = $stmt->execute($query_params);
        $response["success"] = 1;
        $response["message"] = 'Customer created for ' .$phone.'.'.$result->customer->id;
        die(json_encode($response));
		
    }else {
		$response["success"] = 0;
        $response["message"] = "Customer phone number not found.";
        die(json_encode($response));
	}
	

	
} else {
	$response["success"] = 0;
    $response["message"] = "Payment Failed, please try again.";
    die(json_encode($response));
}

}  else {


?>



<form  action="createCust.php" method="post">
	<input name="phone" value=""><br />
	<input name="nonce" value=""><br />
	<input type="submit" id="submit" value="Submit">
</form>

<?php } ?>