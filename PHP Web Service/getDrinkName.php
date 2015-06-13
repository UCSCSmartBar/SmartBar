<?php
/*****************************************************************
* File name: getDrinkName.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/13/15
* Description: This function returns the drinks name based on the
* recipe that is recieved.
******************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {

	if (empty($_POST['recipe']) ) {     
        
        $response["success"] = 0;
        $response["message"] = "Please Enter a Recipe.";

        die(json_encode($response));
    }
  
     $query = " 
		SELECT 
			drinkName,
			recipe
		FROM drinks
		WHERE 
			(recipe = :recipe)
    ";
    
    $query_params = array(
        ':recipe' => $_POST['recipe']

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
        //compare the two recipes
        if ($_POST['recipe'] === $row['recipe'] ) {
			$drink = $row['drinkName'];
            $drink_found = 1;
        }
    }

	// Return status if the BAC was found or not
    if ($drink_found) {
        $response["success"] = 1;
        $response["message"] = "$drink";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Drink Not Found";
        die(json_encode($response));
    }
} else {
?>
	<h1>Get Drink Name</h1> 
	<form action="getDrinkName.php" method="post"> 
	    Recipe:<br /> 
	    <input type="text" name="recipe" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Get Drink" /> 
	</form>
<?php
	}
?>