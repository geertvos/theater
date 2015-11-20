Theater
=======
Theater is an actor system that allows you to built scalable applications that are distributed by nature and run massively parallel. 

How does it work?
-----------------
In an actor system we take message passing between objects very literal. Instead of invoking method on objects the communication between objects only happens by sending it a message. 

In theater you can define your objects as actors. Each actor can send and receive messages. Simply sending a message to an actor that does not exist will create one. Each actor is allowed to have state, just like real objects. Theater runs by default in a clustered configuratino with a gossip network for discovery of nodes. Actors are distributed acros the cluster for scalability. Add more nodes and you can add more actors. By storing the state in a shared repository we can easily migrate actors form VM to VM, from node to node. 

Example case
-----------------
Theater can be used for massive parallel computation. For example:

- We make one actor that reads a file with input data. Each line of input get send to one of the ten processing actors. 
- Each processing actor does some form of processing on the line of data. The result is send to the result actor.
- The result actor aggregates all the results and e-mails them to the initiator.

With this setup we can scale the computation from a single machine with 1 core to a cluster of 10 machines. Also, we can scale the cluster while the computation is running. 





