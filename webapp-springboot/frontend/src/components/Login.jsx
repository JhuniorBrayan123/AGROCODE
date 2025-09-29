import React, { useState, useEffect } from "react";
import ImagenProfile from "../assets/Login.jpg"; // Tu imagen de perfil
import appFirebase from "../credenciales";
import {
  getAuth,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
} from "firebase/auth";

// --- IMPORTO LAS IMÁGENES PARA EL FONDO DINÁMICO ---
import fondo1 from "../assets/campo1.jpg";
import fondo2 from "../assets/tractor.jpg";
import fondo3 from "../assets/plantas.jpg";
import fondo4 from "../assets/cosecha.jpg";
// Puedes añadir más imágenes aquí
const backgroundImages = [fondo1, fondo2, fondo3, fondo4];
// --------------------------------------------------

const auth = getAuth(appFirebase);

const Login = () => {
  const [registrando, setRegistrando] = useState(false);
  
  // --- AÑADIMOS EL ESTADO PARA LA IMAGEN DE FONDO ---
  const [currentBackgroundIndex, setCurrentBackgroundIndex] = useState(0);

  // --- EFECTO PARA CAMBIAR LA IMAGEN DE FONDO CADA CIERTO TIEMPO ---
  useEffect(() => {
    const intervalId = setInterval(() => {
      // Avanza al siguiente índice, volviendo al inicio si llega al final
      setCurrentBackgroundIndex(prevIndex => 
        (prevIndex + 1) % backgroundImages.length
      );
    }, 8000); // Cambia la imagen cada 8 segundos (8000 ms)

    // Limpieza: Detiene el intervalo cuando el componente se desmonta
    return () => clearInterval(intervalId);
  }, []); // El array vacío asegura que solo se ejecute al montar y desmontar

  const functAutenticacion = async (e) => {
    e.preventDefault();
    const correo = e.target.email.value;
    const contraseña = e.target.password.value;

    try {
      if (registrando) {
        await createUserWithEmailAndPassword(auth, correo, contraseña);
      } else {
        await signInWithEmailAndPassword(auth, correo, contraseña);
      }
    } catch (error) {
      console.error("Error en autenticación:", error.message);
      alert("Error: " + error.message);
    }
  };

  return (
    // APLICAMOS LA IMAGEN DE FONDO USANDO ESTILOS INLINE Y LA VARIABLE CSS
    <div 
      className="container"
      // Pasamos la URL de la imagen actual a la variable CSS --bg-image
      style={{ '--bg-image': `url(${backgroundImages[currentBackgroundIndex]})` }}
    > 
      <div className="row">
        <div className="col-md-12"> 
          <div className="padre">
            <div className="card card-body shadow-lg login-box">
              <img src={ImagenProfile} alt="Perfil" className="estilo-profile" />
              <form onSubmit={functAutenticacion}>
                <input
                  type="text"
                  placeholder="Ingresar Email"
                  className="cajatexto"
                  id="email"
                />
                <input
                  type="password"
                  placeholder="Ingresar contraseña"
                  className="cajatexto"
                  id="password"
                />
                <button className="btn-form">
                  {registrando ? "Registrarme" : "Iniciar sesión"}
                </button>
              </form>

              <h4 className="texto-registrar">
                {registrando
                  ? "Si ya tienes cuenta, inicia sesión"
                  : "¿No tienes cuenta? Regístrate aquí"}
                <button 
                  onClick={() => setRegistrando(!registrando)}
                  className="btnswicht"
                >
                  {registrando ? "Inicia Sesión" : "Registrar"}
                </button>
              </h4>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;