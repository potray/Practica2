package practica.util;

/**
 * Estructura de datos que contiene el mapa por donde se mueve el drone.
 * @author Dani
 */ 

public class Map {
	private int matrix[][];
	private int heigh;
	private int width;
	public final static int LIBRE = 0;
	public final static int OBSTACULO = 1;
	public final static int VISITADO = 2;
	public final static int OBJETIVO = 3;

	/**
	 * Constructor por defecto. Todas las celdas se rellenan con el valor LIBRE.
	 * @author Dani
	 * @param heigh 	Altura del mapa
	 * @param width 	Anchura del mapa
	 */
	public Map(int heigh, int width) {
		matrix = new int[heigh][width];
		this.heigh = heigh;
		this.width = width;

		// Por defecto todo está libre
		for (int i = 0; i < heigh; i++)
			for (int j = 0; j < width; j++)
				matrix[i][j] = LIBRE;
	}

	/**
	 * Constructor por copia.
	 * @author Dani
	 * @param map 	Mapa original a copiar.
	 */
	public Map(Map map) {
		// Inicialización de componentes
		matrix = new int[map.getHeigh()][map.getWidth()];
		heigh = map.getHeigh();
		width = map.getWidth();

		// Copia de valores
		for (int i = 0; i < heigh; i++)
			for (int j = 0; j < width; j++)
				matrix[i][j] = map.getValue(j, i);
	}

	/**
	 * Getter de la altura
	 * @return Altura del mapa
	 */
	public int getHeigh() {
		return heigh;
	}

	/**
	 * Getter de la anchura.
	 * @return Anchura del mapa
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Getter del valor de una celda.
	 * @author Dani
	 * @param x 	Columna.
	 * @param y 	Fila.
	 * @return Valor de la celda en la posición x,y.
	 */
	public int getValue(int x, int y) {
		// Añadida una comprobación: si la x o la y están fuera de los límites del mapa, devolver OBSTACULO
		if (x < 0 || y < 0 || x >= this.getWidth() || y >= this.getHeigh()) {
			return OBSTACULO;
		} else {
			return matrix[y][x];
		}
	}

	/**
	 * Setter del valor de una celda
	 * @param x 	Columna.
	 * @param y 	Fila.
	 * @param value Valor nuevo de la celda.
	 */
	public void setvalue(int x, int y, int value) {
		matrix[y][x] = value;
	}

}
