package org.apache.maven.configuracionHibernate;

public class ConfiguracionHibernate {


	private String driverClass;
	private String url;
	private String username;
	private String password;
	
	public ConfiguracionHibernate(String driverClass, String url, String username, String password) {
		super();
		this.driverClass = driverClass;
		this.url = url;
		this.username = username;
		this.password = password;
	}
	
	public ConfiguracionHibernate() {
		super();
		this.driverClass = "";
		this.url = "";
		this.username = "";
		this.password = "";
	}

	// Métodos getters y setters para las propiedades de conexión
	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
