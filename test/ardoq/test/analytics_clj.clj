(ns ardoq.test.analytics-clj
  (:import [org.joda.time DateTime])
  (:require [clojure.test :refer :all]
            [ardoq.analytics-clj :as a]))

(def called (atom false))

(defn my-test-fixture [f]
  (reset! called false)
  (f))

(use-fixtures :each my-test-fixture)

(deftest track-user-id-and-event
  (with-redefs [a/track* (fn [client user-id event properties options]
                           (is (= "client" client))
                           (is (= "user-id" user-id))
                           (is (= "My Event" event))
                           (is (= {} properties))
                           (is (= {"library" "analytics-clj"} (.getContext options)))
                           (swap! called not))]
    (a/track "client" "user-id" "My Event")
    (is (true? @called))))

(deftest track-user-event-properties
  (with-redefs [a/track* (fn [client user-id event properties options]
                           (is (= "client" client))
                           (is (= "user-id" user-id))
                           (is (= "My Event" event))
                           (is (= {"my-property" "abc123"} properties))
                           (is (= {"library" "analytics-clj"} (.getContext options)))
                           (swap! called not))]
    (a/track "client" "user-id" "My Event" {:my-property "abc123"})
    (is (true? @called))))

(deftest track-user-id-event-properties-options
  (let [now (DateTime/now)]
    (with-redefs [a/track* (fn [client user-id event properties options]
                             (is (= "client" client))
                             (is (= "user-id" user-id))
                             (is (= "My Event" event))
                             (is (= {"my-property" "abc123"} properties))
                             (is (= {"library" "analytics-clj"} (.getContext options)))
                             (is (= now (.getTimestamp options)))
                             (is (= {"my-integration" true} (.getIntegrations options)))
                             (is (= "anonymous" (.getAnonymousId options)))
                             (swap! called not))]
      (a/track "client" "user-id" "My Event" {:my-property "abc123"} {:timestamp    now
                                                                      :integration  "my-integration"
                                                                      :anonymous-id "anonymous"})
      (is (true? @called)))))
;
(deftest test-make-alias-from-to-only
  (with-redefs [a/make-alias* (fn [client from to options]
                                (is (= "client" client))
                                (is (= "from" from))
                                (is (= "to" to))
                                (is (= {"library" "analytics-clj"} (.getContext options)))
                                (swap! called not))]
    (a/make-alias "client" "from" "to")
    (is (true? @called))))
;
(deftest test-make-alias-user-id-from-to-options
  (let [now (DateTime/now)]
    (with-redefs [a/make-alias* (fn [client from to options]
                                  (is (= "client" client))
                                  (is (= "from" from))
                                  (is (= "to" to))
                                  (is (= {"library" "analytics-clj"} (.getContext options)))
                                  (is (= now (.getTimestamp options)))
                                  (is (= {"my-integration" true} (.getIntegrations options)))
                                  (is (= "anonymous" (.getAnonymousId options)))
                                  (swap! called not))]
      (a/make-alias "client" "from" "to" {:timestamp    now
                                          :integration  "my-integration"
                                          :anonymous-id "anonymous"})
      (is (true? @called)))))

(deftest test-identify-user-id-only
  (with-redefs [a/identify* (fn [client user-id traits options]
                              (is (= "client" client))
                              (is (= "user-id" user-id))
                              (is (= {} traits))
                              (is (= {"library" "analytics-clj"} (.getContext options)))
                              (swap! called not))]
    (a/identify "client" "user-id")
    (is (true? @called))))
;
(deftest test-identify-user-id-traits
  (let [now (DateTime/now)]
    (with-redefs [a/identify* (fn [client user-id traits options]
                                (is (= "client" client))
                                (is (= "user-id" user-id))
                                (is (= {"trait" "one"} traits))
                                (is (= {"library" "analytics-clj"} (.getContext options)))
                                (swap! called not))]
      (a/identify "client" "user-id" {:trait "one"})
      (is (true? @called)))))


(deftest test-initialize
  (is (= "madeupid" (.getWriteKey (a/initialize "madeupid")))))