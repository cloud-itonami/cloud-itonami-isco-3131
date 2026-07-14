(ns plant-ops.store-test
  (:require [clojure.test :refer [deftest is testing]]
            [plant-ops.store :as store]))

(deftest mem-store-plant-lookup
  (testing "lookup registered plant"
    (let [s (store/mem-store
             {:plant-1 {:name "Thermal Plant 1" :status :operational :verified? true}})]
      (is (= {:name "Thermal Plant 1" :status :operational :verified? true}
             (store/plant s :plant-1)))))
  (testing "unregistered plant returns nil"
    (let [s (store/mem-store {})]
      (is (nil? (store/plant s :nonexistent))))))

(deftest mem-store-commit-record
  (testing "commit-record persists record"
    (let [s (store/mem-store {:plant-1 {:name "Plant 1" :status :operational :verified? true}})
          record {:plant-id :plant-1 :op :log-production-reading :payload {:value 50}}]
      (store/commit-record! s record)
      (is (some #{record} (store/records s)))))
  (testing "commit-record! requires plant-id"
    (let [s (store/mem-store {})]
      (is (thrown? #?(:clj Exception :cljs js/Error)
                   (store/commit-record! s {:op :foo :payload {}}))))))

(deftest mem-store-records
  (testing "records starts empty"
    (let [s (store/mem-store {})]
      (is (empty? (store/records s)))))
  (testing "multiple commits are persisted"
    (let [s (store/mem-store {:plant-1 {:name "Plant 1" :status :operational :verified? true}})
          r1 {:plant-id :plant-1 :op :log-production-reading :payload {:value 50}}
          r2 {:plant-id :plant-1 :op :schedule-maintenance :payload {:date "2026-07-20"}}]
      (store/commit-record! s r1)
      (store/commit-record! s r2)
      (is (= 2 (count (store/records s)))))))

(deftest mem-store-audit-ledger
  (testing "ledger starts empty"
    (let [s (store/mem-store {})]
      (is (empty? (store/ledger s)))))
  (testing "append-ledger! persists entry"
    (let [s (store/mem-store {})]
      (store/append-ledger! s {:disposition :commit :record {:op :foo}})
      (is (seq (store/ledger s))))))
