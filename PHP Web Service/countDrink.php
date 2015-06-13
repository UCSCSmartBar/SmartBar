<?php
/*****************************************************************
* File name: countDrink.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/13/15
* Description: Adds a drink to the users row
******************************************************************/
//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {

	if (empty($_POST['pin']) ) {         
        $response["success"] = 0;
        $response["message"] = "Please Enter a pin.";

        die(json_encode($response));
    }
  
    $query = " 
		SELECT
			drinkTotal
		FROM users
		WHERE 
			(userPin = :pin)
    ";
    
    $query_params = array(
        ':pin' => $_POST['pin']
    );
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
        
    }
    //fetching all the rows from the query
    $row = $stmt->fetch();
	
    if ($row) {
		$drinksTot = $row['drinkTotal'];
    }
	
	$query1 = " 
		UPDATE
			users
		SET drinkTotal = :drinkstot
		WHERE 
			(userPin = :pin)
    ";
    
    $query_params1 = array(
        ':pin' => $_POST['pin'],
		':drinkstot' => ($drinksTot + 1)
    );
    
    try {
        $stmt1   = $db->prepare($query1);
        $result1 = $stmt1->execute($query_params1);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
        
    }
	

    $drink_found = 1;
    $dispDrink = $drinksTot + 1;
	

	// Return status if the BAC was found or not
    if ($drink_found) {
        $response["success"] = 1;
        $response["message"] = "$dispDrink";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Drink Not Found";
        die(json_encode($response));
    }
} else {
?>
	<h1>Count Drink</h1> 
	<form action="countDrink.php" method="post"> 
	    Pin:<br /> 
	    <input type="text" name="pin" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Count Drink" /> 
	</form>
<?php
	}
?>