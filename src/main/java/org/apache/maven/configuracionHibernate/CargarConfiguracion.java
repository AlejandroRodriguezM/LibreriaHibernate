package org.apache.maven.configuracionHibernate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CargarConfiguracion {

//	public static void modificarConfiguracion(String url, String username, String password) {
//
//		File resourcesDirectory = new File("src/main/resources");
//		File inputFile = new File(resourcesDirectory, "hibernate.cfg.xml");
//	    InputStream inputStream = CargarConfiguracion.class.getResourceAsStream("/hibernate.cfg.xml");
//
//		StringBuilder xmlContent = new StringBuilder();
//		try (BufferedReader reader = new BufferedReader(
//				new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
//			String line;
//			while ((line = reader.readLine()) != null) {
//				if (line.contains("hibernate.connection.url")) {
//					line = "<property name=\"hibernate.connection.url\">" + url + "</property>";
//				} else if (line.contains("hibernate.connection.username")) {
//					line = "<property name=\"hibernate.connection.username\">" + username + "</property>";
//				} else if (line.contains("hibernate.connection.password")) {
//					line = "<property name=\"hibernate.connection.password\">" + password + "</property>";
//				}
//				xmlContent.append(line).append("\n");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//
//		try (BufferedWriter writer = new BufferedWriter(
//				new OutputStreamWriter(new FileOutputStream(inputFile), StandardCharsets.UTF_8))) {
//			writer.write(xmlContent.toString());
//			System.out.println("Configuración de Hibernate actualizada correctamente.");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public static void modificarConfiguracion(String url, String username, String password) {
		// Obtener el flujo de entrada del recurso
		InputStream inputStream = CargarConfiguracion.class.getResourceAsStream("/hibernate.cfg.xml");

		// Verificar si se pudo obtener el flujo de entrada
		if (inputStream == null) {
			System.err.println("No se pudo obtener el flujo de entrada del archivo hibernate.cfg.xml");
			return;
		}

		// Crear un archivo temporal para escribir el contenido modificado
		File tempFile = null;
		try {
			tempFile = File.createTempFile("hibernate", ".tmp");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("hibernate.connection.url")) {
					line = "<property name=\"hibernate.connection.url\">" + url + "</property>";
				} else if (line.contains("hibernate.connection.username")) {
					line = "<property name=\"hibernate.connection.username\">" + username + "</property>";
				} else if (line.contains("hibernate.connection.password")) {
					line = "<property name=\"hibernate.connection.password\">" + password + "</property>";
				}
				writer.write(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Reemplazar el archivo original con el nuevo archivo modificado
		File originalFile = new File(CargarConfiguracion.class.getResource("/hibernate.cfg.xml").getFile());
		if (!tempFile.renameTo(originalFile)) {
			System.err.println("Error al reemplazar el archivo original.");
		} else {
			System.out.println("Archivo hibernate.cfg.xml modificado correctamente.");
		}
	}

}
