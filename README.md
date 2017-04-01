# 概要
spring-boot + kafka 实现收发消息（仅供入门参考）。
# 环境
- JDK 1.7+
- spring-boot 1.5.3+
- kafka 1.0+
- zookeeper

## Zookeeper配置

> Zookeeper的安装和配置十分简单, 既可以配置成单机模式, 也可以配置成集群模式.，该资源来源于网络，均整理验证。

### 单机模式
点击这里下载zookeeper的安装包之后, 解压到合适目录. 进入zookeeper目录下的conf子目录, 创建zoo.cfg:
```
tickTime=2000    
dataDir=/Users/apple/zookeeper/data    
dataLogDir=/Users/apple/zookeeper/logs    
clientPort=4180 
```
参数说明:
- tickTime: zookeeper中使用的基本时间单位, 毫秒值.
- dataDir: 数据目录. 可以是任意目录.
- dataLogDir: log目录, 同样可以是任意目录. 如果没有设置该参数, 将使用和dataDir相同的设置.
- clientPort: 监听client连接的端口号.

环境变量
```
#zookeeper Environment Variables
export ZOOKEEPER_HOME=/usr/zookeeper/zookeeper-3.4.9
export PATH=.:$ZOOKEEPER_HOME/bin:$PATH
```

 至此, zookeeper的单机模式已经配置好了. 启动server只需运行脚本:
```
bin/zkServer.sh start 
```
 Server启动之后, 就可以启动client连接server了, 执行脚本:
```
bin/zkCli.sh -server localhost:4180  
```
### 伪集群模式
所谓伪集群, 是指在单台机器中启动多个zookeeper进程, 并组成一个集群. 以启动3个zookeeper进程为例.

将zookeeper的目录拷贝2份:
|--zookeeper0  
|--zookeeper1  
|--zookeeper2  
更改zookeeper0/conf/zoo.cfg文件为:
```
tickTime=2000    
initLimit=5    
syncLimit=2    
dataDir=/Users/apple/zookeeper0/data    
dataLogDir=/Users/apple/zookeeper0/logs    
clientPort=4180  
server.0=127.0.0.1:8880:7770    
server.1=127.0.0.1:8881:7771    
server.2=127.0.0.1:8882:7772  
```
新增了几个参数, 其含义如下:

initLimit: zookeeper集群中的包含多台server, 其中一台为leader, 集群中其余的server为follower. initLimit参数配置初始化连接时, follower和leader之间的最长心跳时间. 此时该参数设置为5, 说明时间限制为5倍tickTime, 即5*2000=10000ms=10s.
syncLimit: 该参数配置leader和follower之间发送消息, 请求和应答的最大时间长度. 此时该参数设置为2, 说明时间限制为2倍tickTime, 即4000ms.
server.X=A:B:C 其中X是一个数字, 表示这是第几号server. A是该server所在的IP地址. B配置该server和集群中的leader交换消息所使用的端口. C配置选举leader时所使用的端口. 由于配置的是伪集群模式, 所以各个server的B, C参数必须不同.
参照zookeeper0/conf/zoo.cfg, 配置zookeeper1/conf/zoo.cfg, 和zookeeper2/conf/zoo.cfg文件. 只需更改dataDir, dataLogDir, clientPort参数即可.

在之前设置的dataDir中新建myid文件, 写入一个数字, 该数字表示这是第几号server. 该数字必须和zoo.cfg文件中的server.X中的X一一对应.
/Users/apple/zookeeper0/data/myid文件中写入0, /Users/apple/zookeeper1/data/myid文件中写入1, /Users/apple/zookeeper2/data/myid文件中写入2.

分别进入/Users/apple/zookeeper0/bin, /Users/apple/zookeeper1/bin, /Users/apple/zookeeper2/bin三个目录, 启动server.
任意选择一个server目录, 启动客户端:
```
bin/zkCli.sh -server localhost:4180  
```

