<?php
/*****************************************************************
* File name: addDrinkLib.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 2/15/15
* Description: This function adds a drink to the drink library
******************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
    if (empty($_POST['drinkname']) ) {
		//JSON response
        $response["success"] = 0;
        $response["message"] = "Please Enter a drink name";
        
        die(json_encode($response));
    }
	if (empty($_POST['recipe']) ) {     
        
        $response["success"] = 0;
        $response["message"] = "Please Enter a drink recipe.";

        die(json_encode($response));
    }
    
     
    $query = "INSERT INTO drinks (drinkName, recipe) VALUES (:drinkname, :recipe)";
    
    //updatetokens with the data to avoid SQL injection:
    $query_params = array(
		':drinkname' => $_POST['drinkname'],
        ':recipe' => $_POST['recipe'],
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
    $response["message"] = "Drink Successfully Added to the Library!";
    echo json_encode($response);
    
    //for a php webservice you could do a simple redirect and die.
    //header("Location: login.php"); 
    //die("Redirecting to login.php");
    
    
} else {
?>
	<h1>Add Drink to Library</h1> 
	<form action="addDrinkLib.php" method="post"> 
	    Drink Name:<br /> 
	    <input type="text" name="drinkname" value="" /> 
	    <br /><br /> 
	    Recipe:<br /> 
	    <input type="text" name="recipe" value="" /> 
        <br /><br /> 
  
	    <input type="submit" value="Add new drink to the Library" /> 
	</form>
	<?php
}

?>