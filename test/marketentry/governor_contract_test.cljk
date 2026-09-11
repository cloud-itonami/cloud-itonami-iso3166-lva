(ns marketentry.governor-contract-test
  "The governor contract as executable tests -- this vertical's own
  Trust Controls implemented faithfully. The core invariant under
  test:

    MarketEntry-LLM never drafts or submits a filing the Market-Entry
    Compliance Governor would reject, `:filing/draft`/`:filing/submit`
    NEVER auto-commit at any phase, `:engagement/intake` MAY auto-commit
    when clean, and every decision (commit OR hold) leaves exactly one
    ledger fact. On top of that shared family contract, this repo's
    OWN flagship invariant: the governor correctly distinguishes IUB
    (legal/regulatory-oversight authority) from VDAA (EIS's separate
    technical operator, renamed from VRAA in 2024) and HARD-holds any
    proposal that fuses them."
  (:require [clojure.test :refer [deftest is testing]]
            [langgraph.graph :as g]
            [marketentry.store :as store]
            [marketentry.operation :as op]))

(defn- fresh []
  (let [db (store/seed-db)]
    [db (op/build db)]))

(def operator {:actor-id "op-1" :actor-role :market-entry-operator :phase 3})

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn- assess!
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-assess") {:op :jurisdiction/assess :subject subject} operator)
  (approve! actor (str tid-prefix "-assess")))

(defn- draft!
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-draft") {:op :filing/draft :subject subject} operator)
  (approve! actor (str tid-prefix "-draft")))

(deftest clean-intake-auto-commits
  (let [[db actor] (fresh)
        res (exec-op actor "t1"
                  {:op :engagement/intake :subject "eng-1"
                   :patch {:id "eng-1" :operator "Kita Systems SIA"}} operator)]
    (is (= :commit (get-in res [:state :disposition])))
    (is (= "Kita Systems SIA" (:operator (store/engagement db "eng-1"))) "SSoT actually updated")
    (is (= 1 (count (store/ledger db))))))

(deftest jurisdiction-assess-always-needs-approval
  (testing "assess is never in any phase's :auto set -- always human approval, even when clean"
    (let [[db actor] (fresh)
          res (exec-op actor "t2" {:op :jurisdiction/assess :subject "eng-1"} operator)]
      (is (= :interrupted (:status res)))
      (let [r2 (approve! actor "t2")]
        (is (= :commit (get-in r2 [:state :disposition])))
        (is (some? (store/assessment-of db "eng-1")))))))

(deftest fabricated-jurisdiction-is-held
  (testing "a jurisdiction/assess proposal with no official spec-basis -> HOLD"
    (let [[db actor] (fresh)
          res (exec-op actor "t3"
                    {:op :jurisdiction/assess :subject "eng-1" :no-spec? true} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:no-spec-basis} (-> (store/ledger db) first :basis)))
      (is (nil? (store/assessment-of db "eng-1")) "no assessment written"))))

