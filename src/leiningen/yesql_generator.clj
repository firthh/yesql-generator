(ns leiningen.yesql-generator
  (:require [clojure.java.jdbc :as jdbc]))

(defmulti get-tables :subprotocol)
(defmethod get-tables "mysql" [db]
  (->> (jdbc/query db ["SHOW TABLES"])
       (map (fn [m] (first (vals m))))
       (filter (fn [name] (not= name "schema_info")))))

(defmulti describe-table :subprotocol)
(defmethod describe-table "mysql" [db name]
  (jdbc/query db [(str "describe " name)]))

(defn generate-select-sql [table-name fields]
  (let [primary-keys (map :field (filter #(= (:key %) "PRI") fields))]
    (str "-- name: select-" table-name
         "\r\n"
         "SELECT "
         (clojure.string/join ", " (map :field fields))
         " FROM " table-name
         " WHERE " (clojure.string/join" AND " (map (fn [v] (str v "=" (keyword v))) primary-keys)))))

(defn generate-insert-sql [table-name fields]
  (let [primary-keys (map :field (filter #(= (:key %) "PRI") fields))]
    (str "-- name: insert-" table-name
         "\r\n"
         "INSERT INTO " table-name
         "("
         (clojure.string/join ", " (map :field fields))
         ")"
         " VALUES (" (clojure.string/join ", " (map (comp keyword :field) fields)) ")")))

(defn generate-delete-sql [table-name fields]
  (let [primary-keys (map :field (filter #(= (:key %) "PRI") fields))]
    (str "-- name: delete-" table-name
         "\r\n"
         "DELETE"
         " FROM " table-name
         " WHERE " (clojure.string/join" AND " (map (fn [v] (str v "=" (keyword v))) primary-keys)))))

(defn generate-update-sql [table-name fields]
  (let [primary-keys (filter #(= (:key %) "PRI") fields)
        not-primary-keys (filter #(not= (:key %) "PRI") fields)]
    (str "-- name: update-" table-name
         "\r\n"
         "UPDATE " table-name
         " SET "
         (clojure.string/join" AND " (map (fn [v] (str (:field v) "=" (keyword (:field v)))) not-primary-keys))
         " FROM " table-name
         " WHERE " (clojure.string/join" AND " (map (fn [v] (str (:field v) "=" (keyword (:field v)))) primary-keys)))))

(defn generate-table-queries [db table-name]
  (let [fields (describe-table db table-name)]
    (clojure.string/join
     "\r\n\r\n"
     [(generate-insert-sql table-name fields)
      (generate-select-sql table-name fields)
      (generate-delete-sql table-name fields)
      (generate-update-sql table-name fields)])))

(defn yesql-generator
  "I don't do a lot."
  [project & args]
  (let [filename "queries.sql"
        db {}]
    (->> (get-tables db)
         (map (partial generate-table-queries db))
         (clojure.string/join "\r\n\r\n")
         (spit filename))))
