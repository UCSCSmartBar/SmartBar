<?php
/*****************************************************************
* File name: addDrink.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 2/15/15
* Description: This function takes the username, pin, and drink from
* the POST fields and inserts it into the database with the matching
* username. Returns a JSON response of weather the drink was successfully
* added or failed.
******************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
    if (empty($_POST['username']) ) {
		//JSON response
        $response["success"] = 0;
        $response["message"] = "Please Enter a Username";
        
        die(json_encode($response));
    }
	if (empty($_POST['pin']) ) {     
        
        $response["success"] = 0;
        $response["message"] = "Please Enter a Pin.";

        die(json_encode($response));
    }
	if (empty($_POST['drink']) ) {
        
        $response["success"] = 0;
        $response["message"] = "Please Enter a Drink.";
        
        die(json_encode($response));
    }
    
     
    $query = "UPDATE users SET userPin=:pin, drinks=:drink, drinkQueue = 1 WHERE userName = :user";
    
    //updatetokens with the data to avoid SQL injection:
    $query_params = array(
		':user' => $_POST['username'],
        ':pin' => $_POST['pin'],
        ':drink' => $_POST['drink']
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
    $response["message"] = "Drink Successfully Added!";
    echo json_encode($response);
    
    //for a php webservice you could do a simple redirect and die.
    //header("Location: login.php"); 
    //die("Redirecting to login.php");
    
    
} else {
?>
	<h1>Add Drinks</h1> 
	<form action="addDrink.php" method="post"> 
	    Username:<br /> 
	    <input type="text" name="username" value="" /> 
	    <br /><br /> 
	    Pin:<br /> 
	    <input type="password" name="pin" value="" /> 
        <br /><br /> 
	    Drink:<br /> 
	    <input type="text" name="drink" value="" /> 
	    <br /><br /> 
  
	    <input type="submit" value="Add new drink" /> 
	</form>
	<?php
}

?>