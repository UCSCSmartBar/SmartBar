<?php
/*****************************************************************
* File name: liquidLevels.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/26/15
* Description: Displays current container levels from the current 
* inventory
******************************************************************/
	require("config.inc.php");
	//Get Inventory from database
	$sql = " 
            SELECT 
                inv
            FROM inventory
        ";
	$result = $db->query($sql);
	$row = $result->fetch(); 
	$invarray = explode("@", $row["inv"]);
	
	//echo $invarray[2];
	//echo "<br />";
	$info = explode(",", $invarray[0]);
	$SpiritTotal = $info[1];
	$MixersTotal = $info[2];
	
	$levels = [];
	//levels: Container # | Spirit | Brand | Current Vol | Total Vol
	for($i = 0; $i < $SpiritTotal; $i++){
		$inv = explode(",", $invarray[$i+1]);
		$levels[] = $inv[0]; // Container #
		$levels[] = $inv[1]; // Spirit
		$levels[] = $inv[2]; // Brand
		$levels[] = $inv[3]; // Current Vol
		$levels[] = $inv[4]; // Total Vol
	}
	
	$index = 0;
	for($j = 0; $j < $SpiritTotal; $j++ ){		
		echo "Container #: " . $levels[$index] . "<br />";
		echo "Spirit: ";
		echo drinkStrings($levels[$index+1]) . "<br />";
		echo "Brand: " . $levels[$index+2] . "<br />";
		echo "Current Volume (Oz): " . round($levels[$index+3], 1) . "<br />";
		echo "Total Volume (Oz): " . round($levels[$index+4], 1);
		$index = $index+5;
	}
	
	//echo print_r($levels);
	
	$index = 0;
	for($k = 0; $k < $MixersTotal; $k++ ){		
		echo "Container #: " . $mLevels [$index] . "<br />";
		echo "Mixer: ";
		echo "Brand: " . $mLevels [$index+2] . "<br />";
		echo "Current Volume (Oz): " . round($mLevels [$index+3], 1) . "<br />";
		echo "Total Volume (Oz): " . round($mLevels [$index+4], 1);
		$index = $index+5;
	}
	
	
	function drinkStrings($spirit){
		switch($spirit){
			case "AS":
				echo "Absinthe";
				break;
				
			case "AB":

				echo "Bitters";

			break;
			case "BO":
				echo "Bourbon";
				break;
			case "BR":
				echo "Brandy";
				break;
			case "CG":
				echo "Cognac";
				break;
			case "EV":
				echo "EverClear";
				break;
			case "GN":
				echo "Gin";
				break;
			case "MO":
				echo "Moonshine";
				break;
			case "ME":
				echo "Mezcal";
				break;
			case "RM":
				echo "Rum";
				break;
			case "ST":
				echo "Scotch";
				break;
			case "TQ":
				echo "Tequila";
				break;
				
			case "TS":

				echo "Triplesec";

			break;
			case "VE":
				echo "Vermouth";
				break;
			case "VO":
				echo "Vodka";
				break;
			case "WH":
				echo "Whiskey";
				break;
			case "BS":

				echo "Blood Orange Italian Soda";

				break;
			case "MA":

				echo "Mango Italian Soda";

				break;
			case "CH":

				echo "Cherry Italian Soda";

				break;
			case "WM":

				echo "Watermelon Italian Soda";

				break;
			case "VA":

				echo "Vanilla Italian Soda";

				break;
		}
	}

	?>