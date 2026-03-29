import { useState } from 'react';

function App() {
  const [inputText, setInputText] = useState("");
  const [message, setMessage] = useState("");

  function callGet() {
    fetch("http://localhost:8080/api/greeting")
        .then((response) => response.text())
        .then((data) => setMessage(data));
}

  function callPost(inputText: any) {
    fetch("http://localhost:8080/api/echo",
        {
          method: "POST",
          headers: {
            "Content-Type": "text/plain", 
          },
          body: inputText,
        })
        .then((response) => response.text())
        .then((data) => setMessage(data));
        
  }

  function handleKeyDown(event: any) {
    if (event.key === "Enter") {
      callPost(inputText);
    }
  }

  return <div>
      <input
        type = "text"
        value = {inputText}
        onChange = {(event) => setInputText(event.target.value)}
        onKeyDown = {handleKeyDown}
        placeholder = "Type Here"
      />
      <button onClick= {callGet}>Test GET</button>
      <h2>{message}</h2>
  </div>
}

export default App;