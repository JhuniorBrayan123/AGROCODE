import React from 'react';
import './Navbar.css';
import LogoPlanta from '../assets/logo-planta.svg.svg'; // Ruta relativa corregida

const Navbar = () => {
  return (
    <nav className="navbar">
      <div className="navbar-logo">
        <img src={LogoPlanta} alt="Logo IBS - Planta" className="logo-icon" />
        <span className="logo-text">IBS</span>
      </div>
      <div className="navbar-actions">
        <input type="text" placeholder="Buscar" className="search-input" />
        <a href="/login" className="btn">Iniciar Sesión</a>
        <a href="/register" className="btn">Registrarse</a>
        <></>
      </div>
    </nav>
  );
};

export default Navbar;