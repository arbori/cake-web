
# Cake Web Framework

**Cake Web** is a lightweight, annotation-free Java web framework built on the principle that **web development should be simple, explicit, and purely Java-based**. No annotations, no XML, no complex configuration — just clean Java code and HTTP conventions.

## Philosophy

Cake Web was created with two core goals:

1. **Simplicity**: Developers should only need to understand Java and HTTP to build an API. The framework's behavior is driven by naming conventions, not magic.

2. **Efficiency**: The framework minimizes layers between HTTP requests and business logic, reducing overhead and making the codebase transparent.

## Key Features

- ✅ **No annotations** — Pure Java convention-based routing
- ✅ **No interfaces required** — Resources are plain Java classes
- ✅ **No configuration files** — Everything is discovered by naming
- ✅ **HTTP method mapping** — Method names match HTTP verbs (get, post, put, delete, etc.)
- ✅ **URI to class mapping** — Package structure maps to URL paths
- ✅ **Multi-source parameter binding** — Path, body, query, and headers as method parameters
- ✅ **Type conversion** — Automatic string to Java type conversion
- ✅ **Method resolution caching** — Performance optimization
- ✅ **All HTTP methods** — GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH
- ✅ **Built-in Tomcat** — No external server required
- ✅ **Exception mapping** — Clean error handling with HTTP status codes

---

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>cake.web</groupId>
    <artifactId>cake-web</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Create a Resource

```java
package com.mybank.loan;  // Maps to /com/mybank/loan

public class Customer {
    // GET /com/mybank/loan/customer/123
    public CustomerResponse get(Integer customerId) {
        return customerService.findById(customerId);
    }

    // POST /com/mybank/loan/customer
    public CustomerResponse post(CustomerRequest request) {
        return customerService.create(request);
    }
}
```

### 3. Start the Application

```java
public class Application {
    public static void main(String[] args) throws Exception {
        CakeWebApplication.run(config -> {
            config.setPort(8080);
            config.setContextPath("/api");
        });
    }
}
```
## Resource Mapping
### URI to Class Mapping

The framework maps URL paths to Java classes using package names and class names:

|URL Path|Package|Class
|--|--|--|
|/com/bank/loan/customer|com.bank.loan|Customer|  
|/com/bank/loan/customer/123|com.bank.loan|Customer|
|/com/bank/loan/address|com.bank.loan|Address|  

**Rules:**
-   Each path segment becomes a package segment
-   The final segment becomes the class name (capitalized)
-   The framework searches from the root package upward

### HTTP Method Mapping
Method names must match HTTP verbs (case-insensitive):

|HTTP Method| Java Method
|--|--
GET| get()
POST| post()
PUT| put()
DELETE| delete()
PATCH| patch()
HEAD| head()
OPTIONS| options()
TRACE| trace()
CONNECT| connect()

**Example:**
```java
public class Customer {
    public CustomerResponse get(Integer id) { /* GET request */ }
    public CustomerResponse post(CustomerRequest req) { /* POST request */ }
    public CustomerResponse put(Integer id, CustomerRequest req) { /* PUT request */ }
    public void delete(Integer id) { /* DELETE request */ }
}
```

## Parameter Binding
Cake Web supports **four sources** of HTTP request data as method parameters. The framework identifies the source based on the parameter type.

### 1. Path Parameters
Path parameters are the **first N parameters** in the method signature, where N is the number of extra path segments after the resource class.

**Example:**
```
URL: /com/bank/loan/customer/123/order/456
Tokens: ["com", "bank", "loan", "customer", "123", "order", "456"]
Resource: com.bank.loan.Customer
Path parameters: ["123"]
Resource: com.bank.loan.Order
Path parameters: ["456"]
```
```java
public class Customer {
    public CustomerResponse get(Integer customerId) {
        // customerId = 123, orderId = 456
    }
}

public class Order {
    public OrderResponse get(CustomerResponse customerResponse, Integer orderId) {
        // customerResponse, orderId = 456
    }
}
```

**Supported Path Parameter Conversion Types:**
-   `Byte`, `Short`, `Integer`, `Long`, `BigInteger`
-   `Float`, `Double`, `BigDecimal`
-   `Boolean`
-   `String`
-   `UUID`
-   `LocalDate`, `LocalTime`, `LocalDateTime`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`

### 2. Body Content (`BodyContent`)
Mark a class with `BodyContent` to bind the HTTP request body (JSON) to a method parameter.

```java
import cake.web.exchange.content.BodyContent;

