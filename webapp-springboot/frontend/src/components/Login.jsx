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
  const functAutenticacion = async (e) => {
    e.preventDefault();
    const correo = e.target.email.value;
    const contraseña = e.target.password.value;
    

    if (registrando) {
      await createUserWithEmailAndPassword(auth, correo, contraseña)
    }else {
      await signInWithEmailAndPassword(auth, correo, contraseña)
    }
  };

  return (
    <div className="container">
      <div className="row">
        <div className="col-md-4">
          <div className="padre">
            <div className="card card-body shadow-lg">
              <img src={ImagenProfile} alt="" className="estilo.profile" />
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
                <button onClick={() => setRegistrando(!registrando)}>
                  {registrando ? "Inicia Sesión" : "Registrar"}
                </button>
              </h4>
            </div>
          </div>
        </div>

        <div className="col-md-8">
          <img src={Imagen} alt="" className="tamaño-imagen" />
        </div>
      </div>
    </div>
  );
};

export default Login;
