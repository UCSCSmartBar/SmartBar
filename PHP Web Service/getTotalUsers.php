<?php

/*****************************************************************
* File name: getTotalUsers.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/19/15
* Description: Returns the amount of users in the table
******************************************************************/

	//Call MySQL database (smartbar)
	require_once("config.inc.php");
	
	$sql = " 
            SELECT userId
			FROM users
			ORDER BY userId 
			DESC LIMIT 1
        ";
    
	//Run SQL
    $result = $db->query($sql);

	//Get Results
	$row = $result->fetch();
	
	echo $row["userId"];

	
	
?>