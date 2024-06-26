/**
 * Contiene las clases que hacen funcionar las diferentes funciones de uso de back end y front de todo el proyecto
 *  
*/
package org.apache.maven.Funcionamiento;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.maven.Controladores.CargaComicsController;
import org.apache.maven.comicManagement.Comic;
import org.apache.maven.comicManagement.ComicFichero;
import org.apache.maven.dbmanager.ComicManagerDAO;
import org.apache.maven.dbmanager.ConectManager;
import org.apache.maven.dbmanager.DBUtilidades;
import org.apache.maven.dbmanager.DBUtilidades.TipoBusqueda;
import org.apache.maven.dbmanager.InsertManager;
import org.apache.maven.dbmanager.SelectManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;

/**
 * Esta clase sirve para crear tanto los ficheros Excel como los ficheros CSV,
 * la exportacion de estos o la importacion
 *
 * @author Alejandro Rodriguez
 */
public class FuncionesExcel {

	/**
	 * Objeto para manejar la navegación en la interfaz gráfica.
	 */
	private static Ventanas nav = new Ventanas();

	private static final String USER_HOME_DIRECTORY = System.getProperty("user.home");
	private static final String DOCUMENTS_PATH = USER_HOME_DIRECTORY + File.separator + "Documents";

	// Para portadas
	public static final String DEFAULT_PORTADA_IMAGE_PATH = DOCUMENTS_PATH + File.separator + "libreria_comics"
			+ File.separator + ConectManager.DB_NAME + File.separator + "portadas";

	// Para la base de la ruta de imágenes predeterminada
	private static final String DEFAULT_IMAGE_PATH_BASE = DOCUMENTS_PATH + File.separator + "libreria_comics"
			+ File.separator + ConectManager.DB_NAME;

	private static final String LOG_FILE_NAME = "log_"
			+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".txt";

	private static int NUMERO_LINEAS_FICHERO = 0;

	private static int NUMERO_COMICS_LEIDOS = 0;

