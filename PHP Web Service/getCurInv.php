<?php

/*****************************************************************
* File name: getCurInv.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/19/15
* Description: Returns the current drinks that the SmartBar can pour
******************************************************************/
	require("config.inc.php");
	
	$sql = " SELECT drinkName FROM drinks WHERE stock = 1";
    
	//Run SQL
    $result = $db->query($sql);
    

	//Get Results
	while($row = $result->fetch()) {
	    $drinks_results .= $row["drinkName"] . ",";
    }
	$drinksarray = explode(",", $drinks_results);
	for($i = 0; $i < count($drinksarray); $i++){
		echo $drinksarray[$i];
		echo "<br />";
	}
	
	
	
	
?>