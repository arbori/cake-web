package cake.web.exchange;

import java.util.Map;
import java.util.Optional;

public class GetRequestExchange extends AbstractRequestExchange {
    public GetRequestExchange(String requestURI, String contextPath) {
        super(requestURI, contextPath);
    }

    public Object get(Map<String, String[]> queryParameters) throws NoSuchMethodException {
        for (int baseEnd = 0; baseEnd <= tokens.size() - 1; baseEnd++) {
            String basePackage = join(tokens.subList(0, baseEnd + 1), ".");
            int nextIndex = baseEnd + 1;
            try {
                Optional<Object> result = resolveChainWithBase(basePackage, nextIndex, queryParameters, null, "get");
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (ClassNotFoundException e) {
                // try next base split
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                // try next base split
            }
        }

        throw new NoSuchMethodException("No resource chain resolved for URI: " + requestURI);
    }
}
