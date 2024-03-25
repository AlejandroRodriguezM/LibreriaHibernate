/**
 * Contiene las clases que hacen funcionar las diferentes funciones de uso de back end y front de todo el proyecto
 *  
*/
package org.apache.maven.Funcionamiento;

import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Clase que proporciona funciones para asignar tooltips a botones, ComboBox y
 * TextField en la interfaz de usuario.
 */
public class FuncionesTooltips {

	/**
	 * Fuente utilizada para los tooltips en la interfaz gráfica.
	 */
	private static final Font TOOLTIP_FONT = Font.font("Comic Sans MS", FontWeight.NORMAL, FontPosture.REGULAR, 13);

	public static void assignTooltips(Map<Node, String> tooltipsMap) {
	    for (Node elemento : tooltipsMap.keySet()) {
	        String mensaje = tooltipsMap.get(elemento);
	        Tooltip tooltip = new Tooltip(mensaje);
	        tooltip.setFont(TOOLTIP_FONT);
	        Tooltip.install(elemento, tooltip);
	    }
	}


}