<?php include('header.php'); 

if( $_SESSION['user_login_status'] == 0) {
	echo '<meta http-equiv="refresh" content="0; URL=http://smartbarproject.com/">';
	exit();
}
?>
<!-- SUBHEADER 
================================================== -->
<div id="subheader">
	<div class="row">
		<div class="twelve columns">
			<div style="float:left">
				<span style="font-size:19pt;font-weight:bold">My SmartBar</span>
			</div>
			<div style="float:right">
				 <span style="font-size:19pt;font-weight:bold"><?php echo $_SESSION['user_name'];?></span>
			</div>
		</div>
	</div>
</div>
<div class="hr">
</div>


<div class="row" align="center">
<!-- TABS-->
<?php 

if ($_SESSION['user_admin'] == 0) {
	include('includes/userView.php');
} if($_SESSION['user_admin'] == 1){
	include('includes/adminView.php');
}
?>


</div>
<?php include('footer.php'); ?>