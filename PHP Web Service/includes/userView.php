	
	
	
	<div class="twelve columns">

		<h5>My SmarBar Account</h5>
		<dl class="tabs">
			<dd class="active"><a href="#accntinfo">My Info</a></dd>
			<dd><a href="#upaccnt">Update My Info</a></dd>
			<dd><a href="#stats">Stats</a></dd>
			<dd><a href="#pay">Payment</a></dd>
		</dl>
		<ul class="tabs-content">
			<li class="active" id="accntinfoTab">
			<p align="left" >
			
				<span class="infotitle">Username:</span> <span class="info"><?php echo $_SESSION['user_name']; ?></span>  <br />
				<span class="infotitle">Email:</span> <span class="info"><?php echo $_SESSION['user_email']; ?></span> <br />				
				<span class="infotitle">Phone:</span> <span class="info"><?php echo $_SESSION['user_pin']; ?></span> <br />
				<span class="infotitle">Age:</span> <span class="info"><?php echo $_SESSION['user_age']; ?></span> <br />
				<span class="infotitle">Sex:</span> <span class="info"><?php echo $_SESSION['user_sex']; ?> </span> <br />
			</p>
			</li>
			<li id="upaccntTab">
				<?php include("updateAccntInfo.php"); ?>
			</li>
			<li id="statsTab">
				<p align="left" >
			<br />
				<span class="infotitle">Total Drinks:</span> <span class="info"><?php include('getUserDrinks.php'); ?> </span>  <br />

			</p>
			</li>
			<li id="payTab">
				
				<?php include("includes/payment.php"); ?>

			</li>
		</ul>
	</div>