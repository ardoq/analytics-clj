(ns ardoq.analytics-clj
  (:import org.joda.time.DateTime
           [com.github.segmentio AnalyticsClient Options Defaults]
           [com.github.segmentio.models Traits Context Callback EventProperties]))

(defn- map-keys
  "Maps a function over the keys of an associative collection."
  [f coll]
  (persistent! (reduce-kv #(assoc! %1 (f %2) %3)
                          (transient (empty coll))
                          coll)))

(defn initialize
  "Initializes and returns a client with given secret.

   Options:
   :host - the segment.io api endpoint (with scheme). Default value 'https://api.segment.io'.
   :max-queue-size - maximum number of messages to allow into the queue before no new messages are accepted. Default value 10000.
   :timeout - number of milliseconds to wait before timing out the request. Default value 10000."
  ([secret]
     (initialize secret {}))
  ([secret {:keys [host max-queue-size timeout]
            :or {host Defaults/HOST max-queue-size Defaults/MAX_QUEUE_SIZE timeout Defaults/TIMEOUT}}]
     (AnalyticsClient. secret (doto (Options.)
                                (.setHost host)
                                (.setMaxQueueSize max-queue-size)
                                (.setTimeout timeout)))))

(defn identify
  "Identifying a user ties all of their actions to an id, and associates user traits to that id.

   Can also take an empty map of traits if you need to specify options, but no traits.

   Options:
   :timestamp - a DateTime representing when the aliasing took place. If the alias just happened, leave it blank and we'll
                use the server's time.
   :context - map that describes anything that doesn't fit into this event's properties (such as the user's IP {:ip 'some-ip'})
   :callback - a callback that is fired when this track's batch is flushed to the server. Note: this callback is fired on the
               same thread as the async event loop that made the request. You should not perform any kind of long running operation on it.
               Example: (fn [^Boolean success ^String message] ...)"
  ([^AnalyticsClient client ^String user-id]
     (identify client user-id nil))
  ([^AnalyticsClient client ^String user-id traits & [{:keys [timestamp context callback]}]]
     (let [the-callback (if callback (reify Callback (onResponse [this success message] (callback success message))))
           t (reduce (fn [t [k v]] (.put t (name k) v)) (Traits.) traits)
           c (reduce (fn [t [k v]] (.put t (name k) v)) (Context.) context)]
       (.identify client user-id t timestamp c the-callback))))


(defn track
  "Whenever a user triggers an event, you’ll want to track it.

   Arguments:
   event - describes what this user just did. It's a human readable description like 'Played a Song', 'Printed a Report' or 'Updated Status'.
   properties - map with items that describe the event in more detail. This argument is optional, but highly recommended—you’ll find these
               properties extremely useful later.

   Options:
   :timestamp - a DateTime representing when the identify took place.If the identify just happened,leave it blank and we'll
                use the server's time. If you are importing data from the past, make sure you provide this argument.
   :context - map that describes anything that doesn't fit into this event's properties (such as the user's IP {:ip 'some-ip'})
   :callback - a callback that is fired when this track's batch is flushed to the server. Note: this callback is fired on the
               same thread as the async event loop that made the request. You should not perform any kind of long running operation on it.
               Example: (fn [^Boolean success ^String message] ...):"
  ([^AnalyticsClient client ^String user-id ^String event]
     (track client user-id event {}))
  ([^AnalyticsClient client ^String user-id ^String event properties]
     (track client user-id event properties {} {}))
  ([^AnalyticsClient client ^String user-id ^String event properties & [{:keys [timestamp context callback]}]]
     (let [the-callback (if callback (reify Callback (onResponse [this success message] (callback success message))))
           props (EventProperties. (into-array Object (flatten (vec (map-keys name properties)))))
           c (reduce (fn [t [k v]] (.put t (name k) v)) (Context.) context)]
       (.track client user-id event props timestamp c the-callback))))

(defn make-alias
  "Aliases an anonymous user into an identified user.

   from - the anonymous user's id before they are logged in.
   to - the identified user's id after they're logged in.

   Options:
   :timestamp - a DateTime representing when the identify took place.If the identify just happened,leave it blank and we'll
                use the server's time. If you are importing data from the past, make sure you provide this argument.
   :context - map that describes anything that doesn't fit into this event's properties (such as the user's IP {:ip 'some-ip'})
   :callback - a callback that is fired when this track's batch is flushed to the server. Note: this callback is fired on the
               same thread as the async event loop that made the request. You should not perform any kind of long running operation on it.
               Example: (fn [^Boolean success ^String message] ...)"
  ([^AnalyticsClient client ^String from ^String to] (make-alias client from to {}))
  ([^AnalyticsClient client ^String from ^String to & [{:keys [timestamp context callback]}]]
     (let [the-callback (if callback (reify Callback (onResponse [this success message] (callback success message))))
           c (reduce (fn [t [k v]] (.put t (name k) v)) (Context.) context)]
       (.alias client from to timestamp context the-callback))))

(defn flush-queue
  "Call flush to block until all the messages are flushed to the server. This is especially useful when turning off your web server
   to make sure we have all your messages."
  [^AnalyticsClient client]
  (.flush client))
