<?php


if (!empty($_POST)) {

require_once '../../../../usr/local/bin/vendor/braintree/braintree_php/lib/Braintree.php';
require_once("config.inc.php");

Braintree_Configuration::environment('sandbox');
Braintree_Configuration::merchantId('p8dmdzsc4hn4794k');
Braintree_Configuration::publicKey('thwvhysnh3jb7jzt');
Braintree_Configuration::privateKey('ae2e17373a23d9ad87524ca26250ef52');

echo "Processing payment...";
$nonce = $_POST["payment_method_nonce"];


if ($_SESSION['user_braintree'] == 0){
	$result = Braintree_Customer::create(array(
    'phone' => $_SESSION['user_pin'],
	'paymentMethodNonce' => $nonce
	));
	if($result->success){
		
		$query = "UPDATE users SET braintree = :id WHERE userPin = :phone";

		//updatetokens with the data to avoid SQL injection:
		$query_params = array(
			':phone' => $_SESSION['user_pin'],
			':id' => $result->customer->id

		);
		$stmt   = $db->prepare($query);
		$result2 = $stmt->execute($query_params);
		
		$result3 = Braintree_Transaction::sale(array(
		  'amount' => $_POST["price"],
		  'paymentMethodNonce' => $nonce,
		  'customerId' => $result->customer->id
		));
		$_SESSION['user_braintree'] = $result->customer->id;
	} else {
		echo '<meta http-equiv="refresh" content="0; URL=https://smartbarproject.com/index.php?payfail">';
	}
	
} else {
	$result4 = Braintree_Transaction::sale(array(
	  'amount' => $_POST["price"],
	  'customerId' => $_SESSION['user_braintree']
	));
}

if ($result4->success or $result3->success){
		echo '<meta http-equiv="refresh" content="0; URL=https://smartbarproject.com/index.php?paysuccess">';
} else {
		echo '<meta http-equiv="refresh" content="0; URL=https://smartbarproject.com/index.php?payfail">';
}

}  else {

if ($_SESSION['user_braintree'] == 0){


?>


<div style="text-align:left">
<form id="checkout" action="mySB.php#pay" method="post">
	<span class="infotitle">Card Name: </span>
	<input data-braintree-name="cardholder_name"  value="John Smith" style="width:200px;border:1px solid #ccc"><br />
	<span class="infotitle">Card #: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
	<input data-braintree-name="number" value="5105105105105100" style="width:200px;border:1px solid #ccc"><br />
	
	<span class="infotitle">Expiration: &nbsp;</span>
	<input data-braintree-name="expiration_date" value="5/12" style="width:200px;border:1px solid #ccc"><br />
	<span class="infotitle">Amount: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
	<input name="price" value="25" style="width:200px;border:1px solid #ccc"><br /><br />


	<input type="submit" id="submit" value="Pay" style="margin-left:18%">
</form>
</div>
<?php } else {


 ?>

<form id="checkout" action="mySB.php#pay" method="post">
	Amount:
	<input name="price" value="25"><br />


	<input type="submit" id="submit" value="Pay">
</form>




<?php
}} ?>