	/**
	 * Convierte un archivo Excel (XLSX) en formato CSV y guarda los datos en un
	 * nuevo archivo CSV.
	 *
	 * @param fichero El archivo Excel (XLSX) del cual se extraerán los datos para
	 *                el archivo CSV.
	 */
	public void createCSV(File fichero) {

		// For storing data into CSV files
		StringBuffer data = new StringBuffer();

		try {
			// Creating input stream
			FileInputStream fis = new FileInputStream(fichero);

			Workbook workbook = new XSSFWorkbook(fis);

			// Get first sheet from the workbook
			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) {
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {

					Cell cell = cellIterator.next();

					switch (cell.getCellType()) {
					case BOOLEAN:
						data.append(cell.getBooleanCellValue() + ";");
						break;

					case NUMERIC:
						data.append(cell.getNumericCellValue() + ";");
						break;

					case STRING:
						data.append(cell.getStringCellValue() + ";");
						break;

					case BLANK:
						data.append("" + ";");
						break;

					default:
						data.append(cell + ";");
					}
				}
				data.append('\n');
			}

			FileOutputStream fos = new FileOutputStream(
					fichero.getAbsolutePath().substring(0, fichero.getAbsolutePath().lastIndexOf(".")) + ".csv");
			fos.write(data.toString().getBytes());
			fos.close();
			workbook.close();

		} catch (Exception e) {
			Utilidades.manejarExcepcion(e);
		}
	}

	/**
	 * Abre un cuadro de diálogo para seleccionar una carpeta de portadas.
	 *
	 * @return La carpeta seleccionada como objeto File.
	 */
	public File carpetaPortadas() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File directorio = directoryChooser.showDialog(null);
		return directorio;
	}

	public File carpetaExcelExportado() {
		String frase = "Fichero Excel xlsx";

		String formato = "*.xlsx";

		File fichero = Utilidades.tratarFichero(frase, formato).showSaveDialog(null); // Llamada a funcion
		return fichero;
	}

	/**
	 * Abre un cuadro de diálogo para seleccionar una carpeta de portadas en un hilo
	 * de tarea (Task).
	 *
	 * @return La carpeta seleccionada como objeto File.
	 */
	public File carpetaPortadasTask() {
		final File[] directorio = new File[1];
		CountDownLatch latch = new CountDownLatch(1);

		Platform.runLater(() -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directorio[0] = directoryChooser.showDialog(null);
			latch.countDown();
		});

		try {
			latch.await(); // Esperar hasta que se complete la selección del directorio
		} catch (InterruptedException e) {
			Utilidades.manejarExcepcion(e);
		}

		return directorio[0];
	}

	/**
	 * Crea una tarea que se encarga de generar un archivo Excel a partir de los
	 * datos de la base de datos.
	 *
	 * @param fichero El archivo en el que se exportarán los datos.
	 * @return Una tarea que realiza la exportación y devuelve true si se realiza
	 *         con éxito, o false si ocurre un error.
	 */
	public Task<Boolean> crearExcelTask(List<Comic> listaComics, String tipoBusqueda) {

		NUMERO_COMICS_LEIDOS = 0;
		File directorioImagenes = carpetaPortadas();
		File directorioFichero = carpetaExcelExportado();
		NUMERO_LINEAS_FICHERO = ComicManagerDAO.countRows(SelectManager.TAMANIO_DATABASE);
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				try (FileOutputStream outputStream = new FileOutputStream(directorioFichero)) {

					Cell celda;
					Row fila;
					Sheet hoja;
					Workbook libro;
					String encabezado;
					String[] encabezados = { "ID", "nomComic", "caja_deposito", "precio_comic", "codigo_comic",
							"numComic", "nomVariante", "Firma", "nomEditorial", "Formato", "Procedencia",
							"fecha_publicacion", "nomGuionista", "nomDibujante", "puntuacion", "portada", "key_issue",
							"url_referencia", "estado" };
					int indiceFila = 0;

					directorioFichero.createNewFile();

					libro = new XSSFWorkbook();

					hoja = libro.createSheet("Base de datos comics");

					fila = hoja.createRow(indiceFila);
					for (int i = 0; i < encabezados.length; i++) {
						encabezado = encabezados[i];
						celda = fila.createCell(i);
						celda.setCellValue(encabezado);
					}
					AtomicReference<CargaComicsController> cargaComicsControllerRef = new AtomicReference<>();
					nav.verCargaComics(cargaComicsControllerRef);
					Row filaCopy = fila; // Create a copy of fila here
					int indiceFinal = indiceFila;
					indiceFinal++;
					List<Comic> listaComicsCopy = new ArrayList<>(listaComics);

					for (Comic comic : listaComicsCopy) {
						filaCopy = hoja.createRow(indiceFinal);
						filaCopy.createCell(0).setCellValue("");
						filaCopy.createCell(1).setCellValue(comic.getNombre());
						filaCopy.createCell(2).setCellValue(comic.getNumCaja());
						filaCopy.createCell(3).setCellValue(comic.getPrecio_comic());
						filaCopy.createCell(4).setCellValue(comic.getCodigo_comic());
						filaCopy.createCell(5).setCellValue(comic.getNumero());
						filaCopy.createCell(6).setCellValue(comic.getVariante());
						filaCopy.createCell(7).setCellValue(comic.getFirma());
						filaCopy.createCell(8).setCellValue(comic.getEditorial());
						filaCopy.createCell(9).setCellValue(comic.getFormato());
						filaCopy.createCell(10).setCellValue(comic.getProcedencia());
						filaCopy.createCell(11).setCellValue(comic.getFecha());
						filaCopy.createCell(12).setCellValue(comic.getGuionista());
						filaCopy.createCell(13).setCellValue(comic.getDibujante());
						filaCopy.createCell(14).setCellValue(comic.getPuntuacion());
						filaCopy.createCell(15).setCellValue(comic.getImagen());
						filaCopy.createCell(16).setCellValue(comic.getKey_issue());
						filaCopy.createCell(17).setCellValue(comic.getUrl_referencia());
						filaCopy.createCell(18).setCellValue(comic.getEstado());

						cargaComics(comic, cargaComicsControllerRef, directorioImagenes, false);

						if (!directorioFichero.exists()) {
							directorioFichero.mkdir();
						}

						if (tipoBusqueda.equalsIgnoreCase("Completa")
								|| tipoBusqueda.equalsIgnoreCase("Parcial") && directorioFichero != null) {
							Utilidades.saveImageFromDataBase(comic.getImagen(), directorioImagenes);

						}

						indiceFinal++; // Increment the index for the next row
					}

					Platform.runLater(() -> {
						cargaComicsControllerRef.get().cargarDatosEnCargaComics("", "100%", 100.0);
					});

					libro.write(outputStream);
					libro.close();
					createCSV(directorioFichero);

					return true;
				} catch (FileNotFoundException ex) {
					Utilidades.manejarExcepcion(ex);
					return false;
				} catch (IOException ex) {
					Utilidades.manejarExcepcion(ex);
					return false;
				}
			}
		};

		return task;
	}

	/**
	 * Guarda los datos en un archivo de Excel y crea un archivo ZIP que contiene el
	 * archivo Excel.
	 *
	 * @param nombre_carpeta El nombre de la carpeta para la copia de seguridad.
	 * @throws SQLException
	 */
	public void savedataExcel(String nombre_carpeta) throws SQLException {
		NUMERO_COMICS_LEIDOS = 0;
		Cell celda;
		Row fila;
		Sheet hoja;
		Workbook libro;
		String encabezado;
		String[] encabezados = { "ID", "nomComic", "caja_deposito", "precio_comic", "codigo_comic", "numComic",
				"nomVariante", "Firma", "nomEditorial", "Formato", "Procedencia", "fecha_publicacion", "nomGuionista",
				"nomDibujante", "puntuacion", "portada", "key_issue", "url_referencia", "estado" };
		int indiceFila = 0;

		String userDir = System.getProperty("user.home");

		String ubicacion = userDir + File.separator + "AppData" + File.separator + "Roaming";
		String direccion = ubicacion + File.separator + "libreria" + File.separator + ConectManager.DB_NAME
				+ File.separator + "backups" + File.separator + nombre_carpeta;
		try {

			File carpetaLibreria = new File(direccion);
			File fichero = new File(carpetaLibreria, "BaseDatos.xlsx");
			fichero.createNewFile();

			NUMERO_LINEAS_FICHERO = ComicManagerDAO.countRows(SelectManager.TAMANIO_DATABASE);

			String sentenciaSQL = DBUtilidades.construirSentenciaSQL(TipoBusqueda.COMPLETA);

			List<Comic> listaComics = SelectManager.verLibreria(sentenciaSQL);

			libro = new XSSFWorkbook();

			hoja = libro.createSheet("Base de datos comics");

			fila = hoja.createRow(indiceFila);
			for (int i = 0; i < encabezados.length; i++) {
				encabezado = encabezados[i];
				celda = fila.createCell(i);
				celda.setCellValue(encabezado);
				celda.getStringCellValue().getBytes(Charset.forName("UTF-8"));
			}

			Thread excelThread = new Thread(() -> {
				try {
					AtomicReference<CargaComicsController> cargaComicsControllerRef = new AtomicReference<>();
					nav.verCargaComics(cargaComicsControllerRef);

					Row filaCopy = fila; // Create a copy of fila here
					int indiceFinal = indiceFila;
					indiceFinal++;
					List<Comic> listaComicsCopy = new ArrayList<>(listaComics);
					for (Comic comic : listaComicsCopy) {
						filaCopy = hoja.createRow(indiceFinal);
						filaCopy.createCell(0).setCellValue("");
						filaCopy.createCell(1).setCellValue(comic.getNombre());
						filaCopy.createCell(2).setCellValue(comic.getNumCaja());
						filaCopy.createCell(3).setCellValue(comic.getPrecio_comic());
						filaCopy.createCell(4).setCellValue(comic.getCodigo_comic());
						filaCopy.createCell(5).setCellValue(comic.getNumero());
						filaCopy.createCell(6).setCellValue(comic.getVariante());
						filaCopy.createCell(7).setCellValue(comic.getFirma());
						filaCopy.createCell(8).setCellValue(comic.getEditorial());
						filaCopy.createCell(9).setCellValue(comic.getFormato());
						filaCopy.createCell(10).setCellValue(comic.getProcedencia());
						filaCopy.createCell(11).setCellValue(comic.getFecha());
						filaCopy.createCell(12).setCellValue(comic.getGuionista());
						filaCopy.createCell(13).setCellValue(comic.getDibujante());
						filaCopy.createCell(14).setCellValue(comic.getPuntuacion());
						filaCopy.createCell(15).setCellValue(comic.getImagen());
						filaCopy.createCell(16).setCellValue(comic.getKey_issue());
						filaCopy.createCell(17).setCellValue(comic.getUrl_referencia());
						filaCopy.createCell(18).setCellValue(comic.getEstado());

						cargaComics(comic, cargaComicsControllerRef, carpetaLibreria, false);

						indiceFinal++; // Increment the index for the next row

					}

					try (FileOutputStream outputStream = new FileOutputStream(fichero);) {
						Platform.runLater(() -> {
							cargaComicsControllerRef.get().cargarDatosEnCargaComics("", "100%", 100.0);
						});

						libro.write(outputStream);
						libro.close();
						createCSV(fichero);
						
						String zipPath = carpetaLibreria.getAbsolutePath() + File.separator + "excel_" + nombre_carpeta
								+ ".zip";
						File zipFile = new File(zipPath);

						if (!zipFile.exists()) {
							if (!zipFile.createNewFile()) {
								throw new IOException("Failed to create backup zip file.");
							}
						}

						Utilidades.addFileToZip(fichero, "BaseDatos.xlsx", zipFile);

						fichero.delete();
					} catch (IOException ex) {
						nav.alertaException(ex.toString());
					}
				} catch (Exception e) {
					nav.alertaException(e.toString());
				}
			});

			excelThread.start();
		} catch (

		IOException e) {
			Utilidades.manejarExcepcion(e);
		}
	}

	public Task<Boolean> procesarArchivoCSVTask(File fichero) {
		
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				try {

					checkCSVColumns(fichero.getAbsolutePath().toString());
					int numeroLineas = Utilidades.contarLineasFichero(fichero);
					actualizarNumLineas(numeroLineas);
					procesarCSVInternamente(fichero);
					return true; // Indicar que la operación fue exitosa
				} catch (SQLException e) {
					e.printStackTrace();
					Platform.runLater(
							() -> nav.alertaException("Error al guardar datos en la base de datos: " + e.getMessage()));
					return false; // Indicar que la operación falló
				} catch (IOException e) {
					e.printStackTrace();
					Platform.runLater(() -> nav.alertaException("Error al leer el archivo CSV: " + e.getMessage()));
					return false; // Indicar que la operación falló
				}
			}
		};
		return task;
	}

	private static void actualizarNumLineas(int numLineas) {
		NUMERO_LINEAS_FICHERO = numLineas;
	}

	private void procesarCSVInternamente(File fichero) throws SQLException, IOException {
		NUMERO_COMICS_LEIDOS = 0;
		Utilidades.crearCarpeta();
		try (BufferedReader lineReader = new BufferedReader(new FileReader(fichero))) {
			// Inicializar directorio con el valor predeterminado
			File directorio = new File(DEFAULT_PORTADA_IMAGE_PATH + File.separator);

			// Obtener confirmación para continuar la subida de portadas
			CompletableFuture<Boolean> confirmacionFuture = nav.cancelar_subida_portadas();
			boolean continuarSubida = confirmacionFuture.join();

			// Actualizar el directorio si se va a continuar la subida de portadas
			if (continuarSubida) {
				directorio = carpetaPortadasTask();
				directorio = (directorio == null) ? new File(DEFAULT_PORTADA_IMAGE_PATH + File.separator) : directorio;

			}

			// Inicializar referencia al controlador de carga de cómics
			AtomicReference<CargaComicsController> cargaComicsControllerRef = new AtomicReference<>();
			nav.verCargaComics(cargaComicsControllerRef);

			// Leer la primera línea del archivo
			lineReader.readLine();

			// Copiar directorio al directorio predeterminado
			Utilidades.copyDirectory(directorio.getAbsolutePath(), DEFAULT_PORTADA_IMAGE_PATH );
			AtomicReference<File> directorioRef = new AtomicReference<>(directorio);

			// Procesar líneas restantes del archivo
			lineReader.lines().forEach(lineText -> {
				try {
					Comic comicNuevo = ComicFichero.datosComicFichero(lineText);

					InsertManager.insertarDatos(comicNuevo, true);
					
					cargaComics(comicNuevo, cargaComicsControllerRef, directorioRef.get(), true);

				} catch (Exception e) {
					// Manejar cualquier excepción durante el procesamiento de la línea
					e.printStackTrace();
				}
			});

			// Actualizar UI con el progreso final
			Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics("", "100%", 100.0));

			// Abrir el archivo de registro
			Platform.runLater(
					() -> Utilidades.abrirArchivo(DEFAULT_IMAGE_PATH_BASE + File.separator + LOG_FILE_NAME));

		} catch (IOException e) {
			// Propagar la excepción al nivel superior
			throw e;
		}
	}

	public void cargaComics(Comic comicNuevo, AtomicReference<CargaComicsController> cargaComicsControllerRef,
			File directorio, boolean esImportado) {
		String nombre_portada = "";
		String nombre_modificado = "";

		if (esImportado) {
			nombre_portada = Utilidades.obtenerNombrePortada(false, comicNuevo.getImagen());
			nombre_modificado = Utilidades.convertirNombreArchivo(nombre_portada);
			if (!Utilidades.existeArchivo(directorio.getAbsolutePath(), nombre_portada)) {
				
				copiarPortadaPredeterminada(DEFAULT_PORTADA_IMAGE_PATH , nombre_modificado);
				
				/////////////////////////////////////////////////////////////////////
				////// HAY QUE ARREGLARLO, AL CAMBIAR EL NOMBRE ESTO DA FALLO SIEMPRE//
				//////////////////////////////////////////////////////////////////////
//				generarLogFaltaPortada(DEFAULT_IMAGE_PATH_BASE, LOG_FILE_NAME, nombre_portada);
			}
		}

		StringBuilder finalTextoBuilder = new StringBuilder("Comic: ").append(comicNuevo.getNombre()).append(" - ")
				.append(comicNuevo.getNumero()).append(" - ").append(comicNuevo.getVariante()).append("\n");

		if (esImportado && !Utilidades.existeArchivo(DEFAULT_PORTADA_IMAGE_PATH , nombre_modificado)) {
			finalTextoBuilder.append("Comic: ").append(comicNuevo.getNombre()).append(" - ")
					.append(comicNuevo.getNumero()).append(" - ").append(comicNuevo.getVariante()).append("\n");
		}

		Platform.runLater(() -> {
			double progress = (double) NUMERO_COMICS_LEIDOS / (NUMERO_LINEAS_FICHERO + 1);
			String porcentaje = String.format("%.2f%%", progress * 100);

			cargaComicsControllerRef.get().cargarDatosEnCargaComics(finalTextoBuilder.toString(), porcentaje, progress);
		});

		NUMERO_COMICS_LEIDOS++;
	}

	/**
	 * Función que copia la portada predeterminada y cambia su nombre
	 *
	 * @param defaultImagePath El directorio de las portadas
	 * @param nombreModificado El nombre del archivo modificado
	 * @throws IOException
	 */
	public void copiarPortadaPredeterminada(String defaultImagePath, String nombreModificado) {
		if (defaultImagePath == null || nombreModificado == null) {
			throw new IllegalArgumentException("defaultImagePath y nombreModificado no pueden ser nulos");
		}

		File sourceFile = new File(defaultImagePath, nombreModificado);
		File destinationFile = new File(defaultImagePath, nombreModificado);

		try {
			if (!sourceFile.exists()) {
				try (InputStream input = getClass().getResourceAsStream("sinPortada.jpg");
						OutputStream output = new FileOutputStream(destinationFile)) {

					File destinationDirectory = destinationFile.getParentFile();
					if (!destinationDirectory.exists()) {
						destinationDirectory.mkdirs();
					}

					destinationFile.createNewFile();

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = input.read(buffer)) != -1) {
						output.write(buffer, 0, bytesRead);
					}
				}
			} else {
				Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			Utilidades.manejarExcepcion(e);
		}
	}

