package org.apache.maven.configuracionHibernate;

import java.net.URISyntaxException;

import org.apache.maven.Funcionamiento.FuncionesFicheros;

public class GuardarConfiguracion {

	public static void guardarConfiguracion() throws URISyntaxException {
		String ficheroConfig[] = FuncionesFicheros.datosEnvioFichero();

		String puerto = ficheroConfig[0];
		String database = ficheroConfig[1];
		String usuario = ficheroConfig[2];
		String contrasena = ficheroConfig[3];
		String hosting = ficheroConfig[4];

		String url = "jdbc:mysql://" + hosting + ":" + puerto + "/" + database;

		// Actualizar la configuración de Hibernate
		ConfiguracionHibernate configuracionHibernate = new ConfiguracionHibernate();
		configuracionHibernate.setDriverClass("com.mysql.jdbc.Driver");
		configuracionHibernate.setUrl(url);
		configuracionHibernate.setUsername(usuario);
		configuracionHibernate.setPassword(contrasena);

		CargarConfiguracion.modificarConfiguracion(url, usuario, contrasena);

	}

}
