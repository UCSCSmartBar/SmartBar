<?php
/*****************************************************************
* File name: getBAC.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/3/15
* Description: This function gets the BAC from the users row
******************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {

	if (empty($_POST['pin']) ) {     
        $response["success"] = 0;
        $response["message"] = "Please Enter a Pin.";
        die(json_encode($response));
    }

     $query = " 
            SELECT 
                userId,
                userName,
				userPin,
				BAC
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
    
    $validated_info = false;
    
    //fetching all the rows from the query
    $row = $stmt->fetch();
    if ($row) {
        //compare the two pins
        if ($_POST['pin'] === $row['userPin'] ) {
			$bac = $row['BAC'];
			$name = $row['userName'];
            $bac_found = 1;
        }
    }

	// Return status if the BAC was found or not
    if ($bac_found) {
        $response["success"] = 1;
        $response["message"] = "$bac";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "BAC Not Found";
        die(json_encode($response));
    }
} else {
?>
	<h1>Get BAC</h1> 
	<form action="getBAC.php" method="post"> 
	    Phone/Pin:<br /> 
	    <input type="text" name="pin" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Get BAC" /> 
	</form>
<?php
	}
?>