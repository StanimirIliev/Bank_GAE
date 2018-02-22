<!DOCTYPE html>
<html>
<head>
    <title>E corp</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="static/index/css/desktop.css" rel="stylesheet" />
    <link media="screen and (max-width:600px)" href="static/index/css/phone.css" rel="stylesheet" />
</head>
<body class="doc">
<script src="static/index/js/functionality.js"></script>
<img class="logo" src="static/index/images/logo.png" />
<div class="container" >
    <form action="/registration" class="form" method="post">
        <#list errors as error>
        <p class="error">${error.content}</p>
    </#list>
    <label class="form__label" for="username">Username</label>
    <input class="form__input" id="username" name="username" />
    <label class="form__label" for="password">Password</label>
    <input class="form__input" id="password" type="password" name="password"/>
    <label class="form__label" for="confirmPassword">Confirm password</label>
    <input class="form__input" id="confirmPassword" type="password" name="confirmPassword"/>
    <button  class="button form__button">Register</button>
</form>
<button class="button" onclick="location.href='/index'">
    Back
</button>
</div>
</body>
        </html>