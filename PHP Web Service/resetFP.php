<?php
/*****************************************************************
* File name: isFP.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/26/15
* Description: Clears the fingerprint for the associated pin
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

    $query = "UPDATE users SET fingerprint='' WHERE (userPin = :pin)";
    
    //updatetokens with the data to avoid SQL injection:
    $query_params = array(
        ':pin' => $_POST['pin']
		
    );
    
    //run our query, and create the drink
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Pin error. Please Try Again!";
        die(json_encode($response));
    }
    
    //We have now successfully added a new drink!

    $response["success"] = 1;
    $response["message"] = "Fingerprint successfully cleared for " . $_POST['pin'] . ".";
    echo json_encode($response);
	
	
	
} else {
?>
	<h1>Reset FP</h1> 
	<form action="resetFP.php" method="post"> 
	    Phone/Pin:<br /> 
	    <input type="text" name="pin" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Get BAC" /> 
	</form>
<?php
	}
?>