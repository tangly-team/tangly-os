# 1. Replace Guard and Action classes with JDK BiPredicate and BiFunction classes

Date: 2017-06-01

## Status

Implemented

## Context

We need an easy to understand data model for finite state machine.

## Decision

We use standard classes of the JDK to minimize contextual complexity when learning and using the FSM library. We will
evaluate if the loss of domain names for classes could have a negative impact of the legibility of the library.
## Consequences

We have a cleaner and simpler interface and have increase dependency to regular JDK classes.

