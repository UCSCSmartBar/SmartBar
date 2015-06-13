<?php include("header.php"); ?>
<!-- SLIDER 
================================================== -->
<div id="ei-slider" class="ei-slider">
	<ul class="ei-slider-large">
		<li>
		<img src="./images/bar.jpg" alt="image01" class="responsiveslide">
	</ul>
<!-- SUBHEADER
================================================== -->
<div id="subheader">
	<div class="row">
		<div class="twelve columns">
			<p class="text-center">
				 <span style="color:#fff">Welcome to SmartBar <?php echo $_SESSION['user_name']; ?></span>
			</p>
		</div>
	</div>
</div>
<!-- CONTENT 
================================================== -->
<div class="row">
	<div class="twelve columns">
		<div class="centersectiontitle">
			<h4><?php
			if(isset($_GET['paysuccess'])) {
			echo "Thank you for your payment!";
			echo "<br />";
			echo "<br />";
			echo "<br />";
			}
			if(isset($_GET['payfail'])) {
			echo "Payment failed, please try again.";
			echo "<center>";
			echo "<a href='https://smartbarproject.com/mySB.php#pay'> Here </a>";
			echo "</center>";
			echo "<br />";
			echo "<br />";
			echo "<br />";
			}
			?>
			How to use the SmartBar</h4>
		</div>
	</div>
	<div class="four columns">
		<h5>Mobile App</h5>
		<p>
			Coming soon.
		</p>
		<p>
			<!--<a href="#" class="readmore">Learn more</a>-->
		</p>
	</div>
	<div class="four columns">
		<h5>Website</h5>
		<p>
			Review drink statistics for users and bar statistics for owners.
		</p>
		<p>
			<!--<a href="#" class="readmore">Learn more</a>-->
		</p>
	</div>
	<div class="four columns">
		<h5>At the SmartBar</h5>
		<p>
			Order at our UI.
		</p>
		<p>
			<!--<a href="#" class="readmore">Learn more</a>-->
		</p>
	</div>
</div>
<div class="hr">
</div>
<!-- TESTIMONIALS 
================================================== -->
<div class="row">
	<div align="center" class="twelve columns">
		<h1>Download our App! (Soon)</h1><br />
		<img src="./images/s5.png" />
	</div>
</div>


<?php include("footer.php"); ?>
