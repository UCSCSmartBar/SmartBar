<?php
/*****************************************************************
* File name: findPhone.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 5/7/15
* Description: Returns the phone number based on the provided username
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
			(userName= :usr)
    ";
    
    $query_params = array(
        ':usr' => $_POST['username']

    );
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Username not found. Please Try Again!";
        die(json_encode($response));
    }
     
    //fetching all the rows from the query
    $row = $stmt->fetch();
    if ($row) {
        //compare the two recipes
        if ($_POST['username'] === $row['userName'] ) {
			$phone = $row['userPin'];
            $phone_found = 1;
        }
    }

	// Return status if the BAC was found or not
    if ($phone_found) {
        $response["success"] = 1;
        $response["message"] = "$phone";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Phone Number Not Found";
        die(json_encode($response));
    }
} else {
?>
	<form action="findPhone.php" method="post"> 
	    Username:<br /> 
	    <input type="text" name="username" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Get Phone" /> 
	</form>
<?php
	}
?>