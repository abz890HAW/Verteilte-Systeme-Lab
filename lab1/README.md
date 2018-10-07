## How to run
### Server
```bash
( cd out/production/lab1 && rmiregistry ) &
sleep 1; java -classpath ./out/production/lab1 -Djava.rmi.server.codebase=file:./out/production/lab1 CDateServer
```
### Client
```bash
java -classpath ./out/production/lab1 CDateClient
```