import React, { useState } from "react";
import Imagen from "../assets/Login.jpg"; // revisa si es .jpg o .png
import ImagenProfile from "../assets/profile.jpeg";
import appFirebase from "../credenciales";
import {
  getAuth,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
} from "firebase/auth";

const auth = getAuth(appFirebase);

const Login = () => {
  const [registrando, setRegistrando] = useState(false);

  return (
    <div className="container">
      <div className="row">
        <div className="col-md-4"></div>
        <div className="padre">
          <div className="card-body shadow-lg">
            <img src={ImagenProfile} alt="" className="estilo.profile" />
            <form>
              <input
                type="text"
                placeholder="Ingresar Email"
                className="cajatexto"
              />
              <input
                type="password"
                placeholder="Ingresar contraseña"
                className="cajatexto"
              />
              <button className="btn-form">
                {registrando ? "Registrarme" : "Iniciar sesión"}
              </button>
            </form>

            <h4>
              {registrando
                ? "Si ya tienes cuenta, inicia sesión"
                : "¿No tienes cuenta? Regístrate aquí"}
            </h4>

            <button>{registrando ? "Inicia Sesión" : "Registrar"}</button>
          </div>
        </div>

        <div className="col-md-4">
          <img src={Imagen} alt="" className="tamaño-imagen" />
        </div>
      </div>
    </div>
  );
};

export default Login;
