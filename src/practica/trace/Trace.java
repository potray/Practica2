package practica.trace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import practica.lib.ErrorLibrary;
import practica.util.GPSLocation;

/**
 * Camino que recorre un drone.
 * @author Daniel
 *
 */

public class Trace extends LinkedList<Choice>{
	
	/**
	 * Constructor vacío, crea una traza sin elementos.
	 * @author Daniel
	 */
	public Trace(){
		super();
	}
	
	/**
	 * 
	 * Constructor por copia, crea una traza a partir de una subtraza.
	 * @param tr traza a copiar.
	 * @param start posición inicial de la copia. 
	 * @param end posición final de la copia.
	 * @throws IllegalArgumentException si end < start.
	 */
	public Trace (Trace tr, int start, int end) throws IllegalArgumentException{
		super();
		if (end < start)
			throw new IllegalArgumentException(ErrorLibrary.TraceEndLowerThanStart);
		//Copio los elementos de la traza antigua.
		for (int i = start; i <= end; i++)
			push(tr.get(i));		
	}
	
	/**
	 * Devuelve la posición en el mapa que tenía un drone en un momento de la traza.
	 * @param i índice dentro de la traza de la posición.
	 * @return posición en el mapa que tenía el drone.
	 */	
	public GPSLocation getLocation (int i){
		return get(i).getLocation();
	}
	
	
}
