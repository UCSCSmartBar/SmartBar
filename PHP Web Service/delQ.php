<?php
/*****************************************************************
* File name: getQ.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 2/27/15
* Description: This function returns the drinks in the queue by 
* returning the phone numbers of the users who have a drink.
******************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");
if (!empty($_POST)) {
    $query = "UPDATE users SET drinkQueue = 0 WHERE userPin = :pin"; 

   //updatetokens with the data to avoid SQL injection:
    $query_params = array(
        ':pin' => $_POST['pin']
    );
    
    //run our query, and create the drink
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
		$found = 1;
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
    }
  
	    // Return status if the drink was found or not
    if ($found) {
        $response["success"] = 1;
        $response["message"] = "Drink Removed!";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Drink Not Found";
        die(json_encode($response));
    }
	
	
	
   


} else {
?> 
	<h1>Delete Drink</h1> 
	<form action="delQ.php" method="post"> 
	    Pin:<br /> 
	    <input type="text" name="pin" value="" /> 
        <br /><br /> 
  
	    <input type="submit" value="Delete drink" /> 
	</form>
	
		<?php
}

?>
