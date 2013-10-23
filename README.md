`ardoq.analytics-clj`
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

ardoq.analytics-clj> (def client (initialize "<secret>"))

ardoq.analytics-clj> (identify client "user-id" {:email "test@example.org"})

ardoq.analytics-clj> (track client "user-id" "Logged in" {:plan {:type "trial" :started (DateTime.)}})

;;You can also use options like :contet, timestamp and a callback function. See api for details
ardoq.analytics-clj> (track client "user-id" "Played song" {:title "My title"} 
                         {:context {:ip "10.0.0.1"} 
                          :callback (fn [s m] (println "\n\nDONE!"))})

ardoq.analytics-clj> (make-alias client "user-id" "real-id")

```

