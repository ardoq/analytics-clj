segmentio
=========

Idiomatic Clojure wrapper for the segmet.io java api.

See [segmet.io java library](https://segment.io/libraries/java) for more info.

##Artifacts

Artifacts are [released to clojars](https://clojars.org/segmentio).

If you are using Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

##Installation

With Leiningen:

```clj
[segmentio "0.1.0"]
```

With Maven:

```xml
<dependency>
  <groupId>segmentio</groupId>
  <artifactId>segmentio</artifactId>
  <version>0.1.0</version>
</dependency>

```

## Usage


```clojure

segmentio.analytics> (def client (initialize "<api-token>"))

segmentio.analytics> (identify client "user-id" {:email "test@example.org"})

segmentio.analytics> (track client "user-id" "Logged in" {:plan {:type "trial" :started (DateTime.)}})

;;You can also use options like :contet, timestamp and a callback function. See api for details
segmentio.analytics> (track client "user-id" "Played song" {:title "Foobar"} 
                         {:context {:ip "10.0.0.1"} 
                          :callback (fn [s m] (println "\n\nDONE!"))})

segmentio.analytics> (make-alias client "user-id" "foobar")

```

