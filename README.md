# framework
Service-oriented java development framework, use many design pattern such as Composite, State, to improve users partition their system more rationally in the development process, as well as focus on the implementation of their business. When adding new features, users only need to add a new type of message and the new implementation, the existing business logic completely isolated.


the first api:

```
   queryServices:

   url: localhost:8080/api?action=queryServices
   response:
        {
            queryServices: {
                success: true,
                response: {
                    results: [
                    {
                        name: "-|ResourceManager",
                        state: "STARTED",
                        level: 1
                    },
                    {
                        name: "----|TaskManager",
                        state: "STARTED",
                        level: 2
                    },
                    {
                        name: "----|ApiManager",
                        state: "STARTED",
                        level: 2
                    }
                    ]
                }
            }
       }
```