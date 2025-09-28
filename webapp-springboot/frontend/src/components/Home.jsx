import React, {useState} from "react";

const Home = ({correoUsuario}) => {
    return (
        <div>
            <h1>Bienvenido a la página de inicio {correoUsuario}</h1>
        </div>
    );
}

export default Home;