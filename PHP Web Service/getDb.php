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
				*		
            FROM users 
            
        ";
    
   
   $result = $db->query($sql);


	   $queue_found = 1;
       while($row = $result->fetch()) {
           $drinks_results .= $row["iserId"] . "%";
		   $drinks_results .= $row["userName"] . "%";
		   $drinks_results .= $row["passWord"] . "%";
		   $drinks_results .= $row["userPin"] . "%";
		   $drinks_results .= $row["age"] . "%";
		   $drinks_results .= $row["weight"] . "%";
		   $drinks_results .= $row["sex"] . "%";
		   $drinks_results .= $row["drinks"] . "%";
		   $drinks_results .= $row["fingerprint"] . "%";
		   $drinks_results .= $row["BAC"] . "%";
		   $drinks_results .= $row["drinkQueue"] . "%";
		   $drinks_results .= $row["created"] . "%";
       }
       

  
	    // Return status if the drink was found or not
    if ($queue_found) {
        $response["success"] = 1;
        $response["message"] = "$drinks_results";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Drink Not Found";
        die(json_encode($response));
    }
	
	
	
   



?> 
