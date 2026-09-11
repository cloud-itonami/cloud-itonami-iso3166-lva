# cloud-itonami-iso3166-lva

Open ISO 3166 Blueprint for **LVA**: Latvia.

This repository designs a forkable OSS business for an independent
public-sector market-entry consultant: an already-incorporated operator
(e.g. a `cloud-itonami-cofog-{code}`, `cloud-itonami-isco-{code}`,
`cloud-itonami-unspsc-{segment}` or `cloud-itonami-{ISIC}` blueprint
fork) gets a Compliance Advisor + independent **Market-Entry Compliance
Governor** to navigate public-procurement registration, local business/
tax registration, and EU single-market rules in Latvia, so the
operator can win and service a government contract without hiring a
full in-house compliance department.

## No robotics premise — digital/data service exemption

Market-entry and procurement-compliance navigation is a pure data/software
service with no physical-domain work (portal registration, document
checklists, regulatory-change monitoring) — the same exemption class as
`cloud-itonami-6310` (HR SaaS replacement) and `cloud-itonami-gtin-*`.
`blueprint.edn` sets `:itonami.blueprint/robotics false` and
`:required-technologies` lists only real capabilities (`:identity`,
`:forms`, `:dmn`, `:bpmn`, `:audit-ledger`), no `:robotics`.

## Core Contract

```text
operator intake + prior filing history
        |
        v
Compliance Advisor -> Market-Entry Compliance Governor -> filing draft, or human sign-off
        |
        v
gated portal registration / filing submission + audit ledger
```

No automated proposal can submit a portal registration or filing the
governor refuses, suppress a compliance record, or claim a legal/tax
conclusion the governor has not cleared. `:filing/submit` is never in any
phase's `:auto` set — it always requires human sign-off (mirrors
`cloud-itonami-M6910`'s `filing-submit-never-auto-at-any-phase`
invariant).

## What this is NOT

- **Not the government of Latvia.** See
  [`docs/business-model.md`](docs/business-model.md) for the boundary with
  `com-etzhayyim-ooyake` (read-only civic mirror), `matsurigoto` (sovereign
  statecraft), `com-etzhayyim-toritsugi` (individual citizen concierge),
  `legal-entity.etzhayyim.com` (read-only data aggregation), and
  `cloud-itonami-M6910` (company incorporation — a different regulatory
  phase this blueprint assumes is already complete).
- **Not legal or tax advice.** Every regulatory claim must cite the
  official source and route final filings to Latvian-licensed counsel
  or a registered agent where the law requires licensed representation.

## Capability layer

