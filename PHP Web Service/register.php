<?php
/*****************************************************************
* File name: register.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 2/6/15
* Description: This function takes the POST fields of the page username,
* password, age, weight, and sex and inserts them into the datatbase 
* after checking to see that the username isnt already in use. Then it
* returns a JSON response if the user was succesfully inserted or failed.
******************************************************************/

//Config.inc contains the information for our database
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
    //If the username or password is empty when the user submits
    //the form, the page will die.
    if (empty($_POST['username']) || empty($_POST['password'])) {

        //JSON response 
        $response["success"] = 0;
        $response["message"] = "Please Enter Both a Username and Password.";
        
        die(json_encode($response));
    }
    
    //Check to see if the username exists
	
    //":user" is just a blank variable that we will change before we execute the query.
    //This helps protect us from SQL injections
    $query        = " SELECT 1 FROM users WHERE userName = :user";
    $query_params = array(
        ':user' => $_POST['username']
    );
    
    try {
        // These two statements run the query against your database table. 
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        //JSON data:
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
    }
    
    //fetch is an array of returned data.  
	//Check to see if username exists
    $row = $stmt->fetch();
    if ($row) {
        $response["success"] = 0;
        $response["message"] = "I'm sorry, this username is already in use";
        die(json_encode($response));
    }
    
    //If username isnt in use now we can create a new one
    $query = "INSERT INTO users ( userName, passWord, age, weight, sex ) VALUES ( :user, :pass, :age, :weight, :sex ) ";
    
    //Update tokens with the data:
    $query_params = array(
        ':user' => $_POST['username'],
        ':pass' => $_POST['password'],
		':age' => $_POST['age'],
		':weight' => $_POST['weight'],
		':sex' => $_POST['sex']
    );
    
    //run our query, and create the user
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
    }
    
    //Successfully added to the database!
    $response["success"] = 1;
    $response["message"] = "Username Successfully Added!";
    echo json_encode($response);
    
    //for a php webservice we could do a simple redirect and die.
    //header("Location: login.php"); 
    //die("Redirecting to login.php");
    
    
} else {
?>
	<h1>Register</h1> 
	<form action="register.php" method="post"> 
	    Username:<br /> 
	    <input type="text" name="username" value="" /> 
	    <br /><br /> 
	    Password:<br /> 
	    <input type="password" name="password" value="" /> 
        <br /><br /> 
	    Age:<br /> 
	    <input type="text" name="age" value="" /> 
	    <br /><br /> 
        Weight:<br /> 
	    <input type="text" name="weight" value="" /> 
	    <br /><br /> 
        Sex:<br /> 
	    <input type="text" name="sex" value="" /> 
	    <br /><br /> 
	    <input type="submit" value="Register New User" /> 
	</form>
	<?php
}

?>