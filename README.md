# predix-jpa-cf
Predix Labs Java example on how to run the Database and Redis cache services.
The example contains Rest controller to call database and Redis services.  The services configuration uses three libraries:
- spring-boot-starter-data-jpa
- spring-boot-starter-redis
- spring-cloud-cloudfoundry-connector

######Please see details in the Developer notes.

##Project structure

   ``` 
├── LICENSE.md
├── README.md
├── manifest.yml
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── ge
    │   │           └── predix
    │   │               └── labs
    │   │                   └── data
    │   │                       └── jpa
    │   │                           ├── Application.java
    │   │                           ├── config
    │   │                           │   ├── CloudFoundryDataSourceConfiguration.java
    │   │                           │   └── ServicesConfiguration.java
    │   │                           ├── domain
    │   │                           │   └── Customer.java
    │   │                           ├── service
    │   │                           │   └── CustomerService.java
    │   │                           └── web
    │   │                               ├── CacheController.java
    │   │                               └── CustomerApiController.java
    │   └── resources
    │       └── initialCustomers.sql
    └── test
   ``` 
#### CloudFoundryDataSourceConfiguration.java:
 -  extends AbstractCloudConfig class to get the ConnectionFactory object associated with the bound services Postgress and Redis
 
  ```
@Configuration
public class CloudFoundryDataSourceConfiguration extends AbstractCloudConfig  {

    @Bean
    public DataSource dataSource() {
    		return connectionFactory().dataSource();
    }
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return connectionFactory().redisConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() throws Exception {
        RedisTemplate<String, Object> ro = new RedisTemplate<String, Object>();
        ro.setConnectionFactory(redisConnectionFactory());
        return ro;
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean( DataSource dataSource  ) throws Exception {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource( dataSource );
        em.setPackagesToScan(Customer.class.getPackage().getName());
        em.setPersistenceProvider(new HibernatePersistence());
        Map<String, String> p = new HashMap<String, String>();
        p.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "create");
        p.put(org.hibernate.cfg.Environment.HBM2DDL_IMPORT_FILES, "initialCustomers.sql");
        p.put(org.hibernate.cfg.Environment.DIALECT, PostgreSQLDialect.class.getName());
        p.put(org.hibernate.cfg.Environment.SHOW_SQL, "true");
        em.setJpaPropertyMap(p);
        return em;
    }

    @Bean
    public CacheManager cacheManager() throws Exception {
        return new RedisCacheManager(redisTemplate());
    }

}
	
   ``` 
   
#### ServiceConfiguration.java
 - Enable Transaction Manager 

   ```  
@Configuration
@EnableCaching
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {CustomerService.class})
public class ServicesConfiguration {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) throws Exception {
        return new JpaTransactionManager(entityManagerFactory);
    }
} 

   ```  
## Installation
 - clone repository  
    `>git clone https://github.com/PredixDev/predix-jpa-cf.git`
 - check that you have on your market space postgres and redis services 
 
    `>cf m`
   
   ``` 
   Getting services from the marketplace in org sergey.vyatkin@ge.com / space dev as sergey.vyatkin@ge.com...
   OK

   service                    plans       description   
   business-operations        beta        Upgrade your service using a subscription-based business model.   
   logstash-3                 free        Logstash 1.4 service for application development and testing   
   p-rabbitmq                 standard    RabbitMQ is a robust and scalable high-performance multi-protocol messaging broker.  
   postgres                   shared      Reliable PostgrSQL Service   
   redis-1                    shared-vm   Redis service to provide a key-value store   
   riakcs                     developer   An S3-compatible object store based on Riak CS 
   ...
   ```
 - create new services for postgres and redis like 
    ``` 
     >cf cs postgres shared postgres_sv 
     >cf cs  redis-1  shared-vm   redis_sv 
    ``` 
###### application dynamically detects bond services.  You do not need to change a code. 

 - deploy application 
 
  ```
    >cd predix-jpa-cf
    
    >mvn clean package
    
    >cf push 
    
  ```
## Use a browser to test app's REST: 

 [http://predix-jpa-sv.run.aws-usw02-pr.ice.predix.io/customers] (http://predix-jpa-sv.run.aws-usw02-pr.ice.predix.io/customers)
   ```  
 returns json list of customer from resources/initialCustomers.sql
 [{"id":785001,"name":"Sam","phone":"(925)-123-4567","tstamp":1447719635998},
 {"id":785002,"name":"Sergey","phone":"(925)-223-4567","tstamp":1447719636007},
 {"id":785003,"name":"Robert","phone":"(925)-423-4567","tstamp":1447719636015},
 {"id":785004,"name":"Alex","phone":"(925)-523-4567","tstamp":1447719636018},
 {"id":785005,"name":"Savva","phone":"(925)-623-4567","tstamp":1447719636020},
 {"id":785006,"name":"Josh","phone":"(925)-723-4567","tstamp":1447719636022},
 {"id":785007,"name":"Patrick","phone":"(925)-823-4567","tstamp":1447719636025},
 {"id":785008,"name":"Andy","phone":"(925)-923-4567","tstamp":1447719636027},
 {"id":785009,"name":"Eric","phone":"(925)-013-4567","tstamp":1447719636030},
 {"id":785010,"name":"Chris","phone":"(925)-023-4567","tstamp":1447719636032},
 {"id":785011,"name":"Raj","phone":"(925)-033-4567","tstamp":1447719636034},
 {"id":785012,"name":"Vic","phone":"(925)-043-4567","tstamp":1447719636037},
 {"id":785013,"name":"Rich","phone":"(925)-053-4567","tstamp":1447719636040},
 {"id":785014,"name":"Mark","phone":"(925)-063-4567","tstamp":1447719636042}] 
 
 http://predix-jpa-sv.run.aws-usw02-pr.ice.predix.io/customers/{id}
 returns customer by id number 
 {"id":785001,"name":"Sam","phone":"(925)-123-4567","tstamp":1447719635998}
    ```
 [http://predix-jpa-sv.run.aws-usw02-pr.ice.predix.io/search?q=J] (http://predix-jpa-sv.run.aws-usw02-pr.ice.predix.io/search?q=J)
    ```
 returns all customers containing letter "J" 
 [{"id":785006,"name":"Josh","phone":"(925)-723-4567","tstamp":1447719636022},
 {"id":785011,"name":"Raj","phone":"(925)-033-4567","tstamp":1447719636034}]
    ```
 [http://predix-jpa-sv.run.aws-usw02-pr.ice.predix.io/cache] (http://predix-jpa-sv.run.aws-usw02-pr.ice.predix.io/cache)
    ```
 returns 
 customers - Cache contains records: 

 785001:Name:Sam:Phone:(925)-123-4567
 
   ```  

#### Developer notes:

 - To load in eclipse you may use [SpringSource Tool Suite - STS](https://spring.io/tools/sts/all)  
  ```
  >mvn eclipse:clean eclipse:eclipse  
  
  open eclipse and use the following commands:
  File/Import/General/Existing Projects/Browse to predix-jpa-cf dir   
  ```
 - Maven library dependency for Database and Redis services:
    ```
		<dependency>
		<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-redis</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-cloudfoundry-connector</artifactId>
			<version>${spring-cloud.version}</version>
		</dependency>
	
    ```
