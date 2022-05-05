

# flink13cdc

flink版本使用的flink1.13.0

# mongodb

1. 部署

   https://blog.csdn.net/DH719491759/article/details/116781313

2. 开启oplog, 需要修改下存储引擎，配置副本集，搭建集群, 配置如下

   mongodb的服务端口
   
   port=27017
   
   数据存放目录
   
   dbpath=/data/mogodb/mongodb-4.4.13/server/27017/data/db
   
   指定日志文件
   
   logpath=/data/mogodb/mongodb-4.4.13/server/27017/log/mongodb.log
   
   写日志的模式，设置为true为追加。默认是覆盖。如果未指定此设置，启动时MongoDB的将覆盖现有的日志文件。
   
   logappend=true
   
   绑定地址，默认127.0.0.1，只能通过本地连接
   
   bind_ip=0.0.0.0
   
   是否后台运行，true为后台运行，默认false
   
   fork=true
   
   是否开启操作日志
   
   journal=true
   
   副本集配置，所有主机必须有相同的名称作为同一个副本集
   
   replSet=shard1
   关闭认证方式
   auth=false
   设置存储引擎
   storageEngine=wiredTiger

3. 配置依赖，编写程序

   官方案例：https://ververica.github.io/flink-cdc-connectors/master/content/connectors/mongodb-cdc.html

```
<dependency>
    <groupId>com.ververica</groupId>
    <artifactId>flink-connector-mongodb-cdc</artifactId>
    <version>2.2.0</version>
</dependency>
```

```
// Stream API 写法
SourceFunction<String> sourceFunction = MongoDBSource.<String>builder()
        .hosts("172.16.43.182:27018")
        .username("test")
        .password("123456")
        .databaseList("test") //不要使用admin系统库，会无法watch
        .collectionList("test.book")
        .deserializer(new JsonDebeziumDeserializationSchema())
        //.deserializer(new MongodbDeserialization())
        .build();
```

4. mongodb基本用法

https://www.w3cschool.cn/mongodb/

5. 可用性

在flink-cdc 2.10的时候新增oracle、mongodb支持（mongodb不支持对指定集合进行监控，支持单库的监控）

在flink-cdc 2.2.0版本新增了对mongodb单个集合的支持,也可以对多个库、多个集合进行执行

2.1.0 和 2.2.0 都要一个共同特点，只能监控到mongodb的初始化的数据，即历史数据，对于该数据库或者该集合新增、删除、修改的操作监控不到

flink-cdc 2.1.0 与 flink-cdc 2.2.0 均兼容 flink1.13.* 与 flink1.14.*

