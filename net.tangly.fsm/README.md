##Purpose
The *tangly fsm* is a finite state machine library written in Java 8. You can use it in productive projects or academic assignments.

The library provides

* Definition of hierarchical state machine descriptions. The machine states and transitions are generic classes. You provide an enumeration for the
  set of states, and an enumeration for the set of events triggering the machine. The builder pattern is used to create complex state machine 
  definition declaratively.
* Actions are implemented as consumer lambda expressions, guard are predicate lambda expressions. The standard library of Java 8 is extensively used.
* A runtime engine processing events on a finite state machine description. Multiple instances of the same description can be instantiated. The class 
  owning the state machine is passed as context to all guards and actions.
* Support classes to implement listeners and logging are provided. A documentation helper can generate a graphical representation of a state 
  machine using the graph dot language.
  
You will find extensive documentation under 
    ./src/site/user-guide-fsm.adoc
    ./src/site/user-guide-actor.adoc

##Download
You can download the source file from the git repository and build the library with the provided gradle configuration file.

##License
The source code is under Apache license 2.0.
The documentation and examples are under creative common (CC Attribution 4.0 International).

##Tips and Tricks
