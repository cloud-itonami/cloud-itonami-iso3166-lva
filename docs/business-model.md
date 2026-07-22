# Business Model: Independent Public-Sector Market-Entry & Procurement Compliance Service — Latvia

## Classification

- Repository: `cloud-itonami-iso3166-lva`
- ISO 3166: `LVA` (Latvia)
- Activity: public-procurement market-entry and ongoing regulatory-
  compliance navigation for an already-incorporated operator
- Social impact: [:eu-single-market-access :public-spend-transparency :cross-border-friction-reduction]

## Customer

- an already-incorporated `cloud-itonami-cofog-{code}` /
  `cloud-itonami-isco-{code}` / `cloud-itonami-unspsc-{segment}` /
  `cloud-itonami-{ISIC}` operator wanting to bid on a Latvian
  public contract
- a foreign SME or civic-tech vendor entering the public sector in
  Latvia for the first time
- a `cloud-itonami-M6910` client that has just completed incorporation and
  now needs public-sector market access

## Offer

- registration walkthrough for the Electronic Procurement System (EIS,
  eis.gov.lv), where most Latvian public tenders are conducted, plus
  procurement-law compliance and notice monitoring under IUB
  (Iepirkumu uzraudzības birojs / Procurement Monitoring Bureau) —
  **note**: IUB is the legal/regulatory-oversight authority; EIS
  itself is technically operated by a separate agency, VDAA (Valsts
  digitālās attīstības aģentūra, renamed from VRAA in 2024) — this
  blueprint's Market-Entry Compliance Governor keeps that distinction
  explicit rather than fusing the two (see README Implementation
  status)
- business/tax registration checklist: an entry in the Register of
  Enterprises (Uzņēmumu reģistrs, UR, ur.gov.lv), a state institution
  providing free official company information
- local-content / preferential-procurement navigation: EU-wide open
  tendering above EU thresholds (no national-content quota as an EU
  member state), but Latvian-language submission is typically required
- ongoing regulatory-change monitoring subscription
- compliance-audit export package for the client's own records

## Revenue

- per-engagement market-entry fee (one-time registration + checklist
  completion)
- recurring regulatory-change monitoring subscription
- compliance-audit export package

## Trust Controls

- any actual portal registration or filing submission requires
  Market-Entry Compliance Governor clearance and always escalates to
  human sign-off (`:filing/submit` is never automated at any phase)
- a false or fabricated regulatory-requirement claim is a HARD hold that
  cannot be overridden by human approval alone — it must be corrected
  against a cited official source first
- this service does **not** provide legal or tax advice; characterization
  and filing on the client's behalf beyond checklist/draft assistance
  routes to Latvian-licensed counsel or a registered agent
- every requirement cites the official portal or regulation, never
  invented

## Boundary with adjacent actors (read before forking)

- **`com-etzhayyim-ooyake`** (etzhayyim/root): read-only civic-wayfinding
  mirror of government structure, non-commercial, barred from acting as
  or for the government (G3 impersonation ban). This blueprint is
  commercial and never claims to be an official channel.
- **`matsurigoto`** (etzhayyim/root): sovereign e-government statecraft —
  literally the government, for etzhayyim's own covenant or an adopting
  nation-state. This blueprint is an independent operator the government
  contracts with or that bids into its procurement — never the
  government.
- **`com-etzhayyim-toritsugi`** (etzhayyim/root): guides a consenting
  INDIVIDUAL citizen through their OWN procedure, non-profit,
  donation-only. This blueprint's client is a business operator, not an
  individual citizen, and it is commercial.
- **`legal-entity.etzhayyim.com`**: read-only aggregated company-registry
  data, no execution. This blueprint executes (gated) registrations.
- **`cloud-itonami-M6910`**: helps a client BECOME a legal entity
  (incorporation, ISIC 6910) — a prior, different regulatory phase
  (company law). This blueprint assumes incorporation is already done and
  handles public-procurement market entry (a different regulatory domain).
- **`cloud-itonami-cofog-{code}`**: a jurisdiction-agnostic operator
  template for ONE public function. This blueprint is the orthogonal
  jurisdiction-specific axis — the two compose (fork a COFOG-function
  blueprint AND this one to operate in Latvia).
