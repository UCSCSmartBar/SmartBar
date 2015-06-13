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

    $sql = " 
            SELECT 
				userPin
            FROM users 
            WHERE 
                drinkQueue = 1
        ";
    
   
   $result = $db->query($sql);

   if ($result) {
       // output data of each row
	   $queue_found = 1;
       while($row = $result->fetch()) {
           $queue_results .= $row["userPin"] . ",";
       }
   } else {
       echo "0 results";
   }
  
	    // Return status if the drink was found or not
    if ($queue_found) {
        $response["success"] = 1;
        $response["message"] = "$queue_results";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Drink Not Found";
        die(json_encode($response));
    }
	
	
	
   



?> 
