(ns ardoq.analytics-clj
  (:import
    [com.github.segmentio AnalyticsClient Config Defaults]
    [com.github.segmentio.models Traits Context Props Options]))

(def context
  (doto (Context.)
    (.put "library" "analytics-clj")))

(defn- map-keys
  "Maps a function over the keys of an associative collection."
  [f coll]
  (persistent! (reduce-kv #(assoc! %1 (f %2) %3)
                          (transient (empty coll))
                          coll)))

(defn- identify*
  [client user-id traits options]
  (.identify client user-id traits options))

(defn- track* [client user-id event properties options]
  (.track client user-id event properties options))

(defn- make-alias*
  [client from to options]
  (.alias client from to options))

(defn- create-options [timestamp integration anonymous-id]
  (let [options (doto (Options.)
                  (.setContext context))]
    (when timestamp
      (.setTimestamp options timestamp))
    (when integration
      (.setIntegration options integration true))
    (when anonymous-id
      (.setAnonymousId options anonymous-id))
    options))

(defn initialize
  "Initializes and returns a client with given secret.

   Options:
   :host - the segment.io api endpoint (with scheme). Default value 'https://api.segment.io'.
   :max-queue-size - maximum number of messages to allow into the queue before no new messages are accepted. Default value 10000.
   :timeout - number of milliseconds to wait before timing out the request. Default value 10000."
  ([secret]
    (initialize secret {}))
  ([secret {:keys [host max-queue-size timeout]
            :or   {host Defaults/HOST max-queue-size Defaults/MAX_QUEUE_SIZE timeout Defaults/TIMEOUT}}]
    (AnalyticsClient. secret (doto (Config.)
                               (.setHost host)
                               (.setMaxQueueSize max-queue-size)
                               (.setTimeout timeout)))))

(defn identify
  "Identifying a user ties all of their actions to an id, and associates user traits to that id.

   Can also take an empty map of traits if you need to specify options, but no traits.

   Options: a map with keys:
      :timestamp - DateTime, useful for backdating events
      :integration - this call will be sent to the target integration
      :anonymous-id - the cookie / anonymous Id of this visitor
  "
  ([^AnalyticsClient client ^String user-id]
    (identify client user-id nil))
  ([^AnalyticsClient client ^String user-id traits & [{:keys [timestamp integration anonymous-id]}]]
    (let [traits (reduce (fn [t [k v]] (.put ^Traits t (name k) v)) (Traits.) traits)
          options (create-options timestamp integration anonymous-id)]
      (identify* client user-id traits options))))

(defn full-name
  "Returns the full name of the map key. If it's a symbol, retrieves the full namespaced name and returns that instead."
  [k]
  (if (keyword? k)
    (str (.-sym k))
    (name k)))

(defn track
  "Whenever a user triggers an event, you’ll want to track it.

   Arguments:
   event - describes what this user just did. It's a human readable description like 'Played a Song', 'Printed a Report' or 'Updated Status'.
   properties - map with items that describe the event in more detail. This argument is optional, but highly recommended—you’ll find these
               properties extremely useful later.

   Options: a map with keys:
      :timestamp - DateTime, useful for backdating events
      :integration - this call will be sent to the target integration
      :anonymous-id - the cookie / anonymous Id of this visitor
  "
  ([^AnalyticsClient client ^String user-id ^String event]
    (track client user-id event {}))
  ([^AnalyticsClient client ^String user-id ^String event properties]
    (track client user-id event properties {} {}))
  ([^AnalyticsClient client ^String user-id ^String event properties & [{:keys [timestamp integration anonymous-id]}]]
    (let [properties (Props. (into-array Object (apply concat (vec (map-keys full-name properties)))))
          options (create-options timestamp integration anonymous-id)]
      (track* client user-id event properties options))))

(defn make-alias
  "Aliases an anonymous user into an identified user.

   from - the anonymous user's id before they are logged in.
   to - the identified user's id after they're logged in.

   Options: a map with keys:
      :timestamp - DateTime, useful for backdating events
      :integration - this call will be sent to the target integration
      :anonymous-id - the cookie / anonymous Id of this visitor
  "
  ([^AnalyticsClient client ^String from ^String to] (make-alias client from to {}))
  ([^AnalyticsClient client ^String from ^String to & [{:keys [timestamp integration anonymous-id]}]]
    (let [options (create-options timestamp integration anonymous-id)]
      (make-alias* client from to options))))

(defn flush-queue
  "Call flush to block until all the messages are flushed to the server. This is especially useful when turning off your web server
   to make sure we have all your messages."
  [^AnalyticsClient client]
  (.flush client))
