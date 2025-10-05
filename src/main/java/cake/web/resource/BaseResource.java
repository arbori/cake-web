package cake.web.resource;

import cake.web.exchange.content.ParserJson;

/**
 * Base class for resource classes to handle request body content
 */
public class BaseResource {
    private Object bodyObject;

    public Object getBodyObject() {
        return bodyObject;
    }
    
    public void setBodyObject(Object bodyObject) {
        this.bodyObject = bodyObject;
    }

    public void setBodyContent(String bodyContent) {
        ParserJson.parseJsonToObject(bodyObject, bodyContent);
    }
}
