(ns marketentry.governor
  "Market-Entry Compliance Governor -- the independent compliance layer
  that earns the MarketEntry-LLM the right to commit. The LLM has no
  notion of jurisdictional procurement law, whether Latvian business
  registration (Uzņēmumu reģistrs) is actually on file, whether the
  EIS e-procurement platform's separate legal-oversight and technical-
  operator authorities have been kept distinct or fused into one
  fabricated fact, whether a claimed engagement fee actually equals
  base + months x rate, whether a VAT registration has been verified
  for a filing that requires it, or when a draft stops being a draft
  and becomes a real-world portal submission, so this MUST be a
  separate system able to *reject* a proposal and fall back to HOLD.

  `:itonami.blueprint/governor` is `:market-entry-compliance-governor`
  (shared family keyword on blueprints; this is one of several
  *running* implementations of that governor across the iso3166
  family).

  This blueprint's own text (docs/business-model.md Trust Controls:
  'any actual portal registration or filing submission requires
  Market-Entry Compliance Governor clearance and always escalates to
  human sign-off'; 'a false or fabricated regulatory-requirement claim
  is a HARD hold') names exactly the checks below.

  Eight checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them. The confidence/actuation gate is
  SOFT: it asks a human to look (low confidence / actuation), and the
  human may approve -- but see `marketentry.phase`: for `:stake
  :actuation/draft-filing`/`:actuation/submit-filing` NO phase ever
  allows auto-commit either. Two independent layers agree that
  actuation is always a human call.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source
                                       (`marketentry.facts`), or invent
                                       one?
    2. Evidence incomplete         -- for `:filing/draft`/
                                       `:filing/submit`, has the
                                       jurisdiction actually been
                                       assessed with a full evidence
                                       checklist on file?
    3. UR-registration missing     -- for `:filing/submit`, when the
                                       engagement declares
                                       `:requires-ur-registration?
                                       true`, INDEPENDENTLY verify
                                       `:has-ur-registration?` is
                                       true. Grounded in Uzņēmumu
                                       reģistrs (Register of
                                       Enterprises) business
                                       registration -- the state
                                       institution under the Ministry
                                       of Justice that registers
                                       Latvian commercial companies
                                       and merchants.
    4. Platform-operator fusion    -- for `:jurisdiction/assess`, when
                                       the jurisdiction has a distinct
                                       platform-operator spec-basis on
                                       file, INDEPENDENTLY verify the
                                       proposal keeps the legal/
                                       regulatory-oversight authority
                                       (IUB) and the e-procurement
                                       platform's separate technical
                                       operator (VDAA, renamed from
                                       VRAA in 2024) DISTINCT -- never
                                       collapses them into one fused
                                       'IUB operates EIS' fact.
                                       FLAGSHIP genuinely new check for
                                       the iso3166 family (grep-
                                       verified absent as a governor
                                       check function name fleet-wide
                                       at build time) -- this is the
                                       central fabrication trap for
                                       this jurisdiction: most naive
                                       sources casually fuse these two
                                       authorities.
    5. Engagement fee mismatch     -- for `:filing/submit`,
                                       INDEPENDENTLY recompute whether
                                       the engagement's own `:claimed-
                                       fee` equals `base-fee +
                                       monthly-rate x monitoring-
                                       months` -- honest reapplication
                                       of the ground-truth-recompute
                                       discipline sibling actors use.
    6. VAT unverified              -- for `:filing/submit`, when the
                                       engagement declares
                                       `:requires-vat-registration?
                                       true`, INDEPENDENTLY check
                                       `:vat-registration-verified?`.
                                       Grounded in VID (Valsts
                                       ieņēmumu dienests, State
                                       Revenue Service) VAT Register.
                                       CONDITIONAL on the engagement's
                                       own ground truth.
    7. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:filing/draft`/
                                       `:filing/submit` (REAL acts)
                                       -> escalate.

  Two more guards, double-draft/double-submit prevention, are enforced
  off dedicated `:drafted?`/`:submitted?` facts (never a `:status`
  value)."
  (:require [marketentry.facts :as facts]
            [marketentry.registry :as registry]
            [marketentry.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Drafting a real portal package and submitting a real portal
  registration are the two real-world actuation events this actor
  performs."
  #{:actuation/draft-filing :actuation/submit-filing})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:jurisdiction/assess` (or `:filing/draft`/`:filing/submit`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's market-entry requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :filing/draft :filing/submit} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:filing/draft`/`:filing/submit`, the jurisdiction's required
  registration evidence must actually be satisfied."
  [{:keys [op subject]} st]
  (when (contains? #{:filing/draft :filing/submit} op)
    (let [e (store/engagement st subject)
          assessment (store/assessment-of st subject)]
      (when-not (and assessment
                     (facts/required-evidence-satisfied?
                      (:jurisdiction e) (:checklist assessment)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(Uzņēmumu reģistrs登録/PIL・IUB遵守/EIS登録/VID VAT登録等)が充足していない状態での提案"}]))))

(defn- ur-registration-missing-violations
  "For `:filing/submit`, when the engagement declares
  `:requires-ur-registration? true`, INDEPENDENTLY verify
  `:has-ur-registration?` is true. Grounded in Uzņēmumu reģistrs
  (Register of Enterprises) -- the state institution under the
  Ministry of Justice that registers Latvian commercial companies and
  merchants. CONDITIONAL on the engagement's own
  `:requires-ur-registration?` ground truth."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (true? (:requires-ur-registration? e))
                 (not (true? (:has-ur-registration? e))))
        [{:rule :ur-registration-missing
          :detail (str subject " はUzņēmumu reģistrs(企業登記簿)への登録確認を要するが未確認 -- 提出提案は進められない")}]))))

(defn- platform-operator-fusion-violations
  "For `:jurisdiction/assess`, when the jurisdiction has a distinct
  platform-operator spec-basis on file (`marketentry.facts/
  platform-operator-spec-basis`), INDEPENDENTLY verify the proposal's
  own claim keeps the legal/regulatory-oversight authority and the
  e-procurement platform's separate technical operator DISTINCT. For
  LVA: IUB (Iepirkumu uzraudzības birojs) is the legal/oversight
  authority; VDAA (Valsts digitālās attīstības aģentūra, renamed from
  VRAA in 2024) is EIS's separate technical operator. A proposal that
  collapses them into a single fused authority (the near-universal
  naive-source mistake for this jurisdiction: 'IUB operates EIS'), or
  omits one, or cites either against the wrong catalogued value, is a
  HARD violation -- FLAGSHIP genuinely new check for the iso3166
  family."
  [{:keys [op]} proposal]
  (when (= op :jurisdiction/assess)
    (let [value (:value proposal)
          iso3 (:jurisdiction value)
          pob (facts/platform-operator-spec-basis iso3)]
      (when pob
        (let [legal (:platform-legal-authority value)
              operator (:platform-technical-operator value)]
          (when (or (nil? legal)
                    (nil? operator)
                    (= legal operator)
                    (not= legal (:platform-oversight-authority pob))
                    (not= operator (:platform-operator-authority pob)))
            [{:rule :platform-operator-fused
              :detail (str iso3 " のEISプラットフォーム運営主体の記載が法的監督機関(IUB)と技術運営者(VDAA)を"
                          "混同しているか、未記載/カタログ値と不一致 -- 別個の主体として検証できない")}]))))))

(defn- engagement-fee-mismatch-violations
  "For `:filing/submit`, INDEPENDENTLY recompute whether the
  engagement's own claimed fee equals base + months x rate."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when-not (registry/engagement-fee-matches-claim? e)
        [{:rule :engagement-fee-mismatch
          :detail (str subject " の申告手数料(" (:claimed-fee e)
                      ")が独立再計算値(" (registry/compute-engagement-fee e) ")と一致しない")}]))))

(defn- vat-unverified-violations
  "For `:filing/submit`, when the engagement declares
  `:requires-vat-registration? true`, INDEPENDENTLY check
  `:vat-registration-verified?` -- grounded in VID (Valsts ieņēmumu
  dienests, State Revenue Service) VAT Register. CONDITIONAL on the
  engagement's own ground truth."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (true? (:requires-vat-registration? e))
                 (not (true? (:vat-registration-verified? e))))
        [{:rule :vat-unverified
          :detail (str subject " はVID VAT登録確認を要するが未確認 -- 提出提案は進められない")}]))))

(defn- already-drafted-violations
  "For `:filing/draft`, refuses to draft the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/draft)
    (when (store/engagement-already-drafted? st subject)
      [{:rule :already-drafted
        :detail (str subject " は既にドラフト済み")}])))

(defn- already-submitted-violations
  "For `:filing/submit`, refuses to submit the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (when (store/engagement-already-submitted? st subject)
      [{:rule :already-submitted
        :detail (str subject " は既に提出済み")}])))

(defn check
  "Censors a MarketEntry-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (ur-registration-missing-violations request st)
                           (platform-operator-fusion-violations request proposal)
                           (engagement-fee-mismatch-violations request st)
                           (vat-unverified-violations request st)
                           (already-drafted-violations request st)
                           (already-submitted-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
