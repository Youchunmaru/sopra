package messages.configuration;


import enums.FieldType;
import logic.game.map.Field;

import java.util.logging.Logger;

import static java.util.logging.Level.INFO;


/**
 * The ScenarioConfig class represents the object representation of the scenarioconfiguration json file.
 * Its contents describe the initial state of the game map.
 * The file ending should be .scenario.json.
 * The Field types can be :
 * CITY
 * MOUNTAINS
 * PLATEAU
 * DUNE
 * FLAT_SAND
 * HELIPORT
 * @author Samuel Gröner, Janine Grimmer, Julian Korinth
 */
public class ScenarioConfig {
    public String[][] scenario;

    public ScenarioConfig(String[][] scenario) {
        this.scenario = scenario;
    }
    //always null
    private Field[][] scenarioFields;

    /**
     * Constructor for new scenario configuration
     *
     * @param sizeX positive integer, size of columns of game map
     * @param sizeY positive integer, size of rows of game map
     * @author Janine Grimmer
     */
    public ScenarioConfig(int sizeX, int sizeY) {
        scenarioFields = new Field[sizeX][sizeY];
    }

    /**
     * Method to get scenario config
     *
     * @return Field[][] that contains the start scenario of the game map
     * @author Janine Grimmer
     */
    public Field[][] getScenario() {
        return scenarioFields;
    }

    /**
     * Method to add fields to the scenario / start game map
     *
     * @param field field to be added
     * @param x     column position where field shall be added
     * @param y     row position where field shall be added
     *              @author Janine Grimmer
     */
    public void add(Field field, int x, int y) {
        Logger.getAnonymousLogger().log(INFO, "Index of x = {0} and y = {1}",
                new Object[]{x,y});
        scenarioFields[x][y] = field;
    }


    /***
     * Used to print current game map on console.
     * @return String that contains the game map
     * @author Janine Grimmer
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < scenarioFields.length; i++) {
            stringBuilder.append("\n");
            for (int j = 0; j < scenarioFields.length; j++) {
                stringBuilder.append(" " + scenarioFields[i][j].getFieldType().toString());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Called to make scenario as two dimensional string array. Needed in setup phase.
     * @return two-dimensional string array
     * @author Janine Grimmer
     */
    public String[][] asString() {
        String[][] scenarioArray = new String[scenarioFields[0].length][scenarioFields[1].length];
        for (int i = 0; i < scenarioFields[0].length; i++) {
            for (int j = 0; j < scenarioFields[1].length; j++) {
                scenarioArray[i][j] = scenarioFields[i][j].toString();

            }
        }
        return scenarioArray;
    }

    /**
     * Called to make scenario as two dimensional field type array. Needed in setup phase to generate game map.
     * @return two-dimensional {@link FieldType} array
     * @author Janine Grimmer
     */
    public FieldType[][] asFieldType(){
        FieldType[][] typeScenario = new FieldType[scenarioFields[0].length][scenarioFields[1].length];
        for (int i = 0; i < scenarioFields[0].length; i++) {
            for (int j = 0; j < scenarioFields[1].length; j++) {
                typeScenario[i][j] = scenarioFields[i][j].getFieldType();
            }
        }
        return typeScenario;
    }
}
