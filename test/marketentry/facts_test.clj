(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest lva-has-spec-basis
  (let [sb (facts/spec-basis "LVA")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (= 4 (count (:required-evidence sb))) "recommended check count is 4, not padded")
    (is (some? (facts/rep-spec-basis "LVA")))
    (is (some? (facts/corporate-number-spec-basis "LVA")))
    (is (some? (facts/platform-operator-spec-basis "LVA")))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "LVA")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "LVA" all)))
    (is (not (facts/required-evidence-satisfied? "LVA" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "ATL" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["LVA" "USA" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 2 (:covered c)))
    (is (= ["ATL"] (:missing-jurisdictions c)))))

;; ---- the central fabrication trap for this jurisdiction ----

(deftest iub-and-vdaa-are-distinct-authorities
  (testing "EIS platform-operator spec-basis keeps legal oversight (IUB) and technical operator (VDAA) SEPARATE"
    (let [pob (facts/platform-operator-spec-basis "LVA")]
      (is (some? pob))
      (is (some? (:platform-oversight-authority pob)))
      (is (some? (:platform-operator-authority pob)))
      (is (not= (:platform-oversight-authority pob) (:platform-operator-authority pob))
          "IUB (oversight) and VDAA (technical operator) must never be the same value")
      (is (re-find #"IUB" (:platform-oversight-authority pob)))
      (is (re-find #"VDAA" (:platform-operator-authority pob)))
      (is (re-find #"VRAA" (:platform-operator-note pob))
          "the note must record VDAA's 2024 rename from VRAA")
      (is (not (re-find #"IUB" (:platform-operator-authority pob)))
          "the technical-operator value itself must not also name IUB (no fusion)"))))

(deftest owner-authority-is-iub-not-fused-with-eis-operator
  (testing "top-level :owner-authority (the general procurement-law authority) is IUB, distinct from the platform operator"
    (let [sb (facts/spec-basis "LVA")]
      (is (re-find #"IUB" (:owner-authority sb)))
      (is (not= (:owner-authority sb) (:platform-operator-authority (facts/platform-operator-spec-basis "LVA")))))))

(deftest no-platform-operator-spec-basis-for-jurisdictions-without-one
  (is (nil? (facts/platform-operator-spec-basis "USA")))
  (is (nil? (facts/platform-operator-spec-basis "ATL"))))
