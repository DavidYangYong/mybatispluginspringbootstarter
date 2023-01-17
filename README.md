## What is MyBatis-Plugin?

MyBatis-Plugin is an powerful enhanced toolkit of MyBatis for simplify development. This toolkit provides some efficient, useful, out-of-the-box features for MyBatis, use it can effectively save your development time.

## Features

-   Fully compatible with MyBatis
-   Auto configuration on startup
-   Out-of-the-box interfaces for operate database
-   Powerful and flexible where condition wrapper
-   Multiple strategy to generate primary key
-   Lambda-style API
-   Almighty and highly customizable code generator
-   Automatic paging operation
-   SQL Inject defense
-   Support active record
-   Support pluggable custom interface
-   Build-in many useful extensions

## Getting started

-   Add MyBatis-Plus dependency
    - Latest Version: [![Maven Central](https://img.shields.io/maven-central/v/com.baomidou/mybatis-plus.svg)](https://search.maven.org/search?q=g:com.baomidou%20a:mybatis-*)
    - Maven:
      ```xml
      <dependency>
          <groupId>com.fl</groupId>
          <artifactId>mybatis-plugin-spring-boot-starter</artifactId>
          <version>Latest Version</version>
      </dependency>
      ```
    - Gradle
      ```groovy
      compile group: 'com.fl', name: 'mybatis-plugin-spring-boot-starter', version: 'Latest Version'
      ```
-   Modify mapper file extends BaseMapper interface

    ```java
    public interface UserMapper extends BaseMapper<User> {

    }
    ```

- Use it
  ``` java
  List<User> userList = userMapper.selectList(
          new QueryWrapper<User>()
                  .lambda()
                  .ge(User::getAge, 18)
  );
  ```
    MyBatis-Plus will execute the following SQL
    ```sql
    SELECT * FROM user WHERE age >= 18
    ```
## License

MyBatis-Plugin is under the Apache 2.0 license. See the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) file for details.
