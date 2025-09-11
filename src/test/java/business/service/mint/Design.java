package business.service.mint;

import java.util.Date;

public class Design {
	public static final String METHOD_GET_P0 = "public String get()";
	public static final String METHOD_GET_P1 = "public String get(int id)";
	public static final String METHOD_GET_P3 = "public String get(int id, String name, String address)";
	public static final String METHOD_GET_P5 = "public String get(int id, String name, String address, float x, Date register)";
	
    /**
     * @return
     */
    public String get() {
        return METHOD_GET_P0;
    }
	
    /**
     * @param id
     * @return
     */
    public String get(Integer id) {
        return METHOD_GET_P1;
    }

    /**
     * @param id
     * @param name
     * @param address
     * @return
     */
    public String get(int id, String name, String address) {
        return METHOD_GET_P3;
    }

    /**
     * @param id
     * @param name
     * @param address
     * @return
     */
    public String get(int id, String name, int idede) {
        return METHOD_GET_P3;
    }

    /**
     * @param id
     * @param name
     * @param address
     * @param x
     * @param register
     * @return
     */
    public String get(int id, String name, String address, float x, Date register) {
        return METHOD_GET_P5;
    }
}
