<form method="post" action="login.php" name="loginform">

    <label for="login_input_username">Username</label>
    <input id="login_input_username" class="login_input" style="width:200px;border:1px solid #ccc" type="text" name="user_name" required />

    <label for="login_input_password">Password</label>
    <input id="login_input_password" class="login_input" style="width:200px;border:1px solid #ccc" type="password" name="user_password" autocomplete="off" required />

    <input type="submit"  name="login" value="Log in" />

</form>
