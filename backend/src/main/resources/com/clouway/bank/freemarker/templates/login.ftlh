<!DOCTYPE html>
<html>
<head>
    <title>E corp</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="static/index/css/desktop.css" rel="stylesheet"/>
    <link media="screen and (max-width:600px)" href="static/index/css/phone.css" rel="stylesheet"/>
</head>
<body class="doc">
<script src="static/index/js/functionality.js"></script>
<img class="logo" src="static/index/images/logo.png"/>
<div class="container">
    <form action="/login" class="form" method="post">
        <#if content != "">
        <p class="error">${content}</p>
    </#if>
    <label class="form__label" for="usernameOrEmail">Username/E-mail</label>
    <input class="form__input" id="usernameOrEmail" name="usernameOrEmail"/>
    <label class="form__label" for="password">Password</label>
    <input class="form__input" id="password" type="password" name="password"/>
    <button class="button form__button">Log in</button>
</form>
<button class="button" onclick="location.href='/index'">
    Back
</button>
</div>
</body>
        </html>