- MongoDB版本

  MongoDB 版本 >= 3.6
  我们使用[更改流](https://docs.mongodb.com/manual/changeStreams/)功能（3.6 版中的新功能）来捕获更改数据。

- 集群部署

  [需要副本集](https://docs.mongodb.com/manual/replication/)或[分片集群](https://docs.mongodb.com/manual/sharding/)。

- 存储引擎

  [需要WiredTiger](https://docs.mongodb.com/manual/core/wiredtiger/#std-label-storage-wiredtiger)存储引擎。

- [副本集协议版本](https://docs.mongodb.com/manual/reference/replica-configuration/#mongodb-rsconf-rsconf.protocolVersion)

  需要副本集协议版本 1 [(pv1)](https://docs.mongodb.com/manual/reference/replica-configuration/#mongodb-rsconf-rsconf.protocolVersion)。
  从版本 4.0 开始，MongoDB 仅支持 pv1。pv1 是使用 MongoDB 3.2 或更高版本创建的所有新副本集的默认值。

- 特权

  `changeStream`MongoDB Kafka 连接器`read`需要权限

  您可以使用以下示例进行简单授权。
  更详细的授权请参考[MongoDB 数据库用户角色](https://docs.mongodb.com/manual/reference/built-in-roles/#database-user-roles)。

  ```
  use admin;
  db.createUser({
    user: "flinkuser",
    pwd: "flinkpw",
    roles: [
      { role: "read", db: "admin" }, //read role includes changeStream privilege 
      { role: "readAnyDatabase", db: "admin" } //for snapshot reading
    ]
  });
  ```



# mysql

1.部署

http://www.360doc.com/content/21/1209/15/78047646_1007851637.shtml

2.开启binlog

启用binlog，通过配置 /etc/my.cnf 或 /etc/mysql/mysql.conf.d/mysqld.cnf 配置文件的 log-bin 选择

log-bin=mysql-bin

binlog_format=ROW

配好之后需要重启mysql

```
# CentOS 6
service mysqld restart
 
# CentOS 7
systemctl restart mysqld
```

登录mysql，验证是否确实开了binlog，value=ON则表示开启了binlog

mysql> show variables like '%log_bin%';

log_bin = ON 则表示开启了binlog

3.配置依赖，书写程序

官方案例：https://ververica.github.io/flink-cdc-connectors/master/content/connectors/mysql-cdc.html

其他案例：https://www.jianshu.com/p/cc489384b593?ivk_sa=1024320u

```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.16</version>
</dependency>
<dependency>
     <groupId>com.ververica</groupId>
     <artifactId>flink-connector-mysql-cdc</artifactId>
     <version>2.2.0</version>
</dependency>
```

```
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint
        env.enableCheckpointing(3000);

        Properties props = new Properties();
        props.setProperty("scan.startup.mode","initial");
MySqlSource<JSONObject> mySqlSource = MySqlSource.<JSONObject>builder()
        .hostname("172.16.43.164")
        .port(3306)
        //.serverTimeZone("convertToNull")
        .databaseList("164testdatax")
        .tableList("164testdatax.user")
        .username("root")
        .password("123")
        .debeziumProperties(props)
        .deserializer(new MyCdcDeserilization2())
        .build();

env
        .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
        .setParallelism(1)
        .print().setParallelism(1);
```

4. mysql的基本用法

https://www.runoob.com/mysql/mysql-administration.html

5. 可用性

支持一下类MySQL数据库

| Connector                                                    | Database                                                     | Driver              |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------- |
| [mysql-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/mysql-cdc.html#) | [MySQL](https://dev.mysql.com/doc): 5.6, 5.7, 8.0.x[RDS MySQL](https://www.aliyun.com/product/rds/mysql): 5.6, 5.7, 8.0.x[PolarDB MySQL](https://www.aliyun.com/product/polardb): 5.6, 5.7, 8.0.x[Aurora MySQL](https://aws.amazon.com/cn/rds/aurora): 5.6, 5.7, 8.0.x[MariaDB](https://mariadb.org/): 10.x[PolarDB X](https://github.com/ApsaraDB/galaxysql): 2.0.1 | JDBC Driver: 8.0.21 |

flink cdc2.2.0之前不支持mysql5.6版本，flink cdc2.2.0开始支持了mysql5.6 、5.7、8.0.x版本



# oracle

1.部署

https://www.modb.pro/doc/4763

2.开启日志归档、启用补充日志记录

https://ververica.github.io/flink-cdc-connectors/master/content/connectors/oracle-cdc.html

归档日志会占用大量磁盘空间，建议定期清理过期日志

必须为捕获的表或数据库启用补充日志记录，以便数据更改捕获已更改数据库行的*之前*状态。下面说明了如何在表/数据库级别进行配置。

```
-- Enable supplemental logging for a specific table:
ALTER TABLE inventory.customers ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;
-- Enable supplemental logging for database
ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;
```

3. 配置依赖，书写程序

官方案例：https://ververica.github.io/flink-cdc-connectors/master/content/connectors/oracle-cdc.html

其他案例：https://blog.csdn.net/qq_33824503/article/details/123994244

```
<dependency>
    <groupId>com.ververica</groupId>
    <artifactId>flink-connector-oracle-cdc</artifactId>
    <version>2.2.0</version>
</dependency>
```

```
SourceFunction<JSONObject> sourceFunction = OracleSource.<JSONObject>builder()
        .hostname("172.16.43.182")
        .port(1521)
        .database("helowin")
        .schemaList("SCOTT")
        .tableList("SCOTT.DEPT")
        .username("system")
        .password("system")
        //.deserializer(new JsonDebeziumDeserializationSchema())
        .deserializer(new MyCdcDeserilization2())
        .build();
```

4. oracle的基本用法

https://www.w3cschool.cn/oraclejc/

5. 可用性

flink cdc 2.1.0 开始新增对oracle的支持，目前 Flink CDC 项目里测试了 Oracle 11，12 和 19 三个版本。

# postgresql

1.部署

https://blog.csdn.net/weixin_43230682/article/details/108403642

2.开启binlog

https://www.likecs.com/show-133207.html

2.1 更改配置文件postgresql.conf

\# 更改wal日志方式为logical
wal_level = logical # minimal, replica, or logical

\# 更改solts最大数量（默认值为10），flink-cdc默认一张表占用一个slots
max_replication_slots = 20 # max number of replication slots

\# 更改wal发送最大进程数（默认值为10），这个值和上面的solts设置一样
max_wal_senders = 20 # max number of walsender processes
\# 中断那些停止活动超过指定毫秒数的复制连接，可以适当设置大一点（默认60s）
wal_sender_timeout = 180s # in milliseconds; 0 disable　　

更改配置文件postgresql.conf完成，需要重启pg服务生效，所以一般是在业务低峰期更改

 

2.2 新建用户并且给用户复制流权限

-- pg新建用户
CREATE USER user WITH PASSWORD 'pwd';

-- 给用户复制流权限
ALTER ROLE user replication;

-- 给用户登录数据库权限
grant CONNECT ON DATABASE test to user;

-- 把当前库public下所有表查询权限赋给用户
GRANT SELECT ON ALL TABLES IN SCHEMA public TO user;

2.3 发布表

```
-- 设置发布为true
update pg_publication set puballtables=true where pubname is not null;
-- 把所有表进行发布
CREATE PUBLICATION dbz_publication FOR ALL TABLES;
-- 查询哪些表已经发布
select * from pg_publication_tables;
```

2.4 更改表的复制标识包含更新和删除的值

```
-- 更改复制标识包含更新和删除之前值
ALTER TABLE test0425 REPLICA IDENTITY FULL;-- 查看复制标识（为f标识说明设置成功）select relreplident from pg_class where relname='test0425';
```

3. 配置依赖，书写程序

官方案例：https://ververica.github.io/flink-cdc-connectors/master/content/connectors/postgres-cdc.html

```
<dependency>
    <groupId>com.ververica</groupId>
    <artifactId>flink-connector-postgres-cdc</artifactId>
    <version>2.2.0</version>
</dependency>
```

```
SourceFunction<JSONObject> sourceFunction = PostgreSQLSource.<JSONObject>builder()
        .hostname("172.16.43.182")
        .port(5432)
        .database("pgsql")
        .schemaList("public")
        .tableList("public.cdc_test")
        .username("pgsql")
        .password("123456")
        .debeziumProperties(props)
        .decodingPluginName("pgoutput") //默认值decoderbufs，不设置会报文件找不到的错误
        //.deserializer(new JsonDebeziumDeserializationSchema())
        .deserializer(new MyCdcDeserilization2())
        .build();
```

4. postgresql的基本用法

https://www.runoob.com/postgresql/postgresql-tutorial.html

5. 可用性

支持9.6、10、11、12版本

# sqlserver

1. 部署

https://cloud.tencent.com/developer/article/1911796

2. 开启数据库和表的cdc

https://www.cnblogs.com/Nahshon/p/14914861.html

3. 配置依赖，书写程序

官方案例：https://ververica.github.io/flink-cdc-connectors/master/content/connectors/sqlserver-cdc.html

```
<dependency>
    <groupId>com.ververica</groupId>
    <artifactId>flink-connector-sqlserver-cdc</artifactId>
    <version>2.2.0</version>
</dependency>
```

```
SourceFunction<JSONObject> sourceFunction = SqlServerSource.<JSONObject>builder()
        .hostname("172.16.43.182")
        .port(1433)
        .database("flinkcdc")
        .tableList("dbo.student")
        .username("SA")
        .password("!QAZxsw2")
        .debeziumProperties(props)
        //.deserializer(new JsonDebeziumDeserializationSchema())
        .deserializer(new MyCdcDeserilization2())
        .build();
```

4. sqlserver的基本用法

https://www.w3cschool.cn/sqlserver/

5. 可用性

在flink cdc 2.2.0 开始支持，支持sqlserver 2012、2014、2016、2017、2019版本



# flink cdc 2.1.0 更新和改进

新增：

 Oracle CDC 连接器和 MongoDB CDC 连接器

改进：

- MySQL CDC 支持百亿级数据的超大表，支持 MySQL 全部数据类型，通过连接池复用等优化大幅提升稳定性。同时提供支持无锁算法，并发读取的 DataStream API，用户可以借此搭建整库同步链路；
- 新增了 Oracle CDC 连接器， 支持从 Oracle 数据库获取全量历史数据和增量变更数据；
- 新增了 MongoDB CDC 连接器，支持从 MongoDB 数据库获取全量历史数据和增量变更数据；
- 所有连接器均支持了 metadata column 功能， 用户通过 SQL 就可以访问库名，表名，数据变更时间等 meta 信息，这对分库分表场景的数据集成非常实用；
- 丰富 Flink CDC 入门文档，增加多种场景的端到端实践教程。

在 2.2 版本之前，Flink CDC 连接器都只对应一个 Flink 大版本，比如很多用户反馈 Flink CDC 2.1 只能在 Flink 1.13 版本的集群上使用

缺点：

不支持mysql5.6 ,支持mysql5.7

只能和flink1.13.* 搭配使用

参考连接：https://baijiahao.baidu.com/s?id=1716919815586154346&wfr=spider&for=pc



# flink cdc 2.2.0 更新和改进

支持从 MySQL，MariaDB, RDS MySQL，Aurora MySQL，PolarDB MySQL，PostgreSQL，Oracle，MongoDB，SqlServer，OceanBase，PolarDB-X，TiDB 等数据库中实时地读取存量历史数据和增量变更数据，用户既可以选择用户友好的 SQL API，也可以使用功能更为强大的 DataStream API。



新增：

OceanBase，PolarDB-X，SqlServer，TiDB 四种数据源接入，均支持全量和增量一体化同步。至此，Flink CDC 已支持 12 种数据源。

其中新增 OceanBase CDC，SqlServer CDC，TiDB CDC 三个连接器，而 PolarDB-X 的支持则是通过对 MySQL CDC 连接器进行兼容适配实现。

改进：

Flink CDC 兼容 Flink 1.13 和 Flink 1.14 两个大版本，2.2 版本的所有 Connector 都支持跑在 Flink 1.13.* 或 Flink 1.14.* 的集群上。

提供增量快照读取框架，方便其他连接器接入，其他连接器采用该框架后，便可以提供无锁算法，并发读取，断点续传等功能。

MySQL CDC 支持动态加表，该功能可以在无需重新读取已有表的基础上，增加需要监控的表，添加的表会自动先同步该表的全量数据再无缝切换到同步增量数据。

MongoDB CDC 支持正则表达式过滤集合，该功能可以让用户在作业中指定所需监控的库名和集合名，用户可以用一个作业中监控多个数据库或多个集合。

MySQL CDC 支持 5.6 ，满足低版本的mysql用户需求

flink cdc 2.2.0目前支持的数据库及版本

| Connector                                                    | Database                                                     | Driver                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ----------------------- |
| [mongodb-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/mongodb-cdc.html) | [MongoDB](https://www.mongodb.com/): 3.6, 4.x, 5.0           | MongoDB Driver: 4.3.1   |
| [mysql-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/mysql-cdc.html) | [MySQL](https://dev.mysql.com/doc): 5.6, 5.7, 8.0.x[RDS MySQL](https://www.aliyun.com/product/rds/mysql): 5.6, 5.7, 8.0.x[PolarDB MySQL](https://www.aliyun.com/product/polardb): 5.6, 5.7, 8.0.x[Aurora MySQL](https://aws.amazon.com/cn/rds/aurora): 5.6, 5.7, 8.0.x[MariaDB](https://mariadb.org/): 10.x[PolarDB X](https://github.com/ApsaraDB/galaxysql): 2.0.1 | JDBC Driver: 8.0.27     |
| [oceanbase-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/oceanbase-cdc.html) | [OceanBase CE](https://open.oceanbase.com/): 3.1.x           | JDBC Driver: 5.7.4x     |
| [oracle-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/oracle-cdc.html) | [Oracle](https://www.oracle.com/index.html): 11, 12, 19      | Oracle Driver: 19.3.0.0 |
| [postgres-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/postgres-cdc.html) | [PostgreSQL](https://www.postgresql.org/): 9.6, 10, 11, 12   | JDBC Driver: 42.2.12    |
| [sqlserver-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/sqlserver-cdc.html) | [Sqlserver](https://www.microsoft.com/sql-server): 2012, 2014, 2016, 2017, 2019 | JDBC Driver: 7.2.2.jre8 |
| [tidb-cdc](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/tidb-cdc.html) | [TiDB](https://www.pingcap.com/): 5.1.x, 5.2.x, 5.3.x, 5.4.x, 6.0.0 | JDBC Driver: 8.0.27     |

flink cdc 和 flink 版本的对应关系

| Flink® CDC Version | Flink® Version |
| ------------------ | -------------- |
| 1.0.0              | 1.11.*         |
| 1.1.0              | 1.11.*         |
| 1.2.0              | 1.12.*         |
| 1.3.0              | 1.12.*         |
| 1.4.0              | 1.13.*         |
| 2.0.*              | 1.13.*         |
| 2.1.*              | 1.13.*         |
| 2.2.*              | 1.13.*, 1.14.* |

Table/SQL API 需要在flink 1.12+ ，并且 Java 8+

参考连接：https://blog.csdn.net/weixin_44904816/article/details/123836091

