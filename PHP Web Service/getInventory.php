<?php

/*****************************************************************
* File name: getTotalUsers.php
* Author: Brendan Short
* Project: UCSC SmartBar
* Date: 5/11/15
* Description: Returns the amount of users in the table
******************************************************************/

	//Call MySQL database (smartbar)
	require_once("config.inc.php");
	
	// Set up SQL query
	$sql = " 
            SELECT inv
			FROM inventory
        ";
    
	//Run SQL
    $result = $db->query($sql);

	//Get Results
	$fetched_inventory = $result->fetch();
	
	// Echo gathered inventory to screen
	//	echo $fetched_inventory["inv"]; 

	// Encode message into json
 	$response["success"] = 1;
    $response["message"] = $fetched_inventory["inv"];
    die(json_encode($response)); 
	
?>