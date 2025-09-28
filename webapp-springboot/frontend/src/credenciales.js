// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyAcsLTVxERe620E4wwhD0y7RBXL8bL08EM",
  authDomain: "webapp-15b99.firebaseapp.com",
  projectId: "webapp-15b99",
  storageBucket: "webapp-15b99.firebasestorage.app",
  messagingSenderId: "899394966749",
  appId: "1:899394966749:web:a1d006ec602b4988d7b734",
};

// Initialize Firebase
const appFirebase = initializeApp(firebaseConfig);
export default appFirebase;
