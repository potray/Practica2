package practica.util;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import practica.Launcher;
import practica.agent.Satellite;
import practica.map.Map;

public class Visualizer extends JFrame {
	private JComboBox <String> mapSelector;
	private JButton btnLoadMap;
	private JButton btnThinkOnce;
	private JButton btnFindTarget;
	private JLabel miniMap;
	private JLabel satelliteMapIcon;
	private Map mapToLoad;
	private Launcher launcher;
	
	private Satellite satellite;
	
	/**
	 * Setter de satelite.
	 * @param sat satélite para poder comunicarse con él.
	 */
	public void setSatelite(Satellite sat){
		satellite = sat;
	}
	
	/**
	 * Getter del mapa.
	 * @return el mapa que ha cargado.
	 */
	public Map getMapToLoad(){
		return mapToLoad;
	}
	
	/**
	 * Constructor. Inicializa componentes y se hace visible.
	 */
	public Visualizer(Launcher l) {		
		initialize();
		launcher = l;
		setVisible(true);			
	}
	
	/**
	 * Crea todos los componentes, los coloca, y asigna los eventos.
	 */
	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 250, 300);
		
		//Meter los nombres de los mapas
		File f = new File ("src/maps");
		String [] mapNames = f.list();
		getContentPane().setLayout(null);
		mapSelector = new JComboBox <String> (mapNames);
		mapSelector.addActionListener(new MapSelectorActionListener());
		
		satelliteMapIcon = new JLabel("");
		satelliteMapIcon.setBounds(10, 10, 500, 500);
		getContentPane().add(satelliteMapIcon);
		mapSelector.setBounds(10, 11, 112, 20);
		getContentPane().add(mapSelector);
		
		btnLoadMap = new JButton("Load map");
		btnLoadMap.addActionListener(new BtnLoadMapActionListener());
		btnLoadMap.setEnabled(false);
		btnLoadMap.setBounds(129, 10, 96, 23);
		getContentPane().add(btnLoadMap);
		
		miniMap = new JLabel("");
		miniMap.setBounds(10, 44, 210, 210);
		getContentPane().add(miniMap);
		
		btnThinkOnce = new JButton("Think once");
		btnThinkOnce.addActionListener(new BtnThinkOnceActionListener());
		btnThinkOnce.setBounds(10, 528, 190, 23);
		getContentPane().add(btnThinkOnce);
		
		btnFindTarget = new JButton("Find target");
		btnFindTarget.addActionListener(new BtnFindTargetActionListener());
		btnFindTarget.setBounds(320, 528, 190, 23);
		getContentPane().add(btnFindTarget);
	}
	
	/**
	 * Activa el botón "Think Once"
	 */
	public void enableThinkOnce(){
		btnThinkOnce.setEnabled(true);
	}
	
	/**
	 * Mira si el botón "Think Once" está habilitado.
	 * @return true si está deshabilitado (y por lo tanto se pulsó). False si no.
	 */
	public boolean isBtnThinkOnceEnabled(){
		return btnThinkOnce.isEnabled();
	}
	
	/**
	 * Mira si el botón "Find target" está habilitado.
	 * @return true si está deshabilitado (y por lo tanto se pulsó). False si no.
	 */
	public boolean isBtnFindTargetEnabled(){
		return btnFindTarget.isEnabled();
	}
	
	/**
	 * Actualiza el mapa.
	 */
	public void updateMap(){
        satelliteMapIcon.setIcon(new ImageIcon(ImgMapConverter.mapToScalatedImg(satellite.getMapSeguimiento(), 500, 500)));
	}
	
	private class MapSelectorActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnLoadMap.setEnabled(true);
			ImageIcon mapIcon = new ImageIcon(Visualizer.class.getResource("/maps/" + mapSelector.getSelectedItem().toString()));
			//Me creo una imagen a partir de la del icono
			Image img = mapIcon.getImage();
			//Me creo otra reescalándola.
			Image scalatedImg = img.getScaledInstance(210, 210, Image.SCALE_SMOOTH);
			//Se la asigno al icono
			mapIcon.setImage(scalatedImg);
			//Asigno el icon al label
			miniMap.setIcon(mapIcon);
		}
	}
	private class BtnLoadMapActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			setBounds(100, 100, 550, 600);
			mapSelector.setVisible(false);
			miniMap.setVisible(false);
			btnLoadMap.setVisible(false);
	        
	        mapToLoad = ImgMapConverter.imgToMap("src/maps/" + mapSelector.getSelectedItem().toString());
	        launcher.launch();			
		}
	}
	private class BtnFindTargetActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnThinkOnce.setEnabled(false);
			btnFindTarget.setEnabled(false);
			System.out.println("Botones desactivados");
		}
	}
	private class BtnThinkOnceActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			btnThinkOnce.setEnabled(false);
		}
	}
}
