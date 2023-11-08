/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import clases.Mensaje;
import clases.MessageEnum;
import clases.Usuario;
import excepciones.CredentialsException;
import excepciones.InvalidCPFormat;
import excepciones.NotCompleteException;
import excepciones.InvalidEmailFormat;
import excepciones.InvalidPassFormat;
import excepciones.PassDontMatch;
import excepciones.InvalidNameLength;
import excepciones.InvalidTlfFormat;
import excepciones.ServerErrorException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import modelo.Implementacion;
import modelo.InterfazCliente;
import sockets.ClienteSocket;

/**
 *
 * @author David, Gonzalo.
 */
public class ControllerSignUp {

    Stage stage;
    private static final Logger logMsg = Logger.getLogger("");
    Image oAbierto = new Image(getClass().getResource("/view/ojoAbierto.png").toExternalForm());
    Image oCerrado = new Image(getClass().getResource("/view/ojoCerrado.png").toExternalForm());

    @FXML
    private Button btnRegistrarse;
    @FXML
    private TextField txtFieldEmailR;
    @FXML
    private TextField txtFieldNombre;
    @FXML
    private TextField txtFieldDireccion;
    @FXML
    private TextField txtFieldCodigoPostal;
    @FXML
    private TextField txtFieldTelefono;
    @FXML
    private PasswordField passFieldR;
    @FXML
    private PasswordField passFieldR1;
    @FXML
    private TextField txtFieldPassR;
    @FXML
    private TextField txtFieldPassR1;
    @FXML
    private ImageView ivMostrarPassR;
    @FXML
    private ImageView ivMostrarPassR1;
    @FXML
    private Hyperlink hplNoCuenta;
    @FXML
    private ToggleButton tbMostrarPassR;
    @FXML
    private ToggleButton tbMostrarPassR1;

