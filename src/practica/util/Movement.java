
package practica.util;

/**
 * 
 * @author Ismael
 * Clase Par para tratar los datos como un todo (mas tarde modificado a una tupla de 3 elementos) para contol de distancia al objetivo
 * si esta en meta o no, y second (que por desgracia ya no me acuerdo)
 */
public class Movement{
	private float first;
	private int second;
	private boolean third;

	/**
	 * @author Ismael
	 * Constructor
	 * @param first
	 * @param second
	 * @param third
	 */
	public Movement(float first,int second, boolean third){
		this.first = first;
		this.second = second;
		this.third=third;

	}
	/**
	 * @author Ismael
	 * Introduce un valor en el primer elemento 
	 * @param newFirst
	 */
	public void setDistance(int newFirst){
		first =newFirst;
	}
	
	/**
	 * @author Ismael 
	 * Introduce un valor en el segundo elemento
	 * @param newSecond
	 */
	public void setMove(int newSecond){
		second =newSecond;
	}
	
	/**
	 * @author Ismael
	 * Introduce un valor en el tercer elemento
	 * @param newThird
	 */
	public void setValid(boolean newThird){
		third =newThird;
	}
	/**
	 * @author Ismael
	 * devuelve el valor del primer elemento
	 * @return first
	 */
	public float getDistance(){
		return first;
	}
	
	/**
	 * @author Ismael
	 * Devuelve el valor del segundo elemento
	 * @return second
	 */
	public int getMove(){
		return second;
	}
	
	/**
	 * @author Ismael
	 * Devuelve el valor del tercer elemento
	 * @return third
	 */
	public boolean isValid(){
		return third;
	}
}