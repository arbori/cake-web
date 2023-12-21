# Cake Web
The first part of the Cake framework deals with Web development, which aims to be as simple as possible, using only Java language resources, minimizing the use of configuration files, annotations, other frameworks, or even building a complex library to handle access requests to endpoints.

To construct a API using Cake-Web it is enough to create a correspondence between endpoints and java packages with its classes. For example, to respond a request made to endpoint /theapi/thepath/theresource, it is simple like define a package theapi.thepath and a class in this package with name TheResource. This class do not need implement any interfaces or extends any classes, but for each http verb needed for the API, the class has a method for handle it with the same name.
The API need handle requests for GET? Just implement a method get(...) in the class TheResource with the needed parameters! Simple like that!
The same is valid for the others verbs like PUT, POST, DELETE and so on.
