package practica.agent;


import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.LinkedBlockingQueue;

import edu.emory.mathcs.backport.java.util.concurrent.PriorityBlockingQueue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import practica.util.GPSLocation;
import practica.util.ImgMapConverter;
import practica.util.Map;
import practica.util.MessageQueue;
import practica.util.SharedMap;
import practica.util.Visualizer;
import practica.util.DroneStatus;

public class Satellite extends SingleAgent {
	/**
	 * TODOauthor Dani
	 * TODO cambiar a SharedMap cuando esté arreglado.
	 */
	private Map mapOriginal;						//Mapa original a partir del cual transcurre todo.
	private Map mapSeguimiento;						//Mapa que se va actualizando a medida que los drones se muevan.
	private double goalPosX;						//Coordenada X del objetivo.
	private double goalPosY;						//Cordenada Y del objetivo.
	private AgentID [] drones;						//Array que contiene las IDs de los drones.
	private DroneStatus [] droneStuses;				//Array que contiene los estados de los drones.
	private int maxDrones;							//Número máximo de drones que acepta el satélite.
	private int connectedDrones;					//Número de drones conectados.
	private LinkedBlockingQueue messageQueue;		//Cola de mensajes
	private Visualizer visualizer;					//Visualizador.
	private boolean usingVisualizer;				//Variable para controlar si se está usando el visualizador.
	private boolean exit;							//Variable para controlar la terminación de la ejecución del satélite.
	
	/**
	 * Constructor
	 * @author Dani
	 * FIXME otros autores añadiros.
	 * @param sat ID del satélite.
	 * @param mapa mapa que se usará.
	 * @param maxDrones número máximo de drones que aceptará el satélite.
	 * @throws Exception
	 */
	public Satellite(AgentID sat, Map map, int maxDrones) throws Exception{
		//Inicialización de atributos.
		super(sat);
		exit = false;
		/**
		 * @TODOauthor Dani
		 * TODO cambiar a SharedMap cuando esté arreglado.
		 */
		mapOriginal = new Map(map);
		mapSeguimiento = new Map(map);
		drones = new AgentID [maxDrones];
		droneStuses = new DroneStatus [maxDrones];
		this.maxDrones = maxDrones;
		connectedDrones = 0;	
		//TODO cambiar a PriorityBlockingQueue.
		messageQueue = new LinkedBlockingQueue();
		
		
		//Calcular la posición del objetivo.
		//Se suman todas las posiciones que contienen un objetivo y se halla la media.
		float horizontalPositions = 0, verticalPositions = 0, adjacentSquares=0;
		for(int i = 0; i < mapOriginal.getHeigh(); i ++)
		    for(int j = 0; j < mapOriginal.getWidth(); j ++){
		        if(mapOriginal.getValue(j,i) == Map.OBJETIVO){
		            horizontalPositions += j;
		            verticalPositions += i;
		            adjacentSquares ++;
		        }
		    }
		
		goalPosX = horizontalPositions / adjacentSquares;
		goalPosY = verticalPositions / adjacentSquares;
		
		usingVisualizer = false;
	}
	
	/**
	 * Constructor con un visualizador
	 * @author Dani
	 * @param sat 			ID del satélite.
	 * @param mapa 			mapa que se usará.
	 * @param maxDrones 	número máximo de drones que aceptará el satélite.
	 * @param v 			visualizador.
	 * @throws Exception
	 */
	public Satellite(AgentID sat, Map mapa, int maxDrones, Visualizer v) throws Exception{
		this (sat, mapa, maxDrones);		
		visualizer = v;
		usingVisualizer = true;
	}
	
