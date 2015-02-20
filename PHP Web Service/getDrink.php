<?php
/*****************************************************************
* File name: getDrink.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 2/12/15
* Description: This function uses the pin in the POST field and searches
* the users table for the user with the matching pin. Then it looks at their 
* drinks column for that user and returns that drink recipe from the
* drinks table in a JSON response.
******************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");

if (!empty($_POST)) {
    //gets drink info based of a name.
    $query = " 
            SELECT 
                userId,
                drinks,
				userPin
            FROM users 
            WHERE 
                userPin = :userpin
        ";
    
    $query_params = array(
        ':userpin' => $_POST['pin']
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
    
    $validated_info = false;
    
    //fetching all the rows from the query
    $row = $stmt->fetch();
    if ($row) {
        //compare the two pins
        if ($_POST['pin'] === $row['userPin']) {
			$drink = $row['drinks'];
            $drink_found = 1;
        }
    }
	
	//Get the drink recipe
	$query2 = " 
            SELECT 
				drinkId,
				drinkName,
				recipe
            FROM drinks 
            WHERE 
                drinkName = :userdrink
        ";
	$query2_params = array(
        ':userdrink' => $drink
    );
    
   $stmt2   = $db->prepare($query2);
   $result2 = $stmt2->execute($query2_params);
   $row2 = $stmt2->fetch();
    if ($row2) {
	//Update tokens to prevent SQL injection
        if ($drink === $row2['drinkName']) {
			$recipe = $row2['recipe'];
			$username = $row2['userName'];
        }
    }
	
	
    // Return status if the drink was found or not
    if ($drink_found) {
        $response["success"] = 1;
        $response["message"] = "$recipe";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Drink Not Found";
        die(json_encode($response));
    }
} else {
?>
		<h1>Get your Drink</h1> 
		<form action="getDrink.php" method="post"> 
		    Pin Number:<br /> 
		    <input type="text" name="pin" placeholder="Pin" /> 
		    <br /><br /> 
		    <input type="submit" value="Get Drink" /> 
		</form> 
		<a href="register.php">Register</a>
	<?php
}

?> 
