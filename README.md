# spring-manageOnetoMany
Manage One to Many associations in a single request (POST/PUT) for Spring based REST API.

Spring does not provide an out-of-the-box solution to manage composite one-to-many relationships; think for example, Invoice-to-LineItems where both the parent and children must be created, modified, and deleted together – most discussion on the internet rely on API client to do the work that actually is backend responsibility which is problematic.

This project uses annotations and Java’s generics and reflection to address these problems. With the class ManageOneToMany in place in the project all that needs to be done is call the methods addChildren and syncChildren in the EventHandlers of the Entity class. See the source code for example.

A client consuming the API then has to send the appropriate JSON with the Parent and the Children via either a POST or PUT SINGLE request, depending on context (i.e. create or modify). The PUT request can edit existing children, delete children (minimum of one however is required), or add more children – all via single request – and the representation of the Parent and Child objects will be modified behind the covers (so very Spring-like).
