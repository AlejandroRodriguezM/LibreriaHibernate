/**
 * Contiene las clases que hacen funcionar las ventanas
 *  
*/
package org.apache.maven.Controladores;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.maven.Funcionamiento.FuncionesComboBox;
import org.apache.maven.Funcionamiento.FuncionesFicheros;
import org.apache.maven.Funcionamiento.Utilidades;
import org.apache.maven.Funcionamiento.Ventanas;
import org.apache.maven.alarmas.AlarmaList;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Esta clase sirve para configurar los datos de la base de datos para que el
 * programa pueda operar correctamente
 *
 * @author Alejandro Rodriguez
 */
public class OpcionesDatosController implements Initializable {

	/**
	 * Etiqueta de alarma para la conexión.
	 */
	@FXML
	private Label alarmaConexion;

	/**
	 * Etiqueta de alarma para la conexión a Internet.
	 */
	@FXML
	private Label alarmaConexionInternet;

	/**
	 * Etiqueta de alarma para la conexión a la base de datos SQL.
	 */
	@FXML
	private Label alarmaConexionSql;

	/**
	 * Botón para crear una nueva base de datos.
	 */
	@FXML
	private Button botonCrearBBDD;

	/**
	 * Botón para salir de la aplicación.
	 */
	@FXML
	private Button botonSalir;

	/**
	 * Botón para volver atrás.
	 */
	@FXML
	private Button botonVolver;

	/**
	 * Botón para abrir un archivo.
	 */
	@FXML
	private Button boton_abrir;

	/**
	 * Botón para guardar un archivo.
	 */
	@FXML
	private Button boton_guardar;

	/**
	 * Botón para restaurar un archivo.
	 */
	@FXML
	private Button boton_restaurar;

	/**
	 * Botón para descargar la base de datos.
	 */
	@FXML
	private Button botonDescargaBBDD;

	/**
	 * Etiqueta para mostrar el nombre del host.
	 */
	@FXML
	private Label etiquetaHost;

	/**
	 * Selector para el nombre de la base de datos.
	 */
	@FXML
	private ComboBox<String> nombreBBDD;

	/**
	 * Campo de texto para ingresar el nombre del host.
	 */
	@FXML
	private TextField nombreHost;

	/**
	 * Etiqueta para mostrar el nombre.
	 */
	@FXML
	private Label nombre_label;

	/**
	 * Campo de contraseña.
	 */
	@FXML
	private PasswordField pass;

	/**
	 * Campo de texto para ingresar la contraseña del usuario.
	 */
	@FXML
	private TextField passUsuarioTextField;

	/**
	 * Etiqueta para mostrar la contraseña.
	 */
	@FXML
	private Label password_label;

	/**
	 * Etiqueta para mostrar el estado del fichero.
	 */
	@FXML
	private Label prontEstadoFichero;

	/**
	 * Etiqueta para mostrar el puerto.
	 */
	@FXML
	private Label puerto_label;

	/**
	 * Campo de texto para ingresar el puerto de la base de datos.
	 */
	@FXML
	private TextField puertobbdd;

	/**
	 * Botón para mostrar u ocultar la contraseña.
	 */
	@FXML
	private ToggleButton toggleEye;

	/**
	 * Imagen del ojo para mostrar u ocultar la contraseña.
	 */
	@FXML
	private ImageView toggleEyeImageView;

	/**
	 * Campo de texto para ingresar el nombre de usuario.
	 */
	@FXML
	private TextField usuario;

	/**
	 * Etiqueta para mostrar el nombre de usuario.
	 */
	@FXML
	private Label usuario_label;

	/**
	 * Instancia de la clase Ventanas para la navegación.
	 */
	private static Ventanas nav = new Ventanas();

	/**
	 * Inicializa el controlador cuando se carga la vista.
	 *
	 * @param location  la ubicación del archivo FXML
	 * @param resources los recursos utilizados por la vista
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		AlarmaList alarmaList = new AlarmaList();

		alarmaList.setAlarmaConexion(alarmaConexion);
		alarmaList.setAlarmaConexionInternet(alarmaConexionInternet);
		alarmaList.setAlarmaConexionSql(alarmaConexionSql);

		alarmaList.iniciarThreadChecker(false);

		FuncionesFicheros.crearEstructura();
		AlarmaList.configureEyeToggle(toggleEyeImageView, passUsuarioTextField, pass);
		restringir_entrada_datos();

		formulario_local();
		actualizarDataBase();

		AlarmaList.iniciarAnimacionEspera(prontEstadoFichero);
		AlarmaList.iniciarAnimacionAlarma(alarmaConexion);
	}

	/**
	 * Llena el formulario de configuración local con valores previamente guardados.
	 */
	public void formulario_local() {

		Map<String, String> datosConfiguracion = FuncionesFicheros.devolverDatosConfig();

		usuario.setText(datosConfiguracion.get("Usuario"));

		pass.setText(datosConfiguracion.get("Password"));

		puertobbdd.setText(datosConfiguracion.get("Puerto"));

		nombreHost.setText(datosConfiguracion.get("Hosting"));

		rellenarComboDB(datosConfiguracion);
	}

