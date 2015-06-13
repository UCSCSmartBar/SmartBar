<?php

/*****************************************************************
* File name: getCurInv.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/19/15
* Description: Returns the average age
******************************************************************/
	require("config.inc.php");
	
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
	
	$totalUsers = $row["userId"];
	
	
	$sql1 = " SELECT SUM(age) from users";
    
	//Run SQL
    $result1 = $db->query($sql1);

	//Get Results
	$row1 = $result1->fetch();
	
	echo intval($row1["SUM(age)"] / $totalUsers);
	
	
?>