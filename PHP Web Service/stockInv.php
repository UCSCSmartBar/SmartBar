<?php

/*****************************************************************
* File name: stockInv.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 2/12/15
* Description: Checks if the ingredients to make drinks are avaliable
* in the inventory (No Brand support yet)
******************************************************************/
	require("config.inc.php");
	$sql = " 
            SELECT 
                drinkName,
				recipe
            FROM drinks 

        ";
    
	//Run SQL
    $result = $db->query($sql);
    
    
    //fetching all the rows from the query
    //$row = $stmt->fetch();

	//Get Results
	while($row = $result->fetch()) {
	    $drinks_results .= $row["drinkName"] . ",";
	    $recipe_results .= $row["recipe"] . "%";
    }
	//Drinks and recipes array
	$drinksarray = explode(",", $drinks_results);
	$recipesarray = explode("%", $recipe_results);
	
	//Get Inventory
	$sql1 = " 
            SELECT 
                inv
            FROM inventory

        ";
	$result1 = $db->query($sql1);
	
	$row1 = $result1->fetch(); 
	$invarray = explode("@", $row1["inv"]);
	
	//echo $invarray[2];
	//echo "<br />";
	
	$inStock = [];
	
	//What drinks are in stock? Return how much and the brand
	for($i = 0; $i <= (count($invarray) - 1); $i++){
		$inv = explode(",", $invarray[$i]);
		if(($inv[3]) > 0){
			$inStock[] = $inv[1];
			$inStock[] = $inv[0];
			$inStock[] = $inv[3];
		}
	}

	//echo $recipesarray[0]; 
	echo print_r($inStock);
	echo "<br />";
	//Is the drink avaliable?
    //Go through drink recipe
	$drinksInStock = [];
	$drinkCheck = [];
	$stocked = 0;
	for($j = 0; $j < count($drinksarray) - 1; $j++){ //
		$rec = explode("@", $recipesarray[$j]);
		echo count($rec)-1;
		//echo "<br />";
		for($k = 0; $k < (count($rec) - 1); $k++){
			$splitrec = explode(",", $rec[$k +1]);
			echo $splitrec[0];
			echo "<br />";
			
			for($s = 0; $s < count($inStock); $s++){
				if( strcmp($splitrec[0], $inStock[$s]) === 0 ){
					$stocked = $stocked + 1;
					break;
				}
			}
			
			
		}
		
		echo "Stocked: ";
		echo var_dump($stocked == count($rec)-1 && $stocked > 0);
		echo "<br />";
		//If all the ingredients are in the Inventory
		if ($stocked === (count($rec) - 1) && $stocked > 0){
			$drinksInStock[] = $drinksarray[$j];
		}
		$stocked = 0;
	}
	echo print_r($drinksInStock);
	//Empty stock
	for($l = 0; $l <= count($drinksInStock); $l++){
		$query = "UPDATE drinks SET stock = 0";
		
		//updatetokens with the data to avoid SQL injection:
		$query_params = array(
			':drink' => $drinksInStock[$l]
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
	
	}
	
	
	
	
	//Now we update the database with the drinks we can make
	for($m = 0; $m <= count($drinksInStock); $m++){
		$query = "UPDATE drinks SET stock = 1 WHERE drinkName = :drink";
		
		//updatetokens with the data to avoid SQL injection:
		$query_params = array(
			':drink' => $drinksInStock[$m]
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
	
	}
	
	
?>