Resolves via [`kotoba-lang/iso3166`](https://github.com/kotoba-lang/iso3166)
(ISO 3166 `LVA`). Required capabilities:

- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Implementation status

**`:implemented`.** `src/marketentry/*` is a running langgraph-clj
StateGraph actor (`operation/build`): a MarketEntry-LLM advisor
(`marketentryllm.cljc`) sealed into a single `:advise` node, whose
proposal is ALWAYS routed through the Market-Entry Compliance
Governor (`governor.cljc`) and the rollout phase gate (`phase.cljc`)
before anything touches the SSoT (`store.cljc`, MemStore +
DatomicStore via `io.github.kotoba-lang/langchain-store`).

```
kbb -M:dev:test    # governor contract + facts + phase + registry + store
kbb -M:dev:run     # walk a demo engagement through the full actor graph
```

### Governor checks (priority order, all HARD -- unoverridable by a human approver)

| # | Check | Grounded in |
|---|-------|-------------|
| 1 | Spec-basis (no fabricated jurisdiction) | `marketentry.facts/spec-basis` |
| 2 | Evidence incomplete | the jurisdiction's 4-item `:required-evidence` checklist |
| 3 | `:ur-registration-missing` | Uzņēmumu reģistrs (Register of Enterprises), state institution under the Ministry of Justice — [ur.gov.lv](https://ur.gov.lv/) |
| 4 | `:platform-operator-fused` (**flagship**) | EIS regulator/operator split — see below |
| 5 | Engagement-fee mismatch | independent recompute (`base-fee + monthly-rate x monitoring-months`) |
| 6 | `:vat-unverified` | Valsts ieņēmumu dienests (VID), State Revenue Service — VAT Register, VAT number format `LV` + 11 digits |
| 7 | Confidence floor / actuation gate | `:filing/draft`/`:filing/submit` always escalate |
| — | Double-draft / double-submit guards | dedicated `:drafted?`/`:submitted?` facts |

### The flagship check: IUB is not VDAA

The central fabrication trap for this jurisdiction is collapsing two
different authorities into one. This actor keeps them apart on
purpose, in the catalog (`marketentry.facts/platform-operator-spec-basis`)
and in a dedicated governor check
(`marketentry.governor/platform-operator-fusion-violations`):

- **IUB** (Iepirkumu uzraudzības birojs — Procurement Monitoring
  Bureau) is the **legal/regulatory-oversight authority**: it
  publishes procurement notices and enforces Publisko iepirkumu
  likums (PIL, Public Procurement Law, current text on
  [likumi.lv](https://likumi.lv/)). 2024 total procurement volume:
  €5.45bn, ~13% of GDP.
- **EIS** (Elektronisko iepirkumu sistēma), the transactional
  e-procurement platform at [eis.gov.lv](https://www.eis.gov.lv/), is
  **NOT operated by IUB**. It is technically maintained by **VDAA**
  (Valsts digitālās attīstības aģentūra — State Digital Development
  Agency), under the Ministry of Smart Administration and Regional
  Development, live since 2009-06-01 (Cabinet of Ministers Order
  No. 220). VDAA was renamed from **VRAA** (State Regional
  Development Agency, est. 2007) in **2024**.

A `:jurisdiction/assess` proposal that states or implies "IUB
operates EIS" -- omits the distinction, fuses the two authorities into
one value, or cites either against the wrong catalogued value -- is a
HARD violation the governor rejects unconditionally
(`test/marketentry/governor_contract_test.cljk`'s
`fused-platform-operator-claim-is-held-and-unoverridable` and
`clean-assess-correctly-distinguishes-iub-from-vdaa`).

### Sources cited per check

- Business registration: Uzņēmumu reģistrs (Register of Enterprises),
  state institution under the Ministry of Justice; free official
  company data (open data, CC0, daily refresh) — [ur.gov.lv](https://ur.gov.lv/)
- Procurement law: Publisko iepirkumu likums (PIL), current text —
  [likumi.lv](https://likumi.lv/); oversight/enforcement — Iepirkumu
  uzraudzības birojs (IUB)
- E-procurement platform: EIS (Elektronisko iepirkumu sistēma) —
  [eis.gov.lv](https://www.eis.gov.lv/), technically operated by VDAA
  (renamed from VRAA in 2024)
- Tax/VAT registration: Valsts ieņēmumu dienests (VID), State Revenue
  Service — VAT Register, VAT number format `LV` + 11 digits,
  registration decision within 15 business days of application

### Actuation

- `:engagement/intake` may auto-commit at phase 3 when the governor is
  clean (no portal-facing risk).
- `:jurisdiction/assess` always escalates to human approval, at every
  phase, even when clean.
- `:filing/draft` and `:filing/submit` are **permanently excluded**
  from every phase's `:auto` set (`phase.cljc`) AND are members of the
  governor's own `high-stakes` set (`governor.cljc`) that forces
  escalation independently of phase. Two layers, not one, agree that
  drafting a real EIS portal package or submitting a real EIS portal
  registration is always a human market-entry operator's call.
- Every HARD violation is unoverridable: a human approver sees the
  `:hold` disposition and its `:violations`, but cannot commit past a
  HARD check. Only the confidence/actuation escalation is a genuine
  human decision point (`:approved`/rejected via `:request-approval`).
- Every commit or hold appends exactly one fact to the append-only
  ledger (`store/append-ledger!`, called from both the `:commit` and
  `:hold` StateGraph nodes) — nothing is ever rewritten or removed.

## License

AGPL-3.0-or-later.

## Statute catalog

Alongside `marketentry.facts` (public-procurement market-entry only,
narrow scope), this repo carries a **general-law compliance catalog**
(ADR-2607141700, `cloud-itonami-compliance-fact-federation`) — statutes
a company generally must track for compliance, orthogonal to the
procurement-specific facts above:

| Topic | Law | Source |
|---|---|---|
| corporate-governance / incorporation | Komerclikums (Commercial Law), adopted 13.04.2000, in force since 01.01.2002 | https://likumi.lv/ta/id/5490-komerclikums |
| data-protection / privacy | Fizisko personu datu apstrādes likums (Personal Data Processing Law, supplements EU Regulation 2016/679 GDPR), adopted 21.06.2018, in force since 05.07.2018 | https://likumi.lv/ta/id/300099-fizisko-personu-datu-apstrades-likums |
| labor / employment | Darba likums (Labour Law), adopted 20.06.2001, in force since 01.06.2002 | https://likumi.lv/ta/id/26019-darba-likums |

- `src/statute/facts.cljk` — the catalog, source of truth.
- `schema/statute.edn` — DataScript schema.
- `data/datascript-tx.edn` — derived DataScript tx-data (regenerated
  from the catalog, never hand-edited).

This catalog is orthogonal to `marketentry.facts` and never repeats,
duplicates, or contradicts it: not the Publisko iepirkumu likums (PIL)
procurement law, not IUB's (Iepirkumu uzraudzības birojs) role as the
legal/regulatory-oversight authority, and NOT the deliberately-kept-
separate EIS platform-operator fact (VDAA — Valsts digitālās
attīstības aģentūra, renamed from VRAA in 2024 — operates EIS
technically; IUB does not) — that flagship IUB/VDAA distinction stays
exactly where `marketentry.facts`/`marketentry.governor` already own
it, and nothing here restates or re-fuses it. Where a real relationship
exists it is built on, not re-derived: `lva.komerclikums` (Article 1
defines a 'komersants' as a person/company entered in the commercial
register) is the general-law foundation that `marketentry.facts`'s own
Uzņēmumu reģistrs (Register of Enterprises) business-registration
citation already assumes and administers — this catalog cites the
STATUTE that creates the registration requirement; `marketentry.facts`
cites the REGISTRY BODY (UR, ur.gov.lv) that executes it. Same
provenance discipline as every catalog in this repo: every entry cites
an official source (likumi.lv, operated by VSIA 'Latvijas Vēstnesis'
under the Oficiālo publikāciju un tiesiskās informācijas likums) that
was actually fetched and read, never invented. An item not in
`statute.facts/catalog` has no spec-basis — extend the catalog, never
fabricate an id/url.

## Culture catalog

Alongside the market-entry / statute catalogs, this repo carries a
**country-level regional-culture catalog** (ADR-2607171400 addendum 2,
`cloud-itonami-municipality-culture-catalog` Wave 1, in
`com-junkawasaki/root`) — national dishes, protected products, beverages,
crafts, festivals and heritage sites for Latvia:

- `src/culture/facts.cljk` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.