    /**
     * Mostrar la ventana. La venta debe ser Modal La ventana no debe ser
     * redimensionable. Todo debe estar habilitado. Establecer el focus en campo
     * del email (txtFieldEmail) El botón por defecto debe ser el botón de
     * Iniciar sesión (btnIniciarSesion) Establecer el título de ventana al
     * valor: “Sign In”.
     *
     * @param root
     */
    public void initStage(Parent root) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("SignUp");
        stage.setResizable(false);
        txtFieldEmailR.requestFocus();
        btnRegistrarse.setDefaultButton(true);
        txtFieldPassR.setFocusTraversable(false);
        txtFieldPassR1.setFocusTraversable(false);
        btnRegistrarse.setOnAction(this::signUp);
        hplNoCuenta.setOnAction(this::openSignIn);
        tbMostrarPassR.setOnAction(this::handleShowPassR);
        tbMostrarPassR1.setOnAction(this::handleShowPassR1);
        stage.setOnCloseRequest(this::closeRequest);
        stage.show();
    }

    /**
     * Hyperlink: abrir la ventana signUp con el metodo openSignUp y cerrar la
     * ventana signIn.
     *
     * @param event
     */
    @FXML
    public void openSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/signIn.fxml"));
            Parent root = (Parent) loader.load();
            ControllerSignIn viewController
                    = ((ControllerSignIn) loader.getController());
            viewController.setStage(stage);
            viewController.initStage(root);
        } catch (IOException ex) {
            Logger.getLogger(ControllerSignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Si se cumplen todas las condiciones se llamará a un método de lógica
     * llamado SingUp en la capa de lógica y enviará los datos introducidos al
     * lado servidor para registrarlos: Si el registro no se a podido completar
     * lanzar una excepcion con invalidSignUp con el texto “Email repetido” Si
     * el registro resulta exitoso mostrar un aviso con el texto “Registro
     * realizado correctamente”. Una vez se cierre el aviso, cerrar la ventana
     * Sign Up y abrir ventana de singIn. Si se produce cualquier excepción en
     * el proceso, mostrar un mensaje al usuario con el texto de la excepción.
     *
     * @param event
     */
    @FXML
    public void signUp(ActionEvent event) {
        ClienteSocket csk = new ClienteSocket();
        try {
            if (tbMostrarPassR.isSelected()) {
                passFieldR.setText(txtFieldPassR.getText());
            }
            if (tbMostrarPassR1.isSelected()){
                passFieldR1.setText(txtFieldPassR1.getText());
            }
            checkCompleteFields();
            checkValidEmail();
            checkNameLength();
            checkPassFormat();
            checkPassMatch();
            checkCPFormat();
            checkTlfFormat();
            Usuario user = new Usuario();
            user.setEmail(txtFieldEmailR.getText());
            user.setNombre(txtFieldNombre.getText());
            user.setPass(passFieldR.getText());
            user.setTelefono(Integer.parseInt(txtFieldTelefono.getText()));
            user.setCodigoPostal(Integer.parseInt(txtFieldCodigoPostal.getText()));
            user.setDireccion(txtFieldDireccion.getText());
            user.setCreacion(LocalDate.now());

            MessageEnum action = MessageEnum.SIGNUP;
            Mensaje mensaje = new Mensaje(user, action);

            Mensaje msj2 = csk.signUp(mensaje);
            switch (msj2.getMessageEnum()) {
                case OK:
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Creación de Usuario realizada correctamente.", ButtonType.OK);
                    alert.show();
                    abrirSignIn();
                    break;
                case ERRORSIGNUP:
                    throw new CredentialsException("Las credenciales no coinciden.");
                case ERRORSERVER:
                    throw new ServerErrorException("Error del server.");
                default:
                    System.out.println("Creacion correcta");
            }

        } catch (NotCompleteException | InvalidEmailFormat | InvalidNameLength | InvalidPassFormat | PassDontMatch | InvalidTlfFormat | InvalidCPFormat ex) {
            Logger.getLogger(ControllerSignUp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CredentialsException ex) {
            Logger.getLogger(ControllerSignUp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServerErrorException ex) {
            Logger.getLogger(ControllerSignUp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Validar que todos los textfield estén informados mediante el método
     * checkCompleteFields. Si no están informados lanzar una excepción
     * notCompleteException con el texto “Los campos no están informados”.
     *
     * @throws NotCompleteException
     */
    private void checkCompleteFields() throws NotCompleteException {
            if (txtFieldEmailR.getText().isEmpty() || passFieldR.getText().isEmpty() || passFieldR1.getText().isEmpty() || txtFieldNombre.getText().isEmpty() || txtFieldDireccion.getText().isEmpty() || txtFieldCodigoPostal.getText().isEmpty() || txtFieldTelefono.getText().isEmpty()) {
                logMsg.log(Level.INFO, "Registro Incorrecto");
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor rellene todos los campos", ButtonType.OK);
                alert.show();
                throw new NotCompleteException("Los campos no están informados");
            }
        
    }

    /**
     * Validar que la dirección de email contenga un @ y al menos un punto
     * mediante el método de lógica checkValidEmail. Implementar en este
     * expresiones regulares, la clase Pattern de Java y el método match. Si no
     * se cumplen dichas condiciones lanzar un excepción invalidEmailFormat con
     * el texto “La dirección de email debe tener un @ y al menos un punto” . *
     *
     * Validar que el correo escrito tenga un mínimo de tres caracteres antes
     * del @ mediante el método de lógica checkValidEmail. Implementar en este
     * expresiones regulares, la clase Pattern de Java y el método match. Si no
     * se cumplen dicho número de caracteres lanzar una excepción
     * invalidEmailFormat con el texto “La dirección de email debe tener al
     * menos tres caracteres antes del @”
     *
     * @throws InvalidEmailFormat
     */
    private void checkValidEmail() throws InvalidEmailFormat {
        Pattern pattern1 = Pattern.compile("^[a-zA-Z0-9._]+@[a-zA-Z0-9]+\\.[a-zA-Z]{2,}$");
        Pattern pattern2 = Pattern.compile("^[a-zA-Z0-9._]{3,}+@(.+)$");

        Matcher matcher = pattern1.matcher(txtFieldEmailR.getText());
        if (matcher.find() == false) {
            Logger.getLogger("");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor introduzca un correo valido", ButtonType.OK);
            alert.show();
            throw new InvalidEmailFormat("La dirección de email debe tener un @ y al menos un punto");

        }

        matcher = pattern2.matcher(txtFieldEmailR.getText());
        if (matcher.find() == false) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor introduzca un correo que contenga 3 caracteres antes del @", ButtonType.OK);
            alert.show();
            throw new InvalidEmailFormat("La dirección de email debe tener al menos tres caracteres antes del @");
        }

    }

    /**
     * Validar que la longitud del nombre tenga un mínimo de 3 caracteres con el
     * método de lógica checkNameLength. Si no cumple dicho mínimo de
     * caracteres, lanzar una excepción invalidNameLength con el texto “El
     * usuario debe tener al menos tres caracteres”.
     *
     * @throws InvalidNameLength
     */
    private void checkNameLength() throws InvalidNameLength {
        Pattern pattern = Pattern.compile("^[a-zA-Z]{3,}+\\s[a-zA-Z]+\\s[a-zA-Z]+$");
        Matcher matcher = pattern.matcher(txtFieldNombre.getText());
        if (matcher.find() == false) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor un nombre valido", ButtonType.OK);
            alert.show();
            throw new InvalidNameLength("El nombre debe tener al menos tres caracteres");

        }
    }

    /**
     * Validar que la contraseña tenga un mínimo de 8 caracteres con el método
     * de lógica checkPassFormat. Si no cumple dicho mínimo, lanzar una
     * excepción invalidPassFormat con el texto “La contraseña debe tener al
     * menos 8 caracteres”. Validar que la contraseña tenga una letra y un
     * número mediante el método de lógica checkPassFormat. Si no se cumple,
     * lanzar una excepción invalidPassFormat con el texto “La contraseña debe
     * tener al menos una letra y un número”.
     *
     * @throws InvalidPassFormat
     */
    private void checkPassFormat() throws InvalidPassFormat {
        Pattern pattern1 = Pattern.compile("^(.+).{7,}");
        Pattern pattern2 = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9]).");
        Matcher matcher = pattern1.matcher(passFieldR.getText());
        if (matcher.find() == false) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor introduce una contraseña valida con 8 caracteres un numero y una letra minimo", ButtonType.OK);
            alert.show();
            throw new InvalidPassFormat("La contraseña debe tener al menos 8 caracteres");
        }

        matcher = pattern2.matcher(passFieldR.getText());
        if (matcher.find() == false) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor introduce una contraseña valida con 8 caracteres un numero y una letra minimo", ButtonType.OK);
            alert.show();
            throw new InvalidPassFormat("La contraseña debe tener al menos una letra y un número");
        }
    }

    /**
     * Comprobar que la primera contraseña proporcionada y la segunda coinciden
     * con el método de lógica checkPassMatch. Si no coinciden, lanzar una
     * excepción passDontMatch con el texto “Las contraseñas proporcionadas no
     * coinciden”.
     *
     * @throws PassDontMatch
     */
    private void checkPassMatch() throws PassDontMatch {
        String pass1 = passFieldR.getText();
        String pass2 = passFieldR1.getText();

        if (!pass1.equals(pass2)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor que las contraseñas coinciden", ButtonType.OK);
            alert.show();
            throw new PassDontMatch("Las contraseñas proporcionadas no coinciden");
        }
    }

    /**
     * Comprobar que el número de teléfono introducido en txtFieldTelefono se
     * compone de 9 dígitos numéricos con el método de lógica checkTlfFormat. Si
     * las condiciones no se cumplen, lanzar una excepción invalidTlfFormat con
     * el texto “Por favor, introduce un número de teléfono de al menos 9
     * dígitos”.
     *
     * @throws InvalidTlfFormat
     */
    private void checkTlfFormat() throws InvalidTlfFormat {
        Pattern pattern = Pattern.compile("^[0-9]{9}$");
        Matcher matcher = pattern.matcher(txtFieldTelefono.getText());
        if (matcher.find() == false) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor introduzca un numero de telefono valido");
            alert.show();
            throw new InvalidTlfFormat("Por favor, introduce un número de teléfono de al menos 9 dígitos");
        }
    }

    /**
     * Comprobar que el texto introducido en txtFieldCodigoPostal se compone de
     * 5 dígitos numéricos con el método de lógica checkCPFormat. Si las
     * condiciones no se cumplen, lanzar una excepción invalidCPFormat con el
     * texto “Por favor, introduce un código postal de cinco dígitos”.
     *
     * @throws InvalidCPFormat
     */
    private void checkCPFormat() throws InvalidCPFormat {
        Pattern pattern = Pattern.compile("^[0-9]{5}$");
        Matcher matcher = pattern.matcher(txtFieldCodigoPostal.getText());
        if (matcher.find() == false) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error de registro: \nPorfavor introduzca un codigo postal valido");
            alert.show();
            throw new InvalidCPFormat("“Por favor, introduce un código postal de cinco dígitos");
        }
    }

    private void closeRequest(WindowEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Estas seguro de que quieres cerrar la aplicacion?",
                ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            InterfazCliente inter = new Implementacion();
            inter.closeApli();
        } else {
            event.consume();
        }
    }

    public void handleOpenLogIn(ActionEvent actionEvent) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signIn.fxml"));

            Parent root = loader.load();

            ControllerSignIn viewController = ((ControllerSignIn) loader.getController());
            viewController.setStage(stage);
            viewController.initStage(root);

        } catch (IOException ex) {
            Logger.getLogger(ControllerSignIn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void handleShowPassR(ActionEvent actionEvent) {
        if (tbMostrarPassR.isSelected()) {
            txtFieldPassR.setVisible(true);
            txtFieldPassR.setFocusTraversable(true);
            txtFieldPassR.requestFocus();
            txtFieldPassR.positionCaret(txtFieldPassR.getText().length());
            passFieldR.setVisible(false);
            passFieldR.setFocusTraversable(false);
            txtFieldPassR.setText(passFieldR.getText());
            ivMostrarPassR.setImage(oAbierto);
        } else {
            txtFieldPassR.setVisible(false);
            txtFieldPassR.setFocusTraversable(false);
            passFieldR.setVisible(true);
            passFieldR.setFocusTraversable(true);
            passFieldR.requestFocus();
            passFieldR.positionCaret(0);
            passFieldR.setText(txtFieldPassR.getText());
            ivMostrarPassR.setImage(oCerrado);
        }
    }

    @FXML
    public void handleShowPassR1(ActionEvent actionEvent) {
        if (tbMostrarPassR1.isSelected()) {
            txtFieldPassR1.setVisible(true);
            txtFieldPassR1.setFocusTraversable(true);
            txtFieldPassR1.requestFocus();
            passFieldR1.setVisible(false);
            passFieldR1.setFocusTraversable(false);
            txtFieldPassR1.setText(passFieldR1.getText());
            ivMostrarPassR1.setImage(oAbierto);
        } else {
            txtFieldPassR1.setVisible(false);
            txtFieldPassR1.setFocusTraversable(false);
            passFieldR1.setVisible(true);
            passFieldR1.setFocusTraversable(true);
            passFieldR1.requestFocus();
            passFieldR1.setText(txtFieldPassR1.getText());
            ivMostrarPassR1.setImage(oCerrado);
        }

    }

    private void abrirSignIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signIn.fxml"));

            Parent root = loader.load();

            ControllerSignIn viewController = ((ControllerSignIn) loader.getController());
            viewController.setStage(stage);
            viewController.initStage(root);
        } catch (IOException ex) {
            Logger.getLogger(ControllerSignIn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
