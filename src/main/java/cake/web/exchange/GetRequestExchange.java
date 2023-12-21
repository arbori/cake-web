package cake.web.exchange;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetRequestExchange {
	private Class<?> getClass;
	private List<Method> getMethods;
	
	/**
	 * 
	 * @param requestURI
	 * @param contextPath
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public GetRequestExchange(String requestURI, String contextPath) throws ClassNotFoundException, NoSuchMethodException {
        getClass = extractClass(requestURI, contextPath);

        getMethods = extractGetMethod(getClass);
	}

	/**
	 * 
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException
	 */
	public String get(Map<?, ?> parameters) throws NoSuchMethodException {
		Method getMethod = null;
		
		if(parameters.size() == 0) {
            for(Method method: getMethods) {
               if("get".equals(method.getName()) && method.getParameterCount() == 0) {
                   getMethod = method;
               }
            }

	        if(getMethod == null) {
	        	 throw new NoSuchMethodException("There is no get method without parameters.");
	        }
	        
	        return callGetMethod(getClass, getMethod);
		}
		else {
			throw new NoSuchMethodException("Not implemented support for get method with parameters.");
		}
	}


    /**
     * 
     */
    private Class<?> extractClass(String uri, String contextPath) throws ClassNotFoundException {
        if(uri == null || uri.isEmpty() || contextPath == null || contextPath.isEmpty()) {
            throw new ClassNotFoundException("Correspondent class not found.");
        }

        String uriClassName = uri.substring(uri.indexOf(contextPath) + contextPath.length(), uri.length())
        		.replace("/", ".");

        int letterToUpperPossition = uriClassName.lastIndexOf(".") + 1;

        byte[] uriClassNameBytes = uriClassName.getBytes();

        if (uriClassNameBytes[letterToUpperPossition] > (byte) 'Z') {
            uriClassNameBytes[letterToUpperPossition] -= (byte) 32;
        }

        return Class.forName(new String(uriClassNameBytes));
    }

    /**
     * @throws NoSuchMethodException 
     * 
     */
    private List<Method> extractGetMethod(Class<?> getClass) throws NoSuchMethodException {
        if(getClass == null) {
            throw new NoSuchMethodException("The get class is null.");
        }

    	ArrayList<Method> result = new ArrayList<>();
    	
        Method[] methods = getClass.getMethods();

        for (Method method : methods) {
            String methodName = method.getName();

            if ("get".equals(methodName)) {
            	result.add(method);
            }
        }
        
        if(result.isEmpty()) {
        	 throw new NoSuchMethodException("Get method not found.");
        }

        return result;
    }
    
    /**
     * 
     * @param getMethod
     * @return
     * @throws ClassNotFoundException
     */
    private String callGetMethod(Class<?> getClass, Method getMethod) {
        Object objectGet = null;

        try {
            Constructor<?> constructor = getClass.getConstructor();
            
            objectGet = constructor.newInstance();
        } catch (NoSuchMethodException |SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
            e1.printStackTrace();

            return "";
        } 

        Object message = null;

        try {
            message = getMethod.invoke(objectGet);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            
            message = "System erro call method get!";
        }

        return message.toString();
    }
}