//	/**
//	 * Función que genera el log cuando falta una portada
//	 *
//	 * @param defaultImagePathBase El directorio base de las portadas
//	 * @param logFileName          El nombre del archivo de log
//	 * @param nombreModificado     El nombre del archivo modificado
//	 * @throws IOException
//	 */
//	private void generarLogFaltaPortada(String defaultImagePathBase, String logFileName, String nombreModificado) {
//		String logFilePath = defaultImagePathBase + File.separator + logFileName;
//		try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
//			writer.write("Falta portada: " + nombreModificado);
//			writer.newLine();
//		} catch (IOException e) {
//			Utilidades.manejarExcepcion(e);
//		}
//	}

	private void checkCSVColumns(String filePath) throws IOException {
		// Columnas esperadas
		String[] expectedColumns = { "ID", "nomComic", "caja_deposito", "precio_comic", "codigo_comic", "numComic",
				"nomVariante", "Firma", "nomEditorial", "Formato", "Procedencia", "fecha_publicacion", "nomGuionista",
				"nomDibujante", "puntuacion", "portada", "key_issue", "url_referencia", "estado" };

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			if ((line = br.readLine()) != null) {
				// Obtener las columnas de la primera línea del archivo CSV
				String[] columns = line.split(";");
				// Verificar si las columnas coinciden con las esperadas
				if (columns.length != expectedColumns.length) {
					throw new IOException("El número de columnas no coincide");
				}
				for (int i = 0; i < columns.length; i++) {
					if (!columns[i].trim().equalsIgnoreCase(expectedColumns[i])) {
						throw new IOException("El nombre de la columna en la posición " + i + " no coincide");
					}
				}
			} else {
				throw new IOException("El archivo CSV está vacío");
			}
		}
	}
}
