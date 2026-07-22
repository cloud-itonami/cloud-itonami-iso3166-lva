(ns marketentry.facts
  "Per-jurisdiction public-procurement market-entry regulatory catalog
  for the Latvia market-entry actor -- the G2-style spec-basis table
  the Market-Entry Compliance Governor checks every
  `:jurisdiction/assess` proposal against ('did the advisor cite an
  OFFICIAL public source for this jurisdiction's requirements, or did
  it invent one?').

  This blueprint's own text (docs/business-model.md) names Latvia's
  real market-entry surface: registration with Uzņēmumu reģistrs
  (Register of Enterprises, a state institution under the Ministry of
  Justice), Publisko iepirkumu likums (PIL, Public Procurement Law)
  compliance under the oversight of Iepirkumu uzraudzības birojs (IUB,
  the Procurement Monitoring Bureau), registration on the EIS
  (Elektronisko iepirkumu sistēma) e-procurement platform, and VAT
  registration with Valsts ieņēmumu dienests (VID, the State Revenue
  Service).

  ** CRITICAL structural nuance this catalog exists to get right **:
  EIS (eis.gov.lv) is NOT operated by IUB. IUB is the legal/
  regulatory-oversight authority for public procurement (publishes
  notices, enforces PIL); the e-procurement TRANSACTIONAL PLATFORM
  itself is technically maintained by a DIFFERENT authority -- VDAA
  (Valsts digitālās attīstības aģentūra, State Digital Development
  Agency), under the Ministry of Smart Administration and Regional
  Development, live since 2009-06-01 (Cabinet of Ministers Order
  No. 220). VDAA was renamed from VRAA (State Regional Development
  Agency, est. 2007) in 2024. Most naive sources casually fuse 'IUB
  operates EIS' into one fact -- this catalog deliberately keeps the
  legal authority (`:platform-oversight-authority`) and the technical
  operator (`:platform-operator-authority`) as TWO SEPARATE fields,
  and `marketentry.governor`'s `platform-operator-fusion-violations`
  HARD-holds any proposal that collapses them.

  Coverage is reported HONESTLY (see `coverage`): a jurisdiction not in
  this table has NO spec-basis, full stop -- the advisor must not
  fabricate one, and the governor holds if it tries. The non-Latvia
  entries below (`USA`/`JPN`/`DEU`/`GBR`) are not fresh research for
  this repo -- they are copied verbatim from already-implemented
  sibling iso3166 actors (`cloud-itonami-iso3166-usa`/`-jpn`), kept
  here only as known-good fixtures for `coverage`/multi-jurisdiction
  tests, the same fleet-wide convention every sibling catalog follows.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` is the 4-item
  evidence checklist the governor's `evidence-incomplete-violations`
  checks against `marketentry.facts/required-evidence-satisfied?`; one
  item per distinct Latvian authority this blueprint touches --
  Uzņēmumu reģistrs (business registration), PIL/IUB (procurement-law
  compliance), EIS (platform registration, VDAA-operated), VID (VAT
  registration). `:legal-basis`/`:owner-authority`/`:provenance` are
  the G2 citation the governor requires before any
  `:jurisdiction/assess` proposal can commit. `:rep-*` carries the
  Uzņēmumu reģistrs business-registration citation (this vertical's
  analogue of a resident-representative requirement -- an entry must
  actually be on file with the Register of Enterprises).
  `:corporate-number-*` carries the VID/VAT citation.
  `:platform-oversight-authority`/`:platform-operator-authority`/
  `:platform-operator-note`/`:platform-operator-provenance` are the
  FLAGSHIP new field group for this vertical: they keep the legal
  oversight authority (IUB) and EIS's separate technical operator
  (VDAA) apart on purpose -- see the namespace docstring."
  {"LVA" {:name "Latvia"
          :owner-authority "Iepirkumu uzraudzības birojs (IUB) — Procurement Monitoring Bureau"
          :legal-basis "Publisko iepirkumu likums (PIL) — Public Procurement Law"
          :national-spec "EIS (Elektronisko iepirkumu sistēma) e-procurement registration + Uzņēmumu reģistrs business registration"
          :provenance "https://likumi.lv/"
          :required-evidence ["Uzņēmumu reģistrs (Register of Enterprises) business-registration record"
                              "Publisko iepirkumu likums (PIL) / IUB procurement-law compliance record"
                              "EIS (Elektronisko iepirkumu sistēma) platform-registration record"
                              "VID VAT registration record (VAT Register)"]
          :rep-owner-authority "Uzņēmumu reģistrs (Register of Enterprises), a state institution under the Ministry of Justice"
          :rep-legal-basis "Registration of the commercial company/merchant with the Register of Enterprises; official company data is open data (CC0, daily refresh)"
          :rep-provenance "https://ur.gov.lv/"
          :corporate-number-owner-authority "Valsts ieņēmumu dienests (VID) — State Revenue Service"
          :corporate-number-legal-basis "VAT Register; VAT number format LV + 11 digits; registration decision within 15 business days of application"
          :platform-oversight-authority "Iepirkumu uzraudzības birojs (IUB) — Procurement Monitoring Bureau"
          :platform-operator-authority "Valsts digitālās attīstības aģentūra (VDAA — State Digital Development Agency; renamed from VRAA, State Regional Development Agency, in 2024), under the Ministry of Smart Administration and Regional Development"
          :platform-operator-note "EIS (eis.gov.lv) is NOT operated by IUB. IUB is the legal/regulatory-oversight authority for public procurement; the e-procurement platform itself is technically maintained by the SEPARATE authority VDAA, live since 2009-06-01 (Cabinet of Ministers Order No. 220). VDAA was renamed from VRAA (State Regional Development Agency, est. 2007) in 2024. Do not fuse these into one fact."
          :platform-operator-provenance "https://www.eis.gov.lv/"}
   "USA" {:name "United States"
          :owner-authority "U.S. General Services Administration (GSA) / SAM.gov"
          :legal-basis "Federal Acquisition Regulation (FAR); System for Award Management"
          :national-spec "SAM.gov entity registration + Unique Entity ID (UEI) + NAICS self-certification"
          :provenance "https://sam.gov/"
          :required-evidence ["EIN record"
                              "SAM.gov registration record"
                              "State business registration record"
                              "SAM UEI verification record"]
          :rep-owner-authority "GSA / SAM.gov entity administrators"
          :rep-legal-basis "2 CFR / FAR entity validation — Unique Entity ID (UEI) required for federal awards"
          :rep-provenance "https://sam.gov/"
          :corporate-number-owner-authority "Internal Revenue Service (IRS)"
          :corporate-number-legal-basis "Employer Identification Number (EIN)"
          :corporate-number-provenance "https://www.irs.gov/businesses/small-businesses-self-employed/apply-for-an-employer-identification-number-ein-online"}
   "JPN" {:name "Japan"
          :owner-authority "デジタル庁 / 全省庁統一資格 審査機関"
          :legal-basis "全省庁統一資格 / GEPS"
          :national-spec "unified central-government tender qualification"
          :provenance "https://www.chotatujoho.go.jp/va/com/ShikakuTop.html"
          :required-evidence ["法人番号確認記録"
                              "全省庁統一資格申請記録"
                              "GEPS 事業者登録記録"
                              "日本居住代理人確認記録"]}
   "DEU" {:name "Germany"
          :owner-authority "e-Vergabe platforms"
          :legal-basis "GWB / VgV"
          :national-spec "e-Vergabe supplier registration"
          :provenance "https://www.evergabe-online.de/"
          :required-evidence ["Handelsregister extract"
                              "e-Vergabe registration record"
                              "USt-IdNr record"
                              "Authorized-representative record"]}
   "GBR" {:name "United Kingdom"
          :owner-authority "Crown Commercial Service / Find a Tender"
          :legal-basis "Public Contracts Regulations 2015"
          :national-spec "Find a Tender Service registration"
          :provenance "https://www.find-tender.service.gov.uk/"
          :required-evidence ["Companies House record"
                              "Find a Tender registration record"
                              "VAT registration record"
                              "Authorized-representative record"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO
  spec-basis, and the governor must hold any proposal that tries to
  assess or file on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions
  actually have a spec-basis entry. Never report a missing
  jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-lva R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog for market-entry navigation, "
                 "not a survey of all ~194 jurisdictions -- extend "
                 "`marketentry.facts/catalog`, never fabricate a "
                 "jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings)
  satisfy every evidence item listed for `iso3`? Missing spec-basis ->
  never satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))

(defn rep-spec-basis
  "The jurisdiction's local business-registration requirement map, or
  nil when this catalog has no such regime. For LVA this is the
  Uzņēmumu reģistrs (Register of Enterprises) requirement -- real and
  applicable to any commercial operator bidding into Latvian public
  procurement."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:rep-owner-authority sb)
      (select-keys sb [:rep-owner-authority :rep-legal-basis :rep-provenance]))))

(defn corporate-number-spec-basis
  "The jurisdiction's corporate-number / tax-id regime, or nil. For
  LVA this is the VID VAT Register (VAT number format LV + 11
  digits)."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:corporate-number-owner-authority sb)
      (select-keys sb [:corporate-number-owner-authority
                       :corporate-number-legal-basis
                       :corporate-number-provenance]))))

(defn platform-operator-spec-basis
  "The jurisdiction's e-procurement PLATFORM-OPERATOR citation, or
  nil -- the FLAGSHIP field group for this vertical. Keeps the legal/
  regulatory-oversight authority (`:platform-oversight-authority`)
  and the platform's separate technical operator
  (`:platform-operator-authority`) as two DISTINCT values so a
  consumer (governor, advisor, UI) is structurally prevented from
  collapsing them into one fused fact. For LVA: IUB is the oversight
  authority, VDAA (renamed from VRAA in 2024) is EIS's technical
  operator."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:platform-operator-authority sb)
      (select-keys sb [:platform-oversight-authority
                       :platform-operator-authority
                       :platform-operator-note
                       :platform-operator-provenance]))))
