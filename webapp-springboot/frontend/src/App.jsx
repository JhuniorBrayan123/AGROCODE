import { useState } from 'react'
//importado firebase
import appFirebase from "../src/credenciales.js"
import { getAuth, onAuthStateChanged } from 'firebase/auth'
const auth = getAuth(appFirebase)

import './App.css'

import Login from '../src/components/Login.jsx'
import Home from '../src/components/Home.jsx' 

function App() {
  const [usuario, setUsuario] = useState(null)
  onAuthStateChanged(auth, (usuarioFirebase) => {
    if (usuarioFirebase) {
      // Usuario ha iniciado sesión
      setUsuario(usuarioFirebase)
    }else {
      // Usuario no ha iniciado sesión
      setUsuario(null)
    }
  })

  return (
    <div>
      {usuario ? <Home correoUsuario = {usuario.email} /> : <Login />}
    </div>
  )
   
  
}

export default App
