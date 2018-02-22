var loginForm = null;
var registerForm = null;
var login = null;
var register = null;

window.onload = init;

function init() {
    loginForm = document.getElementById("loginForm");
    registerForm = document.getElementById("registerForm");
    loginButton = document.getElementById("buttonLogin");
    registerButton = document.getElementById("buttonRegister");
}

function loginShowHide() {
  if(loginForm.getAttribute("class") === "form") {
    hideLogin()
  }
  else {
    if(registerForm.getAttribute("class") === "form") {
      hideRegister()
    }
    showLogin()
  }
}
function registerShowHide() {
  if(registerForm.getAttribute("class") === "form") {
    hideRegister()
  }
  else {
    if(loginForm.getAttribute("class") === "form") {
      hideLogin()
    }
    showRegister()
  }
}
function hideLogin() {
  loginForm.setAttribute("class", "form form--hidden")
  loginButton.innerText = "I have already account";
}
function showLogin() {
  loginForm.setAttribute("class", "form");
  loginButton.innerText = "Back";
}
function hideRegister() {
  registerForm.setAttribute("class", "form form--hidden")
  registerButton.innerText = "Create new account";
}
function showRegister() {
  registerForm.setAttribute("class", "form")
  registerButton.innerText = "Back";
}