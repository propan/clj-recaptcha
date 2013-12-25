(ns clj-recaptcha.client-test
  (:require [clojure.test :refer :all]
            [clj-recaptcha.client :as c]))

(deftest verify
  (testing "No challenge or response is given"
    (let [res (c/verify "KEY" nil nil nil)]
      (is (= {:valid? false :error "incorrect-captcha-sol"} res))))

  (testing "Handles exceptions"
    (with-redefs-fn {#'clj-http.client/post (fn [url query] (throw (Exception. "Troubles!")))}
      #(let [res (c/verify "KEY" "123" "678" "127.0.0.1")]
         (is (= {:valid? false :error "recaptcha-not-reachable"} res)))))
  
  (testing "Successful case"
    (with-redefs-fn {#'clj-http.client/post (fn [url query]
                                              (when (and (= url "http://www.google.com/recaptcha/api/verify")
                                                         (= query {:form-params        {:privatekey "KEY"
                                                                                        :remoteip   "127.0.0.1"
                                                                                        :challenge  "123"
                                                                                        :response   "678"}
                                                                   :proxy-host         nil
                                                                   :proxy-port         nil
                                                                   :connection-manager nil}))
                                                {:status 200
                                                 :body   "true\n"}))}
      #(let [res (c/verify "KEY" "123" "678" "127.0.0.1")]
         (is (= {:valid? true :error nil} res)))))
  
  (testing "Incorrect user's input"
    (with-redefs-fn {#'clj-http.client/post (fn [url query]
                                              (when (and (= url "http://www.google.com/recaptcha/api/verify")
                                                         (= query {:form-params        {:privatekey "KEY"
                                                                                        :remoteip   "127.0.0.1"
                                                                                        :challenge  "123"
                                                                                        :response   "678"}
                                                                   :proxy-host         "localhost"
                                                                   :proxy-port         456
                                                                   :connection-manager :CM}))
                                                {:status 200
                                                 :body   "false\nsome-error-code"}))}
      #(let [res (c/verify "KEY" "123" "678" "127.0.0.1" :proxy-host "localhost" :proxy-port 456 :connection-manager :CM)]
         (is (= {:valid? false :error "some-error-code"} res))))))

(deftest create-conn-manager-test
  (with-redefs-fn {#'clj-http.conn-mgr/make-reusable-conn-manager (fn [opts]
                                                                    (when (= opts {:threads 5})
                                                                      :ok))}
    #(is (= :ok (c/create-conn-manager {:threads 5})))))

(deftest render-test
  (testing "Render with default parameters"
    (let [res (c/render "234KEY")]
      (is (true? (.contains res "http://www.google.com/recaptcha/api/noscript?k=234KEY")))
      (is (true? (.contains res "http://www.google.com/recaptcha/api/challenge?k=234KEY")))))

  (testing "Render with ssl and no noscript section"
    (let [res (c/render "234KEY" :ssl? true :noscript? false)]
      (is (false? (.contains res "http://www.google.com/recaptcha/api/noscript?k=234KEY")))
      (is (true? (.contains res "https://www.google.com/recaptcha/api/challenge?k=234KEY")))))

  (testing "Render with error and no noscript section"
    (let [res (c/render "234KEY" :error "recaptcha-not-reachable" :noscript? false)]
      (is (false? (.contains res "http://www.google.com/recaptcha/api/noscript?k=234KEY")))
      (is (true? (.contains res "http://www.google.com/recaptcha/api/challenge?k=234KEY&error=recaptcha-not-reachable")))))

  (testing "Render with theming"
    (let [res (c/render "234KEY" :display {:theme "clean" :lang "de"} :iframe-height 111 :iframe-width 222)]
      (is (true? (.contains res "RecaptchaOptions")))
      (is (true? (.contains res "height='111'")))
      (is (true? (.contains res "width='222'")))      
      (is (true? (.contains res "http://www.google.com/recaptcha/api/noscript?k=234KEY")))
      (is (true? (.contains res "http://www.google.com/recaptcha/api/challenge?k=234KEY"))))))
