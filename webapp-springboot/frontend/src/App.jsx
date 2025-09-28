import { useState, useEffect } from 'react'; // Añadimos useEffect para manejar onAuthStateChanged
import appFirebase from "../src/credenciales.js";
import { getAuth, onAuthStateChanged } from 'firebase/auth';
import './App.css';
import Navbar from './components/Navbar'; // Ajusta la ruta si está en otra carpeta
import Login from '../src/components/Login.jsx';
import Home from '../src/components/Home.jsx';

const auth = getAuth(appFirebase);

function App() {
  const [usuario, setUsuario] = useState(null);

  useEffect(() => {
    // useEffect para evitar memory leaks con onAuthStateChanged
    const unsubscribe = onAuthStateChanged(auth, (usuarioFirebase) => {
      if (usuarioFirebase) {
        setUsuario(usuarioFirebase);
      } else {
        setUsuario(null);
      }
    });
    return () => unsubscribe(); // Limpia el listener al desmontar
  }, []);

  return (
    <div>
      <Navbar usuario={usuario} /> {/* Pasamos el estado del usuario al Navbar */}
      {usuario ? <Home correoUsuario={usuario.email} /> : <Login />}
    </div>
  );
}

export default App;
