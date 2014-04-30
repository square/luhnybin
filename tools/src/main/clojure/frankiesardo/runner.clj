(ns frankiesardo.runner
  (:require
   [clojure.java.io :as io])
  (:gen-class))

(defn ^Boolean digit? [^Character c]
  (Character/isDigit c))

(defn ^Boolean separator? [^Character c]
  (or (= c \space) (= c \-)))

(defn ^Integer digit->int [^Character c]
  (Character/getNumericValue c))

(defn ^Boolean digit-or-separator? [^Character c]
  (or (digit? c) (separator? c)))

(defn ^Boolean ignored? [^Character c]
  (not (digit? c)))

(defn iterate-drop1 [coll]
  (take (count coll) (iterate #(drop 1 %) coll)))

(defn ^Boolean luhn-test? [digits]
  (->> digits
       reverse
       (map-indexed #(if (even? %1) %2 (* 2 %2)))
       (reduce #(+ %1 (quot %2 10) (rem %2 10)) 0)
       (#(zero? (mod % 10)))))

(defn find-card? [digits length]
  (if (>= (count digits) length)
    (luhn-test? (subvec digits 0 length))
    false))

(defn max-mask-from-char [possible-cc-number]
  (let [first-char (first possible-cc-number)]
    (if (separator? first-char)
      [first-char 0]
      (let [all-digits (->> possible-cc-number (filter digit?) (map digit->int) (vec))]
        (cond
         (find-card? all-digits 16) [first-char 16]
         (find-card? all-digits 15) [first-char 15]
         (find-card? all-digits 14) [first-char 14]
         :else                      [first-char 0])))))

(defn take-or-hide [[result curr-mask-length] [digit digit-mask-length]]
  (cond
   (separator? digit)       [(conj result digit) curr-mask-length]
   (pos? digit-mask-length) [(conj result \X) (dec (max digit-mask-length curr-mask-length))]
   (pos? curr-mask-length)  [(conj result \X) (dec curr-mask-length)]
   :else                    [(conj result digit) 0]))

(defn mask-cc-number [possible-cc-number] ; [1 2 3 - 4 5 ...]
  (->> (iterate-drop1 possible-cc-number) ; ([1 2 3 - 4 5] [2 3 - 4 5] [3 - 4 5] [- 4 5] [4 5] ...)
       (map max-mask-from-char)           ; ([1 14] [2 0] [3 0] [- 0] [4 15] [5 16] ...)
       (reduce take-or-hide [[] 0])       ; [(\X \X \X - \X \X ...) 0]
       first))                            ; (\X \X \X - \X \X ...)

(defn mask-line [line]
  (let [[ignored candidate] (split-with ignored? line)
        [possible-cc-number remaining] (split-with digit-or-separator? candidate)]
    (concat ignored (mask-cc-number possible-cc-number) (if (seq remaining) (mask-line remaining)))))

(defn -main [& args]
  (with-open [rdr (io/reader *in*)]
    (doseq [line (line-seq rdr)]
      (println (apply str (mask-line line))))))
