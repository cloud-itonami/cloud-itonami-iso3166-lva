(ns statute.facts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [statute.facts :as facts]))

(deftest lva-has-spec-basis
  (let [sb (facts/spec-basis "LVA")]
    (is (= 3 (count sb)))
    (is (every? #(str/starts-with? (:statute/url %) "https://likumi.lv/") sb))
    (is (every? :statute/law-number sb))
    (is (every? #(= "LVA" (:statute/jurisdiction %)) sb))
    (is (= (count sb) (count (set (map :statute/id sb)))))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["LVA" "JPN" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "JPN"] (:missing-jurisdictions c)))))

(deftest by-topic-filters
  (is (= ["lva.komerclikums"]
         (mapv :statute/id (facts/by-topic "LVA" :corporate-governance))))
  (is (= ["lva.fizisko-personu-datu-apstrades-likums"]
         (mapv :statute/id (facts/by-topic "LVA" :data-protection))))
  (is (= ["lva.darba-likums"]
         (mapv :statute/id (facts/by-topic "LVA" :labor))))
  (is (empty? (facts/by-topic "LVA" :environment)))
  (is (empty? (facts/by-topic "ATL" :labor))))

;; ---- this catalog is orthogonal to marketentry.facts, never re-fuses
;; ---- or contradicts its flagship IUB/VDAA platform-operator split ----

(deftest statute-catalog-never-fuses-iub-and-vdaa
  (testing "the general-law catalog is orthogonal to marketentry.facts's
  procurement-specific EIS platform-oversight/operator split and must
  never restate or collapse it"
    (let [blob (pr-str (facts/spec-basis "LVA"))]
      (is (not (str/includes? blob "VDAA"))
          "EIS's technical-operator authority belongs to marketentry.facts only")
      (is (not (str/includes? blob "VRAA"))
          "the pre-2024 VDAA name belongs to marketentry.facts only")
      (is (not (re-find #"IUB operates EIS" blob))
          "the fused-authority failure mode must never appear here either"))))

(deftest statute-catalog-does-not-duplicate-marketentry-provenance
  (testing "statute.facts cites likumi.lv statutes directly; it does not
  restate marketentry.facts's Uzņēmumu reģistrs/PIL/EIS/VID citations"
    (let [blob (pr-str (facts/spec-basis "LVA"))]
      (is (not (str/includes? blob "Publisko iepirkumu likums")))
      (is (not (str/includes? blob "eis.gov.lv")))
      (is (not (str/includes? blob "ur.gov.lv"))))))