	private void actualizarDataBase() {
		// Escuchador para el campo de texto "usuario"
		usuario.textProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				actualizarComboBoxNombreBBDD();
			});
		});

		// Escuchador para el campo de texto "password"
		pass.textProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				actualizarComboBoxNombreBBDD();
			});
		});

		// Escuchador para el campo de texto "password"
		passUsuarioTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				actualizarComboBoxNombreBBDD();
			});
		});

		// Escuchador para el campo de texto "puerto"
		puertobbdd.textProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				actualizarComboBoxNombreBBDD();
			});
		});

		// Escuchador para el campo de texto "nombreHost"
		nombreHost.textProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				actualizarComboBoxNombreBBDD();
			});
		});
	}

	/**
	 * Actualiza el ComboBox de nombreBBDD con opciones disponibles.
	 */
	private void actualizarComboBoxNombreBBDD() {
		String usuario = this.usuario.getText();
		String password = this.pass.getText();
		String puerto = this.puertobbdd.getText();
		String hosting = this.nombreHost.getText();

		if (usuario.isEmpty() || password.isEmpty() || puerto.isEmpty() || hosting.isEmpty()) {
			nombreBBDD.getSelectionModel().clearSelection();
			nombreBBDD.setDisable(true);
			nombreBBDD.setEditable(false);
		} else {
			nombreBBDD.getItems().clear();
			nombreBBDD.setDisable(false);

			Map<String, String> datosConfiguracion = new HashMap<>();
			datosConfiguracion.put("Usuario", usuario);
			datosConfiguracion.put("Puerto", puerto);
			datosConfiguracion.put("Password", password);
			datosConfiguracion.put("Database", "");
			datosConfiguracion.put("Hosting", hosting);

			rellenarComboDB(datosConfiguracion);
		}
	}

	public void rellenarComboDB(Map<String, String> datosConfiguracion) {
		AlarmaList alarmaList = new AlarmaList();

		String puertoTexto = datosConfiguracion.get("Puerto");
		String databaseTexto = datosConfiguracion.get("Database");
		String hostingTexto = datosConfiguracion.get("Hosting");

		if (Utilidades.isMySQLServiceRunning(hostingTexto, puertoTexto)) {

			List<String> opciones = FuncionesFicheros.obtenerOpcionesNombreBBDD(datosConfiguracion);
			AlarmaList.detenerAnimacion();
			prontEstadoFichero.setText("");
			if (!opciones.isEmpty()) {

				AlarmaList.iniciarAnimacionEspera(prontEstadoFichero);
				nombreBBDD.getItems().addAll(opciones);
				nombreBBDD.getSelectionModel().selectFirst();
			}

			devolverDB(opciones, databaseTexto);
			nombreHost.setEditable(false);
		} else {
			alarmaList.iniciarAnimacionConexionRed(alarmaConexionSql);
		}
	}

	private String devolverDB(List<String> database, String databaseFichero) {
		int selectedIndex = -1;

		for (int i = 0; i < database.size(); i++) {
			String nombreDB = database.get(i);
			if (nombreDB.equalsIgnoreCase(databaseFichero)) {
				selectedIndex = i;
				break;
			}
		}

		List<String> updatedList = new ArrayList<>(database);

		if (selectedIndex != -1) {
			// If found, move the matching item to the front
			String removedItem = updatedList.remove(selectedIndex);
			updatedList.add(0, removedItem);
		}

		nombreBBDD.getItems().setAll(updatedList);

		if (!updatedList.isEmpty()) {
			nombreBBDD.getSelectionModel().selectFirst();
			return nombreBBDD.getSelectionModel().getSelectedItem();
		} else {
			return "";
		}
	}

	/**
	 * Funcion que permite restringir entrada de datos de todo aquello que no sea un
	 * numero entero en los comboBox numeroComic y caja_comic
	 */
	public void restringir_entrada_datos() {
		puertobbdd.setTextFormatter(FuncionesComboBox.validador_Nenteros());
	}

	/**
	 * Abre la ubicación de la carpeta "libreria" en el sistema de archivos del
	 * usuario.
	 *
	 * @param event el evento que desencadena la acción
	 */
	@FXML
	void abrirUbicacion(ActionEvent event) {
		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator
				+ "libreria";

		File carpeta = new File(ubicacion);

		if (Desktop.isDesktopSupported() && carpeta.exists()) {
			try {
				Desktop.getDesktop().open(carpeta);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Abre la ventana para crear la base de datos.
	 *
	 * @param event el evento que desencadena la acción
	 */
	@FXML
	void crearBBDD(ActionEvent event) {
		nav.verCrearBBDD();

		Stage myStage = (Stage) this.botonCrearBBDD.getScene().getWindow();
		myStage.close();
	}

	/**
	 * Guarda los datos en un archivo de configuración y realiza otras tareas
	 * relacionadas.
	 *
	 * @param event el evento que desencadena la acción
	 * @throws SQLException
	 */
	@FXML
	void guardarDatos(ActionEvent event) throws SQLException {

		String datos[] = new String[6];
		datos[0] = puertobbdd.getText();
		datos[1] = nombreBBDD.getValue();
		datos[2] = usuario.getText();
		datos[3] = pass.getText();
		datos[4] = nombreHost.getText();
		datos[5] = "";

		FuncionesFicheros.guardarDatosBaseLocal(datos, prontEstadoFichero, alarmaConexion);
	}

	/**
	 * Restaura la configuración a los valores predeterminados.
	 *
	 * @param event el evento que desencadena la acción
	 */
	@FXML
	void restaurarConfiguracion(ActionEvent event) {

		AlarmaList alarmaList = new AlarmaList();

		if (nav.borrarContenidoConfiguracion()) {
			String userHome = System.getProperty("user.home");
			String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
			String ficheroLocal = ubicacion + File.separator + "libreria" + File.separator + "configuracion_local.conf";

			// Verificar y eliminar los archivos dentro de la carpeta "libreria"
			File ficheroConfiguracionLocal = new File(ficheroLocal);

			ficheroConfiguracionLocal.delete();

			FuncionesFicheros.crearEstructura();

			limpiar_datos();
			AlarmaList.detenerAnimacion();
			prontEstadoFichero.setStyle("-fx-background-color: #f5af2d");
			alarmaList.iniciarAnimacionRestaurado(prontEstadoFichero);
		} else {
			AlarmaList.detenerAnimacion();
			prontEstadoFichero.setStyle("-fx-background-color: #DD370F");
			alarmaList.iniciarAnimacionRestauradoError(prontEstadoFichero);
		}
	}

	/**
	 * Limpia los datos en los campos de texto.
	 */
	public void limpiar_datos() {
		usuario.setText("");

		pass.setText("");

		puertobbdd.setText("");

		nombreBBDD.getSelectionModel().clearSelection();
	}

	/**
	 * Funcion para abrir el navegador y acceder a la URL
	 *
	 * @param event
	 */
	@FXML
	void accesoMySqlWorkbench(ActionEvent event) {
		String url1 = "https://dev.mysql.com/downloads/windows/installer/8.0.html";
		String url2 = "https://www.youtube.com/watch?v=FvXQBKsp0OI&ab_channel=MisterioRojo";

		if (Utilidades.isWindows()) {
			Utilidades.accesoWebWindows(url1); // Llamada a funcion
			Utilidades.accesoWebWindows(url2); // Llamada a funcion
		} else {
			if (Utilidades.isUnix()) {
				Utilidades.accesoWebLinux(url1); // Llamada a funcion
				Utilidades.accesoWebLinux(url2); // Llamada a funcion
			} else {
				Utilidades.accesoWebMac(url1); // Llamada a funcion
				Utilidades.accesoWebMac(url2); // Llamada a funcion
			}
		}
	}

	/**
	 * Vuelve al programa principal desde la ventana actual.
	 *
	 * @param event el evento que desencadena la acción
	 */
	@FXML
	void volverPrograma(ActionEvent event) {
		nav.verAccesoBBDD(); // Llamada a metodo para abrir la ventana anterior

		Stage myStage = (Stage) this.botonVolver.getScene().getWindow();
		myStage.close();
	}

	/**
	 * Maneja la acción de salida del programa.
	 *
	 * @param event el evento que desencadena la acción
	 */
	@FXML
	public void salirPrograma(ActionEvent event) {

		if (nav.salirPrograma(event)) { // Llamada a metodo que permite salir completamente del programa
			Stage myStage = (Stage) this.botonSalir.getScene().getWindow();
			myStage.close();
		}
	}

	/**
	 * Cierra el programa a la fuerza correctamente.
	 */
	public void closeWindows() { // Metodo que permite cerrar completamente el programa en caso de cerrar a la //
		// fuerza.
		Stage myStage = (Stage) this.botonSalir.getScene().getWindow();
		myStage.close();
	}

}