public class CustomerRequest implements BodyContent {
    private String name;
    private Double salary;
    // getters and setters
}
```

**Resource method:**
```java
public class Customer {
    // POST /com/bank/loan/customer
    public CustomerResponse post(CustomerRequest request) {
        // request is populated from JSON body
    }
}
```

**Expected JSON:**
```java
{
    "customerRequest": {
        "name": "John Doe",
        "salary": 50000.00
    }
}
```
**Key Convention:** The JSON must contain an object with the key matching the class name in lowercase (e.g., `"customerRequest"` for `CustomerRequest`).

### 3. Query Parameters (`QueryParamContent`)
Mark a class with `QueryParamContent` to bind URL query parameters to a method parameter.
```java
import cake.web.exchange.content.QueryParamContent;

public class CustomerQuery implements QueryParamContent {
    private String city;
    private Integer minAge;
    // getters and setters
}
```
**Resource method:**
```java
public class Customer {
    // GET /com/bank/loan/customer?city=SaoPaulo&minAge=18
    public List<CustomerResponse> get(CustomerQuery query) {
        // query.getCity() = "SaoPaulo"
        // query.getMinAge() = 18
    }
}
```
### 4. Headers (`HeaderContent`)
Mark a class with `HeaderContent` to bind HTTP headers to a method parameter.
```java
import cake.web.exchange.content.HeaderContent;

public class RequestHeaders implements HeaderContent {
    private String authorization;
    private String traceId;
    // getters and setters
}
```

**Resource method:**
```java
public class Customer {
    // GET with headers: Authorization: Bearer xxx, Trace-Id: abc123
    public CustomerResponse get(Integer id, RequestHeaders headers) {
        // headers.getAuthorization() = "Bearer xxx"
        // headers.getTraceId() = "abc123"
    }
}
```
**Header Field Matching:**
-   Field `authorization` matches header `Authorization` (case-insensitive)
-   Field `traceId` matches header `Trace-Id` (camelCase to kebab-case)

## Method Signature Rules

A valid resource method must follow these rules:

### 1. Parameter Order

Parameters must appear in this order:

```text
method(PATH_PARAMS..., [BODY], [QUERY], [HEADER])
```
**Examples:**

```java
// Valid
public void get(Integer id, CustomerQuery query)           // PATH + QUERY
public void post(Integer id, CustomerRequest body)         // PATH + BODY
public void put(Integer id, CustomerRequest body, RequestHeaders headers) // PATH + BODY + HEADER

// Invalid (order violation)
public void get(CustomerQuery query, Integer id)           // QUERY before PATH
public void post(CustomerRequest body, Integer id)         // BODY before PATH
```
### 2. Parameter Limits

-   **At most one**  `BodyContent` parameter
-   **At most one**  `QueryParamContent` parameter
-   **At most one**  `HeaderContent` parameter    
-   Any number of path parameters

### 3. Overload Restriction

**A resource class cannot have multiple methods with the same HTTP verb name and same number of parameters.**

```java
public class Customer {
    // ❌ Ambiguous (both have 1 parameter)
    public void get(Integer id) { }
    public void get(String name) { }
    
    // ✅ Allowed (different parameter counts)
    public void get() { }                // 0 params
    public void get(Integer id) { }      // 1 param
    public void get(Integer id, String flag) { } // 2 params
}
```
### 4. Visibility

-   Method must be **public**
-   Method must be **non-static**
-   Class must have a **public no-argument constructor**

### 5. Primitive Types

**Primitive types (int, long, boolean, etc.) are NOT allowed** as method parameters. Use wrapper types (Integer, Long, Boolean, etc.) to represent null values for missing parameters.
```java
public class Customer {
    // ❌ Not allowed
    public void get(int id) { }
    
    // ✅ Correct
    public void get(Integer id) { }
}
```
## Parent Resource Injection

When a URI has nested resources, parent resources are automatically instantiated and passed to child resources as a parameter.

**Example:**
```text
URL: /com/bank/loan/customer/123/proposal/456
Resource chain: Customer → Proposal
```

```java
package com.bank.loan;

public class Customer {
    public CustomerResponse get(Integer customerId) {
        // Returns CustomerResponse object for ID 123
    }
}

