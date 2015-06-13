<?php
// show potential errors / feedback (from registration object)
if (isset($registration)) {
    if ($registration->errors) {
        foreach ($registration->errors as $error) {
            echo $error;
        }
    }
    if ($registration->messages) {
        foreach ($registration->messages as $message) {
            echo $message;
        }
    }
}
?>

<!-- register form -->
<div class="row">
	<div class="twelve columns">
		<form method="post" action="registration.php" id="register" name="registerform">
			<fieldset>
				<legend>Register</legend>
				<!-- the user name input field uses a HTML5 pattern check -->
				<label for="login_input_username" class="login_input_label">Username (only letters and numbers, 2 to 64 characters)</label>
				<input id="login_input_username" value="<?php if(isset($_POST['user_name'])) echo $_POST['user_name'];  ?>" class="login_input" type="text" style="width:200px;border:1px solid #ccc" pattern="[a-zA-Z0-9]{2,64}" name="user_name" required />
				<br />
				<!-- the email input field uses a HTML5 email type check -->
				<label for="login_input_email" class="login_input_label">Email</label>
				<input id="login_input_email" value="<?php if(isset($_POST['user_email'])) echo $_POST['user_email'];  ?>" class="login_input" type="email" style="width:200px;border:1px solid #ccc" name="user_email" required />
				<br />
				<label for="login_input_password_new" class="login_input_label">Password (min. 6 characters)</label>
				<input id="password"  class="login_input" value="<?php if(isset($_POST['user_password_new'])) echo $_POST['user_password_new'];  ?>" type="password" style="width:200px;border:1px solid #ccc" name="user_password_new" pattern=".{6,}" required autocomplete="off" />
				<span id="result"></span>
				
				<br />
				<label for="login_input_password_repeat" class="login_input_label">Repeat password</label>
				<input id="password2" class="login_input" value="<?php if(isset($_POST['user_password_repeat'])) echo $_POST['user_password_repeat'];  ?>" type="password" style="width:200px;border:1px solid #ccc" name="user_password_repeat" pattern=".{6,}" required autocomplete="off" />
				<span id="result2"></span>
				<br />
				<label for="login_input_pin" class="login_input_label">Phone Number (11 digits)</label>
				<input id="login_input_pin" value="<?php if(isset($_POST['user_pin'])) echo $_POST['user_pin'];  ?>" class="login_input" type="text" name="user_pin" style="width:200px;border:1px solid #ccc" required />
				<br />
				<label for="login_input_age" class="login_input_label">Age (21+)</label>
				<input id="login_input_age" value="<?php if(isset($_POST['user_age'])) echo $_POST['user_age'];  ?>" class="login_input" type="text" name="user_age" style="width:200px;border:1px solid #ccc" required />
				<br />
				
				<label for="login_input_sex" class="login_input_label">Sex</label>
				<select name="user_sex" style="width:100px;border: solid 1px #ddd">
				  <option value="M">Male</option>
				  <option value="F">Female</option>
				  <option value="O">Other</option>
			    </select>
				<br /><br />
				<input type="submit"  name="register" value="Register" />
			</fieldset>
		</form>
	</div>
</div>



