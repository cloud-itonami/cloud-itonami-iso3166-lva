(ns statute.facts
  "General-law compliance catalog for Latvia (LVA) -- extends this repo's
  existing `marketentry.facts` (public-procurement market-entry only,
  narrow scope) with a second, orthogonal catalog of statutes a company
  operating in this jurisdiction must generally track for compliance.
  Mirrors cloud-itonami-iso3166-jpn/-usa/-est/-isl/-swe/-nor/-dnk/-fin's
  `statute.facts` (ADR-2607141700, cloud-itonami-compliance-fact-
  federation).

  Every entry cites an OFFICIAL likumi.lv URL -- the SAME authoritative
  legal-text portal `marketentry.facts` already cites as its
  `:provenance` for the whole LVA entry (`https://likumi.lv/`), operated
  by VSIA 'Latvijas Vēstnesis' (Latvia's official gazette publisher)
  under the Oficiālo publikāciju un tiesiskās informācijas likums
  (Official Publications and Legal Information Law) -- confirmed
  directly from likumi.lv's own `/par-mums` (about us) page, fetched
  this session. Unlike Estonia's riigiteataja.ee (an Angular SPA behind
  Cloudflare that returned only a loading shell to curl/WebFetch in the
  `cloud-itonami-iso3166-est` session, per that repo's `statute.facts`
  docstring), likumi.lv rendered directly to WebFetch with no
  bot-detection challenge encountered -- every citation below was
  fetched and read from likumi.lv itself, both the normal document page
  (`/ta/id/<id>-<slug>`) and its print-friendly metadata view
  (`/body_print.php?id=<id>&lang=lv`, which surfaces the Izdevējs
  [issuer] / Veids [type] / Pieņemts [adopted] / Stājas spēkā [entry
  into force] / Publicēts [published] fields verbatim for each
  document).

  ** Citation-convention note ** -- Latvia does not use a sequential
  Act-number registry the way Sweden cites 'SFS 2005:551' or Iceland
  cites 'Law No. 138/1994'; Latvian statutes are cited by title plus
  the Saeima's adoption date, and likumi.lv assigns its own internal
  document id (the numeric id in the citation URL below). Each entry's
  `:statute/law-number` therefore records the adoption date, entry-into-
  force date, likumi.lv document id, and Latvijas Vēstnesis/Ziņotājs
  publication reference actually read off likumi.lv -- not a fabricated
  sequential act number.

  ** Orthogonality with `marketentry.facts` ** -- nothing below repeats,
  duplicates, or contradicts `marketentry.facts`'s own citations for
  this jurisdiction: not the Publisko iepirkumu likums (PIL) procurement
  law itself, not IUB's (Iepirkumu uzraudzības birojs) role as the
  legal/regulatory-oversight authority, and NOT the deliberately-kept-
  separate EIS platform-operator fact (VDAA -- Valsts digitālās
  attīstības aģentūra, renamed from VRAA in 2024 -- operates EIS
  technically; IUB does not). This catalog is the orthogonal general-
  law axis (company/data/labor law), not the procurement-specific one.
  Where a real relationship exists it is BUILT ON, never re-derived:
  `lva.komerclikums` below is the general commercial-law foundation
  (Article 1 defines 'komersants' as a person/company entered in the
  commercial register) that `marketentry.facts`'s own
  `:rep-legal-basis` citation (registration with Uzņēmumu reģistrs, the
  Register of Enterprises) already assumes and administers -- this
  catalog cites the STATUTE that creates that registration requirement;
  `marketentry.facts` cites the REGISTRY BODY (UR, ur.gov.lv) that
  executes it. Two distinct, non-contradictory citations for the same
  underlying legal fact.

  A law not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of statute entries. `:statute/url` + `:statute/law-number`
  are the citation the governor requires before any compliance-fact
  proposal referencing this law can commit."
  {"LVA"
   [{:statute/id "lva.komerclikums"
     :statute/title "Komerclikums (Commercial Law)"
     :statute/jurisdiction "LVA"
     :statute/kind :law
     :statute/law-number "Adopted by the Saeima 13.04.2000, in force since 01.01.2002 (likumi.lv document id 5490); published in Latvijas Vēstnesis 158/160 (04.05.2000) and Latvijas Republikas Saeimas un Ministru Kabineta Ziņotājs 11 (01.06.2000), per likumi.lv's own print-metadata view. General commercial-law foundation for `marketentry.facts`'s Uzņēmumu reģistrs (Register of Enterprises) business-registration citation -- Article 1 defines a 'komersants' as a natural person or commercial company entered in the commercial register."
     :statute/url "https://likumi.lv/ta/id/5490-komerclikums"
     :statute/url-provenance :official-likumi-lv
     :statute/enacted-date "2002-01-01"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:corporate-governance :incorporation}}
    {:statute/id "lva.fizisko-personu-datu-apstrades-likums"
     :statute/title "Fizisko personu datu apstrādes likums (Personal Data Processing Law -- supplements EU Regulation 2016/679, GDPR, at the national level)"
     :statute/jurisdiction "LVA"
     :statute/kind :law
     :statute/law-number "Adopted by the Saeima 21.06.2018, in force since 05.07.2018 (likumi.lv document id 300099); published in Latvijas Vēstnesis 132 (04.07.2018), OP numurs 2018/132.1, per likumi.lv's own print-metadata view. This iteration independently confirmed Section 1's own text: 'The terms specified in Article 4 of the Regulation (EU) 2016/679 ... are used in the Law' -- the national law adopts GDPR's own terminology and operates within the GDPR framework, it does not restate GDPR's substantive rules."
     :statute/url "https://likumi.lv/ta/id/300099-fizisko-personu-datu-apstrades-likums"
     :statute/url-provenance :official-likumi-lv
     :statute/enacted-date "2018-07-05"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:data-protection :privacy}}
    {:statute/id "lva.darba-likums"
     :statute/title "Darba likums (Labour Law)"
     :statute/jurisdiction "LVA"
     :statute/kind :law
     :statute/law-number "Adopted by the Saeima 20.06.2001, in force since 01.06.2002 (likumi.lv document id 26019); published in Latvijas Vēstnesis 105 (06.07.2001) and Latvijas Republikas Saeimas un Ministru Kabineta Ziņotājs 15 (09.08.2001), per likumi.lv's own print-metadata view. This iteration independently confirmed Section 1 ('Employment relationship is governed by the Constitution ... this Law ... a collective agreement and working procedure regulations') and Section 2's scope (applies to all employers regardless of legal status, and to employees whose relationship with an employer is based on an employment contract; state/local-government officials with separately regulated compensation are exempt from most provisions)."
     :statute/url "https://likumi.lv/ta/id/26019-darba-likums"
     :statute/url-provenance :official-likumi-lv
     :statute/enacted-date "2002-06-01"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:labor :employment}}]})

(defn spec-basis
  "The jurisdiction's statute vector, or nil -- nil means NO spec-basis
  for that jurisdiction yet."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report, same shape/discipline as `marketentry.facts/coverage`:
  never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-lva statute.facts Wave 0 (ADR-2607141700): "
                 (count (get catalog "LVA")) " LVA statute(s) seeded with an "
                 "official likumi.lv citation. Extend `statute.facts/catalog`, "
                 "never fabricate a law-id or URL.")})))

(defn by-topic
  "Statutes for `iso3` tagged with `topic` (e.g. :labor, :data-protection)."
  [iso3 topic]
  (filterv #(contains? (:statute/topic %) topic) (spec-basis iso3)))
