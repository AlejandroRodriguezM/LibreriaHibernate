/**
 * Contiene las clases que hacen funcionar las ventanas
 *  
*/
package org.apache.maven.Controladores;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.maven.Funcionamiento.FuncionesApis;
import org.apache.maven.Funcionamiento.FuncionesFicheros;
import org.apache.maven.Funcionamiento.Utilidades;
import org.apache.maven.Funcionamiento.Ventanas;
import org.apache.maven.alarmas.AlarmaList;
import org.apache.maven.configuracionHibernate.GuardarConfiguracion;
import org.apache.maven.dbmanager.ConectManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Esta clase sirve para acceder a la base de datos y poder realizar diferentes
 * funciones.
 *
 * @author Alejandro Rodriguez
 */
public class AccesoBBDDController implements Initializable {

	/**
	 * Etiqueta para mostrar una alarma relacionada con la conexión.
	 */
	@FXML
	private Label alarmaConexion;

	/**
	 * Etiqueta para mostrar una alarma relacionada con la conexión a Internet.
	 */
	@FXML
	private Label alarmaConexionInternet;

	/**
	 * Etiqueta para mostrar una alarma relacionada con la conexión a la base de
	 * datos SQL.
	 */
	@FXML
	private Label alarmaConexionSql;

	/**
	 * Botón para acceder a la base de datos.
	 */
	@FXML
	private Button botonAccesobbdd;

	/**
	 * Botón para acceder a la base de datos en línea.
	 */
	@FXML
	private Button botonAccesobbddOnline;

	/**
	 * Botón para enviar datos.
	 */
	@FXML
	private Button botonEnviar;

	/**
	 * Botón para acceder a las opciones.
	 */
	@FXML
	private Button botonOpciones;

	/**
	 * Botón para salir de la aplicación.
	 */
	@FXML
	private Button botonSalir;

	/**
	 * Casilla de verificación para recordar la configuración.
	 */
	@FXML
	private CheckBox checkRecordar;

	/**
	 * Etiqueta para mostrar el servidor.
	 */
	@FXML
	private Label labelServidor;

	/**
	 * Etiqueta para mostrar el nombre de usuario.
	 */
	@FXML
	private Label nomUsuarioLabel;

	/**
	 * Campo de texto para ingresar el nombre de usuario.
	 */
	@FXML
	private TextField nomUsuarioText;

	/**
	 * Etiqueta para mostrar la contraseña del usuario.
	 */
	@FXML
	private Label passUsuarioLabel;

	/**
	 * Campo de contraseña para ingresar la contraseña del usuario.
	 */
	@FXML
	private PasswordField passUsuarioText;

	/**
	 * Etiqueta para mostrar el estado de la conexión.
	 */
	@FXML
	private Label prontEstadoConexion;

	/**
	 * Botón de alternancia para cambiar entre dos estados.
	 */
	@FXML
	private ToggleButton toogleButton;

	/**
	 * Botón de alternancia para mostrar u ocultar la contraseña.
	 */
	@FXML
	private ToggleButton toggleEye;

	/**
	 * Vista de imagen para el botón de alternancia para mostrar u ocultar la
	 * contraseña.
	 */
	@FXML
	private ImageView toggleEyeImageView;

	/**
	 * Campo de texto para ingresar la contraseña del usuario.
	 */
	@FXML
	private TextField passUsuarioTextField;
	

	/**
	 * Estado del botón de alternancia del ojo.
	 */
	private boolean estadoOjo = false;

	/**
	 * Objeto para gestionar las ventanas de navegación.
	 */
	private static Ventanas nav = new Ventanas();

	private static AlarmaList alarmaList = new AlarmaList();

	private static ExecutorService executorService;

	/**
	 * Inicializa el controlador cuando se carga la vista.
	 *
	 * @param location  la ubicación del archivo FXML
	 * @param resources los recursos utilizados por la vista
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		FuncionesApis.guardarDatosClavesMarvel();

		FuncionesApis.guardarApiComicVine();
		
//		try {
//			GuardarConfiguracion.guardarConfiguracion();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		alarmaList.setAlarmaConexion(alarmaConexion);
		alarmaList.setAlarmaConexionInternet(alarmaConexionInternet);
		alarmaList.setAlarmaConexionSql(alarmaConexionSql);
		alarmaList.setAlarmaConexionPrincipal(prontEstadoConexion);

		FuncionesApis.comprobarApisComics();

		ConectManager.asignarValoresPorDefecto();

		Platform.runLater(() -> {
			alarmaList.iniciarThreadChecker(false);
			if (Utilidades.isInternetAvailable()) {
				Utilidades.cargarTasasDeCambioDesdeArchivo();
			}
			// Crear estructura si no existe
			FuncionesFicheros.crearEstructura();
			Utilidades.crearCarpeta();
			ConectManager.closeConnection();
		});
	}

	/**
	 * Maneja el evento de entrar en línea. Guarda el usuario y contraseña si se
	 * seleccionó la opción "Recordar".
	 *
	 * @param event El evento de acción que desencadenó la función.
	 * @throws IOException Si ocurre un error de entrada/salida.
	 */
	@FXML
	void entrarOnline(ActionEvent event) throws IOException {

		if (checkRecordar.isSelected()) {
			String valorPass;
			if (estadoOjo) {
				valorPass = passUsuarioTextField.getText();
				passUsuarioText.setText(valorPass);
			} else {
				valorPass = passUsuarioText.getText();
				passUsuarioTextField.setText(valorPass);
			}

			Utilidades.guardarUsuario(nomUsuarioText, valorPass);
		}
	}

