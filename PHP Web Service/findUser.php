<?php
/*****************************************************************
* File name: findUser.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 5/8/15
* Description: Returns the username based on the provided phone
******************************************************************/


//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
  
     $query = " 
		SELECT 
			userName,
			userPin
		FROM users
		WHERE 
			(userPin = :phone)
    ";
    
    $query_params = array(
        ':phone' => $_POST['phone']

    );
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Phone number not found. Please Try Again!";
        die(json_encode($response));
    }
     
    //fetching all the rows from the query
    $row = $stmt->fetch();
    if ($row) {
        //compare the two recipes
        if ($_POST['phone'] === $row['userPin'] ) {
			$name = $row['userName'];
            $name_found = 1;
        }
    }

	// Return status if the BAC was found or not
    if ($name_found) {
        $response["success"] = 1;
        $response["message"] = "$name";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Phone Number Not Found";
        die(json_encode($response));
    }
} else {
?>
	<form action="findUser.php" method="post"> 
	    Phone:<br /> 
	    <input type="text" name="phone" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Get Phone" /> 
	</form>
<?php
	}
?>