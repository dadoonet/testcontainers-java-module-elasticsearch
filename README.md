**IMPORTANT**: This project has moved under testcontainers project as an [elasticsearch module](https://github.com/testcontainers/testcontainers-java/tree/master/modules/elasticsearch).

No more released will be done in this repository.

# TestContainers elasticsearch testing module

[![Build Status](https://travis-ci.org/dadoonet/testcontainers-java-module-elasticsearch.svg?branch=master)](https://travis-ci.org/dadoonet/testcontainers-java-module-elasticsearch)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.pilato.elasticsearch.testcontainers/testcontainers-elasticsearch/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/fr.pilato.elasticsearch.testcontainers/testcontainers-elasticsearch/)

Testcontainers module for [elasticsearch](https://www.elastic.co/products/elasticsearch).

Note that it's based on the [official Docker image](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/docker.html) provided by elastic.

See [testcontainers.org](https://www.testcontainers.org) for more information about Testcontainers.

## Usage example

You can start an elasticsearch container instance from any Java application by using:

```java
// Create the elasticsearch container.
ElasticsearchContainer container = new ElasticsearchContainer();

// Optional but highly recommended: Specify the version you need.
container.withVersion("6.3.0");

// Optional: you can also set what is the Docker registry you want to use with.
container.withBaseUrl("docker.elastic.co/elasticsearch/elasticsearch");

// Optional: define which plugin you would like to install.
// It will download it from internet when building the image
container.withPlugin("discovery-gce");

// Optional: define the plugins directory which should contain plugins ZIP files you want to install.
// Note that if you want to just download an official plugin, use withPlugin(String) instead.
container.withPluginDir(Paths.get("/path/to/zipped-plugins-dir"));

// Optional: you can also change the password for X-Pack (for versions >= 6.1).
container.withEnv("ELASTIC_PASSWORD", "changeme");

// Optional: you can add secured settings in case you are using a plugin which requires it.
container.withSecureSetting("foo", "bar");

// Start the container. This step might take some time...
container.start();

// Do whatever you want here.
final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));
RestClient client = RestClient.builder(container.getHost())
        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
        .build();
Response response = client.performRequest("GET", "/");

// Stop the container.
container.stop();
```

## JUnit 4 Usage example

Running elasticsearch as a resource during a test:

```java
public class SomeTest {
    @Rule
    public ElasticsearchResource elasticsearch = new ElasticsearchResource();

    @Test
    public void someTestMethod() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "changeme"));

        RestClient client = RestClient.builder(elasticsearch.getHost())
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
        Response response = client.performRequest("GET", "/");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
```

The default version is read from a property file named `elasticsearch.properties` in `fr.pilato.elasticsearch.containers` package.
If you provide an `elasticsearch.properties` in `fr.pilato.elasticsearch.containers` package,
the settings will be read from it:

```properties
baseUrl=docker.elastic.co/elasticsearch/elasticsearch
version=6.3.0
```

You can also define this programmatically with:

```java
@Rule
public ElasticsearchResource elasticsearch = new ElasticsearchResource(
        "docker.elastic.co/elasticsearch/elasticsearch", // baseUrl (can be null)
        "6.3.0",                                         // version (can be null)
        Paths.get("/path/to/zipped-plugins-dir"),        // pluginsDir (can be null)
        Collections.singletonList("ingest-attachment"),  // standard plugins (can be empty)
        Collections.singletonMap("foo", "bar"),          // Map of secured settings (can be empty)
        "changeme");                                     // X-Pack security password to set (can be null)
```

Note that if you are still using the [TransportClient](https://www.elastic.co/guide/en/elasticsearch/client/java-api/6.3/transport-client.html)
(not recommended as deprecated), the default cluster name is set to `docker-cluster` so you need to change `cluster.name` setting
or set `client.transport.ignore_cluster_name` to `true`.

## Running without x-pack

If you prefer to start a Docker image without x-pack plugin, which means with no security or
other advanced features, you can use this baseUrl instead: `docker.elastic.co/elasticsearch/elasticsearch-oss`.

## Dependency information

### Maven

```
<dependency>
    <groupId>fr.pilato.elasticsearch.testcontainers</groupId>
    <artifactId>testcontainers-elasticsearch</artifactId>
    <version>0.1</version>
</dependency>
```

### Gradle

```
compile group: 'fr.pilato.elasticsearch.testcontainers', name: 'testcontainers-elasticsearch', version: '0.1'
```


# Release guide

To release the project you need to run the release plugin with the `release` profile as you need to sign the artifacts:

```sh
mvn release:prepare
git push --tags
mvn release:perform -Prelease
```

If you need to skip the tests, run:

```sh
mvn release:perform -Prelease -Darguments="-DskipTests"
```

To announce the release, run:

```sh
cd target/checkout
# Run the following command if you want to check the announcement email
mvn changes:announcement-generate
cat target/announcement/announcement.vm

# Announce the release (change your smtp username and password)
mvn changes:announcement-mail -Dchanges.username='YourSmtpUserName' -Dchanges.password='YourSmtpUserPassword'
```

# License

See [LICENSE](LICENSE).

# Copyright

Copyright (c) 2017, 2018 David Pilato.