	/**
	 * Configura el formulario en línea con la información almacenada en el archivo
	 * de configuración.
	 */
	public void formulario_online() {

		Map<String, String> datosConfiguracion = FuncionesFicheros.devolverDatosConfig();

		String usuarioTexto = datosConfiguracion.get("Usuario");
		String passwordTexto = datosConfiguracion.get("Password");

		nomUsuarioText.setText(usuarioTexto);

		passUsuarioText.setText(passwordTexto);

		checkRecordar.setSelected(true); // Marcar el CheckBox

	}

	/**
	 * Funcion que permite el acceso a la ventana de menuPrincipal
	 *
	 * @param event
	 */
	@FXML
	void entrarMenu(ActionEvent event) {

		if (!Utilidades.isMySQLServiceRunning(ConectManager.DB_HOST, ConectManager.DB_PORT)) {
			AlarmaList.detenerAnimacion();
			AlarmaList.iniciarAnimacionConexion(prontEstadoConexion);
			AlarmaList.iniciarAnimacionAlarma(alarmaConexion);
			return;
		}
		
		
		try {
			GuardarConfiguracion.guardarConfiguracion();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (ConectManager.estadoConexion) { // Siempre que el metodo de la clase DBManager sea true, permitira acceder
			ConectManager.resetConnection();
			// al menu principal
			nav.verMenuPrincipal();
			closeWindow();
		} else { // En caso contrario mostrara el siguiente mensaje.
			AlarmaList.detenerAnimacion();
			ConectManager.asignarValoresPorDefecto();
			prontEstadoConexion.setStyle("-fx-background-color: #DD370F");
			AlarmaList.iniciarAnimacionConexion(prontEstadoConexion);
		}
	}

	/**
	 * Maneja el evento de enviar datos a la base de datos.
	 *
	 * @param event El evento de acción que desencadenó la función.
	 * @throws SQLException
	 */
	@FXML
	void enviarDatos(ActionEvent event) throws SQLException {

		String[] datosFichero = FuncionesFicheros.datosEnvioFichero();

		if (configurarConexion()) {
			ConectManager.datosBBDD(datosFichero);
			if (ConectManager.isConnected()) {

				AlarmaList.detenerAnimacion();
				AlarmaList.iniciarAnimacionConectado(prontEstadoConexion);
				AlarmaList.manejarConexionExitosa(datosFichero, prontEstadoConexion);
			} else {
				AlarmaList.manejarErrorConexion("No estás conectado a la base de datos.", prontEstadoConexion);
			}
		}
	}

	/**
	 * Configura la conexión a la base de datos.
	 * 
	 * @return true si la configuración es exitosa, false de lo contrario.
	 */
	private boolean configurarConexion() {

		if (!ConectManager.loadDriver()) {
			return false;
		}

		return true;
	}

	public Scene miStageVentana() {

		Scene scene = botonEnviar.getScene();
		return scene;

	}

	@FXML
	void reActivarConexion(ActionEvent event) {
		Task<Boolean> iniciarXAMPPTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {

				// Realizar las operaciones de inicio de XAMPP aquí
				return Utilidades.iniciarXAMPP();
			}
		};

		iniciarXAMPPTask.setOnSucceeded(e -> {

			// Cerrar el hilo después de completar la tarea
			if (!executorService.isShutdown()) {
				executorService.shutdown();
			}

		});

		// Iniciar la tarea en un nuevo hilo utilizando un ExecutorService
		executorService = Executors.newSingleThreadExecutor();
		executorService.submit(iniciarXAMPPTask);
	}

	/**
	 * Permite salir completamente del programa.
	 *
	 * @param event
	 */
	@FXML
	public void salirPrograma(ActionEvent event) {

		if (nav.salirPrograma(event)) { // Llamada a metodo que permite salir completamente del programa
			Stage myStage = (Stage) this.botonSalir.getScene().getWindow();
			myStage.close();
		}
	}

	/**
	 * Método que se ejecuta al hacer clic en el botón de opciones del programa.
	 * Abre la ventana de opciones y cierra la ventana actual.
	 */
	@FXML
	void opcionesPrograma(ActionEvent event) {

		nav.verOpciones();

		Stage myStage = (Stage) this.botonOpciones.getScene().getWindow();
		myStage.close();
	}

	/**
	 * Cierra el programa a la fuerza correctamente.
	 */
	public void closeWindow() { // Metodo que permite cerrar completamente el programa en caso de cerrar a la //
		// fuerza.
		Stage myStage = (Stage) this.botonEnviar.getScene().getWindow();
		myStage.close();
	}
}