### 集群模式
集群模式的配置和伪集群基本一致.
由于集群模式下, 各server部署在不同的机器上, 因此各server的conf/zoo.cfg文件可以完全一样.
下面是一个示例:
```
tickTime=2000    
initLimit=5    
syncLimit=2    
dataDir=/home/zookeeper/data    
dataLogDir=/home/zookeeper/logs    
clientPort=4180  
server.0=10.211.55.4:2888:3888  
server.1=10.211.55.5:2888:3888    
server.2=10.211.55.6:2888:3888
```
示例中部署了3台zookeeper server, 分别部署在10.211.55.4, 10.211.55.5, 10.211.55.6上. 需要注意的是, 各server的dataDir目录下的myid文件中的数字必须不同.
10.211.55.4 server的myid为0, 10.211.55.5 server的myid为1, 10.211.55.6 server的myid为2.

## kafka集群
### 环境资源

|   主机|  IP |  说明 |
| ------------ | ------------ | ------------ |
|  master |  10.211.55.4 |  同时部署单机zookeeper |
|  server_1 |  10.211.55.5 | slave  |
|  server_2 | 10.211.55.6  |  slave |
### 下载storm
下载到本地后，再上传到目标服务器。
```
scp kafka_2.11-0.10.2.0.tgz wangmingbo@10.211.55.4:/home/wangmingbo
```
### 安装
```
mkdir /usr/kafka/
cd /usr/kafka/
mv /home/wangmingbo/kafka_2.11-0.10.2.0.tgz /usr/kafka/
tar -zxvf kafka_2.11-0.10.2.0.tgz  
```
### 配置环境变量
```
vi /etc/profile
```
```
#kafka Environment Variables
export KAFKA_HOME=/usr/kafka/kafka_2.11-0.10.2.0
export PATH=.:$KAFKA_HOME/bin:$PATH
```
环境变量即时生效
```
source /etc/profile
```
### 将kafka目录授权给hadoop用户
```
 chown -R hadoop:hadoop /usr/kafka/
```
#### 配置server.properties
```
vi server.properties
```
```
broker.id=0  #当前机器在集群中的唯一标识(每台服务器的broker.id都不能相同,可以默认设置为ip后一位地址)，和zookeeper的myid性质一样
port=19092 #当前kafka对外提供服务的端口默认是9092
num.network.threads=3 #这个是borker进行网络处理的线程数
num.io.threads=8 #这个是borker进行I/O处理的线程数
log.dirs=/usr/kafka/kafka-logs #消息存放的目录，这个目录可以配置为“，”逗号分割的表达式，上面的num.io.threads要大于这个目录的个数这个目录，如果配置多个目录，新创建的topic他把消息持久化的地方是，当前以逗号分割的目录中，那个分区数最少就放那一个
socket.send.buffer.bytes=102400 #发送缓冲区buffer大小，数据不是一下子就发送的，先回存储到缓冲区了到达一定的大小后在发送，能提高性能
socket.receive.buffer.bytes=102400 #kafka接收缓冲区大小，当数据到达一定大小后在序列化到磁盘
socket.request.max.bytes=104857600 #这个参数是向kafka请求消息或者向kafka发送消息的请请求的最大数，这个值不能超过java的堆栈大小
num.partitions=1 #默认的分区数，一个topic默认1个分区数
log.retention.hours=168 #默认消息的最大持久化时间，168小时，7天
message.max.byte=5242880  #消息保存的最大值5M
default.replication.factor=2  #kafka保存消息的副本数，如果一个副本失效了，另一个还可以继续提供服务
replica.fetch.max.bytes=5242880  #取消息的最大直接数
log.segment.bytes=1073741824 #这个参数是：因为kafka的消息是以追加的形式落地到文件，当超过这个值的时候，kafka会新起一个文件
log.retention.check.interval.ms=300000 #每隔300000毫秒去检查上面配置的log失效时间（log.retention.hours=168 ），到目录查看是否有过期的消息如果有，删除
log.cleaner.enable=false #是否启用log压缩，一般不用启用，启用的话可以提高性能
zookeeper.connect=master:2181/kafka #设置zookeeper的连接端口。默认Kafka会使用ZooKeeper默认的/路径，这样有关Kafka的ZooKeeper配置就会散落在根路径下面，如果 你有其他的应用也在使用ZooKeeper集群，查看ZooKeeper中数据可能会不直观，所以强烈建议指定一个chroot路径，直接在 zookeeper.connect配置项中指定。eg:zookeeper.connect=master:1218,server_1:2181/kafka
```
> 在zk中创建，kafka。

