<?php
/*****************************************************************
* File name: isFP.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/26/15
* Description: Checks if the pin has an associated fingerprint
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
				LEFT(fingerprint, 1) AS fingerprint
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
			$fingerprint = $row['fingerprint'];
			if ($fingerprint > 0){
				$fp_found = 1;
			}
        }
    }

	// Return status if the BAC was found or not
    if ($fp_found) {
        $response["success"] = 1;
        $response["message"] = "Fingerprint Found for " . $_POST['pin'] . ".";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Fingerprint Not Found for " . $_POST['pin'] . ".";
        die(json_encode($response));
    }
} else {
?>
	<h1>Get FP</h1> 
	<form action="isFP.php" method="post"> 
	    Phone/Pin:<br /> 
	    <input type="text" name="pin" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Get BAC" /> 
	</form>
<?php
	}
?>