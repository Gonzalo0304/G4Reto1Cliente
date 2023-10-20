/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author David
 */
public class ControllerSignIn {

    @FXML
    private Button btnIniciarSesion;
    private TextField txtFieldEmail;
    private TextField txtFieldPass;
    private Hyperlink hplNoCuenta;

    public void iniciarSesion() {
        String texto = txtFieldEmail.getText().toString();
        String pass = txtFieldPass.getText().toString();
        if (texto.isEmpty()) {
            System.out.println("correo no bien");
        }

        if (pass.isEmpty()) {
            System.out.println("passwd bien");
        }
    }

    public void noCuenta() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signUp.fxml"));

            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setScene(scene);

            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(ControllerSignIn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}