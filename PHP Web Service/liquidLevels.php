<?php
/*****************************************************************
* File name: liquidLevels.php
* Author: Eloy Salinas
* Project: UCSC SmartBar
* Date: 4/26/15
* Description: Displays current container levels from the current 
* inventory
******************************************************************/	//Database connection
	require("config.inc.php");
	//Get Inventory from database
	$sql = " 
            SELECT 
                inv
            FROM inventory
        ";	//Run query
	$result = $db->query($sql);
	$row = $result->fetch(); 		//Split up string by @
	$invarray = explode("@", $row["inv"]);
	
	//echo $invarray[2];
	//echo "<br />";	//Spirit and Mixer totals
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
	
	$index = 0;	echo '<div id="liquidlvlspirit">';	echo '<h4><span style="color:#440099">Drinks</span></h4>';
	for($j = 0; $j < $SpiritTotal; $j++ ){				
		echo "Container #: " . $levels[$index] . "<br />";
		echo "Spirit: ";
		echo drinkStrings($levels[$index+1]) . "<br />";
		echo "Brand: " . $levels[$index+2] . "<br />";
		echo "Current Volume (Oz): " . round($levels[$index+3], 1) . "<br />";
		echo "Total Volume (Oz): " . round($levels[$index+4], 1);				$contPercent = round(($levels[$index+3]/$levels[$index+4])*100  ,0);				//Graphical percent bar		echo '<dl style="width:280px;">';		echo '<dd><div id="data-'.($j+1).'" class="bar" style="width:'.$contPercent.'%">'.$contPercent.'%</div></dd>';		echo '</dl><br /><br /><br />';
		$index = $index+5;
	}	echo '</div>';
		$mLevels = [];
	//echo print_r($levels);	//levels: Container # | Spirit | Brand | Current Vol | Total Vol	for($l = 0; $l < $MixersTotal; $l++){		$inv = explode(",", $invarray[$l+1+$SpiritTotal]);		$mLevels[] = $inv[0]; // Container #		$mLevels[] = $inv[1]; // Mixer		$mLevels[] = $inv[2]; // Brand		$mLevels[] = $inv[3]; // Current Vol		$mLevels[] = $inv[4]; // Total Vol 	}
		echo '<div id="liquidlvlmixer">';	echo '<h4><span style="color:#440099">Mixers</span></h4>';
	$index = 0;
	for($k = 0; $k < $MixersTotal; $k++ ){		
		echo "Container #: " . $mLevels [$index] . "<br />";
		echo "Mixer: ";		echo mixerStrings($mLevels[$index+1]) . "<br />";
		echo "Brand: " . $mLevels [$index+2] . "<br />";
		echo "Current Volume (Oz): " . round($mLevels [$index+3], 1) . "<br />";
		echo "Total Volume (Oz): " . round($mLevels [$index+4], 1);				$contPercent = round(($mLevels[$index+3]/$mLevels[$index+4])*100  ,0);				//Graphical percent bar		echo '<dl style="width:280px;">';		echo '<dd><div id="data-'.($k+1+$SpiritTotal).'" class="bar" style="width:'.$contPercent.'%">'.$contPercent.'%</div></dd>';		echo '</dl><br /><br /><br />';
		$index = $index+5;
	}	echo '</div>';
	
		//Convert from inventory to actual drink names
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
				break;			case "GT":				echo "Gatorade";				break;			case "WA":				echo "Water";				break;			case "GA":				echo "Ginger Ale";				break;			case "SP":				echo "SP";				break;
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
	}		//Convert inventory codes into mixer names	function mixerStrings($mixer){		switch($mixer){			case "AB":				echo "Angostura Bitters";				break;			case "LE":				echo "Lemonade";				break;			case "CO":				echo "Cola";				break;			case "CR":				echo "Cream";				break;			case "EG":				echo "Eggs";				break;			case "GA":				echo "Ginger Ale";				break;			case "GR":				echo "Grenadine";				break;			case "IC":				echo "Ice Cream";				break;			case "KL":				echo "Kina Lillet";				break;			case "MK":				echo "Milk";				break;			case "OB":				echo "Orange Bitters";				break;			case "SS":				echo "Simple Sugar";				break;			case "SM":				echo "Sour Mix";				break;			case "SP":				echo "Sprite/7-Up";				break;			case "CF":				echo "Tea/Coffee";				break;			case "WA":				echo "Water";				break;			case "SO":				echo "Soda";				break;			case "TO":				echo "Tonic";				break;		}	}

	?>