<?php
/**********************************************************************
* File name: putPrice.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 5/11/15
* Description: Adds the price from the given drink name in the database
***********************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
    
     
    $query = "UPDATE drinks SET price=:price WHERE (drinkName = :drink)";
    
    //updatetokens with the data to avoid SQL injection:
    $query_params = array(
        ':drink' => $_POST['drink'],
		':price' => $_POST['price']
		
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
    $response["message"] = "Price succesfully added.";
    echo json_encode($response);
    
    
    
} else {
?>
	<h1>Put Price</h1> 
	<form action="putPrice.php" method="post"> 
	    Drink Name:<br /> 
	    <input type="text" name="drink" value="" /> 
	    <br /><br /> 
		Price:<br /> 
	    <input type="text" name="price" value="" /> 
	    <br /><br /> 		
  
	    <input type="submit" value="Add Price" /> 
	</form>
	<?php
}

?>