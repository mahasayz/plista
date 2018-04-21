# Build and Packaging

Compile and package the code to JAR
```
sbt assembly
```

# Usage

Run the JAR prepared in the previous step
```
java -jar target/scala-2.11/plista-test.jar
```

Now the backend server is running. In order to start the crawler, execute the following command:
```
curl -XGET 'localhost:8080/crawler/start'
```

Now you should start seeing ticks in your console that the crawler has started.

## Submit URL for Crawling

Try submitting an URL to crawl as follows:
```
curl -XPOST 'localhost:8080/crawler/crawl' -H "Content-Type: application/json" -d '{"url":"http://www.example.com"}'
```
## Query Content

Now try querying any text you find visible on the page, for the example domain above, let's try looking for word `Example` as follows:
```
curl -XPOST 'localhost:8080/crawler/query' -H "Content-Type: application/json" -d '{"keyword":"Example"}'
```
