# API Scope
API scope is a microservice responsible for storing and retrieving user-readable scopes (permissions) for the API Platform.

### Unit tests
```
sbt test
```

### Integration tests
```
sbt it:test
```

### Publish a scope
API Publisher use this endpoint to notify of a list of new or updated scopes
request: 
```
POST /scope
```
Payload:
```
[
   {
       "key": "read:employment",
       "name":"Read Employment Data",
       "description":"Ability to read employment data"
   }
]
```
response:
```
200 OK
```

### Fetch a scope
request:
```
GET /scope/{key}
```

response:
```
{
   "key": "read:employment",
   "name":"Read Employment Data",
   "description":"Ability to read employment data"
}
```

### Fetch all scopes
request:
```
GET /scope
```

response:
```
[
{
   "key": "read:employment",
   "name":"Read Employment Data",
   "description":"Ability to read employment data"
}
]
```

### Fetch multiple scopes
request:
```
GET /scope?keys=read:employment read:paye
```

response:
```
[
{
   "key": "read:employment",
   "name":"Read Employment Data",
   "description":"Ability to read employment data"
},
{
   "key": "read:paye",
   "name":"Read Paye Data",
   "description":"Ability to read paye data"
}
]
```

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
