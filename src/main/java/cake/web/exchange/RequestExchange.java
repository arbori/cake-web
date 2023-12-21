package cake.web.exchange;

import java.lang.reflect.Parameter;
import java.util.Set;

public class RequestExchange {
    /**
     * 
     */
    private RequestExchange() {
    }

    /**
     * 
     * @param keySet
     * @param parameters
     * @return
     */
    public static long similarity(Set<String> keySet, Parameter[] parameters) {
        int result = 0;
        String name = null;

        for(Parameter parameter: parameters) {
            if((name = retriveNameFromParameter(parameter)) == null) {
                name = retriveNameFromAnotation(parameter);
            }

            if(keySet.contains(name)) {
                result++;
            }
        }
        
        return result;
    }

    static String retriveNameFromParameter(Parameter parameter) {
        if(parameter.isNamePresent()) {
            return parameter.getName();
        }

        return null;
    }

    static String retriveNameFromAnotation(Parameter parameter) {
        ParamInfo paramInfo = parameter.getAnnotation(ParamInfo.class);
                
        if(paramInfo != null) {
            return paramInfo.name();
        }

        return null;
    }
}