	/**
	 * Hebra de recepción de mensajes
	 * @author Dani
	 * @param msg mensaje recibido.
	 */
	public void onMessage (ACLMessage msg){	
		try {
			messageQueue.put(msg);
			System.out.println("mensaje recibido!");	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Se envia un mensaje del tipo "typeMessag" al agente "id" con el contenido "datas".
	 * @author Dani
	 * FIXME: otros autores añadiros.
	 * @param typeMessage 	Tipo del mensaje
	 * @param id   			Identificador del destinatario
	 * @param protocolo		Protocolo del mensaje.
	 * @param datas			Contenido del mensaje
	 */
	private void send(int typeMessage, String protocol, AgentID id, JSONObject datas) {

		ACLMessage msg = new ACLMessage(typeMessage);
		msg.setSender(this.getAid());
		msg.addReceiver(id);
		msg.setProtocol(protocol);
		if (datas != null)
			msg.setContent(datas.toString());
		else
			msg.setContent("");
		this.send(msg);
	}	

	/**
	 * Se crea un mensaje del tipo FAIL para informar de algun fallo al agente dron.
	 * FIXME otros autores añadiros.
	 * @author Dani.
	 * @param dron 			Identificador del agente dron.
	 * @param cad_error 	Cadena descriptiva del error producido.
	 */
	private void sendError(String protocol, AgentID dron, String cad_error) {
		JSONObject error = new JSONObject();

		try {
			error.put("fail", cad_error);
		} catch (JSONException e) {
			e.printStackTrace(); // esta excepcion nunca va a suceder porque la clave siempre es fail aun asi hay que capturarla y por eso no se lo comunico al dron
		}
		System.err.println("Agente " + this.getName() + " " + cad_error);

		send(ACLMessage.FAILURE, "", dron, error);
	}
	
	/**
	 * Busca el status correspondiente a un drone.
	 * @author Dani
	 * @param droneID id del drone cuyo estatus se quiere obtener.
	 * @return el status encontrado.
	 */
	private DroneStatus findStatus (AgentID droneID){
		DroneStatus status = null;

		for (int i = 0; i < connectedDrones; i++){
			//FIXME if (drones[i] == droneID) 
			if (drones[i].toString().equals(droneID.toString()))
				status =  droneStuses[i];
		}
		
		return status;
	}
	
	/**
	 * Se calcula el valor del ángulo que forma la baliza y el EjeX horizontal tomando como centro
	 * a el agente drone.
	 * FIXME autores añadiros
	 * @param posX Posición relativa de la baliza con respecto al drone.
	 * @param posY Posición relativa de la baliza con respecto al drone.
	 * @return valor del ángulo.
	 */
	private double calculateAngle(double posX, double posY){
		double angle = 0;
		
		if(posX > 0 && posY >= 0)
			angle = Math.atan(posY / posX);
		else if(posX > 0 && posY < 0)
			angle = Math.atan(posY / posX) + (2.0*Math.PI);
		else if(posX == 0 && posY > 0)
			angle = Math.PI / 2.0;
		else if(posX == 0 && posY < 0)
			angle = (3*Math.PI) / 2.0;
		else if(posX < 0)
			angle = Math.atan(posY / posX) + Math.PI;
		
		return angle;
	}
	

	/**
	 * Este método obtiene los valores de las celdas en las 9 casillas que rodean el drone 
	 * (incluyendo en la que se encuentra el drone)
	 * FIXME autores añadiros.
	 * @return Array de enteros con las inmediaciones del drone.
	 */
	private int[] getSurroundings(DroneStatus status){
		GPSLocation gps = status.getLocation();
		int[] surroundings = new int[9];
		int posX = gps.getPositionX();
		int posY = gps.getPositionY();
		
		// Recorre desde la posición dron -1  hasta la del dron + 1, tanto en X como en Y
		for (int i = 0; i< 3; i++){
			for(int j = 0; j < 3; j++){
				surroundings[i+j*3] = mapSeguimiento.getValue(posX-1+i, posY-1+j);
			}
		}
		
		return surroundings;
	}
	
	/**
	 * Creamos el objeto JSON status:
	 * Status: {“connected”:”YES”, “ready”:”YES”, “gps”:{“x”:10,”y”:5},
	 * “goal”:”No”, “gonio”:{“alpha”:0, “dist”:4.0}, “battery”:100,
	 * “radar”:[0,0,0,0,0,0,0,1,1]}
	 * FIXME autores añadiros.
	 * @return Objeto JSon con el contenido de Status
	 * @throws JSONException  Si la clave es null
	 */
	private JSONObject createJSONStatus(DroneStatus droneStatus) throws JSONException {
		
		GPSLocation gps = droneStatus.getLocation();
		
		int posXDrone = gps.getPositionX(), posYDrone = gps.getPositionY();
		double distance = Math.sqrt(Math.pow(goalPosX - posXDrone, 2) + Math.pow(goalPosY - posYDrone, 2));
		double angle = calculateAngle(goalPosX - posXDrone, goalPosY - posYDrone);

		JSONObject status = new JSONObject();
		status.put("connected", "Yes");
		status.put("ready", "Yes");
		
		JSONObject aux = new JSONObject();
		aux.put("x", gps.getPositionX());
		aux.put("y", gps.getPositionY());

		status.put("gps", aux);

		if(mapOriginal.getValue(posXDrone, posYDrone) == Map.OBJETIVO)
			status.put("goal", "Si");
		else
			status.put("goal", "No");

		JSONObject angleAndDistance = new JSONObject();
		angleAndDistance.put("alpha", angle);
		angleAndDistance.put("dist", distance);
		status.put("gonio", angleAndDistance);
		status.put("battery", droneStatus.getBattery());
		
		int[] surroundings = getSurroundings(droneStatus);
		JSONArray jsArray = new JSONArray(surroundings);
		status.put("radar", jsArray);

		return status;
	}



	/**
	 * En función del valor recibido por el dron se actualiza el mapa interno
	 * del satelite con la nueva posición del drone (x, y en funcion de la
	 * dirección elegida) o se da por finalizada la comunicación.
	 * @author Dani
	 * FIXME: otros autores añadiros.
	 * @param droneID		Identificador del agente dron.
	 * @param ob		Objeto JSon con los valores de la decision del drone: 
	 * 					-  0 : El dron decide ir al Este. 
	 * 					-  1 : El dron decide ir al Sur. 
	 * 					-  2 : El dron decide ir al Oeste. 
	 * 					-  3 : El dron decide ir al Norte.
	 *            		- -1: Fin de la comunicación
	 * @return Se devuelve "true" si se debe finalizar la comunicación y "false" en caso contrario.
	 */
	private boolean evalueDecision(AgentID droneID, JSONObject ob) {
		//Busco el status del drone
		DroneStatus droneStatus = findStatus(droneID);
		GPSLocation gps = droneStatus.getLocation();
		
		int decision, x = -1, y = -1;

		try {
			decision = ob.getInt("decision");
		} catch (JSONException e) {
			//Cambio de P3: si el JSON no está creado el satélite devuelve NOT_UNDERSTOOD en lugar de FAILURE, ya que no es culpa del satélite.
			send(ACLMessage.NOT_UNDERSTOOD,"IMoved", droneID, null);
			return true;
		}

		switch (decision) {

		case Drone.ESTE: // Este
			x = gps.getPositionX() + 1;
			y = gps.getPositionY();
			break;

		case Drone.SUR: // Sur
			x = gps.getPositionX();
			y = gps.getPositionY() + 1;
			break;

		case Drone.OESTE: // Oeste
			x = gps.getPositionX() - 1;
			y = gps.getPositionY();
			break;

		case Drone.NORTE: // Norte
			x = gps.getPositionX();
			y = gps.getPositionY() - 1;
			break;

			//FIXME He usado los nuevos valores de fin. Estan asi porque pense que esta vez al satelite si le interesaba diferenciar.
		case Drone.END_SUCCESS:
		case Drone.END_FAIL:
			return true;
		default: // Fin, No me gusta, prefiero un case para el fin y en el default sea un caso de error pero no me deja poner -1 en el case.
			sendError("IMoved", droneID, "Error al actualizar el mapa");
			break;
		}

		try {
			gps.setPositionX(x);
			gps.setPositionY(y);
			/**
			 * @TOODauthor Dani
			 * TODO cambiar al método setValue de la clase SharedMap, añadiendo como  4º argumento la id del drone.
			 */
			mapSeguimiento.setValue(x, y, Map.VISITADO);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}


	/**
	 * Hebra de ejecución del satélite. 
	 * @author Dani
	 * FIXME otros autores añadiros.
	 */
	@Override
	protected void execute() {
		ACLMessage proccesingMessage = null;
		boolean exit = false;
		System.out.println("Agente " + this.getName() + " en ejecución");
		
		while (!exit) {
			//Si la cola de mensajes no está vacía, saca un elemento y lo procesa.
			if (!messageQueue.isEmpty()){				
				try {
					proccesingMessage = (ACLMessage) messageQueue.take();
				} catch (InterruptedException e) {
					System.out.println("¡Cola vacía!");
					e.printStackTrace();
				}
				System.out.println("Procesando mensaje: protocolo " + proccesingMessage.getProtocol());
				switch (proccesingMessage.getProtocol()){
					case "Register" : onRegister(proccesingMessage); break;
					case "SendMeMyStatus" : 
						onStatusQueried (proccesingMessage); 			
						break;
					case "IMoved" : 
						onDroneMoved (proccesingMessage); 
						break;
					case "DroneReachedGoalSubscription" : onSubscribe(proccesingMessage); break;
					case "LetMeKnowWhenSomeoneMoves" : onSubscribe(proccesingMessage); break;
					case "SendOriginalMap" : onMapQueried(proccesingMessage); break;
					case "SendSharedMap" : onMapQueried(proccesingMessage); break;
					case "SendAllDroneIDs" : onDronesIDQueried(proccesingMessage); break;
					case "SendPositionOfDrone" : onDronePositionQueried(proccesingMessage); break;
					case "SendDistanceOfDrone" : onDroneDistanceQueried(proccesingMessage); break;
					case "SendBateryOfDrone" : onDroneBatteryQueried(proccesingMessage); break;
				}		
			}
		}
	}

	

	@Override
	/**
	 * FIXME autores añdiros.
	 */
	public void finalize() {
		System.out.println("Agente " + this.getName() + " ha finalizado");
		ImgMapConverter.mapToImg("src/maps/resutado.png", mapSeguimiento);
	}

	/**
	 * Getter del mapa original.
	 * @return el mapa original.
	 */
	public Map getMapOriginal() {
		return mapOriginal;
	}

	/**
	 * Getter del mapa de seguimiento.
	 * @return el mapa de seguimiento.
	 */
	public Map getMapSeguimiento() {
		return mapSeguimiento;
	}
	
	//TODO Implementation
	public void onMapQueried (ACLMessage msg){
		
	}
	
	//TODO Implementation
	//Esto es un placeholder y el código siguiente deberá de ser borrado/comentado por quien implemente el protocolo de comunicación inicial
	public void onRegister (ACLMessage msg){
		drones[connectedDrones] = msg.getSender();
		droneStuses[connectedDrones] = new DroneStatus(msg.getSender(), "DroneP2", new GPSLocation());
		connectedDrones ++;
	}
	
	/**
	 * Rutina de tratamiento de un mensaje con el protocolo "SendMeMyStatus".
	 * Los posibles mensajes que se mandan son:
	 * - La performativa no es REQUEST => NOT_UNDERSTOOD.
	 * - Hubo error al crear el JSON con el status => FAILURE + "Error al crear Status".
	 * - Todo va bien => INFORM + JSON con el status del drone.
	 * @author Dani
	 * @param msg mensaje a tratar
	 * @return objeto JSON a mandar.
	 */
	public void onStatusQueried(ACLMessage msg) {
		/**
		 * @TODOauthor Dani
		 * TODO Esto es completamente temporal y tendrá que ser eliminado cuando se implemente el protocolo inicial.
		 */
		if (connectedDrones == 0){
			onRegister(msg);
		}
		//Si hay visualizador, manda actualizar su mapa.
		if (usingVisualizer){
			visualizer.updateMap();
			//Si no está pulsado "Find Target" y está pulsado "Think Once" hay que habilitar "Think Once". Si "Find Target" está pulsado, no se debe de hacer nada.
			if (visualizer.isBtnFindTargetEnabled() && !visualizer.isBtnThinkOnceEnabled())
				visualizer.enableThinkOnce();
		}

		if (msg.getPerformative().equals("REQUEST")){			
			//Construcción del objeto JSON			
			
			try {				
				//Mando el status en formato JSON del drone que me lo solicitó.
				send (ACLMessage.INFORM, "SendMeMyStatus", msg.getSender(), createJSONStatus(findStatus(msg.getSender())));		

				System.out.println("Mensaje mandado con su status.");
			} catch (JSONException e) {
				//Si hubo error al crear el objeto JSOn se manda un error.
				e.printStackTrace();
				sendError("SendMeMyStatus", msg.getSender(), "Error al crear Status");
			}
		}
		else{
			// El mensaje recibido es de tipo distinto a Request, se manda un not understood.
			send(ACLMessage.NOT_UNDERSTOOD, "SendMeMyStatus", msg.getSender(), null);
		}
	}
	
	/**
	 * Rutina de tratamiento de un mensaje con el protocolo "IMoved"
	 * Los posibles mensajes que se mandan son:
	 * - La performativa no es REQUEST => NOT_UNDERSTOOD.
	 * - Falla al crear el objeto JSON con el contenido del mensaje => FAILURE + "Error al crear objeto JSON con la decision".
	 * - El drone ha metido un valor inválido en la decisión => NOT_UNDERSTOOD (se manda en evalueDecision).
	 * - Hay un fallo al actualizar el mapa => FAILURE + "Error al actualizar el mapa".
	 * - Todo va bien => INFORM.
	 * @author Dani
	 * @param msg mensaje a tratar.
	 */
	public void onDroneMoved(ACLMessage msg) {
		if (msg.getPerformative().equals("REQUEST")){
			if (usingVisualizer)
				if (visualizer.isBtnThinkOnceEnabled())
					while (visualizer.isBtnThinkOnceEnabled()){
						System.out.print("");//Necesario para volver a comprobar la condición del while.
					}

			JSONObject aux = null;
			try {
				aux = new JSONObject(msg.getContent());
			} catch (JSONException e) {
				sendError("IMoved", msg.getSender(),"Error al crear objeto JSON con la decision");
			}

			/**
			 * @TODOauthor Dani
			 * TODO no se debe de salir, sino que debe de gestionar la llegada del drone al objetivo.
			 */
			exit = evalueDecision(msg.getSender(), aux);
			//FIXME if (!exit) (enviar incluso cuando ha terminado
				send(ACLMessage.INFORM, "IMoved", msg.getSender(), null);
		}
		else{
			// El mensaje recibido es de tipo distinto a Request, se manda un not understood.
			send(ACLMessage.NOT_UNDERSTOOD, "IMoved", msg.getSender(), null);
		}		
	}
	
	//TODO Implementation
	public void onDronePositionQueried (ACLMessage msg){
	}
	
	//TODO Implementation
	public void onDronesIDQueried (ACLMessage msg){
	}
	
	//TODO Implementation
	public void onSubscribe (ACLMessage msg){
	}
	
	//TODO Implementation
	public void onDroneBatteryQueried (ACLMessage msg){
	}
	
	//TODO Implementation
	public void onDroneDistanceQueried (ACLMessage msg){
	}
}