(deftest draft-without-assessment-is-held
  (testing "filing/draft before any jurisdiction assessment -> HOLD (evidence incomplete)"
    (let [[db actor] (fresh)
          res (exec-op actor "t4" {:op :filing/draft :subject "eng-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:evidence-incomplete} (-> (store/ledger db) first :basis))))))

;; ---- the central fabrication trap for this jurisdiction: IUB vs VDAA ----

(deftest clean-assess-correctly-distinguishes-iub-from-vdaa
  (testing "a clean jurisdiction/assess proposal keeps the legal-oversight authority (IUB) and EIS's technical operator (VDAA) separate -- settles as normal escalate/approve/commit, no fusion hold"
    (let [[db actor] (fresh)
          res (exec-op actor "t5" {:op :jurisdiction/assess :subject "eng-1"} operator)]
      (is (= :interrupted (:status res)) "still escalates for human approval, same as any assess")
      (let [r2 (approve! actor "t5")
            assessment (store/assessment-of db "eng-1")]
        (is (= :commit (get-in r2 [:state :disposition])))
        (is (= "Iepirkumu uzraudzības birojs (IUB) — Procurement Monitoring Bureau"
               (:platform-legal-authority assessment)))
        (is (re-find #"VDAA" (:platform-technical-operator assessment)))
        (is (not= (:platform-legal-authority assessment) (:platform-technical-operator assessment)))))))

(deftest fused-platform-operator-claim-is-held-and-unoverridable
  (testing "a jurisdiction/assess proposal that fuses IUB and VDAA into one authority ('IUB operates EIS') -> HARD hold, settles immediately, no interrupt"
    (let [[db actor] (fresh)
          res (exec-op actor "t6" {:op :jurisdiction/assess :subject "eng-1" :fuse-platform-operator? true} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:platform-operator-fused} (-> (store/ledger db) first :basis)))
      (is (nil? (store/assessment-of db "eng-1")) "no fused assessment written -- HARD violations never commit"))))

(deftest ur-registration-missing-is-held-and-unoverridable
  (testing "missing Register of Enterprises (Uzņēmumu reģistrs) verification -> HARD hold (flagship engagement-ground-truth check)"
    (let [[db actor] (fresh)
          _ (assess! actor "t7pre" "eng-4")
          _ (draft! actor "t7pre" "eng-4")
          res (exec-op actor "t7" {:op :filing/submit :subject "eng-4"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:ur-registration-missing} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest engagement-fee-mismatch-is-held
  (testing "claimed fee that doesn't equal base + months x rate -> HOLD"
    (let [[db actor] (fresh)
          _ (assess! actor "t8pre" "eng-3")
          _ (draft! actor "t8pre" "eng-3")
          res (exec-op actor "t8" {:op :filing/submit :subject "eng-3"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:engagement-fee-mismatch} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest vat-unverified-is-held-and-unoverridable
  (testing "unverified VID VAT registration when required -> HARD hold"
    (let [[db actor] (fresh)
          _ (assess! actor "t9pre" "eng-5")
          _ (draft! actor "t9pre" "eng-5")
          res (exec-op actor "t9" {:op :filing/submit :subject "eng-5"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:vat-unverified} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest submit-always-escalates-then-human-decides
  (testing "a clean fully-assessed submit still ALWAYS interrupts for human approval -- filing/submit never auto-commits at any phase"
    (let [[db actor] (fresh)
          _ (assess! actor "t10pre" "eng-1")
          _ (draft! actor "t10pre" "eng-1")
          r1 (exec-op actor "t10" {:op :filing/submit :subject "eng-1"} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, submit record drafted"
        (let [r2 (approve! actor "t10")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:submitted? (store/engagement db "eng-1"))))
          (is (= 1 (count (store/submit-history db))) "one draft submit record"))))))

(deftest draft-always-escalates-then-human-decides
  (testing "a clean fully-assessed draft still ALWAYS interrupts for human approval -- filing/draft never auto-commits at any phase"
    (let [[db actor] (fresh)
          _ (assess! actor "t11pre" "eng-1")
          r1 (exec-op actor "t11" {:op :filing/draft :subject "eng-1"} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, draft record drafted"
        (let [r2 (approve! actor "t11")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:drafted? (store/engagement db "eng-1"))))
          (is (= 1 (count (store/draft-history db))) "one draft record"))))))

(deftest engagement-double-draft-is-held
  (testing "drafting the same engagement twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (assess! actor "t12pre" "eng-1")
          _ (draft! actor "t12pre" "eng-1")
          res (exec-op actor "t12" {:op :filing/draft :subject "eng-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-drafted} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/draft-history db))) "still only the one earlier draft"))))

(deftest engagement-double-submit-is-held
  (testing "submitting the same engagement twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (assess! actor "t13pre" "eng-1")
          _ (draft! actor "t13pre" "eng-1")
          _ (exec-op actor "t13a" {:op :filing/submit :subject "eng-1"} operator)
          _ (approve! actor "t13a")
          res (exec-op actor "t13" {:op :filing/submit :subject "eng-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-submitted} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/submit-history db))) "still only the one earlier submit"))))

(deftest every-decision-leaves-one-ledger-fact
  (testing "write-only-through-ledger: N operations -> N ledger facts (append-only, never rewritten)"
    (let [[db actor] (fresh)]
      (exec-op actor "a" {:op :engagement/intake :subject "eng-1"
                          :patch {:id "eng-1" :operator "Kita Systems SIA"}} operator)
      (exec-op actor "b" {:op :jurisdiction/assess :subject "eng-1" :no-spec? true} operator)
      (exec-op actor "c" {:op :jurisdiction/assess :subject "eng-1" :fuse-platform-operator? true} operator)
      (is (= 3 (count (store/ledger db)))
          "one commit + two holds, all recorded, none overwritten")
      (is (every? #(contains? #{:commit :hold} (:disposition %)) (store/ledger db))))))

(deftest ledger-is-append-only
  (testing "the same engine used across N runs never shrinks or rewrites the ledger, only grows it"
    (let [[db actor] (fresh)
          before (store/ledger db)]
      (is (empty? before))
      (exec-op actor "x1" {:op :engagement/intake :subject "eng-1" :patch {:id "eng-1"}} operator)
      (let [after-1 (store/ledger db)]
        (is (= 1 (count after-1)))
        (is (= before (take (count before) after-1)) "prior facts untouched")
        (exec-op actor "x2" {:op :jurisdiction/assess :subject "eng-1" :no-spec? true} operator)
        (let [after-2 (store/ledger db)]
          (is (= 2 (count after-2)))
          (is (= after-1 (take (count after-1) after-2)) "earlier facts still present, unchanged, in order"))))))

(deftest interrupt-before-request-approval-pauses-the-actor
  (testing "the compiled graph's interrupt-before boundary actually pauses execution -- no record/ledger write happens until resume"
    (let [[db actor] (fresh)
          r1 (exec-op actor "t14" {:op :jurisdiction/assess :subject "eng-1"} operator)]
      (is (= :interrupted (:status r1)))
      (is (nil? (store/assessment-of db "eng-1")) "nothing committed while paused")
      (is (empty? (store/ledger db)) "no ledger fact written until the human decides")
      (approve! actor "t14")
      (is (some? (store/assessment-of db "eng-1")) "commits only after resume")
      (is (= 1 (count (store/ledger db)))))))
