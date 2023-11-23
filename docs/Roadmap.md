# BaSyx Roadmap
Here, a list of features, components, and general updates that are planned in the future is described. Please note that this list is not exhaustive and reprioritization might happen.

## BaSyx AAS Core
````mermaid
timeline
  continuous : AAS API Spec Implementation
  Q4/2023 : RBAC Security
  Q1/2024 : AASX Upload at runtime : AASX File Server with MongoDB & InMemory Persistency : AAS Client SDK : Dynamic Integration of Features/Backends in OTS components
  Q2/2024 : Code Generation based on AASX files (Client, ...)
````

## BaSyx AAS Integration Components

### DataBridge (see [basyx-databridge](https://github.com/eclipse-basyx/basyx-databridge))
````mermaid
timeline
  Q1/2024 : Bi-directional Data Exchange, e.g., with OPC UA : Support for Asset Interface Description Submodel and Asset Interface Mapping Description Submodel
  Q2/2024 : GUI-based Configuration : Integration of more complex data transformation, e.g., data aggregation of historic data
````

### System of Record Integration (see [basyx-applications](https://github.com/eclipse-basyx/basyx-applications/tree/main/dataintegrator))
````mermaid
%%{init: { 'theme': 'neutral' } }%%
timeline
  Q4/2023 : Release of first prototype
````
