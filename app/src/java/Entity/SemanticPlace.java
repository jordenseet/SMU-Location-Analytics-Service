package Entity;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author G7T4
 */
public class SemanticPlace{
	private String name;
	private int locationId;
	
    /**
     *create semantic place object
     * @param locationId
     * @param name
     */
    public SemanticPlace(int locationId, String name) {
		this.locationId = locationId;
                this.name = name;
	}
	
    /**
     *get semantic place name
     * @return name
     */
    public String getName(){
		return name;
	}
	
    /**
     *get location id
     * @return location id
     */
    public int getLocationId(){
		return locationId;
	}
	
    /**
     *get valid name
     * @param name
     * @return true if it is a valid name, else return false
     */
    public boolean validName(String name) {
		try {
			String building = name.substring(0,7);
			char level = name.charAt(7);
			String place = name.substring(8);
			
			if (!building.equals("SMUSISL") && !building.equals("SMUSISB") || !Character.isDigit(level)) {
				return false;
			}
			return true;
		} catch (IndexOutOfBoundsException ioob) {
			return false;
		}
	}
	
    /**
     *valid location ID
     * @param locationId
     * @return true if it is valid, else return false
     */
    public boolean validLocationId(int locationId) {
		String locId = "" + locationId;
		if (locId.matches("^\\d+$")) {
			return true;
		}
		return false;
	}
}