```
zkCli.sh -server master:2181
create /kafka
```
这样，每次连接Kafka集群的时候（使用--zookeeper选项），也必须使用带chroot路径的连接字符串，后面会看到。

### 启动集群服务
分别启动各个节点服务
```
kafka-server-start.sh -daemon ${KAFKA_HOME}/config/server.properties
```
## 验证
### 查看topic
显示所有的topic
```
kafka-topics.sh --list --zookeeper master:2181/kafka
```
### 创建topic
创建一个名称为test的Topic，2个分区，并且复制因子为1
```
kafka-topics.sh -zookeeper master:2181/kafka -topic test -replication-factor 1 -partitions 2 -create
```
### 删除topic
```
kafka-topics.sh  --zookeeper master:2181/kafka --delete --topic test2
```
### 创建producer
```
kafka-console-producer.sh -broker-list master:9092,server_1:9092 -topic test
```
### 创建consumer
```
kafka-console-consumer.sh -zookeeper master:2181/kafka -from-begining -topic test
```
### 查看topic状态
```
kafka-topics.sh --describe --zookeeper master:2181/kafka --topic test
Topic:ks
PartitionCount:2	ReplicationFactor:2	Configs:
Topic: ks	Partition: 0	Leader: 5	Replicas: 5,4	Isr: 5,4
Topic: ks	Partition: 1	Leader: 4	Replicas: 4,5	Isr: 4,5
```

-  Partition： 分区
- Leader   ： 负责读写指定分区的节点
- Replicas ： 复制该分区log的节点列表
- Isr      ： "in-sync" replicas，当前活跃的副本列表（是一个子集），并且可能成为Leader

###  到ZK中查看kafka集群

```
zkCli.sh -server master:2181
```

```
[zk: master:2181(CONNECTED) 0] ls /
[isr_change_notification, zookeeper, admin, consumers, cluster, config, controller, storm, brokers, controller_epoch]
[zk: master:2181(CONNECTED) 1] ls /brokers
[seqid, topics, ids]
[zk: master:2181(CONNECTED) 2] ls /brokers/ 
seqid    topics   ids
[zk: master:2181(CONNECTED) 2] ls /brokers/ids
[5, 4]
[zk: master:2181(CONNECTED) 3] ls /brokers/ids/4
[]
[zk: master:2181(CONNECTED) 4] ls /brokers/topics/
ks     test
[zk: master:2181(CONNECTED) 4] ls /brokers/topics/

ks     test
[zk: master:2181(CONNECTED) 4] ls /brokers/topics/

ks     test
[zk: master:2181(CONNECTED) 4] ls /brokers/topics/test/partitions
[1, 0]
[zk: master:2181(CONNECTED) 5]
```

# 编译
	下载源码并编译，参见maven官方文档
# 测试
1. 启动monitor (根据测试需要动态设置kafka-demo日志级别)；
访问地址：
```
http://{ip}:{port(默认8080)}
```
1. 启动kafka-demo.java -jar (编译后的kafka-demo.jar包名).jar --kafka.bootstrap.servers={kafka集群地址:格式{ip}:{port},多个以逗号分隔} --kafka.consumer.topic={topic name}
eg:
```
java -jar kafka-demo-0.0.1-SNAPSHOT.jar --kafka.bootstrap.servers=10.211.55.4:9092,10.211.55.5:9092 --kafka.consumer.topic=test
```
> 默认端口为8081,需要更改端口号，启动时，增加--server.port={自定义端口}

1. 测试验证
 开启debug模式（通过monitor开启即可），可查看收发消息内容信息。访问地址：http://{ip}:{port}/producer/test/{产生的消息数量}/{kafka topic 名}
 eg:
 ```
 curl http://localhost:8081/producer/test/60/test2
 ```

