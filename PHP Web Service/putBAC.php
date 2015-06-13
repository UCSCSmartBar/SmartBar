<?php
/*****************************************************************
* File name: addDrink.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/3/15
* Description: This function takes BAC as input and inserts into the 
* database in the respective users row.
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
		if (empty($_POST['bac']) ) {     
        
        $response["success"] = 0;
        $response["message"] = "Please Enter a BAC value.";

        die(json_encode($response));
    }

    
     
    $query = "UPDATE users SET BAC=:bac WHERE (userPin = :pin)";
    
    //updatetokens with the data to avoid SQL injection:
    $query_params = array(
        ':pin' => $_POST['pin'],
		':bac' => $_POST['bac']
		
    );
    
    //run our query, and create the drink
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
    }
    
    //We have now successfully added a new drink!

    $response["success"] = 1;
    $response["message"] = "BAC successfully Added for " . $_POST['pin'] . ".";
    echo json_encode($response);
    
    
    
} else {
?>
	<h1>Add BAC</h1> 
	<form action="putBAC.php" method="post"> 
	    Phone/Pin:<br /> 
	    <input type="text" name="pin" value="" /> 
	    <br /><br /> 
		BAC:<br /> 
	    <input type="text" name="bac" value="" /> 
	    <br /><br /> 		
  
	    <input type="submit" value="Add BAC" /> 
	</form>
	<?php
}

?>