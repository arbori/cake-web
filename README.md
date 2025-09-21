# Cake Web
Cake framework focuses on web development, striving for maximum simplicity. Unlike many other frameworks, which often burden developers with extensive libraries, numerous annotations, and complex configuration files that obscure the underlying Java code, Cake Web aims to achieve two key goals. Firstly, it seeks to be a framework where developers only need a solid understanding of Java code and the HTTP protocol to construct an API. Secondly, efficiency is paramount. The more layers of code separating an HTTP request from the API's core functionality, the less efficient the processing becomes. Therefore, our solution was to significantly minimize the framework's own codebase.

Clean Architecture principles emphasize that an application's design should inherently communicate its function. When developers are compelled to use an abundance of annotations, configuration files, and various interface implementations, the application's actual purpose can easily get lost in the noise. To address this, we've made a deliberate choice to rely solely on the Java language's capabilities for handling incoming HTTP requests and routing them to the appropriate business logic. This approach makes API description entirely Java-centric. For example, imagine you need to respond to a request for `/theapi/thepath/theresource`. You would simply define a package named `theapi.thepath` and a class within it called `TheResource`. There's no need for this class to implement any interfaces or extend other classes; instead, each HTTP verb required for the API is handled by a method in the class bearing the same name as the verb.

---

## Resource Controller Specification

Cake Web controllers (resources) are plain Java classes with **minimal requirements**.  
The framework resolves an incoming HTTP request into a controller instance, sets attributes, and invokes the appropriate method following the rules below.

### 1. Class Location and Naming
- The package path corresponds to the request path segments.  
  Example: request `/cakeweb/com/bank/loan/customer` → class `com.bank.loan.Customer`.  
- Class names must be capitalized versions of the path token.  

### 2. Constructor
- Controllers must provide a **public no-argument constructor**.  
- Dependency injection, if needed, should be handled explicitly by the developer inside the constructor.

### 3. Query Parameters
- Query parameters are mapped to **setter methods** on the controller.  
- A setter follows standard Java conventions:  
  ```java
  public void setName(String name) { ... }
- Only the first value of each query parameter is considered.
- Type conversion is supported for standard types (String, Integer, Long, Double, Boolean, etc.).

### 4. Path Parameters
- Path parameters (extra tokens after the resource name) are mapped to the arguments of the controller’s HTTP verb method (get, post, etc.).
- Example:
'''swift
Request: GET /cakeweb/com/bank/loan/customer/1
Matches:
'''java
public CustomerResult get(Integer customerId) { ... }

### 5. HTTP Methods
- A controller defines public methods named after the HTTP verb (get, post, put, delete).
- The number and type of parameters must match (or be convertible from) the path parameters.
- Return value:
    - May be a DTO or result object (terminal resource).
    - May be another resource (to chain deeper into the URI).

### 6. Exemple
'''java
package com.bank.loan;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.service.CustomerService;
import cake.web.exchange.ParameterNotFoundException;

public class Customer {
    private CustomerService customerService = new CustomerService();

    private String name;
    private String email;

    // query parameters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    // path parameter
    public CustomerResult get(Integer customerId) throws ParameterNotFoundException {
        if (customerId == null) {
            throw new ParameterNotFoundException("customerId is required");
        }

        CustomerDTO dto;
        if (name != null && !name.isEmpty() && email != null && !email.isEmpty()) {
            dto = customerService.getCustomerByIdNameEmail(customerId, name, email);
        } else {
            dto = customerService.getCustomerById(customerId);
        }

        return new CustomerResult(dto.getCustomerId(), dto.getName(), dto.getEmail());
    }
}

### Performance Considerations
One of Cake Web’s core goals is efficiency. Reflection is powerful but can be expensive when performed repeatedly.
To mitigate this, Cake Web employs caching of class lookups and method resolutions:

- Class Cache
When a request path is resolved into a fully qualified class name (FQCN), the result is cached. Future requests for the same URI segment will reuse the cached class reference instead of performing another Class.forName(...) lookup.

- Method Cache
When a resource method (e.g., get(Integer)) is resolved based on path parameters, the framework stores this resolution in a cache.
Subsequent requests with the same resource and compatible parameters will directly reuse the cached Method object.

This means:
- The first request to a new resource may be slightly slower due to reflection and analysis.
- Subsequent requests are significantly faster because they bypass most of the reflection work.
Unit tests are included to verify this behavior, checking that the average execution time for repeated requests is reduced, ensuring the caching layer delivers consistent performance improvements.

---

### Summary
- No annotations.
- No interfaces.
- No XML/JSON configuration.
- Pure Java classes, discovered by convention.
- Caching built-in for efficiency.

This contract ensures maximum simplicity while preserving flexibility and runtime performance.
