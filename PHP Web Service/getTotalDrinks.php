<?php

/*****************************************************************
* File name: getTotalDrinks.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/19/15
* Description: Returns the amount of drinks poured by the SmartBar
******************************************************************/
	require("config.inc.php");
	
	$sql = " SELECT SUM(drinkTotal) from users";
    
	//Run SQL
    $result = $db->query($sql);
    
    

	//Get Results
	$row = $result->fetch();
	
	echo $row["SUM(drinkTotal)"];

	
	
?>