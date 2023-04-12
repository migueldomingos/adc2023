function register() {
  const username = document.getElementById("username").value;
  const name = document.getElementById("name").value;
  const password = document.getElementById("password").value;
  const confirmation = document.getElementById("confirmation").value;
  const email = document.getElementById("email").value;
  const phoneNum = document.getElementById("phoneNum").value;
  const mobileNum = document.getElementById("mobileNum").value;
  const ocupation = document.getElementById("ocupation").value;
  const workspace = document.getElementById("workspace").value;
  const addr = document.getElementById("addr").value;
  const postalCode = document.getElementById("postalCode").value;
  const NIF = document.getElementById("NIF").value;

  const data = {
    username,
    name,
    password,
    confirmation,
    email,
    phoneNum,
    mobileNum,
    ocupation,
    workspace,
    addr,
    postalCode,
    NIF,
  };

  const url = "/rest/register/add";

  fetch(url, {
    method: "POST",
    body: JSON.stringify(data),
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (response.ok) {
        window.location.href = "/index.html";
        return response.json;
      } else {
        throw new Error("Failed to register user.");
      }
    })
    .then((data) => {
      console.log(data.message);
      document.getElementById("success-message").textContent = data.message;
    })
    .catch((error) => {
      console.error(error);
      document.getElementById("error-message").textContent = error.message;
    });
}