public class Proposal {
    // Parent resource result (CustomerResponse) is passed as first parameter
    public ProposalResponse get(CustomerResponse customerResponse, Integer proposalId) {
        // customerResponse is the result of Customer.get(123)
        // proposalId = 456
    }
}
```
**How it works:**

1.  Framework instantiates `Customer`
2.  Calls `Customer.get(123)` → returns `CustomerResponse` object
3.  Passes `CustomerResponse` object as first parameter to `Proposal.get()`
4.  Calls `Proposal.get(customerResponse, 456)`

## Caching

Cake Web uses **two-level caching** for performance:

### 1. Class Cache

The framework caches loaded classes by fully qualified name. Subsequent requests for the same URI segment reuse the cached class reference.

### 2. Method Cache

Method resolution (which method to call based on parameter types) is cached. Subsequent requests with the same parameter types reuse the cached method.

**Cache Key Format:**
```text
<className>#<parameterTypeHints>#<httpMethodName>
```
**Example:**
```text
com.bank.loan.Customer#integer#get
com.bank.loan.Customer#java.lang.String,java.lang.Integer#post
```
**Performance Impact:**

-   First request to a new endpoint: Slower (reflection and resolution)
    
-   Subsequent requests: **Significantly faster** (cache hit)
    

----------

## Exception Handling

### Exception Hierarchy
```text
Throwable
├── RuntimeException
│   ├── FrameworkException (infrastructure errors → HTTP 500)
│   │   ├── ResourceResolutionException
│   │   ├── MethodInvocationException
│   │   └── PrimitiveNotAllowedException
│   └── BusinessException (business errors → HTTP 4xx)
│       ├── BadRequestException (→ HTTP 400)
│       ├── NotFoundException (→ HTTP 404)
│       └── (custom business exceptions)
├── Exception
│   ├── AmbiguityException (→ HTTP 500)
│   ├── HttpMethodException (→ HTTP 405)
│   └── ParameterNotFoundException (→ HTTP 404)
```
### Custom Exception Mapper

Implement `ExceptionMapper` to handle exceptions:
```java
public class MyExceptionMapper implements ExceptionMapper {
    @Override
    public void handle(Throwable ex, HttpServletResponse response) {
        if (ex instanceof MyBusinessException) {
            response.setStatus(422);
            response.getWriter().println("Business error: " + ex.getMessage());
        }
    }
}
```
## Configuration

### CakeWebConfig
```java
CakeWebApplication.run(config -> {
    config.setPort(8080);                 // Server port
    config.setContextPath("/api");         // Context path
    config.setBaseDir("/tmp/cake-web");    // Base directory for Tomcat
});
```
### Default Values

Property

Default

Port

8080

Context Path

""

Base Directory

`java.io.tmpdir`

----------

## Limitations

Limitation

Explanation

**No annotations**

All behavior is convention-based

**No method overloading**

Only one method per HTTP verb + parameter count

**No primitive types**

Use wrapper types (Integer, Long, Boolean)

**Parameter order enforced**

PATH params first, then BODY, QUERY, HEADER

**At most one of each**

Only one BodyContent, QueryParamContent, HeaderContent per method

**JSON format**

Body content must be wrapped: `{"className": {...}}`

**No file uploads**

Coming soon

**No List support**

Coming soon

----------

## Examples

### Complete Customer Resource
```java
package com.bank.loan;

import cake.web.exception.NotFoundException;
import cake.web.exchange.content.BodyContent;
import cake.web.exchange.content.HeaderContent;
import cake.web.exchange.content.QueryParamContent;

public class Customer {
    private final CustomerService service = new CustomerService();

    // GET /customer/123
    public CustomerResponse get(Integer id) throws NotFoundException {
        return service.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    // GET /customer?city=SaoPaulo
    public List<CustomerResponse> get(CustomerQuery query) {
        return service.findByCity(query.getCity());
    }

    // POST /customer
    public CustomerResponse post(CustomerRequest request) {
        return service.create(request);
    }

    // PUT /customer/123
    public CustomerResponse put(Integer id, CustomerRequest request) {
        return service.update(id, request);
    }

    // DELETE /customer/123
    public void delete(Integer id) {
        service.delete(id);
    }

    // POST /customer/123/address
    public AddressResponse post(Integer customerId, AddressRequest request, RequestHeaders headers) {
        // Path param: customerId
        // Body: AddressRequest
        // Headers: RequestHeaders
        return service.addAddress(customerId, request, headers.getTraceId());
    }
}
```
### Supporting Classes
```java
// Body Content
public class CustomerRequest implements BodyContent {
    private String name;
    private Double salary;
    // getters and setters
}

// Query Parameters
public class CustomerQuery implements QueryParamContent {
    private String city;
    // getters and setters
}

// Headers
public class RequestHeaders implements HeaderContent {
    private String traceId;
    // getters and setters
}

// Response DTO
public class CustomerResponse {
    private Integer id;
    private String name;
    // getters and setters
}
```
### Expected JSON
```json
// POST /customer
{
    "customerRequest": {
        "name": "John Doe",
        "salary": 75000.00
    }
}

// POST /customer/123/address
{
    "addressRequest": {
        "street": "Main St",
        "city": "Springfield",
        "zipcode": "12345"
    }
}
```
## Javadoc

For detailed API documentation, see the [Javadoc](https://docs/javadoc/index.html).

----------

## Contributing

1.  Fork the repository
2.  Create a feature branch
3.  Submit a pull request
    
----------

## License

MIT License — See LICENSE file for details.