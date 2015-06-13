<div class="twelve columns">
	<h5>My SmarBar Admin Account</h5>
	<dl class="tabs">
		<dd class="active"><a href="#accntinfo">Bar Info</a></dd>
		<dd><a href="#upaccnt">Stock Info</a></dd>
		<dd><a href="#stats">Liquid Levels</a></dd>
	</dl>
	<ul class="tabs-content">
		<li class="active" id="accntinfoTab">
			<p align="left" >
			<span class="infotitle">Current Registered Users:</span> <span class="info"><?php include('./getTotalUsers.php'); ?></span>  <br />
			<span class="infotitle">Drinks Poured:</span> <span class="info"><?php include('./getTotalDrinks.php'); ?></span> <br />
			<span class="infotitle">Average User Age:</span> <span class="info"><?php include('./getAvgAge.php'); ?></span> <br />			
			<br />
			</p>
		</li>
		<li id="upaccntTab">
			<p align="left" >
			<span class="infotitle">Drinks in Stock:</span> <br /><span class="info"><?php include('./getCurInv.php'); ?></span>  <br />
			<br />
			</p>
		</li>
		<li id="statsTab">
			<p align="left" >
			<span class="infotitle">Container Levels:</span> <br /><span class="info"><?php include('./liquidLevels.php'); ?></span>  <br />
			<br />
			</p>
		</li>
	</ul>
</div>