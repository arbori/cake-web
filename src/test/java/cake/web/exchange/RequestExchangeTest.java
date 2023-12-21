package cake.web.exchange;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import business.service.mint.GetWithParameters;
import business.service.mint.GetWithoutParam;

/**
 * An incoming GET call can contain a set of parameters with information for the query. Cake-Web seeks to associate this call to one of the get's methods implemented in the class that represents the endpoint.
 * 
 * Given the set of parameters of the call and one of the analyzed get methods, the similarity is the number of parameters with the same name in both sets. That is, it is the size of the intersection of the two sets.
 */
public class RequestExchangeTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
    }

	/**
	 * The similarity is a meansure between two sets of names. If the method 
     * have no parameter, the similarity will be allways 0
	 */
	@Test
	public void testSimilarityGetWithoutParam() {
        long similarityExpected;
        long similarityActual;

        similarityExpected = 0;
        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("id")), 
            GetWithoutParam.class.getMethods()[0].getParameters());

        assertEquals(similarityExpected, similarityActual);
    }

	/**
	 * Calculating the similarity of two sets has to do with matching without repetition. The GET call parameters can has only one of than in common, or two, three, or all of than.
     * 
     * This test the similarity for all possibility, or in a better way, the enoght conditions to this computation be confidentable. 
	 */
	@Test
	public void testSimilarityGetWithParam() {
        long similarityExpected;
        long similarityActual;

        Method[] methods = GetWithParameters.class.getMethods();
        Parameter[] parameters = methods[0].getParameters();

        Map<String, Parameter> mapParam = new HashMap<String, Parameter>(parameters.length);

        for(Parameter param: parameters) {
            mapParam.put(RequestExchange.retriveNameFromAnotation(param), param);
        }

        similarityExpected = 1;
        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("id")), 
            parameters);

        assertEquals(similarityExpected, similarityActual);

        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("name")), 
            parameters);

        assertEquals(similarityExpected, similarityActual);

        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("salary")), 
            parameters);

        assertEquals(similarityExpected, similarityActual);

        similarityExpected = 2;
        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("id", "name")), 
            parameters);

        assertEquals(similarityExpected, similarityActual);

        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("name", "salary")), 
            parameters);

        assertEquals(similarityExpected, similarityActual);

        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("id", "salary")), 
            parameters);

        assertEquals(similarityExpected, similarityActual);

        similarityExpected = 3;
        similarityActual = RequestExchange.similarity(
            new HashSet<>(Arrays.asList("id", "name", "salary")), 
            parameters);

        assertEquals(similarityExpected, similarityActual);
    }
}
