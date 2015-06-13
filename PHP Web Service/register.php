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
	    if (empty($_POST['username'])) {
	
        //JSON response 
        $response["success"] = 0;
        $response["message"] = "Please Enter a Username.";
        
        die(json_encode($response));
    }
    if (strlen($_POST['password']) < 6) {
	
        //JSON response 
        $response["success"] = 0;
        $response["message"] = "Please Enter a password of at least 6 characters.";
        
        die(json_encode($response));
    }
    if (!filter_var($_POST['email'], FILTER_VALIDATE_EMAIL)) {
	
        //JSON response 
        $response["success"] = 0;
        $response["message"] = "Your email is not of valid format.";
        
        die(json_encode($response));
    }
	if (strlen($_POST['phone']) != 11) {
	
        //JSON response 
        $response["success"] = 0;
        $response["message"] = "Please Enter an 11 digit phone number.";
        
        die(json_encode($response));
    }
	if ($_POST['age'] < 21) {
	
        //JSON response 
        $response["success"] = 0;
        $response["message"] = "You must be 21 years of age to legally drink and use our Bar.";
        
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
    $query = "INSERT INTO users ( userName, passWord, email, userPin, age, sex ) VALUES ( :user, :pass, :email, :pin, :age, :sex ) ";
    
    //Update tokens with the data:

    $query_params = array(
        ':user' => $_POST['username'],
        ':pass' => password_hash($_POST['password'], PASSWORD_DEFAULT),
		':age' => $_POST['age'],
		':sex' => $_POST['sex'],
		':email' => $_POST['email'],
		':pin' => $_POST['phone'],
    );
    
    //run our query, and create the user
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Create user failed.";
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
	<div align="center">
		
		<form action="register.php" method="post"> 
			Username:<br />
			<input type="text" name="username" value="" style="width:200px;border:1px solid #ccc" /> 
			<br /><br /> 
			Password:<br /> 
			<input type="password" name="password" value="" style="width:200px;border:1px solid #ccc" /> 
			<br /><br /> 
			Email:<br /> 
			<input type="email" name="email" value="" style="width:200px;border:1px solid #ccc" /> 
			<br /><br /> 
			Phone Number:<br /> 
			<input type="text" name="phone" value="" style="width:200px;border:1px solid #ccc" /> 
			<br /><br /> 
			Age:<br /> 
			<input type="text" name="age" value="" style="width:200px;border:1px solid #ccc" /> 
			<br /><br /> 
			Weight:<br /> 
			<input type="text" name="weight" value="" style="width:200px;border:1px solid #ccc" /> 
			<br /><br /> 
			Sex:<br /> 
			<input type="text" name="sex" value="" style="width:200px;border:1px solid #ccc"  /> 
			<br /><br /> 
			<input type="submit" value="Register New User" /> 
		</form>
	</div>
	<?php
}

?>
