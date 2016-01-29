(ns leiningen.yesql-generator
  (:require [clojure.java.jdbc :as jdbc]))

(defn get-database-type [db & args]
  (-> db
      :connection
      .getMetaData
      .getDatabaseProductName))

(defmulti get-tables get-database-type)
(defmethod get-tables "MySQL" [db]
  (->> (jdbc/query db ["SHOW TABLES"])
       (map (fn [m] (first (vals m))))
       (filter (fn [name] (not= name "schema_info")))))

(defmulti describe-table get-database-type)
(defmethod describe-table "MySQL" [db name]
  (jdbc/query db [(str "describe " name)]))

(defn generate-select-sql [table-name fields]
  (let [primary-keys (map :field (filter #(= (:key %) "PRI") fields))]
    (format
     "--name: select-%s
SELECT %s
FROM %s
WHERE %s
"
     table-name
     (clojure.string/join ", " (map :field fields))
     table-name
     (clojure.string/join" AND " (map (fn [v] (str v "=" (keyword v))) primary-keys)))))

(defn generate-insert-sql [table-name fields]
  (let [primary-keys (map :field (filter #(= (:key %) "PRI") fields))]
    (format
     "-- name:insert-%s!
INSERT INTO %s(%s)
VALUES (%s)
"
     table-name
     table-name
     (clojure.string/join ", " (map :field fields))
     (clojure.string/join ", " (map (comp keyword :field) fields)) ")")))

(defn generate-delete-sql [table-name fields]
  (let [primary-keys (map :field (filter #(= (:key %) "PRI") fields))]
    (format
     "-- name: delete-%s!
DELETE FROM %s
WHERE %s
"
     table-name
     table-name
     (clojure.string/join" AND " (map (fn [v] (str v "=" (keyword v))) primary-keys)))))

(defn generate-update-sql [table-name fields]
  (let [primary-keys (filter #(= (:key %) "PRI") fields)
        not-primary-keys (filter #(not= (:key %) "PRI") fields)]
    (format
     "-- name: update-%s!
UPDATE %s
SET %s
FROM %s
WHERE %s
"
     table-name
     table-name
     (clojure.string/join" AND " (map (fn [v] (str (:field v) "=" (keyword (:field v)))) not-primary-keys))
     table-name
     (clojure.string/join" AND " (map (fn [v] (str (:field v) "=" (keyword (:field v)))) primary-keys)))))

(defn generate-table-queries [db table-name]
  (let [fields (describe-table db table-name)]
    (clojure.string/join
     "\n"
     [(generate-insert-sql table-name fields)
      (generate-select-sql table-name fields)
      (generate-delete-sql table-name fields)
      (generate-update-sql table-name fields)])))

(defn yesql-generator
  "I don't do a lot."
  [project & args]
  (let [filename "queries.sql"
        db {:connection (jdbc/get-connection {})}]
    (->> (get-tables db)
         (map (partial generate-table-queries db))
         (clojure.string/join "\n")
         (spit filename))
    (.close (:connection db))))
