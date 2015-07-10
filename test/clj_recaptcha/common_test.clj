(ns clj-recaptcha.common-test
    (:require [clojure.test :refer :all]
              [clj-recaptcha.common :as c]))

(deftest create-conn-manager-test
  (with-redefs-fn {#'clj-http.conn-mgr/make-reusable-conn-manager (fn [opts]
                                                                      (when (= opts {:threads 5})
                                                                                     :ok))}
                  #(is (= :ok (c/create-conn-manager {:threads 5})))))

