docker stop client
docker rm client
docker rmi my/rpcclient:0.0.1
cd /Users/redamancy/Desktop/redamancy/ElegantCode/code/graduationProject/redamancy-rpc-application/application-client
docker build -t my/rpcclient:0.0.1 .
docker run --name client -p 8084:8080 -e SERVER_PORT=8080 -e RPC_PORT=53242 -e NODE="abc" --net nacos-net -d my/rpcclient:0.0.1

docker stop server1 server2
docker rm server1 server2
docker rmi my/rpcserve:0.0.1
cd /Users/redamancy/Desktop/redamancy/ElegantCode/code/graduationProject/redamancy-rpc-application/application-serve
docker build -t my/rpcserve:0.0.1 .
docker run --name server1 -e SERVER_PORT=8080 -e RPC_PORT=-1 -e NODE="iamnode1" --net nacos-net -d my/rpcserve:0.0.1
docker run --name server2 -e SERVER_PORT=8080 -e RPC_PORT=-1 -e NODE="iamnode2" --net nacos-net -d my/rpcserve:0.0.1
