<?php
 
/*
 * Following code will create a new user row
 * All product details are read from HTTP Post Request
 */
 
// array for JSON response
$response = array();
 
// check for required fields
if (isset($_POST['userName']) && isset($_POST['passWord']) && isset($_POST['age']) && isset($_POST['weight']) && isset($_POST['sex'])) {
 
    $userName = $_POST['userName'];
    $passWord = $_POST['passWord'];
	$age = $_POST['age'];
	$weight = $_POST['weight'];
    $sex = $_POST['sex'];
 
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // mysql inserting a new row
    $result = mysql_query("INSERT INTO users(userName, passWord, age, weight, sex) VALUES('$userName', '$passWord', '$age', '$weight', '$sex')");
 
    // check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Product successfully created.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "Oops! An error occurred.";
 
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>