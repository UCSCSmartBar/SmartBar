<?php
// show potential errors / feedback (from login object)
if (isset($login)) {
    if ($login->errors) {
        foreach ($login->errors as $error) {
            echo $error;
        }
    }
    if ($login->messages) {
        foreach ($login->messages as $message) {
            echo $message;
        }
    }
}
?>

<!-- login form box -->

<div class="row">
	<div class="twelve columns">
		
			<form method="post" action="login.php" name="loginform">
				<fieldset>
					<legend>Login</legend>

					<label for="login_input_username" class="login_input_label">Username</label>
					<input id="login_input_username" class="login_input" style="width:200px;border:1px solid #ccc" type="text" name="user_name" required />

					<label for="login_input_password" class="login_input_label">Password</label>
					<input id="login_input_password" class="login_input" style="width:200px;border:1px solid #ccc" type="password" name="user_password" autocomplete="off" required />

					<input type="submit"  name="login" value="Log in" />
				</fieldset>
			</form>

<a href="registration.php">Register new account</a>
		
	</div>
</div>
